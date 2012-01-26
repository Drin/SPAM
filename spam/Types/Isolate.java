package spam.Types;

import spam.Types.Isolate;
import spam.Types.IsolateRegion;

import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

public class Isolate {
   private String mIsolateName;

   private Map<Isolate, Double> mIsolateSimilarities = null;
   private List<IsolateRegion> mITSRegions = null;

   public Isolate(String name) {
      mIsolateName = name;
      mIsolateSimilarities = new HashMap<Isolate, Double>();
      mITSRegions = new ArrayList<IsolateRegion>();
   }

   public Isolate(String name, Map<Isolate, Double> newSimilarityMap) {
      mIsolateName = name;
      mIsolateSimilarities = newSimilarityMap;
      mITSRegions = new ArrayList<IsolateRegion>();
   }

   /*
    * Getter Methods
    */
   public String getName() {
      return mIsolateName;
   }

   public Map<Isolate, Double> getSimilarities() {
      return mIsolateSimilarities;
   }

   public List<IsolateRegion> getITSRegions() {
      return mITSRegions;
   }

   public List<Pyroprint> getPyroprints(IsolateRegion itsRegion) {
      if (mITSRegions.contains(itsRegion)) {
         return mITSRegions.get(itsRegion).getPyroprints();
      }

      else {
         return null;
      }
   }

   public double getSimilarity(Isolate otherIsolate) {
      if (mIsolateSimilarities.containsKey(otherIsolate)) {
         return mIsolateSimilarities.get(otherIsolate);
      }

      else if (otherIsolate.mIsolateSimilarities.containsKey(this)) {
         return otherIsolate.mIsolateSimilarities.get(this);
      }

      else {
         return -1;
      }
   }

   /*
    * Setter Methods
    */
   public void setSimilarity(Isolate otherIsolate, double newSimilarity) {
      mIsolateSimilarities.put(otherIsolate, newSimilarity);
   }

   /*
    * Utility Methods
    */
   public void transformSimilarity(Isolate otherIsolate, double alpha, double beta) {
      if (mIsolateSimilarities.containsKey(otherIsolate)) {
         double sim = mIsolateSimilarities.get(otherIsolate);

         if (sim < beta) {
            mIsolateSimilarities.put(otherIsolate, new Double(0));
         }

         else if (sim >= alpha) {
            mIsolateSimilarities.put(otherIsolate, new Double(100));
         }
      }

      else {
         System.err.println("Cannot set similarity between " + mIsolateName +
          " and " otherIsolate);
         System.exit(1);
      }
   }

   public boolean hasSimilarity(Isolate otherIsolate) {
      return mIsolateSimilarities.containsKey(otherIsolate);
   }

   public boolean hasITSRegion(IsolateRegion itsRegion) {
      return mITSRegions.contains(itsRegion);
   }

   public int numITSRegions() {
      return mITSRegions.size();
   }

   /*
    * Overridden Methods
    */
   public boolean equals(Object otherIsolate) {
      if (otherIsolate instanceof Isolate) {
         return mIsolateName.equals(((Isolate) otherIsolate).mIsolateName);
      }

      return false;
   }

   public int hashCode() {
      return mIsolateName.hashCode();
   }

   public String toString() {
      return mIsolateName;
   }
}
