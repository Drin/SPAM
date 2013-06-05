package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.HashSet;

public class ITSRegion extends Clusterable<Pyroprint> {
   private DataMetric<ITSRegion> mMetric;
   private float mAlpha, mBeta;

   public ITSRegion(String regName, DataMetric<ITSRegion> metric) {
      this(regName, (byte) 2, metric);
   }

   public ITSRegion(String regName, byte numPyros, DataMetric<ITSRegion> metric) {
      super(regName, new HashSet<Pyroprint>(numPyros));

      mAlpha = 0.995f;
      mBeta = 0.99f;

      mMetric = metric;
   }

   @Override
   public float compareTo(Clusterable<?> otherObj) {
      if (otherObj instanceof ITSRegion) {
         mMetric.apply(this, (ITSRegion) otherObj);
         float comparison = mMetric.result();

         Logger.debug(String.format(
            "Comparison between regions '%s' and '%s': %.04f\n",
            this.getName(), ((ITSRegion)otherObj).getName(), comparison
         ));

         return comparison;
      }

      return -2;
   }

   @Override
   public ITSRegion deepCopy() {
      ITSRegion newRegion = new ITSRegion(mName, (byte) mData.size(), mMetric);

      for (Pyroprint pyro : mData) {
         newRegion.getData().add(pyro.deepCopy());
      }

      return newRegion;
   }

   @Override
   public String toString() {
      String str = String.format("\t%s:\n", mName);

      for (Pyroprint pyro : mData) { str += String.format("\t\t%s\n", pyro); }

      return str;
   }
}
