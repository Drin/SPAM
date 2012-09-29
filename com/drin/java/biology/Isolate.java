package com.drin.java.biology;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import java.util.Set;
import java.util.HashSet;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends BaseClusterable {
   private Set<ITSRegion> mRegions;

   public Isolate(String isoId, Set<ITSRegion> regions) {
      super(isoId);
      mRegions = regions;
   }

   public Isolate(String isoId) {
      this(isoId, new HashSet<ITSRegion>());
   }

   public Set<ITSRegion> getRegions() { return mRegions; }

   public boolean hasRegion(String regionName) {
      for (ITSRegion region : mRegions) {
         if (region.getName().equals(regionName)) { return true; }
      }

      return false;
   }

   public ITSRegion getRegion(String regionName) {
      for (ITSRegion refRegion : mRegions) {
         if (regionName.equals(refRegion.getName())) { return refRegion; }
      }

      return null;
   }

   public Set<Pyroprint> getPyroprints(String regionName) {
      if (mRegions != null) {
         ITSRegion region = getRegion(regionName);

         if (region != null) { return region.getPyroprints(); }
      }

      return null;
   }

   @Override
   public String toString() {
      String str = "";

      for (ITSRegion region : mRegions) {
         str += String.format("%s - region '%s':\n", mName, region);

         for (Pyroprint pyro : region.getPyroprints()) {
            str += String.format("\tpyroprint %s\n\n", pyro);
         }
      }

      return str;
   }
}
