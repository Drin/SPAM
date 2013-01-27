package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   protected static final int CLUSTER_PAIR_SIZE = 2;

   public AgglomerativeClusterer(List<Cluster> clusters) {
      super(clusters);
   }

   /*
    * distMap persists across calls to findCloseClusters() as a cache for the 
    * cluster distance matrix. clustDistMap is used so that new references are
    * not created for each outer mapping.
    */
   @Override
   protected Cluster[] findCloseClusters(Map<String, Map<String, Double>> distMap, List<Cluster> clusters) {
      Map<String, Double> clustDistMap = null;
      Cluster close_A = null, close_B = null;
      double maxSim = 0;
      boolean isClose = false;

      for (int ndx_A = 0; ndx_A < clusters.size(); ndx_A++) {
         Cluster clust_A = clusters.get(ndx_A);

         //clustDistMap is a reference to a Map in distMap. Any changes to
         //clustDistMap hereafter are persisted to distMap.
         if (distMap != null && distMap.containsKey(clust_A.getName())) {
            clustDistMap = distMap.get(clust_A.getName());
         }
         else if (distMap != null) {
            clustDistMap = new HashMap<String, Double>();
            distMap.put(clust_A.getName(), clustDistMap);
         }

         for (int ndx_B = ndx_A + 1; ndx_B < clusters.size(); ndx_B++) {
            Cluster clust_B = clusters.get(ndx_B);

            double clustDist = -2;

            //Here we need to determine the cluster distance, clustDist. If no
            //previous distance was computed, then store clustDist to
            //clustDistMap, which will persist in distMap
            if (clustDistMap != null && clustDistMap.containsKey(clust_B.getName())) {
               clustDist = clustDistMap.get(clust_B.getName());
            }
            else if (clustDistMap != null) {
               clustDist = clust_A.compareTo(clust_B);
               clustDistMap.put(clust_B.getName(), clustDist);
            }

            //This switch is to minimize code duplication for hierarchical and
            //OHClustering
            isClose = clustDist > maxSim;

            switch (mSimType) {
               case SIMILAR:
                  isClose = isClose && clust_A.isSimilar(clust_B);
                  break;
               case SQUISHY:
               default:
                  isClose = isClose && !clust_A.isDifferent(clust_B);
                  break;
            }

            if (isClose) {
               close_A = clust_A;
               close_B = clust_B;

               maxSim = clustDist;
               isClose = false;
            }
         }

         clustDistMap = null;
      }

      if (close_A != null && close_B != null) {
         Cluster[] closeClusters = new Cluster[] {close_A, close_B};
         return closeClusters;
      }

      return null;
   }

   @Override
   protected void combineClusters(Cluster[] closeClusters, List<Cluster> clusters) {
      if (closeClusters.length != CLUSTER_PAIR_SIZE) {
         Logger.error(-1, "Invalid cluster candidates\n");
      }

      Logger.debug(String.format("combining clusters '%s' and '%s'",
                                 closeClusters[0], closeClusters[1]));

      int removeNdx = -1;

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         Cluster tmpClust = clusters.get(clustNdx);

         if (tmpClust.getName().equals(closeClusters[0].getName())) {
            clusters.set(clustNdx, tmpClust.join(closeClusters[1]));
         }

         else if (tmpClust.getName().equals(closeClusters[1].getName())) {
            removeNdx = clustNdx;
         }
      }

      if (removeNdx != -1) {
         clusters.remove(removeNdx);
      }
      else { Logger.debug("Remove Index is -1. Error during clustering"); }
   }
}
