package spam.dataParser;

import java.util.Map;
import java.util.HashMap;

import java.io.File;

public class ArgumentParser {
   private Map<File, FileSettings> dataFileMap = null;

   public class ArgumentParser() {
      super();
      dataFileMap = new HashMap<File, FileSettings>();
   }

   public void parseArg(String[] arguments) {
      /*
       * This will be the ultra awesome argument parser
      if (arguments.length < 2) {
         System.out.println("Usage: java hclustering -f <filename> -R <16s-23s|23s-5s> [-low <lowerThreshold>] "+
          "[-high <upperThreshold>] [-dist <single|average|complete|ward>]");
      }

         for (String arg : args) {
            if (arg.equals("-f")) {
            }
            else if (arg.equals("-R")) {
            }
            else if (arg.equals("-low")) {
            }
            else if (arg.equals("-high")) {
            }
            else if (arg.equals("-dist")) {
            }
            else {
               System.err.println("Invalid argument: " + arg);
            }
         }
      */
   }
}
