package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   protected static final int CLUSTER_PAIR_SIZE = 2;

   public AgglomerativeClusterer(Set<Cluster> clusters) {
      super(clusters);
   }

   @Override
   protected Cluster[] findCloseClusters(Set<Cluster> clusterSet) {
      double maxSim = 0;
      Cluster closeClust_A = null, closeClust_B = null;

      for (Cluster clust_A : clusterSet) {
         for (Cluster clust_B : clusterSet) {
            if (clust_A.getName().equals(clust_B.getName())) { continue; }

            double clustDist = clust_A.compareTo(clust_B);

            if (clustDist > maxSim && !clust_A.isDifferent(clust_B)) {
               closeClust_A = clust_A;
               closeClust_B = clust_B;

               maxSim = clustDist;
            }
         }
      }

      if (closeClust_A != null && closeClust_B != null) {
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


      Logger.debug(String.format("combining clusters '%s' and '%s'",
                                 closeClusters[0], closeClusters[1]));

      for (Cluster clust_A : clusterSet) {
         if (clust_A.getName().equals(closeClusters[0].getName())) {
            Cluster clust_B = closeClusters[1];
            newClusterSet.add(clust_A.join(clust_B));
         }

         else if (clust_A.getName().equals(closeClusters[1].getName())) {
            continue;
         }
         else { newClusterSet.add(clust_A); }
      }

      return newClusterSet;
   }
}
