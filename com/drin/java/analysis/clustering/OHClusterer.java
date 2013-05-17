package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.clustering.Cluster;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class OHClusterer extends AgglomerativeClusterer {
   private static final int ALPHA_THRESH_NDX = 0, BETA_THRESH_NDX = 1;
   protected Ontology mOntology;

   public OHClusterer(Ontology ontology, List<Double> thresholds) {
      super(thresholds);

      mOntology = ontology;
      mName = "OHClust!";
   }

   @Override
   public void clusterData(List<Cluster> clusters) {
      if (mOntology == null) {
         super.clusterData(clusters);
         return;
      }

      incorporateClusters(clusters);

      if (mOntology.getRoot().hasNewData()) {
         ontologicalCluster(mOntology.getRoot(), mThresholds.get(ALPHA_THRESH_NDX));
      }

      List<Cluster> resultClusters = null;
      if (mOntology.getRoot().getClusters() != null) {
         resultClusters = new ArrayList<Cluster>(mOntology.getRoot().getClusters());

         mResultClusters.put(mThresholds.get(ALPHA_THRESH_NDX),
                             new ArrayList<Cluster>(mOntology.getRoot().getClusters()));
      }
      else {
         System.err.println("No clusters formed!?");
         System.exit(1);
      }

      for (int threshNdx = BETA_THRESH_NDX; threshNdx < mThresholds.size(); threshNdx++) {
         super.clusterDataSet(resultClusters, mThresholds.get(threshNdx));

         mResultClusters.put(mThresholds.get(threshNdx), new ArrayList<Cluster>(resultClusters));
      }
   }

   private void ontologicalCluster(OntologyTerm root, double threshold) {
      List<Cluster> clusters = new ArrayList<Cluster>();
      boolean unclusteredData = false;

      if (root == null || !root.hasNewData()) { return; }
      else if (!root.getPartitions().isEmpty()) {

         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            if (partition.getValue() == null) { continue; }

            if (partition.getValue().hasNewData()) {
               ontologicalCluster(partition.getValue(), threshold);
               unclusteredData = true;
            }

            if (partition.getValue().getClusters() == null) { continue; }

            clusters.addAll(partition.getValue().getClusters());
            if (unclusteredData && root.isTimeSensitive()) {
               clusterDataSet(clusters, threshold);
               root.setClusters(clusters);
            }
         }
      
         if (unclusteredData && !root.isTimeSensitive()) {
            clusterDataSet(clusters, threshold);
            root.setClusters(clusters);
         }
      }

      else if (root.getData() != null) {
         clusters.addAll(root.getData());
         clusterDataSet(clusters, threshold);
         root.setClusters(clusters);
      }
   }

   private void incorporateClusters(List<Cluster> clusters) {
      Map<String, Double> clustSimMap = null;
      Cluster clust_A = null, clust_B = null;

      //determine clusters that are close
      for (int clustNdx_A = 0; clustNdx_A < clusters.size(); clustNdx_A++) {
         clustSimMap = new HashMap<String, Double>(clusters.size() - clustNdx_A);
         clust_A = clusters.get(clustNdx_A);

         for (int clustNdx_B = clustNdx_A + 1; clustNdx_B < clusters.size(); clustNdx_B++) {
            clust_B = clusters.get(clustNdx_B);

            double clustComparison = clust_A.compareTo(clust_B);
            clustSimMap.put(clust_B.getName(), new Double(clustComparison));
         }

         mSimMap.put(clust_A.getName(), clustSimMap);
         mOntology.addData(clust_A);
      }
   }
}
