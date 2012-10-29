package com.drin.java.clustering.dendogram;

import com.drin.java.clustering.dendogram.Dendogram;

public class DendogramNode implements Dendogram {
   private Dendogram mLeft, mRight;

   public DendogramNode(Dendogram left, Dendogram right) {
      mLeft = left;
      mRight = right;
   }

   public Dendogram getLeft() { return mLeft; }
   public Dendogram getRight() { return mRight; }

   public Dendogram join(Dendogram otherDend) {
      return new DendogramNode(this, otherDend);
   }

   public void setLeft(Dendogram left) { mLeft = left; }
   public void setRight(Dendogram right) { mRight = right; }

   @Override
   public String toString() { return DendogramNode.pp(this, "   "); }

   private static String pp(Dendogram node, String prefix) {
      String pretty = "";

      if (node != null) {
         pretty += String.format("%s<Cluster>\n", prefix);

         if (node instanceof DendogramNode) {
            pretty += pp(((DendogramNode)node).mLeft, prefix + "   ");
            pretty += pp(((DendogramNode)node).mRight, prefix + "   ");
         }
         else if (node instanceof DendogramLeaf) {
            pretty += ((DendogramLeaf)node).pp(prefix + "   ");
         }

         pretty += String.format("%s</Cluster>\n", prefix);

         return pretty;
      }

      return "";
   }
}
