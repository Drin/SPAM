package com.drin.clustering;

import java.util.Set;
import java.util.HashSet;

public abstract class Cluster {
   private Set<Clusterable> mElements;

   public Cluster(final Clusterable elem) {
      mElements = new HashSet<Clusterable>(1) {{
         add(elem);
      }};
   }

   public Cluster(final Set<Clusterable> elements) {
      mElements = new HashSet<Clusterable>(elements.size()) {{
         addAll(elements);
      }};
   }

   public int size() { return mElements.size(); }
}
