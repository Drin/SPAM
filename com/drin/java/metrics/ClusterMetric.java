package com.drin.java.metrics;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.BaseClusterable;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.DataComparator;

import java.util.Map;

public abstract class ClusterMetric implements DataMetric<Cluster<BaseClusterable>> {
   protected DataComparator<BaseClusterable, DataMetric<BaseClusterable>> mComparator;
   protected DataMetric<BaseClusterable> mDataMetric;
   protected Double mResult;

   public ClusterMetric(DataComparator<BaseClusterable, DataMetric<BaseClusterable>> dataComparator,
    DataMetric<BaseClusterable> dataMetric) {
      mComparator = dataComparator;
      mDataMetric = dataMetric;
      mResult = null;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public abstract void apply(Cluster<BaseClusterable> data_A, Cluster<BaseClusterable> data_B);

   @Override
   public abstract void reset();

   @Override
   public Double result() {
      return mResult;
   }
}
