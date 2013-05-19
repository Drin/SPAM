package com.drin.java.ontology;

import com.drin.java.clustering.FastCluster;

import com.drin.java.ontology.FastOntologyTerm;
import com.drin.java.ontology.OntologyParser;

import java.io.File;
import java.util.Scanner;

import java.util.Map;

public class FastOntology {
   private FastOntologyTerm mRoot;
   private String[] mColumns, mPartitions;
   private byte mColumnTail, mPartitionTail;

   public FastOntology() {
      mRoot = null;
      
      mColumns = new String[2];
      mPartitions = new String[2];

      mColumnTail = mPartitionTail = 0;
   }
   
   public FastOntology(FastOntology oldOnt) {
      this();
      
      mColumns = oldOnt.mColumns;
      mPartitions = oldOnt.mPartitions;
      
      mColumnTail = oldOnt.mColumnTail;
      mPartitionTail = oldOnt.mPartitionTail;
      
      mRoot = new FastOntologyTerm(oldOnt.mRoot);
   }

   public FastOntologyTerm getRoot() {
      return mRoot;
   }

   public int size() { return mRoot.size(); }

   @Override
   public String toString() {
      return FastOntology.printOntology(mRoot, "root", "");
   }

   public byte getNumCols() { return mColumnTail; }
   public String[] getColumns() {
      return mColumns;
   }
   public void addColumn(String colName) {
      if (mColumns.length == mColumnTail) {
         String tmpArr[] = new String[mColumnTail * 2];
         
         for (int tmpNdx = 0; tmpNdx < mColumnTail; tmpNdx++) {
            tmpArr[tmpNdx] = mColumns[tmpNdx];
         }
         
         mColumns = tmpArr;
      }
      
      mColumns[mColumnTail++] = colName;
   }

   public byte getNumPartitions() { return mPartitionTail; }
   public String[] getPartitions() {
      return mPartitions;
   }
   
   public void addPartition(String partition) {
      if (mPartitions.length == mPartitionTail) {
         String tmpArr[] = new String[mPartitionTail * 2];
         
         for (int tmpNdx = 0; tmpNdx < mPartitionTail; tmpNdx++) {
            tmpArr[tmpNdx] = mPartitions[tmpNdx];
         }
         
         mPartitions = tmpArr;
      }
      
      mPartitions[mPartitionTail++] = partition;
   }

   public boolean addData(FastCluster element) {
      return mRoot.addData(element, (byte) 0);
   }

   public void addFastTerm(FastOntologyTerm newTerm) {
      addColumn(newTerm.getColName());

      for (String partitionVal : newTerm.getPartitions().keySet()) {
         addPartition(partitionVal);
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

         else {
            FastOntology.addFastTerm(partition.getValue(), newTerm);
         }
      }
   }

   private static String printOntology(FastOntologyTerm term, String partitionName, String prefix) {
      String ontologyStr = String.format("%s%s:\n", prefix, partitionName);

      if (term != null) {
         if (term.getData() != null) {
            for (FastCluster element : term.getData()) {
               ontologyStr += String.format("%s%s,", prefix + "   ", element.getID());
            }

            ontologyStr += "\n";
         }
         
         if (term.getClusters() != null) {
            for (FastCluster element : term.getClusters()) {
               ontologyStr += String.format("%s\n%s", prefix + "   ", element);
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
