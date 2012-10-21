package com.drin.java.analysis.clustering;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.Cluster;

import java.util.Set;

public interface Clusterer<E extends BaseClusterable> {

   public void clusterData();
   public Set<Cluster<E>> getClusters();
}
