package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.HierarchicalClusterer.ClusterPair;

import com.drin.java.output.ProgressWriter;

import java.util.List;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   public AgglomerativeClusterer(int dataSize, float thresh,
                                 ProgressWriter writer) {
      super(dataSize, thresh, writer);
      mName = "Agglomerative";
   }

   @Override
   protected ClusterPair findCloseClusters(List<Cluster> clusters,
                                           float threshold) {
      int closeA = -1, closeB = -1;
      float maxSim = 0.0f;

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
      //else { System.err.println("Invalid clusters to combine."); }

      return success;
   }
}
