package com.drin.java.gui.components;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.Clusterable;

import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.output.ClusterWriter;
import com.drin.java.util.Logger;

import javax.swing.SwingWorker;
import javax.swing.JTextArea;

import java.util.List;

public class AnalysisWorker extends SwingWorker<String[], Integer> {
   private static final int DENDOGRAM_INFO = 0,
                            CLUSTER_INFO = 1;
   private Clusterer mClusterer;
   private JTextArea mCanvas;
   private String mOutFile;

   public AnalysisWorker(Clusterer clusterer, JTextArea resultCanvas) {
      mClusterer = clusterer;
      mCanvas = resultCanvas;
      mOutFile = null;
   }

   public void setOutputFile(String outFileName) { mOutFile = outFileName; }

   @Override
   public String[] doInBackground() {
      long startTime = System.currentTimeMillis();
      mClusterer.clusterData(mCanvas);
      String clustInfo = String.format("Elapsed Time: %d\n\n",
                                       System.currentTimeMillis() - startTime);

      String dendInfo = "<Clusters>\n";
      for (Cluster cluster : mClusterer.getClusters()) {
         /*
          * Dendogram Information
          */
         dendInfo += cluster.getDendogram().toString();

         /*
          * Cluster Information
          */
         //clustInfo += String.format("Cluster %s:\n", cluster.getName());
         for (Clusterable<?> elem : cluster.getElements()) {
            clustInfo += String.format("Cluster %s, %s\n", cluster.getName(), elem.getName());
         }
      }

      dendInfo += "</Clusters>\n";

      return new String[] {dendInfo, clustInfo};
   }

   @Override
   protected void done() {
      String[] resultArr = null;

      try {
         resultArr = get();
      }
      catch(InterruptedException interrErr) {
         Logger.error(-1, "Analysis worker interrupted while waiting for " +
                      "Clusterer to finish!");
         interrErr.printStackTrace();
      }
      catch(java.util.concurrent.ExecutionException execErr) {
         Logger.error(-1, "Error while analysis worker was executing!");
         execErr.printStackTrace();
      }

      if (resultArr != null) {
         writeDendogram(resultArr[DENDOGRAM_INFO]);
         writeElements(resultArr[CLUSTER_INFO]);
      
         mCanvas.setText(resultArr[DENDOGRAM_INFO]);
         mCanvas.setText(resultArr[CLUSTER_INFO]);
      }

      Logger.debug("Analysis Worker finished!");
   }

   @Override
   protected void process(List<Integer> chunks) {
      System.out.printf("%d clusters remaining...\n");
      mCanvas.setText(String.format("%d clusters remaining...\n",
       chunks.get(chunks.size() - 1)));
   }

   private void writeElements(String elementStr) {
      ClusterWriter writer = null;

      if (mOutFile != null) {
         writer = new ClusterWriter(mOutFile + ClusterWriter.FileType.CSV);
         writer.writeData(elementStr);
      }
   }

   private void writeDendogram(String dendogramStr) {
      ClusterWriter writer = null;

      if (mOutFile != null) {
         writer = new ClusterWriter(mOutFile + ClusterWriter.FileType.XML);
         writer.writeData(dendogramStr);
      }
   }
}
