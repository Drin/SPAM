package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.Cluster;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import javax.swing.JTextArea;

public abstract class HierarchicalClusterer implements Clusterer {
   protected Set<Cluster> mClusters;
   protected Set<Cluster> mResultClusters;

   public HierarchicalClusterer(Set<Cluster> clusters) {
      mClusters = clusters;

      mResultClusters = new HashSet<Cluster>();
   }

   protected Set<Cluster> clusterDataSet(Set<Cluster> clusterSet, JTextArea canvas) {
      Set<Cluster> newClustSet = new HashSet<Cluster>(clusterSet);
      Map<String, Map<String, Double>> clust_distances =
              new HashMap<String, Map<String, Double>>();

      //TODO hack in order to have some indicator of progress
      double percentIncr = 100.0/newClustSet.size();
      double percentComplete = 0;
      System.out.println("Hierarchical Clustering...");

      while (newClustSet.size() > 1) {
         //TODO hack in order to have some indicator of progress
         canvas.setText(String.format("\n\n\t\t%.02f%% Complete!", percentComplete));
         percentComplete += percentIncr;

         Cluster[] closeClusters = findCloseClusters(null, newClustSet);

         if (closeClusters != null) {
            newClustSet = combineClusters(closeClusters, newClustSet);
         }

         else {
            canvas.setText(String.format("\n\t\tNo more similar clusters...\n\t\t100%% Complete!"));
            break;
         }
      }

      return newClustSet;
   }

   @Override
   public void clusterData(JTextArea canvas) {
      for (Cluster cluster : clusterDataSet(mClusters, canvas)) {
         mResultClusters.add(cluster);
      }
   }

   public Set<Cluster> getClusters() {
      Set<Cluster> resultClusters = new HashSet<Cluster>();

      for (Cluster cluster : mResultClusters) {
         resultClusters.add(cluster);
      }

      return resultClusters;
   }

   protected abstract Cluster[] findCloseClusters(Map<String, Map<String, Double>> distMap,
                                                  Set<Cluster> clusterSet);
   protected abstract Set<Cluster> combineClusters(Cluster[] closeClusters,
                                                   Set<Cluster> clusterSet);
}
