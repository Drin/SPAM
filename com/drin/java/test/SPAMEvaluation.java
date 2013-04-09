package com.drin.java.test;

import com.drin.java.database.CPLOPConnection;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ITSRegionAverageMetric;
import com.drin.java.metrics.ITSRegionMedianMetric;
import com.drin.java.metrics.IsolateAverageMetric;
import com.drin.java.metrics.PyroprintUnstablePearsonMetric;
import com.drin.java.metrics.ClusterAverageMetric;

import com.drin.java.ontology.Ontology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;
import com.drin.java.clustering.ClusterResults;

import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import com.drin.java.output.ClusterWriter;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * SPAMEvaluation serves as a test harness for SPAM and various results
 * reported by any particular analysis method. SPAMEvaluation, being a test
 * harness, will handle instantiation of an analysis method and the execution
 * thereof. I think the best approach would be to allow the test harness to
 * automatically do parameter tuning, or execute its test with a single
 * parameter configuration. Whether parameter tuning occurs or not will be a
 * parameter to the test harness. Another parameter to the test harness will be
 * the data itself. However, the test harness will have to transform the data
 * into the appropriate representation given the choice of analysis method.
 * Finally, an important parameter will be what analysis method to use. This
 * will be selected from a list, and by default will try everything in the
 * list. I believe that the best choice would be for all analysis methods,
 * with automatic parameter tuning, to be tested on a given dataset and their
 * results automatically compared and reported. If otherwise specified, then
 * the test harness can execute a particular analysis method or instantiate
 * a particular configuration.
 */
public class SPAMEvaluation {
   private static final String CONFIG_DIR = "configs",
                               ONT_DIR    = "ontologies",
                               USER_DIR   = System.getProperty("user.dir"),
                               FILE_SEP   = System.getProperty("file.separator");

   private Configuration mConfig;
   private Clusterer mClusterer;
   private Ontology mOntology;
   private DataMetric<Cluster> mClustMetric;

   public SPAMEvaluation(Configuration config, Ontology ontology) {
      mConfig = config;
      mOntology = ontology;
      List<Double> threshList = new ArrayList<Double>();

      threshList.add(new Double(mConfig.getRegionAttr("16-23", Configuration.ALPHA_KEY)));
      threshList.add(new Double(mConfig.getRegionAttr("16-23", Configuration.BETA_KEY)));

      mClusterer = new OHClusterer(ont, threshList);

      try {
         mClustMetric = (DataMetric) Class.forName(
            mConfig.getMetric(Configuration.CLUSTER_KEY)
         ).newInstance();
      }
      catch (Exception err) {
         err.printStackTrace();
      }
   }

   @SuppressWarnings("unchecked")
   public static void main(String[] args) {
      File configDir = new File(CONFIG_DIR);
      Configuration config = null;
      Ontology ont = null;

      if (!configDir.exists() || !configDir.isDirectory()) {
         System.err.println("Invalid configuration directory!");
         System.exit(1);
      }

      for (File configFile : configDir.listFiles()) {
         if (configFile != null && (!configFile.exists() || !configFile.isFile())) {
            continue;
         }

         config = Configuration.loadConfig(configFile);
         String ontFileName = mConfig.getAttr("ontology");

         if (ontFileName != null) {
            ont = Ontology.createOntology(new File(String.format(
               "%s%s%s%s%s", USER_DIR, FILE_SEP, ONT_DIR, FILE_SEP, ontFileName
            )));
         }

         SPAMEvaluation evaluator = new SPAMEvaluation(config, ont);
         evaluator.runTests();
      }
   }

   public void runTests() {
      long startQueryTime = 0, startClusterTime = 0, finishTime = 0;
      List<Clusterable<?>> dataList = null;
      List<Cluster> clusterList = null;

      startQueryTime = System.currentTimeMillis();

      dataList = constructIsolates(mConfiguration, mOntology);

      clusterList = new ArrayList<Cluster>();
      if (dataList != null) {
         for (Clusterable<?> data : dataList) {
            clusterList.add(new HCluster(mClustMetric, data));
         }
      }

      startClusterTime = System.currentTimeMillis();
      mClusterer.clusterData(clusterList);
      finishTime = System.currentTimeMillis();

      String timeOutput = String.format(
         "Time to query Database: %d\n" +
         "Time to cluster data:   %d\n",
         (startClusterTime - startQueryTime),
         (finishTime - startClusterTime)
      );

      String outFile = "HClust_Test";
      if (mOntology != null) { outFile = "OHClust_Test_3_layer"; }

      try {
         ClusterWriter writer = new ClusterWriter(mClusterer.getClusters());

         writer.writeData(outFile + ClusterWriter.FileType.CSV, timeOutput + writer.getClustInfo());
         writer.writeData(outFile + ClusterWriter.FileType.XML, writer.getDendInfo());
      }
      catch(Exception err) {
         err.printStackTrace();
      }
   }

   @SuppressWarnings("unchecked")
   private static List<Clusterable<?>> constructIsolates(Configuration config, Ontology ont) {
      CPLOPConnection conn = CPLOPConnection.getConnection();
      List<Clusterable<?>> dataList = new ArrayList<Clusterable<?>>();
      List<Map<String, Object>> rawDataList = null;

      DataMetric<Isolate> isoMetric = null;
      DataMetric<ITSRegion> regionMetric = null;
      DataMetric<Pyroprint> pyroMetric = null;

      /*
       * query CPLOP for data
       */
      try { rawDataList = conn.getDataByIsoID(ont, null); }
      catch (java.sql.SQLException sqlErr) {
         sqlErr.printStackTrace();
         System.exit(1);
      }

      /*
       * construct object representations
       */
      try {
         isoMetric = (DataMetric) Class.forName(config.getMetric(Configuration.ISOLATE_KEY)).newInstance();
         regionMetric = (DataMetric) Class.forName(config.getMetric(Configuration.ITSREGION_KEY)).newInstance();
         pyroMetric = (DataMetric) Class.forName(config.getMetric(Configuration.PYROPRINT_KEY)).newInstance();
      }
      catch(Exception err) {
         err.printStackTrace();
         System.err.println("fail!");
         System.exit(1);
      }

      Isolate tmpIso = null;
      Pyroprint tmpPyro = null;
      for (Map<String, Object> dataMap : rawDataList) {
         String wellID     = String.valueOf(dataMap.get("well"));
         String isoID      = String.valueOf(dataMap.get("isolate"));
         String regName    = String.valueOf(dataMap.get("region"));
         Integer pyroID    = new Integer(String.valueOf(dataMap.get("pyroprint")));

         String nucleotide = String.valueOf(dataMap.get("nucleotide"));
         double peakHeight = Double.parseDouble(String.valueOf(dataMap.get("pHeight")));

         String pyroName = String.format("%d (%s)", pyroID.intValue(), wellID);

         //Retrieve Isolate
         if (tmpIso == null || !tmpIso.getName().equals(isoID)) {
            tmpIso = new Isolate(isoID, isoMetric);

            if (ont != null) {
               for (Map.Entry<String, Set<String>> tableCols : ont.getTableColumns().entrySet()) {
                  for (String colName : tableCols.getValue()) {
                     if (colName.replace(" ", "").equals("")) { continue; }

                     tmpIso.addLabel(String.valueOf(dataMap.get(colName)));
                  }
               }
            }

            dataList.add(tmpIso);
         }

         if (tmpIso != null) {
            tmpIso.getData().add(new ITSRegion(regName, regionMetric));
         }

         if (tmpPyro == null || !tmpPyro.getName().equals(pyroName)) {
            tmpPyro = new Pyroprint(pyroID.intValue(), wellID, pyroMetric);
            tmpIso.getRegion(regName).add(tmpPyro);
         }

         if (tmpPyro.getName().equals(pyroName) && tmpPyro.getDispLen() <
             Integer.parseInt(config.getRegionAttr(regName, Configuration.LENGTH_KEY))) {
               tmpPyro.addDispensation(nucleotide, peakHeight);
         }
      }

      return dataList;
   }
}