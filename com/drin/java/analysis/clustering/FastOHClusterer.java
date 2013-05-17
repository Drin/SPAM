package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.FastHierarchicalClusterer;

import com.drin.java.ontology.FastOntology;
import com.drin.java.ontology.FastOntologyTerm;

import com.drin.java.clustering.FastCluster;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class FastOHClusterer extends FastHierarchicalClusterer {
   protected FastOntology mOntology;
   protected List<FastCluster> mResultClusters;
   private float mAlphaThresh;

   public FastOHClusterer(FastOntology ontology, short dataSize,
                          float alphaThresh, float betaThresh) {
      super(dataSize, betaThresh);
      mAlphaThresh = alphaThresh;
      mOntology = ontology;
   }

   public void clusterData(List<FastCluster> clusters) {
      if (mOntology == null) {
         super.clusterData(clusters);
         return;
      }

      for (FastCluster fastClust : clusters) { mOntology.addData(fastClust); }

      if (mOntology.getRoot().hasNewData()) {
         ontologicalCluster(mOntology.getRoot(), mAlphaThresh);
      }

      List<FastCluster> mResultClusters = mOntology.getRoot().getClusters();
      if (mResultClusters == null) {
         System.err.println("No clusters formed!?");
         return;
      }

      super.clusterDataSet(mResultClusters, mThresh);
   }

   private void ontologicalCluster(FastOntologyTerm root, float threshold) {
      List<FastCluster> clusters = new ArrayList<FastCluster>();
      boolean unclusteredData = false;

      if (root == null || !root.hasNewData()) { return; }
      else if (!root.getPartitions().isEmpty()) {

         for (Map.Entry<String, FastOntologyTerm> partition : root.getPartitions().entrySet()) {
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
}
