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
   private static final String TIME_OPTION_KEY = "TimeSensitive";

   private String mTableName, mColName;
   private Map<String, Boolean> mOptions;
   private Map<String, OntologyTerm> mPartitions;
   private List<Cluster> mData, mClusters;
   private boolean mHasNewData;

   public OntologyTerm(String tableName, String colName) {
      mTableName = tableName;
      mColName = colName;

      mOptions = new HashMap<String, Boolean>();
      mPartitions = new LinkedHashMap<String, OntologyTerm>();
      mData = null;
      mClusters = null;
      mHasNewData = false;
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
      this(null, null);

      mData = new ArrayList<Cluster>();
      mData.add(element);
      mHasNewData = true;
   }

   public void clearDataFlag() { mHasNewData = false; }
   public void setClusters(List<Cluster> clusters) {
      mClusters = clusters;
      mHasNewData = false;
   }

   public String getTableName() { return mTableName; }
   public String getColName() { return mColName; }
   public List<Cluster> getData() { return mData; }
   public List<Cluster> getClusters() { return mClusters; }

   public Map<String, OntologyTerm> getPartitions() {
      return mPartitions;
   }

   public OntologyTerm getPartition(String partitionName) {
      return mPartitions.get(partitionName);
   }

   public boolean hasNewData() { return mHasNewData; }
   public boolean isTimeSensitive() {
      return mOptions.containsKey(TIME_OPTION_KEY);
   }

   public boolean addData(Cluster element) {
      boolean dataAdded = false;

      if (mPartitions != null && !mPartitions.isEmpty()) {
         boolean isPartitionMatch = false;

         for (Map.Entry<String, OntologyTerm> partition : mPartitions.entrySet()) {
            if (element instanceof Labelable) {
               isPartitionMatch = ((Labelable) element).hasLabel(partition.getKey());
            }
      
            if (isPartitionMatch) {
               if (partition.getValue() == null) {
                  partition.setValue(new OntologyTerm(element));
                  dataAdded = true;
               }
               else { dataAdded = partition.getValue().addData(element); }

               //System.out.printf("matched partition!\n");

               break;
            }
         }

         if (!isPartitionMatch) {
            //System.out.printf("never matched partition!\n");
         }
      }

      else {
         mData.add(element);
         dataAdded = true;
      }

      if (dataAdded) { mHasNewData = true; }

      return dataAdded;
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
