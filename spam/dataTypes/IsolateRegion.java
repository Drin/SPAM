package spam.dataTypes;
/*
 * This enum represents the different methods for sampling E.Coli
 * in Emily's data.
 * fecal represents a sampling during the event
 * immediate represents a sampling immediately following the event
 * later represents a sampling some hours after the event
 */
public enum IsolateRegion {
   /*
    * fecal value = 0
    * immediate value = 1
    * later value = 3
    * deep value = 7;
    * These values are used to represent temporal proximity
    * between the different sampling methods.
    * fecal and immediate are closest to each other
    * while later is closest only to immediate
    */
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
