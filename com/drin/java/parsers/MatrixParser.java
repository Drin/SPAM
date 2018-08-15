package com.drin.java.parsers;

import java.io.File;
import java.util.Scanner;
import java.util.Map;
import java.util.LinkedHashMap;

public class MatrixParser {
   private File mMatrixFile;
   private String mTokenDelim;

   private static final String DEFAULT_DELIMITER = ",";

   public MatrixParser(String filename) {
      mMatrixFile = new File(filename);
      mTokenDelim = DEFAULT_DELIMITER;
   }

   public MatrixParser(String filename, String delimiter) {
      mMatrixFile = new File(filename);
      mTokenDelim = delimiter;
   }

   public static void main(String[] args) {
      if (args[0].endsWith(".csv")) {
         MatrixParser parser = new MatrixParser(args[0]);

         Map<String, Map<String, Float>> data = parser.parseData();

         for (String iso_A : data.keySet()) {
            Map<String, Float> isoCorrMap = data.get(iso_A);
            
            for (String iso_B : isoCorrMap.keySet()) {
               System.out.printf("%s:%s -> %.04f\n", iso_A, iso_B, isoCorrMap.get(iso_B));
            }
         }
      }
   }

   public Map<String, Map<String, Float>> parseData() {
      Map<String, Map<String, Float>> tupleMap = new LinkedHashMap<String, Map<String, Float>>();
      Map<Integer, String> tupleColMap = null;
      Scanner fileScanner = null;

      try {
         fileScanner = new Scanner(mMatrixFile);
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.err.printf("Could not find file '%s'\n", mMatrixFile);
         System.exit(1);
      }

      if (fileScanner.hasNextLine()) {
         tupleColMap = getColumnMap(fileScanner.nextLine().replace("\"","")
                                               .split(mTokenDelim));
      }

      while (fileScanner.hasNextLine()) {
         String tupleStr = fileScanner.nextLine();

         //System.out.println(String.format("read line '%s'", tupleStr));

         tupleMap.putAll(constructTuple(tupleColMap,
          tupleStr.replace("\"","").split(mTokenDelim)));
      }

      return tupleMap;
   }

   private Map<Integer, String> getColumnMap(String[] tupleColumns) {
      Map<Integer, String> tupleColMap = new LinkedHashMap<Integer, String>();

      for (int colNdx = 0; colNdx < tupleColumns.length; colNdx++) {
         tupleColMap.put(colNdx, tupleColumns[colNdx]);
      }

      return tupleColMap;
   }

   private Map<String, Map<String, Float>> constructTuple(Map<Integer, String> colMap,
    String[] tupleData) {
      Map<String, Float> tuple = new LinkedHashMap<String, Float>();

      for (int tupleCol = 1; tupleCol < tupleData.length; tupleCol++) {
         String tupleColName = colMap.get(tupleCol);

         try {
            float tmpVal = Float.parseFloat(tupleData[tupleCol]);

            //If the given value is greater than 1, assume that the correlation
            //is a percentage instead of in decimal form
            if (tmpVal > 1) { tmpVal = tmpVal / 100; }

            //System.out.println(String.format("parsed correlation value [%s]", tmpVal));

            tuple.put(tupleColName, new Float(tmpVal));
         }

         catch (NumberFormatException numErr) {
            System.err.println(String.format(
               "Matrix contains invalid value '%s'", tupleData[tupleCol]
            ));

            System.exit(1);
         }
      }

      Map<String, Map<String, Float>> tupleMap = new LinkedHashMap<String, Map<String, Float>>();

      if (tupleData.length > 0) {
         tupleMap.put(tupleData[0], tuple);
      }

      return tupleMap;
   }
}
