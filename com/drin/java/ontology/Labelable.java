package com.drin.java.ontology;

import java.util.Map;

public interface Labelable {
   public Map<String, Boolean> getLabels();
   public void addLabel(String labelName);
   public boolean hasLabel(String labelName);
}
