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

   public Dendogram getDendogram() { return mDendogram; }
   public Set<E> getElements() { return mElements; }
   public void add(E element) { mElements.add(element); }
   public abstract Cluster<E> join(Cluster<E> otherClust);
   public int size() { return mElements.size(); }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster<?>) {
         Cluster<?> otherClust = (Cluster<?>) otherObj;

         for (E elem : mElements) {
            if (!otherClust.mElements.contains(elem)) { return false; }
         }

         //return this.getName().equals(otherClust.getName());
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
