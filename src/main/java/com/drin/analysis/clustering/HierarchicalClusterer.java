package com.drin.analysis.clustering;

import com.drin.analysis.clustering.Clusterer;

import com.drin.clustering.Cluster;
import com.drin.clustering.CloseClusterPair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public abstract class HierarchicalClusterer implements Clusterer {
   private static final Logger mLogger = LogManager.getLogger("HierarchicalClusterer");

   protected Map<String, Map<String, Double>> mDistMap;

   public HierarchicalClusterer(Map<String, Map<String, Double>> distMap) {
      super();

      mDistMap = distMap;
   }

   public HierarchicalClusterer(final int numClusters) {
      this(new HashMap<String, Map<String, Double>>(numClusters));
   }

   public HierarchicalClusterer() {
      this(new HashMap<String, Map<String, Double>>());
   }

   @Override
   public List<Cluster> clusterData(List<Cluster> clusters) {
      List<Cluster> resultClusters = new ArrayList<Cluster>(clusters);

      CloseClusterPair clusterPair = findCloseClusterIndices(resultClusters);

      while (clusterPair != null) {
         mLogger.info(String.format(
            "combining clusters %s (%d) and %s (%d)",
            clusterPair.getLeftClusterName(), clusterPair.getLeftIndex(),
            clusterPair.getRightClusterName(), clusterPair.getRightIndex()
         ));

         combineClusters(resultClusters, clusterPair);
      }

      mLogger.info("No remaining clusters are close");

      return resultClusters;
   }

   protected void clearDistanceMap() { mDistMap.clear(); }

   protected abstract CloseClusterPair findCloseClusterIndices(List<Cluster> clusters);
   protected abstract Cluster combineClusters(List<Cluster> clusters,
                                              CloseClusterPair closeClusterIndices);
}
