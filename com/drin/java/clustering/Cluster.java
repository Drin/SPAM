package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public abstract class Cluster {
   private static final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
   private static int CLUST_ID = 1;

   protected int mId, mSize;

   protected DataMetric<Cluster> mMetric;
   protected List<Clusterable<?>> mElements;

   protected String[] mMetaLabels;
   protected float mDiameter, mMean;

   public Cluster(int clustId, int clustSize, DataMetric<Cluster> metric) {
      mId = clustId;
      mMetric = metric;

      mElements = new ArrayList<Clusterable<?>>(clustSize);
      mMetaLabels = null;

      mSize = 0;
      mDiameter = -2;
      mMean = -2;
   }

   public Cluster(int clustSize, DataMetric<Cluster> metric) {
      this(CLUST_ID++, clustSize, metric);
   }

   public Cluster(Cluster oldCluster) {
      this(CLUST_ID++, oldCluster.size(), oldCluster.mMetric);

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
   public static void shutdownThreadPool() { mThreadPool.shutdown(); }

   public int getId() { return mId; }
   public int size() { return mSize; }

   public List<Clusterable<?>> getElements() { return mElements; }

   public float getDiameter() {
      if (mDiameter == -1) { computeStatistics(); }
      return mDiameter;
   }
   public float getMean() {
      if (mMean == -1) { computeStatistics(); }
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
         mMean = total_sim;
         mDiameter = -1;
      }
   }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster) {
         return mId == ((Cluster) otherObj).mId;
      }

      return false;
   }

   public float compareTo(Cluster otherClust) {
      mMetric.apply(this, otherClust);
      float comparison = mMetric.result();

      Logger.error(mMetric.getError(), String.format(
         "error computing metric between '%d' and '%d'\n",
         mId, otherClust.mId
      ));

      return comparison;
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
