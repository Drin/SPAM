package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.ontology.Ontology;

import java.util.Map;
import java.util.List;

public interface Clusterer {
   public String getName();
   public float getThreshold();
   public float getInterStrainSim();
   public Map<Float, List<Cluster>> getClusters();

   public void clusterData(List<Cluster> clusters);
   public void clusterData(Ontology clustOnt);
}
