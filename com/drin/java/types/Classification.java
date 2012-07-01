package com.drin.java.types;

public class Classification {
   private String mClassification;

   public Classification() {
      mClassification = "unknown";
   }

   public Classification(String name) {
      mClassification = name;
   }

   public int hashCode() {
      return mClassification.hashCode();
   }

   public boolean equals(Object otherObject) {
      if (otherObject instanceof Classification) {
         Classification otherClass = (Classification) otherObject;

         return this.mClassification.equals(otherClass.mClassification);
      }

      return false;
   }

   public String toString() {
      return mClassification;
   }
}
