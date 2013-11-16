package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;

import com.drin.java.output.ProgressWriter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class OHClusterer extends AgglomerativeClusterer {
   private float mAlphaThresh;

   public OHClusterer(int dataSize, float alphaThresh, float betaThresh,
                      ProgressWriter writer) {
      super(dataSize, betaThresh, writer);
      mName = "OHClust!";
      mAlphaThresh = alphaThresh;
   }

   @Override
   public void clusterData(Ontology clustOnt) {
      mResultClusters.clear();

      if (clustOnt == null || clustOnt.getRoot() == null ||
         !clustOnt.getRoot().hasNewData()) {
         return;
      }

      System.err.println("Beginning ontological cluster");
      ontologicalCluster(clustOnt.getRoot(), mAlphaThresh);

      if (clustOnt.getRoot().getClusters() == null) {
         System.err.printf("No clusters formed. Possible Error.\n");
         return;
      }
      
      List<Cluster> clusters = copyClusters(clustOnt.getRoot().getClusters());
      mResultClusters.put(mAlphaThresh, clusters);
      System.err.println("finished clustering using alpha threshold: " + mAlphaThresh);
      
      //mThresh is the beta threshold
      clusters = copyClusters(clusters);
      super.clusterDataSet(clusters, mThresh);
      mResultClusters.put(mThresh, clusters);
      System.err.println("finished clustering using beta threshold: " + mThresh);
   }

   private void ontologicalCluster(OntologyTerm root, float threshold) {
      boolean unclusteredData = false;
      List<Cluster> clusters = new ArrayList<Cluster>();

      if (!root.getPartitions().isEmpty()) {
         for (Map.Entry<String, OntologyTerm> partition : root.getPartitions().entrySet()) {
            if (partition.getValue() == null) { continue; }

            if (partition.getValue().hasNewData()) {
               ontologicalCluster(partition.getValue(), threshold);
               unclusteredData = true;
            }

            if (partition.getValue().getClusters() == null) { continue; }

            for (Cluster clust : partition.getValue().getClusters()) {
               if (clust instanceof HCluster) {
                  clusters.add(new HCluster((HCluster) clust));
               }
            }
            
            if (unclusteredData && root.isTimeSensitive()) {
               clusterDataSet(clusters, threshold);
            }
         }
      
         if (unclusteredData && !root.isTimeSensitive()) {
            clusterDataSet(clusters, threshold);
         }
      }

      if (root.getData() != null && root.getData().size() > 0) {
         for (Cluster clust : root.getData()) {
            if (clust instanceof HCluster) {
               clusters.add(new HCluster((HCluster) clust));
            }
         }
         
         clusterDataSet(clusters, threshold);
      }
      
      root.setClusters(clusters);
   }

   private List<Cluster> copyClusters(List<Cluster> clusters) {
      List<Cluster> clusterCopies = new ArrayList<Cluster>(clusters.size());
      
      for (Cluster clust : clusters) {
         if (clust instanceof HCluster) {
            clusterCopies.add(new HCluster((HCluster) clust));
         }
      }

      return clusterCopies;
   }
}
