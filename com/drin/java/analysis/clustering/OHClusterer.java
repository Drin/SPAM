package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.clustering.Cluster;

import com.drin.java.util.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JTextArea;

public class OHClusterer extends AgglomerativeClusterer {
   protected Ontology mOntology;

   public OHClusterer(List<Cluster> clusters, Ontology ontology) {
      super(clusters);

      //mSimType affects how close clusters are determined. See findCloseCluster()
      //in com.drin.java.analysis.clustering.AgglomerativeClusterer
      mOntology = ontology;

      if (ontology != null) { mSimType = CLUST_SIMILARITY.SIMILAR; }
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

   //TODO be sure that the clusters that are not above the upper threshold get
   //added at the end or the result clusters are in a state ready to be
   //clustered again using hierarchical
   private List<Cluster> ontologicalCluster(OntologyTerm root, JTextArea canvas) {
      List<Cluster> clusters = new ArrayList<Cluster>();

      if (root == null) { return clusters; }

      else if (root.getPartitions() != null) {
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
}
