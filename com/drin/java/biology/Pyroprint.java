package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.List;
import java.util.ArrayList;

/**
 * A Pyroprint is the result of pyrosequencing each replicate of a specified
 * ITS Region for a given genome. The genome is PCR'd for amplification, then
 * run through PyroMark Q24 Pyrosequencers.
 *
 * A pyroprint is identified by a pyroId, and includes a wellId for
 * convenience. Pyroprints also consist of a dispensation sequence which
 * associates a list of light emittance peak heights to the DNA sequence being
 * analyzed.
 */
public class Pyroprint extends Clusterable<Float> {
   private DataMetric<Pyroprint> mMetric;
   private String mDisp;
   private byte mPyroLen;

   public Pyroprint(String pyroId, byte pyroLen, String disp, DataMetric<Pyroprint> metric) {
      super(pyroId, new ArrayList<Float>(pyroLen));
      mDisp = disp;
      mPyroLen = pyroLen;
      mMetric = metric;
   }

   public byte getPyroLen() { return mPyroLen; }

   /**
    * Get the dispensation sequence used to construct this pyroprint.
    *
    * @return String The dispensation sequence of this pyroprint.
    */
   public String getDispSeq() { return mDisp; }

   /**
    * Check to see if this Pyroprint has the same protocol parameters as the
    * pyroprint being compared to.
    *
    * @param other_pyro The other pyroprint whose protocol parameters should be
    * compared to this pyroprint's protocol parameters.
    * @return boolean A boolean value representing whether this pyroprint's
    * protocol parameters match the other pyroprint's protocol parameters.
    */
   public boolean hasSameProtocol(Pyroprint other) {
      return mPyroLen == other.mPyroLen && mDisp.equals(other.mDisp);
   }

   public boolean addDispensation(byte position, float pHeight) {
      if (position == mData.size() && position < mPyroLen) {
         mData.add(new Float(pHeight));
         return true;
      }

      return false;
   }

   /*
    * Overridden Object Methods
    */
   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Pyroprint) {
         return mName.equals(((Pyroprint) obj).mName);
      }

      return false;
   }

   @Override
   public float compareTo(Clusterable<?> otherObj) {
      if (otherObj instanceof Pyroprint) {
         mMetric.apply(this, (Pyroprint) otherObj);

         float value = mMetric.result();

         Logger.error(mMetric.getError(), String.format(
            "PyroprintComparator:\n\tCorrelation between '%s' and '%s': %.04f",
            mName, otherObj.getName(), value
         ));

         return value;
      }

      return -2;
   }

   @Override
   public Pyroprint deepCopy() {
      Pyroprint newPyro = new Pyroprint(mName, mPyroLen, mDisp, mMetric);

      for (Float peak : mData) { newPyro.getData().add(new Float(peak.floatValue())); }

      return newPyro;
   }

   @Override
   public String toString() {
      String peaks = "";

      for (Float peak : mData) { peaks += ", " + peak.floatValue(); }

      return String.format("%s: %s\n\t%s", this.getName(), mDisp,
                           peaks.substring(2));
   }

}
