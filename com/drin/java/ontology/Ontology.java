package com.drin.java.ontology;

import com.drin.java.clustering.Cluster;

import com.drin.java.ontology.OntologyTerm;
import com.drin.java.ontology.OntologyParser;

import java.io.File;
import java.util.Scanner;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Ontology {
   private String mName;
   private OntologyTerm mRoot;
   private List<String> mColumns;

   public Ontology() {
      mRoot = null;
      
      mColumns = new ArrayList<String>(2);
   }
   
   public Ontology(Ontology oldOnt) {
      this();
      
      mColumns = new ArrayList<String>(oldOnt.mColumns);
      
      mRoot = new OntologyTerm(oldOnt.mRoot);
      mName = oldOnt.mName;
   }

   public OntologyTerm getRoot() {
      return mRoot;
   }

   public int size() { return mRoot.size(); }
   public void setName(String name) { mName = name; }
   public String getName() { return mName; }

   @Override
   public String toString() {
      return Ontology.printOntology(mRoot, "root", "");
   }

   public int getNumCols() { return mColumns.size(); }
   public void addColumn(String colName) { mColumns.add(colName); }
   public List<String> getColumns() { return mColumns; }

   public boolean addData(Cluster element) { return mRoot.addData(element, (byte) 0); }

   public void addTerm(OntologyTerm newTerm) {
      addColumn(newTerm.getColName());

      if (mRoot != null) { Ontology.addTerm(mRoot, newTerm); }
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
               ontologyStr += String.format("%s%s,", prefix + "   ", element.getId());
            }

            ontologyStr += "\n";
         }
         
         if (term.getClusters() != null) {
            for (Cluster element : term.getClusters()) {
               ontologyStr += String.format("%s\n%s", prefix + "   ", element);
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

   public static Ontology createOntology(File ontologyFile) {
      Ontology ont = new Ontology();
      OntologyParser parser = new OntologyParser();
      Scanner termScanner = null;

      ont.setName(ontologyFile.getName());

      try {
         termScanner = new Scanner(ontologyFile).useDelimiter("\n");
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.printf("Could not find file '%s'\n", ontologyFile.getName());
         fileErr.printStackTrace();
      }

      while (termScanner.hasNextLine()) {
         String term = termScanner.nextLine();
   
         if (parser.matchString(term)) {
            ont.addTerm(parser.getTerm(ont.getColumns()));
         }

         System.out.println(ont.size());
      }

      return ont;
   }
}
