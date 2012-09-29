package com.drin.java.metrics;

import com.drin.java.metrics.DataComparator;
import com.drin.java.metrics.TestMetric;

public class TestComparator implements DataComparator<TestMetric, Double> {
   private static TestComparator mDriver = new TestComparator();

   public Double compare(TestMetric metric, Double dbl_A, Double dbl_B) {
      for (double val = dbl_A.doubleValue(); val < dbl_B.doubleValue(); val++) {
         metric.apply(new Double(val), new Double(1));
      }

      Double result = metric.result();
      System.out.printf("the sum from %.02f to %.02f is %.02f\n", dbl_A, dbl_B, result);
      metric.reset();

      return result;
   }

   public boolean isSimilar(TestMetric metric, Double dbl_A, Double dbl_B) {
      throw new UnsupportedOperationException();
   }

   public static void main(String[] args) {
      Double dbl_A = new Double(5);
      Double dbl_B = new Double(10);
      Double result = mDriver.compare(new TestMetric(), dbl_A, dbl_B);

      System.out.printf("result: %.02f\n", result);
   }
}
