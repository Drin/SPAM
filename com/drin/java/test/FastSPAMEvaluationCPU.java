package com.drin.java.test;

import com.drin.java.database.CPLOPConnection;
import com.drin.java.database.CPLOPConnection.IsolateDataContainer;

import com.drin.java.ontology.FastOntology;
import com.drin.java.ontology.FastOntologyTerm;

import com.drin.java.clustering.FastCluster;
import com.drin.java.analysis.clustering.FastHierarchicalClusterer;
import com.drin.java.analysis.clustering.FastOHClusterer;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.List;
import java.util.ArrayList;

import java.io.File;

public class FastSPAMEvaluationCPU {
   private final ExecutorService mThreadPool;

   private CPLOPConnection mConn;
   private FastHierarchicalClusterer mClusterer;
   
   private static int TEST_RUN_ID = 525;
   private static final short ISOLATE_LEN = 188;
   private static final byte LEN_23S     = 93,
                             LEN_16S     = 95;

   private static final float ALPHA_THRESH = 0.90f,
                              BETA_THRESH  = 0.85f;

   private static byte REGION_LENS[] = new byte[] {LEN_23S, LEN_16S};
   private static byte REGION_OFFSETS[] = new byte[] {0, LEN_23S};

   private static final String ONTOLOGIES[] = new String[] {
      "ontologies_build_down/small.ont",
      "ontologies_build_down/medium.ont",
      "ontologies_build_down/large.ont",
      "ontology_partitions/full_3_3_3.ont",
      "ontology_partitions/full_4_4_4.ont",
      "ontology_partitions/full_5_5_5.ont",
      "ontology_partitions/full_6_6_6.ont"
   };

   private static final boolean INSERT_RESULTS = true,
                                USE_TRANSFORM  = false;/*,
                                DEBUG          = true;*/

   public FastSPAMEvaluationCPU(int sizeHint) {
      mThreadPool = Executors.newFixedThreadPool(64);

      mClusterer = null;
      mConn = null;
      try { mConn = new CPLOPConnection(); }
      catch (Exception err) { err.printStackTrace(); }
   }

   public static void main(String[] args) {
      short initSizes[] = new short[] { 500, 750, 1000, 1250, 1500 };
      float upSizes[] = new float[] { 0.10f };
      byte numUps[] = new byte[] { 10 };
      int maxSize = 3000;
      int totalSize = 0;

      //iterate on configurations
      for (byte numUp : numUps) {
         for (float upSize : upSizes) {
            for (short initSize : initSizes) {
               //prepare ontology and class
               for (String ontFile : ONTOLOGIES) {
                  FastOntology clustOnt = FastOntology.createFastOntology(new File(ontFile));
                  FastSPAMEvaluationCPU runner = new FastSPAMEvaluationCPU(maxSize);

                  //get the data
                  long dataFetchTime = System.currentTimeMillis();
                  System.out.println("HAVE YOU CHANGED TEST RUN ID!?");
                  System.out.println("fetching data...");
                  IsolateDataContainer data = runner.getIsolateData(clustOnt, maxSize);
                  System.out.printf("finished fetching data in %ds!\n",
                     (System.currentTimeMillis() - dataFetchTime) / 1000
                  );

                  //populate the packed similarity matrix
                  float simMatrix[] = runner.calculateSimMatrix(data.isoIDs, data.isoData);
                  int[][] simMapping = runner.getSimMapping(data.isoIDs.length);

                  //set static data for lookups
                  FastOntologyTerm.mIsoLabels = data.isoMeta;
                  FastCluster.mNumIsolates = maxSize;
                  FastCluster.mSimMatrix = simMatrix;
                  FastCluster.mSimMapping = simMapping;
                  FastCluster.mTransform = USE_TRANSFORM;

                  totalSize = Math.round(initSize + ((initSize * upSize) * numUp));

                  System.out.printf("initial: %d\nupdate size: %.02f\nnum ups: %d\n",
                     initSize, upSize, numUp
                  );

                  long start = System.currentTimeMillis();

                  for (int i = 0; i < 3; i++) {
                     /*
                     runner.testAgglom(initSize, upSize, numUp, totalSize,
                                       simMapping, simMatrix, data);
                                       */

                     runner.testOHClust(initSize, upSize, numUp, totalSize,
                                        new FastOntology(clustOnt),
                                        simMapping, simMatrix, data);
                  }
                  
                  System.out.printf("Took about %s ms\n", System.currentTimeMillis() - start);
               }
            }
         }
      }

      cleanup();
   }

   //create a lookup table for isolate IDs to a spot in the packed similarity matrix
   public int[][] getSimMapping(int numIsolates) {
      int[][] simMapping = new int[numIsolates][];
      int simNdx = 0;

      for (int ndxA = 0; ndxA < numIsolates; ndxA++) {
         simMapping[ndxA] = new int[numIsolates - ndxA];

         for (int ndxB = (ndxA + 1); ndxB < numIsolates; ndxB++) {
            simMapping[ndxA][ndxB % (numIsolates - ndxA)] = simNdx++;
         }
      }

      return simMapping;
   }

   public void testOHClust(short initSize, float upSize, byte numUps, int numIsolates,
                           FastOntology clustOnt, int[][] simMapping, float[] simMatrix,
                           IsolateDataContainer data) {
      int isoStart = 0, isoEnd = initSize;
      long startCluster = 0, finishCluster = 0, runTimes[] = new long[numUps + 1];
      long fullTime = System.currentTimeMillis();

      mClusterer = new FastOHClusterer(numIsolates, ALPHA_THRESH, BETA_THRESH);

      for (byte currUp = (byte) (-1);  currUp < numUps; currUp++) {
         for (int isoNdx = isoStart; isoNdx < Math.min(isoEnd, numIsolates); isoNdx++) {
            clustOnt.addData(new FastCluster(isoNdx));
         }

         startCluster = System.currentTimeMillis();
         mClusterer.clusterData(clustOnt);
         finishCluster = System.currentTimeMillis() - startCluster;

         runTimes[currUp + 1] = finishCluster;

         System.out.printf("%d ms\n", finishCluster);
         System.out.printf("%d data size\n", isoEnd);
         System.out.printf("%dth update\n", currUp);

         isoStart = isoEnd;
         isoEnd += Math.round(initSize * upSize);

         insertStrainsAndIsolates(TEST_RUN_ID, mClusterer.getClusters(), mClusterer,
                                  data, (byte) (currUp + 1));
      }

      persistResults("OHClust!", clustOnt.getName(), mClusterer, mClusterer.getClusters(),
                     runTimes, System.currentTimeMillis() - fullTime, initSize, upSize,
                     data);
   }

   public void testAgglom(short initSize, float upSize, byte numUps, int numIsolates,
                          int[][] simMapping, float[] simMatrix, IsolateDataContainer data) {
      int isoStart = 0, isoEnd = initSize;
      long startCluster, finishCluster, runTimes[] = new long[numUps + 1];
      long fullTime = System.currentTimeMillis();

      //initialize cluster list
      List<FastCluster> tmpClusters = null;
      List<FastCluster> clusters = new ArrayList<FastCluster>(initSize);

      mClusterer = new FastHierarchicalClusterer(numIsolates, BETA_THRESH);
      for (byte currUp = (byte) (-1);  currUp < numUps; currUp++) {
         for (int isoNdx = isoStart; isoNdx < Math.min(isoEnd, numIsolates); isoNdx++) {
            clusters.add(new FastCluster(isoNdx));
         }
         
         tmpClusters = new ArrayList<FastCluster>(clusters.size());
         for (FastCluster clust : clusters) {
            tmpClusters.add(new FastCluster(clust));
         }

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

      persistResults("Agglomerative", "none", mClusterer, mClusterer.getClusters(),
                     runTimes, System.currentTimeMillis() - fullTime, initSize, upSize,
                     data);
   }

   public static void cleanup() { FastCluster.shutdownThreadPool(); }

   //Insert into test_runs : run_time, algorithm, interclustdist, transform
   //test_run_strain_link : test_run_id, cluster_id, thresh, diameter,
   //                       intra clust sim, % sim
   //test_isolate_strains : test_run_id, cluster_id, thresh, test_isolate_id
   //and test_run_performance : test_run_id, update_id, update_size, run_time
   public void persistResults(String algorithm, String ontName,
                              FastHierarchicalClusterer clusterer,
                              List<FastCluster> clusters, long[] runTimes, long totalTime,
                              short initSize, float upSize, IsolateDataContainer data) {

      if (!INSERT_RESULTS) { return; }

      if (clusters == null && clusterer.getClusters() == null) {
         System.out.println("wtf why is it all null");
      }
      else if (clusters == null) {
         clusters = clusterer.getClusters();
      }

      if (clusters != null) {
         System.out.println("size: " + clusters.size());
         System.out.println(clusterer.getInterStrainSim());
      }

      byte use_transform = 0;
      if (USE_TRANSFORM) { use_transform = 1; }

      insertTestRun(TEST_RUN_ID, algorithm, ontName, clusterer, use_transform, totalTime);
      insertRunPerformance(TEST_RUN_ID, initSize, upSize, runTimes);
      //insertStrainsAndIsolates(TEST_RUN_ID, clusters, clusterer, data);

      TEST_RUN_ID++;
   }

   public void insertTestRun(int runID, String algorith, String ontName,
                             FastHierarchicalClusterer clusterer,
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

   public void insertStrainsAndIsolates(int run_id, List<FastCluster> clusters,
                                        FastHierarchicalClusterer clusterer,
                                        IsolateDataContainer data, byte updateID) {
      String strainInsert = "", isolateInsert = "";

      if (clusters == null) {
         System.out.println("clusters are null for some reason...");
         clusters = clusterer.getClusters();
      }

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         if (clusters.get(clustNdx) == null) { continue; }

         FastCluster clust = clusters.get(clustNdx);

         System.out.printf("cluster size: %d\n", clust.size());

         strainInsert += String.format(
            ",(%d, %d, %.04f, %.04f, %.04f, %.04f, %d)",
            run_id, clust.getID(), clusterer.getThreshold(), clust.getDiameter(),
            clust.getMean(), clust.getPercentSimilar(), updateID
         );

         int[] clustElements = clust.getElements();
         for (int isoNdx = 0; isoNdx < clust.size(); isoNdx++) {
            int isolate_id = data.isoIDs[clustElements[isoNdx]];

            isolateInsert += String.format(
               ",(%d, %d, %.04f, %d, %d)", run_id, clust.getID(), 
               clusterer.getThreshold(), isolate_id, updateID
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



   public IsolateDataContainer getIsolateData(FastOntology ont, int dataSize) {
      IsolateDataContainer data = null;

      try {
         data = mConn.getIsolateData(dataSize);
         data.isoMeta = mConn.getIsolateMetaData(data.isoIDs, ont, dataSize);

         /*
         if (false && DEBUG) {
            for (int iso_ndx = 0; iso_ndx < data.isoIDs.length; iso_ndx++) {
               System.out.printf("%s:", data.isoIDs[iso_ndx]);

               for (int meta_ndx = 0; meta_ndx < data.isoMeta[iso_ndx].length; meta_ndx++) {
                  System.out.printf("\t%s", data.isoMeta[iso_ndx][meta_ndx]);
               }
               
               System.out.println("");

               for (int peak_ndx = 0; peak_ndx < 93; peak_ndx++) {
                  System.out.printf("\t,%s", data.isoData[iso_ndx * ISOLATE_LEN + peak_ndx]);
               }

               System.out.println("");

               for (int peak_ndx = 0; peak_ndx < 95; peak_ndx++) {
                  System.out.printf("\t,%s", data.isoData[iso_ndx * ISOLATE_LEN + 93 + peak_ndx]);
               }

               System.out.println("");
            }

            System.exit(0);
         }
         */
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }

      return data;
   }

   public final float[] calculateSimMatrix(final int[] isoIDs, final float[] isoData) {
      final int[] isoNdxA = new int[] {0}, isoNdxB = new int[] {0};
      final int[] simNdx = new int[] {0};
      final float simMatrix[]  = new float[(isoIDs.length * (isoIDs.length - 1)) / 2];

      for (isoNdxA[0] = 0; isoNdxA[0] < isoIDs.length; isoNdxA[0]++) {
         for (isoNdxB[0] = (isoNdxA[0] + 1); isoNdxB[0] < isoIDs.length; isoNdxB[0]++) {
            mThreadPool.execute(new Runnable() {
               private final int ndxA = isoNdxA[0], ndxB = isoNdxB[0];
               private final int tmpSimNdx = simNdx[0]++;
               private float pearsonSum = 0.0f;
               private float peakHeightA = 0.0f, peakHeightB = 0.0f;
               private float sumA = 0.0f, sumB = 0.0f, sumAB = 0.0f,
                             sumASquared = 0.0f, sumBSquared = 0.0f;

               public void run() {
                  pearsonSum = 0.0f;

                  for (byte regNdx = 0; regNdx < 2; regNdx++) {
                     sumA = 0.0f;
                     sumB = 0.0f;
                     sumAB = 0.0f;
                     sumASquared = 0.0f;
                     sumBSquared = 0.0f;

                     for (byte peakNdx = 0; peakNdx < REGION_LENS[regNdx]; peakNdx++) {

                        peakHeightA = isoData[ndxA * ISOLATE_LEN + peakNdx +
                                              REGION_OFFSETS[regNdx]];

                        peakHeightB = isoData[ndxB * ISOLATE_LEN + peakNdx +
                                              REGION_OFFSETS[regNdx]];

                        sumA += peakHeightA;
                        sumB += peakHeightB;
                        sumASquared += peakHeightA * peakHeightA;
                        sumBSquared += peakHeightB * peakHeightB;
                        sumAB += peakHeightA * peakHeightB;

                     }

                     pearsonSum += (REGION_LENS[regNdx] * sumAB - sumA * sumB) /
                         Math.sqrt((REGION_LENS[regNdx] * sumASquared - sumA * sumA) * 
                                   (REGION_LENS[regNdx] * sumBSquared - sumB * sumB));
                  }

                  if (Float.isNaN(pearsonSum / 2)) {
                     System.out.printf("ndx %d and %d generated a nan!?\n", ndxA, ndxB);
                     System.exit(0);
                  }

                  if (USE_TRANSFORM) {
                     if ((pearsonSum / 2) >= ALPHA_THRESH) {
                        simMatrix[tmpSimNdx] = 1.0f;
                     }
                     else if ((pearsonSum / 2) < BETA_THRESH) {
                        simMatrix[tmpSimNdx] = 0.0f;
                     }
                  }

                  else { simMatrix[tmpSimNdx] = pearsonSum / 2; }
               }
            });
         }
      }

      try {
         mThreadPool.shutdown();
         mThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      }
      catch (SecurityException secErr) { secErr.printStackTrace(); }
      catch (InterruptedException intErr) { intErr.printStackTrace(); }

      return simMatrix;
   }
}
