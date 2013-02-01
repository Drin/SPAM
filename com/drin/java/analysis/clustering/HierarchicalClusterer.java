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
   }

   /*
    * //cluster list, and re-populate on each iteration
    * do
    *    closeClusters = best candidate_pair (hopefully list is a pQueue)
    *    combine(closeClusters)
    *    candidate_pair = recompute(closeClusters)
    *
    *    //this should be implemented in the data structure
    *    if one cluster in candidate_pair is already in list
    *       remove old candidate_pair
    *    add candidate_pair
    *
    *    if peek_candidate_pair() == null: break
    *
    * while (clusters.size() > 1)
    *
    */
   //This takes a threshold parameter so that it's easier for OHClust! to pass
   //mAlphaThreshold rather than having to overwrite the entire method
   protected void clusterDataSet(List<Cluster> clusters, double threshold, JTextArea canvas) {
      double percentIncr = 100.0/clusters.size(), percentComplete = 0;

      CandidateQueue clusterCandidates = findCandidatePairs(clusters, threshold);

      while (clusters.size() > 1) {
         CandidatePair closeClusters = clusterCandidates.dequeue();
         Cluster combinedCluster = combineClusters(closeClusters, clusters);

         clusterCandidates.addAllCandidates(recompute(combinedCluster, clusters, threshold));

         //we call peek because we don't know that combinedCluster is the best
         //candidate
         if (clusterCandidates.peek() == null) { break; }
      }
   }

   protected abstract CandidateQueue findCandidatePairs(List<Cluster> clusters, double threshold);
   protected abstract CandidateQueue recompute(Cluster combinedCluster, List<Cluster> clusters, double threshold);
   protected abstract Cluster combineClusters(CandidatePair closeClusters, List<Cluster> clusters);

   protected enum CLUST_SIMILARITY {
      SIMILAR, SQUISHY
   }
}

/*
while (clusters.size() > 1) {
   if (canvas != null) {
      canvas.setText(String.format("\n\n\t\t%.02f%% Complete!", percentComplete));
      percentComplete += percentIncr;
   }

   Cluster[] closeClusters = findCloseClusters(clusters);

   if (closeClusters != null) { combineClusters(closeClusters, clusters); }
   else { break; }
}
*/
