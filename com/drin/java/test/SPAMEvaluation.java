package com.drin.java.test;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.database.CPLOPConnection;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterAverageMetric;

import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

import java.io.File;

public class SPAMEvaluation {
   private final ExecutorService mThreadPool;

   private CPLOPConnection mConn;
   private HierarchicalClusterer mClusterer;
   
   private static int TEST_RUN_ID = 460;

   private static final float ALPHA_THRESH = 0.995f,
                              BETA_THRESH  = 0.99f;

   private static final String ONTOLOGIES[] = new String[] {
      "ontologies_build_down/large.ont",
   };

   private static final boolean INSERT_RESULTS = true,
                                USE_TRANSFORM  = false;

   public SPAMEvaluation(int sizeHint) {
      mThreadPool = Executors.newFixedThreadPool(8);

      mClusterer = null;
      mConn = null;
      try { mConn = new CPLOPConnection(); }
      catch (Exception err) { err.printStackTrace(); }
   }

   public static void main(String[] args) {
      short initSizes[] = new short[] { 4877 };//, 1000, 1500, 2000, 2500 };
      float upSizes[] = new float[] { 0.0f };
      byte numUps[] = new byte[] { 0 };
      int maxSize = 4877;
      boolean shouldTransform = true;
      int totalSize = 0;

      System.out.println("HAVE YOU CHANGED TEST RUN ID!?");

      for (String ontFile : ONTOLOGIES) {
         Ontology clustOnt = Ontology.createOntology(new File(ontFile));
         System.out.println(clustOnt.size());
         SPAMEvaluation runner = new SPAMEvaluation(maxSize);

         //get the data
         long dataFetchTime = System.currentTimeMillis();
         System.out.println("fetching data...");

         List<Isolate> isoData = runner.getIsolateData(clustOnt, maxSize);

         System.out.printf("finished fetching data in %ds!\n",
            (System.currentTimeMillis() - dataFetchTime) / 1000
         );

         //iterate on configurations
         for (byte numUp : numUps) {
            for (float upSize : upSizes) {
               for (short initSize : initSizes) {
                  totalSize = Math.round(initSize + ((initSize * upSize) * numUp));

                  System.out.printf("initial: %d\nupdate size: %.02f\nnum ups: %d\n",
                     initSize, upSize, numUp
                  );

                  long start = System.currentTimeMillis();

                  for (int i = 0; i < 3; i++) {
                     runner.testAgglom(initSize, upSize, numUp, isoData);

                     runner.testOHClust(initSize, upSize, numUp, new Ontology(clustOnt), isoData);
                  }
                  
                  System.out.printf("Took about %s ms\n", System.currentTimeMillis() - start);
               }
            }
         }
      }
   }

   public void testOHClust(short initSize, float upSize, byte numUps,
                           Ontology clustOnt, List<Isolate> isoData) {
      int isoStart = 0, isoEnd = initSize;
      long startCluster = 0, finishCluster = 0, runTimes[] = new long[numUps + 1];
      long fullTime = System.currentTimeMillis();

      mClusterer = new OHClusterer(isoData.size(), ALPHA_THRESH, BETA_THRESH);

      for (byte currUp = (byte) (-1);  currUp < numUps; currUp++) {
         for (int isoNdx = isoStart; isoNdx < Math.min(isoEnd, isoData.size()); isoNdx++) {
            clustOnt.addData(new HCluster(isoData.get(isoNdx)));
         }

         System.out.println("OHClust! clustering...");

         startCluster = System.currentTimeMillis();
         mClusterer.clusterData(clustOnt);
         finishCluster = System.currentTimeMillis() - startCluster;

         runTimes[currUp + 1] = finishCluster;

         System.out.printf("%d ms\n", finishCluster);
         System.out.printf("%d data size\n", isoEnd);
         System.out.printf("%dth update\n", currUp);

         isoStart = isoEnd;
         isoEnd += Math.round(initSize * upSize);
      }

      for (Map.Entry<Float, List<Cluster>> clustResult : mClusterer.getClusters().entrySet()) {
         persistResults("OHClust!", clustOnt.getName(), mClusterer,
                        clustResult.getKey().floatValue(), clustResult.getValue(),
                        runTimes, System.currentTimeMillis() - fullTime, initSize, upSize);
      }
   }

   public void testAgglom(short initSize, float upSize, byte numUps,
                          List<Isolate> isoData) {
      int isoStart = 0, isoEnd = initSize;
      long startCluster, finishCluster, runTimes[] = new long[numUps + 1];
      long fullTime = System.currentTimeMillis();

      //initialize cluster list
      List<Cluster> tmpClusters = null;
      List<Cluster> clusters = new ArrayList<Cluster>(initSize);

      mClusterer = new AgglomerativeClusterer(isoData.size(), BETA_THRESH);
      for (byte currUp = (byte) (-1);  currUp < numUps; currUp++) {
         for (int isoNdx = isoStart; isoNdx < Math.min(isoEnd, isoData.size()); isoNdx++) {
            clusters.add(new HCluster(isoData.get(isoNdx)));
         }
         
         tmpClusters = new ArrayList<Cluster>(clusters.size());
         for (Cluster clust : clusters) {
            if (clust instanceof HCluster) {
               tmpClusters.add(new HCluster((HCluster) clust));
            }
         }

         System.out.println("clust 0:\n" + tmpClusters.get(0));
         System.out.println("clust 1:\n" + tmpClusters.get(1));

         System.out.println("agglomerative clustering...");

         startCluster = System.currentTimeMillis();
         mClusterer.clusterData(tmpClusters);
         finishCluster = System.currentTimeMillis() - startCluster;

         runTimes[currUp + 1] = finishCluster;

         System.out.printf("%d ms\n", finishCluster);
         System.out.printf("%d data size\n", isoEnd);
         System.out.printf("%dth update\n", currUp);

         isoStart = isoEnd;
         isoEnd += Math.round(initSize * upSize);
      }


      for (Map.Entry<Float, List<Cluster>> clustResult : mClusterer.getClusters().entrySet()) {
         persistResults("Agglomerative", "none", mClusterer,
                        clustResult.getKey().floatValue(), clustResult.getValue(),
                        runTimes, System.currentTimeMillis() - fullTime, initSize, upSize);
      }
   }

   //Insert into test_runs : run_time, algorithm, interclustdist, transform
   //test_run_strain_link : test_run_id, cluster_id, thresh, diameter,
   //                       intra clust sim, % sim
   //test_isolate_strains : test_run_id, cluster_id, thresh, test_isolate_id
   //and test_run_performance : test_run_id, update_id, update_size, run_time
   public void persistResults(String algorithm, String ontName,
                              HierarchicalClusterer clusterer, float threshold,
                              List<Cluster> clusters, long[] runTimes, long totalTime,
                              short initSize, float upSize) {

      if (!INSERT_RESULTS) { return; }

      /*
      if (clusters == null && clusterer.getClusters() == null) {
         System.out.println("wtf; all null");
      }
      else if (clusters == null) { clusters = clusterer.getClusters(); }
      */

      if (clusters != null) {
         System.out.println("size: " + clusters.size());
         System.out.println(clusterer.getInterStrainSim());
      }

      byte use_transform = USE_TRANSFORM ? 1 : 0;

      insertTestRun(TEST_RUN_ID, algorithm, ontName, clusterer, use_transform, totalTime);
      insertRunPerformance(TEST_RUN_ID, initSize, upSize, runTimes);
      insertStrainsAndIsolates(TEST_RUN_ID, threshold, clusters, clusterer);

      TEST_RUN_ID++;
   }

   public void insertTestRun(int runID, String algorith, String ontName,
                             HierarchicalClusterer clusterer,
                             byte use_transform, long totalTime) {
      //int run_id = -1;
      try {
         mConn.insertTestRun(runID, totalTime, algorith, ontName,
                             clusterer.getInterStrainSim(), use_transform);
         //run_id = mConn.getLastRunId();
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }
      //return run_id;
   }

   public void insertRunPerformance(int run_id, short initSize, float upSize, long[] runTimes) {
      String runPerfInsert = "";

      runPerfInsert += String.format(",(%d, %d, %d, %d)",
         run_id, 0, initSize, runTimes[0]
      );
      for (byte timeNdx = 1; timeNdx < runTimes.length; timeNdx++) {
         runPerfInsert += String.format(",(%d, %d, %d, %d)",
            run_id, timeNdx, Math.round(initSize * upSize), runTimes[timeNdx]
         );
      }
      
      try {
         mConn.insertRunPerf(runPerfInsert.substring(1));
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }
   }

   public void insertStrainsAndIsolates(int run_id, float threshold, List<Cluster> clusters, HierarchicalClusterer clusterer) {
      String strainInsert = "", isolateInsert = "";

      /*
      if (clusters == null) {
         System.out.println("clusters are null for some reason...");
         clusters = clusterer.getClusters();
      }
      */

      for (Cluster clust : clusters) {
         System.out.printf("cluster size: %d\n", clust.size());

         strainInsert += String.format(
            ",(%d, %d, %.04f, %.04f, %.04f)",
            run_id, clust.getId(), threshold, clust.getDiameter(),
            clust.getMean()
         );

         for (Clusterable<?> elem : clust.getElements()) {
            isolateInsert += String.format(
               ",(%d, %d, %.04f, '%s')", run_id, clust.getId(), 
               threshold, elem.getName()
            );
         }
      }

      try {
         mConn.insertIsolateAndStrainData(strainInsert.substring(1),
                                          isolateInsert.substring(1));
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }
   }

   public List<Isolate> getIsolateData(Ontology ont, int dataSize) {
      List<Isolate> isoData = null;

      try {
         isoData = mConn.getIsolateData(dataSize, null);

         mConn.getIsolateMetaData(isoData, ont, dataSize);
         System.out.println("retrieved meta-data...");
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }

      return isoData;
   }
}
