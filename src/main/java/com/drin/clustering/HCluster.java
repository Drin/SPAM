package com.drin.clustering;

import com.drin.clustering.Clusterable;
import com.drin.clustering.Cluster;

import java.util.Set;
import java.util.HashSet;

public class HierarchicalCluster extends Cluster {

   public HierarchicalCluster(final Clusterable elem) { super(elem); }

   public HierarchicalCluster(final Set<Clusterable> elements) {
      super(elements);
   }

   public HierarchicalCluster(final Cluster leftClust, final Cluster rightClust) {
      this(new HashSet<Clusterable>(leftClust.size() + rightClust.size()) {{
         addAll(leftClust.mElements);
         addAll(rightClust.mElements);
      }});
   }
}
