package com.drin.java.metrics;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.DataComparator;
import com.drin.java.metrics.ClusterMetric;

import com.drin.java.util.Logger;

public class ClusterComparator<E extends BaseClusterable> implements
             DataComparator<ClusterMetric<E>, Cluster<E>> {

   @Override
   public double compare(ClusterMetric<E> clusterMetric,
                         Cluster<E> clust_A, Cluster<E> clust_B) {
      clusterMetric.apply(clust_A, clust_B);

      double comparison = clusterMetric.result();

      Logger.error(clusterMetric.getError(),
                   String.format("error computing metric between '%s' " +
                                 "and '%s'\n", clust_A.getName(),
                                 clust_B.getName()));

      return comparison;
   }

   @Override
   public boolean isSimilar(ClusterMetric<E> clusterMetric,
                            Cluster<E> clust_A, Cluster<E> clust_B) {
      throw new UnsupportedOperationException();
   }
}
