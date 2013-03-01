package com.drin.java.ontology;

import com.drin.java.clustering.Cluster;

import java.util.List;
import java.util.Map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * The mPartitions Map is a map of edge/branch names to an OntologyTerm
 * (ontology node). The keys of the mPartitions map denote the distinct values
 * for the partition whereas the node itself denotes the data points that match
 * the name of the partition.
 */
public class OntologyTerm {
   private static final String TIME_OPTION_KEY = "TimeSensitive",
                               SCHEME_NAME_DELIMITER = "-";

   private String mTableName, mColName;
   private Map<String, Boolean> mOptions;
   private Map<String, OntologyTerm> mPartitions;
   private List<Cluster> mData, mClusters;

   public OntologyTerm(String tableName, String colName) {
      mTableName = tableName;
      mColName = colName;

      mOptions = new HashMap<String, Boolean>();
      mPartitions = new LinkedHashMap<String, OntologyTerm>();
      mData = null;
      mClusters = null;
   }

   public OntologyTerm(String tableName, String colName,
                       Map<String, Boolean> options, List<String> values) {
      this(tableName, colName);

      for (Map.Entry<String, Boolean> option : options.entrySet()) {
         mOptions.put(option.getKey(), new Boolean(option.getValue().booleanValue()));
      }

      for (String value : values) {
         mPartitions.put(value, null);
      }
   }

   public OntologyTerm(OntologyTerm originalNode) {
      this(originalNode.mTableName, originalNode.mColName);

      for (Map.Entry<String, Boolean> option : originalNode.mOptions.entrySet()) {
         mOptions.put(option.getKey(), new Boolean(option.getValue().booleanValue()));
      }

      for (Map.Entry<String, OntologyTerm> partition : originalNode.mPartitions.entrySet()) {
         if (partition.getValue() == null) {
            mPartitions.put(partition.getKey(), null);
         }
         else {
            mPartitions.put(partition.getKey(), new OntologyTerm(partition.getValue()));
         }
      }
   }

   public OntologyTerm(Cluster element) {
      mTableName = "";
      mOptions = null;
      mPartitions = null;

      mData = new ArrayList<Cluster>();
      mData.add(element);
   }

   public boolean addData(Cluster element) {
      boolean dataAdded = false;

      if (mPartitions != null) {
         for (Map.Entry<String, OntologyTerm> partition : mPartitions.entrySet()) {
            boolean isPartitionMatch = false;

            if (element instanceof Labelable) {
               isPartitionMatch = ((Labelable) element).hasLabel(partition.getKey());
            }
            else {
               String elementName = element.getName().toLowerCase();
               int keyNdx = elementName.indexOf(partition.getKey());
               int delimNdx = elementName.indexOf(SCHEME_NAME_DELIMITER);
               isPartitionMatch = (keyNdx != -1 && keyNdx < delimNdx);
            }
      
            if (isPartitionMatch) {
               if (partition.getValue() == null) {
                  partition.setValue(new OntologyTerm(element));
                  dataAdded = true;
               }
               else { partition.getValue().addData(element); }
            }
         }
      }

      else {
         mData.add(element);
         dataAdded = true;
      }

      return dataAdded;
   }

   public void setClusters(List<Cluster> clusters) {
      mClusters = clusters;
   }

   public void percolateCluster(Cluster element) {
      if (mData == null) {
         mData = new ArrayList<Cluster>();
      }

      mData.add(element);
   }

   public String getTableName() { return mTableName; }
   public String getColName() { return mColName; }
   public List<Cluster> getData() { return mData; }
   public List<Cluster> getClusters() { return mClusters; }

   public boolean isTimeSensitive() {
      return mOptions.containsKey(TIME_OPTION_KEY);
   }

   public Map<String, OntologyTerm> getPartitions() {
      return mPartitions;
   }

   public OntologyTerm getPartition(String partitionName) {
      return mPartitions.get(partitionName);
   }

   @Override
   public String toString() {
      String fmt = String.format("Feature %s(TimeSensitive[%s]):",
       getTableName(), isTimeSensitive());

      for (String partitionName : mPartitions.keySet()) {
         fmt += " " + partitionName + ",";
      }

      return fmt;
   }
}
