package com.drin.java.programs;

import com.drin.java.parsers.FeatureParser;
import com.drin.java.types.FeatureTree;

import java.io.File;
import java.util.Scanner;

public class Test_FeatureTree {
   private static final String DELIMITER = System.getProperty("line.separator");

   public static void main(String[] args) {
      FeatureTree tree = new FeatureTree();
      FeatureParser parser = new FeatureParser();
      Scanner featureScanner = null;

      try {
         featureScanner = new Scanner(new File(args[0])).useDelimiter(DELIMITER);
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.printf("Could not find file '%s'\n", args[0]);
         fileErr.printStackTrace();
      }

      System.out.printf("Scanning for patterns matching '%s'\n", parser.getPattern());

      while (featureScanner.hasNext(parser.getPattern())) {
         String feature = featureScanner.next(parser.getPattern());

         System.out.printf("parsed feature '%s'\n", feature);

         if (parser.matchString(feature)) {
            System.out.printf("feature name: '%s'\n", parser.getFeatureName());
            tree.addFeature(parser.getFeature());
         }
      }

      while (featureScanner.hasNextLine()) {
         String garbage = featureScanner.nextLine();
         System.out.printf("unparsed garbage: '%s'\n", garbage);

         System.out.printf("does '%s' match the regex? %s\n", garbage, parser.matchString(garbage));
      }

      System.out.printf("Constructed feature tree:\n%s", tree.toString());
   }
}
