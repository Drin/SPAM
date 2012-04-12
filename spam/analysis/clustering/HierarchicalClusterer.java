package spam.analysis.clustering;

import spam.analysis.clustering.Clusterer;

import spam.metrics.DataMetric;
import spam.metrics.ClusterMetric;
import spam.types.Cluster;
import spam.util.Configuration;

import java.util.Set;
import java.util.HashSet;

public abstract class HierarchicalClusterer implements Clusterer {
   protected Set<Cluster> mClusters;
   protected ClusterMetric mClusterMetric;

   public HierarchicalClusterer(Set<Cluster> clusters, ClusterMetric clustDist) {
      mClusters = clusters;
      mClusterMetric = clustDist;
   }

   public Set<Cluster> clusterData() {
      do {
         Cluster[] closestClusters = findCloseClusters();
         combineCloseClusters(closestClusters);
      }
      //TODO: determine stop condition
      while ();
   }

   protected abstract Cluster[] findCloseClusters();
   protected abstract Set<Cluster> combineCloseClusters(Cluster[] closestClusters);
}
