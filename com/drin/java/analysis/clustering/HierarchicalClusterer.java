package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.CandidatePair;
import com.drin.java.clustering.CandidateQueue;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JTextArea;

public abstract class HierarchicalClusterer implements Clusterer {
   protected CLUST_SIMILARITY mSimType;
   protected List<Cluster> mClusters;
   protected List<Cluster> mResultClusters;

   protected double mBetaThreshold;

   public HierarchicalClusterer(List<Cluster> clusters, double threshold) {
      mClusters = clusters;
      mBetaThreshold = threshold;

      mSimType = CLUST_SIMILARITY.SQUISHY;
   }

   public List<Cluster> getClusters() { return mResultClusters; }

   @Override
   public void clusterData(JTextArea canvas) {
      mResultClusters = new ArrayList<Cluster>(mClusters);
      clusterDataSet(mResultClusters, mBetaThreshold, canvas);
      //experimentalclusterDataSet(mResultClusters, mBetaThreshold, canvas);
   }

   //This method is the more basic way of clustering. It is primarily being
   //used to compare against the priority queue method
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

   //This takes a threshold parameter so that it's easier for OHClust! to pass
   //mAlphaThreshold rather than having to overwrite the entire method
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

   protected enum CLUST_SIMILARITY {
      SIMILAR, SQUISHY
   }
}
