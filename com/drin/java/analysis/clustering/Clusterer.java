package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;

import java.util.Set;

public interface Clusterer {

   public void clusterData();
   public Set<Cluster> getClusters();
}
