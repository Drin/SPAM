package com.drin.java.util;

import com.drin.java.metrics.IsolateAverageMetric;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;

public class Configuration {
   private static final String DEFAULT_CONFIG_FILE = "props-standard.yml";
   private static Yaml mYamlReader = new Yaml(new Constructor(Configuration.class));
   private static Configuration mConfig = null;

   /*
    * Configuration Variables
    */
   private String clusterMetric, isoMetric, regionMetric, pyroMetric;
   private int pyroLength;
   private boolean debug, transform;

   public static Configuration loadConfig() {
      return mConfig == null ? loadConfig(DEFAULT_CONFIG_FILE) : mConfig;
   }

   public static Configuration loadConfig(String fileName) {
      FileInputStream configStream = null;

      try { configStream = new FileInputStream(new File(fileName)); }
      catch(java.io.FileNotFoundException err) { err.printStackTrace(); }

      mConfig = (Configuration) mYamlReader.load(configStream);

      return mConfig;
   }

   public static void main(String[] args) {
      Configuration config = Configuration.loadConfig();
      System.out.println(config);
      IsolateAverageMetric isoMetric = null;

      try {
         Class<?> isoMetricClass = Class.forName(config.getIsoMetric());
         isoMetric = (IsolateAverageMetric) isoMetricClass.newInstance();
      }
      catch(Exception err) {
         err.printStackTrace();
         System.exit(1);
      }

      isoMetric.testMethod();
   }

   /*
    * Setters
    */
   public void setClusterMetric(String metric) { clusterMetric = metric; }
   public void setIsoMetric(String metric) { isoMetric = metric; }
   public void setRegionMetric(String metric) { regionMetric = metric; }
   public void setPyroMetric(String metric) { pyroMetric = metric; }

   public void setPyroLength(int length) { pyroLength = length; }
   public void setTransform(boolean isTransform) { transform = isTransform; }
   public void setDebug(boolean isDebug) { debug = isDebug; }

   /*
    * Getters
    */
   public String getClusterMetric() { return clusterMetric; }
   public String getIsoMetric() { return isoMetric; }
   public String getRegionMetric() { return regionMetric; }
   public String getPyroMetric() { return pyroMetric; }

   public int getPyroLength() { return pyroLength; }
   public boolean getTransform() { return transform; }
   public boolean getDebug() { return debug; }

   public String toString() {
      return String.format("debug: %s\ntransform: %s\npyroLength: %d\n" +
                           "cluster metric: %s\niso metric: %s\n" +
                           "region Metric: %s\npyro metric: %s\n",
                           debug, transform, pyroLength, clusterMetric,
                           isoMetric, regionMetric, pyroMetric);
   }
}
