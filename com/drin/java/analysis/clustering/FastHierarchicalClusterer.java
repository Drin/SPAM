package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.CandidatePair;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JTextArea;

public abstract class FastHierarchicalClusterer implements Clusterer {
   protected Map<String, Map<String, Double>> mSimMap;
   protected Map<Double, List<Cluster>> mResultClusters;
   protected List<Double> mThresholds;
   protected String mName;

   protected JTextArea mCanvas;
   protected double mPercentComplete, mPercentIncr;

   private long mStartTime;

   public HierarchicalClusterer(List<Double> thresholds) {
      mResultClusters = new HashMap<Double, List<Cluster>>();
      mSimMap = new HashMap<String, Map<String, Double>>();
      mThresholds = thresholds;
      mName = "Hierarchical";

      mPercentComplete = 0;
      mPercentIncr = 0;
   }

   public void resetSimilarityCache() { mSimMap.clear(); }
   public Map<Double, List<Cluster>> getClusters() { return mResultClusters; }

   public String getName() { return mName; }

   @Override
   public void setProgressCanvas(JTextArea canvas) { mCanvas = canvas; }

   @Override
   public void writeProgress() {
      if (mCanvas != null) {
         mCanvas.setText(String.format("\n\n\t\t%.02f%% Complete!", mPercentComplete));
                  
         Logger.debug(String.format("Elapsed time at %.02f%% completion: %d ms",
                                    mPercentComplete,
                                    (System.currentTimeMillis() - mStartTime)));

         mPercentComplete += mPercentIncr;
      }
   }

   @Override
   public void clusterData(List<Cluster> clusters) {
      mResultClusters.clear();

      for (Double threshold : mThresholds) {
         clusterDataSet(clusters, threshold);

         mResultClusters.put(threshold, new ArrayList<Cluster>(clusters));
      }
   }

   protected void clusterDataSet(List<Cluster> clusters, double threshold) {
      CandidatePair closeClusters = null;
      mPercentIncr = 100.0/clusters.size();

      do {
         writeProgress();

         closeClusters = findCloseClusters(clusters, threshold);

         if (closeClusters != null) {
            combineClusters(closeClusters, clusters);
         }

      } while (closeClusters != null && clusters.size() > 1);
   }

   protected abstract CandidatePair findCloseClusters(List<Cluster> clusters, double threshold);
   protected abstract Cluster combineClusters(CandidatePair closeClusters, List<Cluster> clusters);

   public double getInterClusterSimilarity() {
      Double lowThreshold = new Double(100);
      double totalClusterSimilarity = 0, similarityCount = 0;

      for (Double thresh : mResultClusters.keySet()) {
         if (thresh.compareTo(lowThreshold) < 0) { lowThreshold = thresh; }
      }

      List<Cluster> clusters = mResultClusters.get(lowThreshold);
      for (int clustNdx_A = 0; clustNdx_A < clusters.size(); clustNdx_A++) {
         Cluster clust_A = clusters.get(clustNdx_A);

         for (int clustNdx_B = clustNdx_A + 1; clustNdx_B < clusters.size(); clustNdx_B++) {
            Cluster clust_B = clusters.get(clustNdx_B);

            if (mSimMap.containsKey(clust_A.getName())) {
               Map<String, Double> simMap = mSimMap.get(clust_A.getName());

               if (simMap.containsKey(clust_B.getName())) {
                  totalClusterSimilarity += simMap.get(clust_B.getName());
                  similarityCount++;
               }

            }
         }
      }

      return totalClusterSimilarity/similarityCount;
   }
}
