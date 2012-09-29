package com.drin.java.test;

import com.drin.java.biology.Pyroprint;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class TestBiologyTypes {

   public static void main(String[] args) {
      List<Double> pyro_peaks1 = new ArrayList<Double>(),
                   pyro_peaks2 = new ArrayList<Double>();

      for (int peak_ndx = 0; peak_ndx < 12; peak_ndx++) {
         pyro_peaks1.add(new Double(peak_ndx));
         pyro_peaks2.add(new Double(12 - peak_ndx));
      }

      Pyroprint p1 = new Pyroprint(1, "A1", "ATCGATCGATCG", pyro_peaks1);
      Pyroprint p2 = new Pyroprint(2, "B1", "ATCGATCGATCG", pyro_peaks2);

      ITSRegion r1 = new ITSRegion("region1");
      ITSRegion r2 = new ITSRegion("region2");

      r1.add(p1);
      r2.add(p1);

      r1.add(p2);
      r2.add(p2);

      Set<ITSRegion> regionSet = new HashSet<ITSRegion>();
      regionSet.add(r1);
      regionSet.add(r2);

      Isolate i1 = new Isolate("isolate1", regionSet);
      Isolate i2 = new Isolate("isolate2", regionSet);

      System.out.printf("%s\n%s\n", i1, i2);
   }
}
