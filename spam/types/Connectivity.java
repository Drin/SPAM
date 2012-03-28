package spam.types;

/*
 * This enum represents the different connectivity strengths isolates can have
 *    strong - isolates are tightly connected 
 *    weak - isolates are squishily connected
 */
public enum Connectivity {
   STRONG('S'), WEAK('W'), NONE('-');

   private char mCode;

   private Connectivity(char code) {
      mCode = code;
   }
}
