package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.FastHierarchicalClusterer;

import com.drin.java.ontology.FastOntology;
import com.drin.java.ontology.FastOntologyTerm;

import com.drin.java.clustering.FastCluster;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class FastOHClusterer extends FastHierarchicalClusterer {
   private float mAlphaThresh;

   public FastOHClusterer(short dataSize, float alphaThresh, float betaThresh) {
      super(dataSize, betaThresh);
      mAlphaThresh = alphaThresh;
   }

   @Override
   public List<FastCluster> getClusters() { return mResultClusters; }

   @Override
   public void clusterData(FastOntology clustOnt) {
      if (clustOnt == null ||
          clustOnt.getRoot() == null ||
          !clustOnt.getRoot().hasNewData()) {
         return;
      }

      ontologicalCluster(clustOnt.getRoot(), mAlphaThresh);
      
      if (clustOnt.getRoot().getClusters() != null) {
         mResultClusters = new ArrayList<FastCluster>(
               clustOnt.getRoot().getClusters().size()
         );
         
         for (FastCluster clust : clustOnt.getRoot().getClusters()) {
            mResultClusters.add(new FastCluster(clust));
         }
         
         super.clusterDataSet(mResultClusters, mThresh);
      }

      else {
         System.err.println("No clusters formed!?");
         return;
      }
   }

   @Override
   public void clusterData(List<FastCluster> clusters) {
      throw new UnsupportedOperationException();
   }

   private void ontologicalCluster(FastOntologyTerm root, float threshold) {
      boolean unclusteredData = false;
      List<FastCluster> clusters = new ArrayList<FastCluster>();

      if (!root.getPartitions().isEmpty()) {
         for (Map.Entry<String, FastOntologyTerm> partition : root.getPartitions().entrySet()) {
            if (partition.getValue() == null) { continue; }

            if (partition.getValue().hasNewData()) {
               ontologicalCluster(partition.getValue(), threshold);
               unclusteredData = true;
            }

            if (partition.getValue().getClusters() == null) { continue; }

            for (FastCluster clust : partition.getValue().getClusters()) {
               clusters.add(new FastCluster(clust));
            }
            partition.getValue().getClusters().clear();
            
            if (unclusteredData && root.isTimeSensitive()) {
               clusterDataSet(clusters, threshold);
            }
         }
      
         if (unclusteredData && !root.isTimeSensitive()) {
            clusterDataSet(clusters, threshold);
         }
      }

      if (root.getData() != null && root.getData().size() > 0) {
         for (FastCluster clust : root.getData()) {
            clusters.add(new FastCluster(clust));
         }
         
         clusterDataSet(clusters, threshold);
      }
      
      root.setClusters(clusters);
   }
}
