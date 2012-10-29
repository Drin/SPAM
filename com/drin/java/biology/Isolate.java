package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Iterator;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends Clusterable<ITSRegion> {
   private static final int ALPHA_NDX = 0, BETA_NDX = 1;
   private DataMetric<Isolate> mMetric;
   private Map<String, double[]> mThresholds;

   public Isolate(String isoId, Set<ITSRegion> regions, DataMetric<Isolate> metric) {
      super(isoId, regions);

      mMetric = metric;
   }

   public Isolate(String isoId, Map<String, double[]> threshMap,
                  DataMetric<Isolate> metric) {
      super(isoId, null);

      mMetric = metric;

      mThresholds = threshMap;
   }

   @Override
   public double compareTo(Clusterable<?> otherObj) {
      if (otherObj instanceof Isolate) {
         mMetric.apply(this, (Isolate) otherObj);

         double comparison = mMetric.result();

         Logger.debug(String.format("'%s' and '%s' => [%.05f]", this.getName(),
                                    otherObj.getName(), comparison));

         return comparison;
      }

      return -2;
   }

   @Override
   public boolean isSimilar(Clusterable<?> otherObj) {
      if (otherObj instanceof Isolate) {
         if (this.getData() != null) {
            Iterator<ITSRegion> itr_A = this.getData().iterator();

            while (itr_A.hasNext()) {
               ITSRegion region_A = itr_A.next();
               Iterator<ITSRegion> itr_B = ((Isolate)otherObj).getData().iterator();

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
            double comparison = this.compareTo((Isolate) otherObj);

            for (Map.Entry<String, double[]> thresh : mThresholds.entrySet()) {
               return comparison > thresh.getValue()[ALPHA_NDX];
            }
         }
      }

      return false;
   }

   @Override
   public String toString() {
      String str = "";

      if (this.getData() != null) {
         for (ITSRegion region : mData) {
            str += String.format("region '%s':\n", region.getName());

            for (Pyroprint pyro : region.getData()) {
               str += String.format("\tpyroprint %s\n\n", pyro);
            }
         }
      }
      else { return this.getName(); }

      return str;
   }
}
