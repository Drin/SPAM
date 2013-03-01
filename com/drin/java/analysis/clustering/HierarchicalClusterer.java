package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.CandidatePair;
import com.drin.java.clustering.CandidateQueue;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JTextArea;

public abstract class HierarchicalClusterer implements Clusterer {
   protected List<Cluster> mClusters;
   protected List<Double> mThresholds;
   protected Map<Double, List<Cluster>> mResultClusters;

   public HierarchicalClusterer(List<Cluster> clusters, List<Double> thresholds) {
      mClusters = clusters;
      mThresholds = thresholds;
   }

   public Map<Double, List<Cluster>> getClusters() { return mResultClusters; }

   @Override
   public void clusterData(JTextArea canvas) {
      mResultClusters = new HashMap<Double, List<Cluster>>();
      List<Cluster> resultClusters = new ArrayList<Cluster>(mClusters);

      for (Double threshold : mThresholds) {
         clusterDataSet(resultClusters, threshold, canvas);

         mResultClusters.put(threshold, new ArrayList<Cluster>(resultClusters));
      }
   }

   protected void clusterDataSet(List<Cluster> clusters, double threshold, JTextArea canvas) {
      double percentIncr = 100.0/clusters.size(), percentComplete = 0;
      Map<String, Map<String, Double>> clustDistMap = new HashMap<String, Map<String, Double>>();

      CandidatePair closeClusters = findCloseClusters(clustDistMap, clusters, threshold);

      for (; closeClusters != null && clusters.size() > 1;
           closeClusters = findCloseClusters(clustDistMap, clusters, threshold)) {
         if (canvas != null) {
            canvas.setText(String.format("\n\n\t\t%.02f%% Complete!", percentComplete));
            percentComplete += percentIncr;
         }

         combineClusters(closeClusters, clusters);
      }
   }

   //This was experimental and an attempt to speed up clustering. It didn't,
   //but it took me awhile to implement so I'm leaving it for my own
   //satisfaction
   protected void experimentalClusterDataSet(List<Cluster> clusters, double threshold, JTextArea canvas) {
      double percentIncr = 100.0/clusters.size(), percentComplete = 0;

      if (canvas != null) {
         canvas.setText(String.format("\n\n\t\t%.02f%% Complete!", percentComplete));
         percentComplete += percentIncr;
      }

      CandidateQueue clusterCandidates = findCandidatePairs(clusters, threshold);
      CandidatePair closeClusters = clusterCandidates.dequeue();

      for (; closeClusters != null; closeClusters = clusterCandidates.dequeue()) {
         if (canvas != null) {
            canvas.setText(String.format("\n\n\t\t%.02f%% Complete!", percentComplete));
            percentComplete += percentIncr;
         }

         Cluster combinedCluster = combineClusters(closeClusters, clusters);
         clusterCandidates.addAllCandidates(recompute(combinedCluster, clusters, threshold));
      }
   }

   protected abstract CandidateQueue findCandidatePairs(List<Cluster> clusters, double threshold);
   protected abstract CandidatePair findCloseClusters(Map<String, Map<String, Double>> distMap, List<Cluster> clusters, double threshold);

   protected abstract CandidateQueue recompute(Cluster combinedCluster, List<Cluster> clusters, double threshold);
   protected abstract Cluster combineClusters(CandidatePair closeClusters, List<Cluster> clusters);
}
