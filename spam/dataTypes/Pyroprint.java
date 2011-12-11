package spam.dataTypes;

import spam.dataParser.XMLParser.PyrogramParser.PyrogramParser;

import java.text.StringCharacterIterator;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Pyroprint {
   private static final String ISOLATE_HEADER = "Isolate Name:";
   private static final String DISP_HEADER = "Dispensation Sequence:";
   private static final String DATA_HEADER = "Peak Values:";

   private String mIsolateName = null; 
   private String mNucleotideSeq = null;

   private ArrayList<Double> mData = null;
   private ArrayList<Double> mResiduals = null;

   private Pyrogram mPyrogram = null;
   private IsolateContext mContext = null;

   private double mMaxPeak = -1, mMean = -1;

   /*==========================================================*/
   /*=======================Constructors=======================*/
   /*==========================================================*/
   Public Pyroprint(String name) {
      mIsolateName = name;
   }

   public Pyroprint(String name, File pyrogramFile) {
      mIsolateName = name;

      mPyrogram = parsePyrogram(pyrogramFile);
   }

   public Pyroprint(String name, String seq, ArrayList<Double> data) {
      mIsolateName = name;

      mPyrogram = new Pyrogram(seq, data);
   }

   public Pyroprint(String name, String seq, List<String> rawDna, int numDisps) {
      mIsolateName = name;

      mPyrogram = buildPyrogram(seq, rawDna, numDisps);
   }

   /*==========================================================*/
   /*===================Constructors Helpers===================*/
   /*==========================================================*/
   public Pyrogram extractPyrogram(File pyrogramFile) {
      return parsePyrogram(pyrogramFile);
   }

   public Pyrogram constructPyrogram(String seq, List<String> rawDna, int numDisps) {
      return buildPyrogram(seq, rawDna, numDisps);
   }

   private Pyrogram parsePyrogram(File pyrogramFile) {
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

      return new Pyrogram(nucleotideSeq, data);
   }

   private Pyrogram buildPyrogram(String seq, List<String> rawDna, int numDisps) {
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

      return new Pyrogram(nucleotideSeq, data);
   }

   public double[] compareTo(Pyroprint other, String comparisonType) {
      return mPyrogram.compareTo(other.getPyrogram(), comparisonType);
   }

   private boolean isDone(int[] iters) {
      int ndx;
      for (ndx = 0; ndx < iters.length && iters[ndx] == 1; ndx++)
         ;
      return ndx == iters.length;
   }

   public String toString() {
      return mPyrogram.toString();
   }

   public Pyrogram getPyrogram() {
      return mPyrogram;
   }
   
   public int getLength() {
      return mPyrogram.getLength();
   }
   
   public List<Double> getPeaks() {
      return mPyrogram.getData();
   }

   public String getDispSequence() {
      return mPyrogram.getDispSequence();
   }

   public int hashCode() {
      return mIsolateName.hashCode();
   }

   public boolean equals(Object otherPyroprint) {
      int dispNdx = 0;
      if (otherPyroprint instanceof Pyroprint) {
         Pyroprint otherPyro = (Pyroprint) otherPyroprint;

         if (this.getLength == otherPyro.getLength()) {
            for (dispNdx = 0; dispNdx < getLength && mData.get(dispNdx) == otherPyro.mData.get(dispNdx) &&
             mNucleotideSeq.charAt(dispNdx) == otherPyro.mNucleotideSeq.charAt(dispNdx); dispNdx++)
               ;
         }

         return mIsolateName.equals(otherPyro.mIsolateName) && dispNdx == getLength;
      }

      return false;
   }

   /*
    * I want this to be able to save in XML and CSV formats
    */
   public void save(String saveFile) {
      mPyrogram.save(saveFile);
   }
}
