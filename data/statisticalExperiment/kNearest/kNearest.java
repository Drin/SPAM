package kNearest;

import java.util.ArrayList;
import java.util.HashMap;

public class kNearest {
   private static kNearest driver = new kNearest();
   private static final int CLASS_LENGTH = 1;
   
   public static void main(String[] args) {
      HashMap<String, String> dataClasses = new HashMap<String, String>();
      HashMap<String, HashMap<String, Double>> dataMap = driver.parseCSV(args[0]);
      int threshold = Integer.parseInt(args[1]);
      
      for (String strain : dataMap.keySet()) {
         dataClasses.put(strain, driver.classify(strain, threshold, dataMap));
      }

      ArrayList<String> sourceStrains = new ArrayList<String>();
      for (String strain : dataClasses.keySet()) {
         driver.insert(sourceStrains, strain);
      }

      for (String strain : sourceStrains) {
         System.out.println("strain " + strain +
          " classified as strain " + dataClasses.get(strain));
      }
   }

   public HashMap<String, HashMap<String, Double>> parseCSV(String fileName) {
      CsvParser parser = new CsvParser(fileName);
      return parser.extractData();
   }

   public String classify(String keyStrain, int threshold,
    HashMap<String, HashMap<String, Double>> dataMap) {
      ArrayList<Pair> tuples = new ArrayList<Pair>();

      for (String strain : dataMap.get(keyStrain).keySet()) {
         Pair tmpPair = new Pair(strain, dataMap.get(keyStrain).get(strain));
         insert(tuples, tmpPair);
      }

      for (String strain : dataMap.keySet()) {
         if (!dataMap.get(keyStrain).containsKey(strain) && !strain.equals(keyStrain)) {
            Pair tmpPair = new Pair(strain, dataMap.get(strain).get(keyStrain));
            insert(tuples, tmpPair);
         }
      }

      return plurality(tuples, threshold);
   }

   //can be abstracted using comparable interface for Pair
   public void insert(ArrayList<Pair> tuples, Pair newPair) {
      //post-mortem loop for insertion sort
      int tupleNdx = 0;
      for (; tupleNdx < tuples.size() &&
       newPair.compareTo(tuples.get(tupleNdx)) <= 0; tupleNdx++)
         ;

      tuples.add(tupleNdx, newPair);
      //System.out.println("tuples: " + tuples);
   }

   //can be abstracted using comparable interface for Pair
   public void insert(ArrayList<String> tuples, String strain) {
      //post-mortem loop for insertion sort
      int tupleNdx = 0;
      for (; tupleNdx < tuples.size() &&
       strain.compareTo(tuples.get(tupleNdx)) <= 0; tupleNdx++)
         ;

      tuples.add(tupleNdx, strain);
      //System.out.println("tuples: " + tuples);
   }

   public String plurality(ArrayList<Pair> tuples, int threshold) {
      HashMap<String, Integer> pluralityMap = new HashMap<String, Integer>();

      for (int tupleNdx = 0; tupleNdx < threshold &&
       tupleNdx < tuples.size(); tupleNdx++) {
         //srcStrain represents a class, and I believe that a class is
         //represented by the first N characters of the strain name
         String strain = tuples.get(tupleNdx).strain();
         String srcStrain = strain.substring(0, CLASS_LENGTH);
         int pluralityVal = 1;
         //System.out.println("class for '" + strain + "' is '" + srcStrain + "'");

         if (pluralityMap.containsKey(srcStrain)) {
            pluralityVal = pluralityMap.get(srcStrain) + 1;
         }

         pluralityMap.put(srcStrain, pluralityVal);
         //System.out.println("placed " + srcStrain + ", " + pluralityVal + " into pluralityMap");

      }

      Pair commonStrain = new Pair("", 0);

      for (String srcStrain : pluralityMap.keySet()) {
         //System.out.println("commonStrain: " + commonStrain + " compared to currentStrain: " + srcStrain + ", " + pluralityMap.get(srcStrain));
         if (commonStrain.compareTo((double) pluralityMap.get(srcStrain)) < 0) {
            commonStrain = new Pair(srcStrain, pluralityMap.get(srcStrain));
         }
      }
      
      return commonStrain.strain();
   }

private class Pair {
   private String strain = null;
   private Double value = null;

   public Pair(String newStrain, Double newVal) {
      strain = newStrain;
      value = newVal;
   }

   public Pair(String newStrain, int newVal) {
      strain = newStrain;
      value = (double) newVal;
   }

   public double compareTo(Pair otherPair) {
      return value - otherPair.value;
   }
   
   public double compareTo(Double dbl) {
      return value - dbl;
   }

   public boolean equals(Pair otherPair) {
      return strain.equals(otherPair.strain);
   }

   public double getVal() {
      return value;
   }

   public void setVal(double val) {
      value = val;
   }

   public String strain() {
      return strain;
   }

   public String toString() {
      return strain + ", " + value;
   }
}
}
