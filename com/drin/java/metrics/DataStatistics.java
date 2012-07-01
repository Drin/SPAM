package com.drin.java.metrics;

import com.drin.java.types.DataObject;
import java.util.List;

public interface DataStatistics {
   /**
    * Calculate the minimum data point in the given list of data points.
    */
   public double calcMin(List<DataObject> dataSet);

   /**
    * Calculate the maximum data point in the given list of data points.
    */
   public double calcMax(List<DataObject> dataSet);

   /**
    * Calculate the mean value across all data points in the given list of data points.
    */
   public double calcMean(List<DataObject> dataSet);

   /**
    * Calculate the lower quartile data point in the given list of data points.
    */
   public double calcQ1(List<DataObject> dataSet);

   /**
    * Calculate the median data point in the given list of data points.
    */
   public double calcMed(List<DataObject> dataSet);

   /**
    * Calculate the upper quartile data point in the given list of data points.
    */
   public double calcQ3(List<DataObject> dataSet);

   /**
    * Calculate the standard deviation of all data points in the given list of data points.
    */
   public double calcStdDev(List<DataObject> dataSet);

   /**
    * Calculate the correlation between two lists of data points.
    */
   public double calcCorrelation(List<DataObject> dataSet_A, List<DataObject> dataSet_B);
}
