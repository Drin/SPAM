package com.drin.java.parsers;

import java.io.File;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class CSVParser {
   private File mCSVFile;
   private String mTokenDelim;

   private static final String DEFAULT_DELIMITER = ",";

   public CSVParser(String filename) {
      mCSVFile = new File(filename);
      mTokenDelim = DEFAULT_DELIMITER;
   }

   public CSVParser(String filename, String delimiter) {
      mCSVFile = new File(filename);
      mTokenDelim = delimiter;
   }

   public List<Map<String, Object>> parseData() {
      List<Map<String, Object>> tupleList = new ArrayList<Map<String, Object>>();
      Map<Integer, String> tupleColMap = null;
      Scanner fileScanner = null;

      try {
         fileScanner = new Scanner(mCSVFile);
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.err.printf("Could not find file '%s'\n", mCSVFile);
         System.exit(1);
      }

      if (fileScanner.hasNextLine()) {
         tupleColMap = getColumnMap(fileScanner.nextLine().replace("\"","")
                                               .split(mTokenDelim));
      }

      while (fileScanner.hasNextLine()) {
         tupleList.add(constructTuple(tupleColMap,
          fileScanner.nextLine().replace("\"","").split(mTokenDelim)));
      }

      return tupleList;
   }

   private Map<Integer, String> getColumnMap(String[] tupleColumns) {
      Map<Integer, String> tupleColMap = new HashMap<Integer, String>();

      for (int colNdx = 0; colNdx < tupleColumns.length; colNdx++) {
         tupleColMap.put(colNdx, tupleColumns[colNdx]);
      }

      return tupleColMap;
   }

   private Map<String, Object> constructTuple(Map<Integer, String> colMap,
    String[] tupleData) {
      Map<String, Object> tuple = new HashMap<String, Object>();

      for (int tupleCol = 0; tupleCol < tupleData.length; tupleCol++) {
         String tupleColName = colMap.get(tupleCol);
         tuple.put(tupleColName, tupleData[tupleCol]);
      }

      return tuple;
   }
}
