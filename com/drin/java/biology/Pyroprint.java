package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

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
public class Pyroprint extends Clusterable<Double> {
   private String mDisp;
   private DataMetric<Pyroprint> mMetric;

   public Pyroprint(int pyroId, String wellId, String dispSeq,
                    List<Double> data, DataMetric<Pyroprint> metric) {
      super(String.format("%d (%s)", pyroId, wellId), data);
      mDisp = dispSeq;
      mMetric = metric;
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
      return this.size() == other_pyro.size() &&
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

      for (Double peakVal : mData) {
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

      for (Double peak : mData) {
         total += peak.doubleValue();
      }

      return new Double(total/mData.size());
   }

   /*
    * Overridden Object Methods
    */
   @Override
   public String toString() {
      String str = mDisp + "\n\t";

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
   public boolean isSimilar(Clusterable<?> otherObj) {
      if (otherObj instanceof Pyroprint) {
         return hasSameProtocol((Pyroprint) otherObj);
      }

      return false;
   }

   @Override
   public double compareTo(Clusterable<?> otherObj) {
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
