package com.drin.java.analysis.clustering;

import java.lang.reflect.Array;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   protected static final int CLUSTER_PAIR_SIZE = 2;

   private double mThreshold;

   public AgglomerativeClusterer(Set<Cluster> clusters, double thresh) {
      super(clusters);
      mThreshold = thresh;
   }

   @Override
   protected Cluster[] findCloseClusters(Set<Cluster> clusterSet) {
      double maxSim = 0;
      Cluster closeClust_A = null, closeClust_B = null;

      for (Cluster clust_A : clusterSet) {
         for (Cluster clust_B : clusterSet) {
            if (clust_A.isSimilar(clust_B)) { continue; }

            double clustDist = clust_A.compareTo(clust_B);

            if (clustDist > maxSim && clustDist > mThreshold) {
               closeClust_A = clust_A;
               closeClust_B = clust_B;

               maxSim = clustDist;
            }
         }
      }

      if (closeClust_A != null && closeClust_B != null) {
         @SuppressWarnings(value={"unchecked", "rawtypes"})
         Cluster[] closeClusters = new Cluster[] {closeClust_A, closeClust_B};
         return closeClusters;
      }

      return null;
   }

   @Override
   protected Set<Cluster> combineClusters(Cluster[] closeClusters,
                                          Set<Cluster> clusterSet) {
      Set<Cluster> newClusterSet = new HashSet<Cluster>();

      if (closeClusters.length != CLUSTER_PAIR_SIZE) {
         Logger.error(-1, "Invalid cluster pair to be combined\n");
      }

      for (Cluster clust_A : clusterSet) {
         if (clust_A.isSimilar(closeClusters[0])) {
            Cluster clust_B = closeClusters[1];
            newClusterSet.add(clust_A.join(clust_B));
         }

         else if (clust_A.equals(closeClusters[1])) { continue; }
         else { newClusterSet.add(clust_A); }
      }

      return newClusterSet;
   }
}
