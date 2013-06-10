package com.drin.java.biology;

import com.drin.java.clustering.Clusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.util.Logger;

import java.util.Iterator;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends Clusterable<ITSRegion> {
   private static final int ALPHA_NDX = 0, BETA_NDX = 1;
   private Map<String, Float> mComparisonCache;

   private int mIdNum;

   public Isolate(String isoId) {
      this(isoId, 2);
   }

   public Isolate(String isoId, int dataSize) {
      super(isoId, new HashSet<ITSRegion>(dataSize));

      mIdNum = -1;
      mComparisonCache = new HashMap<String, Float>();
   }

   public void setIdNum(int idNum) { mIdNum = idNum; }
   public int getIdNum() { return mIdNum; }

   @Override
   public float compareTo(Clusterable<?> otherObj) {
      Iterator<ITSRegion> itrA, itrB;
      float comparison = 0.0f;
      byte numRegions = 0;

      if (otherObj instanceof Isolate) {
         if (!mComparisonCache.containsKey(otherObj.getName())) {
            itrA = mData.iterator();

            while (itrA.hasNext()) {
               ITSRegion regionA = itrA.next();

               itrB = ((Isolate) otherObj).getData().iterator();
               while (itrB.hasNext()) {
                  ITSRegion regionB = itrB.next();

                  if (regionA.equals(regionB)) {
                     comparison += regionA.compareTo(regionB);
                     numRegions++;

                     break;
                  }
               }
            }

            if (numRegions == 2) {
               comparison = comparison / numRegions;
            }
            else {
               System.err.println("Invalid # of Regions: " + numRegions);
               System.exit(0);
            }

            mComparisonCache.put(otherObj.getName(), new Float(comparison));

            return comparison;
         }

         Float compVal = mComparisonCache.get(otherObj.getName());
         if (compVal != null) { return compVal.floatValue(); }
      }

      return -2;
   }

   @Override
   public Clusterable<ITSRegion> deepCopy() {
      Clusterable<ITSRegion> newIsolate = new Isolate(mName);

      for (ITSRegion region : mData) {
         newIsolate.getData().add(region.deepCopy());
      }

      return newIsolate;
   }

   @Override
   public String toString() {
      String str = String.format("isolate '%s' [%d regions]:\n",
                                 this.getName(), mData.size());

      for (ITSRegion region : mData) {
         str += String.format("\tregion '%s' [%d pyroprints]:\n",
                              region.getName(), region.getData().size());

         for (Pyroprint pyro : region.getData()) {
            str += String.format("\t\tpyroprint %s\n\n", pyro);
         }
      }

      return str;
   }
}
