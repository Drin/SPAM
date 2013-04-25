package com.drin.java.ontology;

import com.drin.java.ontology.OntologyTerm;
import com.drin.java.database.CPLOPConnection;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Ontology List Format:
 * <OntologyTerm>(<Options>):<Partition>, <Partition>, ...;
 * <OntologyTerm>(<Options>):<Partition>, <Partition>, ...;
 * ...
 */
public class OntologyParser {
   private static final String FEATURE_PATTERN = "(^[^#].*)[.]([^#].*)\\((.*)\\):(.*)",
                               FEATURE_DELIMITER = ";",
                               OPTION_DELIM = ",",
                               VALUE_DELIM = ",";

   private static final int TABLE_NAME_NDX = 1,
                            TABLE_COL_NDX = 2,
                            OPTION_NDX = 3,
                            VALUE_NDX = 4;

   private CPLOPConnection mConn = null;
   private Pattern mRegexPattern;
   private Matcher mRegexMatch;

   public OntologyParser() {
      mRegexPattern = Pattern.compile(FEATURE_PATTERN + FEATURE_DELIMITER);
      mRegexMatch = null;

      if (mConn == null) {
         mConn = CPLOPConnection.getConnection();
      }
   }

   public static void main(String[] args) {
      OntologyParser parser = new OntologyParser();

      String searchStr = args[0];

      System.out.println(searchStr);

      if (parser.matchString(searchStr)) {
         parser.printOntology();
      }

      else {
         System.out.printf("Malformed feature description\n");
      }
   }

   public String getTermDelimiter() {
      return FEATURE_DELIMITER;
   }

   public Pattern getPattern() {
      return mRegexPattern;
   }

   public boolean matchString(String searchStr) {
      mRegexMatch = mRegexPattern.matcher(searchStr);

      return mRegexMatch.matches();
   }

   public OntologyTerm getTerm() {
      String tableName = getTermTableName(), colName = getTermColName();
      List<String> partitions = getTermValues();

      if (partitions.isEmpty()) {
         try {
            partitions = mConn.getDistinctValues(tableName, colName);
         }
         catch (java.sql.SQLException sqlErr) {
            System.out.printf("SQL Exception: '%s'\n", sqlErr);
            System.exit(1);
         }
      }

      return new OntologyTerm(tableName, colName, getTermOptions(), partitions);
   }

   public String getTermTableName() {
      if (mRegexMatch != null && mRegexMatch.matches()) {
         return mRegexMatch.group(TABLE_NAME_NDX).trim();
      }

      return "";
   }

   public String getTermColName() {
      if (mRegexMatch != null && mRegexMatch.matches()) {
         return mRegexMatch.group(TABLE_COL_NDX).trim();
      }

      return "";
   }

   public Map<String, Boolean> getTermOptions() {
      Map<String, Boolean> optionMap = new HashMap<String, Boolean>();

      if (mRegexMatch != null && mRegexMatch.matches()) {
         String[] optionArr = mRegexMatch.group(OPTION_NDX).replaceAll("\\s", "").split(OPTION_DELIM);

         for (int optionNdx = 0; optionNdx < optionArr.length; optionNdx++) {
            optionMap.put(optionArr[optionNdx], Boolean.TRUE);
         }
      }

      return optionMap;
   }

   public List<String> getTermValues() {
      List<String> valueList = new ArrayList<String>();

      if (mRegexMatch != null && mRegexMatch.matches()) {
         String[] valueArr = mRegexMatch.group(VALUE_NDX).split(VALUE_DELIM);

         for (int valNdx = 0; valNdx < valueArr.length; valNdx++) {
            if (!valueArr[valNdx].replaceAll(" ", "").equals("")) {
               valueList.add(valueArr[valNdx].trim());
            }
         }
      }

      return valueList;
   }

   public void printOntology() {
      String feature = String.format("Name: %s\n", mRegexMatch.group(TABLE_NAME_NDX).trim());
      Map<String, Boolean> optionMap = getTermOptions();
      List<String> valueList = getTermValues();

      feature += "Options:\n";
      for (String optionName : optionMap.keySet()) {
         if (optionMap.get(optionName).booleanValue()) {
            feature += String.format("\t%s\n", optionName);
         }
      }

      feature += "Values:\n";
      for (String value : valueList) {
         feature += String.format("\t%s\n", value);
      }

      System.out.printf("%s\n", feature);
   }
}
