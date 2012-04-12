package spam.analysis.clustering;

import spam.analysis.clustering.Clusterer;

import spam.metrics.DataMetric;
import spam.metrics.ClusterMetric;
import spam.types.Cluster;
import spam.util.Configuration;

import java.util.Set;
import java.util.HashSet;

/*
 * TODO: need to create some classes:
 *    ComparisonValue
 *    DataMetric
 *    ClusterMetric
 *    Cluster
 *    Configuration
 */

public abstract class AgglomerativeClusterer implements Clusterer {
   private static final int CLUSTER_PAIR_SIZE;

   public AgglomerativeClusterer(Set<Cluster> clusters, ClusterMetric clustDist) {
      super(clusters, clustDist);
   }

   protected Cluster[] findCloseClusters() {
      Cluster[] closestClusters = new Cluster[CLUSTER_PAIR_SIZE];
      ComparisonValue bestValue = ComparisonValue.NONE;

      for (Cluster cluster_A : mClusters) {

         for (Cluster cluster_B : mClusters) {
            ComparisonValue value = mClusterMetric.compare(cluster_A, cluster_B);

            if (value.compareTo(bestValue) < 0) {
               bestValue = value;
               closestClusters[0] = cluster_A;
               closestClusters[1] = cluster_B;
            }
         }
      }

      return closestClusters;
   }

   protected Set<Cluster> combineCloseClusters(Cluster[] closestClusters) {
      Set<Cluster> newClusterSet = new HashSet<Cluster>();

      if (closestClusters.length != CLUSTER_PAIR_SIZE) {
         System.err.printf("Invalid cluster pair to be combined\n");
         System.exit(1);
      }

      for (Cluster cluster_A : mClusters) {
         if (cluster_A.equals(closestClusters[0])) {
            Cluster cluster_B = closestClusters[1];
            newClusterSet.add(Cluster.combineClusters(cluster_A, cluster_B));
         }

         else if (cluster_A.equals(closestClusters[1])) { continue; }
         else { newClusterSet.add(cluster_A); }
      }

      return newClusterSet;
   }
}
