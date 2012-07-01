package com.drin.java.types;

import com.drin.java.types.ITSRegion;
import com.drin.java.types.Pyroprint;

import java.util.Set;
import java.util.HashSet;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 * Note: Isolate equality depends on the equals method in DataObject
 */
public class Isolate extends DataObject {
   private Set<ITSRegion> mRegions;

   public Isolate(String isoId, Set<ITSRegion> regions) {
      super(isoId);
      mRegions = regions;
   }

   public Isolate(String isoId) {
      this(isoId, new HashSet<ITSRegion>());
   }

   public Set<ITSRegion> getRegions() {
      return mRegions;
   }

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
}
