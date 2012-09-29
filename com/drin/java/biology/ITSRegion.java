package com.drin.java.biology;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.Threshold;

import java.util.Set;
import java.util.HashSet;

public class ITSRegion extends BaseClusterable {
   private static final double DEFAULT_ALPHA = 0.995,
                               DEFAULT_BETA = 0.99;

   private Set<Pyroprint> mPyroprints;
   private Threshold mThreshold;

   public ITSRegion(String regionName, double alpha, double beta) {
      super(regionName);

      mThreshold = new Threshold(alpha, beta);
      mPyroprints = new HashSet<Pyroprint>();
   }

   public ITSRegion(String regionName) {
      this(regionName, DEFAULT_ALPHA, DEFAULT_BETA);
   }

   public Threshold getThreshold() { return mThreshold; }
   public Set<Pyroprint> getPyroprints() { return mPyroprints; }
   public void add(Pyroprint pyro) { mPyroprints.add(pyro); }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof ITSRegion) {
         return mName.equals(((ITSRegion) otherObj).mName);
      }

      return false;
   }
}
