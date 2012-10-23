package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.dendogram.Dendogram;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

@SuppressWarnings("rawtypes")
public abstract class Cluster extends Clusterable<Clusterable> {
   private static int CLUST_ID = 1;

   protected DataMetric<Cluster> mMetric;
   protected Dendogram mDendogram;

   public Cluster(DataMetric<Cluster> metric) { this(CLUST_ID++, metric); }

   public Cluster(int clustId, DataMetric<Cluster> metric) {
      super(String.format("%d", clustId), new HashSet<Clusterable>());

      mMetric = metric;
      mDendogram = null;
   }

   public abstract Cluster join(Cluster otherClust);

   public Dendogram getDendogram() { return mDendogram; }

   public void add(Clusterable element) { mData.add(element); }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster) {
         Cluster otherClust = (Cluster) otherObj;

         for (Clusterable elem : mData) {
            if (!otherClust.mData.contains(elem)) { return false; }
         }

         //return this.getName().equals(otherClust.getName());
         return true;
      }

      return false;
   }

   public String prettyPrint(String prefix) {
      String str = "";

      for (Clusterable element : mData) {
         str += String.format("%s\n", element);
      }

      return str;
   }

   @Override
   public String toString() { return prettyPrint(""); }

   @Override
   public double compareTo(Clusterable<Clusterable> otherObj) {
      if (otherObj instanceof Cluster) {
         mMetric.apply(this, (Cluster) otherObj);

         double comparison = mMetric.result();

         Logger.error(mMetric.getError(),
                      String.format("error computing metric between '%s' " +
                                    "and '%s'\n", this.getName(),
                                    ((Cluster)otherObj).getName()));

         return comparison;
      }

      return -2;
   }

   @Override
   public boolean isSimilar(Clusterable<Clusterable> otherObj) {
      if (otherObj instanceof Cluster) {
         return this.getName().equals(((Cluster)otherObj).getName());
      }

      return false;
   }
}
