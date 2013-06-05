package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Iterator;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends Clusterable<ITSRegion> {
   private static final int ALPHA_NDX = 0, BETA_NDX = 1;
   private DataMetric<Isolate> mMetric;
   private Map<String, Float> mComparisonCache;

   public Isolate(String isoId, DataMetric<Isolate> metric) {
      this(isoId, 2, metric);
   }

   public Isolate(String isoId, int dataSize, DataMetric<Isolate> metric) {
      super(isoId, new HashSet<ITSRegion>(dataSize));

      mMetric = metric;
      mComparisonCache = new HashMap<String, Float>();
   }

   @Override
   public float compareTo(Clusterable<?> otherObj) {
      if (otherObj instanceof Isolate) {
         if (!mComparisonCache.containsKey(otherObj.getName())) {
            mMetric.apply(this, (Isolate) otherObj);
            float comparison = mMetric.result();

            mComparisonCache.put(otherObj.getName(), new Float(comparison));

            Logger.debug(String.format(
               "'%s' and '%s' => [%.05f]",
               this.getName(), otherObj.getName(), comparison
            ));

            return comparison;
         }

         Float compVal = mComparisonCache.get(otherObj.getName());
         if (compVal != null) { return compVal.floatValue(); }
      }

      return -2;
   }

   @Override
   public Clusterable<ITSRegion> deepCopy() {
      Clusterable<ITSRegion> newIsolate = new Isolate(mName, mMetric);

      for (ITSRegion region : mData) {
         newIsolate.getData().add(region.deepCopy());
      }

      return newIsolate;
   }

   @Override
   public String toString() {
      String str = String.format("isolate '%s' [%d regions]:\n",
                                 this.getName(), mData.size());

      for (ITSRegion region : mData) {
         str += String.format("\tregion '%s' [%d pyroprints]:\n",
                              region.getName(), region.getData().size());

         for (Pyroprint pyro : region.getData()) {
            str += String.format("\t\tpyroprint %s\n\n", pyro);
         }
      }

      return str;
   }
}
