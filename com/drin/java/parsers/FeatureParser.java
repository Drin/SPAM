package com.drin.java.parsers;

import com.drin.java.types.FeatureNode;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * FeatureList Format:
 * <FeatureName>(<Options>):<Partition>, <Partition>, ...;
 * <FeatureName>(<Options>):<Partition>, <Partition>, ...;
 * ...
 */
public class FeatureParser {
   private static final String FEATURE_PATTERN = "(^[^#].*)\\((.*)\\):(.*)",
                               FEATURE_DELIMITER = ";",
                               OPTION_DELIM = ",",
                               VALUE_DELIM = ",";
   private static final int NAME_NDX = 1,
                            OPTION_NDX = 2,
                            VALUE_NDX = 3;
   private Pattern mRegexPattern;
   private Matcher mRegexMatch;

   public FeatureParser() {
      mRegexPattern = Pattern.compile(FEATURE_PATTERN + FEATURE_DELIMITER);
      mRegexMatch = null;
   }

   public static void main(String[] args) {
      FeatureParser parser = new FeatureParser();

      String searchStr = args[0];

      if (parser.matchString(searchStr)) {
         parser.printFeature();
      }

      else {
         System.out.printf("Malformed feature description\n");
      }
   }

   public String getFeatureDelimiter() {
      return FEATURE_DELIMITER;
   }

   public Pattern getPattern() {
      return mRegexPattern;
   }

   public boolean matchString(String searchStr) {
      mRegexMatch = mRegexPattern.matcher(searchStr);

      return mRegexMatch.matches();
   }

   public FeatureNode getFeature() {
      return new FeatureNode(getFeatureName(), getFeatureOptions(), getFeatureValues());
   }

   public String getFeatureName() {
      if (mRegexMatch != null && mRegexMatch.matches()) {
         return mRegexMatch.group(NAME_NDX);
      }

      return "";
   }

   public Map<String, Boolean> getFeatureOptions() {
      Map<String, Boolean> optionMap = new HashMap<String, Boolean>();

      if (mRegexMatch != null && mRegexMatch.matches()) {
         String[] optionArr = mRegexMatch.group(OPTION_NDX).replace(" ", "").split(OPTION_DELIM);

         for (int optionNdx = 0; optionNdx < optionArr.length; optionNdx++) {
            optionMap.put(optionArr[optionNdx], Boolean.TRUE);
         }
      }

      return optionMap;
   }

   public List<String> getFeatureValues() {
      List<String> valueList = new ArrayList<String>();

      if (mRegexMatch != null && mRegexMatch.matches()) {
         String[] valueArr = mRegexMatch.group(VALUE_NDX).replace(" ", "").split(VALUE_DELIM);

         for (int valNdx = 0; valNdx < valueArr.length; valNdx++) {
            valueList.add(valueArr[valNdx]);
         }
      }

      return valueList;
   }

   public void printFeature() {
      String feature = String.format("Name: %s\n", mRegexMatch.group(NAME_NDX));
      Map<String, Boolean> optionMap = getFeatureOptions();
      List<String> valueList = getFeatureValues();

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
