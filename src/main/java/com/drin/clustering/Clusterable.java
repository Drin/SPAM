package com.drin.clustering;

import java.util.Collection;

public abstract class Clusterable {
   protected String mName;

   public Clusterable(String name) { mName = name; }

   @Override
   public String toString() { return mName; }

   @Override
   public int hashCode() { return mName.hashCode(); }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Clusterable) {
         return mName.equals(((Clusterable)obj).mName);
      }

      return false;
   }

   public abstract double compareTo(Clusterable otherData);
}
