package com.drin.java.clustering;

import com.drin.java.metrics.DataMetric;

import java.util.Map;

public abstract class FastClusterable<K, V> {
   protected String mName;
   protected Map<K, V> mData;

   public FastClusterable(String name, Map<K, V> data) {
      mName = name;
      mData = data;
   }

   @Override
   public String toString() { return mName; }

   @Override
   public int hashCode() { return mName.hashCode(); }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof FastClusterable) {
         return mName.equals(((FastClusterable)obj).getName());
      }

      return false;
   }

   public int size() { return mData.size(); }
   public String getName() { return mName; }
   public Map<K, V> getData() { return mData; }

   //public abstract String getDesignation();
   public abstract double compareTo(FastClusterable<?> otherData);
   public abstract boolean isSimilar(FastClusterable<?> otherData);
   public abstract boolean isDifferent(FastClusterable<?> otherData);
}
