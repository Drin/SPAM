package com.drin.java.analysis.clustering;

import com.drin.java.clustering.Cluster;
import com.drin.java.ontology.Ontology;
import com.drin.java.output.ProgressWriter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public abstract class HierarchicalClusterer implements Clusterer {
   protected String mName;
   protected float mThresh;
   protected ProgressWriter mWriter;
   protected Map<Float, List<Cluster>> mResultClusters;

   public HierarchicalClusterer(int dataSize, float threshold,
                                ProgressWriter writer) {
      mName = "Hierarchical";
      mThresh = threshold;
      mWriter = writer;

      mResultClusters = new HashMap<Float, List<Cluster>>(dataSize);
   }

   public String getName() { return mName; }
   public float getThreshold() { return mThresh; }
   public Map<Float, List<Cluster>> getClusters() { return mResultClusters; }

   public float getInterStrainSim() {
      float totalClusterSimilarity = 0.0f;
      int similarityCount = 0;

      List<Cluster> clusters = mResultClusters.get(mThresh);
      for (int clustNdxA = 0; clustNdxA < clusters.size(); clustNdxA++) {
         Cluster clustA = clusters.get(clustNdxA);

         for (int clustNdxB = clustNdxA + 1; clustNdxB < clusters.size(); clustNdxB++) {
            Cluster clustB = clusters.get(clustNdxB);

            totalClusterSimilarity += clustA.compareTo(clustB);
            similarityCount++;
         }
      }

      if (similarityCount <= 0) { return -2.0f; }
      return totalClusterSimilarity/similarityCount;
   }

   /*
    * Methods for invoking clustering
    */
   public void clusterData(Ontology clustOnt) {
      throw new UnsupportedOperationException();
   }

   public void clusterData(List<Cluster> clusters) {
      mResultClusters.clear();

      clusterDataSet(clusters, mThresh);
      mResultClusters.put(mThresh, clusters);
   }

   protected void clusterDataSet(List<Cluster> clusters, float threshold) {
      int initialSize = clusters.size();
      float iterCount = 0.0f;
      boolean combineSuccess = false;

      do {
         if (mWriter != null) {
            mWriter.writeProgress((iterCount++)/initialSize);
         }

         ClusterPair clustPair = findCloseClusters(clusters, threshold);
         combineSuccess = combineClusters(clusters, clustPair);

      } while (combineSuccess && clusters.size() > 1);

      if (mWriter != null) { mWriter.writeProgress(1.0f); }
   }

   /*
    * Abstract Methods
    */
   protected abstract ClusterPair findCloseClusters(List<Cluster> clusters, float threshold);
   protected abstract boolean combineClusters(List<Cluster> clusters, ClusterPair clustPair);

   public class ClusterPair {
      public int mClustANdx, mClustBNdx;
      public float mClustSim;

      public ClusterPair(int clustANdx, int clustBNdx, float clustSim) {
         mClustANdx = clustANdx;
         mClustBNdx = clustBNdx;
         mClustSim = clustSim;
      }
   }
}
