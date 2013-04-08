package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.CandidatePair;
import com.drin.java.clustering.CandidateQueue;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class AgglomerativeClusterer extends HierarchicalClusterer {

   public AgglomerativeClusterer(List<Double> thresholds) {
      super(thresholds);
   }

   @Override
   protected CandidatePair findCloseClusters(List<Cluster> clusters, double threshold) {
      Map<String, Double> clustSimMap = null;
      Cluster close_A = null, close_B = null;
      double maxSim = 0;

      for (int ndx_A = 0; ndx_A < clusters.size(); ndx_A++) {
         Cluster clust_A = clusters.get(ndx_A);

         if (!mSimMap.containsKey(clust_A.getName())) {
            mSimMap.put(clust_A.getName(), new HashMap<String, Double>());
         }

         clustSimMap = mSimMap.get(clust_A.getName());

         for (int ndx_B = ndx_A + 1; ndx_B < clusters.size(); ndx_B++) {
            Cluster clust_B = clusters.get(ndx_B);

            if (!clustSimMap.containsKey(clust_B.getName())) {
               clustSimMap.put(clust_B.getName(), clust_A.compareTo(clust_B));
            }

            double clustDist = clustSimMap.get(clust_B.getName());

            if (clustDist > maxSim && clustDist > threshold) {
               close_A = clust_A;
               close_B = clust_B;
               maxSim = clustDist;
            }
         }

         clustSimMap = null;
      }

      if (close_A != null && close_B != null) {
         return new CandidatePair(close_A, close_B, maxSim);
      }

      return null;
   }

   @Override
   protected Cluster combineClusters(CandidatePair closeClusters, List<Cluster> clusters) {
      int removeNdx = -1;
      Cluster combinedCluster = null;

      Logger.debug(String.format("combining clusters '%s' and '%s'",
                                 closeClusters.getLeftClusterName(),
                                 closeClusters.getRightClusterName()));

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

      if (removeNdx != -1) { clusters.remove(removeNdx); }
      else {
         Logger.debug("Remove Index is -1. Error during clustering.\nCluster List:\n");
      }

      return combinedCluster;
   }
}
