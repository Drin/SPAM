package com.drin.java.gui.components;

import com.drin.java.clustering.Cluster;
import com.drin.java.analysis.clustering.Clusterer;

import com.drin.java.output.ClusterWriter;

import javax.swing.SwingWorker;
import javax.swing.JTextArea;

import java.util.Set;
import java.util.List;

public class AnalysisWorker extends SwingWorker<String[], Integer> {
   private static final int DENDOGRAM_NDX = 0,
                            ISOLATE_LIST_NDX = 1;

   private Clusterer mClusterer;
   private JTextArea mCanvas;

   private String mOutFile;

   public AnalysisWorker(Clusterer clusterer, JTextArea resultCanvas) {
      mClusterer = clusterer;
      mCanvas = resultCanvas;
      mOutFile = null;
   }

   public void setOutputFile(String outFileName) {
      mOutFile = outFileName;
   }

   @Override
   public String[] doInBackground() {
      mClusterer.clusterData();

      String[] clusters = new String[mClusterer.getClusters().size()];

      int clustNdx = 0;
      for (Cluster cluster : mClusterer.getClusters()) {
         clusters[clustNdx++] = cluster.toString();
      }

      return clusters;
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

   @Override
   protected void done() {
      String[] resultArr = null;

      try {
         resultArr = get();
      }
      catch(InterruptedException interrErr) {
         System.out.printf("Analysis worker interrupted while waiting for Clusterer to finish!\n");
         interrErr.printStackTrace();
      }
      catch(java.util.concurrent.ExecutionException execErr) {
         System.out.printf("Error while analysis worker was executing!\n");
         execErr.printStackTrace();
      }

      if (resultArr != null) {
         writeDendogram(resultArr[DENDOGRAM_NDX]);
         writeElements(resultArr[ISOLATE_LIST_NDX]);
      
         mCanvas.setText(resultArr[DENDOGRAM_NDX]);
      }

      System.out.println("done!");
   }

   @Override
   protected void process(List<Integer> chunks) {
      System.out.printf("%d clusters remaining...\n");
      mCanvas.setText(String.format("%d clusters remaining...\n",
       chunks.get(chunks.size() - 1)));
   }
}
