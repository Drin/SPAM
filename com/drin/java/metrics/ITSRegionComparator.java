package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.metrics.ITSRegionMetric;

import com.drin.java.util.Logger;

public class ITSRegionComparator implements DataComparator<ITSRegionMetric,
                                                           ITSRegion> {

   @Override
   public double compare(ITSRegionMetric regionMetric,
                         ITSRegion elem_A, ITSRegion elem_B) {
      regionMetric.apply(elem_A, elem_B);

      double comparison = regionMetric.result();

      Logger.debug(String.format("ITSRegionComparator:\n\tComparison " +
                                 "between '%s' and '%s': %.04f\n",
                                 elem_A.getName(), elem_B.getName(),
                                 comparison));

      return comparison;
   }

   @Override
   public boolean isSimilar(ITSRegionMetric regionMetric,
                            ITSRegion elem_A, ITSRegion elem_B) {
      double comparison = this.compare(regionMetric, elem_A, elem_B);

      return regionMetric.transformResult(comparison) == 1;
   }
}
