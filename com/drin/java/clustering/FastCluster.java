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

   public FastCluster(short iso_id) {
      mElements = new short[2];
      mElements[0] = iso_id;
   }

   public short getID() { return mElements[0]; }
   public short size() { return (short) mElements.length; }
   //TODO
   public float getDiameter() { return 0.0f; }
   public float getMean() { return 0.0f; }

   public float compareTo(FastCluster other) {
      final short[] elemsA = mElements, elemsB = other.mElements;
      Float comparison = null;

      try {
         comparison = mThreadPool.submit(new Callable<Float>() {
            public Float call() {
               float clustSim = 0;

               for (short elemNdxA = 0; elemNdxA < elemsA.length; elemNdxA++) {
                  for (short elemNdxB = 0; elemNdxB < elemsB.length; elemNdxB++) {
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

   @Override
   public String toString() {
      String str = "";

      for (short elemNdx = 0; elemNdx < mElements.length; elemNdx++) {
         str += String.format("%d, ", mElements[elemNdx]);
      }

      return str;
   }
}
