package com.drin.java.clustering.dendogram;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.dendogram.Dendogram;
import com.drin.java.clustering.dendogram.DendogramNode;

public class DendogramLeaf implements Dendogram {
   private BaseClusterable mElement;

   public DendogramLeaf(BaseClusterable element) { mElement = element; }

   public BaseClusterable getData() { return mElement; }

   public Dendogram join(Dendogram otherDend) {
      return new DendogramNode(this, otherDend);
   }
}
