package com.drin.java.analysis.clustering;

import java.lang.reflect.Array;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.HCluster;

import com.drin.java.metrics.ClusterMetric;
import com.drin.java.metrics.ClusterComparator;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public class AgglomerativeClusterer<E extends BaseClusterable> extends
             HierarchicalClusterer<E> {
   protected static final int CLUSTER_PAIR_SIZE = 2;
   protected static final boolean METRIC_IS_DISTANCE = false;

   private double mThreshold;

   public AgglomerativeClusterer(Set<HCluster<E>> clusters, double thresh,
                                 ClusterMetric<E> metric,
                                 ClusterComparator<E> comp) {
      super(clusters, metric, comp);
      mThreshold = thresh;
   }

   @Override
   protected HCluster<E>[] findCloseClusters(Set<HCluster<E>> clusterSet) {
      double minDist = Double.MAX_VALUE, maxSim = 0;
      HCluster<E> closeClust_A = null, closeClust_B = null;

      for (HCluster<E> clust_A : clusterSet) {
         for (HCluster<E> clust_B : clusterSet) {

            if (clust_A.getName().equals(clust_B.getName())) { continue; }

            double clustDist = mComp.compare(mMetric, clust_A, clust_B);

            Logger.debug(String.format("comparison between cluster '%s' and " +
                                       "cluster '%s' is %.04f", 
                                       clust_A.getName(), clust_B.getName(),
                                       clustDist));

            if (clustDist > maxSim && clustDist > mThreshold) {
               closeClust_A = clust_A;
               closeClust_B = clust_B;
               maxSim = clustDist;
            }

            else if (clustDist < minDist && clustDist < mThreshold) {
               closeClust_A = clust_A;
               closeClust_B = clust_B;
               minDist = clustDist;
            }
         }
      }

      if (closeClust_A != null && closeClust_B != null) {
         @SuppressWarnings(value={"unchecked", "rawtypes"})
         HCluster<E>[] closeClusters = new HCluster[] {closeClust_A, closeClust_B};
         return closeClusters;
      }

      return null;
   }

   @Override
   protected Set<HCluster<E>> combineClusters(HCluster<E>[] closeClusters,
                                              Set<HCluster<E>> clusterSet) {
      Set<HCluster<E>> newClusterSet = new HashSet<HCluster<E>>();

      if (closeClusters.length != CLUSTER_PAIR_SIZE) {
         Logger.error(-1, "Invalid cluster pair to be combined\n");
      }

      for (HCluster<E> clust_A : clusterSet) {
         if (clust_A.getName().equals(closeClusters[0].getName())) {
            HCluster<E> clust_B = closeClusters[1];
            newClusterSet.add(clust_A.join(clust_B));
         }

         else if (clust_A.equals(closeClusters[1])) { continue; }
         else { newClusterSet.add(clust_A); }
      }

      return newClusterSet;
   }
}
