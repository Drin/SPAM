package spam.dataParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class that is used to parse out the DNA input file and
 * put each sequence in a seperate string
 * in the array list that is passed in.
 * It supports differing lengths of sequences, that is one
 * sequence could be of length 10 and another could be of length 20.
 * @author nate
 *
 */

public class Polyparse {
   private String dispOrder = null;
   private int startPos = 1;
   private static String newLine = System.getProperty("line.separator");
   private static final String PYRO_EXT = ".pyrorun";
   private boolean debug = false;

   /**
    * Constructor for the Polyparse object
    * @param dispSeq the order the DNA is to be examined in,
    *  currently unnused for this class
    */

   public Polyparse(String dispSeq, int startPos) {
      dispOrder = dispSeq;
      this.startPos = startPos;
   }
   
   /**
    * Method that grabs each sequence of DNA and copies it over into each string
    * in the array list.
    * @return an array list of strings, each string contains one sequence of DNA
    * @throws IOException
    */
   public ArrayList<String> parse(String seqFile) throws IOException {
      Scanner fileData = null;
      ArrayList<String> dnaStrings = new ArrayList<String>();
      
      if (seqFile == null || seqFile.equals("")) {
         System.err.println("No file selected to parse");
         return null;
      }

      try {
         fileData = new Scanner(new BufferedReader(new FileReader(seqFile)));
      }
      catch (FileNotFoundException e) {
         System.err.println("Could not find file " + seqFile +
          ", verify that file exists.");
      }

      //Check for the DNA notation in the file that needs to be dropped
      //from the string
      String newSeq = "";

      while (fileData.hasNextLine()) {
         //analyze file line by line
         if (debug) System.out.println("parsing line in data");
         String currentLine = fileData.nextLine();
         if (debug) System.out.println("currentLine is: " + currentLine);

         //ignore whitespace
         if (currentLine.equals("")) continue;
         
         //currentLine starts with ">" if it is a description and not dna
         else if (currentLine.startsWith(">")) {
            //This will add a "dummy" sequence when first > is seen
            if (debug) System.out.println("adding " + newSeq + " to dnaStrings. ");
            dnaStrings.add(newSeq);
            newSeq = "";
         }
         
         //if currentLine is not a description then it is dna
         else newSeq += currentLine;
      }
      
      if (debug)
         System.out.println("\"dummy\" element in arr: " + dnaStrings.get(0));
      
      //remove dummy sequence
      dnaStrings.remove(0);
      //add last sequence
      dnaStrings.add(newSeq);

      return dnaStrings;
   }
   
   public static String parseSequence(File seqFile) {
      BufferedReader seqReader = null;

      try {
         seqReader = new BufferedReader(new FileReader(seqFile));
      }

      catch(FileNotFoundException f) {
         System.err.println("Error in parseSequence: " + f.getStackTrace());
      }

      String seqText = null, rawSequence = "";

      try {
         while ((seqText = seqReader.readLine()) != null) {
            rawSequence += seqText.replace(newLine, "");
         }
      }
      catch (IOException io) {
         System.err.println("Error in parseSequence: " + io.getStackTrace());
      }

      return rawSequence;
   }
   
   public String getOrder() {
      return dispOrder;
   }

   public void setOrder(String newDispOrder) {
      dispOrder = newDispOrder;
   }
}
