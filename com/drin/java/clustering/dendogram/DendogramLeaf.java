package com.drin.java.clustering.dendogram;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.dendogram.Dendogram;
import com.drin.java.clustering.dendogram.DendogramNode;

@SuppressWarnings("rawtypes")
public class DendogramLeaf implements Dendogram {
   private Clusterable mElement;

   public DendogramLeaf(Clusterable element) { mElement = element; }

   public Clusterable getData() { return mElement; }

   public Dendogram join(Dendogram otherDend) {
      return new DendogramNode(this, otherDend);
   }
}
