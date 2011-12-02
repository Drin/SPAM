package spam.dataTypes;

/*
 * This class represents the correlation value between two isolates. This gets
 * its own class to best abstract the regions and correlation-centric
 * organization.
 */
public class IsolateCorrelation {
   private Isolate mIsolateOne, mIsolateTwo;
   private Double mCorrelation_16_23, mCorrelation_23_5;
   private double mUpperThreshold_16_23 = 99.7, mLowerThreshold_16_23 = 95;
   private double mUpperThreshold_23_5 = 99.7, mLowerThreshold_23_5 = 95;

   private String mAggregationMethod = "average";

   public IsolateCorrelation(Isolate isolateA, Isolate isolateB) {
      super();

      mIsolateOne = isolateA;
      mIsolateTwo = isolateB;

      mCorrelation_16_23 = null;
      mCorrelation_23_5 = null;
   }

   public Double compareTo(IsolateCorrelation otherCorrelation) {
      Double myCorrelationVal = getCorrelation();
      Double otherCorrelationVal = otherCorrelation.getCorrelation();

      if (myCorrelationVal != null && otherCorrelationVal != null)
         return myCorrelationVal - otherCorrelationVal;

      else
         return null;
   }

   public void setThresholds_16_23(double lower, double upper) {
      mUpperThreshold_16_23 = upper;
      mLowerThreshold_16_23 = lower;
   }

   public void setThresholds_23_5(double lower, double upper) {
      mUpperThreshold_23_5 = upper;
      mLowerThreshold_23_5 = lower;
   }

   public void set16_23(double correlation) {
      mCorrelation_16_23 = correlation;
   }

   public void set23_5(double correlation) {
      mCorrelation_23_5 = correlation;
   }

   public Double get16_23() {
      return mCorrelation_16_23;
   }

   public Double get23_5() {
      return mCorrelation_23_5;
   }

   public boolean has16_23() {
      return mCorrelation_16_23 != null;
   }

   public boolean has23_5() {
      return mCorrelation_23_5 != null;
   }

   public boolean isCompleteCorrelation(int numRegions) {
      if (numRegions == 1)
         return has16_23() || has23_5();
      else if (numRegions == 2)
         return has16_23() && has23_5();
      else {
         System.err.println("invalid number of regions!");
         System.exit(1);
      }
      return false;
   }

   public boolean debugSimilar() {
      System.out.println(mIsolateOne + " and " + mIsolateTwo +
       "correlation: " + mCorrelation_16_23 + "(16-23) correlation: " + mCorrelation_23_5 + "(23-5)");

      System.out.println("16-23 Threshold: " + mUpperThreshold_16_23 +
       "23-5 Threshold: " + mUpperThreshold_23_5);

      try {
         if (mCorrelation_16_23 <= mUpperThreshold_16_23 || mCorrelation_23_5 <= mUpperThreshold_23_5) {
            return false;
         }
      }
      catch (NullPointerException nullErr) {
         return false;
      }

      return true;
   }

   public boolean isSimilar() {
      try {
         if (mCorrelation_16_23 <= mUpperThreshold_16_23 || mCorrelation_23_5 <= mUpperThreshold_23_5) {
            return false;
         }
      }
      catch (NullPointerException nullErr) {
         return false;
      }

      return true;
   }

   public boolean isDifferent() {
      try {
         if (mCorrelation_16_23 <= mLowerThreshold_16_23 || mCorrelation_23_5 <= mLowerThreshold_23_5) {
            return true;
         }
      }
      catch (NullPointerException nullErr) {
         return true;
      }

      return false;
   }

   public Double getCorrelation() {
      if (mCorrelation_16_23 != null && mCorrelation_23_5 != null) {
         if (mCorrelation_16_23 < mLowerThreshold_16_23 || mCorrelation_23_5 < mLowerThreshold_23_5) {
            return 0.0;
         }
         else if (mCorrelation_16_23 >= mUpperThreshold_16_23 && mCorrelation_23_5 >= mUpperThreshold_23_5) {
            return 100.0;
         }

         else {
            if (mAggregationMethod.equals("minimum")) {
               return Math.min(mCorrelation_16_23, mCorrelation_23_5);
            }
            else if (mAggregationMethod.equals("average")) {
               //System.out.println("average?");
               return (mCorrelation_16_23 + mCorrelation_23_5) / 2.0;
            }
         }
      }

      else if (mCorrelation_16_23 == null && mCorrelation_23_5 == null)
         return null;

      else {
         if (mCorrelation_16_23 != null) {
            return mCorrelation_16_23 >= mUpperThreshold_16_23 ? 100 :
                   mCorrelation_16_23 < mLowerThreshold_16_23 ? 0 : mCorrelation_16_23;
         }
         else if (mCorrelation_23_5 != null) {
            return mCorrelation_23_5 >= mUpperThreshold_23_5 ? 100 :
                   mCorrelation_23_5 < mLowerThreshold_23_5 ? 0 : mCorrelation_23_5;
         }
      }
      return mCorrelation_16_23 != null ? mCorrelation_16_23 : mCorrelation_23_5;
   }

   public Isolate getIsolateOne() {
      return mIsolateOne;
   }

   public Isolate getIsolateTwo() {
      return mIsolateTwo;
   }

   public int hashCode() {
      return mIsolateOne.hashCode() + mIsolateTwo.hashCode();
   }

   public boolean equals(Object otherCorrelation) {
      if (otherCorrelation instanceof IsolateCorrelation) {
         IsolateCorrelation tmpCorr = (IsolateCorrelation) otherCorrelation;

         if (mIsolateOne.equals(tmpCorr.getIsolateOne())) {
            return mIsolateTwo.equals(tmpCorr.getIsolateTwo());
         }
         else if (mIsolateOne.equals(tmpCorr.getIsolateTwo())) {
            return mIsolateTwo.equals(tmpCorr.getIsolateOne());
         }
      }

      return false;
   }
}
