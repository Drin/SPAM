package com.drin.java.types;

import com.drin.java.types.DataObject;
import com.drin.java.metrics.DataMetric;

import java.util.List;
import java.util.ArrayList;

public abstract class Cluster<E extends DataObject> extends DataObject {
   private static int CLUST_ID = 1;
   private static final String DEFAULT_NAME = "cluster ";
   protected List<E> mElements;

   public Cluster(String name) {
      super(name);
      mElements = new ArrayList<E>();
   }

   public Cluster(String name, E element) {
      this(name);
      mElements.add(element);
   }

   public Cluster() {
      this(DEFAULT_NAME + CLUST_ID++);
   }

   public int size() {
      return mElements.size();
   }

   public E getElement(int ndx) {
      return mElements.get(ndx);
   }

   @SuppressWarnings("unchecked")
   public List<E> getElements() {
      List<E> elementList = new ArrayList<E>();

      for (E element : mElements) {
         if (element instanceof Cluster) {
            elementList.addAll(((Cluster) element).getElements());
         }
         else { elementList.add(element); }
      }

      return elementList;
   }

   public List<E> getElementList() {
      return mElements;
   }

   public Cluster<E> addElement(E newElement) {
      mElements.add(newElement);

      return this;
   }

   public abstract Cluster<E> union(Cluster<E> otherCluster);

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster) {
         int isoNdx = 0;

         for (; isoNdx < mElements.size() && mElements.get(isoNdx).equals(
          ((Cluster)otherObj).mElements.get(isoNdx)); isoNdx++) { }

         return isoNdx == mElements.size();
      }

      return false;
   }

   public abstract String getDendogram(DataMetric dataMetric);

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
