package spam.dataTypes;

public class Region {
   private String mName = null;
   private double mBeta = -1, mAlpha = -1;

   private static final double DEFAULT_BETA = .95, DEFAULT_ALPHA = .997;

   public Region(String name, double beta, double alpha) {
      mName = name;
      mBeta = beta;
      mAlpha = alpha;
   }

   public Region(String name) {
      this(name, DEFAULT_BETA, DEFAULT_ALPHA);
   }

   public String getName() {
      return mName;
   }

   public void setName(String name) {
      mName = name;
   }

   public double getBeta() {
      return mBeta;
   }

   public void setBeta(double beta) {
      mBeta = beta;
   }

   public double getAlpha() {
      return mAlpha;
   }

   public void setAlpha(double alpha) {
      mAlpha = alpha;
   }

   public int hashCode() {
      return mName.hashCode();
   }

   public boolean equals(Object otherRegion) {
      if (otherRegion instanceof Region) {
         return this.mName.equals(((Region) otherRegion).mName);
      }
      
      return false;
   }
}
