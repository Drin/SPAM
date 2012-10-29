package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public class ITSRegion extends Clusterable<Pyroprint> {
   private DataMetric<ITSRegion> mMetric;
   private double mAlpha, mBeta;

   public ITSRegion(String regionName, double alpha, double beta,
                    DataMetric<ITSRegion> metric) {
      super(regionName, new HashSet<Pyroprint>());
      mAlpha = alpha;
      mBeta = beta;
      mMetric = metric;
   }

   @Override
   public double compareTo(Clusterable<?> otherObj) {
      if (otherObj instanceof ITSRegion) {
         mMetric.apply(this, (ITSRegion) otherObj);

         double comparison = mMetric.result();

         Logger.debug(String.format("ITSRegionComparator:\n\tComparison " +
                                    "between '%s' and '%s': %.04f\n",
                                    this.getName(),
                                    ((ITSRegion)otherObj).getName(),
                                    comparison));

         return comparison;
      }

      return -2;
   }

   @Override
   public boolean isSimilar(Clusterable<?> otherObj) {
      if (otherObj instanceof ITSRegion) {
         return this.compareTo((ITSRegion) otherObj) >= mAlpha;
      }

      return false;
   }

   @Override
   public boolean isDifferent(Clusterable<?> otherObj) {
      if (otherObj instanceof ITSRegion) {
         return this.compareTo((ITSRegion) otherObj) < mBeta;
      }

      return false;
   }

   @Override
   public String toString() {
      String str = mName;

      for (Pyroprint pyro : mData) {
         str += String.format("\t%s\n", pyro);
      }

      return str;
   }
}
