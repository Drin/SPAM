package com.drin.java.ontology;

import java.util.Map;
import java.util.HashMap;

public class OntologyLabel implements Labelable {
   protected Map<String, String> mLabelMap;

   public OntologyLabel() {
      mLabelMap = new HashMap<String, String>();
   }

   public Map<String, String> getLabels() { return mLabelMap; }

   public void addLabel(String labelName, String labelValue) {
      mLabelMap.put(labelName, labelValue);
   }

   public boolean hasLabel(String labelName) {
      return mLabelMap.containsKey(labelName);
   }

   public String getLabelValue(String labelName) {
      if (hasLabel(labelName)) {
         return mLabelMap.get(labelName);
      }

      return null;
   }

   public void addAll(OntologyLabel oldLabels) {
      mLabelMap.putAll(oldLabels.getLabels());
   }
}
