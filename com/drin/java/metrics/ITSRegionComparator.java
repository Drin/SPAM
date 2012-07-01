package com.drin.java.metrics;

import com.drin.java.types.ITSRegion;
import com.drin.java.types.Pyroprint;
import com.drin.java.metrics.ITSRegionMetric;

public class ITSRegionComparator implements DataComparator<ITSRegion, ITSRegionMetric> {

   @Override
   public Double compare(ITSRegionMetric regionMetric, ITSRegion elem_A, ITSRegion elem_B) {
      Double comparison = null;

      for (Pyroprint pyro_A : elem_A.getPyroprints()) {
         for (Pyroprint pyro_B : elem_B.getPyroprints()) {
            regionMetric.apply(pyro_A, pyro_B);
         }
      }

      comparison = regionMetric.result();
      regionMetric.reset();

      return comparison;
   }

   @Override
   public boolean isSimilar(ITSRegionMetric regionMetric, ITSRegion elem_A, ITSRegion elem_B) {
      Double comparison = compare(regionMetric, elem_A, elem_B);

      return comparison != null && comparison.compareTo(regionMetric.getAlphaThreshold()) >= 0;
   }
}
