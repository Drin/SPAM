package com.drin.java.clustering;

public class CandidatePair implements Comparable<CandidatePair> {
   private Cluster mLeftClust, mRightClust;
   private double mCandidateSim;

   public CandidatePair(Cluster clust_A, Cluster clust_B, double sim) {
      mLeftClust = clust_A;
      mRightClust = clust_B;

      mCandidateSim = sim;
   }

   public Cluster getLeftCluster() { return mLeftClust; }
   public Cluster getRightCluster() { return mRightClust; }

   public Cluster getLeftClusterName() { return mLeftClust.getName(); }
   public Cluster getRightClusterName() { return mRightClust.getName(); }

   public double getPairDistance() { return mCandidateSim; }

   public int compareTo(CandidatePair otherPair) {
      return mCandidateSim - otherPair.mCandidateSim;
   }
}
