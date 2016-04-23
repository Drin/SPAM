package com.drin.util;

import java.io.File;

import java.util.Scanner;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Class that contains utility parsing methods for various texts or files.
 *
 * @author amontana
 */
public class Parser {

   /**
    * Parse a FASTA formatted file for all DNA Cassettes.
    *
    * @return A Map keyed by a cassette header and containing mappings to DNA
    * cassettes.
    */
   public static Map<String, String> parseFastaFile(File fastaFile) {
      Map<String, String> cassetteMap = new LinkedHashMap<String, String>();
      String cassetteHeader = null;
      String dnaString = "";
      Scanner scanner = null;
      
      try {
         scanner = new Scanner(seqFile);
      }

      catch (java.io.FileNotFoundException fileErr) {
         System.err.printf("Could not find file '%s'", fastaFile);
         return null;
      }

      while (fileData.hasNextLine()) {
         String currentLine = fileData.nextLine();

         if (currentLine.startsWith(">")) {
            if (cassetteHeader != null && !dnaString.equals("")) {
               cassetteMap.put(cassetteHeader, dnaString);
            }

            cassetteHeader = currentLine.substring(1); 
            dnaString = "";
         }
         
         else dnaString += currentLine;
      }
      
      if (cassetteHeader != null && !dnaString.equals("")) {
         cassetteMap.put(cassetteHeader, dnaString);
      }

      return cassetteMap;
   }

   /**
    * Expands a dispensation sequence of the form 3(AT) to ATATAT. This method
    * leaves sequences within ()'s intact if there is no leading number and the
    * state is already in a repeating state.
    *
    * @return Returns the expanded string, or null on error.
    */
   public static String getExpandedSequence(String sequence) {
      String expandedSeq = "", repeatSeq = "";
      boolean repeatRegion = false, permRegion = false;
      int repeatNum = 0;
      
      for (char seqChar : sequence.toUpperCase().toCharArray()) {
         switch (seqChar) {
            case ')':
               if (permRegion) {
                  permRegion = false;

                  if (repeat) { repeatSeq += seqChar; }
                  else { expandedSeq += seqChar; }
               }

               if (repeat) {
                  for (int rptCnt = 0; rptCnt < repeatNum; rptCnt++) {
                     expandedSeq += repeatSeq;
                  }

                  repeatNum = 0;
                  repeatSeq = "";
                  repeat = false;
               }

               break;

            case '(':
               if (!repeat && repeatNum != 0) {
                  repeat = true;
               }

               else {
                  permRegion = true;
                  if (repeat) { repeatSeq += seqChar; }
                  else { expandedSeq += seqChar; }
               }

               break;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
               repeatNum = (10 * repeatNum) +  (seqChar - '0');
               break;

            case 'A':
            case 'T':
            case 'C':
            case 'G':
               if (repeat) { repeatSeq += seqChar; }
               else { expandedSeq += seqChar; }
               break;

            case ' ':
               break;

            default:
               System.err.printf("Cannot expand sequence containing '%s'\n", seqChar);
               return null;
         }
      }

      System.out.printf("Sequence '%s' expanded to '%s'\n", sequence, expandedSeq);
      return repeatRegion ? null : expandedSeq;
   }
}
