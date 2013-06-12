package com.drin.java.analysis.clustering;

import com.drin.java.clustering.FastCluster;
import com.drin.java.ontology.FastOntology;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.List;
import java.util.ArrayList;

public class FastHierarchicalClusterer {
   //private static final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
   protected List<FastCluster> mResultClusters;
   protected float mThresh;

   public FastHierarchicalClusterer(int dataSize, float threshold) {
      mResultClusters = new ArrayList<FastCluster>(dataSize);
      mThresh = threshold;
   }

   public List<FastCluster> getClusters() { return mResultClusters; }
   public float getThreshold() { return mThresh; }

   public void persistentClusterData(List<FastCluster> clusters) {
      mResultClusters.addAll(clusters);
      clusterDataSet(mResultClusters, mThresh);
   }

   public void clusterData(List<FastCluster> clusters) {
      mResultClusters = clusters;
      clusterDataSet(mResultClusters, mThresh);
   }

   public void clusterData(FastOntology clustOnt) { throw new UnsupportedOperationException(); }

   protected void clusterDataSet(List<FastCluster> clusters, float threshold) {
      int closeA = -1, closeB = -1;
      float maxSim = 0;

      do {
         closeA = closeB = -1;
         maxSim = 0;

         for (int ndxA = 0; ndxA < clusters.size(); ndxA++) {
            FastCluster clustA = clusters.get(ndxA);

            for (int ndxB = (ndxA + 1); ndxB < clusters.size(); ndxB++) {
               FastCluster clustB = clusters.get(ndxB);

               float clustSim = clustA.compareTo(clustB);
               if (clustSim > maxSim && clustSim > threshold) {
                  closeA = ndxA;
                  closeB = ndxB;
                  maxSim = clustSim;
               }
            }
         }

         if (closeA != -1 && closeB != -1) {
            clusters.get(closeA).incorporate(clusters.get(closeB));
            clusters.remove(closeB);
         }
      } while (closeA != -1 && closeB != -1 && clusters.size() > 1);
   }

   /*
   protected void clusterDataSet(List<FastCluster> clusters, final float threshold) {
      final Object lockObj = new Object();
      final float[] maxSim = new float[] { 0.0f };
      final int[] closeA = new int[] { -1 },
                  closeB = new int[] { -1 };

      do {
         closeA[0] = closeB[0] = -1;
         maxSim[0] = 0.0f;

         for (int ndxA = 0; ndxA < clusters.size(); ndxA++) {
            final FastCluster clustA = clusters.get(ndxA);

            for (int ndxB = ndxA + 1; ndxB < clusters.size(); ndxB++) {
               final int tmpNdxA = ndxA, tmpNdxB = ndxB;
               final FastCluster clustB = clusters.get(ndxB);

               mThreadPool.execute(new Runnable() {
                  public void run() {
                     float clustSim = clustA.compareTo(clustB);

                     synchronized (lockObj) {
                        if (clustSim > maxSim[0] && clustSim > threshold) {
                           closeA[0] = tmpNdxA;
                           closeB[0] = tmpNdxB;
                           maxSim[0] = clustSim;
                        }
                     }
                  }

               });
            }
         }

         if (closeA[0] != -1 && closeB[0] != -1) {
            clusters.get(closeA[0]).incorporate(clusters.get(closeB[0]));
            clusters.remove(closeB[0]);
         }

      } while (closeA[0] != -1 && closeB[0] != -1 && clusters.size() > 1);
   }
   */

   public float getInterStrainSim() {
      float totalClusterSimilarity = 0;
      int similarityCount = 0;

      for (int clustNdxA = 0; clustNdxA < mResultClusters.size(); clustNdxA++) {
         FastCluster clustA = mResultClusters.get(clustNdxA);

         for (int clustNdxB = (clustNdxA + 1); clustNdxB < mResultClusters.size(); clustNdxB++) {
            FastCluster clustB = mResultClusters.get(clustNdxB);

            totalClusterSimilarity += clustA.compareTo(clustB);
            similarityCount++;
         }
      }

      if (similarityCount > 0) {
         return totalClusterSimilarity/similarityCount;
      }
      else { return -1; }
   }

   /*
   public static void shutdownThreadPool() {
      mThreadPool.shutdown();
   }
   */

   public class FastClusterPair {
      public int mClustANdx, mClustBNdx;
      public float mClustSim;

      public FastClusterPair(int clustANdx, int clustBNdx, float clustSim) {
         mClustANdx = clustANdx;
         mClustBNdx = clustBNdx;
         mClustSim = clustSim;
      }
   }
}
