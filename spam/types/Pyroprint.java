package spam.types;

import spam.parser.pyrorun.PyrogramParser.PyrogramParser;

import java.text.StringCharacterIterator;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Pyroprint {
   private static final int DISP_NDX = 0,
                            PEAK_LIST_NDX = 1;

   private static final String ISOLATE_HEADER = "Isolate Name:",
                               DISP_HEADER = "Dispensation Sequence:",
                               DATA_HEADER = "Peak Values:";

   private String mIsolateName = null; 
   private String mDispensation = null;
   private List<Double> mPeaks = null;

   private double mMaxPeak = -1, mMeanPeak = -1;

   /*==========================================================*/
   /*=======================Constructors=======================*/
   /*==========================================================*/
   public Pyroprint(String name, String dnaSeq, List<Double> data) {
      mIsolateName = name;
      mDispensation = dnaSeq;
      mPeaks = data;

      mMaxPeak = findMaxPeak(mPeaks);
      mMeanPeak = calcMeanPeak(mPeaks);
   }

   public Pyroprint(String name, File pyrogramFile) {
      Object[] pyrogramData = parsePyrogram(pyrogramFile);

      super(name, pyrogramData[DISP_NDX], pyrogramData[PEAK_LIST_NDX]);
   }

   public Pyroprint(String name, String seq, List<String> rawDna, int numDisps) {
      Object[] pyrogramData = buildPyrogram(seq, rawDna, numDisps);

      super(name, pyrogramData[DISP_NDX], pyrogramData[PEAK_LIST_NDX]);
   }


   /*
    * Overridden Object Methods
    */
   public String toString() {
      String str = "";

      for (char nucleotide : mNucleotideSeq.toCharArray()) {
         str += nucleotide + ", ";
      }
      str = str.substring(0, str.length() - 2) + "\n";

      for (double peak : mPeaks) {
         str += peak + ", ";
      }
      str = str.substring(0, str.length() - 2);

      return str;
   }

   public int hashCode() {
      return mIsolateName.hashCode();
   }

   public boolean equals(Object otherPyroprint) {
      if (otherPyroprint instanceof Pyroprint) {
         Pyroprint otherPyro = (Pyroprint) otherPyroprint;

         return mIsolateName.equals(otherPyroprint.getIsolateName()) &&
          mPyrogram.equals(otherPyroprint.mPyrogram);
      }

      return false;
   }

   /*
    * Getter Methods
    */

   public int getLength() {
      return mPeaks.size();
   }

   public List<Double> getPeaks() {
      return mPeaks;
   }

   public String getDispSequence() {
      return mDispensation;
   }

   public double getMaxPeak() {
      return mMaxPeak;
   }

   public double getMeanPeak() {
      return mMeanPeak;
   }

   public String getIsolateName() {
      return mIsolateName;
   }

   /*
    * Utility Methods
    */
   private double findMaxPeak(List<Double> dataVector) {
      double mMaxPeak = -1;

      for (double peakVal : dataVector) {
         mMaxPeak = Math.max(mMaxPeak, peakVal);
      }

      return mMaxPeak;
   }

   private double calcMeanPeak(List<Double> dataVector) {
      double total = 0;

      for (double peak : dataVector) {
         total += peak;
      }

      return total/dataVector.size();
   }

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


   /*==========================================================*/
   /*===================Constructors Helpers===================*/
   /*==========================================================*/
   /*
    * return value is Object[2] where:
    *    Object[0] = dispensation sequence
    *    Object[1] = list of peak heights
    */
   private Object[] parsePyrogram(File pyrogramFile) {
      String nucleotideSeq = null;
      ArrayList<Double> data = null;

      if (pyrogramFile.getName().endsWith(".xml")) {
         PyrogramParser parser = new PyrogramParser(pyrogramFile);

         nucleotideSeq = parser.getDispensation();
         data = parser.getData();
      }

      else if (pyrogramFile.getName().endsWith(".csv")) {
         Scanner csvReader = null;

         try {
            csvReader = new Scanner(pyrogramFile);
         }

         catch(java.io.FileNotFoundException fileErr) {
            System.err.println("Could not find file: " + pyrogramFile +
             "\nerror: " + fileErr);
            return null;
         }

         while (csvReader.hasNextLine()) {
            String tmpLine = csvReader.nextLine();

            String[] lineTokens = tmpLine.split(",");

            //This represents the dispensation line
            if (lineTokens[0].equals(ISOLATE_HEADER)) {
               mIsolateName = lineTokens[1];
            }
            else if (lineTokens[0].equals(DISP_HEADER)) {
               nucleotideSeq = lineTokens[1];
            }

            //This represents the peak values line
            else if (lineTokens[0].equals(DATA_HEADER)) {
               for (int tokenNdx = 1; tokenNdx < mNucleotideSeq.length(); tokenNdx++) {
                  try {
                     Double tmpDbl = Double.parseDouble(lineTokens[tokenNdx]);
                     data.add(tmpDbl);
                  }
                  catch (NumberFormatException formErr) {
                     System.err.println("invalid peak value:\n" +
                      "position: " + tokenNdx + "\nvalue: " + lineTokens[tokenNdx]);
                  }
               }
            }

            //This represents a messed up pyrogram file
            else {
               System.err.println("invalid contents in file: " + pyrogramFile);
               return null;
            }
         }
      }
      
      else {
         System.err.println("File " + pyrogramFile + " has invalid file extension");
         return null;
      }

      return new Object[2] { nucleotideSeq, data };
   }

   /*
    * return value is Object[2] where:
    *    Object[0] = dispensation sequence
    *    Object[1] = list of peak heights
    */
   private Object[] buildPyrogram(String seq, List<String> rawDna, int numDisps) {
      List<StringCharacterIterator> seqItrs = new ArrayList<StringCharacterIterator>();
      List<Double> data = new ArrayList<Double>();
      String nucleotideSeq = "";

      int seqsDone[] = new int[rawDna.size()];
      int pyroSequenceNdx = -1;

      for (String dna: rawDna) {
         seqItrs.add(new StringCharacterIterator(dna));
      }

      while (!isDone(seqsDone) && numDisps > 0) {
         for (char nucleotide: seq.toCharArray()) {
            pyroSequenceNdx++;
            data.add(0.0);

            //build the complete nucleotide sequence
            nucleotideSeq += nucleotide;

            //advance each iterator
            for (int currItr = 0; currItr < seqItrs.size(); currItr++) {
               StringCharacterIterator seqItr = seqItrs.get(currItr);

               //while the iterator is not exhausted
               while (seqItr.getIndex() < seqItr.getEndIndex() - 1) {
                  //if the current nucleotide in the current dna string
                  //is the nucleotide that should be dispensed
                  if (seqItr.current() == nucleotide) {
                     data.set(pyroSequenceNdx, data.get(pyroSequenceNdx) + 1);
                     seqItr.next();
                  }

                  //blank nucleotide
                  else if (seqItr.current() == '-') { seqItr.next(); }
                   
                  else if (seqItr.current() != 'A' && seqItr.current() != 'T' &&
                   seqItr.current() != 'C' && seqItr.current() != 'G') {
                     //if the current dna nucleotide is not a valid nucleotide
                     seqItr.next();
                  }
                  
                  //For when you find a non match, break and go to
                  //the next dna strand
                  else { break; }
               }

               if (seqItr.getIndex() == seqItr.getEndIndex() - 1) {
                  seqsDone[currItr] = 1;
               }
            }
         }

         if (--numDisps < 1) { break; }
      }

      return new Object[2] { nucleotideSeq, data };
   }

   private boolean isDone(int[] iters) {
      int ndx;
      for (ndx = 0; ndx < iters.length && iters[ndx] == 1; ndx++)
         ;
      return ndx == iters.length;
   }

}
