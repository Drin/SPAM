package com.drin.java;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.database.CPLOPConnection;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyTerm;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;
import com.drin.java.clustering.ClusterResults;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterAverageMetric;

import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import com.drin.java.util.Configuration;
import com.drin.java.output.ProgressWriter;
import com.drin.java.output.ClusterWriter;

/*
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
*/

import javax.swing.JTextArea;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import java.io.File;

public class SPAMMain {
   private static CPLOPConnection mConn = null;

   private static final int   DEFAULT_SIZE = 0;

   private static final String DEFAULT_CONFIG_FILE = "config.cfg",
                               CLUST_METRIC = "cluster metric",
                               ISO_METRIC   = "isolate metric",
                               REG_METRIC   = "region metric",
                               PYRO_METRIC  = "pyroprint metric",
                               PYRO_LEN     = "pyroprint length",
                               ALPHA_KEY = "alpha threshold",
                               BETA_KEY  = "beta threshold";

   private HierarchicalClusterer mClusterer;
   private Ontology mOntology;
   private List<Cluster> matchClusters;
   private ProgressWriter mProgWriter;

   private float mAlphaThresh, mBetaThresh;

   /*
    * Constructors
    */
   private SPAMMain(int sizeHint, String configFile, ProgressWriter writer) {
      if (mConn == null) {
         try { mConn = new CPLOPConnection(); }
         catch (Exception err) {
            System.out.printf("* Unable to connect to CPLOP *\n");
            err.printStackTrace();
            System.exit(1);
         }
      }

      if (!Configuration.isInitialized()) {
         System.out.printf("Loading config file %s\n", configFile);
         Configuration.loadConfig(configFile);
      }
      else { System.out.printf("Configuration already populated\n"); }

      mClusterer = null;
      mOntology = null;
      matchClusters = null;
      mProgWriter = null;
   }

   public SPAMMain(ProgressWriter writer) {
      this(DEFAULT_SIZE, DEFAULT_CONFIG_FILE, writer);
   }

   public SPAMMain(int sizeHint, ProgressWriter writer) {
      this(sizeHint, DEFAULT_CONFIG_FILE, writer);
   }

   /*
    * Main Method. Used if SPAM is run without a GUI
    * (i.e. headless/programmatically)
    */
   public static void main(String[] args) {
      /*
      for (Map.Entry<Float, List<Cluster>> result : mClusterer.getClusters().entrySet()) {
      }
      */
   }

   /*
    * API definition on which a GUI should be built, but provides all necessary
    * functionality
    */

   //Be able to retrieve the results of clustering
   public Map<Float, List<Cluster>> getClusters() {
      return mClusterer.getClusters();
   }

   //Be able to add data to a currently maintained collection of data. In this
   //way re-initialization does not have to be done necessarily.
   public void addData(List<Isolate> isoData) {
      if (mOntology != null) {
         for (Isolate isolate : isoData) {
            mOntology.addData(new HCluster(isolate));
         }
      }
   }

   public void loadConfiguration(String config) {
      if (config == null) { Configuration.loadConfig(DEFAULT_CONFIG_FILE); }
      else { Configuration.loadConfig(config); }
   }

   //Be able to modify thresholds and other parameters
   public void setParams(String region, String param, Object val) {
   }

   public void setAlpha(float alphaThresh) {
      mAlphaThresh = alphaThresh;
   }

   public void setBeta(float betaThresh) {
      mBetaThresh = betaThresh;
   }

   public void setProgressWriter(JTextArea progressCanvas) {
      mProgWriter = new ProgressWriter(progressCanvas);
   }

   public void writeResults(String outFileName) {
      ClusterResults results = new ClusterResults(mClusterer.getClusters());
      ClusterWriter writer = new ClusterWriter(mClusterer.getClusters());

      if (outFileName != null) {
         writer.writeData(outFileName + ".csv", results.toString());
      }

      if (mProgWriter != null) {
         mProgWriter.writeText("Results written to " + outFileName + ".csv");
      }
   }

   //Be able to invoke clustering
   public Map<Float, List<Cluster>> clusterData(String dataSetIDs, String tableName) {
      Map<Float, List<Cluster>> clusterResults = new HashMap<Float, List<Cluster>>();

      List<Cluster> clusters1 = new ArrayList<Cluster>();
      List<Cluster> clusters2 = new ArrayList<Cluster>();
      List<Isolate> isolateDataList = getIsolateData(dataSetIDs);

      /*
       * Prep Cluster lists. Prepping 2 to naively prevent modification of one
       * list when clustering at 2 different thresholds
       */
      for (Isolate isolate : isolateDataList) {
         clusters1.add(new HCluster(isolate));
      }

      Cluster.resetClusterIDs();

      for (Isolate isolate : isolateDataList) {
         clusters2.add(new HCluster(isolate));
      }

      /*
       * Cluster the data and print/return the results
       */

      //ignore ontologies for now... just agglomerative cluster
      mClusterer = new AgglomerativeClusterer(clusters1.size(), mAlphaThresh, mProgWriter);
      mClusterer.clusterData(clusters1);
      clusterResults.put(new Float(mAlphaThresh), clusters1);

      System.out.println(new ClusterResults(mClusterer.getClusters()));

      //ignore ontologies for now... just agglomerative cluster
      mClusterer = new AgglomerativeClusterer(clusters2.size(), mBetaThresh, mProgWriter);
      mClusterer.clusterData(clusters2);
      clusterResults.put(new Float(mBetaThresh), clusters2);

      System.out.println(new ClusterResults(mClusterer.getClusters()));

      return clusterResults;
   }

   public Map<Float, List<Cluster>> clusterData(Set<String> dataSetIDs,
                                                Map<String, Map<String, Float>> corrMap) {
      Map<Float, List<Cluster>> clusterResults = new HashMap<Float, List<Cluster>>();
      List<Cluster> clusters1 = new ArrayList<Cluster>();
      List<Cluster> clusters2 = new ArrayList<Cluster>();

      Isolate tmpIso = null;

      for (String isoName : dataSetIDs) {
         tmpIso = new Isolate(isoName);

         if (corrMap.containsKey(isoName)) {
            tmpIso.setCache(corrMap.get(isoName));
         }

         clusters1.add(new HCluster(tmpIso));
      }

      Cluster.resetClusterIDs();

      for (String isoName : dataSetIDs) {
         tmpIso = new Isolate(isoName);

         if (corrMap.containsKey(isoName)) {
            tmpIso.setCache(corrMap.get(isoName));
         }

         clusters2.add(new HCluster(tmpIso));
      }

      /*
       * Cluster the data and print/return the results
       */

      //ignore ontologies for now... just agglomerative cluster
      mClusterer = new AgglomerativeClusterer(clusters1.size(), mAlphaThresh, mProgWriter);
      mClusterer.clusterData(clusters1);
      clusterResults.put(new Float(mAlphaThresh), clusters1);

      System.out.println(new ClusterResults(mClusterer.getClusters()));

      //ignore ontologies for now... just agglomerative cluster
      mClusterer = new AgglomerativeClusterer(clusters2.size(), mBetaThresh, mProgWriter);
      mClusterer.clusterData(clusters2);
      clusterResults.put(new Float(mBetaThresh), clusters2);

      System.out.println(new ClusterResults(mClusterer.getClusters()));

      return clusterResults;
   }

   //Be able to match data against clusters, instead of include data in the
   //clusters
   public void matchData() {
   }

   //Be able to choose or clear an ontology for clustering
   public void setOntology(Ontology clustOnt) {
      mOntology = clustOnt;
   }

   //Be able to choose the type of clustering method:
   //   Hierarchical
   //   OHClust
   //   others as they are implemented
   public void setClusterMethod() {
   }

   //Be able to modify the various distance metrics used:
   //   cluster metric
   //   isolate metric
   //   region metric
   //   pyroprint metric
   public void setMetrics() {
   }

   //Be able to modify what meta attributes are output and in what order
   public void setOutput() {
   }

   public List<Isolate> getIsolateData(Ontology ont, int dataSize) {
      List<Isolate> isoData = null;

      try {
         isoData = mConn.getIsolateData(dataSize, null);

         mConn.getIsolateMetaData(isoData, ont, dataSize);
         System.out.println("retrieved meta-data...");
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }

      return isoData;
   }

   public List<Isolate> getIsolateData(String dataSet) {
      List<Isolate> isoData = null;
      int dataSize = 0;

      try {
         if (dataSet != null) {
            String[] idList = dataSet.split(",");
            System.out.println(dataSet);
            System.out.println(idList.length);
            dataSize = idList.length;
         }

         isoData = mConn.getIsolateData(dataSize, dataSet);
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }

      return isoData;
   }
}
