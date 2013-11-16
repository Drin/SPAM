package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.Pyroprint;

import com.drin.java.util.Logger;
import com.drin.java.util.Configuration;

import java.util.HashSet;

public class ITSRegion extends Clusterable<Pyroprint> {
   private static final String PARAMETER_SECTION = "parameters",
                               TRANSFORM_ATTR    = "apply transform";

   private float mAlpha, mBeta;
   private boolean mTransform;

   public ITSRegion(String regName) {
      this(regName, (byte) 2);
   }

   public ITSRegion(String regName, byte numPyros) {
      super(regName, new HashSet<Pyroprint>(numPyros));

      mAlpha = 0.995f;
      mBeta = 0.99f;

      mTransform = Configuration.getBoolean(PARAMETER_SECTION, TRANSFORM_ATTR);
   }

   @Override
   public float compareTo(Clusterable<?> otherObj) {
      float comparison = 0.0f, correlation = 0.0f;
      int count = 0;

      if (otherObj instanceof ITSRegion) {
         for (Pyroprint pyroA : mData) {
            for (Pyroprint pyroB : ((ITSRegion) otherObj).getData()) {
               correlation = pyroA.compareTo(pyroB);

               if (!mTransform) { comparison += correlation; }
               else if (correlation >= mAlpha) { comparison += 1.0f; }
               else if (correlation >= mBeta) { comparison += correlation; }

               count++;
            }
         }

         if (count <= 0) { 
            System.err.println("Error in ITSRegion comparison");
            System.exit(0);
         }
         else { comparison /= count; }

         if (mTransform) {
            if (comparison >= mAlpha) { comparison = 1.0f; }
            else if (comparison < mBeta) { comparison = 0.0f; }
         }

         return comparison;
      }

      return -2.0f;
   }

   @Override
   public ITSRegion deepCopy() {
      ITSRegion newRegion = new ITSRegion(mName, (byte) mData.size());

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
