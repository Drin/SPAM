package com.drin.java.util;

import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Configuration {
   private static final int PROP_KEY = 0,
                            PROP_VAL = 1;

   private static final String ASSIGN_PATTERN = "^\\s*(.+?)\\s*=\\s*(.+?)\\s*",
                               SECTION_PATTERN = "^\\[(.+?)\\]";

   private static Map<String, Map<String, String>> mConfigMap =
              new HashMap<String, Map<String, String>>();

   public static boolean isInitialized() {
      return !mConfigMap.isEmpty();
   }

   public static void loadConfig(String propFile) {
      //Initialize and prepare File Scanner
      Scanner propReader = null;

      try { propReader = new Scanner(new File(propFile)); }
      catch (java.io.FileNotFoundException fileErr) {
         System.err.printf("Could not find file '%s'\n", propFile);
      }

      //Parse configuration file
      String sectionName = "", configLine = "";
      Matcher assignMatch = null, sectionMatch = null;
      Pattern assignment = Pattern.compile(ASSIGN_PATTERN),
              section    = Pattern.compile(SECTION_PATTERN);

      while (propReader.hasNextLine()) {
         configLine = propReader.nextLine();

         sectionMatch = section.matcher(configLine);
         if (sectionMatch.matches()) {
            sectionName = sectionMatch.group(1);
            mConfigMap.put(sectionName, new HashMap<String, String>());
            continue;
         }

         assignMatch = assignment.matcher(configLine);
         if (assignMatch.matches()) {
            mConfigMap.get(sectionName).put(assignMatch.group(1), assignMatch.group(2));
         }
      }
   }

   private static String getVal(String section, String prop) {
      if (mConfigMap.containsKey(section)) {
         Map<String, String> propMap = mConfigMap.get(section);

         if (propMap.containsKey(prop)) {
            return propMap.get(prop);
         }
      }

      return null;
   }

   public static String getString(String section, String prop) {
      return getVal(section, prop);
   }

   public static Boolean getBoolean(String section, String prop) {
      String val = getVal(section, prop);

      if (val != null) { return Boolean.valueOf(val); }
      return null;
   }

   public static Integer getInt(String section, String prop) {
      String val = getVal(section, prop);

      if (val != null) { return Integer.valueOf(val); }
      return null;
   }

   public static Float getFloat(String section, String prop) {
      String val = getVal(section, prop);

      if (val != null) { return Float.valueOf(val); }
      return null;
   }
}
