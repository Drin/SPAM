package com.drin.java.types;

import com.drin.java.types.DataObject;

import java.util.List;

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
public class Pyroprint extends DataObject {
   private List<Double> mPeaks;
   private String mDisp;

   public Pyroprint(int pyroId, String wellId, String dispSeq, List<Double> data) {
      super(String.format("%d (%s)", pyroId, wellId));
      mDisp = dispSeq;
      mPeaks = data;
   }

   /*
    * Overridden Object Methods
    */
   @Override
   public String toString() {
      String str = mDisp + "\n";

      for (Double peak : mPeaks) {
         str += peak.doubleValue() + ", ";
      }

      return str;
   }

   @Override
   public boolean equals(Object otherPyroprint) {
      if (otherPyroprint instanceof Pyroprint) {
         Pyroprint otherPyro = (Pyroprint) otherPyroprint;

         if (mName.equals(otherPyro.mName) &&
          mPeaks.size() == otherPyro.mPeaks.size()) {
            for (int peakNdx = 0; peakNdx < mPeaks.size(); peakNdx++) {
               Double peakOne = mPeaks.get(peakNdx);
               Double peakTwo = otherPyro.mPeaks.get(peakNdx);

               if (peakOne.compareTo(peakTwo) != 0) {
                  return false;
               }
            }

            return true;
         }
      }

      return false;
   }

   /*
    * Getter Methods
    */

   /**
    * Get the length of the pyroprint as number of dispensations.
    *
    * @return int A value representing number of dispensations.
    */
   public int getLength() {
      return mPeaks.size();
   }

   /**
    * Get a list of the peak values for all dispensations of this pyroprint.
    *
    * @return List<Double> List of peak heights
    */
   public List<Double> getPeaks() {
      return mPeaks;
   }

   /**
    * Get the dispensation sequence used to construct this pyroprint.
    *
    * @return String The dispensation sequence of this pyroprint.
    */
   public String getDispSeq() {
      return mDisp;
   }

   /*
    * Utility Methods
    */
   /**
    * Find the maximum peak height for the given list of peak heights. This
    * method returns -1 if there are no peak heights for this pyroprint.
    *
    * @return double The value of the peak height for the given list of peak
    * heights.
    */
   public double getMaxPeak() {
      double mMaxPeak = -1;

      for (double peakVal : mPeaks) {
         mMaxPeak = Math.max(mMaxPeak, peakVal);
      }

      return mMaxPeak;
   }

   /**
    * Find the average peak height for this pyroprint.
    *
    * @return double The average peak height for this pyroprint.
    */
   public double getMeanPeak() {
      double total = 0;

      for (Double peak : mPeaks) {
         total += peak.doubleValue();
      }

      return total/mPeaks.size();
   }
}
