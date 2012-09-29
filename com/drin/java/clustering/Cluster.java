package com.drin.java.clustering;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.dendogram.Dendogram;

import java.util.Set;
import java.util.HashSet;

public abstract class Cluster<E extends BaseClusterable> extends BaseClusterable {
   private static int CLUST_ID = 1;

   protected Dendogram mDendogram;
   protected Set<E> mElements;

   public Cluster() { this(CLUST_ID++); }
   public Cluster(int clustId) {
      super(String.format("%d", clustId));
      mElements = new HashSet<E>();
      mDendogram = null;
   }

   public int size() { return mElements.size(); }
   public void add(E element) { mElements.add(element); }
   public Set<E> getElements() { return mElements; }
   public Dendogram getDendogram() { return mDendogram; }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster<?>) {
         Cluster<?> otherClust = (Cluster<?>) otherObj;
         int isoNdx = 0;

         for (E elem : mElements) {
            if (!otherClust.mElements.contains(elem)) { return false; }
         }

         return true;
      }

      return false;
   }

   public String prettyPrint(String prefix) {
      String str = "";

      for (E element : mElements) {
         str += String.format("%s\n", element);
      }

      return str;
   }

   @Override
   public String toString() {
      return prettyPrint("");
   }
}
