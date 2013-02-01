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
   protected List<Cluster> mBoundaryClusters;
   protected Ontology mOntology;
   protected double mAlphaThreshold;

   public OHClusterer(List<Cluster> coreClusters, List<Cluster> boundaryClusters,
                      Ontology ontology, double alpha, double beta) {
      super(coreClusters, beta);
      mBoundaryClusters = boundaryClusters;
      mAlphaThreshold = alpha;

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
         mResultClusters = new ArrayList<Cluster>(mBoundaryClusters);
         mResultClusters.addAll(ontologicalCluster(mOntology.getRoot(), mAlphaThreshold, canvas));
         super.clusterDataSet(mResultClusters, mBetaThreshold, canvas);
      }
   }

   private List<Cluster> ontologicalCluster(OntologyTerm root, double threshold, JTextArea canvas) {
      List<Cluster> clusters = new ArrayList<Cluster>();

      if (root == null) { return clusters; }

      else if (root.getPartitions() != null) {
         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            clusters.addAll(ontologicalCluster(partition.getValue(), threshold, canvas));
      
            if (root.isTimeSensitive()) {
               Logger.debug("Clustering time sensitive clusters...");

               clusterDataSet(clusters, threshold, canvas);
               root.setClusters(clusters);

               printOntology();
            }
         }
      
         if (!root.isTimeSensitive()) {
            Logger.debug("Clustering non time sensitive clusters...");

            clusterDataSet(clusters, threshold, canvas);
            root.setClusters(clusters);

            printOntology();

            return clusters;
         }
      }

      else if (root.getData() != null) {
         Logger.debug("percolating leaf cluster sets");

         clusters.addAll(root.getData());
         clusterDataSet(clusters, threshold, canvas);
         root.setClusters(clusters);

         printOntology();
      }

      return clusters;
   }
}
