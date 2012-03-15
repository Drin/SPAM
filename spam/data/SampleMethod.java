package spam.data;
/*
 * This enum represents the different methods for sampling E.Coli
 * in Emily's data.
 * fecal represents a sampling during the event
 * immediate represents a sampling immediately following the event
 * later represents a sampling some hours after the event
 */
public enum SampleMethod {
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
   FECAL('f', 0), IMM('i', 1), LATER('l', 3), DEEP('d', 7), BEFORE('b', -2);

   private char mEncoding;
   private int mValue;

   private SampleMethod(char encoding, int val) {
      mEncoding = encoding;
      mValue = val;
   }

   public boolean equals(char otherChar) {
      return mEncoding == otherChar;
   }

   public boolean equals(SampleMethod otherMethod) {
      return mEncoding ==  otherMethod.mEncoding;
   }

   public int dist(SampleMethod otherMethod) {
      return Math.abs(mValue - otherMethod.mValue);
   }

   public String toString() {
      switch (mEncoding) {
         case 'f':
            return "Fecal";
         case 'i':
            return "Immediate";
         case 'l':
            return "Later";
         case 'd':
            return "Deep";
         case 'b':
            return "Before";
         default:
            return "Unknown";
      }
   }

   public static SampleMethod getMethod(char encoding) {
      switch (encoding) {
         case 'f':
            return FECAL;
         case 'i':
            return IMM;
         case 'l':
            return LATER;
         case 'd':
            return DEEP;
         case 'b':
            return BEFORE;
         default:
            System.err.println("Invalid Isolate encoding: " + encoding);
            return null;
      }
   }
}
