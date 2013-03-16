package com.drin.java.biology;

import com.drin.java.ontology.Labelable;
import com.drin.java.ontology.OntologyLabel;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends Clusterable<ITSRegion> implements Labelable {
   private DataMetric<Isolate> mMetric;
   private Map<String, Double> mComparisonCache;

   protected OntologyLabel mLabel;

   public Isolate(String isoId, DataMetric<Isolate> metric) {
      super(isoId, new HashSet<ITSRegion>());

      mMetric = metric;
      mLabel = new OntologyLabel();
      mComparisonCache = new HashMap<String, Double>();
   }

   public void addLabel(String labelName) {
      mLabel.addLabel(labelName);
   }

   public boolean hasLabel(String labelName) {
      return mLabel.hasLabel(labelName);
   }

   public ITSRegion getRegion(String regionName) {
      for (ITSRegion region : mData) {
         if (region.getName().equals(regionName)) {
            return region;
         }
      }

      return null;
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
