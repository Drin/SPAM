package spam.outputHandlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
/**
 * Class used to create the Histogram from the array of strings of the DNA and the 
 * given order the DNA is to be examined in. The algorithm uses an array of pointers
 * that point to the current char in each string that are advanced each time the
 * Pyrogram algorithm is incremented. There is also a second createHistogram method
 * that outputs information that is scaled, that is, if there are 7 sequences and 
 * one iteration of the algorithm counts 14 A's the ouput is 14/7 = 2.0.
 * @author nate
 *
 */
public class PolyHist {
   ArrayList<String> sequences;
   PrintWriter outputStream = null;
   String csvOrd, seqOrder;
   String crlf;
   String total;
   private int seqIters = -1;
   int [] histArr;
   double [] histDoubleArr;
   
   /**
    * Constructor of the PolyHist object.
    * @param array The array of strings used for the Pyrogram algorithm.
    * @param order The order of the DNA that will be used to examine the sequences of DNA.
    * @param len Used to record the maximum length (cycles) of the Histogram. Currently unused.
    */
   public PolyHist(ArrayList<String> array, String seqOrd){
      sequences = array;
      //will read from a setting that may be set via menu
      seqOrder = seqOrd;
      histArr = new int[seqOrder.length()];
      histDoubleArr = new double[4];
      crlf = System.getProperty( "line.separator" );
   }
   
   /**
    * Algorithm to create the Histogram.
    * @return An int with the number of cycles of the Histogram
    * @throws IOException
    */
   public String createHistogram(String outFile) throws IOException{
      total = "";
      //for iterating over each dna sequence in sequences
      ArrayList<StringCharacterIterator> seqItrs =
       new ArrayList<StringCharacterIterator>();
      for (String dna: sequences) {
         seqItrs.add(new StringCharacterIterator(dna));
      }
      
      String seqOrderOutput = "";
      for (char nucleotide : seqOrder.toCharArray()) {
         seqOrderOutput += nucleotide + ",";
      }
      seqOrderOutput = seqOrderOutput.substring(0, seqOrderOutput.length() - 1) + crlf;
      
      if (outFile != null) {
         File outputFile = new File(outFile);
         if (outputFile.exists()) {
            outputFile.delete();
         }
         outputFile.createNewFile();
         outputFile.deleteOnExit();
         outputStream = 
             new PrintWriter(new FileWriter(outputFile));
         outputStream.write(seqOrderOutput);
      }
      else {
         total += seqOrderOutput;
      }
      
      /**
       * The algorithm that uses pointers to a specific char in each string and advances
       * those pointers each iteration of the pyrogram algorithm.
       * 
       * Edit: Uses an array of string indices to allow advancing
       * through each string by any number of characters
       */
      //int curNucleotide = 0;
      int seqsDone[] = new int[sequences.size()];
      while (!Done(seqsDone) && seqIters > 0) {
         int curNucleotide = 0;
         for (char nucleotide: seqOrder.toCharArray()) {
            for (int itrNum = 0; itrNum < seqItrs.size(); itrNum++) {
               StringCharacterIterator seqItr = seqItrs.get(itrNum);

               while (seqItr.getIndex() < seqItr.getEndIndex() - 1) {
                  if (seqItr.current() == nucleotide) {
                     histArr[curNucleotide]++;
                     seqItr.next();
                  }
                  else if (seqItr.current() == '-') {
                     //blank nucleotide
                     seqItr.next();
                  }
                   
                  else if (seqItr.current() != 'A' &&
                   seqItr.current() != 'T' &&
                   seqItr.current() != 'C' &&
                   seqItr.current() != 'G') {
                     //if the current dna nucleotide is not a valid nucleotide
                     seqItr.next();
                  }
                  
                  else {
                     //For when you find a non match, break and go to the next dna strand
                     break;
                  }

               }
               if (seqItr.getIndex() == seqItr.getEndIndex() - 1) {
                  seqsDone[itrNum] = 1;
               }
            }
            total += histArr[curNucleotide] + ", ";
            curNucleotide = (curNucleotide + 1) % seqOrder.length();
            if (--seqIters < 1) break;
         }
         /*
         for (int ndx = 0; ndx < seqOrder.length(); ndx++) {
            //total += histArr[ndx] + ", ";
         }*/
         total = total.substring(0, total.lastIndexOf(", ")) + crlf;
         for (int ndx = 0; ndx < histArr.length; ndx++) {
            histArr[ndx] = 0;
         }
      }
      if (outFile != null) {
         outputStream.write(total);
         outputStream.flush();
         
         if (outputStream != null) {
               outputStream.close();
           }
      }

      return total;
      //return createHistogram2(scaleFile);      
   }
   /**
    * A copy of the Histogram algorithm that outputs the information scaled by the number 
    * of sequences.
    * @return int with the number of cycles. Currently unused.
    * @throws IOException
    */
   @SuppressWarnings("unused")
   private int createHistogram2(String scaleFile) throws IOException {
      total = "";
      for(int i = 0; i<4; i++){
         histDoubleArr[i] = 0.0;
      }
      
      File outputScaledFile = new File(scaleFile);
      if (outputScaledFile.exists())
         outputScaledFile.delete();
      outputScaledFile.createNewFile();
      //outputScaledFile.deleteOnExit();
      outputStream = 
          new PrintWriter(new FileWriter(outputScaledFile, false));
      outputStream.write(seqOrder + crlf);
      outputStream.write(total);
      
      if (outputStream != null) {
            outputStream.close();
        }
      return sequences.size();
      //return leng;
   }
   
   /**
    * Check to see if all DNA sequences in the array list of DNA sequences have been read
    * @param seqsDone An array of ints representing finished on unfinished sequences.
    * @return A boolean that will give true if all DNA sequences are exhausted and false otherwise.
    */
   public boolean Done(int seqsDone[]){
      int ndx;
      for (ndx = 0; ndx < seqsDone.length && seqsDone[ndx] == 1; ndx++)
         ;
      return ndx == seqsDone.length;
   }
   
   public void setSeqIters(int iters) {
      if (iters > 0) {
         seqIters = iters;
      }
      else {
         System.err.println("cannot set the number of iterations below 1");
      }
   }
}
