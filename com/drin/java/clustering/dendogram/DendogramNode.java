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

   public String toString() { return pp(this, "   "); }

   private String pp(Dendogram node, String prefix) {
      String pretty = "";

      if (node != null) {
         pretty += String.format("%s<Cluster name=\"%s\">\n", prefix,
          String.format("[%s_%s]", mLeft, mRight));

         //pretty += pp(node.mLeft, prefix + "   ");
         //pretty += pp(node.mRight, prefix + "   ");

         pretty += String.format("%s</Cluster>\n", prefix);

         return pretty;
      }

      return "";
   }
}
