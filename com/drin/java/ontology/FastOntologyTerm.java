package com.drin.java.ontology;

import com.drin.java.clustering.FastCluster;

import java.util.List;
import java.util.Map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * The mPartitions Map is a map of edge/branch names to an FastOntologyTerm
 * (ontology node). The keys of the mPartitions map denote the distinct values
 * for the partition whereas the node itself denotes the data points that match
 * the name of the partition.
 */
public class FastOntologyTerm {
   public static String[][] mIsoLabels;
   private static final String TIME_OPTION_KEY = "TimeSensitive";

   private String mTableName, mColName;
   private Map<String, Boolean> mOptions;
   private Map<String, FastOntologyTerm> mPartitions;
   private List<FastCluster> mData, mClusters;
   private boolean mHasNewData;

   public FastOntologyTerm(String tableName, String colName) {
      mTableName = tableName;
      mColName = colName;

      mOptions = new HashMap<String, Boolean>();
      mPartitions = new LinkedHashMap<String, FastOntologyTerm>();
      mData = null;
      mClusters = null;
      mHasNewData = false;
   }

   public FastOntologyTerm(String tableName, String colName,
                       Map<String, Boolean> options, List<String> values) {
      this(tableName, colName);

      for (Map.Entry<String, Boolean> option : options.entrySet()) {
         mOptions.put(option.getKey(), new Boolean(option.getValue().booleanValue()));
      }

      for (String value : values) {
         mPartitions.put(value, null);
      }
   }

   public FastOntologyTerm(FastOntologyTerm originalNode) {
      this(originalNode.mTableName, originalNode.mColName);

      for (Map.Entry<String, Boolean> option : originalNode.mOptions.entrySet()) {
         mOptions.put(option.getKey(), new Boolean(option.getValue().booleanValue()));
      }

      for (Map.Entry<String, FastOntologyTerm> partition : originalNode.mPartitions.entrySet()) {
         if (partition.getValue() == null) {
            mPartitions.put(partition.getKey(), null);
         }
         else {
            mPartitions.put(partition.getKey(), new FastOntologyTerm(partition.getValue()));
         }
      }
   }

   public FastOntologyTerm(FastCluster element) {
      this(null, null);

      mData = new ArrayList<FastCluster>();
      mData.add(element);
      mHasNewData = true;
   }

   public void clearDataFlag() { mHasNewData = false; }
   public void setClusters(List<FastCluster> clusters) {
      mClusters = clusters;
      mHasNewData = false;
   }

   public String getTableName() { return mTableName; }
   public String getColName() { return mColName; }
   public List<FastCluster> getData() { return mData; }
   public List<FastCluster> getClusters() { return mClusters; }

   public Map<String, FastOntologyTerm> getPartitions() {
      return mPartitions;
   }

   public FastOntologyTerm getPartition(String partitionName) {
      return mPartitions.get(partitionName);
   }

   public boolean hasNewData() { return mHasNewData; }
   public boolean isTimeSensitive() {
      return mOptions.containsKey(TIME_OPTION_KEY);
   }

   public boolean addData(FastCluster element) {
      boolean dataAdded = false;

      if (mPartitions != null && mIsoLabels != null && element.getID() < mIsoLabels.length) {
         for (int labelNdx = 0; labelNdx < mIsoLabels[element.getID()].length; labelNdx++) {
            String isoLabel = mIsoLabels[element.getID()][labelNdx];

            if (mPartitions.containsKey(isoLabel)) {
               if (mPartitions.get(isoLabel) == null) {
                  mPartitions.put(isoLabel, new FastOntologyTerm(element));
               }
               else { dataAdded = mPartitions.get(isoLabel).addData(element); }
            }
         }
      }

      if (!dataAdded) {
         if (mData == null) { mData = new ArrayList<FastCluster>(); }

         if (!mData.contains(element)) {
            mData.add(element);
            dataAdded = true;
         }
      }

      mHasNewData = dataAdded;

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
