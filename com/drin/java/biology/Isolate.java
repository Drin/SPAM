package com.drin.java.biology;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Isolate represents a bacterial isolate collected by a biologist.
 *
 */
public class Isolate extends BaseClusterable {
   private Map<String, ITSRegion> mRegions;

   public Isolate(String isoId, Set<ITSRegion> regions) {
      super(isoId);
      
      mRegions = new HashMap<String, ITSRegion>();

      if (regions != null) {
         for (ITSRegion region : regions) {
            mRegions.put(region.getName(), region);
         }
      }
   }

   public Isolate(String isoId) {
      this(isoId, null);
   }

   public Map<String, ITSRegion> getRegions() { return mRegions; }

   public boolean hasRegion(String regionName) {
      if (mRegions != null) {
         return mRegions.containsKey(regionName);
      }

      return false;
   }

   public ITSRegion getRegion(String regionName) {
      if (mRegions != null) {
         return mRegions.get(regionName);
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

      for (Map.Entry<String, ITSRegion> region : mRegions.entrySet()) {
         str += String.format("%s - region '%s':\n", mName, region.getKey());

         for (Pyroprint pyro : region.getValue().getPyroprints()) {
            str += String.format("\tpyroprint %s\n\n", pyro);
         }
      }

      return str;
   }
}
