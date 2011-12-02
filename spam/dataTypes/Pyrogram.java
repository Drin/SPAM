package spam.dataTypes;

import gui.dataParser.XMLParser.PyrogramParser.PyrogramParser;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Pyrogram {
   private String mNucleotideSeq = null;
   private ArrayList<Double> mData = null;
   private ArrayList<Double> mResiduals = null;
   private double mMaxPeak = -1, mMean = -1;
   
   public Pyrogram(String seq, ArrayList<Double> data) {
      mNucleotideSeq = seq;
      mData = data;

      mMaxPeak = findMaxPeak(mData);
      mMean = calcMean(mData);

      mResiduals = calcResiduals(mData, mMean);
   }

   //TODO create a constructor for making a pyrogram from a file
   public Pyrogram(File parseFile) {
      if (parseFile.getName().endsWith(".xml")) {
         PyrogramParser parser = new PyrogramParser(parseFile);

         mNucleotideSeq = parser.getDispensation();
         mData = parser.getData();
      }

      else if (parseFile.getName().endsWith(".csv")) {
         Scanner csvReader = null;

         try {
            csvReader = new Scanner(parseFile);
         }

         catch(java.io.FileNotFoundException fileErr) {
            System.err.println("Could not find file: " + parseFile +
             "\nerror: " + fileErr);
            return;
         }

         while (csvReader.hasNextLine()) {
            String tmpLine = csvReader.nextLine();

            String[] lineTokens = tmpLine.split(",");

            //This represents the dispensation line
            if (lineTokens.length == 2) {
               //lineTokens[0] is "dispensation sequence:"
               //lineTokens[1] should be the actual sequence
               mNucleotideSeq = lineTokens[1];
            }

            //This represents the peak values line
            else if (lineTokens.length == mNucleotideSeq.length() + 1) {
               for (int tokenNdx = 1; tokenNdx < mNucleotideSeq.length(); tokenNdx++) {
                  //lineTokens[0] is "peak values:"
                  //lineTokens[n] should be a peak value
                  try {
                     Double tmpDbl = Double.parseDouble(lineTokens[tokenNdx]);
                     mData.add(tmpDbl);
                  }
                  catch (NumberFormatException formErr) {
                     System.err.println("invalid peak value:\n" +
                      "position: " + tokenNdx + "\nvalue: " + lineTokens[tokenNdx]);
                  }
               }
            }

            //This represents a messed up pyrogram file
            else {
               System.err.println("invalid contents in file: " + parseFile);
               return;
            }
         }
      }
      
      else {
         System.err.println("File " + parseFile + " has invalid file extension");
         return;
      }
   }

   public Pyrogram(String seq, ArrayList<String> rawDna, int numDisps) {
      mData = new ArrayList<Double>();
      ArrayList<StringCharacterIterator> seqItrs = new ArrayList<StringCharacterIterator>();
      mNucleotideSeq = "";

      for (String dna: rawDna) {
         seqItrs.add(new StringCharacterIterator(dna));
      }

      int seqsDone[] = new int[rawDna.size()];
      int pyroSequenceNdx = -1;
      while (!isDone(seqsDone) && numDisps > 0) {

         for (char nucleotide: seq.toCharArray()) {
            pyroSequenceNdx++;
            mData.add(0.0);

            //System.out.println("CURRENT NUCLEOTIDE: " + nucleotide);
            //build the expanded nucleotide sequence
            mNucleotideSeq += nucleotide;

            //advance each iterator
            for (int currItr = 0; currItr < seqItrs.size(); currItr++) {

               StringCharacterIterator seqItr = seqItrs.get(currItr);

               //while the iterator is not exhausted
               while (seqItr.getIndex() < seqItr.getEndIndex() - 1) {
                  //if the current nucleotide in the current dna string
                  //is the nucleotide that should be dispensed
                  if (seqItr.current() == nucleotide) {
                     /*
                     System.out.println("current: " + seqItr.current() +
                      " matches nucleotide: " + nucleotide);
                     System.out.println("data.size: " + mData.size() +
                      " pyroSeqNdx: " + pyroSequenceNdx);
                      */

                     mData.set(pyroSequenceNdx, mData.get(pyroSequenceNdx) + 1);
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
                     //For when you find a non match, break and go to
                     //the next dna strand
                     break;
                  }
               }

               if (seqItr.getIndex() == seqItr.getEndIndex() - 1) {
                  seqsDone[currItr] = 1;
               }
            }
         }

         if (--numDisps < 1) break;
      }

      /*
      System.out.println("mNucleotideSeq: " + mNucleotideSeq);
      System.out.println("data is: ");
      for (int peakVal : mData) {
         System.out.print(peakVal + ", ");
      }
      */

      mMaxPeak = findMaxPeak(mData);
      mMean = calcMean(mData);
      mResiduals = calcResiduals(mData, mMean);
   }

   //should return a double[][] where each row represents the comparisons for a
   //particular length of pyrogram
   //double[][] comparisonSteps
   //foreach (row : double[][])
   //   double[row][0] = correlation/comparisonValue;
   //   double[row][1] = same/different;
   public double[] compareTo(Pyrogram other, String comparisonType) {
      double returnNum = 0;
      int different = 0;
      
      //Pearson correlation preparation
      double pyroOneMean = 0, pyroTwoMean = 0, pearsonSum = 0,
       stdDevOne = 0, stdDevTwo = 0;
      if (comparisonType.equals("Pearson Correlation")) {
         pyroOneMean = calcMean(getData());
         pyroTwoMean = other.calcMean(other.getData());
      }
      
      else if (comparisonType.equals("Equality")) {
         System.out.println("Basic comparison true...");
         returnNum = 1;
      }
      
      //actual comparison being made
      for (int dataNdx = 0; dataNdx < Math.min(mData.size(),
       other.getData().size()); dataNdx++) {
         if (mData.get(dataNdx) != other.mData.get(dataNdx)) {
            different = 1;
         }
            
         if (comparisonType.equals("Pearson Correlation")) {
            pearsonSum += (mData.get(dataNdx) - pyroOneMean) *
             (other.mData.get(dataNdx) - pyroTwoMean);
            stdDevOne += Math.pow((mData.get(dataNdx) - pyroOneMean), 2);
            stdDevTwo += Math.pow((other.mData.get(dataNdx) - pyroTwoMean), 2);
         }
      
         else if (comparisonType.equals("Euclidean")) {
            //Euclidean distance
            double dist = Math.sqrt(Math.pow(mData.get(dataNdx), 2) +
             Math.pow(other.mData.get(dataNdx), 2));
            returnNum += dist;
            //corrCycle[dataNdx] = dist;
         }
            
         else if (mData.get(dataNdx) != other.mData.get(dataNdx)) {
            if (comparisonType.equals("Equality")) {
               //direct equality comparison
               System.out.println("Basic comparison false..");
               //gonna have to change this 
               returnNum = 0;
            }
            
            else if (comparisonType.equals("Direct Comparison")) {
               //number of differing locations
               returnNum++;
            }
         }
      }
      
      if (comparisonType.equals("Pearson Correlation")) {
         returnNum = (pearsonSum) / (Math.sqrt(stdDevOne) * Math.sqrt(stdDevTwo));
      }
      
      double[] diffVals = new double[2];
      
      diffVals[0] = returnNum;
      diffVals[1] = different;
      
      return diffVals;
   }

   private boolean isDone(int[] iters) {
      int ndx;
      for (ndx = 0; ndx < iters.length && iters[ndx] == 1; ndx++)
         ;
      return ndx == iters.length;
   }

   private ArrayList<Double> calcResiduals(ArrayList<Double> dataVector, double meanVal) {
      ArrayList<Double> residualVector = new ArrayList<Double>(dataVector.size());

      for (int ndx = 0; ndx < dataVector.size(); ndx++) {
         mData.set(ndx, dataVector.get(ndx) - meanVal);
      }

      return residualVector;
   }
   
   public String toString() {
      String str = "";
      for (char nucleotide : mNucleotideSeq.toCharArray()) {
         str += ", ";
      }
      str = str.substring(0, str.length() - 2) + "\n";

      for (double i : mData) {
         str += i + ", ";
      }
      str = str.substring(0, str.length() - 2);

      return str;
   }
   
   private double findMaxPeak(ArrayList<Double> dataVector) {
      double mMaxPeak = -1;

      for (double peakVal : dataVector) {
         mMaxPeak = Math.max(mMaxPeak, peakVal);
      }

      return mMaxPeak;
   }

   private double calcMean(ArrayList<Double> dataVector) {
      double total = 0;

      for (double peak : dataVector) {
         total += peak;
      }

      return total/dataVector.size();
   }
   
   public int getLength() {
      return mData.size();
   }
   
   public ArrayList<Double> getData() {
      return mData;
   }

   public ArrayList<Double> getResiduals() {
      return mResiduals;
   }

   public String getSequence() {
      return mNucleotideSeq;
   }

   public double getMaxPeak() {
      return mMaxPeak;
   }

   /*
    * I want this to be able to save in XML and CSV formats
    */
   public void save(String saveFile) {
      File csvFile = null, xmlFile = null;

      try {
         if (!saveFile.endsWith(".csv"))
            csvFile = new File(saveFile + ".csv");
         if (!saveFile.endsWith(".xml"))
            xmlFile = new File(saveFile + ".xml");

         csvFile.createNewFile();
         xmlFile.createNewFile();
      }
      catch (NullPointerException nullErr) {
         System.err.println("files: '" + saveFile + ".csv', '" +
          xmlFile + ".xml' do not exist\nerror: " + nullErr);
         return;
      }
      catch (java.io.IOException ioErr) {
         System.err.println("error trying to create new files: " +
          csvFile + ", " + xmlFile + "\nerror: " + ioErr);
         return;
      }

      writeCSV(csvFile);
      writeXML(xmlFile);
   }

   private void writeCSV(File csvFile) {
      PrintStream csvPrinter = null;
      try {
         csvPrinter = new PrintStream(new FileOutputStream(csvFile));
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.err.println("Could not find file: " + csvFile + "\nerror: " + fileErr);
         return;
      }
      catch (SecurityException secErr) {
         System.err.println("Invalid permissions for file: " + csvFile + "\nerror: " + secErr);
         return;
      }

      try {
         csvPrinter.printf("dispensation sequence:,%s\n", mNucleotideSeq);
         csvPrinter.printf("peak values:,");

         for (int dispNdx = 0; dispNdx < mData.size(); dispNdx++) {
            csvPrinter.printf("%.2f,", mData.get(dispNdx));
         }

         csvPrinter.printf("\n");
         csvPrinter.close();
      }
      catch (Exception err) {
         System.err.println("error while printing csv format: " + err);
         return;
      }
   }

   /*
    * XML format:
    * <pyrogram sequence="<seq>">
    *    <dispensation position="<index>" nucleotide="<nucleotide>">peakVal</dispensation>
    *    ...
    * </pyrogram>
    */
   private void writeXML(File xmlFile) {
      PrintStream xmlPrinter = null;
      try {
         xmlPrinter = new PrintStream(new FileOutputStream(xmlFile));
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.err.println("Could not find file: " + xmlFile + "\nerror: " + fileErr);
         return;
      }
      catch (SecurityException secErr) {
         System.err.println("Invalid permissions for file: " + xmlFile + "\nerror: " + secErr);
         return;
      }

      try {
         xmlPrinter.printf("<pyrogram sequence=\"%s\">\n", mNucleotideSeq);

         for (int dispNdx = 0; dispNdx < mData.size(); dispNdx++) {
            xmlPrinter.printf("\t<dispensation position=\"%d\" nucleotide=\"%c\">%.2f</dispensation>\n",
             dispNdx, mNucleotideSeq.charAt(dispNdx), mData.get(dispNdx));
         }

         xmlPrinter.printf("</pyrogram>\n");
         xmlPrinter.close();
      }
      catch (Exception err) {
         System.err.println("error while printing XML format: " + err);
         return;
      }
   }
}
