package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.HierarchicalClusterer.ClusterPair;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   public AgglomerativeClusterer(int dataSize, float thresh) {
      super(dataSize, thresh);

      mName = "Agglomerative";
   }

   @Override
   protected ClusterPair findCloseClusters(final List<Cluster> clusters,
                                           final float threshold) {
      int closeA = -1, closeB = -1;
      float maxSim = 0;

      for (int ndxA = 0; ndxA < clusters.size(); ndxA++) {
         Cluster clustA = clusters.get(ndxA);

         for (int ndxB = ndxA + 1; ndxB < clusters.size(); ndxB++) {
            Cluster clustB = clusters.get(ndxB);

            float clustSim = clustA.compareTo(clustB);
            if (clustSim > maxSim && clustSim > threshold) {
               closeA = ndxA;
               closeB = ndxB;
               maxSim = clustSim;
            }
         }
      }

      return new ClusterPair(closeA, closeB, maxSim);
   }

   @Override
   protected boolean combineClusters(List<Cluster> clusters, ClusterPair clustPair) {
      boolean success = false;

      if (clustPair.mClustANdx != -1 && clustPair.mClustBNdx!= -1) {
         clusters.get(clustPair.mClustANdx).join(
            clusters.get(clustPair.mClustBNdx)
         );

         clusters.remove(clustPair.mClustBNdx);
         success = true;
      }
      else { Logger.error(-1, "Invalid clusters to combine.\n"); }

      return success;
   }
}
