package com.drin.java.gui.components;

import com.drin.java.gui.components.SchemaItem;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTree;
import javax.swing.JColorChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SchemaCellEditor extends DefaultTreeCellEditor {
   @SuppressWarnings("unused")
   private JTree mTree;
   @SuppressWarnings("unused")
   private DefaultTreeCellRenderer mRenderer;

   public SchemaCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
      super(tree, renderer);

      mTree = tree;
      mRenderer = renderer;
   }

   public void actionPerformed(ActionEvent e) {
      System.out.println("action!");
   }

   public Component getTreeCellEditorComponent(JTree tree, Object val, boolean sel,
    boolean expanded, boolean leaf, int row) {
      Component comp = super.getTreeCellEditorComponent(tree, val, sel, expanded, leaf, row);

      if (val instanceof DefaultMutableTreeNode) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) val;

         if (node.getUserObject() instanceof SchemaItem) {
            final SchemaItem schemaItem = (SchemaItem) node.getUserObject();

            final JColorChooser colorPalette = new JColorChooser();
            JDialog colorDialog = JColorChooser.createDialog(comp, "Grouping", true, colorPalette,
             new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                   schemaItem.setGrouping(colorPalette.getColor());
                }
             }, null);

            colorDialog.setVisible(true);

            System.err.printf("rawr\n");
         }
      }

      //TODO: figure out how to make it not editable but still trigger the
      //action
      return comp;
   }

   protected void startEditingTimer() {
      System.out.println("rawrrrrr");
   }
}
