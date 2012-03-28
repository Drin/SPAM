package spam.analysis.clustering;

import spam.util.Configuration;

import spam.types.Cluster;

import java.util.Set;
import java.util.HashSet;

public abstract class HierarchicalClustering {
   private Set<Cluster> mClusters;

   public HierarchicalCluster() {
      mClusters = new HashSet<Cluster>();
   }

   public HierarchicalClustering(Set<Cluster> clusters) {
      this();

      mClusters = clusters;
   }

   public HierarchicalClustering(Set<Object> dataSet) {
      this();

      Set<Cluster> clusterSet = new HashSet<Cluster>();

      for (Object dataObject : dataSet) {
         clusterSet.add(new Cluster(dataObject));
      }

      mClusters = clusterSet;
   }

   //TODO determine a good return type
   //also determine parameter type
   //    perhaps a map? list? data partitions should not be visible in this
   //    method
   public void clusterData() {
      Set<Cluster> newClusterSet = new HashSet<Cluster>();
      
      //TODO think about how and where region thresholds are going to be
      //accessed for chronology sensitive, and how to make it not class with
      //any "standard" or other hierarchical method thresholds

      for (Cluster cluster_A : mClusters) {
         for (Cluster cluster_B : mClusters) {
            if (!cluster_A.equals(cluster_B) && cluster_A.isSimilar(cluster_B,
         }
      }
   }
}
