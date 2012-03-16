package spam.gui.components;

import spam.gui.components.SchemaItem;

import java.awt.Component;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SchemaCellRenderer extends DefaultTreeCellRenderer {

   public SchemaCellRenderer() {
      super();
   }

   public Component getTreeCellRendererComponent(JTree tree, Object val, boolean sel,
    boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, val, sel, expanded, leaf, row, hasFocus);

      if (val instanceof DefaultMutableTreeNode) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) val;

         if (node.getUserObject() instanceof SchemaItem) {
            JPanel schemaItemPanel = new JPanel();
            SchemaItem schemaItem = (SchemaItem) node.getUserObject();

            if (schemaItem.getGrouping() != null) {
               schemaItemPanel.setBackground(schemaItem.getGrouping());
            }

            schemaItemPanel.add(this);
            schemaItemPanel.add(schemaItem.getClusterOption());

            return schemaItemPanel;
         }
      }

      return this;
   }
}
