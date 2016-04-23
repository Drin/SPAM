package com.drin.ontology;

import java.util.Set;
import java.util.HashSet;

//TODO transition to TreeSet
/*
 * Ideally, in the future, a TreeSet can be used to represent an actual
 * ontology, which can then more efficiently drive the order of clustering and
 * the population of the ontological structure.
 */
public class OntologyLabel implements Labelable {
   private Set<String> mLabels;

   public OntologyLabel() { mLabels = new HashSet<String>(); }

   public OntologyLabel(final String[] labels) {
      mLabels = new HashSet<String>(labels.length);

      for (final String label : labels) {
         mLabels.add(label);
      }
   }

   public Set<String> getLabels() { return mLabels; }

   public void addLabel(String label) { mLabels.add(label); }
   public boolean hasLabel(String label) { return mLabels.contains(label); }

   public void addAll(OntologyLabel oldLabels) {
      mLabels.addAll(oldLabels.mLabels);
   }

}
