package com.drin.java.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.Clusterable;

import java.util.List;
import java.util.Map;

public class ClusterResults {
   private Map<Double, List<Cluster>> mClusters;
   private long mClusterTime;

   public ClusterResults(Map<Double, List<Cluster>> finalClusters, long time) {
      mClusters = finalClusters;
      mClusterTime = time;
   }

   public ClusterResults(Map<Double, List<Cluster>> finalClusters) {
      this(finalClusters, -1);
   }

   public long getClusterTime() {
      return mClusterTime;
   }

   public String getElapsedTime() {
      long hours = mClusterTime / 3600000;
      long minutes = (mClusterTime % 3600000) / 60000;
      long seconds = ((mClusterTime % 3600000) % 60000) / 1000;

      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
   }

   public String[] getSQLInserts(int clusterRun) {
      String[] sqlInserts = new String[100];
      int isoId = -1, delimNdx = -1, clustNum = 0, isolateNum = 0, limit = 1000, sqlNdx = 0;
      String strainInsert = "INSERT INTO test_run_strain_link(" +
                             "test_run_id, cluster_id, cluster_threshold, " +
                             "strain_diameter, average_isolate_similarity, " +
                             "percent_similar_isolates) VALUES ";
      String isolateInsert = "INSERT INTO test_isolate_strains(" +
                              "test_run_id, cluster_id, cluster_threshold, " +
                              "name_prefix, name_suffix) VALUES ";
      String run_strain_link = strainInsert, isolate_strain = isolateInsert, elementName, isoDesignation;

      for (Map.Entry<Double, List<Cluster>> clusterData : mClusters.entrySet()) {
         for (Cluster cluster : clusterData.getValue()) {

            if (sqlNdx >= sqlInserts.length - 2) {
               String[] newArr = new String[sqlInserts.length * 2];
               for (int ndx = 0; ndx < sqlInserts.length; ndx++) {
                  newArr[ndx] = sqlInserts[ndx];
               }
            }

            if (clustNum > 0 && clustNum++ % limit == 0) {
               sqlInserts[sqlNdx++] = run_strain_link.substring(0, run_strain_link.length() - 2);
               System.out.printf("sqlInsert: \n\t%s\n", sqlInserts[sqlNdx - 1]);

               run_strain_link = strainInsert;
            }

            run_strain_link += String.format(
               "(%d, %d, %.04f, %.04f, %.04f, %.04f), ",
               clusterRun, cluster.getId(), clusterData.getKey().doubleValue(),
               cluster.getDiameter(), cluster.getPercentSimilar(), cluster.getMean()
            );

            for (Clusterable<?> element : cluster.getElements()) {
               elementName = element.getName();
               delimNdx = elementName.indexOf("-");
               isoDesignation = elementName.substring(0, delimNdx);
               isoId = Integer.parseInt(elementName.substring(delimNdx + 1, elementName.length()));

               if (isolateNum > 0 && isolateNum++ % limit == 0) {
                  sqlInserts[sqlNdx++] = isolate_strain.substring(0, isolate_strain.length() - 2);
                  System.out.printf("sqlInsert: \n\t%s\n", sqlInserts[sqlNdx - 1]);

                  isolate_strain = isolateInsert;
               }

               isolate_strain += String.format(
                  "(%d, %d, %.04f, '%s', %d), ",
                  clusterRun, cluster.getId(), clusterData.getKey().doubleValue(),
                  isoDesignation, isoId
               );
            }
         }
      }

      if (!run_strain_link.equals(strainInsert)) {
         sqlInserts[sqlNdx++] = run_strain_link.substring(0, run_strain_link.length() - 2);
      }

      if (!isolate_strain.equals(isolateInsert)) {
         sqlInserts[sqlNdx++] = isolate_strain.substring(0, isolate_strain.length() - 2);
      }

      return sqlInserts;
   }

   public String toString() {
      String clustInfo = "", dendInfo = "", clustContents = "", bigClustName = "";
      int biggestClust = 0;

      for (Map.Entry<Double, List<Cluster>> clusterData : mClusters.entrySet()) {
         dendInfo += String.format("<Clusters threshold=\"%.04f\">\n", clusterData.getKey());
         clustInfo += String.format("\nThreshold: %.04f\nNumber of Result Clusters, %d\n",
                                     clusterData.getKey(), clusterData.getValue().size());

         for (Cluster cluster : clusterData.getValue()) {
            if (cluster.size() > biggestClust) {
               biggestClust = cluster.size();
               bigClustName = String.format("\"Cluster %d\"", cluster.getId());
            }

            for (Clusterable<?> elem : cluster.getElements()) {
               clustContents += String.format("Cluster %d, %s\n", cluster.getId(), elem.getName());
            }
         }

         dendInfo += "</Clusters>\n";
         clustInfo += String.format("Largest Cluster, %s\nLargest Cluster Size, %d\n\n%s",
                                    bigClustName, biggestClust, clustContents);
      }

      return String.format("Elapsed Time: %dms\n%s%s", mClusterTime,
                           ("******\n\nDendogram:\n" + dendInfo),
                           ("******\n\nClusters:\n" + clustInfo));
   }
}
