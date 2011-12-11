package spam.dataTypes;

import spam.dataTypes.Pyroprint;
import spam.dataTypes.Region;
import spam.dataTypes.RegionAggregation;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/*
 * This class represents the correlation value between two pyroprints. This gets
 * its own class to best abstract the regions and correlation-centric
 * organization.
 */
public class PyroprintCorrelation {
   private Pyroprint mPyroOne = null, mPyroTwo = null;
   private RegionAggregation mRegionAgg = null;

   private Map<Region, Double> mRegionSimMap = null;

   public PyroprintCorrelation(Pyroprint pyroA, Pyroprint pyroB) {
      super();

      mPyroOne = pyroA;
      mPyroTwo = pyroB;

      mRegionSimMap = new HashMap<Region, Double>();
   }

   public PyroprintCorrelation(Pyroprint pyroA, Pyroprint pyroB, Region region, Double corr) {
      this(pyroA, pyroB);

      mRegionSimMap.put(region, corr);
   }

   public PyroprintCorrelation(Pyroprint pyroA, Pyroprint pyroB, RegionAggregation type) {
      this(pyroA, pyroB);

      mRegionAgg = type;
   }

   public void setAggregationType(RegionAggregation type) {
      mRegionAgg = type;
   }

   public RegionAggregation getAggregationType() {
      return mRegionAgg;
   }

   public Double compareTo(PyroprintCorrelation otherCorr) {
      Iterator regionItr = mRegionSimMap.keySet().iterator();
      double totalDiff = 0;

      while (regionItr.hasNext()) {
         Region tmpRegion = (Region) regionItr.next();

         Double compareVal = this.compareTo(otherCorr, tmpRegion);

         if (compareVal != null)
            totalDiff += compareVal;

         else
            return null;
      }

      return totalDiff;
   }

   public Double compareTo(PyroprintCorrelation otherCorr, Region region) {
      Double myCorrVal = this.getCorr(region);
      Double otherCorrVal = otherCorr.getCorr(region);

      if (myCorrVal != null && otherCorrVal != null)
         return myCorrVal - otherCorrVal;

      else
         return null;
   }

   public Double getCorr(Region region) {
      return mRegionSimMap.get(region);
   }

   public boolean hasCorr(Region region) {
      return mRegionSimMap.containsKey(region);
   }

   public boolean isSimilar() {
      try {
         for (Region region : mRegionSimMap.keySet()) {
            if (mRegionSimMap.get(region) <= region.getAlpha()) {
               return false;
            }
         }
      }
      catch (NullPointerException nullErr) {
         return false;
      }

      return true;
   }

   public boolean isDifferent() {
      try {
         for (Region region : mRegionSimMap.keySet()) {
            if (mRegionSimMap.get(region) <= region.getBeta()) {
               return true;
            }
         }
      }
      catch (NullPointerException nullErr) {
         return true;
      }

      return false;
   }

   public void addCorr(Region region, Double corr) {
      mRegionSimMap.put(region, corr);
   }

   public Double getCorr() {
      Double corrVal = null;

      for (Region region : mRegionSimMap.keySet()) {
         Double tmpCorrVal = mRegionSimMap.get(region);

         if (tmpCorrVal == null) {
            continue;
         }

         else if (tmpCorrVal == 0 || (corrVal != null && corrVal == 0)) {
            corrVal = 0.0;
            continue;
         }

         switch (mRegionAgg) {
            case MIN:
               corrVal = corrVal == null ? tmpCorrVal : Math.min(corrVal, tmpCorrVal);
               break;
            case AVG:
               corrVal = corrVal == null ? tmpCorrVal : corrVal + tmpCorrVal;
               break;
            default:
               System.err.println("Invalid region aggregation type");
               break;
         }

      }
      
      if (corrVal != null && mRegionAgg == RegionAggregation.AVG) {
         return corrVal / mRegionSimMap.size();
      }

      return corrVal;
   }

   public Pyroprint getPyroOne() {
      return mPyroOne;
   }

   public Pyroprint getPyroTwo() {
      return mPyroTwo;
   }

   public int hashCode() {
      return mPyroOne.hashCode() + mPyroTwo.hashCode();
   }

   public boolean equals(Object otherCorr) {
      if (otherCorr instanceof PyroprintCorrelation) {
         PyroprintCorrelation tmpCorr = (PyroprintCorrelation) otherCorr;

         if (mPyroOne.equals(tmpCorr.getPyroOne())) {
            return mPyroTwo.equals(tmpCorr.getPyroTwo());
         }

         else if (mPyroOne.equals(tmpCorr.getPyroTwo())) {
            return mPyroTwo.equals(tmpCorr.getPyroOne());
         }
      }

      return false;
   }
}
