package com.drin.java.analysis.clustering;

import com.drin.java.types.Cluster;
import com.drin.java.analysis.clustering.ClusterAnalyzer;

import java.util.Set;

public interface Clusterer {

   public void clusterData();
   public String[] getResults(ClusterAnalyzer analyzer);
}
