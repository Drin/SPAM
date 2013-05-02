package com.drin.java.biology;

import com.drin.java.ontology.Labelable;
import com.drin.java.ontology.OntologyLabel;

import com.drin.java.clustering.Clusterable;
import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.ArrayList;
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
public class Pyroprint extends Clusterable<Double> implements Labelable {
   private String mDisp;
   private DataMetric<Pyroprint> mMetric;

   protected OntologyLabel mLabel;

   public Pyroprint(int pyroId, String wellId, DataMetric<Pyroprint> metric) {
      this(String.format("%d (%s)", pyroId, wellId), metric);
   }

   public Pyroprint(String isoId, DataMetric<Pyroprint> metric) {
      super(isoId, new ArrayList<Double>());

      mDisp = "";
      mMetric = metric;
      mLabel = new OntologyLabel();
   }

   public Map<String, String> getLabels() { return mLabel.getLabels(); }

   public void addLabel(String labelName, String labelValue) {
      mLabel.addLabel(labelName, labelValue);
   }

   public boolean hasLabel(String labelName) {
      return mLabel.hasLabel(labelName);
   }

   public String getLabelValue(String labelName) {
      return mLabel.getLabelValue(labelName);
   }

   public void addDispensation(String nucleotide, double peakHeight) {
      mDisp += nucleotide;
      mData.add(new Double(peakHeight));
   }

   public int getDispLen() { return mDisp.length(); }

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
   public boolean hasSameProtocol(Pyroprint other_pyro) {
      return this.size() == other_pyro.size() &&
             this.getDispSeq().equals(other_pyro.getDispSeq());
   }

   /*
    * Overridden Object Methods
    */
   @Override
   public String toString() {
      String str = this.getName() + ": " + mDisp + "\n\t";

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
