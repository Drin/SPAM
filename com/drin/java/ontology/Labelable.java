package com.drin.java.ontology;

import java.util.Map;

public interface Labelable {
   public Map<String, String> getLabels();
   public void addLabel(String labelName, String labelValue);
   public boolean hasLabel(String labelName);
   public String getLabelValue(String labelName);
}
