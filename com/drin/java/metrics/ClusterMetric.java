package com.drin.java.metrics;

import com.drin.java.types.Cluster;
import com.drin.java.types.DataObject;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.DataComparator;

import java.util.Map;

public abstract class ClusterMetric implements DataMetric<Cluster<DataObject>> {
   protected DataComparator<DataObject, DataMetric<DataObject>> mComparator;
   protected DataMetric<DataObject> mDataMetric;
   protected Double mResult;

   public ClusterMetric(DataComparator<DataObject, DataMetric<DataObject>> dataComparator,
    DataMetric<DataObject> dataMetric) {
      mComparator = dataComparator;
      mDataMetric = dataMetric;
      mResult = null;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public abstract void apply(Cluster<DataObject> data_A, Cluster<DataObject> data_B);

   @Override
   public abstract void reset();

   @Override
   public Double result() {
      return mResult;
   }
}
