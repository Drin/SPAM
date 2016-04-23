package com.drin.analysis.clustering;

import com.drin.clustering.Cluster;

import java.util.List;

public interface Clusterer {
   public List<Cluster> clusterData(List<Cluster> clusters);
}
