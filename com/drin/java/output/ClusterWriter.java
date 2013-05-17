package com.drin.java.output;

import com.drin.java.database.CPLOPConnection;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.Clusterable;
import com.drin.java.analysis.clustering.Clusterer;

import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileWriter;

public class ClusterWriter {
   private static final String DEFAULT_DIR = "ClusterResults",
                               FILE_SEP = System.getProperty("file.separator");

   private CPLOPConnection mConn;
   private Clusterer mClusterer;
   private Map<Double, List<Cluster>> mClusterData;
   private String mDendInfo, mClustInfo;
   private long mElapsedTime;

   public ClusterWriter(Clusterer clusterer, Map<Double, List<Cluster>> clusterData, long elapsedTime) {
      try { mConn = new CPLOPConnection();}
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }
      mClusterer = clusterer;
      mClusterData = clusterData;
      mElapsedTime = elapsedTime;

      mDendInfo = null;
      mClustInfo = null;
   }

   private String getElapsedTime(long clusterTime) {
      long hours = clusterTime / 3600000;
      long minutes = (clusterTime % 3600000) / 60000;
      long seconds = ((clusterTime % 3600000) % 60000) / 1000;

      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
   }


   private String[] getSQLInserts(int clusterRun, Map<Double, List<Cluster>> clusters) {
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

      for (Map.Entry<Double, List<Cluster>> clusterData : clusters.entrySet()) {
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

   public String getDendInfo() {
      if (mDendInfo == null) {
         getClusterOutput();
      }

      return mDendInfo;
   }

   public String getClustInfo() {
      if (mClustInfo == null) {
         getClusterOutput();
      }

      return mClustInfo;
   }

   public void clearClusterOutput() {
      mDendInfo = null;
      mClustInfo = null;
   }

   private void getClusterOutput() {
      String clustContents = "", bigClustName = "";
      int biggestClust = 0;

      mDendInfo = "";
      mClustInfo = "";

      for (Map.Entry<Double, List<Cluster>> clusterData : mClusterData.entrySet()) {
         mDendInfo += String.format("<Clusters threshold=\"%.04f\">\n", clusterData.getKey());
         mClustInfo += String.format("\nThreshold: %.04f\nNumber of Result Clusters, %d\n",
                                     clusterData.getKey(), clusterData.getValue().size());

         for (Cluster cluster : clusterData.getValue()) {
            /*
             * Dendogram Information
             */
            mDendInfo += cluster.getDendogram().toString();

            /*
             * Cluster Information
             */
            if (cluster.size() > biggestClust) {
               biggestClust = cluster.size();
               bigClustName = String.format("\"Cluster %s\"", cluster.getName());
            }

            for (Clusterable<?> elem : cluster.getElements()) {
               clustContents += String.format("Cluster %s, %s\n", cluster.getName(), elem.getName());
            }
         }

         mDendInfo += "</Clusters>\n";
         mClustInfo += String.format("Largest Cluster, %s\nLargest Cluster Size, %d\n\n%s",
                                     bigClustName, biggestClust, clustContents);

         clustContents = "";
         bigClustName = "";
         biggestClust = 0;
      }
   }

   public void persistResults() {
      int use_transform = 0;
      Clusterer clusterer = mClusterer;
      int initialSize = mClusterData.get(new Double(0.99)).size();

      try {
         mConn.insertNewRun(String.format(
            "INSERT INTO test_runs(run_date, run_time, cluster_algorithm," +
                                  "average_strain_similarity, use_transform) " +
            "VALUES (?, '%s', '%s', %.04f, %d)",
            getElapsedTime(mElapsedTime), clusterer.getName(),
            clusterer.getInterClusterSimilarity(), use_transform
         ));

         int runID = mConn.getTestRunId();

         String performanceInsert = String.format(
               "INSERT INTO test_run_performance(" +
               "test_run_id, update_id, update_size, run_time) " +
               "VALUES (%d, %d, %d, %d)",
               runID, 0, initialSize, mElapsedTime
         );
         mConn.executeInsert(performanceInsert);

         if (runID != -1) {
            for (String sqlQuery : getSQLInserts(runID, mClusterData)) {
               if (sqlQuery != null) {
                  System.out.printf("%s\n", sqlQuery);

                  try { mConn.executeInsert(sqlQuery); }
                  catch(java.sql.SQLException sqlErr) {
                     sqlErr.printStackTrace();
                  }
               }
            }
         }
      }
      catch (java.sql.SQLException sqlErr) {
         sqlErr.printStackTrace();
      }
   }

   public void writeData(String outputFile, String text) {
      FileWriter writer = null;

      try {
         File outFile = new File(System.getProperty("user.dir") + FILE_SEP +
                                 DEFAULT_DIR + FILE_SEP + outputFile);

         if (!outFile.getParentFile().exists()) { outFile.getParentFile().mkdirs(); }
         if (!outFile.isFile()) { outFile.createNewFile(); }

         writer = new FileWriter(outFile);
         writer.write(text);
         writer.close();
      }
      catch (java.io.IOException ioErr) {
         System.err.printf("IOException when writing to file '%s'\n", outputFile);
         ioErr.printStackTrace();
         return;
      }
   }

   public enum FileType {
      CSV(".csv"), MATRIX(".matrix"), XML(".xml"), PYRORUN(".pyrorun");

      private String mExt;

      private FileType(String fileExt) {
         mExt = fileExt;
      }

      @Override
      public String toString() {
         return mExt;
      }
   }
}
