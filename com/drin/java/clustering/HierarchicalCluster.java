package com.drin.java.clustering;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.clustering.dendogram.DendogramNode;
import com.drin.java.clustering.dendogram.DendogramLeaf;

import java.util.Set;

public class HierarchicalCluster<E extends BaseClusterable> extends Cluster<E> {

   public HierarchicalCluster() { super(); }

   public HierarchicalCluster(E elem) {
      this();
      mElements.add(elem);
      mDendogram = new DendogramLeaf(elem);
   }

   public void join(HierarchicalCluster<E> otherClust) {
      mElements.addAll(otherClust.mElements);
      mDendogram = new DendogramNode(mDendogram, otherClust.mDendogram);
   }
}
