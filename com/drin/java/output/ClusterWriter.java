package com.drin.java.output;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.Clusterable;

import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileWriter;

public class ClusterWriter {
   private static final String DEFAULT_DIR = "ClusterResults",
                               FILE_SEP = System.getProperty("file.separator");

   private Map<Float, List<Cluster>> mClusterData;
   private String mClustInfo;

   public ClusterWriter(Map<Float, List<Cluster>> clusterData) {
      mClusterData = clusterData;
      mClustInfo = null;
   }

   public String getClustInfo() {
      if (mClustInfo == null) {
         getClusterOutput();
      }

      return mClustInfo;
   }

   public void clearClusterOutput() { mClustInfo = null; }

   private void getClusterOutput() {
      String clustContents = "", bigClustName = "";
      int biggestClust = 0;

      mClustInfo = "";

      for (Map.Entry<Float, List<Cluster>> clusterData : mClusterData.entrySet()) {
         mClustInfo += String.format("\nThreshold: %.04f\nNumber of Result Clusters, %d\n",
                                     clusterData.getKey(), clusterData.getValue().size());

         for (Cluster cluster : clusterData.getValue()) {
            /*
             * Cluster Information
             */
            if (cluster.size() > biggestClust) {
               biggestClust = cluster.size();
               bigClustName = String.format("\"Cluster %d\"", cluster.getId());
            }

            for (Clusterable<?> elem : cluster.getElements()) {
               clustContents += String.format("Cluster %d, %s\n", cluster.getId(), elem.getName());
            }
         }

         mClustInfo += String.format("Largest Cluster, %s\nLargest Cluster Size, %d\n\n%s",
                                     bigClustName, biggestClust, clustContents);

         clustContents = "";
         bigClustName = "";
         biggestClust = 0;
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
      private FileType(String fileExt) { mExt = fileExt; }

      @Override
      public String toString() { return mExt; }
   }
}
