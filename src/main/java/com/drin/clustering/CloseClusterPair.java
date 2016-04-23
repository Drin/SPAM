package com.drin.clustering;

import com.drin.clustering.Cluster;

import java.util.List;

public class CloseClusterPair {
   private Cluster mLeftClust, mRightClust;
   private int mLeftNdx, mRightNdx;

   public CloseClusterPair(final Cluster leftClust, final Cluster rightClust,
                           final int leftNdx, final int rightNdx) {
      mLeftClust = leftClust;
      mLeftNdx = leftNdx;

      mRightClust = rightClust;
      mRightNdx = rightNdx;
   }

   public static CloseClusterPair fromClusterIndices(final List<Cluster> clusters,
                                                     Integer[] closeClusterIndices) {
      final int leftNdx = closeClusterIndices[0], rightNdx = closeClusterIndices[1];

      return new CloseClusterPair(
         clusters.get(leftNdx), clusters.get(rightNdx),
         leftNdx, rightNdx
      );
   }

   public Cluster getLeftCluster() { return mLeftClust; }
   public Cluster getRightCluster() { return mRightClust; }

   public int getLeftIndex() { return mLeftNdx; }
   public int getRightIndex() { return mRightNdx; }

   public String getLeftClusterName() { return mLeftClust.getName(); }
   public String getRightClusterName() { return mRightClust.getName(); }

   public String toString() {
      return String.format(
         "leftCluster (%d): %s\nrightCluster (%d): %s",
         mLeftNdx, mLeftClust, mRightNdx, mRightClust
      );
   }
}
