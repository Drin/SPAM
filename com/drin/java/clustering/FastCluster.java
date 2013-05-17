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
   private short[] mElements;
   private short mTail, mSize;

   private float mDiameter, mMean, mPerSim;

   public FastCluster(short iso_id) {
      mElements = new short[2];
      mElements[0] = iso_id;
      mTail = 1;
      mSize = 1;

      mDiameter = mMean = mPerSim = -1.0f;
   }

   public short getID() { return mElements[0]; }
   public short size() { return mSize; }
   public short[] getElements() { return mElements; }

   private void computeStatistics () {
      float total_sim = 0, diameter = Float.MAX_VALUE;
      short num_sim = 0, count = 0;
      float clustSim = 0.0f;

      for (short ndxA = 0; ndxA < mSize; ndxA++) {
         for (short ndxB = (short) (ndxA + 1); ndxB < mSize; ndxB++) {
            clustSim = mSimMatrix[mSimMapping[mElements[ndxA]][
               mElements[ndxB] % (mNumIsolates - mElements[ndxA])]
            ];

            total_sim += clustSim;
            diameter = Math.min(diameter, clustSim);
            if (clustSim > 0.75) { num_sim++; }
            count++;
         }
      }

      mMean = total_sim/count;
      mDiameter = diameter;
      mPerSim = num_sim/count;
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
      final short[] elemsA = mElements, elemsB = other.mElements;
      final short lastA = mSize, lastB = other.mSize;
      Float comparison = null;

      try {
         comparison = mThreadPool.submit(new Callable<Float>() {
            public Float call() {
               float clustSim = 0;

               for (short elemNdxA = 0; elemNdxA < lastA; elemNdxA++) {
                  for (short elemNdxB = 0; elemNdxB < lastB; elemNdxB++) {
                     if (elemsA[elemNdxA] > elemsB[elemNdxB]) {
                        clustSim += mSimMatrix[mSimMapping[elemsB[elemNdxB]][
                           elemsA[elemNdxA] % (mNumIsolates - elemsA[elemNdxA])]
                        ];
                     }
                     else if (elemsA[elemNdxA] < elemsB[elemNdxB]) {
                        clustSim += mSimMatrix[mSimMapping[elemsA[elemNdxA]][
                           elemsB[elemNdxB] % (mNumIsolates - elemsA[elemNdxA])]
                        ];
                     }
                  }
               }

               return new Float(clustSim);
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
         short[] newArr = new short[(mElements.length * 2) + other.mSize];

         for (short newNdx = 0; newNdx < mSize; newNdx++) {
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

      for (short elemNdx = 0; elemNdx < mElements.length; elemNdx++) {
         str += String.format("%d, ", mElements[elemNdx]);
      }

      return str;
   }
}
