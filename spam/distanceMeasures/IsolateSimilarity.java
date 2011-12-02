package TemporalHClustering.distanceMeasures;

import TemporalHClustering.dataStructures.IsolateSimilarityMatrix;
import TemporalHClustering.dataTypes.Isolate;
import TemporalHClustering.dataTypes.SampleMethod;

public class IsolateSimilarity {

   public static double getCorrelation(IsolateSimilarityMatrix matrix, Isolate sample1, Isolate sample2) {
      return matrix.getCorrelationVal(sample1, sample2);
   }

   public static int getSimilarity(IsolateSimilarityMatrix matrix1, IsolateSimilarityMatrix matrix2,
    Isolate sample1, Isolate sample2) {
      double correlation1 = IsolateSimilarity.getCorrelation(matrix1, sample1, sample2);
      double correlation2 = IsolateSimilarity.getCorrelation(matrix2, sample1, sample2);

      SimilarityScore similarity1 = SimilarityScore.getScore(correlation1);
      SimilarityScore similarity2 = SimilarityScore.getScore(correlation2);

      return similarity1.combine(similarity2);
   }

   private enum SimilarityScore {
      MATCH(7), PROBABLE(3), UNPROBABLE(1), UNMATCH(0);

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
