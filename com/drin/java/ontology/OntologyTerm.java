package com.drin.java.ontology;

import com.drin.java.clustering.Cluster;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
                               SCHEME_NAME_DELIMITER = "-";
   private String mName;
   private Map<String, Boolean> mOptions;
   private Map<String, OntologyTerm> mPartitions;
   private Set<Cluster> mData, mClusters;

   public OntologyTerm(String name) {
      mName = name;

      mOptions = new HashMap<String, Boolean>();
      mPartitions = new LinkedHashMap<String, OntologyTerm>();
      mData = null;
      mClusters = null;
   }

   public OntologyTerm(String name, Map<String, Boolean> options, List<String> values) {
      this(name);

      for (Map.Entry<String, Boolean> option : options.entrySet()) {
         mOptions.put(option.getKey(), new Boolean(option.getValue().booleanValue()));
      }

      for (String value : values) {
         mPartitions.put(value, null);
      }
   }

   public OntologyTerm(Cluster element) {
      mName = "";
      mOptions = null;
      mPartitions = null;

      mData = new HashSet<Cluster>();
      mData.add(element);
   }

   public OntologyTerm(OntologyTerm originalNode) {
      this(originalNode.mName);

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

   public boolean addData(Cluster element) {
      boolean dataAdded = false;

      if (mPartitions != null) {
         for (Map.Entry<String, OntologyTerm> partition : mPartitions.entrySet()) {
            int keyNdx = element.getName().toLowerCase().indexOf(partition.getKey());
      
            if (System.getenv().containsKey("DEBUG")) {
               System.out.printf("element '%s' has keyNdx %d(%s) but the end of its " +
                "naming scheme is at %d(%s)\n", element.getName(), keyNdx, partition.getKey(),
                element.getName().indexOf(SCHEME_NAME_DELIMITER), SCHEME_NAME_DELIMITER);
            }
      
            if (keyNdx != -1 && keyNdx < element.getName().indexOf(SCHEME_NAME_DELIMITER)) {
               if (partition.getValue() == null) {
                  partition.setValue(new OntologyTerm(element));
                  dataAdded = true;
               }
               else {
                  partition.getValue().addData(element);
               }
            }
         }
      }
      else {
         mData.add(element);
         dataAdded = true;
      }

      return dataAdded;
   }

   public void setClusters(Set<Cluster> clusterSet) {
      mClusters = clusterSet;
   }

   public void percolateCluster(Cluster element) {
      if (mData == null) {
         mData = new HashSet<Cluster>();
      }

      mData.add(element);
   }

   public String getName() {
      return mName;
   }

   public Set<Cluster> getData() {
      return mData;
   }

   public Set<Cluster> getClusters() {
      return mClusters;
   }

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
       getName(), isTimeSensitive());

      for (String partitionName : mPartitions.keySet()) {
         fmt += " " + partitionName + ",";
      }

      return fmt;
   }
}
