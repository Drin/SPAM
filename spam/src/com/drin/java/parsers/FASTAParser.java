package com.drin.java.parsers;

import java.io.File;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class FASTAParser {
   private File mFile;
   private List<String> mDnaStrings = null;

   public FASTAParser(File fastaFile) {
      mFile = fastaFile;
      mDnaStrings = new ArrayList<String>();

      try {
         Scanner fileScanner = new Scanner(mFile);
         String tmpDnaString = "";

         while (fileScanner.hasNextLine()) {
            String fileLine = fileScanner.nextLine();

            if (fileLine.startsWith(">")) {
               mDnaStrings.add(tmpDnaString);
               tmpDnaString = "";
            }

            else {
               tmpDnaString += fileLine;
            }
         }

         //remove dummy sequence
         mDnaStrings.remove(0);
         //add last sequence
         mDnaStrings.add(tmpDnaString);
         
         if (fileScanner != null) {
            fileScanner.close();
         }
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.println("Could not find file: " + mFile);
         System.exit(1);
      }
   }

   public List<String> getDnaStrings() {
      return mDnaStrings;
   }
}
