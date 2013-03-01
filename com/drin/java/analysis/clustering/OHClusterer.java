package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.clustering.Cluster;

import com.drin.java.util.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JTextArea;

public class OHClusterer extends AgglomerativeClusterer {
   private static final int ALPHA_THRESH_NDX = 0, BETA_THRESH_NDX = 1;
   protected List<Cluster> mBoundaryClusters;
   protected Ontology mOntology;

   public OHClusterer(List<Cluster> coreClusters, List<Cluster> boundaryClusters,
                      Ontology ontology, List<Double> thresholds) {
      super(coreClusters, thresholds);
      mBoundaryClusters = boundaryClusters;
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
         mResultClusters = new HashMap<Double, List<Cluster>>();
         List<Cluster> resultClusters = ontologicalCluster(mOntology.getRoot(),
                                                           mThresholds.get(ALPHA_THRESH_NDX),
                                                           canvas);

         mResultClusters.put(mThresholds.get(ALPHA_THRESH_NDX),
                             new ArrayList<Cluster>(resultClusters));

         for (int threshNdx = BETA_THRESH_NDX; threshNdx < mThresholds.size(); threshNdx++) {
            super.clusterDataSet(resultClusters, mThresholds.get(threshNdx), canvas);

            mResultClusters.put(mThresholds.get(threshNdx), new ArrayList<Cluster>(resultClusters));
         }
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
