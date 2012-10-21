package com.drin.java.analysis.clustering;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.HCluster;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.ClusterMetric;
import com.drin.java.metrics.ClusterComparator;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.util.Configuration;

import java.util.Set;
import java.util.HashSet;

public abstract class HierarchicalClusterer<E extends BaseClusterable> implements Clusterer<E> {
   protected Set<HCluster<E>> mClusters;
   protected Set<HCluster<E>> mResultClusters;

   protected ClusterMetric<E> mMetric;
   protected ClusterComparator<E> mComp;

   protected double mThreshold;

   public HierarchicalClusterer(Set<HCluster<E>> clusters, double threshold,
                                ClusterMetric<E> metric,
                                ClusterComparator<E> comp) {
      mThreshold = 0;
      mClusters = clusters;
      mMetric = metric;
      mComp = comp;

      mResultClusters = new HashSet<HCluster<E>>();
   }

   protected Set<HCluster<E>> clusterDataSet(Set<HCluster<E>> clusterSet) {
      Set<HCluster<E>> newClustSet = new HashSet<HCluster<E>>(clusterSet);

      while (newClustSet.size() > 1) {
         HCluster<E>[] closeClusters = findCloseClusters(newClustSet);

         if (closeClusters != null) {
            newClustSet = combineClusters(closeClusters, newClustSet);
         }

         else { break; }
      }

      return newClustSet;
   }

   @Override
   public void clusterData() {
      for (HCluster<E> cluster : clusterDataSet(mClusters)) {
         mResultClusters.add(cluster);
      }

      if (System.getenv().containsKey("DEBUG")) {
         /*
         System.out.println("result clusters:");
         for (Cluster cluster : mResultClusters) {
            System.out.println(cluster.getName());
         }
         */
      }
   }

   public Set<Cluster<E>> getClusters() {
      Set<Cluster<E>> resultClusters = new HashSet<Cluster<E>>();

      for (HCluster<E> cluster : mResultClusters) {
         resultClusters.add(cluster);
      }

      return resultClusters;
   }

   protected abstract HCluster<E>[] findCloseClusters(Set<HCluster<E>> clusterSet);
   protected abstract Set<HCluster<E>> combineClusters(HCluster<E>[] closeClusters,
                                                       Set<HCluster<E>> clusterSet);
}
