package spam.types;

import spam.types.Pyroprint;

import java.util.List;
import java.util.ArrayList;

public class IsolateRegion {
   private String mRegion = null;
   private List<Pyroprint> mPyroprints = null;

   private double mAlpha = -1, mBeta = -1;

   private IsolateRegion(String regionName, double alpha, double beta) {
      mRegion = regionName;

      mPyroprints = new ArrayList<Pyroprint>();

      mAlpha = alpha;
      mBeta = beta;
   }

   /*
    * Getter Methods
    */
   public String getRegionName() {
      return mRegion;
   }

   public double getAlphaThreshold() {
      return mAlpha;
   }

   public double getbetaThreshold() {
      return mbeta;
   }

   public List<Pyroprint> getPyroprints() {
      return mPyroprints;
   }

   /*
    * Utility Methods
    */
   public void addPyroprint(Pyroprint newPyro) {
      mPyroprints.add(newPyro);
   }

   public int getNumPyroprints() {
      return mPyroprints.size();
   }

   /*
    * Overridden Methods
    */
   public boolean equals(Object otherRegion) {
      if (otherRegion instanceof IsolateRegion) {
         IsolateRegion region = (IsolateRegion) otherRegion;

         return mRegion.equals(region.mRegion);
      }

      return false;
   }

   public int hashCode() {
      return mRegion.hashCode();
   }

   public String toString() {
      return mRegion;
   }
}
