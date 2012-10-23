package com.drin.java.analysis.clustering;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.ClusterMetric;
import com.drin.java.metrics.ClusterComparator;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.util.Configuration;

import java.util.Set;
import java.util.HashSet;

public abstract class HierarchicalClusterer<E extends BaseClusterable> implements Clusterer<E> {
   protected Set<Cluster<E>> mClusters;
   protected Set<Cluster<E>> mResultClusters;

   protected ClusterMetric<E> mMetric;
   protected ClusterComparator<E> mComp;

   public HierarchicalClusterer(Set<Cluster<E>> clusters,
                                ClusterMetric<E> metric,
                                ClusterComparator<E> comp) {
      mClusters = clusters;
      mMetric = metric;
      mComp = comp;

      mResultClusters = new HashSet<Cluster<E>>();
   }

   protected Set<Cluster<E>> clusterDataSet(Set<Cluster<E>> clusterSet) {
      Set<Cluster<E>> newClustSet = new HashSet<Cluster<E>>(clusterSet);

      while (newClustSet.size() > 1) {
         Cluster<E>[] closeClusters = findCloseClusters(newClustSet);

         if (closeClusters != null) {
            newClustSet = combineClusters(closeClusters, newClustSet);
         }

         else { break; }
      }

      return newClustSet;
   }

   @Override
   public void clusterData() {
      for (Cluster<E> cluster : clusterDataSet(mClusters)) {
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

      for (Cluster<E> cluster : mResultClusters) {
         resultClusters.add(cluster);
      }

      return resultClusters;
   }

   protected abstract Cluster<E>[] findCloseClusters(Set<Cluster<E>> clusterSet);
   protected abstract Set<Cluster<E>> combineClusters(Cluster<E>[] closeClusters,
                                                      Set<Cluster<E>> clusterSet);
}
