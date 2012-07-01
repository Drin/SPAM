package com.drin.java.gui.components;

import java.awt.Color;

import javax.swing.JCheckBox;

public class SchemaItem {
   private static final String OPTION_TEXT = "Ordered";
   private String mText;
   private JCheckBox mClusterOption;
   private Color mGrouping;

   public SchemaItem(String itemText, boolean setClustering) {
      mText = itemText;
      mClusterOption = new JCheckBox(OPTION_TEXT, setClustering);
      mGrouping = Color.white;

      mClusterOption.setBackground(mGrouping);
   }

   public SchemaItem(String itemText) {
      this(itemText, false);
   }

   public String getText() {
      return mText;
   }

   public JCheckBox getClusterOption() {
      return mClusterOption;
   }

   public Color getGrouping() {
      return mGrouping;
   }

   public void setGrouping(Color color) {
      mGrouping = color;
   }

   public String toString() {
      return mText;
   }
}
