package com.drin.biology;

import com.drin.clustering.Clusterable;

import com.drin.util.Logger;

import java.util.List;
import java.util.Iterator;

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
public class Pyroprint extends Clusterable {
   private String mDispSeq;
   private List<Double> mPeakHeights;

   public Pyroprint(int pyroId, String wellId, String dispSeq, List<Double> peakHeights) {
      super(String.format("%d (%s)", pyroId, wellId));

      mDispSeq = dispSeq;
      mPeakHeights = peakHeights;
   }

   public Pyroprint(String isoId, String dispSeq, List<Double> peakHeights) {
      super(isoId);

      mDispSeq = dispSeq;
      mPeakHeights = peakHeights;
   }

   /**
    * Get the dispensation sequence used to construct this pyroprint.
    *
    * @return String The dispensation sequence of this pyroprint.
    */
   public String getDispSeq() { return mDispSeq; }

   /**
    * Validate the bare minimum of this Pyroprint.
    *
    * @return boolean True if the length of this pyroprint's dispensation
    * sequence is equal to the number of peak heights associated with it, False
    * otherwise.
    */
   public boolean isValidPyroprint() {
      return mDispSeq.length() == mPeakHeights.size();
   }

   /**
    * Check to see if this Pyroprint has the same protocol parameters as the
    * pyroprint being compared to.
    *
    * @param other_pyro The pyroprint whose protocol is to be compared to.
    * @return boolean True if this pyroprint and other_pyro have the same
    * pyroprint protocol, false otherwise.
    */
   public boolean hasSameProtocol(Pyroprint other_pyro) {
      return mPeakHeights.size() == other_pyro.mPeakHeights.size() &&
             mDispSeq.equals(other_pyro.mDispSeq);
   }

   /*
    * Utility Methods
    */
   /**
    * Find the maximum peak height for the given list of peak heights. This
    * method returns null if there are no peak heights for this pyroprint.
    *
    * @return Double The value of the peak height for the given list of peak
    * heights, null if there are no peak heights associated with this
    * pyroprint.
    */
   public Double getMaxPeak() {
      if (mPeakHeights.isEmpty()) { return null; }

      double maxPeak = -1d;

      for (Double peakVal : mPeakHeights) {
         maxPeak = Math.max(maxPeak, peakVal.doubleValue());
      }

      return new Double(maxPeak);
   }

   /*
    * Overridden Object Methods
    */
   @Override
   public String toString() {
      String str = this.getName() + ": " + mDispSeq + "\n\t";

      for (Double peak : mData) {
         str += peak.doubleValue() + ", ";
      }

      return str;
   }

   @Override
   public boolean equals(Object otherPyroprint) {
      if (otherPyroprint instanceof Pyroprint) {
         Pyroprint otherPyro = (Pyroprint) otherPyroprint;

         if (mName.equals(otherPyro.mName) &&
          mData.size() == otherPyro.mData.size()) {
            Iterator<Double> itr_A = mData.iterator();
            Iterator<Double> itr_B = otherPyro.mData.iterator();

            while (itr_A.hasNext() && itr_B.hasNext()) {
               Double peakOne = itr_A.next();
               Double peakTwo = itr_B.next();

               if (peakOne.compareTo(peakTwo) != 0) { return false; }
            }

            return true;
         }
      }

      return false;
   }

   @Override
   public double compareTo(Clusterable otherObj) {
      if (otherObj instanceof Pyroprint) {
         mMetric.apply(this, (Pyroprint) otherObj);

         double value = mMetric.result();

         Logger.error(mMetric.getError(),
                      String.format("PyroprintComparator:\n\tCorrelation " +
                                    "between '%s' and '%s': %.04f",
                                    getName(), ((Pyroprint)otherObj).getName(),
                                    value));

         return value;
      }

      return -2;
   }
}
