package com.drin.java.ontology;

import com.drin.java.clustering.Cluster;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;

/**
 * The mPartitions Map is a map of edge/branch names to an OntologyTerm
 * (ontology node). The keys of the mPartitions map denote the distinct values
 * for the partition whereas the node itself denotes the data points that match
 * the name of the partition.
 */
public class OntologyTerm {
   private static final String TIME_OPTION_KEY = "TimeSensitive",
                               STATIC_OPTION_KEY = "StaticOntology",
                               SQUISHY_OPTION_KEY = "SquishyCorr",
                               SIMILAR_OPTION_KEY = "SimilarCorr";

   private boolean mHasNewData;
   private String mColName;
   private Map<String, Boolean> mOptions;
   private Map<String, OntologyTerm> mPartitions;
   private List<Cluster> mData, mClusters;

   public OntologyTerm(String colName) {
      if (colName.equals("")) { mColName = null; }
      else { mColName = colName; }

      mHasNewData = false;
      mData = null;
      mClusters = null;
      mOptions = new HashMap<String, Boolean>();
      mPartitions = new LinkedHashMap<String, OntologyTerm>();
   }

   public OntologyTerm(String colName, Map<String, Boolean> options) {
      this(colName);

      for (Map.Entry<String, Boolean> option : options.entrySet()) {
         mOptions.put(option.getKey(), new Boolean(option.getValue().booleanValue()));
      }
   }

   public OntologyTerm(OntologyTerm originalNode) {
      this(originalNode.mColName);

      mOptions = originalNode.mOptions;

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
      this("");

      mHasNewData = true;
      mData = new ArrayList<Cluster>();
      mData.add(element);
   }

   public int size() {
      int size = 1;

      for (Map.Entry<String, OntologyTerm> partition : mPartitions.entrySet()) {
         if (partition != null && partition.getValue() != null) {
            size += partition.getValue().size();
         }
         else { size += 1; }
      }

      return size;
   }

   public void clearDataFlag() { mHasNewData = false; }

   public void setPartitions(Map<String, OntologyTerm> partitions) {
      mPartitions = partitions;
   }

   public void setClusters(List<Cluster> clusters) {
      mClusters = clusters;
      mHasNewData = false;
   }

   public String getColName() { return mColName; }
   public List<Cluster> getData() { return mData; }
   public List<Cluster> getClusters() { return mClusters; }
   public Map<String, OntologyTerm> getPartitions() { return mPartitions; }
   public OntologyTerm getPartition(String partitionName) {
      return mPartitions.get(partitionName);
   }

   public boolean hasNewData() { return mHasNewData; }
   public boolean isTimeSensitive() { return mOptions.containsKey(TIME_OPTION_KEY); }
   public boolean isStatic() { return mOptions.containsKey(STATIC_OPTION_KEY); }
   public boolean isSquishyCorr() { return mOptions.containsKey(SQUISHY_OPTION_KEY); }
   public boolean isSimilarCorr() { return mOptions.containsKey(SIMILAR_OPTION_KEY); }

   public boolean addData(Cluster element, byte labelNdx) {
      boolean dataAdded = false;

      if (mPartitions != null && element.getMetaData() != null && mPartitions.size() > 0) {
         if (labelNdx < element.getMetaData().length) {
            String isoLabel = element.getMetaData()[labelNdx];

            if (mPartitions.containsKey(isoLabel)) {

               if (mPartitions.get(isoLabel) == null) {
                  mPartitions.put(isoLabel, new OntologyTerm(element));
                  dataAdded = true;
               }
               else {
                  if (labelNdx < element.getMetaData().length - 1) {
                     element.getMetaData()[labelNdx + 1] =
                        isoLabel + ":" + element.getMetaData()[labelNdx + 1];
                  }

                  dataAdded = mPartitions.get(isoLabel).addData(
                     element, (byte) (labelNdx + 1)
                  );
               }
            }
         }
      }

      if (!dataAdded) {
         if (mData == null) { mData = new ArrayList<Cluster>(); }
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
      String fmt = String.format("Feature %s(TimeSensitive[%s]):", mColName, isTimeSensitive());

      for (String partitionName : mPartitions.keySet()) {
         fmt += " " + partitionName + ",";
      }

      return fmt;
   }
}
