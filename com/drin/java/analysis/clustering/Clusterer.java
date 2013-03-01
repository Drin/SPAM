package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;

import javax.swing.JTextArea;

import java.util.Map;
import java.util.List;

public interface Clusterer {

   public void clusterData(JTextArea canvas);
   public Map<Double, List<Cluster>> getClusters();
}
