package com.drin.analysis.classification;

import com.drin.types.DataObject;
import com.drin.types.ComparisonValue;
import com.drin.types.Classification;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class KNN {
   private int mNumNeighbors;
   private List<DataObject> mDataListing;

   public KNN(int kVal) {
      mNumNeighbors = kVal;
      mDataListing = new ArrayList<DataObject>();
   }

   public KNN(int kVal, List<DataObject> inputData) {
      mNumNeighbors = kVal;
      mDataListing = inputData;
   }

   public void classifyData() {
      for (DataObject dataItem : mDataListing) {
         List<DataObject> neighborList = this.getNeighbors(dataItem);

         System.out.printf("DataItem: %s\n", dataItem);
         
         for (DataObject neighbor : neighborList) {
            System.out.printf("\tneighbor: %s\n", neighbor);
         }

         dataItem.setClassification(this.getPlurality(neighborList));
      }
   }

   private List<DataObject> getNeighbors(DataObject dataItem) {
      List<Map<DataObject, ComparisonValue>> neighborList =
       new ArrayList<Map<DataObject, ComparisonValue>>();
      int neighborNdx = 0;

      for (DataObject dataItem2 : mDataListing) {
         ComparisonValue val = dataItem.compareTo(dataItem2);
         Object[] neighborInfo = new Object[] { dataItem2, val };

         for (neighborNdx = 0; neighborNdx < neighborList.size(); neighborNdx++) {
            Object[] neighbor = neighborList.get(neighborNdx);

            if (dataItem.getMostSimilar(dataItem2, neighbor).equals(dataItem2)) {
               break;
            }
         }

         neighborList.add(neighborNdx, dataItem2);
         /*
         for (DataObject dataVal : neighborList) {
            System.out.printf("%s, ", dataVal);
         }
         System.out.printf("\n");
         */
      }

      return neighborList;
   }

   private Classification getPlurality(List<DataObject> neighborList) {
      Map<Classification, Integer> occurrenceCount = new HashMap<Classification, Integer>();

      for (int neighborNdx = 0; neighborNdx < neighborList.size() &&
       neighborNdx < mNumNeighbors; neighborNdx++) {
         Classification neighborClass = neighborList.get(neighborNdx).getClassification();

         if (!occurrenceCount.containsKey(neighborClass)) {
            occurrenceCount.put(neighborClass, 0);
         }

         int count = occurrenceCount.get(neighborClass);
         occurrenceCount.put(neighborClass, count + 1);
      }

      Classification plurality = null;
      int maxClassCount = 0;

      for (Classification dataClass : occurrenceCount.keySet()) {
         int classCount = occurrenceCount.get(dataClass);

         if (classCount > maxClassCount) {
            maxClassCount = classCount;
            plurality = dataClass;
         }
      }

      return plurality;
   }
}
