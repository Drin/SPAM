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
   protected CandidatePair findCloseClusters(Map<String, Map<String, Double>> distMap, List<Cluster> clusters, double threshold) {
      Map<String, Double> clustDistMap = null;
      Cluster close_A = null, close_B = null;
      double maxSim = 0;

      for (int ndx_A = 0; ndx_A < clusters.size(); ndx_A++) {
         Cluster clust_A = clusters.get(ndx_A);

         if (!distMap.containsKey(clust_A.getName())) {
            distMap.put(clust_A.getName(), new HashMap<String, Double>());
         }

         clustDistMap = distMap.get(clust_A.getName());

         for (int ndx_B = ndx_A + 1; ndx_B < clusters.size(); ndx_B++) {
            Cluster clust_B = clusters.get(ndx_B);

            if (!clustDistMap.containsKey(clust_B.getName())) {
               clustDistMap.put(clust_B.getName(), clust_A.compareTo(clust_B));
            }

            double clustDist = clustDistMap.get(clust_B.getName());

            if (clustDist > maxSim && clustDist > threshold) {
               close_A = clust_A;
               close_B = clust_B;
               maxSim = clustDist;
            }
         }

         clustDistMap = null;
      }

      if (close_A != null && close_B != null) {
         return new CandidatePair(close_A, close_B, maxSim);
      }

      return null;
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
