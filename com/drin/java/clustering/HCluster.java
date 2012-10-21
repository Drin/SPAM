package com.drin.java.clustering;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.clustering.dendogram.DendogramNode;
import com.drin.java.clustering.dendogram.DendogramLeaf;

import java.util.Set;

public class HCluster<E extends BaseClusterable> extends Cluster<E> {

   public HCluster() { super(); }

   public HCluster(E elem) {
      this();
      mElements.add(elem);
      mDendogram = new DendogramLeaf(elem);
   }

   public HCluster<E> join(HCluster<E> otherClust) {
      HCluster<E> newCluster = new HCluster<E>();

      newCluster.mElements.addAll(this.mElements);
      newCluster.mElements.addAll(otherClust.mElements);

      newCluster.mDendogram = new DendogramNode(this.mDendogram,
                                                otherClust.mDendogram);

      return newCluster;
   }
}
