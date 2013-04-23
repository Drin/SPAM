package com.drin.java;

import com.drin.java.database.CPLOPConnection;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterAverageMetric;
import com.drin.java.metrics.IsolateAverageMetric;
import com.drin.java.metrics.ITSRegionMedianMetric;
import com.drin.java.metrics.PyroprintUnstablePearsonMetric;

import com.drin.java.ontology.Ontology;

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

public class ClusterInterface {
   private CPLOPConnection mConn;
   private Configuration mConfig;

   public ClusterInterface() {
      mConfig = Configuration.loadConfig();
      mConn = CPLOPConnection.getConnection();
   }

   /*
    * Isolates.commonName(): human, cow;",
    * Isolates.hostID(): Winnie,   Collin;",
    * Pyroprints.pyroPrintedDate(TimeSensitive): \t;"
   */
   public static void main(String[] args) {
      ClusterInterface testInterface = new ClusterInterface();

      String testOntology = String.format("%s\n%s\n%s",
         "Isolates.commonName():;",
         "Isolates.hostID(): ;",
         "Pyroprints.pyroPrintedDate(TimeSensitive):   ;"
      );

      /*String testDataSet = "'Sw-029', 'Sw-030', 'Sw-018', 'Sw-019', 'Sw-020', " +
                           "'Sw-021', 'Sw-033', 'Sw-032'";*/
      String testDataSet = null;

      ClusterResults results = testInterface.clusterData(testOntology, testDataSet);

      System.out.println(results);
   }

   public ClusterResults clusterData(String ontologyStr, String selectedData) {
      Ontology ontology = null;
      Clusterer clusterer = null;
      ClusterAverageMetric clustMetric = new ClusterAverageMetric();

      if (ontologyStr != null) {
         ontology = Ontology.constructOntology(ontologyStr);
      }

      List<Clusterable<?>> dataList = constructIsolates(ontology, queryData(ontology, selectedData));

      List<Cluster> clusters = new ArrayList<Cluster>();
      for (Clusterable<?> data : dataList) {
         Cluster tmpClust = new HCluster(clustMetric, data);
         clusters.add(tmpClust);
      }

      List<Double> thresholds = new ArrayList<Double>();
      thresholds.add(new Double(mConfig.getRegionAttr("16-23", Configuration.ALPHA_KEY)));
      thresholds.add(new Double(mConfig.getRegionAttr("16-23", Configuration.BETA_KEY)));

      clusterer = new OHClusterer(ontology, thresholds);
      clusterer.clusterData(clusters);

      return new ClusterResults(clusterer.getClusters());
   }

   private List<Map<String, Object>> queryData(Ontology ont, String dataSet) {
      List<Map<String, Object>> dataList = null;

      try { dataList = mConn.getDataByIsoID(ont, dataSet); }
      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         sqlErr.printStackTrace();
         System.exit(1);
      }

      return dataList;
   }

   private List<Clusterable<?>> constructIsolates(Ontology ont, List<Map<String, Object>> rawDataList) {
      List<Clusterable<?>> dataList = new ArrayList<Clusterable<?>>();

      DataMetric<Isolate> isoMetric = new IsolateAverageMetric();
      DataMetric<ITSRegion> regionMetric = new ITSRegionMedianMetric();
      DataMetric<Pyroprint> pyroMetric = new PyroprintUnstablePearsonMetric();

      Isolate tmpIso = null;
      Pyroprint tmpPyro = null;
      for (Map<String, Object> dataMap : rawDataList) {
         String wellID = String.valueOf(dataMap.get("well"));
         String isoID = String.valueOf(dataMap.get("isolate"));
         String regName = String.valueOf(dataMap.get("region"));
         Integer pyroID = new Integer(String.valueOf(dataMap.get("pyroprint")));

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

                     tmpIso.addLabel(String.valueOf(dataMap.get(colName)).trim());
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
