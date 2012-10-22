package com.drin.java.analysis.clustering;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.HCluster;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import com.drin.java.metrics.ClusterMetric;
import com.drin.java.metrics.ClusterComparator;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class OHClustering<E extends BaseClusterable> extends
             AgglomerativeClusterer<E> {
   protected Ontology mOntology;
   private double mAlpha, mBeta;

   public OHClustering(Set<HCluster<E>> clusters,
                       double alpha, double beta,
                       Ontology ontology,
                       ClusterMetric<E> metric,
                       ClusterComparator<E> comp) {
      super(clusters, metric, comp);

      mAlpha = alpha;
      mBeta = beta;
      mOntology = ontology;
   }

   private void printOntology() {
      System.out.printf("cluster tree:\n%s\n", mOntology.printClusters());
   }

   @Override
   public void clusterData() {
      if (mOntology == null) { super.clusterData(); }
      else {
         for (HCluster cluster : ontologicalCluster(mOntology.getRoot())) {
            mResultClusters.add(cluster);
         }
      }
   }

   private Set<HCluster<E>> ontologicalCluster(OntologyTerm root) {
      if (root == null) { return new HashSet<HCluster<E>>(); }

      Set<HCluster> clusters = new HashSet<HCluster>();

      if (root.getPartitions() != null) {
         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            clusters.addAll(ontologicalCluster(partition.getValue()));
      
            if (root.isTimeSensitive()) {
               Logger.debug("Clustering time sensitive clusters...");

               clusters = clusterDataSet(clusters);
               root.setClusters(clusters);

               printOntology();
            }
         }
      
         if (!root.isTimeSensitive()) {
            Logger.debug("Clustering non time sensitive clusters...");

            clusters = clusterDataSet(clusters);
            root.setClusters(clusters);

            printOntology();

            return clusters;
         }
      }

      else if (root.getData() != null) {
         Logger.debug("percolating leaf cluster sets");

         clusters.addAll(root.getData());
         clusters = clusterDataSet(clusters);
         root.setClusters(clusters);

         printOntology();
      }

      return clusters;
   }


   @Override
   protected Cluster[] findCloseClusters(Set<HCluster<E>> clusters) {
      double minDist = Double.MAX_VALUE, maxSim = 0;
      HCluster closeClust_A = null, closeClust_B = null;

      for (HCluster clust_A : clusters) {
         for (HCluster clust_B : clusters) {
            if (clust_A.getName().equals(clust_B.getName())) { continue; }

            double dist = mComp.compare(mMetric, clust_A, clust_B);

            if (System.getenv().containsKey("DEBUG")) {
               System.out.printf("'%s' -> '%s': %.04f (null: %s)\n",
                clust_A.getName(), clust_B.getName(), clustDist.doubleValue(),
                clustDist == null);
            }

            if (dist > maxSim && dist > mBetaThreshold) {
               closeClust_A = clust_A;
               closeClust_B = clust_B;

               maxSim = dist;
            }

            if (dist < minDist) {
               closeClust_A = clust_A;
               closeClust_B = clust_B;

               minDist = dist;
            }

         }
      }

      if (System.getenv().containsKey("DEBUG")) {
         if (closeClust_A == null || closeClust_B == null) {
            System.out.println("Closest clusters are null!\nclusterset: ");
      
            for (Cluster clust_A : clusterSet) {
               System.out.printf("%s, ", clust_A.getName());
            }
      
            System.out.printf("\n");
         }
      }

      if (closeClust_A != null && closeClust_B != null) {
         @SuppressWarnings(value={"unchecked", "rawtypes"})
         HCluster<E>[] closeClusters = new HCluster[] {closeClust_A, closeClust_B};
         return closeClusters;
      }

      return null;
   }

   @Override
   protected Set<HCluster<E>> combineClusters(HCluster<E>[] closeClusters,
                                                   Set<HCluster<E>> clusters) {
      Set<HCluster<E>> newClusters = super.combineClusters(closeClusters, clusters);

      if (closeClusters.length != CLUSTER_PAIR_SIZE) {
         Logger.error(-1, "No clusters were combined");
      }

      for (HCluster<E> cluster : newClusters) {
         if (cluster.getName().equals(closeCluster[0].getName())) {
            continue;
         }

         Logger.debug(String.format("recomputing clusters '%s' and '%s'\n",
                                    currClusterName, clusterName));
   
         //TODO
         //mClusterMetric.recompute(closeClusters[0], cluster);
      }

      return newClusters;
   }
}
