package com.drin.java.clustering;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;

public class FastCluster {
   public static int mNumIsolates = -1;
   public static float[] mSimMatrix = null;
   public static int[][] mSimMapping = null;
   public static boolean mTransform = false;

   private static final float ALPHA_THRESH = 0.90f,
                              BETA_THRESH  = 0.85f;

   private static final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
   private int[] mElements;
   private int mTail;

   private float mDiameter, mMean, mPerSim;

   public FastCluster(int isoID, int clustSize) {
      mElements = new int[clustSize];
      mElements[0] = isoID;
      mTail = 1;

      mDiameter = mMean = mPerSim = -1.0f;
   }
   
   public FastCluster(int isoID) {
      this(isoID, 2);
   }
   
   public FastCluster(FastCluster oldClust) {
      this(oldClust.mElements[0], oldClust.size());
      
      for (int elemNdx = 1; elemNdx < oldClust.size(); elemNdx++) {
         mElements[mTail++] = oldClust.mElements[elemNdx];
      }
   }

   public int getID() { return mElements[0]; }
   public int size() { return mTail; }
   public int[] getElements() { return mElements; }

   public boolean equals(Object other) {
      if (other instanceof FastCluster) {
         return mElements[0] == ((FastCluster) other).mElements[0];
      }
      
      return false;
   }

   private void computeStatistics () {
      float total_sim = 0, diameter = Float.MAX_VALUE;
      short num_sim = 0, count = 0;
      float clustSim = 0.0f;

      for (int ndxA = 0; ndxA < mTail; ndxA++) {
         for (int ndxB = (short) (ndxA + 1); ndxB < mTail; ndxB++) {
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
      final int lastA = mTail, lastB = other.mTail;
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
                           elemsA[elemNdxA] % (mNumIsolates - elemsB[elemNdxB])]
                        ];
                     }
                     else if (elemsA[elemNdxA] < elemsB[elemNdxB]) {
                        simCount++;
                        clustSim += mSimMatrix[mSimMapping[elemsA[elemNdxA]][
                           elemsB[elemNdxB] % (mNumIsolates - elemsA[elemNdxA])]
                        ];
                     }
                     else if (elemsA[elemNdxA] == elemsB[elemNdxB]) {
                        System.out.printf("comparison between the same isolate!\n" +
                           "[ %s and %s ]\n", elemsA[elemNdxA], elemsB[elemNdxB]
                        );
                     }
                  }
               }


               try {
                  if (simCount > 0) { return new Float(clustSim/simCount); }
               }
               catch (Exception err) {
                  System.out.println("Caught inside of the thread");
                  err.printStackTrace();
               }

               return new Float(0.0f);
            }
         }).get();
      }
      catch (Exception err) {
         System.out.println("Caught outside of the thread");
         err.printStackTrace();
         System.exit(0);
      }

      if (comparison != null) {
         if (mTransform) {
            if (comparison.floatValue() >= ALPHA_THRESH) {
               return 1.0f;
            }
            else if (comparison.floatValue() < BETA_THRESH) {
               return 0.0f;
            }
         }

         return comparison.floatValue();
      }

      return 0.0f;
   }

   public void incorporate(FastCluster other) {
      if (mElements.length < mTail + other.mTail) {
         int[] newArr = new int[(mElements.length * 2) + other.mTail];

         for (int newNdx = 0; newNdx < mTail; newNdx++) {
            newArr[newNdx] = mElements[newNdx];
         }

         mElements = newArr;
      }

      for (short otherNdx = 0; otherNdx < other.mTail; otherNdx++) {
         mElements[mTail++] = other.mElements[otherNdx];
      }
   }

   public static void shutdownThreadPool() {
      mThreadPool.shutdown();
   }

   @Override
   public String toString() {
      String str = "";

      for (int elemNdx = 0; elemNdx < mTail; elemNdx++) {
         str += String.format("%d, ", mElements[elemNdx]);
      }

      return str;
   }
}
