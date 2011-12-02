package TemporalHClustering.dataStructures;

import TemporalHClustering.dataTypes.IsolateRegion;
import TemporalHClustering.dataTypes.Isolate;
import TemporalHClustering.dataTypes.IsolateCorrelation;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class IsolateSimilarityMatrix {
   private Map<Isolate, Map<Isolate, IsolateCorrelation>> mSimilarityMatrix = null;
   //mIsolateMappingping maps days to the list of isolates collected on that day
   private Map<String, Map<Integer, List<Isolate>>> mIsolateMapping = null;

   private final double INVALID_MAPPING = -2;
   private double distThreshold_16_23_upper = -1, distThreshold_23_5_upper = -1;
   private double distThreshold_16_23_lower = -1, distThreshold_23_5_lower = -1;

   public IsolateSimilarityMatrix() {
      super();

      mSimilarityMatrix = new HashMap<Isolate, Map<Isolate, IsolateCorrelation>>();
      mIsolateMapping = new HashMap<String, Map<Integer, List<Isolate>>>();
   }

   public int size() {
      return mSimilarityMatrix.size();
   }

   public Map<String, Map<Integer, List<Isolate>>> getIsolateMap() {
      return mIsolateMapping;
   }

   public Map<Isolate, Map<Isolate, IsolateCorrelation>> getSimilarityMatrix() {
      return mSimilarityMatrix;
   }

   public IsolateCorrelation getCorrelation(Isolate isolateOne, Isolate isolateTwo) {
      if (!mSimilarityMatrix.containsKey(isolateOne) ||
       !mSimilarityMatrix.get(isolateOne).containsKey(isolateTwo)) {
         return null;
      }

      return mSimilarityMatrix.get(isolateOne).get(isolateTwo);
   }

   public double getCorrelationVal(Isolate isolateOne, Isolate isolateTwo) {
      IsolateCorrelation correlation = getCorrelation(isolateOne, isolateTwo);

      if (correlation == null) {
         correlation = getCorrelation(isolateTwo, isolateOne);
      }


      /*
      if (correlation != null) {
         System.out.printf("correlation between %s and %s: 16-23: %.03f 23-5: %.03f aggregate: %.03f\n",
          isolateOne, isolateTwo, correlation.get16_23(), correlation.get23_5(), correlation.getCorrelation());
      }
      */

      return correlation != null ? correlation.getCorrelation() : 0;
   }

   public void transformCorrelation(Isolate isolateOne, Isolate isolateTwo, double upperThreshold, double lowerThreshold) {
      IsolateCorrelation correlation = getCorrelation(isolateOne, isolateTwo);

      if (correlation == null) {
         correlation = getCorrelation(isolateTwo, isolateOne);
      }

      if (correlation != null) {
         if (correlation.getCorrelation() > upperThreshold) {
            correlation.set16_23(100);
            correlation.set23_5(100);
         }
         else if (correlation.getCorrelation() < lowerThreshold) {
            correlation.set16_23(0);
            correlation.set23_5(0);
         }
      }
   }

   public void setUpperDistanceThreshold(IsolateRegion region, double distThreshold) {
      switch (region) {
         case ITS_16_23:
            distThreshold_16_23_upper = distThreshold;
            break;
         case ITS_23_5:
            distThreshold_23_5_upper = distThreshold;
            break;
         default:
            System.err.println("Invalid region: " + region);
            break;
      }
   }

   public void setLowerDistanceThreshold(IsolateRegion region, double distThreshold) {
      switch (region) {
         case ITS_16_23:
            distThreshold_16_23_lower = distThreshold;
            break;
         case ITS_23_5:
            distThreshold_23_5_lower = distThreshold;
            break;
         default:
            System.err.println("Invalid region: " + region);
            break;
      }
   }

   public double getUpperDistanceThreshold(IsolateRegion region) {
      switch (region) {
         case ITS_16_23:
            return distThreshold_16_23_upper;
         case ITS_23_5:
            return distThreshold_23_5_upper;
         default:
            System.err.println("Invalid region: " + region);
            break;
      }

      return -1;
   }

   public double getLowerDistanceThreshold(IsolateRegion region) {
      switch (region) {
         case ITS_16_23:
            return distThreshold_16_23_lower;
         case ITS_23_5:
            return distThreshold_23_5_lower;
         default:
            System.err.println("Invalid region: " + region);
            break;
      }

      return -1;
   }

   /*
   public boolean isAboveThresholds(Isolate sample1, Isolate sample2) {
      Map<Isolate, Double> correlationMap_16_23 = null;
      Map<Isolate, Double> correlationMap_23_5 = null;

      double ITS_16_23_val = 0, ITS_23_5_val = 0;

      /*
       * Its possible that using 3 for a missing region is not the correct
       * thing to do
       *

      if (mSimilarityMatrix_16_23.containsKey(sample1)) {
         correlationMap_16_23 = mSimilarityMatrix_16_23.get(sample1);

         //TODO changed the return value of standardizeCorrelation to be the
         //value passed to it. This is because I think I should troubleshoot
         //against the correlations to be sure I'm getting similar results as
         //what I had gotten previously

         ITS_16_23_val = correlationMap_16_23.containsKey(sample2) ?
          standardizeCorrelation(correlationMap_16_23.get(sample2)) : 0;

         //System.out.printf("16_23: %.03f, 16_23_corr: %.03f\n",
          //ITS_16_23_val, correlationMap_16_23.get(sample2));
      }
      
      if (mSimilarityMatrix_23_5.containsKey(sample1)) {
         correlationMap_23_5 = mSimilarityMatrix_23_5.get(sample1);

         ITS_23_5_val = correlationMap_23_5.containsKey(sample2) ?
          standardizeCorrelation(correlationMap_23_5.get(sample2)) : 0;
      }

      return ITS_16_23_val >= distThreshold_16_23 && ITS_23_5_val >= distThreshold_23_5;
   }

   public boolean hasCorrelation(Isolate sample1, Isolate sample2) {
      boolean has16_23Correlation = mSimilarityMatrix_16_23.containsKey(sample1) &&
       mSimilarityMatrix_16_23.get(sample1).containsKey(sample2);

      boolean has23_5Correlation = mSimilarityMatrix_23_5.containsKey(sample1) &&
       mSimilarityMatrix_23_5.get(sample1).containsKey(sample2);

      return has16_23Correlation || has23_5Correlation;
   }
   */

   public boolean hasCorrelation(Isolate isolate_A, Isolate isolate_B) {
      if ((mSimilarityMatrix.containsKey(isolate_A) &&
       mSimilarityMatrix.get(isolate_A).containsKey(isolate_B)) ||
       (mSimilarityMatrix.containsKey(isolate_B) &&
       mSimilarityMatrix.get(isolate_B).containsKey(isolate_A))) {
         return true;
      }

      return false;
   }

   public void addCorrelation(IsolateCorrelation correlation) {
      Isolate isolate_A = correlation.getIsolateOne();
      Isolate isolate_B = correlation.getIsolateTwo();

      if (!mSimilarityMatrix.containsKey(isolate_A)) {
         mSimilarityMatrix.put(isolate_A, new HashMap<Isolate, IsolateCorrelation>());
      }

      mSimilarityMatrix.get(isolate_A).put(isolate_B, correlation);

      addIsolate(isolate_A);
      addIsolate(isolate_B);
   }

   public IsolateCorrelation removeCorrelation(Isolate isolate_A, Isolate isolate_B) {
      IsolateCorrelation removedCorr = null;

      if (mSimilarityMatrix.containsKey(isolate_A) && mSimilarityMatrix.get(isolate_A).containsKey(isolate_B)) {
         removedCorr = mSimilarityMatrix.get(isolate_A).remove(isolate_B);
      }
      if (mSimilarityMatrix.containsKey(isolate_B) && mSimilarityMatrix.get(isolate_B).containsKey(isolate_A)) {
         mSimilarityMatrix.get(isolate_B).remove(isolate_A);
      }

      return removedCorr;
   }

   private void addIsolate(Isolate isolate) {
      if (!mIsolateMapping.containsKey(isolate.getTechnician())) {
         mIsolateMapping.put(isolate.getTechnician(), new LinkedHashMap<Integer, List<Isolate>>());
      }

      Map<Integer, List<Isolate>> innerIsolateMapping = mIsolateMapping.get(isolate.getTechnician());

      if (!innerIsolateMapping.containsKey(isolate.getDay())) {
         innerIsolateMapping.put(isolate.getDay(), new ArrayList<Isolate>());
      }

      if (!innerIsolateMapping.get(isolate.getDay()).contains(isolate)) {
         innerIsolateMapping.get(isolate.getDay()).add(isolate);
      }
   }

   /*
   public void addSimilarity(IsolateRegion region, Isolate sample1, Isolate sample2, double similarity) {
      add(region, sample1, sample2, similarity);
      add(region, sample2, sample1, similarity);
   }

   private void add(IsolateRegion region, Isolate sample1, Isolate sample2, double similarity) {
      Map<Isolate, Map<Isolate, Double>> mSimilarityMatrix = null;
      Map<Isolate, Double> correlationMap = null;

      switch (region) {
         case ITS_16_23:
            mSimilarityMatrix = mSimilarityMatrix_16_23;
            break;
         case ITS_23_5:
            mSimilarityMatrix = mSimilarityMatrix_23_5;
            break;
         default:
            System.err.println("Invalid region: " + region);
            break;
      }

      //the isolate does not yet exist in the mapping, add a new map for it
      correlationMap = mSimilarityMatrix.containsKey(sample1) ?
       mSimilarityMatrix.get(sample1) : new HashMap<Isolate, Double>();

      //add the similarity value between sample 1 and sample 2
      correlationMap.put(sample2, similarity);
      //associate sample2 with sample 1
      mSimilarityMatrix.put(sample1, correlationMap);
   }

   public double getSimilarity(Isolate sample1, Isolate sample2) {
      Map<Isolate, Double> correlationMap_16_23 = null;
      Map<Isolate, Double> correlationMap_23_5 = null;

      double ITS_16_23_val = 0, ITS_23_5_val = 0;

      /*
       * Its possible that using 3 for a missing region is not the correct
       * thing to do
       *

      if (mSimilarityMatrix_16_23.containsKey(sample1)) {
         correlationMap_16_23 = mSimilarityMatrix_16_23.get(sample1);

         //TODO changed the return value of standardizeCorrelation to be the
         //value passed to it. This is because I think I should troubleshoot
         //against the correlations to be sure I'm getting similar results as
         //what I had gotten previously

         ITS_16_23_val = correlationMap_16_23.containsKey(sample2) ?
          standardizeCorrelation(correlationMap_16_23.get(sample2)) : 0;

         //System.out.printf("16_23: %.03f, 16_23_corr: %.03f\n",
          //ITS_16_23_val, correlationMap_16_23.get(sample2));
      }
      
      if (mSimilarityMatrix_23_5.containsKey(sample1)) {
         correlationMap_23_5 = mSimilarityMatrix_23_5.get(sample1);

         ITS_23_5_val = correlationMap_23_5.containsKey(sample2) ?
          standardizeCorrelation(correlationMap_23_5.get(sample2)) : 0;
      }

      double total = ITS_16_23_val + ITS_23_5_val;

      if (ITS_16_23_val > 0 && ITS_23_5_val > 0) {
         return total / 2;
      }

      return total;
   }
   */

   private double standardizeCorrelation(Double correlation) {
      return correlation;
      /*
      if (correlation < 95) return 0;
      else if (correlation < 98) return 0;
      else if (correlation < 99.7) return 3;
      else return 5;
      */
   }

   //TODO add toString()
   private enum SimilarityScore {
      //MATCH(7), PROBABLE(3), UNPROBABLE(1), UNMATCH(0);
      MATCH(5), PROBABLE(3), UNPROBABLE(0), UNMATCH(0);

      private int mScore = -1;

      private SimilarityScore(int score) {
         mScore = score;
      }

      public static SimilarityScore getScore(double correlation) {
         if (correlation < 95) return UNMATCH;
         else if (correlation < 98) return UNPROBABLE;
         else if (correlation < 99.7) return PROBABLE;
         else return MATCH;
      }

      public int combine(SimilarityScore otherRegionScore) {
         return mScore + otherRegionScore.mScore;
      }
   }
}
