package spam.dataTypes;

import spam.dataTypes.Connectivity;

import spam.dataStructures.IsolateSimilarityMatrix;

import java.util.Map;

public class PyroRunData {
   private IsolateRegion mRegion;
   private Cluster.distType mClusterDistType;
   private Map<Connectivity, IsolateSimilarityMatrix> mIsolateNetworks;
   private double mDistThreshold, mLowerThreshold, mUpperThreshold;

   public PyroRunData() {
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

   public void setDistanceType(Cluster.distType distanceType) {
      mClusterDistType = distanceType;
   }

   public Cluster.distType getDistanceType() {
      return mClusterDistType;
   }

   public void setIsolateNetworks(Map<Connectivity, IsolateSimilarityMatrix> isolateNetworks) {
      mIsolateNetworks = isolateNetworks;
   }

   public IsolateSimilarityMatrix getStrongNetwork() {
      return mIsolateNetworks.get(Connectivity.STRONG);
   }

   public IsolateSimilarityMatrix getWeakNetwork() {
      return mIsolateNetworks.get(Connectivity.WEAK);
   }

   public Map<Connectivity, IsolateSimilarityMatrix> getNetworks() {
      return mIsolateNetworks;
   }

   public void setDistanceThreshold(double distThreshold) {
      mDistThreshold = distThreshold;
   }

   public double getDistanceThreshold() {
      return mDistThreshold;
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

}
