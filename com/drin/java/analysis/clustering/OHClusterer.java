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

   public OHClusterer(Ontology ontology, List<Double> thresholds) {
      super(thresholds);

      mOntology = ontology;
      mBoundaryClusters = new ArrayList<Cluster>();
   }

   private void printOntology() {
      System.out.printf("cluster tree:\n%s\n", mOntology.printClusters());
   }

   public List<Cluster> getCoreClusters() {
      return mOntology.getRoot().getClusters();
   }

   public List<Cluster> getBoundaryClusters() {
      return mBoundaryClusters;
   }
   
   @Override
   public void clusterData(List<Cluster> clusters) {
      mResultClusters.clear();
      if (mOntology == null) {
         super.clusterData(clusters);
         return;
      }

      separateCoreClusters(clusters, mThresholds.get(ALPHA_THRESH_NDX));
      System.out.println("ontology: " + mOntology);
      System.exit(0);

      ontologicalCluster(mOntology.getRoot(), mThresholds.get(ALPHA_THRESH_NDX));

      List<Cluster> resultClusters = mOntology.getRoot().getClusters();
      mResultClusters.put(mThresholds.get(ALPHA_THRESH_NDX),
                          new ArrayList<Cluster>(resultClusters));

      printOntology();

      for (int threshNdx = BETA_THRESH_NDX; threshNdx < mThresholds.size(); threshNdx++) {
         super.clusterDataSet(resultClusters, mThresholds.get(threshNdx));

         mResultClusters.put(mThresholds.get(threshNdx), new ArrayList<Cluster>(resultClusters));
      }
   }

   private void ontologicalCluster(OntologyTerm root, double threshold) {
      List<Cluster> clusters = new ArrayList<Cluster>();
      boolean unclusteredData = false;

      if (root != null && root.hasNewData() && !root.getPartitions().isEmpty()) {
         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            if (partition.getValue() == null) { continue; }

            if (partition.getValue().hasNewData()) {
               ontologicalCluster(partition.getValue(), threshold);
               unclusteredData = true;
            }

            clusters.addAll(partition.getValue().getClusters());

            if (unclusteredData && root.isTimeSensitive()) {
               Logger.debug("Clustering time sensitive clusters...");

               clusterDataSet(clusters, threshold);
               root.setClusters(clusters);
            }
         }
      
         if (unclusteredData && !root.isTimeSensitive()) {
            Logger.debug("Clustering non time sensitive clusters...");

            clusterDataSet(clusters, threshold);
            root.setClusters(clusters);
         }
      }

      else if (root != null && root.getData() != null && root.hasNewData()) {
         Logger.debug("percolating leaf cluster sets");

         clusters.addAll(root.getData());
         clusterDataSet(clusters, threshold);
         root.setClusters(clusters);
      }
   }

   //modifies mSimMap and mBoundaryClusters
   private void separateCoreClusters(List<Cluster> clusters, double threshold) {
      Map<String, Cluster> coreClusters = new HashMap<String, Cluster>();
      Map<String, Double> clustSimMap = new HashMap<String, Double>();
      Cluster clust_A = null, clust_B = null;

      //determine clusters that are close
      for (int clustNdx_A = 0; clustNdx_A < clusters.size(); clustNdx_A++) {
         clust_A = clusters.get(clustNdx_A);

         for (int clustNdx_B = clustNdx_A + 1; clustNdx_B < clusters.size(); clustNdx_B++) {
            clust_B = clusters.get(clustNdx_B);

            double clustComparison = clust_A.compareTo(clust_B);
            clustSimMap.put(clust_B.getName(), new Double(clustComparison));

            if (clustComparison >= threshold) {
               if (!coreClusters.containsKey(clust_A.getName())) {
                  coreClusters.put(clust_A.getName(), clust_A);
               }
               if (!coreClusters.containsKey(clust_B.getName())) {
                  coreClusters.put(clust_B.getName(), clust_B);
               }
            }
         }

         mSimMap.put(clust_A.getName(), clustSimMap);
         clustSimMap.clear();
      }

      //clusters that are very similar to another go into ontology tree
      //clusters that were not similar to anything go into boundary clusters
      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         clust_A = clusters.get(clustNdx);

         if (coreClusters.containsKey(clust_A.getName())) {
            mOntology.addData(coreClusters.get(clust_A.getName()));
            System.out.printf("adding cluster %s to ontology\n", clust_A.getName());
         }
         else {
            mBoundaryClusters.add(clust_A);
         }
      }
   }
}
