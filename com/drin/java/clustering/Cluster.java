package com.drin.java.clustering;

import com.drin.java.ontology.Labelable;
import com.drin.java.ontology.OntologyLabel;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.dendogram.Dendogram;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public abstract class Cluster implements Labelable {
   private static int CLUST_ID = 1;
   private String mName;

   protected OntologyLabel mLabel;
   protected DataMetric<Cluster> mMetric;
   protected Dendogram mDendogram;
   protected Set<Clusterable<?>> mElements;
   protected double mDiameter, mMean;

   public Cluster(DataMetric<Cluster> metric) { this(CLUST_ID++, metric); }
   public Cluster(int clustId, DataMetric<Cluster> metric) {
      mName = String.format("%d", clustId);
      mMetric = metric;
      
      mDendogram = null;
      mLabel = new OntologyLabel();
      mElements = new HashSet<Clusterable<?>>();

      mDiameter = -2;
      mMean = -2;
   }

   public static void resetClusterIDs() { Cluster.CLUST_ID = 1; }
   public String getName() { return mName; }
   public int size() { return mElements.size(); }
   public double getDiameter() { return mDiameter; }
   public double getMean() { return mMean; }

   public abstract void computeStatistics();
   public abstract Cluster join(Cluster otherClust);

   public Dendogram getDendogram() { return mDendogram; }
   public Set<Clusterable<?>> getElements() { return mElements; }
   public void add(Clusterable<?> element) { mElements.add(element); }

   /*
    * This is for ontological labels. Clusters should have a set of labels that
    * is a superset of the labels of its data points.
    */
   public Map<String, Boolean> getLabels() { return mLabel.getLabels(); }
   public void addLabel(String label) { mLabel.addLabel(label); }
   public boolean hasLabel(String label) { return mLabel.hasLabel(label); }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof Cluster) {
         return mName.equals(((Cluster) otherObj).getName());
      }

      return false;
   }

   public double compareTo(Cluster otherClust) {
      mMetric.apply(this, otherClust);
      double comparison = mMetric.result();

      Logger.error(mMetric.getError(), String.format("error computing metric" +
                                       " between '%s' and '%s'\n", this.mName,
                                       otherClust.mName));

      return comparison;
   }

   @Override
   public String toString() {
      String str = this.getName() + ": ";

      for (Clusterable<?> element : mElements) {
         str += String.format("%s, ", element);
      }

      return str;
   }

   public String prettyPrint(String prefix) {
      String str = this.getName() + ":\n";

      for (Clusterable<?> element : mElements) {
         str += String.format("%s\n", element);
      }

      return str;
   }

}
