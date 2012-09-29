package com.drin.java.biology;

import com.drin.java.clustering.BaseClusterable;

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
public class Pyroprint extends BaseClusterable {
   private List<Double> mPeaks;
   private String mDisp;

   public Pyroprint(int pyroId, String wellId, String dispSeq, List<Double> data) {
      super(String.format("%d (%s)", pyroId, wellId));
      mDisp = dispSeq;
      mPeaks = data;
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

   /**
    * Check to see if this Pyroprint has the same protocol parameters as the
    * pyroprint being compared to.
    *
    * @param other_pyro The other pyroprint whose protocol parameters should be
    * compared to this pyroprint's protocol parameters.
    * @return boolean A boolean value representing whether this pyroprint's
    * protocol parameters match the other pyroprint's protocol parameters.
    */
   public boolean hasSameProtocol(Pyroprint other_pyro) {
      return this.getLength() == other_pyro.getLength() &&
             this.getDispSeq().equals(other_pyro.getDispSeq());
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
   public Double getMaxPeak() {
      double mMaxPeak = -1;

      for (Double peakVal : mPeaks) {
         mMaxPeak = Math.max(mMaxPeak, peakVal.doubleValue());
      }

      return new Double(mMaxPeak);
   }

   /**
    * Find the average peak height for this pyroprint.
    *
    * @return double The average peak height for this pyroprint.
    */
   public Double getMeanPeak() {
      double total = 0;

      for (Double peak : mPeaks) {
         total += peak.doubleValue();
      }

      return new Double(total/mPeaks.size());
   }

   /*
    * Overridden Object Methods
    */
   @Override
   public String toString() {
      String str = mDisp + "\n\t";

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
}
