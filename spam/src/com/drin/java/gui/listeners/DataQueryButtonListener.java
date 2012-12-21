package com.drin.java.gui.listeners;

import com.drin.java.database.CPLOPConnection;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.BoxLayout;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DataQueryButtonListener implements ActionListener {
   /*
    * CONSTANTS
    */
   private final int DIALOG_HEIGHT = 550, DIALOG_WIDTH = 600;

   private JDialog mDialog = null;
   private Container mPane = null;
   private JComboBox<String> mDataTypeOptions = null;
   private JTextField mDataSetField = null;
   private JTable mTable = null;

   private Object[][] mTableData = null;
   private Object[] mTableDataColumns = null;

   @SuppressWarnings("unused")
   private String mRecentDir;

   private CPLOPConnection mConn = null;

   public DataQueryButtonListener(JTextField textField, JComboBox<String> dataTypeOptions,
    CPLOPConnection conn) {
      mDataTypeOptions = dataTypeOptions;
      mDataSetField = textField;
      mRecentDir = null;

      mDialog = new JDialog(mDialog);
      mDialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      mDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      mDialog.setLocationRelativeTo(null);
      mDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

      mPane = mDialog.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));

      mConn = conn;
   }

   public void actionPerformed(ActionEvent e) {
      mPane.removeAll();
      String dataType = (String) mDataTypeOptions.getSelectedItem();

      if (dataType.equals("Isolates")) {
         mDialog.setTitle("Isolate Data Set");
         mPane.add(prepareIsolateDataView());
      }

      else if (dataType.equals("Pyroprints")) {
         mDialog.setTitle("Pyroprint Data Set");
         mPane.add(preparePyroprintDataView());
      }

      else if (dataType.equals("Experiments")) {
      }

      else if (dataType.equals("Matrix")) {
      }

      else {
         System.out.println("Invalid dataType option.");
         System.exit(1);
      }

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

   /*
    * Even though everything in this method has isolate in the name, it is
    * actually abstracted for both isolates and pyroprints since either ID is
    * present in the first column of the table view.
    */
   private JButton createOkayButton() {
      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            int[] selectedRows = mTable.getSelectedRows();
            List<String> isolateList = new ArrayList<String>();

            for (int rowNdx : selectedRows) {
               int tableRowNdx = mTable.convertRowIndexToModel(rowNdx);
               isolateList.add(String.valueOf(mTableData[tableRowNdx][0]));
            }

            String isolateListStr = "";
            for (String isolateId : isolateList) {
               isolateListStr += String.format("'%s', ", isolateId);
            }

            if (!isolateListStr.equals("")) {
               mDataSetField.setText(
                isolateListStr.substring(0, isolateListStr.length() - 2));
            }

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

   private JScrollPane preparePyroprintDataView() {
      List<Map<String, Object>> pyroprints = null;

      try {
         pyroprints = mConn.getPyroprintDataSet();
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         sqlErr.printStackTrace();
         System.exit(1);
      }

      mTableData = new Object[pyroprints.size()][];
      mTableDataColumns = new Object[] {"pyroprint", "isolate", "well", "region",
                                        "dispensation", "forwardPrimer",
                                        "reversePrimer", "sequencePrimer"};

      for (int rowNdx = 0; rowNdx < pyroprints.size(); rowNdx++) {
         Map<String, Object> isoTuple = pyroprints.get(rowNdx);
         Object[] tupleData = new Object[isoTuple.size()];

         for (int colNdx = 0; colNdx < mTableDataColumns.length; colNdx++) {
            tupleData[colNdx] = isoTuple.get((String) mTableDataColumns[colNdx]);
         }

         mTableData[rowNdx] = tupleData;
      }

      mTable = new JTable(mTableData, mTableDataColumns);
      JScrollPane pyroprintScrollPane = new JScrollPane(mTable);

      mTable.setAutoCreateRowSorter(true);
      mTable.setFillsViewportHeight(true);

      return pyroprintScrollPane;
   }
   
   private JScrollPane prepareIsolateDataView() {
      List<Map<String, Object>> isolates = null;

      try {
         //TODO
         //isolates = mConn.getIsolateDataSet();
         isolates = mConn.getIsolateDataSetWithBothRegions();
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         sqlErr.printStackTrace();
         System.exit(1);
      }

      mTableData = new Object[isolates.size()][];
      mTableDataColumns = new Object[] {"id", "name", "host", "sample",
                                       "stored", "pyroprinted"};

      for (int rowNdx = 0; rowNdx < isolates.size(); rowNdx++) {
         Map<String, Object> isoTuple = isolates.get(rowNdx);
         Object[] tupleData = new Object[isoTuple.size()];

         for (int colNdx = 0; colNdx < mTableDataColumns.length; colNdx++) {
            tupleData[colNdx] = isoTuple.get((String) mTableDataColumns[colNdx]);
         }

         mTableData[rowNdx] = tupleData;
      }

      mTable = new JTable(mTableData, mTableDataColumns);
      JScrollPane isolateScrollPane = new JScrollPane(mTable);

      mTable.setAutoCreateRowSorter(true);
      mTable.setFillsViewportHeight(true);

      return isolateScrollPane;
   }
}
