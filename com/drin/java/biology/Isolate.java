package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends Clusterable<ITSRegion> {
   private DataMetric<Isolate> mMetric;

   public Isolate(String isoId, Set<ITSRegion> regions, DataMetric<Isolate> metric) {
      super(isoId, regions);

      mMetric = metric;
   }

   public Isolate(String isoId, DataMetric<Isolate> metric) {
      this(isoId, null, metric);
   }

   @Override
   public double compareTo(Clusterable<?> otherObj) {
      if (otherObj instanceof Isolate) {
         mMetric.apply(this, (Isolate) otherObj);

         double comparison = mMetric.result();

         Logger.debug(String.format("Isolate Comparator:\n\tisolate " +
                                    "comparison: %.04f", comparison));

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
         else return this.compareTo((Isolate) otherObj) > 99.5;
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
