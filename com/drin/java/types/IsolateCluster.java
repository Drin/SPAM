package com.drin.java.types;

import com.drin.java.types.Cluster;
import com.drin.java.types.Isolate;
import com.drin.java.metrics.DataMetric;

public class IsolateCluster extends Cluster<Isolate> {
   public IsolateCluster(String name) {
      super(name);
   }

   public IsolateCluster(String name, Isolate isolate) {
      super(name, isolate);
   }

   @Override
   public Cluster<Isolate> union(Cluster<Isolate> otherCluster) {
      mElements.addAll(otherCluster.mElements);

      return this;
   }

   @Override
   public String getDendogram(DataMetric dataMetric) {
      String prefix = "   ", dendogram = "";

      dendogram += String.format("%s<Cluster name=\"%s\">\n",
       prefix, getElement(0).getName());

      dendogram += String.format("%s<Isolate name=\"%s\"/>\n",
       prefix + "   ", getElement(0).getName());

      dendogram += String.format("%s<Cluster/>\n", prefix);

      return dendogram;
   }
}
