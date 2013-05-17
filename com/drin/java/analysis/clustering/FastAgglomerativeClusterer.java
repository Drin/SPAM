package com.drin.java.analysis.clustering;

import com.drin.java.clustering.FastCluster;
import com.drin.java.clustering.FastHierarchicalClusterer;
import com.drin.java.clustering.FastHierarchicalClusterer.FastClusterPair;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class FastAgglomerativeClusterer extends FastHierarchicalClusterer {

   public FastAgglomerativeClusterer(float threshold) { super(threshold); }

   @Override
   protected FastClusterPair combineCloseClusters(List<FastCluster> clusters, float threshold) {
      short closeA = -1, closeB = -1;
      float maxSim = 0;

      for (short ndx_A = 0; ndx_A < clusters.size(); ndx_A++) {
         FastCluster clustA = clusters.get(ndx_A);

         for (short ndx_B = ndx_A + 1; ndx_B < clusters.size(); ndx_B++) {
            FastCluster clustB = clusters.get(ndx_B);

            float clustSim = clustA.compareTo(clustB);
            if (clustSim > maxSim && clustSim > threshold) {
               closeA = clustA;
               closeB = clustB;
               maxSim = clustSim;
            }
         }
      }

      if (closeA != -1 && closeB != -1) {
         return new FastClusterPair(closeA, closeB, maxSim);
      }

      return null;
   }
}
