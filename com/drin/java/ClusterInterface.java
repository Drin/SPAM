package com.drin.java;

import com.drin.java.database.CPLOPConnection;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.ontology.Ontology;
import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;
import com.drin.java.clustering.ClusterResults;
import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import com.drin.java.parsers.MatrixParser;

import com.drin.java.util.Logger;
import com.drin.java.util.Configuration;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import java.io.File;

public class ClusterInterface {
   private final String[] DATA_TYPE_VALUES = new String[] {"Isolates",
                                                           "Pyroprints",
                                                           "Experiments"};

   private final static String PARAM_SECTION = "parameters",
                               CACHE_SIMS_OPTION = "cache similarities";

   private CPLOPConnection mConn;
   private long startTime;

   public ClusterInterface() {
      Configuration.loadConfig("config.cfg");

      try { mConn = new CPLOPConnection(); }
      catch (Exception err) { err.printStackTrace(); }
   }

   public static void main(String[] args) {
      ClusterInterface testInterface = new ClusterInterface();

      String testOntology = "emilyOntology.ont";
      /*
      String.format("%s\n%s\n%s",
         //"Isolates.commonName():;",
         //"Pyroprints.pyroPrintedDate(TimeSensitive): \t;"
         "Samples.dateCollected():;",
         "Isolates.userName():;",
         "Samples.location():;",
         "Isolates.hostID(): ;"
      );
      */
      //testOntology = null;

      float tmp_alpha = 0.995f, tmp_beta = 0.99f;
      boolean cacheSimilarities = Configuration.getBoolean(PARAM_SECTION, CACHE_SIMS_OPTION),
              matchIsolatesAgainstDB = false;

      String matchSet = null, testDataSet = null;

      if (args.length == 1) {
         testDataSet = testInterface.parseIsoIdFile(args[0]);
      }
      else if (args.length == 2) {
         matchIsolatesAgainstDB = true;

         matchSet = testInterface.parseIsoIdFile(args[0]);
         testDataSet = testInterface.parseIsoIdFile(args[1]);
      }

      /* Prepare samples and clusters for matching */
      List<Isolate> matchSamples = null;
      List<Cluster> matchClusters = null;

      if (matchIsolatesAgainstDB) {
         matchSamples = testInterface.getIsolateData(matchSet);
         matchClusters = new ArrayList<Cluster>();

         for (Isolate sample : matchSamples) {
            matchClusters.add(new HCluster(cacheSimilarities, sample));
         }
      }

      /*
      // Parse STEC IDs
      matchSet = testInterface.parseIsoIdFile("stecIDs");
      // Parse ES IDs
      matchSet = testInterface.parseIsoIdFile("esIDs");
      // Eric Colilert and Direct Isolates
      matchSet = testInterface.parseIsoIdFile("ericColilertAndDirectIDs");
      matchSet = testInterface.parseIsoIdFile("oliviaAllIDs");
      //these are pp IDs not pidgeon IDs
      matchSet = testInterface.parseIsoIdFile("ppIDs");
      // Parse pigeon IDs
      testDataSet = testInterface.parseIsoIdFile("pgIDs");
      // Parse 2011 Bull Isolates
      testDataSet = testInterface.parseIsoIdFile("2011Bulls");
      // Parse 2012 Bull Isolates
      testDataSet = testInterface.parseIsoIdFile("2012Bulls");
      // Parse 2012 Bull and Squirrel Isolates
      testDataSet = testInterface.parseIsoIdFile("2012BullsAndSquirrels");
      testDataSet = testInterface.parseIsoIdFile("AllBulls");
      // Parse All of Josh's Isolates
      testDataSet = testInterface.parseIsoIdFile("allJoshIsolates");
      // Parse all of the isolates in the Direct Plated Creek Isolates dataset
      testDataSet = testInterface.parseIsoIdFile("oliviaDirectIDs");
      // Parse all of the isolates in the Colilert Environmental Samples dataset
      testDataSet = testInterface.parseIsoIdFile("oliviaColilertIDs");
      // Parse all of the isolates in the Direct Plated and
      // Colilert Environmental Samples datasets
      testDataSet = testInterface.parseIsoIdFile("oliviaAllIDs");
      testDataSet = testInterface.parseIsoIdFile("ppIDs");
      testDataSet = testInterface.parseIsoIdFile("dog165_172");
      testDataSet = testInterface.parseIsoIdFile("human1786_1792");
      testDataSet = testInterface.parseIsoIdFile("cow1159_1550");
      testDataSet = testInterface.parseIsoIdFile("emilyIsoIDs");
      */

      /* Cluster execution if getting data from database */
      Map<Float, List<Cluster>> results = testInterface.clusterData(
         testOntology, testDataSet, "Isolates", tmp_alpha, tmp_beta
      );

      /* Data Prep from CSV */
      /*
      MatrixParser csvParser = new MatrixParser(
         "/home/drin/programming/spam/data/black_1108/16-23trimmed95.csv"
         //"/home/drin/programming/spam/data/jason_and_patrick/080813 16-23 Pp experiment.csv"
      );

      Map<String, Map<String, Float>> corrMatrix = csvParser.parseData();

      csvParser = new MatrixParser(
         "/home/drin/programming/spam/data/black_1108/23-5trimmed.csv"
         //"/home/drin/programming/spam/data/jason_and_patrick/080813 23-5 Pp experiment.csv"
      );
      */

      /*
      Map<String, Map<String, Float>> tmpMatrix = csvParser.parseData();

      for (Map.Entry<String, Map<String, Float>> isoMap : corrMatrix.entrySet()) {
         Map<String, Float> tmpIsoMap = tmpMatrix.get(isoMap.getKey());

         for (Map.Entry<String, Float> corrMapping : isoMap.getValue().entrySet()) {
            if (corrMapping.getValue().floatValue() < tmp_beta ||
                tmpIsoMap.get(corrMapping.getKey()).floatValue() < tmp_beta) {
               corrMapping.setValue(0.0f);
            }
            else {
               corrMapping.setValue(
                  (corrMapping.getValue().floatValue() +
                   tmpIsoMap.get(corrMapping.getKey()).floatValue())
                  / 2
               );
            }

            /* Debugging
            System.out.printf("%s, %s, %.04f\n",
               isoMap.getKey(), corrMapping.getKey(), corrMapping.getValue()
            );
         }
      }

      tmpMatrix.clear();
      tmpMatrix = null;

      /* Cluster execution if getting data from CSV 
      Map<Float, List<Cluster>> results = testInterface.clusterData(
         corrMatrix.keySet(), corrMatrix, tmp_alpha, tmp_beta
      );
      */

      /*
       * Retrieve samples for matching against clusters
       */
      Set<Cluster> matchClustersA = new HashSet<Cluster>();
      Set<Cluster> matchClustersB = new HashSet<Cluster>();

      //Match samples against clusters here

      float maxSimA, maxSimB, comparison = 0.0f;
      Cluster closeClustA, closeClustB;

      if (matchIsolatesAgainstDB) {
         for (Cluster matchClust : matchClusters) {
            maxSimA = maxSimB = 0.0f;
            closeClustA = null;
            closeClustB = null;

            //Match at upper threshold
            if (results.containsKey(new Float(tmp_alpha))) {
               for (Cluster clustA : results.get(new Float(tmp_alpha))) {
                  comparison = matchClust.compareTo(clustA);
                  if (comparison > maxSimA && comparison >= tmp_alpha) {
                     maxSimA = comparison;
                     closeClustA = clustA;
                  }
               }
            }

            if (closeClustA != null) {
               closeClustA.join(new HCluster((HCluster) matchClust));
               matchClustersA.add(closeClustA);
            }

            //Match at lower threshold
            for (Cluster clustB : results.get(new Float(tmp_beta))) {
               comparison = matchClust.compareTo(clustB);
               if (comparison > maxSimB && comparison >= tmp_beta) {
                  maxSimB = comparison;
                  closeClustB = clustB;
               }
            }
            if (closeClustB != null) {
               closeClustB.join(new HCluster((HCluster) matchClust));
               matchClustersB.add(closeClustB);
            }
         }
      }


      /*
      System.out.println("ES samples that did not match any clusters at the upper threshold:");
      for (Cluster tmpESClust : loneESClustersA) {
         for (Clusterable<?> elem : tmpESClust.getElements()) {
            System.out.println(String.format(
               "%s, %s, %s, %s, %s", elem.getName(),
               ((Isolate) elem).getHost(), ((Isolate) elem).getSource(),
               ((Isolate) elem).getLoc(), ((Isolate) elem).getDate()
            ));
         }
      }

      System.out.println("ES samples that did not match any clusters at the lower threshold:");
      for (Cluster tmpESClust : loneESClustersB) {
         for (Clusterable<?> elem : tmpESClust.getElements()) {
            System.out.println(String.format(
               "%s, %s, %s, %s, %s", elem.getName(),
               ((Isolate) elem).getHost(), ((Isolate) elem).getSource(),
               ((Isolate) elem).getLoc(), ((Isolate) elem).getDate()
            ));
         }
      }
      */
      /*
      Map<Float, List<Cluster>> ESClusterMap = new HashMap<Float, List<Cluster>>();
      ESClusterMap.put(new Float(tmp_alpha), new ArrayList<Cluster>(ESClustersA));
      ESClusterMap.put(new Float(tmp_beta), new ArrayList<Cluster>(ESClustersB));

      System.out.println(new ClusterResults(ESClusterMap));
      */

      /* 
       * Matching cluster results
       */
      if (matchIsolatesAgainstDB) {
         Map<Float, List<Cluster>> matchClusterMap = new HashMap<Float, List<Cluster>>();
         matchClusterMap.put(new Float(tmp_alpha), new ArrayList<Cluster>(matchClustersA));
         matchClusterMap.put(new Float(tmp_beta), new ArrayList<Cluster>(matchClustersB));

         System.out.println(new ClusterResults(matchClusterMap));
      }

      //TODO remove this after debugWriter has been removed from Pyroprint.java
      Pyroprint.closeWriter();
      Isolate.closeWriter();
   }

   private String parseIsoIdFile(String fileName) {
      Scanner fileScanner = null;
      try { fileScanner = new Scanner(new File(fileName)); }
      catch (Exception err) {
         err.printStackTrace();
         System.exit(1);
      }

      String isoIds = fileScanner.nextLine();
      fileScanner.close();

      return isoIds;
   }

   public Map<Float, List<Cluster>> clusterData(Set<String> isoSet,
                                                Map<String, Map<String, Float>> corrMap,
                                                float alphaThresh, float betaThresh) {
      System.err.println("wrong clustering method");
      Map<Float, List<Cluster>> clusterResults = new HashMap<Float, List<Cluster>>();
      List<Cluster> clusters1 = new ArrayList<Cluster>();
      List<Cluster> clusters2 = new ArrayList<Cluster>();

      Clusterer clusterer = null;

      for (String isoName : isoSet) {
         Isolate tmpIso = new Isolate(isoName);

         if (corrMap.containsKey(isoName)) {
            tmpIso.setCache(corrMap.get(isoName));
         }

         clusters1.add(new HCluster(tmpIso));
      }

      Cluster.resetClusterIDs();

      for (String isoName : isoSet) {
         Isolate tmpIso = new Isolate(isoName);

         if (corrMap.containsKey(isoName)) {
            tmpIso.setCache(corrMap.get(isoName));
         }

         clusters2.add(new HCluster(tmpIso));
      }

      //no ontology given so hardcode it to just hierarchical cluster
      clusterer = new AgglomerativeClusterer(clusters1.size(), alphaThresh, null);
      clusterer.clusterData(clusters1);
      clusterResults.put(new Float(alphaThresh), clusters1);

      //System.out.println(new ClusterResults(clusterer.getClusters()));

      //no ontology given so hardcode it to just hierarchical cluster
      clusterer = new AgglomerativeClusterer(clusters2.size(), betaThresh, null);
      clusterer.clusterData(clusters2);
      clusterResults.put(new Float(betaThresh), clusters2);

      //System.out.println(new ClusterResults(clusterer.getClusters()));

      return clusterResults;
   }

   public Map<Float, List<Cluster>> clusterData(String ontologyFile, String selectedData,
                                                String tableName, float alphaThresh,
                                                float betaThresh) {
      Map<Float, List<Cluster>> clusterResults = new HashMap<Float, List<Cluster>>();
      List<Cluster> clusters1 = new ArrayList<Cluster>();

      List<Isolate> isolateDataList = getIsolateData(selectedData);
      Ontology clusterOnt = null;
      Clusterer clusterer = null;

      boolean useOHClust = false;

      if (useOHClust) {
         clusterOnt = Ontology.createOntology(new File(ontologyFile));
         mConn.getIsolateMetaData(isolateDataList, clusterOnt, isolateDataList.size());
      }

      for (Isolate isolate : isolateDataList) {
         if (useOHClust) { clusterOnt.addData(new HCluster(isolate)); }
         else { clusters1.add(new HCluster(isolate)); }
      }

      //For debugging ontology content
      //System.out.println(clusterOnt);

      //no ontology given so hardcode it to just hierarchical cluster
      if (useOHClust) {
         System.err.println("OHClustering!");
         clusterer = new OHClusterer(clusters1.size(), alphaThresh, betaThresh, null);
         clusterer.clusterData(clusterOnt);
         System.out.println(new ClusterResults(clusterer.getClusters()));
         return clusterer.getClusters();
      }
      else {
         clusterer = new AgglomerativeClusterer(clusters1.size(), betaThresh, null);
         clusterer.clusterData(clusters1);
         clusterResults.put(new Float(betaThresh), clusters1);
         System.out.println(new ClusterResults(clusterResults));
         return clusterResults;
      }
   }

   public List<Isolate> getIsolateData(String dataSet) {
      List<Isolate> isoData = null;
      int dataSize = 0;

      try {
         if (dataSet != null) {
            String[] idList = dataSet.split(",");
            System.out.println(dataSet);
            System.out.println(idList.length);
            dataSize = idList.length;
         }

         isoData = mConn.getIsolateData(dataSize, dataSet);
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }

      return isoData;
   }
}
