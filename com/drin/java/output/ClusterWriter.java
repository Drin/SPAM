package com.drin.java.output;

import java.io.File;
import java.io.FileWriter;

public class ClusterWriter {
   private static final String DEFAULT_DIR = "ClusterResults",
                               FILE_SEP = System.getProperty("file.separator");
   private static final FileType DEFAULT_EXT = FileType.CSV;
   private String mFileName;
   private File mFile;

   public ClusterWriter(String outputFile) {
      mFileName = System.getProperty("user.dir") +
                  FILE_SEP + DEFAULT_DIR +
                  FILE_SEP + outputFile;
   }

   public void writeData(String text) {
      FileWriter writer = null;

      try {
         mFile = new File(mFileName);

         if (!mFile.getParentFile().exists()) { mFile.getParentFile().mkdirs(); }
         if (!mFile.isFile()) { mFile.createNewFile(); }

         writer = new FileWriter(mFile);
         writer.write(text);
         writer.close();
      }
      catch (java.io.IOException ioErr) {
         System.err.printf("IOException when writing to file '%s'\n", mFile.getName());
         ioErr.printStackTrace();
         return;
      }
   }

   public enum FileType {
      CSV(".csv"), MATRIX(".matrix"), XML(".xml"), PYRORUN(".pyrorun");

      private String mExt;

      private FileType(String fileExt) {
         mExt = fileExt;
      }

      @Override
      public String toString() {
         return mExt;
      }
   }
}
