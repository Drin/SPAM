package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.dendogram.Dendogram;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public abstract class Cluster {
   private static int CLUST_ID = 1;
   private String mName;

   protected DataMetric<Cluster> mMetric;
   protected Dendogram mDendogram;
   protected Set<Clusterable<?>> mElements;

   public Cluster(DataMetric<Cluster> metric) { this(CLUST_ID++, metric); }

   public Cluster(int clustId, DataMetric<Cluster> metric) {
      mName = String.format("%d", clustId);
      mMetric = metric;
      
      mElements = new HashSet<Clusterable<?>>();
      mDendogram = null;
   }

   public abstract Cluster join(Cluster otherClust);

   public Dendogram getDendogram() { return mDendogram; }
   public Set<Clusterable<?>> getElements() { return mElements; }

   public void add(Clusterable<?> element) { mElements.add(element); }

   public String getName() { return mName; }

   public double compareTo(Cluster otherClust) {
      mMetric.apply(this, otherClust);

      double comparison = mMetric.result();

      Logger.error(mMetric.getError(),
                   String.format("error computing metric between '%s' " +
                                 "and '%s'\n", this.mName,
                                 otherClust.mName));

      return comparison;
   }

   public boolean isSimilar(Cluster otherClust) {
      for (Clusterable<?> elem_A : mElements) {
         for (Clusterable<?> elem_B : otherClust.mElements) {
            if (!elem_A.isSimilar(elem_B)) { return false; }
         }
      }

      return true;
   }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster) {
         Cluster otherClust = (Cluster) otherObj;

         for (Clusterable<?> elem : mElements) {
            if (!otherClust.mElements.contains(elem)) { return false; }
         }

         //return this.getName().equals(otherClust.getName());
         return true;
      }

      return false;
   }

   @Override
   public String toString() { return prettyPrint("\t"); }

   public String prettyPrint(String prefix) {
      String str = this.getName() + ":\n";

      for (Clusterable<?> element : mElements) {
         str += String.format("%s\n", element);
      }

      return str;
   }
}
