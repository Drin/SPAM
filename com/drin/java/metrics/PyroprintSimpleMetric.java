package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;

public class PyroprintSimpleMetric extends PyroprintMetric {
   protected Map<String, Map<String, Double>> mCorrelationMap;

   public PyroprintSimpleMetric(Map<String, Map<String, Double>> corrMap) {
      super();

      mCorrelationMap = corrMap;
   }

   @Override
   public void apply(Pyroprint elem_A, Pyroprint elem_B) {
      if (mCorrelationMap.containsKey(elem_A.getName())) {
         Map<String, Double> tmp_map = mCorrelationMap.get(elem_A.getName());

         if (tmp_map.containsKey(elem_B.getName())) {
            mResult = tmp_map.get(elem_B.getName()).doubleValue();
         }
         else { setError(-1); }
      }

      else if (mCorrelationMap.containsKey(elem_B.getName())) {
         Map<String, Double> tmp_map = mCorrelationMap.get(elem_B.getName());

         if (tmp_map.containsKey(elem_A.getName())) {
            mResult = tmp_map.get(elem_A.getName()).doubleValue();
         }
         else { setError(-1); }
      }

      Logger.error(getError(),
                   String.format("Could not find correlation value between " +
                                 "%s and %s\n", elem_A.getName(),
                                 elem_B.getName()));
   }

   @Override
   public double result() {
      double result = mResult;

      this.reset();
      return result;
   }
}
