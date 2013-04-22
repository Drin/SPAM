package com.drin.java.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;

public class Configuration {
   public static final String DEBUG_KEY     = "debug",
                              TRANSFORM_KEY = "transform",
                              REGION_KEY    = "regions",
                              CLUSTER_KEY   = "cluster",
                              ISOLATE_KEY   = "isolate",
                              ITSREGION_KEY = "itsregion",
                              PYROPRINT_KEY = "pyroprint",
                              METRIC_KEY    = "metrics",
                              ALPHA_KEY     = "alpha",
                              BETA_KEY      = "beta",
                              LENGTH_KEY    = "pyroLength",
                              ONT_KEY       = "ontology";

   private static final String DEFAULT_CONFIG_DIR  = "configs",
                               DEFAULT_CONFIG_FILE = "props-standard.yml",
                               FILE_SEP = System.getProperty("file.separator");

   private static final String CONFIG_OPTION_PATTERN = "\\s*" +
                                                       "([a-zA-Z0-9+.{}-]*)" +
                                                       "\\s*" +
                                                       ":" +
                                                       "\\s*" +
                                                       "([a-zA-Z0-9+.{}-]*)";

   private static final int NAME_NDX   = 1,
                            VAL_NDX    = 2;

   private static Pattern mRegexPattern = Pattern.compile(CONFIG_OPTION_PATTERN);
   private static Matcher mRegexMatch = null;

   private static Configuration mConfig = null;

   /*
    * Configuration Variables
    */
   private Map<String, Object> mAttributeMap;

   private Configuration(Map<String, Object> attrMap) {
      mAttributeMap = attrMap;
   }

   public static Configuration getConfig() {
      return mConfig;
   }

   public static Configuration loadConfig() {
      return Configuration.loadConfig(new File(String.format(
         "%s%s%s%s%s", System.getProperty("user.dir"), FILE_SEP,
         DEFAULT_CONFIG_DIR, FILE_SEP, DEFAULT_CONFIG_FILE
      )));
   }

   public static Configuration loadConfig(String configStr) {
      Scanner configScanner = new Scanner(configStr);
      mConfig = new Configuration(Configuration.parseConfig(configScanner));

      configScanner.close();

      return mConfig;
   }

   public static Configuration loadConfig(File configFile) {
      Scanner configScanner = null;

      try {
         configScanner = new Scanner(configFile);
         mConfig = new Configuration(Configuration.parseConfig(configScanner));

         configScanner.close();
      }
      catch(java.io.FileNotFoundException err) {
         err.printStackTrace();
         try {
            FileWriter tmpWriter = new FileWriter(configFile);
            tmpWriter.write("test test!");
            tmpWriter.close();
         }
         catch(java.io.IOException ioErr) {
            ioErr.printStackTrace();
         }
      }

      return mConfig;
   }

   public static void createDefaultConfig() {
      File configFile = new File(DEFAULT_CONFIG_FILE);

      if (!configFile.exists()) {
         try {
            FileWriter configWriter = new FileWriter(configFile);
            configWriter.write(getDefaultConfigContent());
            configWriter.close();
         }
         catch(java.io.IOException ioErr) {
            ioErr.printStackTrace();
         }
      }
   }

   private static String getDefaultConfigContent() {
      String paramStr = "";

      String regDefault = String.format("pyroLength: %s\nalpha: %s\nbeta: %s\n",
                                        "93", "99.5", "99");
      String metrics = String.format("metrics: {\ncluster: %s\nisolate: %s\nitsregion: %s\npyroprint: %s\n}\n",
                                     "com.drin.java.metrics.ClusterAverageMetric",
                                     "com.drin.java.metrics.IsolateAverageMetric",
                                     "com.drin.java.metrics.ITSRegionAverageMetric",
                                     "com.drin.java.metrics.PyroprintUnstablePearsonMetric");

      paramStr += "debug: true\n";
      paramStr += "transform: true\n";

      paramStr += "regions: {\n";
      paramStr += "16-23: {\n";
      paramStr += regDefault;
      paramStr += "}\n";
      paramStr += "23-5: {\n";
      paramStr += regDefault;
      paramStr += "}\n";
      paramStr += "}\n";
      paramStr += metrics;

      return paramStr;
   }


   private static Map<String, Object> parseConfig(Scanner configScanner) {
      Map<String, Object> attrMap = new HashMap<String, Object>();
      String configLine = null, attrName = null, attrVal = null;

      while (configScanner.hasNextLine()) {
         configLine = configScanner.nextLine();

         if (configLine.replace(" ", "").equals("}")) { return attrMap; }

         mRegexMatch = mRegexPattern.matcher(configLine);

         if (mRegexMatch.matches()) {
            attrName = mRegexMatch.group(NAME_NDX);
            attrVal  = mRegexMatch.group(VAL_NDX);

            if (attrVal.startsWith("{")) {
               Map<String, Object> subMap = Configuration.parseConfig(configScanner);
               attrMap.put(attrName, subMap);
            }
            else { attrMap.put(attrName, attrVal); }
         }
      }

      return attrMap;
   }

   /*
    * Setters
    */
   public void setAttr(String attrName, String attrVal) {
      mAttributeMap.put(attrName, attrVal);
   }

   @SuppressWarnings("unchecked")
   public void setRegionAttr(String regionName, String attrName, String attrVal) {
      Map<String, Object> regionMap = (Map<String, Object>) mAttributeMap.get(REGION_KEY);
      Map<String, Object> regionSubMap = (Map<String, Object>) regionMap.get(regionName);

      regionSubMap.put(attrName, attrVal);
   }

   @SuppressWarnings("unchecked")
   public void setMetric(String attrName, String attrVal) {
      Map<String, Object> metricMap = (Map<String, Object>) mAttributeMap.get(METRIC_KEY);
      metricMap.put(attrName, attrVal);
   }

   /*
    * Getters
    */
   public Map<String, Object> getAttributes() {
      return mAttributeMap;
   }

   public String getAttr(String attrName) {
      Object mapping = mAttributeMap.get(attrName);
      if (mapping != null && !(mapping instanceof Map<?, ?>)) {
         return String.valueOf(mapping);
      }

      return null;
   }

   @SuppressWarnings("unchecked")
   public String getRegionAttr(String regionName, String attrName) {
      Object mapping = mAttributeMap.get(REGION_KEY);
      if (mapping != null && mapping instanceof Map<?, ?>) {
         Object regionMapping = ((Map<String, Object>) mapping).get(regionName);

         if (regionMapping != null && regionMapping instanceof Map<?, ?>) {
            Map<String, String> regionMap = (Map<String, String>) regionMapping;
            return regionMap.get(attrName);
         }
      }

      return null;
   }

   public String getMetric(String attrName) {
      return getSubMapping(METRIC_KEY, attrName);
   }

   @SuppressWarnings("unchecked")
   public String getSubMapping(String mapName, String attrName) {
      Object mapping = mAttributeMap.get(mapName);
      if (mapping != null && mapping instanceof Map<?, ?>) {
         return ((Map<String, String>) mapping).get(attrName);
      }
      
      return null;
   }

   /*
    * Utility Methods
    */
   public String toString() {
      return Configuration.stringifyAttrMap(mAttributeMap, "");
   }

   @SuppressWarnings("unchecked")
   private static String stringifyAttrMap(Map<String, Object> attrMap, String indent) {
      String stringifiedMap = "";

      for (Map.Entry<String, Object> entry : attrMap.entrySet()) {
         if (entry.getValue() instanceof Map<?, ?>) {
            stringifiedMap += String.format("%s%s => \n%s\n", indent, entry.getKey(),
                                            stringifyAttrMap((Map<String, Object>) entry.getValue(),
                                                             indent + "\t"));
         }
         else {
            stringifiedMap += String.format("%s%s => %s\n", indent,
                                            entry.getKey(), entry.getValue());
         }
      }

      return stringifiedMap;
   }
}
