package com.drin.java.biology;

import com.drin.java.clustering.FastClusterable;
import com.drin.java.biology.FastPyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Iterator;

import java.util.Map;

/**
 * FastIsolate represents a bacterial isolate collected by a biologist.
 *
 */
public class FastIsolate extends FastClusterable<String, FastPyroprint> {
   private static final int ALPHA_NDX = 0, BETA_NDX = 1;
   private DataMetric<String, FastPyroprint> mMetric;
   private Map<String, double[]> mThresholds;

   public FastIsolate(String isoId, Set<ITSRegion> regions, DataMetric<FastIsolate> metric) {
      super(isoId, regions);

      mMetric = metric;
   }

   public FastIsolate(String isoId, Map<String, double[]> threshMap,
                  DataMetric<FastIsolate> metric) {
      super(isoId, null);

      mMetric = metric;

      mThresholds = threshMap;
   }

   @Override
   public double compareTo(FastClusterable<?> otherObj) {
      if (otherObj instanceof FastIsolate) {
         mMetric.apply(this, (FastIsolate) otherObj);

         double comparison = mMetric.result();

         Logger.debug(String.format("'%s' and '%s' => [%.05f]", this.getName(),
                                    otherObj.getName(), comparison));

         return comparison;
      }

      return -2;
   }

   @Override
   public boolean isSimilar(FastClusterable<?> otherObj) {
      if (otherObj instanceof FastIsolate) {
         if (this.getData() != null) {
            Iterator<ITSRegion> itr_A = this.getData().iterator();

            while (itr_A.hasNext()) {
               ITSRegion region_A = itr_A.next();
               Iterator<ITSRegion> itr_B = ((FastIsolate)otherObj).getData().iterator();

               while (itr_B.hasNext()) {
                  ITSRegion region_B = itr_B.next();

                  if (region_A.getName().equals(region_B.getName())) {
                     if (!region_A.isSimilar(region_B)) { return false; }
                  }
               }
            }

            return true;
         }
         else if (mThresholds != null) {
            double comparison = this.compareTo((FastIsolate) otherObj);

            for (Map.Entry<String, double[]> thresh : mThresholds.entrySet()) {
               if (comparison < thresh.getValue()[ALPHA_NDX]) { return false; }
            }

            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isDifferent(FastClusterable<?> otherObj) {
      if (otherObj instanceof FastIsolate) {
         if (this.getData() != null) {
            Iterator<ITSRegion> itr_A = this.getData().iterator();

            while (itr_A.hasNext()) {
               ITSRegion region_A = itr_A.next();
               Iterator<ITSRegion> itr_B = ((FastIsolate)otherObj).getData().iterator();

               while (itr_B.hasNext()) {
                  ITSRegion region_B = itr_B.next();

                  if (region_A.getName().equals(region_B.getName())) {
                     if (region_A.isDifferent(region_B)) { return true; }
                  }
               }
            }

            return false;
         }
         else if (mThresholds != null) {
            double comparison = this.compareTo((FastIsolate) otherObj);

            for (Map.Entry<String, double[]> thresh : mThresholds.entrySet()) {
               if (comparison >= thresh.getValue()[BETA_NDX]) { return false; }
            }

            return true;
         }
      }

      return true;
   }

   @Override
   public String toString() {
      String str = "";

      if (this.getData() != null) {
         for (ITSRegion region : mData) {
            str += String.format("region '%s':\n", region.getName());

            for (FastPyroprint pyro : region.getData()) {
               str += String.format("\tpyroprint %s\n\n", pyro);
            }
         }
      }
      else { return this.getName(); }

      return str;
   }
}
