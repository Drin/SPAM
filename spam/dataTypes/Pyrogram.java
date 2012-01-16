package spam.dataTypes;

import java.text.StringCharacterIterator;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Pyrogram {
   private String mNucleotideSeq = null;
   private List<Double> mData = null;
   private List<Double> mResiduals = null;
   private double mMaxPeak = -1, mMean = -1;
   
   public Pyrogram(String seq, List<Double> data) {
      mNucleotideSeq = seq;
      mData = data;

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

   private List<Double> calcResiduals(List<Double> dataVector, double meanVal) {
      List<Double> residualVector = new ArrayList<Double>(dataVector.size());

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
   
   private double findMaxPeak(List<Double> dataVector) {
      double mMaxPeak = -1;

      for (double peakVal : dataVector) {
         mMaxPeak = Math.max(mMaxPeak, peakVal);
      }

      return mMaxPeak;
   }

   private double calcMean(List<Double> dataVector) {
      double total = 0;

      for (double peak : dataVector) {
         total += peak;
      }

      return total/dataVector.size();
   }
   
   public int getLength() {
      return mData.size();
   }
   
   public List<Double> getData() {
      return mData;
   }

   public List<Double> getResiduals() {
      return mResiduals;
   }

   public String getDispSequence() {
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
