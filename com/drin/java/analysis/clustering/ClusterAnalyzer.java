package com.drin.java.analysis.clustering;

import com.drin.java.types.DataObject;
import com.drin.java.types.Cluster;
import com.drin.java.types.HierarchicalCluster;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.DataComparator;
import com.drin.java.metrics.ClusterStatistics;
import com.drin.java.metrics.ClusterComparator;

import java.util.Set;
import java.util.Map;

public class ClusterAnalyzer {
   private static final boolean DEBUG = false;
   private static final String MIN_KEY = "min",
                               MAX_KEY = "max",
                               MEAN_KEY = "mean";
   private DataMetric mDataMetric;
   private String mResult, mDendogram, mElements;

   public ClusterAnalyzer(DataMetric dataMetric) {
      mDataMetric = dataMetric;

      mResult = null;
      mDendogram = null;
      mElements = null;
   }

   public void analyzeClusters(Set<Cluster<DataObject>> clusterSet) {
      String stats = "", dend = "<Clusters>\n", elementStr = "", prefix = "\t";
      Cluster<DataObject> clust = null;

      /*
      stats = String.format("Cluster:, Max pairwise correlation:, " +
       "Min pairwise correlation:, Avg pairwise correlation:,\n");
       */

      for (Cluster<DataObject> obj : clusterSet) {
         //for (DataObject obj : cluster.getElementList()) {
            if (obj instanceof Cluster) {
               clust = (Cluster<DataObject>) obj;
            }
            else { continue; }

            /*
             * Dendogram Construction
             */
            dend += clust.getDendogram(mDataMetric) + "\n";

            elementStr += String.format("Cluster (%s):, Isolates:,\n", clust.getName());
            for (DataObject element : clust.getElements()) {
               elementStr += String.format("\t,%s,\n", element.getName());
            }
      
            /*
             * Cluster statistics construction
             *
            Map<String, Double> statMap = ClusterStatistics.calcStats(mDataMetric, clust.getElements());
            
            if (statMap != null) {
               stats += String.format("%s, %.04f, %.04f, %.04f\n", clust.getName(),
                statMap.get(MAX_KEY), statMap.get(MIN_KEY), statMap.get(MEAN_KEY));
            }
            */
         //}
      }
      
      mResult = stats;
      mDendogram = dend + "</Clusters>\n";
      mElements = elementStr;
   }

   public String getClusterElements() {
      if (mElements == null) { return "No cluster elements to report\n"; }
      else { return mElements; }
   }

   public String getStats() {
      if (mResult == null) { return "No cluster statistics calculated\n"; }
      else { return mResult; }
   }

   public String getDendogram() {
      if (mDendogram == null) { return "No valid dendogram representation\n"; }
      else { return mDendogram; }
   }
}
