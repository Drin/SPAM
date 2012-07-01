package com.drin.java.types;

import java.util.Map;

public class DataObject {
   protected String mName;
   protected Map<String, Object> mFeatureMap;

   public DataObject(String name, Map<String, Object> featureMap) {
      mName = name;
      mFeatureMap = featureMap;
   }

   public DataObject(String name) {
      mName = name;
      mFeatureMap = null;
   }

   public String getName() {
      return mName;
   }

   public Object getField(String fieldName) {
      if (mFeatureMap != null) {
         return mFeatureMap.get(fieldName);
      }

      return null;
   }

   public String getString(String fieldName) {
      if (mFeatureMap != null) {
         return String.valueOf(mFeatureMap.get(fieldName));
      }

      return null;
   }

   public Double getDouble(String fieldName) {
      if (mFeatureMap != null) {
         try {
            return Double.parseDouble(String.valueOf(mFeatureMap.get(fieldName)));
         }
      
         catch (NumberFormatException numErr) {
            System.err.printf("Field '%s' is not a double value\n", fieldName);
         }
      }

      return null;
   }

   @Override
   public int hashCode() {
      return mName.hashCode();
   }

   @Override
   public boolean equals(Object otherObject) {
      if (otherObject instanceof DataObject) {
         DataObject otherData = (DataObject) otherObject;

         if (!this.mName.equals(otherData.mName)) { return false; }
         
         if (mFeatureMap == null) { return true; }

         for (String featureName : mFeatureMap.keySet()) {
            if (mFeatureMap.containsKey(featureName) &&
             otherData.mFeatureMap.containsKey(featureName) &&
             mFeatureMap.get(featureName).equals(
             otherData.mFeatureMap.get(featureName))) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public String toString() {
      return mName;
   }
}
