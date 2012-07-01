package com.drin.java.types;

import com.drin.java.types.FeatureNode;
import com.drin.java.parsers.FeatureParser;

import java.io.File;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class FeatureTree {
   private static final boolean DEBUG = false;
   private FeatureNode mRoot;

   public FeatureTree() {
      mRoot = null;
   }

   public FeatureNode getRoot() {
      return mRoot;
   }

   @Override
   public String toString() {
      return FeatureTree.printTree(mRoot, "root", "");
   }

   public void addData(Cluster element) {
      mRoot.addData(element);
      if (DEBUG) { System.out.printf("added element: '%s' to FeatureTree \n", element.getName()); }
   }

   public void addFeature(FeatureNode newFeature) {
      if (DEBUG) {
         System.out.printf("adding new feature:\n\t%s\n", newFeature.toString());
      }

      if (mRoot != null) {
         FeatureTree.addFeature(mRoot, newFeature);
      }

      else { mRoot = new FeatureNode(newFeature); }
   }

   public String printClusters() {
      return FeatureTree.printClusters(mRoot, "root", "");
   }

   public static String printClusters(FeatureNode node, String partitionName, String prefix) {
      if (node == null) { return ""; }

      String treeStr = String.format("%s%s:\n", prefix, partitionName);

      if (node.getClusters() != null) {
         for (Cluster element : node.getClusters()) {
            treeStr += element.prettyPrint(prefix + "   ");
         }

         treeStr += "\n";
      }

      if (node.getPartitions() != null) {
         for (Map.Entry<String, FeatureNode> feature : node.getPartitions().entrySet()) {
            treeStr += FeatureTree.printClusters(feature.getValue(), feature.getKey(), prefix + "   ");
         }
      }

      return treeStr;
   }

   private static void addFeature(FeatureNode root, FeatureNode newFeature) {
      Map<String, FeatureNode> partitionMap = root.getPartitions();

      for (Map.Entry<String, FeatureNode> partition : partitionMap.entrySet()) {
         if (partition.getValue() == null) {
            partition.setValue(new FeatureNode(newFeature));
         }

         else { FeatureTree.addFeature(partition.getValue(), newFeature); }
      }
   }

   private static String printTree(FeatureNode node, String partitionName, String prefix) {
      if (node == null) { return ""; }

      String treeStr = String.format("%s%s:\n", prefix, partitionName);

      if (node.getData() != null) {
         for (Cluster element : node.getData()) {
            treeStr += String.format("%s%s,", prefix + "   ", element.getName());
         }

         treeStr += "\n";
      }

      if (node.getPartitions() != null) {
         for (Map.Entry<String, FeatureNode> feature : node.getPartitions().entrySet()) {
            treeStr += FeatureTree.printTree(feature.getValue(), feature.getKey(), prefix + "   ");
         }
      }

      return treeStr;
   }

   public static FeatureTree constructOrganization(String fileName) {
      if (fileName == null) { return null; }

      FeatureTree tree = new FeatureTree();
      FeatureParser parser = new FeatureParser();
      Scanner featureScanner = null;

      try {
         featureScanner = new Scanner(new File(fileName)).useDelimiter("\n");
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.printf("Could not find file '%s'\n", fileName);
         fileErr.printStackTrace();
      }

      while (featureScanner.hasNextLine()) {
         String feature = featureScanner.nextLine();
   
         if (parser.matchString(feature)) { tree.addFeature(parser.getFeature()); }
      }

      return tree;
   }

   public static void main(String[] args) {
      FeatureTree tree = FeatureTree.constructOrganization(args[0]);
      System.out.println("tree output:\n" + tree);
   }
}
