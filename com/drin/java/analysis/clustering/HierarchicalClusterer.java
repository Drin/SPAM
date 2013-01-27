package com.drin.java.analysis.clustering;

import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.clustering.Cluster;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JTextArea;

public abstract class HierarchicalClusterer implements Clusterer {
   protected CLUST_SIMILARITY mSimType;
   protected List<Cluster> mClusters;
   protected List<Cluster> mResultClusters;

   public HierarchicalClusterer(List<Cluster> clusters) {
      mClusters = clusters;

      mSimType = CLUST_SIMILARITY.SQUISHY;
   }

   public List<Cluster> getClusters() { return mResultClusters; }

   @Override
   public void clusterData(JTextArea canvas) {
      mResultClusters = new ArrayList<Cluster>();
      mResultClusters.addAll(clusterDataSet(mClusters, canvas));
   }

   protected List<Cluster> clusterDataSet(List<Cluster> clusters, JTextArea canvas) {
      List<Cluster> newClusters = new ArrayList<Cluster>(clusters);
      Map<String, Map<String, Double>> clust_distances;

      //TODO hack in order to have some indicator of progress
      double percentIncr = 100.0/newClusters.size();
      double percentComplete = 0;

      /*
       * //Populate list
       * for (Cluster clust_A : clusters)
       *    for (Cluster clust_B : clusters)
       *       candidate_pair = argmax(compare(clust_A, clust_B))
       *
       *    if one cluster in candidate_pair is already in list
       *       remove old candidate_pair
       *
       *    add candidate_pair to list of cluster candidates
       *
       * //cluster list, and re-populate on each iteration
       * do
       *    closeClusters = best candidate_pair (hopefully list is a pQueue)
       *    combine(closeClusters)
       *    candidate_pair = recompute(closeClusters)
       *
       *    if one cluster in candidate_pair is already in list
       *       remove old candidate_pair
       *    add candidate_pair
       *
       *    if peek_candidate_pair() == null: break
       *
       * while (newClusters.size() > 1)
       *
       */

      clust_distances = new HashMap<String, Map<String, Double>>();

      while (newClusters.size() > 1) {
         //TODO hack in order to have some indicator of progress
         if (canvas != null) {
            canvas.setText(String.format("\n\n\t\t%.02f%% Complete!", percentComplete));
            percentComplete += percentIncr;
         }

         Cluster[] closeClusters = findCloseClusters(new HashMap<String, Map<String, Double>>(), newClusters);

         if (closeClusters != null) { combineClusters(closeClusters, newClusters); }
         else { break; }
      }

      return newClusters;
   }

   protected abstract Cluster[] findCloseClusters(Map<String, Map<String, Double>> distMap, List<Cluster> clusters);
   protected abstract void combineClusters(Cluster[] closeClusters, List<Cluster> clusters);

   protected enum CLUST_SIMILARITY {
      SIMILAR, SQUISHY
   }
}
