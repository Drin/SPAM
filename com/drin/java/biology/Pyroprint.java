package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;

import com.drin.java.util.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
   private String mDisp;
   private byte mPyroLen;
   private int mPeakCount;
   private float mPyroASum, mPyroBSum, mProductAB,
                  mPyroASquaredSum, mPyroBSquaredSum;

   public Pyroprint(String pyroId, byte pyroLen, String disp) {
      super(pyroId, new ArrayList<Float>(pyroLen));
      mDisp = disp;
      mPyroLen = pyroLen;
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
      boolean sameDisp = expandDisp(mDisp).substring(0, mPyroLen - 1).equals(
         expandDisp(other.mDisp).substring(0, mPyroLen - 1)
      );

      return mPyroLen == other.mPyroLen && sameDisp;
   }

   public boolean addDispensation(byte position, float pHeight) {
      if (position == mData.size() && position < mPyroLen) {
         mData.add(new Float(pHeight));
         return true;
      }

      return false;
   }

   @Override
   public float compareTo(Clusterable<?> otherObj) {
      float peakA = 0.0f, peakB = 0.0f;

      mPyroASum = mPyroBSum = 0.0f;
      mPyroASquaredSum = mPyroBSquaredSum = mProductAB = 0.0f;
      mPeakCount = 0;

      if (otherObj instanceof Pyroprint && hasSameProtocol((Pyroprint) otherObj)) {
         Iterator<Float> itrA = mData.iterator(),
                         itrB = ((Pyroprint) otherObj).mData.iterator();

         while (itrA.hasNext() && itrB.hasNext()) {
            peakA = itrA.next().floatValue();
            peakB = itrB.next().floatValue();

            mPyroASum += peakA;
            mPyroBSum += peakB;
            
            mPyroASquaredSum += peakA * peakA;
            mPyroBSquaredSum += peakB * peakB;
            
            mProductAB += peakA * peakB;
            mPeakCount++;
         }

         if (mPeakCount > 0) {
            float pearson = 
               ((mPeakCount * mProductAB) - (mPyroASum * mPyroBSum))/ (float) Math.sqrt(
                (((mPeakCount * mPyroASquaredSum) - (mPyroASum * mPyroASum)) *
                 ((mPeakCount * mPyroBSquaredSum) - (mPyroBSum * mPyroBSum)))
            );

            if (pearson > 1) {
               System.err.println("Pearson greater than 1?!");
               System.exit(0);
            }

            return pearson;
         }
      }

      return -2;
   }

   @Override
   public Pyroprint deepCopy() {
      Pyroprint newPyro = new Pyroprint(mName, mPyroLen, mDisp);

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

   private String expandDisp(String disp) {
      String expandedDisp = "";

      Pattern dispCompPat = Pattern.compile("\\d+\\([ATCG]+\\)|[ATCG]+");
      Matcher dispCompMatch = dispCompPat.matcher(disp);

      Pattern repPat = Pattern.compile("(\\d+)\\(([ATCG]+)\\)");
      while (dispCompMatch.find()) {
         Matcher repeatMatch = repPat.matcher(dispCompMatch.group());

         if (repeatMatch.matches()) {
            for (int repNdx = 0; repNdx < Integer.parseInt(repeatMatch.group(1)); repNdx++) {
               expandedDisp += repeatMatch.group(2);
            }
         }
         else { expandedDisp += dispCompMatch.group(); }
      }

      return expandedDisp;
   }
}
