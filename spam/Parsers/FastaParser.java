package spam.dataParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class that is used to parse out the DNA input file and
 * put each sequence in a seperate string
 * in the array list that is passed in.
 * It supports differing lengths of sequences, that is one
 * sequence could be of length 10 and another could be of length 20.
 * @author amontana
 *
 */
public class FastaParser {
   /**
    * Method that grabs each sequence of DNA and copies it over into each string
    * in the array list.
    *
    * @return an array list of strings, each string contains one sequence of DNA
    */
   public static List<String> parse(File fastaFile) {
      List<String> dnaStrings = new ArrayList<String>();
      Scanner scanner = null;
      
      try {
         scanner = new Scanner(new BufferedReader(new FileReader(seqFile)));
      }
      catch (java.io.FileNotFoundException e) {
         System.err.println("Could not find file " + fastaFile +
          ", verify that file exists.");
      }

      String dnaString = "";

      while (fileData.hasNextLine()) {
         String currentLine = fileData.nextLine();

         if (currentLine.equals("")) continue;
         
         else if (currentLine.startsWith(">")) {
            //This will add a "dummy" sequence when first > is seen
            dnaStrings.add(dnaString);
            dnaString = "";
         }
         
         //if currentLine is not a description then it is dna
         else dnaString += currentLine;
      }
      
      //remove dummy sequence
      dnaStrings.remove(0);

      //add last sequence
      dnaStrings.add(dnaString);

      return dnaStrings;
   }
}
