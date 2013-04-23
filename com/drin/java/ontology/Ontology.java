package com.drin.java.ontology;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;
import com.drin.java.metrics.ClusterAverageMetric;

import com.drin.java.ontology.OntologyTerm;
import com.drin.java.ontology.OntologyParser;

import com.drin.java.util.Logger;

import java.io.File;
import java.util.Scanner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Ontology {
   private OntologyTerm mRoot;
   private Map<String, Set<String>> mTableColumns, mColumnPartitions;

   public Ontology() {
      mRoot = null;

      mTableColumns = new HashMap<String, Set<String>>();
      mColumnPartitions = new HashMap<String, Set<String>>();
   }

   public OntologyTerm getRoot() {
      return mRoot;
   }

   @Override
   public String toString() {
      return Ontology.printOntology(mRoot, "root", "");
   }

   public Map<String, Set<String>> getTableColumns() {
      return mTableColumns;
   }

   public Map<String, Set<String>> getColumnPartitions() {
      return mColumnPartitions;
   }

   public boolean addData(Cluster element) {
      return mRoot.addData(element);
   }

   public void addTerm(OntologyTerm newTerm) {
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
         Ontology.addTerm(mRoot, newTerm);
      }

      else { mRoot = new OntologyTerm(newTerm); }
   }

   private static void addTerm(OntologyTerm root, OntologyTerm newTerm) {
      Map<String, OntologyTerm> partitionMap = root.getPartitions();

      for (Map.Entry<String, OntologyTerm> partition : partitionMap.entrySet()) {
         if (partition.getValue() == null) {
            partition.setValue(new OntologyTerm(newTerm));
         }

         else { Ontology.addTerm(partition.getValue(), newTerm); }
      }
   }

   private static String printOntology(OntologyTerm term, String partitionName, String prefix) {
      String ontologyStr = String.format("%s%s:\n", prefix, partitionName);

      if (term != null) {
         if (term.getData() != null) {
            for (Cluster element : term.getData()) {
               ontologyStr += String.format("%s%s,", prefix + "   ", element.getName());
            }

            ontologyStr += "\n";
         }

         if (term.getPartitions() != null) {
            for (Map.Entry<String, OntologyTerm> feature : term.getPartitions().entrySet()) {
               ontologyStr += Ontology.printOntology(feature.getValue(),
                                                     feature.getKey(),
                                                     prefix + "   ");
            }
         }
      }

      return ontologyStr;
   }

   public String printClusters() {
      return Ontology.printClusters(mRoot, "root", "");
   }

   public static String printClusters(OntologyTerm term, String partitionName, String prefix) {
      if (term == null) { return ""; }

      String ontologyStr = String.format("%s%s:\n", prefix, partitionName);

      if (term.getClusters() != null) {
         for (Cluster element : term.getClusters()) {
            ontologyStr += element.prettyPrint(prefix + "   ");
         }

         ontologyStr += "\n";
      }

      if (term.getPartitions() != null) {
         for (Map.Entry<String, OntologyTerm> feature : term.getPartitions().entrySet()) {
            ontologyStr += Ontology.printClusters(feature.getValue(), feature.getKey(), prefix + "   ");
         }
      }

      return ontologyStr;
   }

   public static Ontology constructOntology(String ontologyStr) {
      Ontology ont = new Ontology();
      OntologyParser parser = new OntologyParser();
      Scanner termScanner = new Scanner(ontologyStr).useDelimiter("\n");

      while (termScanner.hasNextLine()) {
         String term = termScanner.nextLine();
   
         if (parser.matchString(term)) { ont.addTerm(parser.getTerm()); }
      }

      return ont;
   }

   public static Ontology createOntology(String ontologyStr) {
      if (ontologyStr == null) { return null; }

      Ontology ont = new Ontology();
      OntologyParser parser = new OntologyParser();
      Scanner termScanner = new Scanner(ontologyStr).useDelimiter("\n");

      while (termScanner.hasNextLine()) {
         String term = termScanner.nextLine();
   
         if (parser.matchString(term)) { ont.addTerm(parser.getTerm()); }
      }

      return ont;
   }

   public static Ontology createOntology(File ontologyFile) {
      Ontology ont = new Ontology();
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
   
         if (parser.matchString(term)) { ont.addTerm(parser.getTerm()); }
      }

      return ont;
   }
}
