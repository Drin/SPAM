package com.drin.java.analysis.clustering;

import com.drin.java.types.DataObject;
import com.drin.java.types.Cluster;
import com.drin.java.types.FeatureTree;
import com.drin.java.types.FeatureNode;
import com.drin.java.metrics.ClusterMatrixMetric;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class PartitionedAgglomerative extends AgglomerativeClusterer {
   private static final boolean METRIC_IS_DISTANCE = false,
                                DEBUG = false;
   protected ClusterMatrixMetric mClusterMetric;
   protected FeatureTree mDataStructure;

   public PartitionedAgglomerative(Set<Cluster> clusters, ClusterMatrixMetric clustMetric) {
      super(clusters);
      mClusterMetric = clustMetric;

      mDataStructure = null;
   }

   public PartitionedAgglomerative(FeatureTree dataStructure, ClusterMatrixMetric clustMetric) {
      super(null);

      mDataStructure = dataStructure;
      mClusterMetric = clustMetric;
   }

   private void printOntology() {
      System.out.printf("cluster tree:\n%s\n", mDataStructure.printClusters());
   }

   @Override
   public void clusterData() {
      if (mDataStructure == null) { super.clusterData(); }
      else {
         for (Cluster cluster : clusterDataStructure(mDataStructure.getRoot())) {
            mResultClusters.add(cluster);
         }
      }
   }

   private Set<Cluster> clusterDataStructure(FeatureNode root) {
      if (root == null) { return new HashSet<Cluster>(); }

      Set<Cluster> clusterSet = new HashSet<Cluster>();

      if (root.getPartitions() != null) {
         for (Map.Entry<String, FeatureNode> partition : root.getPartitions().entrySet()) {
            clusterSet.addAll(clusterDataStructure(partition.getValue()));
      
            if (root.isTimeSensitive()) {
               if (DEBUG) { System.out.println("Clustering time sensitive clusters..."); }
               clusterSet = clusterDataSet(clusterSet);
               root.setClusters(clusterSet);
               printOntology();
            }
         }
      
         if (!root.isTimeSensitive()) {
            if (DEBUG) { System.out.println("Clustering non time sensitive clusters..."); }
            clusterSet = clusterDataSet(clusterSet);
            root.setClusters(clusterSet);
            printOntology();
            return clusterSet;
         }
      }

      else if (root.getData() != null) {
         if (DEBUG) { System.out.println("percolating leaf cluster sets"); }
         clusterSet.addAll(root.getData());
         clusterSet = clusterDataSet(clusterSet);
         root.setClusters(clusterSet);
         printOntology();
      }

      return clusterSet;
   }


   @Override
   protected Cluster[] findCloseClusters(Set<Cluster> clusterSet) {
      Cluster closeClust_A = null, closeClust_B = null;
      double minDist = Double.MAX_VALUE, maxSim = 0;

      for (Cluster clust_A : clusterSet) {
         for (Cluster clust_B : clusterSet) {

            if (!clust_A.equals(clust_B)) {
               mClusterMetric.apply(clust_A, clust_B);
               Double clustDist = mClusterMetric.result();

               if (DEBUG) {
                  System.out.printf("'%s' -> '%s': %.04f (null: %s)\n",
                   clust_A.getName(), clust_B.getName(), clustDist.doubleValue(),
                   clustDist == null);
               }

               if (!METRIC_IS_DISTANCE) {
                  if (clustDist != null && clustDist.doubleValue() > maxSim &&
                   clustDist.doubleValue() > mBetaThreshold) {
                     closeClust_A = clust_A;
                     closeClust_B = clust_B;
                     maxSim = clustDist.doubleValue();
                  }
               }

               else {
                  if (clustDist != null && clustDist.doubleValue() < minDist) {
                     closeClust_A = clust_A;
                     closeClust_B = clust_B;
                     minDist = clustDist.doubleValue();
                  }
               }
            }

         }
      }

      if (DEBUG) {
         if (closeClust_A == null || closeClust_B == null) {
            System.out.println("Closest clusters are null!\nclusterset: ");
      
            for (Cluster clust_A : clusterSet) {
               System.out.printf("%s, ", clust_A.getName());
            }
      
            System.out.printf("\n");
         }
      }

      if (closeClust_A != null && closeClust_B != null) {
         return new Cluster[] {closeClust_A, closeClust_B};
      }

      return null;
   }

   @Override
   protected Set<Cluster> combineCloseClusters(Cluster[] closestClusters,
    Set<Cluster> clusterSet) {
      //Combining clusters should be the same
      Set<Cluster> newClusterSet = super.combineCloseClusters(closestClusters, clusterSet);

      if (closestClusters.length > 0) {
         for (Cluster cluster : newClusterSet) {
            String currClusterName = closestClusters[0].getName();
            String clusterName = cluster.getName();

            if (currClusterName != null && clusterName != null &&
             currClusterName.equals(clusterName)) { continue; }

            if (DEBUG) {
               System.out.printf("Combining clusters '%s' and '%s'\n", currClusterName, clusterName);
               System.out.printf("recomputing cluster distances\n");
            }
      
            mClusterMetric.recompute(closestClusters[0], cluster);
         }
      }

      return newClusterSet;
   }
}
