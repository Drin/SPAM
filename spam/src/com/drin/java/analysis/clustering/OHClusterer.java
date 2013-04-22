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

public class OHClusterer extends AgglomerativeClusterer {
   private static final int ALPHA_THRESH_NDX = 0, BETA_THRESH_NDX = 1;
   protected List<Cluster> mBoundaryClusters;
   protected Ontology mOntology;

   public OHClusterer(Ontology ontology, List<Double> thresholds) {
      super(thresholds);

      mOntology = ontology;
      mBoundaryClusters = new ArrayList<Cluster>();
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

      ontologicalCluster(mOntology.getRoot(), mThresholds.get(ALPHA_THRESH_NDX));

      if (mOntology.getRoot().getClusters() == null) {
         System.out.printf("failed clustering. Got null\n");
         System.exit(1);
      }

      List<Cluster> resultClusters = mOntology.getRoot().getClusters();
      resultClusters.addAll(mBoundaryClusters);

      super.clusterDataSet(resultClusters, mThresholds.get(ALPHA_THRESH_NDX));
      mResultClusters.put(mThresholds.get(ALPHA_THRESH_NDX),
                          new ArrayList<Cluster>(resultClusters));

      for (int threshNdx = BETA_THRESH_NDX; threshNdx < mThresholds.size(); threshNdx++) {
         super.clusterDataSet(resultClusters, mThresholds.get(threshNdx));

         mResultClusters.put(mThresholds.get(threshNdx), new ArrayList<Cluster>(resultClusters));
      }
   }

   private void ontologicalCluster(OntologyTerm root, double threshold) {
      List<Cluster> clusters = new ArrayList<Cluster>();
      boolean unclusteredData = false;

      /*
      System.out.printf(
         "clustering node [%s.%s]...\n" +
         "checking conditions:\n" +
         "\tis root null: %s\n" +
         "\tdoes root have new data: %s\n" +
         "\tdoes root have sub-partitions: %s\n",
         root.getTableName(), root.getColName(),
         (root == null), root.hasNewData(),
         !root.getPartitions().isEmpty()
      );
      */

      if (root == null || !root.hasNewData()) {
         return;
      }

      else if (!root.getPartitions().isEmpty()) {
         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            System.out.printf("clustering partition [%s]\n", partition.getKey());

            if (partition.getValue() == null) {
               System.out.printf("\tnull node\n");
               continue;
            }

            if (partition.getValue().hasNewData()) {
               System.out.printf("\tnew data in partition\n");
               ontologicalCluster(partition.getValue(), threshold);
               unclusteredData = true;
            }

            if (partition.getValue().getClusters() == null) {
               System.out.printf("\tpartition has no clusters\n");
               continue;
            }

            clusters.addAll(partition.getValue().getClusters());

            if (unclusteredData && root.isTimeSensitive()) {
               Logger.debug("Clustering time sensitive clusters...");

               clusterDataSet(clusters, threshold);
               if (clusters == null) {
                  System.out.printf("spot A is null\n");
               }
               root.setClusters(clusters);
            }
         }
      
         if (unclusteredData && !root.isTimeSensitive()) {
            Logger.debug("Clustering non time sensitive clusters...");

            clusterDataSet(clusters, threshold);
            if (clusters == null) {
               System.out.printf("spot B is null\n");
            }
            root.setClusters(clusters);
         }
      }

      else if (root.getData() != null) {
         Logger.debug("percolating leaf cluster sets");
         //System.out.printf("This is a leaf node\n");

         clusters.addAll(root.getData());
         clusterDataSet(clusters, threshold);
         if (clusters == null) {
            System.out.printf("spot C is null\n");
         }
         root.setClusters(clusters);
      }

      else {
         System.out.printf("Node has no data and no edges\n");
      }

      /*
      System.out.printf("finished clustering node [%s.%s]\n",
                        root.getTableName(), root.getColName());
                        */
   }

   //modifies mSimMap and mBoundaryClusters
   private void separateCoreClusters(List<Cluster> clusters, double threshold) {
      Map<String, Cluster> coreClusters = new HashMap<String, Cluster>();
      Map<String, Double> clustSimMap = null;
      Cluster clust_A = null, clust_B = null;

      //determine clusters that are close
      for (int clustNdx_A = 0; clustNdx_A < clusters.size(); clustNdx_A++) {
         clustSimMap = new HashMap<String, Double>();
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
      }

      //clusters that are very similar to another go into ontology tree
      //clusters that were not similar to anything go into boundary clusters
      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         clust_A = clusters.get(clustNdx);

         if (coreClusters.containsKey(clust_A.getName())) {
            mOntology.addData(coreClusters.get(clust_A.getName()));
            //System.out.printf("added new data\n");
         }
         else {
            mBoundaryClusters.add(clust_A);
         }
      }
   }
}
