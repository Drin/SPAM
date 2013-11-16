package com.drin.java.clustering;

import com.drin.java.biology.Isolate;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.Clusterable;

import java.util.List;
import java.util.Map;

public class ClusterResults {
   private Map<Float, List<Cluster>> mClusters;

   public ClusterResults(Map<Float, List<Cluster>> finalClusters) {
      mClusters = finalClusters;
   }

   public String toString() {
      String clustInfo = "", clustContents = "", bigClustName = "";
      int biggestClust = 0;

      for (Map.Entry<Float, List<Cluster>> clusterData : mClusters.entrySet()) {
         clustInfo += String.format("\nThreshold: %.04f\nNumber of Result Clusters, %d\n",
                                     clusterData.getKey(), clusterData.getValue().size());

         for (Cluster cluster : clusterData.getValue()) {
            if (cluster.size() > biggestClust) {
               biggestClust = cluster.size();
               bigClustName = String.format("\"Cluster %d\"", cluster.getId());
            }

            clustContents += String.format("\n\nCluster Id, Isolate Id, Host Id, " +
                                           "Source (userName), Location, " +
                                           "Date\n");

            for (Clusterable<?> elem : cluster.getElements()) {
               clustContents += String.format("Cluster %d, %s, %s, %s, %s, %s\n",
                                              cluster.getId(), elem.getName(),
                                              ((Isolate) elem).getHost(), 
                                              ((Isolate) elem).getSource(), 
                                              ((Isolate) elem).getLoc(), 
                                              ((Isolate) elem).getDate());
            }
         }

         clustInfo += String.format("Largest Cluster, %s\nLargest Cluster Size, %d\n\n%s",
                                    bigClustName, biggestClust, clustContents);

         clustContents = "";
      }

      return String.format("%s", "******\n\nClusters:\n" + clustInfo);
   }
}
