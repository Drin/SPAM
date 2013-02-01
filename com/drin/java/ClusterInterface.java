package com.drin.java;

import com.drin.java.database.CPLOPConnection;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.ClusterAverageMetric;
import com.drin.java.metrics.IsolateAverageMetric;
import com.drin.java.metrics.ITSRegionAverageMetric;
import com.drin.java.metrics.PyroprintUnstablePearsonMetric;

import com.drin.java.ontology.Ontology;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;
import com.drin.java.clustering.ClusterResults;
import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import com.drin.java.util.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class ClusterInterface {
   private final String[] DATA_TYPE_VALUES = new String[] {"Isolates",
                                                           "Pyroprints",
                                                           "Experiments"};

   private CPLOPConnection mConn;
   private long startTime;

   public ClusterInterface() {
      try {
         mConn = new CPLOPConnection();
      }

      catch (CPLOPConnection.DriverException driveErr) {
         System.out.println("Driver Exception:\n" + driveErr + "\nExiting...");
         //driveErr.printStackTrace();
         System.exit(1);
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQL Exception:\n" + sqlErr + "\nExiting...");
         System.exit(1);
      }
   }

   public static void main(String[] args) {
      ClusterInterface testInterface = new ClusterInterface();

      String testOntology = String.format("%s\n%s\n%s",
         "Host(): cw, sw;",
         "Location():R1, R2, MorroBay;",
         "Day(TimeSensitive):     1,2,3,\t4,5,6,7,10;"
      );

      String testDataSet = "'Sw-029', 'Sw-030', 'Sw-018', 'Sw-019', 'Sw-020'";
      double tmp_alpha = .99, tmp_beta = .995;

      ClusterResults results = testInterface.clusterData(testOntology, testDataSet,
                                                         "Isolates", tmp_alpha, tmp_beta);

      System.out.println(results);
   }

   public ClusterResults clusterData(String ontologyStr, String selectedData,
                                     String tableName, double alphaThresh, double betaThresh) {
      Ontology ontology = null;
      Clusterer clusterer = null;
      List<Cluster> clusters = new ArrayList<Cluster>();

      if (ontologyStr != null) {
         ontology = Ontology.constructOntology(ontologyStr);
      }

      Map<String, Isolate> isoMap = constructIsolates(queryData(selectedData, tableName),
                                                      alphaThresh, betaThresh);
      
      ClusterAverageMetric clustMetric = new ClusterAverageMetric();

      for (Map.Entry<String, Isolate> isoEntry : isoMap.entrySet()) {
         Cluster tmpClust = new HCluster(clustMetric, isoEntry.getValue());
         clusters.add(tmpClust);

         if (ontology != null) { ontology.addData(tmpClust); }
      }

      if (ontology != null) {
         List<Cluster> coreClusters = new ArrayList<Cluster>();
         List<Cluster> boundaryClusters = new ArrayList<Cluster>();
         Map<Integer, String> promotedClusters = new HashMap<Integer, String>();

         for (int ndx_A = 0; ndx_A < clusters.size(); ndx_A++) {
            Cluster clust_A = clusters.get(ndx_A);

            for (int ndx_B = ndx_A + 1; ndx_B < clusters.size(); ndx_B++) {
               Cluster clust_B = clusters.get(ndx_B);

               if (clust_A.compareTo(clust_B) > alphaThresh) {
                  if (!promotedClusters.containsKey(new Integer(ndx_A))) {
                     coreClusters.add(clust_A);
                     promotedClusters.put(ndx_A, clust_A.getName());
                  }

                  if (!promotedClusters.containsKey(new Integer(ndx_B))) {
                     coreClusters.add(clust_B);
                     promotedClusters.put(ndx_B, clust_B.getName());
                  }
               }
            }
         }

         for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
            if (!promotedClusters.containsKey(clustNdx)) {
               boundaryClusters.add(clusters.get(clustNdx));
            }
         }

         clusters = null;
         clusterer = new OHClusterer(coreClusters, boundaryClusters,
                                     ontology, alphaThresh, betaThresh);
      }
      else if (ontology == null) {
         clusterer = new AgglomerativeClusterer(clusters, betaThresh);
      }

      clusterer.clusterData(null);

      return new ClusterResults(clusterer.getClusters());
   }

   private List<Map<String, Object>> queryData(String dataSet, String tableName) {
      List<Map<String, Object>> dataList = null;

      if (tableName.equals(DATA_TYPE_VALUES[0])) {
         try {
            dataList = mConn.getDataByIsoID(dataSet);
         }
         catch (java.sql.SQLException sqlErr) {
            System.out.println("SQLException:\nExiting...");
            sqlErr.printStackTrace();
            System.exit(1);
         }
      }

      else if (tableName.equals(DATA_TYPE_VALUES[1])) {
         try {
            dataList = mConn.getDataByPyroID(dataSet);
         }
         catch (java.sql.SQLException sqlErr) {
            System.out.println("SQLException:\nExiting...");
            sqlErr.printStackTrace();
            System.exit(1);
         }
      }

      return dataList;
   }

   private Map<String, Isolate> constructIsolates(List<Map<String, Object>> dataList,
                                                  double alphaThresh, double betaThresh) {
      Map<String, Isolate> isoMap = new HashMap<String, Isolate>();
      Map<String, Map<Integer, Object[]>> pyroDataMap =
         new HashMap<String, Map<Integer, Object[]>>();

      //First pass over the data where ITSRegions and Isolates are constructed.
      String pyroList = "";
      for (Map<String, Object> dataMap : dataList) {
         String isoID = String.valueOf(dataMap.get("isolate"));
         String regName = String.valueOf(dataMap.get("region"));
         String wellID = String.valueOf(dataMap.get("well"));
         Integer pyroID = new Integer(String.valueOf(dataMap.get("pyroprint")));
         double peakHeight = Double.parseDouble(String.valueOf(dataMap.get("pHeight")));
         String nucleotide = String.valueOf(dataMap.get("nucleotide"));

         //Retrieve Isolate
         if (!isoMap.containsKey(isoID)) {
            isoMap.put(isoID, new Isolate(isoID, new HashSet<ITSRegion>(),
                                          new IsolateAverageMetric()));
         }

         isoMap.get(isoID).getData().add(new ITSRegion(regName, alphaThresh, betaThresh,
                                         new ITSRegionAverageMetric(alphaThresh, betaThresh)));

         if (!pyroDataMap.containsKey(isoID)) {
            pyroDataMap.put(isoID, new HashMap<Integer, Object[]>());
         }

         Map<Integer, Object[]> pyroMap = pyroDataMap.get(isoID);

         if (!pyroMap.containsKey(pyroID)) {
            pyroMap.put(pyroID, new Object[] {pyroID, wellID, "",
                                              new ArrayList<Double>(),
                                              regName});
         }

         Object[] pyroData = pyroMap.get(pyroID);

         if (pyroData[2] instanceof String) {
            pyroData[2] = String.valueOf(pyroData[2]).concat(nucleotide);
         }
         if (pyroData[3] instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Double> peakList = (List<Double>) pyroData[3];
            peakList.add(new Double(peakHeight));
         }
      }

      for (Map.Entry<String, Map<Integer, Object[]>> pyroDataEntry : pyroDataMap.entrySet()) {

         for (Map.Entry<Integer, Object[]> pyroEntry : pyroDataEntry.getValue().entrySet()) {
            Object[] pyroData = pyroEntry.getValue();

            @SuppressWarnings("unchecked")
            Pyroprint newPyro = new Pyroprint(Integer.parseInt(String.valueOf(pyroData[0])),
                                              String.valueOf(pyroData[1]),
                                              String.valueOf(pyroData[2]),
                                              (List<Double>) pyroData[3],
                                              new PyroprintUnstablePearsonMetric());

            for (ITSRegion region : isoMap.get(pyroDataEntry.getKey()).getData()) {
               if (region.getName().equals(pyroData[4])) {
                  region.getData().add(newPyro);
               }
            }
         }
      }

      Map<String, Isolate> finalIsoMap = new HashMap<String, Isolate>();

      Logger.debug("Isolates retrieved from CPLOP:");
      for (Map.Entry<String, Isolate> isoEntry : isoMap.entrySet()) {
         Logger.debug(String.format("%s: %s\n", isoEntry.getKey(),
                                    isoEntry.getValue().toString()));

         boolean isCompleteIsolate = true;

         for (ITSRegion region : isoEntry.getValue().getData()) {
            if (region.getData().isEmpty()) {
               Logger.debug(String.format("%s[%s] has no pyroprints\n",
                                          isoEntry.getValue().getName(),
                                          region.getName()));
               isCompleteIsolate = false;
            }
         }

         if (isCompleteIsolate) {
            finalIsoMap.put(isoEntry.getKey(), isoEntry.getValue());
         }
      }

      return isoMap;
   }
}
