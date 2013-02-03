package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.CandidatePair;
import com.drin.java.clustering.CandidateQueue;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   protected static final int CLUSTER_PAIR_SIZE = 2;

   public AgglomerativeClusterer(List<Cluster> clusters, double threshold) {
      super(clusters, threshold);
   }

   @Override
   protected CandidateQueue findCandidatePairs(List<Cluster> clusters, double threshold) {
      CandidateQueue clusterCandidates = new CandidateQueue();

      for (int clustNdx_A = 0; clustNdx_A < clusters.size(); clustNdx_A++) {
         Cluster clust_A = clusters.get(clustNdx_A);

         for (int clustNdx_B = clustNdx_A + 1; clustNdx_B < clusters.size(); clustNdx_B++) {
            Cluster clust_B = clusters.get(clustNdx_B);
            double clustSim = clust_A.compareTo(clust_B);

            if (clustSim > threshold) {
               clusterCandidates.addCandidate(new CandidatePair(clust_A, clust_B, clustSim));
            }
         }
      }

      return clusterCandidates;
   }

   @Override
   protected CandidateQueue recompute(Cluster combinedCluster, List<Cluster> clusters, double threshold) {
      CandidateQueue clusterCandidates = new CandidateQueue();

      for (Cluster otherClust : clusters) {
         if (combinedCluster.getName().equals(otherClust.getName())) { continue; }

         double clustSim = combinedCluster.compareTo(otherClust);

         if (clustSim > threshold) {
            clusterCandidates.addCandidate(new CandidatePair(combinedCluster, otherClust, clustSim));
         }
      }

      return clusterCandidates;
   }

   @Override
   protected Cluster combineClusters(CandidatePair closeClusters, List<Cluster> clusters) {
      Logger.debug(String.format("combining clusters '%s' and '%s'",
                                 closeClusters.getLeftClusterName(),
                                 closeClusters.getRightClusterName()));

      int removeNdx = -1;
      Cluster combinedCluster = null;

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         Cluster tmpClust = clusters.get(clustNdx);

         if (tmpClust.getName().equals(closeClusters.getLeftClusterName())) {
            combinedCluster = tmpClust.join(closeClusters.getRightCluster());

            clusters.set(clustNdx, combinedCluster);
         }

         else if (tmpClust.getName().equals(closeClusters.getRightClusterName())) {
            removeNdx = clustNdx;
         }
      }

      if (removeNdx != -1) {
         clusters.remove(removeNdx);
      }
      else {
         Logger.debug("Remove Index is -1. Error during clustering.\nCluster List:\n");
      }

      return combinedCluster;
   }
}
