package com.drin.java.util;

import java.io.File;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Configuration {
   private static final int PROP_KEY = 0,
                            PROP_VAL = 1;

   private static Map<String, Object> mConfigMap = new HashMap<String, Object>();

   public static void loadConfiguration(File propFile) {
      Scanner propReader = null;

      try { propReader = new Scanner(propFile); }
      catch (java.io.FileNotFoundException fileErr) {
         System.err.printf("Could not find file '%s'\n", propFile);
      }

      while (propReader.hasNextLine()) {
         String[] property = propReader.nextLine().split("=");

         validateProp(property);

         mConfigMap.put(property[PROP_KEY], property[PROP_VAL]);
      }
   }

   public static boolean getBoolean(String propName) {
      if (mConfigMap.containsKey(propName)) {
         return Boolean.valueOf(String.valueOf(mConfigMap.get(propName)));
      }
      //throw new InvalidPropertyException(propName);
      return false;
   }

   private static void validateProp(String[] property) {
      PROP_STATUS status = PROP_STATUS.VALID;

      if (property.length != 2) {
         status = PROP_STATUS.INV_LEN;
      }

      switch (status) {
         case VALID:
            break;

         case INV_LEN:
            System.err.printf("Invalid property format: should be <name>:<value>\n");
            System.exit(1);
            break;

         default:
            System.err.printf("Unknown property validation status\n");
            System.exit(1);
      }
   }

   private enum PROP_STATUS {
      VALID, INV_LEN, INV_VAL;
   }

   public class InvalidPropertyException extends Exception {
      public InvalidPropertyException(String property) {
         super(String.format("Invalid Property '%s'\n", property));
      }
   }
}
