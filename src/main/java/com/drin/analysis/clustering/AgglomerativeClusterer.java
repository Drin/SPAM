package com.drin.analysis.clustering;

import com.drin.clustering.Cluster;
import com.drin.clustering.CandidatePair;

import com.drin.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class AgglomerativeClusterer extends HierarchicalClusterer {

   public AgglomerativeClusterer() { super(); }

   @Override
   protected CandidatePair findCloseClusters(Map<String, Map<String, Double>> distMap, List<Cluster> clusters) {
      Map<String, Double> clustDistMap = null;
      Integer closeNdxA = null, closeNdxB = null;

      double maxSimilarity = 0d;

      for (int ndxA = 0; ndxA < clusters.size(); ndxA++) {
         Cluster clustA = clusters.get(ndxA);

         if (!distMap.containsKey(clustA.getName())) {
            distMap.put(clustA.getName(), new HashMap<String, Double>());
         }

         clustDistMap = distMap.get(clustA.getName());

         for (int ndxB = ndxA + 1; ndxB < clusters.size(); ndxB++) {
            Cluster clustB = clusters.get(ndxB);

            if (!clustDistMap.containsKey(clustB.getName())) {
               clustDistMap.put(clustB.getName(), clustA.compareTo(clustB));
            }

            double clustDist = clustDistMap.get(clustB.getName());

            if (clustDist > maxSim && clustDist > threshold) {
               closeA = clustA;
               closeB = clustB;
               maxSim = clustDist;
            }
         }

         clustDistMap = null;
      }

      if (closeA != null && closeB != null) {
         return new CandidatePair(closeA, closeB, maxSim);
      }

      return null;
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
