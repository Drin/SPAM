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

   public FastCluster(short iso_id) {
      mElements = new short[2];
      mElements[0] = iso_id;
      mTail = 1;
      mSize = 1;
   }

   public short getID() { return mElements[0]; }
   public short size() { return mSize; }

   //TODO
   public float getDiameter() { return 0.0f; }
   public float getMean() { return 0.0f; }

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
