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
   protected Map<String, Map<String, Double>> mSimMap;
   protected Map<Double, List<Cluster>> mResultClusters;
   protected List<Double> mThresholds;

   protected JTextArea mCanvas;
   protected double mPercentComplete, mPercentIncr;

   public HierarchicalClusterer(List<Double> thresholds) {
      mResultClusters = new HashMap<Double, List<Cluster>>();
      mSimMap = new HashMap<String, Map<String, Double>>();
      mThresholds = thresholds;

      mPercentComplete = 0;
      mPercentIncr = 0;
   }

   public void resetSimilarityCache() { mSimMap.clear(); }
   public Map<Double, List<Cluster>> getClusters() { return mResultClusters; }

   @Override
   public void setProgressCanvas(JTextArea canvas) { mCanvas = canvas; }

   @Override
   public void writeProgress() {
      if (mCanvas != null) {
         mCanvas.setText(String.format("\n\n\t\t%.02f%% Complete!", mPercentComplete));
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
}
