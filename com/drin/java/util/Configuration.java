package com.drin.java.util;

import com.drin.java.util.InvalidPropertyException;

import java.io.File;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Configuration {
   private static final String KEY_VAL_DELIM = "=";
   private static final int PROP_KEY = 0,
                            PROP_VAL = 1;

   private static Map<String, Object> mConfigMap = new HashMap<String, Object>();

   public static void loadConfiguration(File propFile) throws InvalidPropertyException {
      Scanner propReader = null;

      try { propReader = new Scanner(propFile); }
      catch (java.io.FileNotFoundException fileErr) {
         System.err.printf("Could not find file '%s'\n", propFile);
      }

      while (propReader.hasNextLine()) {
         String[] property = propReader.nextLine().split(KEY_VAL_DELIM);

         if (property.length < 2 || property[0].startsWith("#")) { continue; }

         validateProp(property);

         mConfigMap.put(property[PROP_KEY], property[PROP_VAL]);
      }
   }

   public static String getString(String propName) {
      if (mConfigMap.containsKey(propName)) {
         return String.valueOf(mConfigMap.get(propName));
      }

      return null;
   }

   public static Boolean getBoolean(String propName) {
      if (mConfigMap.containsKey(propName)) {
         return Boolean.valueOf(String.valueOf(mConfigMap.get(propName)));
      }

      return null;
   }

   public static Integer getInt(String propName) {
      if (mConfigMap.containsKey(propName)) {
         return Integer.valueOf(String.valueOf(mConfigMap.get(propName)));
      }

      return null;
   }

   private static void validateProp(String[] property) throws InvalidPropertyException {
      PROP_STATUS status = PROP_STATUS.VALID;

      if (property.length != 2) {
         status = PROP_STATUS.INV_LEN;
      }

      switch (status) {
         case VALID:
            return;

         case INV_LEN:
            String err = String.format("Invalid property format: should be " +
                                       "<name>%s<value>", KEY_VAL_DELIM);
            throw new InvalidPropertyException(err);

         default:
            throw new InvalidPropertyException("Unknown property");
      }
   }

   private static void debug() {
      for (Map.Entry<String, Object> prop : mConfigMap.entrySet()) {
         System.out.printf("%s => %s\n", prop.getKey(), prop.getValue());
      }
   }

   private enum PROP_STATUS {
      VALID, INV_LEN, INV_VAL;
   }

   public static void main(String[] args) {
      try {
         if (args.length > 0) {
            Configuration.loadConfiguration(new File(args[0]));
         }
         else { Configuration.loadConfiguration(new File("props-standard.cfg")); }

         Configuration.debug();
      }
      catch(InvalidPropertyException err) {
         System.out.println(err);
      }
   }
}
