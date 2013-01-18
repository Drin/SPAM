package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;

import javax.swing.JTextArea;
import java.util.Set;

public interface Clusterer {

   public void clusterData(JTextArea canvas);
   public Set<Cluster> getClusters();
}
