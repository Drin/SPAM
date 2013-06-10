package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.Pyroprint;

import com.drin.java.util.Logger;

import java.util.HashSet;

public class ITSRegion extends Clusterable<Pyroprint> {
   private float mAlpha, mBeta;

   public ITSRegion(String regName) {
      this(regName, (byte) 2);
   }

   public ITSRegion(String regName, byte numPyros) {
      super(regName, new HashSet<Pyroprint>(numPyros));

      mAlpha = 0.995f;
      mBeta = 0.99f;
   }

   @Override
   public float compareTo(Clusterable<?> otherObj) {
      float comparison = 0.0f;
      int count = 0;

      if (otherObj instanceof ITSRegion) {
         for (Pyroprint pyroA : mData) {
            for (Pyroprint pyroB : ((ITSRegion) otherObj).getData()) {
               comparison += pyroA.compareTo(pyroB);
               count++;
            }
         }

         if (count > 0) { return comparison / count; }
         else {
            System.err.println("Error in ITSRegion comparison");
            System.exit(0);
         }
      }

      return -2;
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
