package com.drin.java.biology;

import com.drin.java.ontology.Labelable;
import com.drin.java.ontology.OntologyLabel;

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
public class Isolate extends Clusterable<ITSRegion> implements Labelable {
   private static final int ALPHA_NDX = 0, BETA_NDX = 1;
   private DataMetric<Isolate> mMetric;
   private Map<String, double[]> mThresholds;
   private Map<String, Double> mComparisonCache;

   protected OntologyLabel mLabel;

   public Isolate(String isoId, Set<ITSRegion> regions, DataMetric<Isolate> metric) {
      super(isoId, regions);

      mMetric = metric;
      mLabel = new OntologyLabel();
      mComparisonCache = new HashMap<String, Double>();
   }

   public Isolate(String isoId, Map<String, double[]> threshMap,
                  DataMetric<Isolate> metric) {
      super(isoId, null);

      mMetric = metric;
      mThresholds = threshMap;
      mLabel = new OntologyLabel();
      mComparisonCache = new HashMap<String, Double>();
   }

   public void addLabel(String labelName) {
      mLabel.addLabel(labelName);
   }

   public boolean hasLabel(String labelName) {
      return mLabel.hasLabel(labelName);
   }

   @Override
   public double compareTo(Clusterable<?> otherObj) {
      if (otherObj instanceof Isolate) {
         if (!mComparisonCache.containsKey(otherObj.getName())) {
            mMetric.apply(this, (Isolate) otherObj);
            double comparison = mMetric.result();

            mComparisonCache.put(otherObj.getName(), new Double(comparison));

            Logger.debug(String.format("'%s' and '%s' => [%.05f]", this.getName(),
                                       otherObj.getName(), comparison));
            return comparison;
         }

         Double compVal = mComparisonCache.get(otherObj.getName());
         if (compVal != null) { return compVal.doubleValue(); }
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
               if (comparison < thresh.getValue()[ALPHA_NDX]) { return false; }
            }

            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isDifferent(Clusterable<?> otherObj) {
      if (otherObj instanceof Isolate) {
         if (this.getData() != null) {
            Iterator<ITSRegion> itr_A = this.getData().iterator();

            while (itr_A.hasNext()) {
               ITSRegion region_A = itr_A.next();
               Iterator<ITSRegion> itr_B = ((Isolate)otherObj).getData().iterator();

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
            double comparison = this.compareTo((Isolate) otherObj);

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
      String str = String.format("isolate '%s' [%d regions]:\n",
                                 this.getName(), mData.size());

      if (this.getData() != null) {
         for (ITSRegion region : mData) {
            str += String.format("region '%s' [%d pyroprints]:\n",
                                 region.getName(), region.getData().size());

            for (Pyroprint pyro : region.getData()) {
               str += String.format("\tpyroprint %s\n\n", pyro);
            }
         }
      }
      else { return this.getName(); }

      return str;
   }
}
