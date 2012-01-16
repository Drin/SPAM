package spam.dataTypes;

public enum IsolateRegion {
   ITS_16_23("16s-23s"), ITS_23_5("23s-5s");

   private String mRegion;

   private IsolateRegion(String regionName) {
      mRegion = regionName;
   }

   public boolean equals(String otherRegionName) {
      return mRegion.equals(otherRegionName);
   }

   public boolean equals(IsolateRegion otherRegion) {
      return mRegion.equals(otherRegion.mRegion);
   }

   public String toString() {
      return mRegion;
   }

   public static IsolateRegion getRegion(String regionName) {
      if (regionName.equals("16s-23s")) {
         return ITS_16_23;
      }
      else if (regionName.equals("23s-5s")) {
         return ITS_23_5;
      }
      else {
         System.err.printf("Invalid Region Name: '%s'", regionName);
         return null;
      }
   }
}
