package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.types.DataObject;
import com.drin.java.types.Cluster;
import com.drin.java.types.HierarchicalCluster;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterComparator;

import com.drin.java.util.Configuration;

import java.util.Set;
import java.util.HashSet;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   private static final int CLUSTER_PAIR_SIZE = 2;
   private static final boolean METRIC_IS_DISTANCE = false,
                                DEBUG = false;

   public AgglomerativeClusterer(Set<Cluster> clusters) {
      super(clusters);
   }

   public AgglomerativeClusterer(Set<Cluster> clusters,
    DataMetric clustMetric, ClusterComparator clustComp) {
      super(clusters, clustMetric, clustComp);
   }

   @Override
   protected Cluster[] findCloseClusters(Set<Cluster> clusterSet) {
      Cluster<DataObject> closeClust_A = null, closeClust_B = null;
      double minDist = Double.MAX_VALUE, maxSim = 0;

      for (Cluster<DataObject> clust_A : clusterSet) {
         for (Cluster<DataObject> clust_B : clusterSet) {

            if (!clust_A.equals(clust_B)) {
               Double clustDist = mClusterComp.compare(mDataMetric, clust_A, clust_B);

               if (DEBUG) {
                  System.out.printf("agglomerative clusterer:\n\tcomparison between '%s' and '%s' is %.04f\n",
                   clust_A.getName(), clust_B.getName(), clustDist);
               }

               if (!METRIC_IS_DISTANCE) {
                  if (clustDist != null && clustDist.doubleValue() > maxSim &&
                      clustDist.doubleValue() > mBetaThreshold) {
                     closeClust_A = clust_A;
                     closeClust_B = clust_B;
                     maxSim = clustDist.doubleValue();
                  }
               }

               else {
                  if (clustDist != null && clustDist.doubleValue() < minDist) {
                     closeClust_A = clust_A;
                     closeClust_B = clust_B;
                     minDist = clustDist.doubleValue();
                  }
               }
            }
         }
      }

      if (closeClust_A != null && closeClust_B != null) {
         return new Cluster[] {closeClust_A, closeClust_B};
      }

      return null;
   }

   @Override
   protected Set<Cluster> combineCloseClusters(Cluster[] closestClusters,
    Set<Cluster> clusterSet) {
      Set<Cluster> newClusterSet = new HashSet<Cluster>();

      if (closestClusters.length != CLUSTER_PAIR_SIZE) {
         System.err.printf("Invalid cluster pair to be combined\n");
         System.exit(1);
      }

      for (Cluster<DataObject> cluster_A : clusterSet) {
         if (cluster_A.equals(closestClusters[0])) {
            Cluster<DataObject> cluster_B = closestClusters[1];
            newClusterSet.add(new HierarchicalCluster(cluster_A, cluster_B));
         }

         else if (cluster_A.equals(closestClusters[1])) { continue; }
         else { newClusterSet.add(cluster_A); }
      }

      return newClusterSet;
   }
}
