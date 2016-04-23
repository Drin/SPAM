package com.drin.clustering.dendogram;

import com.drin.clustering.dendogram.Dendogram;
import com.drin.clustering.Cluster;

public class DendogramNode implements Dendogram {
   private Dendogram mLeft, mRight;
   private Cluster mCluster;

   public DendogramNode(Dendogram left, Dendogram right, Cluster clust) {
      mLeft = left;
      mRight = right;

      mCluster = clust;
   }

   public Dendogram getLeft() { return mLeft; }
   public Dendogram getRight() { return mRight; }
   public Cluster getCluster() { return mCluster; }

   public void setLeft(Dendogram left) { mLeft = left; }
   public void setRight(Dendogram right) { mRight = right; }

   @Override
   public String toString() { return DendogramNode.pp(this, "   "); }

   private static String pp(Dendogram dend, String prefix) {
      String pretty = "";

      if (dend != null) {
         if (dend instanceof DendogramNode) {
            DendogramNode node = (DendogramNode) dend;

            pretty += String.format("%s<Cluster diameter=\"%.04f\" " +
                                    "mean=\"%.04f\">\n", prefix,
                                    node.getCluster().getDiameter(),
                                    node.getCluster().getMean());

            pretty += pp(node.mLeft, prefix + "   ");
            pretty += pp(node.mRight, prefix + "   ");

            pretty += String.format("%s</Cluster>\n", prefix);
         }
         else if (dend instanceof DendogramLeaf) {
            DendogramLeaf leaf = (DendogramLeaf) dend;

            pretty += leaf.pp(prefix);
         }

         return pretty;
      }

      return "";
   }
}
