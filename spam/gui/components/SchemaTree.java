package spam.gui.components;

import spam.database.CPLOPConnection;
import spam.gui.components.SchemaItem;
import spam.gui.components.SchemaCellRenderer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;

import java.util.List;
import java.util.Map;

public class SchemaTree extends JPanel {
   private static final String ROOT_NODE_NAME = "CPLOP Schema";

   private DefaultMutableTreeNode mSchemaRoot;
   private CPLOPConnection mConn;
   private JTree mSchemaTree;
   private JScrollPane mSchemaView;

   public SchemaTree() {
      super();

      mSchemaRoot = new DefaultMutableTreeNode(new SchemaItem(ROOT_NODE_NAME));

      try {
         mConn = new CPLOPConnection();
      }

      catch (CPLOPConnection.DriverException driveErr) {
         System.out.println("Driver Exception:\n" + driveErr + "\nExiting...");
         System.exit(1);
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQL Exception:\n" + sqlErr + "\nExiting...");
         System.exit(1);
      }
   }

   public void init() {
      Map<String, List<String>> tableAttributes = null;

      try {
         tableAttributes = mConn.getCPLOPSchema();
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         System.exit(1);
      }

      for (String tableName : tableAttributes.keySet()) {
         SchemaItem tableItem = new SchemaItem(tableName);
         DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(tableItem);

         List<String> tableCols = tableAttributes.get(tableName);
         for (String colName : tableCols) {
            SchemaItem colItem = new SchemaItem(colName);
            DefaultMutableTreeNode colNode = new DefaultMutableTreeNode(colItem);

            tableNode.add(colNode);
         }

         mSchemaRoot.add(tableNode);
      }

      mSchemaTree = new JTree(mSchemaRoot);
      SchemaCellRenderer cellRenderer = new SchemaCellRenderer();
      mSchemaTree.setCellRenderer(cellRenderer);
      mSchemaTree.setCellEditor(new SchemaCellEditor(mSchemaTree, cellRenderer));
      mSchemaTree.setEditable(true);

      mSchemaView = new JScrollPane(mSchemaTree);

      mSchemaTree.getSelectionModel().setSelectionMode(
       TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

      mSchemaView.validate();
      mSchemaView.setVisible(true);

      this.add(mSchemaView);
   }

   public String getSelectedPartitions() {
      String orderedPartitionValues = "";
      TreePath[] selectedPaths = mSchemaTree.getSelectionPaths();

      for (TreePath selectionPath : selectedPaths) {
         if (selectionPath != null) {
            orderedPartitionValues += String.format(", %s", selectionPath.getLastPathComponent());
         }
      }

      return orderedPartitionValues.substring(2);
   }
}
