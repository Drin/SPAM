package spam.dataParser;

public class SequenceParser {
   private String sequence;
   private boolean debug = false;
   
   public SequenceParser(String seq) {
      sequence = seq;
   }
   
   //returns null on error
   public String getExpandedSequence() {
      if (debug) { System.out.println("getting expanded form of " + sequence); }
      //repeat is true if in a parenthetic area e.g. 23(ATG..) false if not e.g. ATGC..
      //permRegion is true if (ATCG) has been found, representing permutations of ATCG,
      //false if permutations should not be inserted
      boolean repeat = false, permRegion = false;
      //expandedSeq is final string, repeatSeq is temporary string to be repeated
      //repeatNum times e.g. 23(ATG..): 23 is repeatNum and "ATG.." is repeatSeq
      String expandedSeq = "", repeatSeq = "";
      int repeatNum = 0;
      
      //for using (ATCG) inside of a repeating region i.e. 23((ATCG))
      //by leaving the (ATCG) intact, we can get 23 repetitions and
      //SequenceFinder will handle the correct permutation replacement
      for (char seqChar : sequence.toLowerCase().toCharArray()) {
         if (debug) { System.out.println("expandedSeq: " + expandedSeq); }

         //expand repeatSeq and add to expandedSeq, reset variables
         if (seqChar == ')') {
            if (permRegion) {
               permRegion = false;
               if (repeat) repeatSeq += seqChar;
               else expandedSeq += seqChar;
               continue;
            }
            if (repeat) {
               for (int nthRepeat = 0; nthRepeat < repeatNum; nthRepeat++) {
                  expandedSeq += repeatSeq;
               }
               repeatNum = 0;
               repeatSeq = "";
               repeat = false;
            }
         }
         //begin using repeatSeq
         else if (seqChar == '(') {
            if(!repeat && repeatNum != 0) {
               repeat = true;
               continue;
            }
            else {
               permRegion = true;
               if (repeat) repeatSeq += seqChar;
               else expandedSeq += seqChar;
            }
         }
         //adjust repeatNum
         else if ((seqChar >= '0' && seqChar <= '9')) {
            repeatNum = (10 * repeatNum) +  (seqChar - '0');
         }
         
         //add to repeatSeq or expandedSeq depending on
         //if you are in a parenthetiseqChar region
         else if (seqChar == 'a' || seqChar == 't' || seqChar == 'c' || seqChar == 'g') {
            if (repeat) repeatSeq += seqChar;
            else expandedSeq += seqChar;
         }

         else if (seqChar == ' ') continue;
         
         //not a valid nucleotide, not a '(' or ')' and not a number.. invalid.
         else {
            System.err.println("Error while parsing sequence." +
               "Invalid character '" + seqChar + "'.");
            return null;
         }

      }

      return repeat ? null : expandedSeq.toUpperCase();
   }
   
   //this would actually be really complicated
   public String getCollapsedSequence() {
      return "";
   }
}
