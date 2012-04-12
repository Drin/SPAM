package spam.analysis.clustering;

import spam.types.Cluster;

import java.util.Set;

public interface Clusterer {

   public Set<Cluster> clusterData();
}
