package spam.dataTypes;

import spam.dataStructures.IsolateSimilarityMatrix;

public class FileSettings {
   private IsolateRegion mRegion;
   private double mDistThreshold, mLowerThreshold, mUpperThreshold;
   private Cluster.distType mClusterDistType;
   private IsolateSimilarityMatrix mSimilarityMatrix;

   public FileSettings() {
      super();
   }

   public void setRegion(IsolateRegion region) {
      if (region == null) {
         System.err.println("Invalid isolate region. exiting...");
         System.exit(1);
      }

      mRegion = region;
   }

   public IsolateRegion getRegion() {
      return mRegion;
   }

   public void setLowerThreshold(double lowThreshold) {
      mLowerThreshold = lowThreshold;
   }

   public double getLowerThreshold() {
      return mLowerThreshold;
   }

   public void setUpperThreshold(double highThreshold) {
      mUpperThreshold = highThreshold;
   }

   public double getUpperThreshold() {
      return mUpperThreshold;
   }

   public void setDistanceType(Cluster.distType distanceType) {
      mClusterDistType = distanceType;
   }

   public Cluster.distType getDistanceType() {
      return mClusterDistType;
   }

   public void setSimilarityMatrix(IsolateSimilarityMatrix similarityMatrix) {
      mSimilarityMatrix = similarityMatrix;
   }

   public IsolateSimilarityMatrix getSimilarityMatrix() {
      return mSimilarityMatrix;
   }

}
