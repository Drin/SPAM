package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Configuration;

import java.util.Set;
import java.util.HashSet;

public abstract class HierarchicalClusterer implements Clusterer {
   protected Set<Cluster> mClusters;
   protected Set<Cluster> mResultClusters;

   protected DataMetric<Cluster> mMetric;

   public HierarchicalClusterer(Set<Cluster> clusters,
                                DataMetric<Cluster> metric) {
      mClusters = clusters;
      mMetric = metric;

      mResultClusters = new HashSet<Cluster>();
   }

   protected Set<Cluster> clusterDataSet(Set<Cluster> clusterSet) {
      Set<Cluster> newClustSet = new HashSet<Cluster>(clusterSet);

      while (newClustSet.size() > 1) {
         Cluster[] closeClusters = findCloseClusters(newClustSet);

         if (closeClusters != null) {
            newClustSet = combineClusters(closeClusters, newClustSet);
         }

         else { break; }
      }

      return newClustSet;
   }

   @Override
   public void clusterData() {
      for (Cluster cluster : clusterDataSet(mClusters)) {
         mResultClusters.add(cluster);
      }
   }

   public Set<Cluster> getClusters() {
      Set<Cluster> resultClusters = new HashSet<Cluster>();

      for (Cluster cluster : mResultClusters) {
         resultClusters.add(cluster);
      }

      return resultClusters;
   }

   protected abstract Cluster[] findCloseClusters(Set<Cluster> clusterSet);
   protected abstract Set<Cluster> combineClusters(Cluster[] closeClusters,
                                                   Set<Cluster> clusterSet);
}
