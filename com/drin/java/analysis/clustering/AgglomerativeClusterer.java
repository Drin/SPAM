package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.CandidatePair;
import com.drin.java.clustering.CandidateQueue;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class AgglomerativeClusterer extends HierarchicalClusterer {
   protected static final int CLUSTER_PAIR_SIZE = 2;

   public AgglomerativeClusterer(List<Cluster> clusters, double threshold) {
      super(clusters, threshold);
   }

   /*
    * //Populate list
    * for (Cluster clust_A : clusters)
    *    for (Cluster clust_B : clusters)
    *       candidate_pair = argmax(compare(clust_A, clust_B))
    *
    *    //this should be implemented in the data structure
    *    if one cluster in candidate_pair is already in list
    *       remove old candidate_pair
    *
    *    add candidate_pair to list of cluster candidates
    */
   @Override
   protected CandidateQueue findCandidatePairs(List<Cluster> clusters, double threshold) {
      CandidateQueue clusterCandidates = new CandidateQueue();

      for (int clustNdx_A = 0; clustNdx_A < clusters.size(); clustNdx_A++) {
         Cluster clust_A = clusters.get(clustNdx_A);

         for (int clustNdx_B = clustNdx_A + 1; clustNdx_B < clusters.size(); clustNdx_B++) {
            Cluster clust_B = clusters.get(clustNdx_B);
            double clustSim = clust_A.compareTo(clust_B);

            if (clustSim > threshold) {
               clusterCandidates.addCandidate(new CandidatePair(clust_A, clust_B, clustSim));
            }
         }
      }

      return clusterCandidates;
   }

   @Override
   protected CandidateQueue recompute(Cluster combinedCluster, List<Cluster> clusters, double threshold) {
      CandidateQueue clusterCandidates = new CandidateQueue();

      for (Cluster otherClust : clusters) {
         if (combinedCluster.getName().equals(otherClust.getName())) { continue; }

         double clustSim = combinedCluster.compareTo(otherClust);

         if (clustSim > threshold) {
            clusterCandidates.addCandidate(new CandidatePair(combinedCluster, otherClust, clustSim));
         }
      }

      return clusterCandidates;
   }

   @Override
   protected Cluster combineClusters(CandidatePair closeClusters, List<Cluster> clusters) {
      Logger.debug(String.format("combining clusters '%s' and '%s'\n\n---\n%s\n---\n\n---\n%s\n---\n\n",
                                 closeClusters.getLeftClusterName(),
                                 closeClusters.getRightClusterName(),
                                 closeClusters.getLeftCluster(),
                                 closeClusters.getRightCluster()));

      int removeNdx = -1;
      Cluster combinedCluster = null;

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         Cluster tmpClust = clusters.get(clustNdx);

         if (tmpClust.getName().equals(closeClusters.getLeftClusterName())) {
            combinedCluster = tmpClust.join(closeClusters.getRightCluster());

            if (combinedCluster == null) {
               System.err.println("wtf couldn't join clusters");
            }

            clusters.set(clustNdx, combinedCluster);
         }

         else if (tmpClust.getName().equals(closeClusters.getRightClusterName())) {
            removeNdx = clustNdx;
         }
      }

      if (removeNdx != -1) {
         clusters.remove(removeNdx);
      }
      else {
         Logger.debug("Remove Index is -1. Error during clustering.\nCluster List:\n");
         for (Cluster clust : clusters) {
            if (clust == null) {
               System.err.println("wtf null cluster");
            }
            Logger.debug(String.format("\n--\n%s\n--\n", clust));
         }
      }

      return combinedCluster;
   }
}

/*
   @Override
   protected Cluster[] findCloseClusters(List<Cluster> clusters) {
      Map<String, Double> clustDistMap = null;
      Cluster close_A = null, close_B = null;
      double maxSim = 0;
      boolean isClose = false;

      for (int ndx_A = 0; ndx_A < clusters.size(); ndx_A++) {
         Cluster clust_A = clusters.get(ndx_A);

         for (int ndx_B = ndx_A + 1; ndx_B < clusters.size(); ndx_B++) {
            Cluster clust_B = clusters.get(ndx_B);
            double clustDist = clust_A.compareTo(clust_B);

            //This switch is to minimize code duplication for hierarchical and
            //OHClustering
            isClose = clustDist > maxSim;

            //TODO SIMILAR should mean that cluster similarity is above alpha
            //SQUISHY should mean that cluster similarity is not below beta
            switch (mSimType) {
               case SIMILAR:
                  isClose = isClose && clust_A.isSimilar(clust_B);
                  break;
               case SQUISHY:
               default:
                  isClose = isClose && !clust_A.isDifferent(clust_B);
                  break;
            }

            if (isClose) {
               close_A = clust_A;
               close_B = clust_B;

               maxSim = clustDist;
               isClose = false;
            }
         }

         clustDistMap = null;
      }

      if (close_A != null && close_B != null) {
         Cluster[] closeClusters = new Cluster[] {close_A, close_B};
         return closeClusters;
      }

      return null;
   }
*/
