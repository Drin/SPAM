package com.drin.java.metrics;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.DataComparator;

import java.util.Map;

public abstract class ClusterMetric<E extends BaseClusterable> implements
                      DataMetric<Cluster<E>> {
   protected double mResult;
   protected int mErrCode;

   protected DataComparator<DataMetric<E>, E> mComparator;
   protected DataMetric<E> mDataMetric;

   public ClusterMetric(DataComparator<DataMetric<E>, E> dataComparator,
                        DataMetric<E> dataMetric) {
      mComparator = dataComparator;
      mDataMetric = dataMetric;

      this.reset();
   }

   @Override
   public abstract void apply(Cluster<E> data_A, Cluster<E> data_B);

   @Override
   public void reset() { mResult = 0; }

   @Override
   public double result() {
      double result = mResult;

      this.reset();
      return result;
   }

   @Override
   public void setError(int errCode) { mErrCode = errCode; }

   @Override
   public int getError() { return mErrCode; }
}
