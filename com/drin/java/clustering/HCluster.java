package com.drin.java.clustering;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;

public class HCluster extends Cluster {
   public HCluster(int clustSize) {
      super(clustSize);
   }

   public HCluster(Clusterable<?> elem) {
      super(1);

      mSize = 1;
      mElements.add(elem);
      mMetaLabels = elem.getMetaData();
   }

   public HCluster(HCluster oldCluster) { super(oldCluster); }

   @Override
   public void join(Cluster otherClust) {
      if (otherClust instanceof HCluster) {
         mElements.addAll(otherClust.mElements);

         /*
         for (int labelNdx = 0; labelNdx < otherClust.mMetaLabels.length; labelNdx++) {
            mMetaLabels[labelNdx] += (", " + otherClust.mMetaLabels[labelNdx]);
         }
         */

         mSize = mElements.size();
      }
      else {
         System.err.println("Error incorporating cluster");
         System.exit(1);
      }
   }
}
