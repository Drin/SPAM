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
import java.util.Set;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

import java.io.File;

public class FastSPAMEvaluationCPU {
   private final ExecutorService mThreadPool;

   private CPLOPConnection mConn;
   //private Clusterer mClusterer;
   //private Ontology mOntology;

   private static final short ISOLATE_LEN = 188;
   private static final byte LEN_23S     = 93,
                             LEN_16S     = 95;

   private static byte REGION_LENS[] = new byte[] {LEN_23S, LEN_16S};
   private static byte REGION_OFFSETS[] = new byte[] {0, LEN_23S};

   public FastSPAMEvaluationCPU(FastOntology ontology) {
      mThreadPool = Executors.newFixedThreadPool(64);
      mConn = null;
      /*
       * float threshList[] = new float[] {0.85, 0.75};
       */

      //mOntology = ontology;
      //mClusterer = new OHClusterer(ontology, threshList);

      try {
         mConn = new CPLOPConnection();
      }
      catch (Exception err) {
         err.printStackTrace();
      }
   }

   public static void main(String[] args) {
      FastSPAMEvaluationCPU runner = new FastSPAMEvaluationCPU(null);
      FastOntology clust_ont = FastOntology.createFastOntology(
         new File("ontologies/specific.ont")
      );

      short initSize = 100, upSize = 100, numUps = 10;
      int[][] simMapping;

      long start = System.currentTimeMillis();

      System.out.printf("initial: %d\nupdate size: %d\nnum ups: %d\n",
         initSize, upSize, numUps
      );

      try {
         //Get Data
         IsolateDataContainer container = runner.getIsolateData(clust_ont, initSize, upSize, numUps);
         FastOntology.mIsoMeta = container.isoMeta;
         FastOntologyTerm.mIsoLabels = container.isoMeta;

         //initialize cluster list
         List<FastCluster> clusters = new ArrayList<FastCluster>(initSize);

         //create a lookup table for isolate IDs to a spot in the packed similarity matrix
         simMapping = new int[container.isoIDs.length][];
         int simNdx = 0;

         for (short ndxA = 0; ndxA < container.isoIDs.length; ndxA++) {
            simMapping[ndxA] = new int[container.isoIDs.length - ndxA];

            for (short ndxB = (short) (ndxA + 1); ndxB < container.isoIDs.length; ndxB++) {
               try {
                  simMapping[ndxA][ndxB % (container.isoIDs.length - ndxA)] = simNdx++;
               }
               catch (Exception err) {
                  err.printStackTrace();
                  System.out.println("ended on ndxA: " + ndxA);
                  System.exit(0);
               }
            }
         }

         //populate the packed similarity matrix
         float simMatrix[] = runner.calculateSimMatrix(container.isoIDs, container.isoData);

         //set the FastCluster static variables needed for comparisons
         FastCluster.mNumIsolates = (short) container.isoIDs.length;
         FastCluster.mSimMatrix = simMatrix;
         FastCluster.mSimMapping = simMapping;

         //FastHierarchicalClusterer clusterer = new FastHierarchicalClusterer((short) container.isoIDs.length, 0.85f);
         FastHierarchicalClusterer clusterer = new FastOHClusterer(clust_ont, (short) container.isoIDs.length, 0.85f, 0.80f);

         List<FastCluster> tmpClusters = null;
         short isoStart = 0, isoEnd = initSize;
         long startCluster = System.currentTimeMillis(), finishCluster;

         for (byte currUp = (byte) (-1);  currUp < numUps; currUp++) {

            for (short isoNdx = isoStart; isoNdx < Math.min(isoEnd, container.isoIDs.length); isoNdx++) {
               clusters.add(new FastCluster(isoNdx));
            }

            tmpClusters = new ArrayList<FastCluster>(clusters);

            clusterer.clusterData(tmpClusters);

            finishCluster = System.currentTimeMillis() - start;

            System.out.printf("%d ms\n", finishCluster);
            System.out.printf("%d data size\n", clusters.size());
         }
      }
      catch (Exception ex) { ex.printStackTrace(); }

      System.out.printf("took about %d ms\n", System.currentTimeMillis() - start);
      FastCluster.shutdownThreadPool();
      System.out.println(clust_ont);
   }

   public IsolateDataContainer getIsolateData(FastOntology ont, short initSize, short upSize, short numUps) {
      short dataSize = (short) (initSize + (upSize * numUps));
      IsolateDataContainer dataContainer = null;

      try {
         dataContainer = mConn.getIsolateData(dataSize);
         dataContainer.isoMeta = mConn.getIsolateMetaData(dataContainer.isoIDs, ont, dataSize);
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }

      return dataContainer;
   }

   public final float[] calculateSimMatrix(final int[] isoIDs, final float[] isoData) {
      final short[] isoNdxA = new short[] {0}, isoNdxB = new short[] {0};
      final int[] simNdx = new int[] {0};
      final float simMatrix[]  = new float[(isoIDs.length * (isoIDs.length - 1)) / 2];

      for (isoNdxA[0] = 0; isoNdxA[0] < isoIDs.length; isoNdxA[0]++) {
         for (isoNdxB[0] = (short) (isoNdxA[0] + 1); isoNdxB[0] < isoIDs.length; isoNdxB[0]++) {
            mThreadPool.execute(new Runnable() {
               private final short ndxA = isoNdxA[0], ndxB = isoNdxB[0];
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

                  simMatrix[tmpSimNdx] = pearsonSum / 2;
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
