package com.drin.biology;

import com.drin.clustering.Clusterable;
import com.drin.biology.Pyroprint;

import com.drin.metrics.DataMetric;

import com.drin.util.Logger;

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
   public double compareTo(final Clusterable<?> otherObj) {
      if (otherObj instanceof ITSRegion) {
         mMetric.apply(this, (ITSRegion) otherObj);

         final double comparison = mMetric.result();

         Logger.debug(String.format(
            "ITSRegionComparator:\n\tComparison between '%s' and '%s': %.04f\n",
            this.getName(), ((ITSRegion)otherObj).getName(), comparison
         ));

         return comparison;
      }

      return -2;
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
