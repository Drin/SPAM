package com.drin.java.biology;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.biology.Pyroprint;

import java.util.Set;
import java.util.HashSet;

public class ITSRegion extends BaseClusterable {
   private Set<Pyroprint> mPyroprints;

   public ITSRegion(String regionName) {
      super(regionName);

      mPyroprints = new HashSet<Pyroprint>();
   }

   public Set<Pyroprint> getPyroprints() { return mPyroprints; }
   public void add(Pyroprint pyro) { mPyroprints.add(pyro); }

   public boolean isSimilarRegion(ITSRegion otherRegion) {
      return mName.equals(otherRegion.mName);
   }

   @Override
   public boolean equals(Object otherObj) {
      if (otherObj instanceof ITSRegion) {
         ITSRegion otherRegion = (ITSRegion) otherObj;

         if (this.isSimilarRegion(otherRegion) &&
             mPyroprints.size() == otherRegion.mPyroprints.size()) {

            for (Pyroprint pyro : mPyroprints) {
               //If this is true (otherRegion does not contain this pyroprint)
               //then the regions do not contain the same pyroprints and are
               //thus not the same region
               if (!otherRegion.mPyroprints.contains(pyro)) { return false; }
            }

            //If all pyroprints were contained in the other and the number of
            //pyroprints in this region are the same as the number in
            //otherRegion and both have the same region name (e.g. '16s-23s')
            //then both regions are the same region
            return true;
         }
      }

      return false;
   }
}
