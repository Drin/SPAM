package spam.util;

import java.io.File;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class Configuration {
   private static final int PROPERTY_NAME = 0,
                            PROPERTY_VAL = 1;

   private static Map<String, Object> mConfigMap = new HashMap<String, Object>();

   public static boolean getBoolean(String propertyName) throws InvalidPropertyException {
      if (mConfigMap.containsKey(propertyName)) {
         return Boolean.valueOf(mConfigMap.get(propertyName));
      }

      throw new InvalidPropertyException(propertyName);
   }

   public static void loadConfiguration(File propertiesFile) {
      Scanner propertyReader = new Scanner(propertiesFile);

      while (propertyReader.hasNextLine()) {
         String[] property = propertyReader.nextLine().split("=");

         validateProperty(property);

         mConfigMap.put(property[PROPERTY_NAME], property[PROPERTY_VAL]);
      }
   }

   private static void validateProperty(String[] property) {
      PROP_STATUS status = PROP_STATUS.VALID;

      if (property.length != 2) {
         status = PROP_STATUS.INV_LEN;
      }

      switch (status) {
         case PROP_STATUS.VALID:
            break;

         case PROP_STATUS.INV_LEN:
            System.err.printf("Invalid property format: '%s'\n", property);
            System.exit(1);

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
