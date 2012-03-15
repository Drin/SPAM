package spam.data;

import spam.data.Pyrogram;
import spam.outputHandlers.MetricsDisplay;

import spam.dataParser.XMLParser.PolyPyroDriver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//import java.util.Random;

public class PyrogramComparer {
   String comparisonType, forPrimer = "", revPrimerComp = "";
   File inputDir;
   //ArrayList<String> sequenceData;
   ArrayList<Pyrogram> pyrograms, revPyrograms;
   ArrayList<ArrayList<double[]>> comparisonMatrices;
   ArrayList<ArrayList<double[]>> revComparisonMatrices;
   ArrayList<ArrayList<int[]>> differenceMatrices;
   ArrayList<ArrayList<int[]>> revDifferenceMatrices;
   ArrayList<String> candidateSequences;
   //constant string abbrev for all permutations of ATCG
   final String PERM_STR = "(ATCG)"; 
   String[] basicPermutations = new String[] { 
         "ATCG", "ATGC", "AGTC", "AGCT", "ACTG", "ACGT",
         "TCGA", "TCAG", "TGAC", "TGCA", "TAGC", "TACG",
         "CTGA", "CTAG", "CGAT", "CGTA", "CAGT", "CATG",
         "GATC", "GACT", "GTAC", "GTCA", "GCTA", "GCAT"};

   public PyrogramComparer(String filePath, String[] readSequences, String compType) {
      inputDir = new File(filePath);
      comparisonType = compType;
      candidateSequences = new ArrayList<String>();
      
      //replace any instances of "(ATCG)" with all permutations of "ATCG"
      for (int ndx = 0; ndx < readSequences.length; ndx++) {
         if (readSequences[ndx].contains(PERM_STR)) {
            for (int permNdx = 0; permNdx < basicPermutations.length; permNdx++) {
               String tmpStr = readSequences[ndx];
               candidateSequences.add(tmpStr.replace(PERM_STR, basicPermutations[permNdx]));
            }
         }
         else {
            System.out.println("size: " + candidateSequences.size());
            candidateSequences.add(ndx, readSequences[ndx]);
         }
      }
      
      //these are for storing all calculation results
      pyrograms = new ArrayList<Pyrogram>();
      revPyrograms = new ArrayList<Pyrogram>();
      comparisonMatrices = new ArrayList<ArrayList<double[]>>();
      revComparisonMatrices = new ArrayList<ArrayList<double[]>>();
      differenceMatrices = new ArrayList<ArrayList<int[]>>();
      revDifferenceMatrices = new ArrayList<ArrayList<int[]>>();
   }
   
   public void setForwardPrimer(String primer) {
      forPrimer = primer;
   }
   
   public void setReversePrimer(String primer) {
      revPrimerComp = primer;
   }
   
   public String[] compareAllSequences() {
      return null;
   }

   public String[] compareForwardSequences(boolean isXML) {
      MetricsDisplay.setDNAFiles(inputDir.listFiles(new FileFilter () {
         public boolean accept(File pathname) {
            return pathname.isFile();
         }
      }));
      
      for (int seqNdx = 0; seqNdx < candidateSequences.size(); seqNdx++) {
         System.out.println("comparing forward pyrograms...");
         /*
          * This section creates a list of pyrograms for this candidate sequence
          */
         //candidateSequences is a list of dispensation sequences
         Polyparse parser = new Polyparse(candidateSequences.get(seqNdx), 1);
         MetricsDisplay.addDispSeq(candidateSequences.get(seqNdx));
         File[] fileList = inputDir.listFiles(new FileFilter() {
            public boolean accept(File fileName) {
               return fileName.isFile();
            }
         });
         
         for (int fileNdx = 0; fileNdx < fileList.length; fileNdx++) {
            //parse dna sequences from file into sequenceData
            //this is for FASTA files
            if (!isXML) {
               ArrayList<String> sequenceData = null;
               if (!fileList[fileNdx].isFile()) continue;
      
               try {
                  sequenceData = parser.parse(inputDir.getAbsolutePath() + File.separator + fileList[fileNdx].getName());
               }
               catch (IOException err) {
                  System.out.println("Error parsing data file: ");
                  err.printStackTrace();
               }

               System.out.println("File: " + fileList[fileNdx]);
            
               //iterate over all dna sequences (foci) in a given file (strain) to
               //parse out forward and reverse primers from each sequence
               for (int curSeq = 0; curSeq < sequenceData.size(); curSeq++) {               
                  String seq = matchPrimers(sequenceData.get(curSeq));
               
                  if (seq == null) {
                     System.out.println("Invalid primers. returning null sequences");
                     return null;
                  }
               
                  //replace the raw sequence at the current index with
                  //the isolated dna segment to sequence (using primers)
                  sequenceData.set(curSeq, seq);
               }
            
               //create a pyrogram (forward and maybe reverse) using candidate sequences and each file
               //then add the pyrogram to a list of all pyrograms to compare
            
               pyrograms.add(createPyrograms(
                candidateSequences.get(seqNdx), sequenceData));
            }
            //this is for xml files
            else {
               File xmlFile = fileList[fileNdx];
               System.out.println("checking out xmlFile: " + xmlFile);
               HashMap<String, String> wellNames =
                PolyPyroDriver.parseXMLHeaders(xmlFile);

               for (String well : wellNames.keySet()) {
                  pyrograms.add(PolyPyroDriver.parseXMLFile(xmlFile.getAbsolutePath(), well));
               }
            }
            /*TODO mode
            if (mode.equals("Forward and Reverse"))
               revPyrograms.add(createRevPyrograms(seqNdx));
               */
         }
         
         /*
          * This section adds the comparison matrix for this candidate sequence
          * to the list of comparison matrices for all candidate sequences
          */
         //TODO make all of these 2dimensional arrays
         ArrayList<double[]> comparisonMatrix = new ArrayList<double[]>();
         //this matrix will not be populated if not building reverse pyrogram
         ArrayList<double[]> revComparisonMatrix = new ArrayList<double[]>();
         ArrayList<int[]> diffMatrix = new ArrayList<int[]>();
         ArrayList<int[]> revDiffMatrix = new ArrayList<int[]>();
      
         //compareNdx is the row, compareToNdx is the column for a comparison matrix
         for (int compareNdx = 0; compareNdx < pyrograms.size(); compareNdx++) {
            System.out.println("creating comparison matrix");
            //TODO make all of these 2 dimensional arrays
            double[] matrixRow = new double[pyrograms.size()];
            //this array will not be populated if not building reverse pyrogram
            double[] revMatrixRow = new double[revPyrograms.size()];
            int[] diffRow = new int[pyrograms.size()];
            int[] revDiffRow = new int[revPyrograms.size()];
         
            for (int compareToNdx = 0; compareToNdx < pyrograms.size(); compareToNdx++) {
               //index 0 is the comparison value (based on distance option)
               //index 1 is the equality value between corresponding indices of the pyrogram
               //TODO call a different compareTo that returns an array of results (for different lengths)
               double[] compResults = pyrograms.get(compareNdx).compareTo(pyrograms.get(compareToNdx), comparisonType);
               matrixRow[compareToNdx] = compResults[0] * 100;
               diffRow[compareToNdx] = (int) compResults[1];

               /* commenting out, pearson correlations seem to be correct
               if (!PyrogramTester.checkPearson(pyrograms.get(compareNdx),
                pyrograms.get(compareToNdx), compResults[0]))
                  System.out.println("Unexpected Pearson");
               */
               
               //have to make sure that only add to this array if reverse pyrogram is being built
               /*TODO mode
               if (mode.equals("Forward and Reverse")) {
                  //index 0 is the comparison value, index 1 is the number of differences between pyrograms
                  double[] revCompResults =
                   revPyrograms.get(compareNdx).compareTo(revPyrograms.get(compareToNdx), comparisonType);
                  revMatrixRow[compareToNdx] = revCompResults[0] * 100; //to return a percentage
                  revDiffRow[compareToNdx] = (int) revCompResults[1];
                  
                  /* commenting out; pearson correlations seem to be correct
                  if(!PyrogramTester.checkPearson(revPyrograms.get(compareNdx),
                   revPyrograms.get(compareToNdx), revCompResults[0]))
                     System.out.println("Unexpected reverse Pearson");
               }
            */
               
            }
            
            comparisonMatrix.add(matrixRow);
            diffMatrix.add(diffRow);
            /*TODO mode
            if (mode.equals("Forward and Reverse")) {
               revComparisonMatrix.add(revMatrixRow);
               revDiffMatrix.add(revDiffRow);
            }
            */
               
         }
         
         comparisonMatrices.add(comparisonMatrix);
         differenceMatrices.add(diffMatrix);
         MetricsDisplay.storeComparisonMatrix(comparisonMatrix);
         MetricsDisplay.storeDifferenceMatrix(diffMatrix);
         
         /*TODO mode
         if (mode.equals("Forward and Reverse")) {
            revComparisonMatrices.add(revComparisonMatrix);
            revDifferenceMatrices.add(revDiffMatrix);
            MetricsDisplay.storeRevComparisonMatrix(revComparisonMatrix);
            MetricsDisplay.storeRevDifferenceMatrix(revDiffMatrix);
         }
         */
         
         
         pyrograms.clear();
         revPyrograms.clear();
      }
      
      //MetricsDisplay.displayMetrics();
      
      /*
       * This section determines the best/optimal comparison matrices in the list of
       * comparison matrices for all candidate sequences
       */

      System.out.println("calling displayMetrics from pyrogram comparer");
      return MetricsDisplay.displayMetrics("Forward");
      //return compareMatrices(mode);
   }
   
   private Pyrogram createRevPyrograms(String dispSeq, ArrayList<String> sequences) {
      int numDispensations = 104;
      ArrayList<String> revSequenceData = new ArrayList<String>();
      for (String s : sequences) {
         revSequenceData.add(reverseString(s));
      }
      
      System.out.println("instantiating Pyrogram");
      String pyrogramText = null;
      
      
      /*
      try {
      }
      catch(IOException err) {
         System.out.println("Error creating pyrogram: ");
         err.printStackTrace();
      }
      System.out.println("Beginning pyrogram formatting...");
      return formatPyrogram(pyrogramText);
      */

      //this should do everything that the above code /used/ to do
      return new Pyrogram(dispSeq, revSequenceData, numDispensations);
   }
   
   private Pyrogram createPyrograms(String dispSeq, ArrayList<String> sequences) {
      int numDispensations = 104;
      System.out.println("instantiating Pyrogram");
      String pyrogramText = null;
      
      
      /*
      pyrogram.setSeqIters(numDispensations);
      try {
         //null argument means to not output pyrogram to file
         pyrogramText = pyrogram.createHistogram(null);
      }
      catch(IOException err) {
         System.out.println("Error creating pyrogram: ");
         err.printStackTrace();
      }
      System.out.println("Beginning pyrogram formatting...");
      return formatPyrogram(pyrogramText);
      */

      //this should do everything that the above code /used/ to do
      return new Pyrogram(dispSeq, sequences, numDispensations);
   }
   
   
   private String matchPrimers(String s) {
      String sReverse = reverseString(s);
      int primerEnd = s.indexOf(forPrimer) + forPrimer.length();
      int revEnd = s.length() - (sReverse.indexOf(revPrimerComp) + revPrimerComp.length());
      
      if (primerEnd < revEnd) {
         return s.substring(primerEnd, revEnd);
      }
      else if (primerEnd == -1) {
         System.out.println("forward primer not found.");
         return null;
      }
      else if (revEnd == -1) {
         System.out.println("reverse primer not found.");
         return null;
      }
      else {
         System.out.println("Primers cross");
         return null;
      }
   }
   
   private String reverseString(String s) {
      String sReverse = "";

      for (int strNdx = s.length() - 1; strNdx >= 0; strNdx--) {
         if (s.charAt(strNdx) != 'A' || s.charAt(strNdx) != 'T' ||
            s.charAt(strNdx) != 'C' || s.charAt(strNdx) != 'G') {
            //TODO add some way to flag sequence
         }
         sReverse += s.charAt(strNdx) == 'A' ? 'T' :
                  s.charAt(strNdx) == 'G' ? 'C' :
                  s.charAt(strNdx) == 'T' ? 'A' :
                  s.charAt(strNdx) == 'C' ? 'G' : s.charAt(strNdx);
      }
      
      return sReverse;
   }
   
   private HashMap<String, String> compareMatrices() {
      //TODO mode
      String mode = "Forward";
      HashMap<String, String> optimalSequences = new HashMap<String, String>();
      double bestValue = 0;
      for (int matrixNdx = 0; matrixNdx < comparisonMatrices.size(); matrixNdx++) {
         //System.out.println("bestValue is: " + bestValue);
         double tmpVal = valueOf(comparisonMatrices.get(matrixNdx));
         String dir = "forward";
         
         /*TODO mode
         if (mode.equals("Forward and Reverse")) {
            double valueTwo = valueOf(revComparisonMatrices.get(matrixNdx));
            
            if (valueTwo < tmpVal) {
               tmpVal = valueTwo;
               dir = "reverse";
            }
         }
         */
         
         if (bestValue == 0) {
            bestValue = tmpVal;
            optimalSequences.put(candidateSequences.get(matrixNdx), dir);
         }
         else {
            if (tmpVal == bestValue) {
               optimalSequences.put(candidateSequences.get(matrixNdx), dir);
            }
            else if (tmpVal < bestValue) {
               bestValue = tmpVal;
               optimalSequences.clear();
               optimalSequences.put(candidateSequences.get(matrixNdx), dir);
            }
         }
      }
      /*
      HashMap<String, String> optimalSequences = new HashMap<String, String>();
      int ndx = 0;
      for (int optimalNdx : optimalSeqIndices.keySet()) {
         if (optimalNdx < candidateSequences.size() && ndx < optimalSequences.size()) {
            //divide optimalSeqIndices values by 2 then multiply by 2 to ensure that
            //reverse and forward indices point to appropriate dispensation sequences
         }
         
         //also need to somehow store best pyrogram?
         ndx++;
      }
      */
      return optimalSequences;
   }
   
   private double valueOf(ArrayList<double[]> comparisonMatrix) {
      double value = 0;
      double size = 0;
      for (double[] cycle : comparisonMatrix) {
         for (int cycleNdx = 0; cycleNdx < cycle.length; cycleNdx++) {
            value += cycle[cycleNdx];
            size++;
         }
      }
      System.out.println("Value is: " + (value/size));
      return value/size;
   }
   
}
