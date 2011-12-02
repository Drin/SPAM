package spam.dataTypes;

public class PyroSequence {
   private int length = 0;
   private ArrayList<String> sequence = null;

   public PyroSequence(int newLength) {
      length = newLength;
      sequence = new ArrayList<String(newLength);
   }

   public PyroSequence(String sequenceStr) {
      sequence = sequenceStr.toCharArray();
      length = sequenceStr.length();
   }

   public PyroSequence(ArrayList<String> seqArr) {
      sequence = seqArr;
      length = seqArr.length;
   }

   public String getSequence() {
      return String.valueOf(sequence);
   }

   public ArrayList<String> getSequence() {
      return sequence;
   }

   public int getLength() {
      return length;
   }
}
