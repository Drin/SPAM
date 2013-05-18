package com.drin.java.clustering;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class FastCluster {
   public static short mNumIsolates = -1;
   public static float[] mSimMatrix = null;
   public static int[][] mSimMapping = null;

   private static final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
   private int[] mElements;
   private int mTail, mSize;

   private float mDiameter, mMean, mPerSim;

   public FastCluster(int iso_id) {
      mElements = new int[2];
      mElements[0] = iso_id;
      mTail = 1;
      mSize = 1;

      mDiameter = mMean = mPerSim = -1.0f;
   }

   public int getID() { return mElements[0]; }
   public int size() { return mSize; }
   public int[] getElements() { return mElements; }

   private void computeStatistics () {
      float total_sim = 0, diameter = Float.MAX_VALUE;
      short num_sim = 0, count = 0;
      float clustSim = 0.0f;

      for (int ndxA = 0; ndxA < mSize; ndxA++) {
         for (int ndxB = (short) (ndxA + 1); ndxB < mSize; ndxB++) {
            clustSim = mSimMatrix[mSimMapping[mElements[ndxA]][
               mElements[ndxB] % (mNumIsolates - mElements[ndxA])]
            ];

            total_sim += clustSim;
            diameter = Math.min(diameter, clustSim);
            if (clustSim > 0.75) { num_sim++; }
            count++;
         }
      }

      if (count > 0) {
         mMean = total_sim/count;
         mPerSim = num_sim/count;
         mDiameter = diameter;
      }
      else {
         mMean = total_sim;
         mPerSim = 1;
         mDiameter = -1;
      }
   }

   public float getDiameter() {
      if (mDiameter == -1) { computeStatistics(); }
      return mDiameter;
   }
   public float getMean() {
      if (mMean == -1) { computeStatistics(); }
      return mMean;
   }

   public float getPercentSimilar() {
      if (mPerSim == -1) { computeStatistics(); }
      return mPerSim;
   }

   public float compareTo(FastCluster other) {
      final int[] elemsA = mElements, elemsB = other.mElements;
      final int lastA = mSize, lastB = other.mSize;
      Float comparison = null;

      try {
         comparison = mThreadPool.submit(new Callable<Float>() {
            public Float call() {
               float clustSim = 0;
               int simCount = 0;

               for (int elemNdxA = 0; elemNdxA < lastA; elemNdxA++) {
                  for (int elemNdxB = 0; elemNdxB < lastB; elemNdxB++) {
                     if (elemsA[elemNdxA] > elemsB[elemNdxB]) {
                        simCount++;
                        clustSim += mSimMatrix[mSimMapping[elemsB[elemNdxB]][
                           elemsA[elemNdxA] % (mNumIsolates - elemsA[elemNdxA])]
                        ];
                     }
                     else if (elemsA[elemNdxA] < elemsB[elemNdxB]) {
                        simCount++;
                        clustSim += mSimMatrix[mSimMapping[elemsA[elemNdxA]][
                           elemsB[elemNdxB] % (mNumIsolates - elemsA[elemNdxA])]
                        ];
                     }
                  }
               }

               if (simCount > 0) { return new Float(clustSim/simCount); }
               else { return new Float(0); }

            }
         }).get();
      }
      catch (Exception err) { err.printStackTrace(); }

      if (comparison != null) {
         return comparison.floatValue();
      }

      return 0;
   }

   public void incorporate(FastCluster other) {
      if (mElements.length < mSize + other.mSize) {
         int[] newArr = new int[(mElements.length * 2) + other.mSize];

         for (int newNdx = 0; newNdx < mSize; newNdx++) {
            newArr[newNdx] = mElements[newNdx];
         }

         mElements = newArr;
      }

      for (short otherNdx = 0; otherNdx < other.mSize; otherNdx++) {
         mElements[mTail++] = other.mElements[otherNdx];
         mSize++;
      }
   }

   public static void shutdownThreadPool() {
      mThreadPool.shutdown();
   }

   @Override
   public String toString() {
      String str = "";

      for (int elemNdx = 0; elemNdx < mSize; elemNdx++) {
         str += String.format("%d, ", mElements[elemNdx]);
      }

      return str;
   }
}
