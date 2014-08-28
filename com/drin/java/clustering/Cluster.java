package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Cluster {
   //private static final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
   private static int CLUST_ID = 1;

   protected int mId, mSize;

   protected List<Clusterable<?>> mElements;
   protected Map<Integer, Float> mSimCache;

   protected String[] mMetaLabels;
   protected float mDiameter, mMean;
   protected boolean mCacheSimilarities;

   public Cluster(int clustId, int clustSize) {
      mId = clustId;

      mElements = new ArrayList<Clusterable<?>>(clustSize);
      mSimCache = new HashMap<Integer, Float>();
      mMetaLabels = null;

      mSize = 0;
      mDiameter = -2.0f;
      mMean = -2.0f;
      mCacheSimilarities = false;
   }

   public Cluster(int clustSize) {
      this(CLUST_ID++, clustSize);
   }

   public Cluster(boolean cacheSims, int clustSize) {
      this(clustSize);

      mCacheSimilarities = cacheSims;
   }

   public Cluster(Cluster oldCluster) {
      this(CLUST_ID++, oldCluster.size());

      for (Clusterable<?> oldElem : oldCluster.getElements()) {
         mElements.add(oldElem.deepCopy());
      }

      mSize = mElements.size();
   }

   /*
    * This is for ontological labels. Clusters should have a set of labels that
    * is a superset of the labels of its data points.
    */
   public void setMetaData(String[] metaLabels) { mMetaLabels = metaLabels; }
   public String[] getMetaData() { return mMetaLabels; }

   public abstract void join(Cluster otherClust);

   public static void resetClusterIDs() { Cluster.CLUST_ID = 1; }
   //public static void shutdownThreadPool() { mThreadPool.shutdown(); }

   public int getId() { return mId; }
   public int size() { return mSize; }
   public String getName() { return mElements.get(0).getName(); }

   public List<Clusterable<?>> getElements() { return mElements; }

   public float getDiameter() {
      if (mDiameter == -2.0f) { computeStatistics(); }
      return mDiameter;
   }
   public float getMean() {
      if (mMean == -2.0f) { computeStatistics(); }
      return mMean;
   }

   protected void computeStatistics() {
      float total_sim = 0.0f, clustSim = 0.0f, diameter = Float.MAX_VALUE;
      int count = 0;

      for (int ndxA = 0; ndxA < mSize; ndxA++) {
         Clusterable<?> elemA = mElements.get(ndxA);

         for (int ndxB = ndxA + 1; ndxB < mSize; ndxB++) {
            Clusterable<?> elemB = mElements.get(ndxB);

            clustSim = elemA.compareTo(elemB);

            if (clustSim > 1.0f) {
               System.err.printf("Similarity too high between %s and %s\n",
                  elemA.getName(), elemB.getName()
               );
            }

            total_sim += clustSim;
            diameter = Math.min(diameter, clustSim);
            count++;
         }
      }

      if (count > 0) {
         mMean = total_sim/count;
         mDiameter = diameter;
      }
      else {
         mMean = -2.0f;
         mDiameter = -2.0f;
      }
   }

   public float compareTo(Cluster otherClust) {
      int count = 0;
      float comparison = 0.0f;

      for (Clusterable<?> elemA : mElements) {
         for (Clusterable<?> elemB : otherClust.getElements()) {
            comparison += elemA.compareTo(elemB);
            count++;
         }
      }

      if (count > 0) {
         if (mCacheSimilarities) {
            mSimCache.put(new Integer(otherClust.getId()),
                           new Float(comparison / count));
         }

         return comparison / count;
      }
      return -2;
   }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster) {
         return mId == ((Cluster) otherObj).mId;
      }

      return false;
   }

   @Override
   public String toString() {
      String elements = "";

      for (Clusterable<?> element : mElements) {
         elements += String.format(", %s", element);
      }

      return String.format("Cluster %d:\n\t%s", mId, elements.substring(2));
   }

   public String prettyPrint(String prefix) {
      String str = mId + ":\n";

      for (Clusterable<?> element : mElements) {
         str += String.format("%s\n", element);
      }

      return str;
   }

}
