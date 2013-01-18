package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.clustering.Cluster;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JTextArea;

public class OHClusterer extends AgglomerativeClusterer {
   protected Ontology mOntology;

   //TODO the 2nd param to super (single threshold for hierarchical clustering)
   //should not be 0...
   public OHClusterer(Set<Cluster> clusters, Ontology ontology) {
      super(clusters);

      mOntology = ontology;
   }

   private void printOntology() {
      System.out.printf("cluster tree:\n%s\n", mOntology.printClusters());
   }
   
   //TODO Canvas is a hack in order to indicate progress
   @Override
   public void clusterData(JTextArea canvas) {
      if (mOntology == null) { super.clusterData(canvas); }
      else {
         for (Cluster cluster : ontologicalCluster(mOntology.getRoot(), canvas)) {
            mResultClusters.add(cluster);
         }
      }
   }

   private Set<Cluster> ontologicalCluster(OntologyTerm root, JTextArea canvas) {
      if (root == null) { return new HashSet<Cluster>(); }

      Set<Cluster> clusters = new HashSet<Cluster>();

      if (root.getPartitions() != null) {
         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            clusters.addAll(ontologicalCluster(partition.getValue(), canvas));
      
            if (root.isTimeSensitive()) {
               Logger.debug("Clustering time sensitive clusters...");

               clusters = clusterDataSet(clusters, canvas);
               root.setClusters(clusters);

               printOntology();
            }
         }
      
         if (!root.isTimeSensitive()) {
            Logger.debug("Clustering non time sensitive clusters...");

            clusters = clusterDataSet(clusters, canvas);
            root.setClusters(clusters);

            printOntology();

            return clusters;
         }
      }

      else if (root.getData() != null) {
         Logger.debug("percolating leaf cluster sets");

         clusters.addAll(root.getData());
         clusters = clusterDataSet(clusters, canvas);
         root.setClusters(clusters);

         printOntology();
      }

      return clusters;
   }


   @Override
   protected Cluster[] findCloseClusters(Map<String, Map<String, Double>> distMap,
                                         Set<Cluster> clusters) {
      double maxSim = 0;
      Cluster closeClust_A = null, closeClust_B = null;

      Logger.debug("finding close clusters...");

      for (Cluster clust_A : clusters) {
         for (Cluster clust_B : clusters) {
            if (clust_A.getName().equals(clust_B.getName())) { continue; }

            double dist = clust_A.compareTo(clust_B);

            if (dist > maxSim && clust_A.isSimilar(clust_B)) {
               closeClust_A = clust_A;
               closeClust_B = clust_B;

               maxSim = dist;
            }
         }
      }

      if (closeClust_A != null && closeClust_B != null) {
         Cluster[] closeClusters = new Cluster[] {closeClust_A, closeClust_B};
         return closeClusters;
      }

      return null;
   }

   @Override
   protected Set<Cluster> combineClusters(Cluster[] closeClusters,
                                          Set<Cluster> clusters) {
      Set<Cluster> newClusters = super.combineClusters(closeClusters, clusters);

      if (closeClusters.length != CLUSTER_PAIR_SIZE) {
         Logger.error(-1, "No cluster distances were recomputed");
      }

      /*
       * TODO: figure out recomputing distances...
      for (Cluster cluster : newClusters) {
         if (cluster.isSimilar(closeClusters[0])) { continue; }

         Logger.debug(String.format("recomputing clusters '%s' and '%s'\n",
                                    cluster.getName(), closeClusters[0].getName()));
   
         mClusterMetric.recompute(closeClusters[0], cluster);
      }
      */

      return newClusters;
   }
}
