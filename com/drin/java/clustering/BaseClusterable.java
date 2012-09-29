package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;

import java.util.Collection;

/**
 * BaseClusterable is an abstract implementation of the {@link Clusterable} interface.
 * BaseClusterable contains implementation details that will be common amongst
 * typical Clusterable objects.
 */
public abstract class BaseClusterable implements Clusterable {
   protected String mName;

   public BaseClusterable(String name) { mName = name; }

   @Override
   public String getName() { return mName; }

   @Override
   public String toString() { return mName; }

   @Override
   public int hashCode() { return mName.hashCode(); }
}
