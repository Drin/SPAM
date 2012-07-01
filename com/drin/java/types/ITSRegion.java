package com.drin.java.types;

import com.drin.java.types.DataObject;
import com.drin.java.types.Pyroprint;
import com.drin.java.types.Threshold;

import java.util.Set;
import java.util.HashSet;

public class ITSRegion extends DataObject {
   private static final double DEFAULT_ALPHA = 0.997,
                               DEFAULT_BETA = 0.95;

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

   public Threshold getThreshold() {
      return mThreshold;
   }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof ITSRegion) {
         return mName.equals(((ITSRegion) otherObj).mName);
      }

      return false;
   }

   public Set<Pyroprint> getPyroprints() {
      return mPyroprints;
   }
}
