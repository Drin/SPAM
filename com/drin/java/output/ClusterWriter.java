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

   private Map<Double, List<Cluster>> mClusterData;
   private String mDendInfo, mClustInfo;

   public ClusterWriter(Map<Double, List<Cluster>> clusterData) {
      mClusterData = clusterData;

      mDendInfo = null;
      mClustInfo = null;
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

      mDendInfo = "<Dendogram>\n\n";
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

      mDendInfo += "\n\n</Dendogram>";
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
