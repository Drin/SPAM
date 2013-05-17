package com.drin.java.ontology;

import com.drin.java.clustering.FastCluster;

import com.drin.java.ontology.FastOntologyTerm;
import com.drin.java.ontology.OntologyParser;

import java.io.File;
import java.util.Scanner;

import java.util.Map;
import java.util.Set;

import java.util.HashMap;
import java.util.HashSet;

public class FastOntology {
   public static String[][] mIsoMeta;
   private FastOntologyTerm mRoot;
   private Map<String, Set<String>> mTableColumns, mColumnPartitions;

   public FastOntology() {
      mRoot = null;

      mTableColumns = new HashMap<String, Set<String>>();
      mColumnPartitions = new HashMap<String, Set<String>>();
   }

   public FastOntologyTerm getRoot() {
      return mRoot;
   }

   @Override
   public String toString() {
      return FastOntology.printOntology(mRoot, "root", "");
   }

   public Map<String, Set<String>> getTableColumns() {
      return mTableColumns;
   }

   public Map<String, Set<String>> getColumnPartitions() {
      return mColumnPartitions;
   }

   public boolean addData(FastCluster element) {
      return mRoot.addData(element);
   }

   public void addFastTerm(FastOntologyTerm newTerm) {
      if (!mTableColumns.containsKey(newTerm.getTableName())) {
         mTableColumns.put(newTerm.getTableName(), new HashSet<String>());
      }
      mTableColumns.get(newTerm.getTableName()).add(newTerm.getColName());

      for (String partitionVal : newTerm.getPartitions().keySet()) {
         if (!mColumnPartitions.containsKey(newTerm.getColName())) {
            mColumnPartitions.put(newTerm.getColName(), new HashSet<String>());
         }

         mColumnPartitions.get(newTerm.getColName()).add(partitionVal);
      }

      if (mRoot != null) {
         FastOntology.addFastTerm(mRoot, newTerm);
      }

      else { mRoot = new FastOntologyTerm(newTerm); }
   }

   private static void addFastTerm(FastOntologyTerm root, FastOntologyTerm newTerm) {
      Map<String, FastOntologyTerm> partitionMap = root.getPartitions();

      for (Map.Entry<String, FastOntologyTerm> partition : partitionMap.entrySet()) {
         if (partition.getValue() == null) {
            partition.setValue(new FastOntologyTerm(newTerm));
         }

         else { FastOntology.addFastTerm(partition.getValue(), newTerm); }
      }
   }

   private static void addTerm(FastOntologyTerm root, FastOntologyTerm newTerm) {
      Map<String, FastOntologyTerm> partitionMap = root.getPartitions();

      for (Map.Entry<String, FastOntologyTerm> partition : partitionMap.entrySet()) {
         if (partition.getValue() == null) {
            partition.setValue(new FastOntologyTerm(newTerm));
         }

         else { FastOntology.addTerm(partition.getValue(), newTerm); }
      }
   }

   private static String printOntology(FastOntologyTerm term, String partitionName, String prefix) {
      String ontologyStr = String.format("%s%s:\n", prefix, partitionName);

      if (term != null) {
         if (term.getData() != null) {
            for (FastCluster element : term.getData()) {
               ontologyStr += String.format("%s%s,", prefix + "   ", element.getID());
               for (int metaNdx = 0; metaNdx < mIsoMeta[element.getID()].length; metaNdx++) {
                  ontologyStr += String.format("%s, ", mIsoMeta[element.getID()][metaNdx]);
               }

               ontologyStr += "\n";
            }

            ontologyStr += "\n";
         }

         if (term.getPartitions() != null) {
            for (Map.Entry<String, FastOntologyTerm> feature : term.getPartitions().entrySet()) {
               ontologyStr += FastOntology.printOntology(feature.getValue(),
                                                         feature.getKey(),
                                                         prefix + "   ");
            }
         }
      }

      return ontologyStr;
   }

   public String printClusters() {
      return FastOntology.printClusters(mRoot, "root", "");
   }

   public static String printClusters(FastOntologyTerm term, String partitionName, String prefix) {
      if (term == null) { return ""; }

      String ontologyStr = String.format("%s%s:\n", prefix, partitionName);

      if (term.getClusters() != null) {
         for (FastCluster element : term.getClusters()) {
            //ontologyStr += element.prettyPrint(prefix + "   ");
         }

         ontologyStr += "\n";
      }

      if (term.getPartitions() != null) {
         for (Map.Entry<String, FastOntologyTerm> feature : term.getPartitions().entrySet()) {
            ontologyStr += FastOntology.printClusters(feature.getValue(), feature.getKey(), prefix + "   ");
         }
      }

      return ontologyStr;
   }

   public static Ontology constructOntology(String ontologyStr) {
      Ontology ont = new Ontology();
      OntologyParser parser = new OntologyParser();
      Scanner termScanner = new Scanner(ontologyStr);
      termScanner.useDelimiter("\n");

      while (termScanner.hasNextLine()) {
         String term = termScanner.nextLine();
   
         if (parser.matchString(term)) { ont.addTerm(parser.getTerm()); }
      }

      termScanner.close();

      return ont;
   }

   public static Ontology createOntology(String ontologyStr) {
      if (ontologyStr == null) { return null; }

      Ontology ont = new Ontology();
      OntologyParser parser = new OntologyParser();
      Scanner termScanner = new Scanner(ontologyStr);
      termScanner.useDelimiter("\n");

      while (termScanner.hasNextLine()) {
         String term = termScanner.nextLine();
   
         if (parser.matchString(term)) { ont.addTerm(parser.getTerm()); }
      }
      
      termScanner.close();

      return ont;
   }

   public static FastOntology createFastOntology(File ontologyFile) {
      FastOntology ont = new FastOntology();
      OntologyParser parser = new OntologyParser();
      Scanner termScanner = null;

      try {
         termScanner = new Scanner(ontologyFile).useDelimiter("\n");
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.printf("Could not find file '%s'\n", ontologyFile.getName());
         fileErr.printStackTrace();
      }

      while (termScanner.hasNextLine()) {
         String term = termScanner.nextLine();
   
         if (parser.matchString(term)) { ont.addFastTerm(parser.getFastTerm()); }
      }

      return ont;
   }
}
