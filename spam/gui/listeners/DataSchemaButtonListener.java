package spam.gui.listeners;

import spam.database.CPLOPConnection;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.BoxLayout;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DataSchemaButtonListener implements ActionListener {
   /*
    * CONSTANTS
    */
   private final int DIALOG_HEIGHT = 550, DIALOG_WIDTH = 600;

   private JDialog mDialog = null;
   private Container mPane = null;
   //private JTextField mDataSetField = null;
   private JTree mDataPartitionTree = null;
   private DefaultMutableTreeNode mListRoot = null;

   private CPLOPConnection mConn = null;

   public DataSchemaButtonListener() {
      //mDataSetField = textField;

      mDialog = new JDialog(mDialog);
      mDialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      mDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      mDialog.setLocationRelativeTo(null);
      mDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

      mPane = mDialog.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));

      try {
         mConn = new CPLOPConnection();
      }

      catch (CPLOPConnection.DriverException driveErr) {
         System.out.println("Driver Exception:\n" + driveErr + "\nExiting...");
         //driveErr.printStackTrace();
         System.exit(1);
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQL Exception:\n" + sqlErr + "\nExiting...");
         System.exit(1);
      }
   }

   public void actionPerformed(ActionEvent e) {
      mPane.removeAll();

      mDialog.setTitle("Data Organization");
      mPane.add(prepareSchemaView());

      mPane.add(initControls());
      mPane.validate();

      mDialog.setVisible(true);
   }

   private JPanel initControls() {
      JPanel controlsPanel = new JPanel();

      controlsPanel.setLayout(new FlowLayout());

      controlsPanel.add(createOkayButton());
      controlsPanel.add(createCancelButton());

      return controlsPanel;
   }

   private JButton createOkayButton() {
      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            /*
            int[] selectedRows = mTable.getSelectedRows();
            List<String> isolateList = new ArrayList<String>();

            for (int rowNdx : selectedRows) {
               int tableRowNdx = mTable.convertRowIndexToModel(rowNdx);
               isolateList.add(String.valueOf(mTableData[tableRowNdx][0]));
            }

            String isolateListStr = "";
            for (String isolateId : isolateList) {
               isolateListStr += isolateId + ", ";
            }

            if (!isolateListStr.equals("")) {
               mDataSetField.setText(
                isolateListStr.substring(0, isolateListStr.length() - 2));
            }
            */

            mDialog.dispose();
         }
      });

      return okayButton;
   }

   private JButton createCancelButton() {
      JButton cancelButton = new JButton("Cancel");

      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            mDialog.dispose();
         }
      });

      return cancelButton;
   }
   
   private JScrollPane prepareSchemaView() {
      Map<String, List<String>> tableAttributes = null;

      try {
         tableAttributes = mConn.getCPLOPSchema();
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         sqlErr.printStackTrace();
         System.exit(1);
      }

      mListRoot = new DefaultMutableTreeNode("Data Tables");

      for (String tableName : tableAttributes.keySet()) {
         List<String> tableCols = tableAttributes.get(tableName);
         DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(tableName);

         for (String colName : tableCols) {
            DefaultMutableTreeNode colNode = new DefaultMutableTreeNode(colName);

            tableNode.add(colNode);
         }

         mListRoot.add(tableNode);
      }

      mDataPartitionTree = new JTree(mListRoot);
      mDataPartitionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

      JScrollPane dataSchemaScrollPane = new JScrollPane(mDataPartitionTree);

      return dataSchemaScrollPane;
   }
}
