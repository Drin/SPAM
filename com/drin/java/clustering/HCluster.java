package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.DataMetric;

public class HCluster extends Cluster {
   public HCluster(int clustSize, DataMetric<Cluster> metric) {
      super(clustSize, metric);
   }

   public HCluster(DataMetric<Cluster> metric, Clusterable<?> elem) {
      super(1, metric);

      mSize = 1;
      mElements.add(elem);
      mMetaLabels = elem.getMetaData();
   }

   public HCluster(HCluster oldCluster) { super(oldCluster); }

   @Override
   public void join(Cluster otherClust) {
      if (otherClust instanceof HCluster) {
         mElements.addAll(otherClust.mElements);

         for (int labelNdx = 0; labelNdx < otherClust.mMetaLabels.length; labelNdx++) {
            mMetaLabels[labelNdx] += (", " + otherClust.mMetaLabels[labelNdx]);
         }

         mSize = mElements.size();
      }
      else {
         System.err.println("Error incorporating cluster");
         System.exit(1);
      }
   }
}
