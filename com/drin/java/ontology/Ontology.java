package com.drin.java.ontology;

import com.drin.java.clustering.Cluster;

import com.drin.java.ontology.OntologyTerm;
import com.drin.java.ontology.OntologyParser;
import com.drin.java.ontology.OntologyParser.TermContainer;

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

   public void addTerm(Map<String, Set<String>> partitions, OntologyTerm newTerm) {
      addColumn(newTerm.getColName());

      System.out.println("adding term...");

      if (mRoot != null) {
         System.out.println("mRoot not null...");
         Ontology.addTerm(partitions, mRoot, newTerm);
      }
      else {
         Map<String, OntologyTerm> termPartitions = new LinkedHashMap<String, OntologyTerm>();

         for (String termPartition : partitions.keySet()) {
            termPartitions.put(termPartition, null);
         }

         mRoot = new OntologyTerm(newTerm);
         mRoot.setPartitions(termPartitions);
      }
   }

   private static void addTerm(Map<String, Set<String>> partitionLookup,
                               OntologyTerm root, OntologyTerm newTerm) {
      Map<String, OntologyTerm> partitionMap = root.getPartitions();

      for (Map.Entry<String, OntologyTerm> partition : partitionMap.entrySet()) {
         Set<String> usefulPartitions = partitionLookup.get(partition.getKey());

         if (usefulPartitions == null || usefulPartitions.isEmpty()) { continue; }

         if (partition.getValue() == null) {
            OntologyTerm newTermCopy = new OntologyTerm(newTerm);
            Map<String, OntologyTerm> termPartitions = new LinkedHashMap<String, OntologyTerm>();
            for (String lookupVal : usefulPartitions) {
               termPartitions.put(partition.getKey() + ":" + lookupVal, null);
            }

            newTermCopy.setPartitions(termPartitions);
            partition.setValue(newTermCopy);
         }
         else { Ontology.addTerm(partitionLookup, partition.getValue(), newTerm); }
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
            TermContainer newTerm = parser.getTerm(
               new ArrayList<String>(ont.getColumns())
            );

            ont.addTerm(newTerm.mPartitionLookup, newTerm.mTerm);
         }

         System.out.println("size: " + ont.size());
         //System.out.println(ont);
      }

      return ont;
   }
}
