package com.drin.java.ontology;

import com.drin.java.clustering.Cluster;
import com.drin.java.ontology.OntologyTerm;
import com.drin.java.ontology.OntologyParser;

import java.io.File;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Ontology {
   private static final boolean DEBUG = false;
   private OntologyTerm mRoot;

   public Ontology() {
      mRoot = null;
   }

   public OntologyTerm getRoot() {
      return mRoot;
   }

   @Override
   public String toString() {
      return Ontology.printOntology(mRoot, "root", "");
   }

   public void addData(Cluster<?> element) {
      mRoot.addData(element);
      if (DEBUG) { System.out.printf("added element: '%s' to Ontology \n", element.getName()); }
   }

   public void addTerm(OntologyTerm newTerm) {
      if (DEBUG) {
         System.out.printf("adding new term:\n\t%s\n", newTerm.toString());
      }

      if (mRoot != null) {
         Ontology.addTerm(mRoot, newTerm);
      }

      else { mRoot = new OntologyTerm(newTerm); }
   }

   public String printClusters() {
      return Ontology.printClusters(mRoot, "root", "");
   }

   public static String printClusters(OntologyTerm term, String partitionName, String prefix) {
      if (term == null) { return ""; }

      String ontologyStr = String.format("%s%s:\n", prefix, partitionName);

      if (term.getClusters() != null) {
         for (Cluster<?> element : term.getClusters()) {
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
      if (term == null) { return ""; }

      String ontologyStr = String.format("%s%s:\n", prefix, partitionName);

      if (term.getData() != null) {
         for (Cluster<?> element : term.getData()) {
            ontologyStr += String.format("%s%s,", prefix + "   ", element.getName());
         }

         ontologyStr += "\n";
      }

      if (term.getPartitions() != null) {
         for (Map.Entry<String, OntologyTerm> feature : term.getPartitions().entrySet()) {
            ontologyStr += Ontology.printOntology(feature.getValue(), feature.getKey(), prefix + "   ");
         }
      }

      return ontologyStr;
   }

   public static Ontology constructOrganization(String fileName) {
      if (fileName == null) { return null; }

      Ontology ont = new Ontology();
      OntologyParser parser = new OntologyParser();
      Scanner termScanner = null;

      try {
         termScanner = new Scanner(new File(fileName)).useDelimiter("\n");
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.printf("Could not find file '%s'\n", fileName);
         fileErr.printStackTrace();
      }

      while (termScanner.hasNextLine()) {
         String term = termScanner.nextLine();
   
         if (parser.matchString(term)) { ont.addTerm(parser.getTerm()); }
      }

      return ont;
   }

   public static void main(String[] args) {
      Ontology ont = Ontology.constructOrganization(args[0]);
      System.out.println("ontology :\n" + ont);
   }
}
