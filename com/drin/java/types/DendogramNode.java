package com.drin.java.types;

import java.util.List;
import java.util.ArrayList;

public class DendogramNode {
   private String mNodeName;
   private DendogramNode mLeft, mRight;

   public DendogramNode(String name, DendogramNode left, DendogramNode right) {
      mNodeName = name;
      mLeft = left;
      mRight = right;
   }

   public DendogramNode(String name) {
      this(name, null, null);
   }

   public DendogramNode getLeft() {
      return mLeft;
   }

   public DendogramNode getRight() {
      return mRight;
   }

   public void setLeft(DendogramNode left) {
      mLeft = left;
   }

   public void setRight(DendogramNode right) {
      mRight = right;
   }

   public String toString() {
      return pp(this, "   ");
   }

   private String pp(DendogramNode node, String prefix) {
      String pretty = "";

      if (node != null) {
         pretty += String.format("%s<Cluster name=\"%s\">\n", prefix, node.mNodeName);

         pretty += pp(node.mLeft, prefix + "   ");
         pretty += pp(node.mRight, prefix + "   ");

         pretty += String.format("%s</Cluster>\n", prefix);

         return pretty;
      }

      return "";
   }
}
