package com.drin.java.clustering;

public class CandidatePair {
   private Cluster mLeftClust, mRightClust;
   private double mCandidateSim;

   public CandidatePair(Cluster clust_A, Cluster clust_B, double sim) {
      mLeftClust = clust_A;
      mRightClust = clust_B;

      mCandidateSim = sim;
   }

   public Cluster getLeftCluster() { return mLeftClust; }
   public Cluster getRightCluster() { return mRightClust; }

   public String getLeftClusterName() { return mLeftClust.getName(); }
   public String getRightClusterName() { return mRightClust.getName(); }

   public double getPairDistance() { return mCandidateSim; }

   public double compareTo(CandidatePair otherPair) {
      return mCandidateSim - otherPair.mCandidateSim;
   }

   public String toString() {
      return String.format("similarity:%.02f\nleft:\n--\n%s\n--\nright:\n--\n%s\n--\n",
                           mCandidateSim, mLeftClust, mRightClust);
   }
}
