package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.DataMetric;

import com.drin.java.clustering.dendogram.Dendogram;
import com.drin.java.clustering.dendogram.DendogramNode;
import com.drin.java.clustering.dendogram.DendogramLeaf;

import java.util.Collection;

public class HCluster extends Cluster {

   public HCluster(DataMetric<Cluster> metric) { super(metric); }

   public HCluster(DataMetric<Cluster> metric, Clusterable<?> elem) {
      this(metric);

      mElements.add(elem);
      mDendogram = new DendogramLeaf(elem);
   }

   public Cluster join(Cluster otherClust) {
      if (otherClust instanceof HCluster) {
         Cluster newCluster = new HCluster(this.mMetric);

         Collection<Clusterable<?>> otherData = ((HCluster)otherClust).mElements;
         Dendogram otherDend = ((HCluster)otherClust).mDendogram;

         newCluster.mElements.addAll(this.mElements);
         newCluster.mElements.addAll(otherData);

         newCluster.mDendogram = new DendogramNode(this.mDendogram, otherDend);

         return newCluster;
      }

      return null;
   }
}
