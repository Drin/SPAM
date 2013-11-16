package com.drin.java.gui.components;

import com.drin.java.clustering.Cluster;
import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.output.ClusterWriter;

import com.drin.java.util.Logger;

import javax.swing.SwingWorker;
import javax.swing.JTextArea;

import java.util.List;
import java.util.Map;

public class AnalysisWorker extends SwingWorker<AnalysisWorker.TaskResult, Integer> {
   private List<Cluster> mClusters;
   private Clusterer mClusterer;
   private JTextArea mCanvas;
   private String mOutFile;

   public AnalysisWorker(Clusterer clusterer, List<Cluster> clusters,
                         JTextArea resultCanvas) {
      mClusters = clusters;
      mClusterer = clusterer;
      mCanvas = resultCanvas;
      mOutFile = null;
   }

   public void setOutputFile(String outFileName) { mOutFile = outFileName; }

   @Override
   public TaskResult doInBackground() {
      long startTime = System.currentTimeMillis();

      mClusterer.clusterData(mClusters);
      return new TaskResult(System.currentTimeMillis() - startTime,
                            mClusterer.getClusters());
   }

   @Override
   protected void done() {
      TaskResult result = null;

      try {
         result = get();
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

      if (result != null) {
         ClusterWriter writer = new ClusterWriter(result.mClusterData);
         String elapsedTime = String.format("Elapsed Time: %d\n\n",
                                            result.mElapsedTime);

         if (mOutFile != null) {
            writer.writeData(mOutFile + ClusterWriter.FileType.CSV, writer.getClustInfo());
         }

         mCanvas.setText(elapsedTime + writer.getClustInfo());
      }

      Logger.debug("Analysis Worker finished!");
   }

   @Override
   protected void process(List<Integer> chunks) {
      System.out.printf("%d clusters remaining...\n");

      mCanvas.setText(String.format("%d clusters remaining...\n",
                      chunks.get(chunks.size() - 1)));
   }

   public class TaskResult {
      public long mElapsedTime;
      public Map<Float, List<Cluster>> mClusterData;

      public TaskResult(long time, Map<Float, List<Cluster>> clusters) {
         mElapsedTime = time;
         mClusterData = clusters;
      }
   }
}
