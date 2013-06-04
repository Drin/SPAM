package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;

import javax.swing.JTextArea;

import java.util.Map;
import java.util.List;

public interface Clusterer {

   public Map<Double, List<Cluster>> getClusters();
   public String getName();
   public double getInterClusterSimilarity();

   public void clusterData(List<Cluster> clusters);
   public void setProgressCanvas(JTextArea canvas);
   public void writeProgress();
}
