package com.drin.java.analysis.clustering;

import com.drin.java.clustering.FastCluster;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class FastHierarchicalClusterer {
   protected List<FastCluster> mResultClusters;
   protected float mThresh;

   public FastHierarchicalClusterer(short dataSize, float threshold) {
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

   protected void clusterDataSet(List<FastCluster> clusters, float threshold) {
      short closeA = -1, closeB = -1;
      float maxSim = 0;

      do {
         closeA = closeB = -1;

         for (short ndxA = 0; ndxA < clusters.size(); ndxA++) {
            FastCluster clustA = clusters.get(ndxA);

            for (short ndxB = (short) (ndxA + 1); ndxB < clusters.size(); ndxB++) {
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

   public float getInterStrainSim() {
      float totalClusterSimilarity = 0;
      short similarityCount = 0;

      for (short clustNdxA = 0; clustNdxA < mResultClusters.size(); clustNdxA++) {
         FastCluster clustA = mResultClusters.get(clustNdxA);

         for (short clustNdxB = (short) (clustNdxA + 1); clustNdxB < mResultClusters.size(); clustNdxB++) {
            FastCluster clustB = mResultClusters.get(clustNdxB);

            totalClusterSimilarity += clustA.compareTo(clustB);
            similarityCount++;
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
