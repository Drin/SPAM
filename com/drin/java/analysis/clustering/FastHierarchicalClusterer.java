package com.drin.java.analysis.clustering;

import com.drin.java.clustering.FastCluster;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class FastHierarchicalClusterer {
   protected List<FastCluster> mResultClusters;
   protected float mThresh;

   public FastHierarchicalClusterer(float threshold) {
      mResultClusters = new HashMap<Double, List<FastCluster>>();
      mThresh = threshold;
   }

   public Map<Double, List<FastCluster>> getClusters() { return mResultClusters; }

   public void persistentClusterData(List<FastCluster> clusters) {
      for (Double threshold : mThresholds) {
         if (!mResultClusters.containsKey(threshold)) {
            mResultClusters.put(threshold, new ArrayList<FastCluster>(clusters.size()));
         }

         List<FastCluster> resultClusters = mResultClusters.get(threshold);
         resultClusters.addAll(clusters);

         clusterDataSet(resultClusters, threshold);

         mResultClusters.put(threshold, new ArrayList<FastCluster>(resultClusters));
      }
   }

   @Override
   public void clusterData(List<FastCluster> clusters) {
      mResultClusters = clusters;
      clusterDataSet(clusters, mThresh);
   }

   protected void clusterDataSet(List<FastCluster> clusters, float threshold) {
      short closeA = -1, closeB = -1;
      float maxSim = 0;

      do {
         closeA = closeB = -1;

         for (short ndxA = 0; ndxA < clusters.size(); ndxA++) {
            FastCluster clustA = clusters.get(ndxA);

            for (short ndxB = ndxA + 1; ndxB < clusters.size(); ndxB++) {
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
            clusters.set(closeA, clusters.get(closeA).incorporate(clusters.get(closeB)));
            clusters.remove(closeB);
         }
      } while (closeA != -1 && closeB != -1 && clusters.size() > 1);
   }

   //TODO
   public double getInterClusterSimilarity() {
      Double lowThreshold = new Double(100);
      double totalClusterSimilarity = 0, similarityCount = 0;

      for (Double thresh : mResultClusters.keySet()) {
         if (thresh.compareTo(lowThreshold) < 0) { lowThreshold = thresh; }
      }

      List<FastCluster> clusters = mResultClusters.get(lowThreshold);
      for (int clustNdx_A = 0; clustNdx_A < clusters.size(); clustNdx_A++) {
         Cluster clust_A = clusters.get(clustNdx_A);

         for (int clustNdx_B = clustNdx_A + 1; clustNdx_B < clusters.size(); clustNdx_B++) {
            Cluster clust_B = clusters.get(clustNdx_B);

            if (mSimMap.containsKey(clust_A.getName())) {
               Map<String, Double> simMap = mSimMap.get(clust_A.getName());

               if (simMap.containsKey(clust_B.getName())) {
                  totalClusterSimilarity += simMap.get(clust_B.getName());
                  similarityCount++;
               }

            }
         }
      }

      return totalClusterSimilarity/similarityCount;
   }

   public class FastClusterPair {
      public short mClustANdx, mClustBNdx;
      public float mClustSim;

      public FastClusterPair(short clustANdx, short clustBNdx, float clustSim) {
         mClustANdx = clustANdx;
         mClustBNdx = clustBNdx;
         mClustSim = clustSim;
      }
   }
}
