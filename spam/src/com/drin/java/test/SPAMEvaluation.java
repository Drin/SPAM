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
import com.drin.java.ontology.Labelable;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;
import com.drin.java.clustering.ClusterResults;

import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import com.drin.java.util.Configuration;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import java.io.File;

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
@SuppressWarnings("unused")
public class SPAMEvaluation {
   private static final String CONFIG_DIR = "configs",
                               ONT_DIR    = "ontologies",
                               USER_DIR   = System.getProperty("user.dir"),
                               FILE_SEP   = System.getProperty("file.separator");

   private CPLOPConnection mConn;
   private Configuration mConfig;
   private Clusterer mClusterer;
   private Ontology mOntology;
   private DataMetric<Cluster> mClustMetric;
   private Random mRand;

   private int mSQLRandSeed;

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public SPAMEvaluation(Configuration config, Ontology ontology) {
      mConn = CPLOPConnection.getConnection();
      mRand = new Random();
      mConfig = config;
      mOntology = ontology;
      List<Double> threshList = new ArrayList<Double>();

      threshList.add(new Double(mConfig.getRegionAttr("16-23", Configuration.ALPHA_KEY)));
      threshList.add(new Double(mConfig.getRegionAttr("16-23", Configuration.BETA_KEY)));

      mClusterer = new OHClusterer(ontology, threshList);

      try {
         mClustMetric = (DataMetric) Class.forName(
            mConfig.getMetric(Configuration.CLUSTER_KEY)
         ).newInstance();
      }
      catch (Exception err) {
         err.printStackTrace();
      }
   }

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

         System.out.println("Loading config file " + configFile.getName());
         config = Configuration.loadConfig(configFile);
         String ontFileName = config.getAttr(Configuration.ONT_KEY);

         if (ontFileName != null) {
            ont = Ontology.createOntology(new File(String.format(
               "%s%s%s%s%s", USER_DIR, FILE_SEP, ONT_DIR, FILE_SEP, ontFileName
            )));
         }

         SPAMEvaluation evaluator = new SPAMEvaluation(config, ont);
         evaluator.runTests();
      }
   }

   public ClusterResults runSingle(List<Cluster> clusterList) {
      long startTime = System.currentTimeMillis();
      mClusterer.clusterData(clusterList);
      long finishTime = System.currentTimeMillis();

      return new ClusterResults(mClusterer.getClusters(), finishTime - startTime);

   }

   /*
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
   */

   public void runTests() {
      List<ClusterResults> resultList = new ArrayList<ClusterResults>();
      List<Clusterable<?>> dataList = null;
      List<Cluster> clusterList = null;

      int initialSize = Integer.parseInt(mConfig.getAttr("initialSize"));
      int updateSize  = Integer.parseInt(mConfig.getAttr("updateSize"));
      int currUpdate = 0, numUpdates  = Integer.parseInt(mConfig.getAttr("numUpdates"));
      int pageSize, pageOffset;

      try {
         mConn.randomizeIsolates(mRand.nextInt(9999));
      }
      catch (java.sql.SQLException sqlErr) {
         sqlErr.printStackTrace();
         System.exit(1);
      }

      while (currUpdate <= numUpdates) {
         clusterList = new ArrayList<Cluster>();

         if (currUpdate == 0) {
            pageSize = initialSize;
            pageOffset = 0;
         }
         else {
            pageSize = updateSize;
            pageOffset = (updateSize * currUpdate) + initialSize;
         }

         dataList = constructIsolates(mConfig, mOntology, pageSize, pageOffset);

         for (Clusterable<?> data : dataList) {
            /*
            System.out.printf("Isolate [%s] has labels:\n", data.getName());

            if (data instanceof Labelable) {
               Labelable dataLabel = (Labelable) data;
               for (Map.Entry<String, Boolean> label : dataLabel.getLabels().entrySet()) {
                  System.out.printf("\t%s\n", label.getKey());
               }
            }
            */

            clusterList.add(new HCluster(mClustMetric, data));
         }

         //TODO persist cluster results
         resultList.add(runSingle(clusterList));

         currUpdate++;
      }
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private List<Clusterable<?>> constructIsolates(Configuration config, Ontology ont,
                                                  int pageSize, int pageOffset) {
      List<Clusterable<?>> dataList = new ArrayList<Clusterable<?>>();
      List<Map<String, Object>> rawDataList = null;

      DataMetric<Isolate> isoMetric = null;
      DataMetric<ITSRegion> regionMetric = null;
      DataMetric<Pyroprint> pyroMetric = null;

      /*
       * query CPLOP for data
       */
      try { rawDataList = mConn.getRandomIsolateData(ont, pageSize, pageOffset); }
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

      System.out.printf("finished processing histogram records into a list of %d isolates\n", dataList.size());

      return dataList;
   }
}
