package spam.dataParser;

public class SequenceParser {
   private String sequence;
   
   public SequenceParser(String seq) {
      sequence = seq;
   }
   
   //returns null on error
   public String getExpandedSequence() {
      System.out.println("getting expanded form of " + sequence);
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
      for (char c : sequence.toLowerCase().toCharArray()) {
         System.out.println("expandedSeq: " + expandedSeq);
         //expand repeatSeq and add to expandedSeq, reset variables
         if (c == ')') {
            if (permRegion) {
               permRegion = false;
               if (repeat) repeatSeq += c;
               else expandedSeq += c;
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
         else if (c == '(') {
            if(!repeat && repeatNum != 0) {
               repeat = true;
               continue;
            }
            else {
               permRegion = true;
               if (repeat) repeatSeq += c;
               else expandedSeq += c;
            }
         }
         //adjust repeatNum
         else if ((c >= '0' && c <= '9')) {
            repeatNum = (10 * repeatNum) +  (c - '0');
         }
         
         //add to repeatSeq or expandedSeq depending on
         //if you are in a parenthetic region
         else if (c == 'a' || c == 't' || c == 'c' || c == 'g') {
            if (repeat) repeatSeq += c;
            else expandedSeq += c;
         }
         else if (c == ' ') continue;
         
         //not a valid nucleotide, not a '(' or ')' and not a number.. invalid.
         else {
            System.out.println("Error while parsing sequence." +
               "Invalid character '" + c + "'.");
            return null;
         }
      }
      //System.out.println("expanded Seq: " + expandedSeq);
      //if the parenthesis was never closed then invalid sequence
      return repeat ? null : expandedSeq.toUpperCase();
   }
   
   //this would actually be really complicated
   public String getCollapsedSequence() {
      return "";
   }
}
