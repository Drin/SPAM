package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.DataMetric;

import com.drin.java.clustering.dendogram.Dendogram;
import com.drin.java.clustering.dendogram.DendogramNode;
import com.drin.java.clustering.dendogram.DendogramLeaf;

import com.drin.java.util.Logger;

import java.util.Collection;

public class HCluster extends Cluster {

   public HCluster(DataMetric<Cluster> metric) { super(metric); }

   public HCluster(DataMetric<Cluster> metric, Clusterable<?> elem) {
      this(metric);

      mElements.add(elem);
      mDendogram = new DendogramLeaf(elem);
   }

   /*
   @Override
   public String getDesignation() {
      //Since elements is a set it's necessary to use the iterator.. quite a
      //bit of overhead but maybe this will be fixed later.
      if (mElements.size() == 1) {
         for (Clusterable<?> clust : mElements) {
            return clust.getDesignation();
         }
      }

      return "";
   }
   */

   public void computeStatistics() {
      double minSim = Double.MAX_VALUE, total = 0;
      int numComparisons = 0;

      for (Clusterable<?> elem_A : mElements) {
         for (Clusterable<?> elem_B : mElements) {
            if (elem_A.getName().equals(elem_B.getName())) { continue; }

            double comparison = elem_A.compareTo(elem_B);

            total += comparison;
            numComparisons++;

            if (comparison < minSim) { minSim = comparison; }
         }
      }

      mDiameter = minSim;
      mMean = numComparisons > 0 ? total / numComparisons : 0;
   }

   public Cluster join(Cluster otherClust) {
      if (otherClust instanceof HCluster) {
         Cluster newCluster = new HCluster(this.mMetric);

         Collection<Clusterable<?>> otherData = ((HCluster)otherClust).mElements;

         newCluster.mElements.addAll(this.mElements);
         newCluster.mElements.addAll(otherData);

         newCluster.computeStatistics();

         Dendogram otherDend = ((HCluster)otherClust).mDendogram;
         newCluster.mDendogram = new DendogramNode(this.mDendogram, otherDend, newCluster);

         return newCluster;
      }

      return null;
   }
}
