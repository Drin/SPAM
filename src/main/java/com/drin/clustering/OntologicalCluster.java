package com.drin.clustering;

import com.drin.clustering.Clusterable;

import com.drin.ontology.Labelable;
import com.drin.ontology.OntologyLabel;

import java.util.Set;

/**
 * OntologicalCluster implements Labelable functionality via composition, using
 * OntologyLabel.
 */
public class OntologicalCluster extends HierarchicalCluster implements Labelable {
   protected OntologyLabel mLabel;

   public OntologicalCluster(final Clusterable elem, final String[] labels) {
      super(elem);
      mLabel = new OntologyLabel(labels);
   }

   public OntologicalCluster(final Set<Clusterable> elements, final String[] labels) {
      super(elements);
      mLabel = new OntologyLabel(labels);
   }

   /*
    * This is for ontological labels. Clusters should have a set of labels that
    * is a superset of the labels of its data points.
    */
   public void addLabel(String label) { mLabel.addLabel(label); }
   public boolean hasLabel(String label) { return mLabel.hasLabel(label); }
}
