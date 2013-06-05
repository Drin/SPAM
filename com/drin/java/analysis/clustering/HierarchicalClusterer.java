package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.ontology.Ontology;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.swing.JTextArea;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public abstract class HierarchicalClusterer implements Clusterer {
   private static final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
   protected Map<Float, List<Cluster>> mResultClusters;
   protected float mThresh;

   protected String mName = "Hierarchical";

   public HierarchicalClusterer(int dataSize, float threshold) {
      mResultClusters = new HashMap<Float, List<Cluster>>();
      mThresh = threshold;
   }

   /* public static void shutdownThreadPool() { mThreadPool.shutdown(); } */

   //TODO
   public void setProgressCanvas(JTextArea canvas) { return; }
   public void writeProgress() { return; }
   public float getInterClusterSimilarity() { return -2.0f; }

   public Map<Float, List<Cluster>> getClusters() { return mResultClusters; }
   public float getThreshold() { return mThresh; }
   public String getName() { return mName; }

   public void clusterData(List<Cluster> clusters) {
      clusterDataSet(clusters, mThresh);
      mResultClusters.put(mThresh, clusters);
   }

   public void clusterData(Ontology clustOnt) {
      throw new UnsupportedOperationException();
   }

   protected void clusterDataSet(List<Cluster> clusters, float threshold) {
      boolean combineSuccess = false;

      do {
         ClusterPair clustPair = findCloseClusters(clusters, threshold);

         combineSuccess = combineClusters(clusters, clustPair);

      } while (combineSuccess && clusters.size() > 1);
   }

   protected abstract ClusterPair findCloseClusters(List<Cluster> clusters, float threshold);
   protected abstract boolean combineClusters(List<Cluster> clusters, ClusterPair clustPair);

   /*
   protected void clusterDataSet(List<Cluster> clusters, final float threshold) {
      final Object lockObj = new Object();
      final float[] maxSim = new float[] { 0.0f };
      final int[] closeA = new int[] { -1 },
                  closeB = new int[] { -1 };

      do {
         closeA[0] = closeB[0] = -1;
         maxSim[0] = 0.0f;

         for (int ndxA = 0; ndxA < clusters.size(); ndxA++) {
            final Cluster clustA = clusters.get(ndxA);

            for (int ndxB = ndxA + 1; ndxB < clusters.size(); ndxB++) {
               final int tmpNdxA = ndxA, tmpNdxB = ndxB;
               final Cluster clustB = clusters.get(ndxB);

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

      List<Cluster> clusters = mResultClusters.get(mThresh);

      for (int clustNdxA = 0; clustNdxA < clusters.size(); clustNdxA++) {
         Cluster clustA = clusters.get(clustNdxA);

         for (int clustNdxB = clustNdxA + 1; clustNdxB < clusters.size(); clustNdxB++) {
            Cluster clustB = clusters.get(clustNdxB);

            totalClusterSimilarity += clustA.compareTo(clustB);
            similarityCount++;
         }
      }

      if (similarityCount > 0) {
         return totalClusterSimilarity/similarityCount;
      }
      else { return -1; }
   }

   public class ClusterPair {
      public int mClustANdx, mClustBNdx;
      public float mClustSim;

      public ClusterPair(int clustANdx, int clustBNdx, float clustSim) {
         mClustANdx = clustANdx;
         mClustBNdx = clustBNdx;
         mClustSim = clustSim;
      }
   }
}
