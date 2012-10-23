package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

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

   @Override
   public double compareTo(Clusterable<ITSRegion> otherObj) {
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
   public boolean isSimilar(Clusterable<ITSRegion> otherObj) {
      if (otherObj instanceof Isolate) {
         return this.getName().equals(((Isolate)otherObj).getName());
      }

      return false;
   }

   @Override
   public String toString() {
      String str = "";

      for (ITSRegion region : mData) {
         str += String.format("region '%s':\n", region.getName());

         for (Pyroprint pyro : region.getData()) {
            str += String.format("\tpyroprint %s\n\n", pyro);
         }
      }

      return str;
   }
}
