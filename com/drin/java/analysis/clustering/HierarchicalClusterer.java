package com.drin.java.analysis.clustering;

import com.drin.java.types.Cluster;
import com.drin.java.types.DataObject;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterComparator;

import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.ClusterAnalyzer;

import com.drin.java.util.Configuration;

import java.util.Set;
import java.util.HashSet;

public abstract class HierarchicalClusterer implements Clusterer {
   private static final boolean DEBUG = false;

   protected Set<Cluster> mClusters;
   protected Set<Cluster<DataObject>> mResultClusters;

   protected DataMetric mDataMetric;
   protected ClusterComparator mClusterComp;

   protected double mBetaThreshold;

   public HierarchicalClusterer(Set<Cluster> clusters,
    DataMetric dataMetric, ClusterComparator clustComp) {
      mClusters = clusters;
      mDataMetric = dataMetric;
      mClusterComp = clustComp;

      mBetaThreshold = 0;

      mResultClusters = new HashSet<Cluster<DataObject>>();
   }

   public HierarchicalClusterer(Set<Cluster> clusters) {
      this(clusters, null, null);
   }

   public void setBetaThreshold(double beta) {
      mBetaThreshold = beta;
   }

   protected Set<Cluster> clusterDataSet(Set<Cluster> clusterSet) {
      Set<Cluster> newClusterSet = new HashSet<Cluster>(clusterSet);

      while (newClusterSet.size() > 1) {
         Cluster[] closestClusters = findCloseClusters(newClusterSet);

         if (closestClusters != null) {
            newClusterSet = combineCloseClusters(closestClusters, newClusterSet);
         }

         else { break; }
      }

      return newClusterSet;
   }

   @Override
   public void clusterData() {
      for (Cluster cluster : clusterDataSet(mClusters)) {
         mResultClusters.add(cluster);
      }

      if (DEBUG) {
         /*
         System.out.println("result clusters:");
         for (Cluster cluster : mResultClusters) {
            System.out.println(cluster.getName());
         }
         */
      }
   }

   public String[] getResults(ClusterAnalyzer analyzer) {
      if (mResultClusters != null) {
         analyzer.analyzeClusters(mResultClusters);

         String results = String.format("%s\n%s\n",
          analyzer.getStats(), analyzer.getDendogram());

         String elements = String.format("%s\n",
          analyzer.getClusterElements());

         return new String[] { results, elements };
      }

      return new String[] { "No results to report\n" };
   }

   protected abstract Cluster[] findCloseClusters(Set<Cluster> clusterSet);
   protected abstract Set<Cluster> combineCloseClusters(Cluster[] closestClusters, Set<Cluster> clusterSet);
}
