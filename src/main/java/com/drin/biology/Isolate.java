package com.drin.biology;

import com.drin.ontology.Labelable;
import com.drin.ontology.OntologyLabel;

import com.drin.clustering.Clusterable;
import com.drin.biology.ITSRegion;
import com.drin.biology.Pyroprint;

import com.drin.metrics.DataMetric;

import com.drin.util.Logger;

import java.util.Iterator;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends Clusterable<ITSRegion> implements Labelable {
   private static final int ALPHA_NDX = 0, BETA_NDX = 1;
   private DataMetric<Isolate> mMetric;
   private Map<String, Double> mComparisonCache;

   protected OntologyLabel mLabel;

   public Isolate(String isoId, DataMetric<Isolate> metric) {
      this(isoId, null, metric);
   }

   public Isolate(String isoId, Set<ITSRegion> regions, DataMetric<Isolate> metric) {
      super(isoId, regions);

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
