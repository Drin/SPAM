package com.drin.analysis.clustering;

import com.drin.analysis.clustering.AgglomerativeClusterer;

import com.drin.ontology.Ontology;
import com.drin.ontology.OntologyTerm;

import com.drin.clustering.Cluster;

import com.drin.util.Logger;

import java.util.List;
import java.util.ArrayList;

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
   public void clusterData() {
      if (mOntology == null) { super.clusterData(); }

      else {
         mResultClusters = new ArrayList<Cluster>();
         List<Cluster> resultClusters = ontologicalCluster(
            mOntology.getRoot(), mThresholds.get(ALPHA_THRESH_NDX)
         );

         mResultClusters.put(
            mThresholds.get(ALPHA_THRESH_NDX),
            new ArrayList<Cluster>(resultClusters)
         );

         for (int threshNdx = BETA_THRESH_NDX; threshNdx < mThresholds.size(); threshNdx++) {
            super.clusterDataSet(resultClusters, mThresholds.get(threshNdx));

            mResultClusters.put(mThresholds.get(threshNdx), new ArrayList<Cluster>(resultClusters));
         }
      }
   }

   private List<Cluster> ontologicalCluster(OntologyTerm root, double threshold) {
      List<Cluster> clusters = new ArrayList<Cluster>();

      if (root == null) { return clusters; }

      else if (root.getPartitions() != null) {
         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            clusters.addAll(ontologicalCluster(partition.getValue(), threshold));
      
            if (root.isTimeSensitive()) {
               Logger.debug("Clustering time sensitive clusters...");

               clusterDataSet(clusters, threshold);
               root.setClusters(clusters);

               printOntology();
            }
         }
      
         if (!root.isTimeSensitive()) {
            Logger.debug("Clustering non time sensitive clusters...");

            clusterDataSet(clusters, threshold);
            root.setClusters(clusters);

            printOntology();

            return clusters;
         }
      }

      else if (root.getData() != null) {
         Logger.debug("percolating leaf cluster sets");

         clusters.addAll(root.getData());
         clusterDataSet(clusters, threshold);
         root.setClusters(clusters);

         printOntology();
      }

      return clusters;
   }
}
