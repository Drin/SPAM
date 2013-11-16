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
   private JTextField mDataSetField = null;
   private JTable mTable = null;

   private Object[][] mTableData = null;
   private Object[] mTableDataColumns = null;

   private CPLOPConnection mConn = null;

   public DataQueryButtonListener(JTextField textField, CPLOPConnection conn) {
      mDataSetField = textField;

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

      mDialog.setTitle("Isolate Data Set");
      mPane.add(prepareIsolateDataView());

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

   /*
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
   */
   
   private JScrollPane prepareIsolateDataView() {
      mTableData = null;
      mTableDataColumns = new Object[] {"isoID", "commonName",
                                        "hostID", "sampleID",
                                        "wellID"};

      try {
         mTableData = mConn.getIsolateDataTableView((String[]) mTableDataColumns);
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         sqlErr.printStackTrace();
         System.exit(1);
      }

      if (mTableData != null) {
         mTable = new JTable(mTableData, mTableDataColumns);
         JScrollPane isolateScrollPane = new JScrollPane(mTable);

         mTable.setAutoCreateRowSorter(true);
         mTable.setFillsViewportHeight(true);

         return isolateScrollPane;
      }

      return null;
   }

   /*
   private JScrollPane prepareExperimentDataView() {
      List<Map<String, Object>> experiments = null;

      try {
         experiments = mConn.getExperimentDataSet();
      }

      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         sqlErr.printStackTrace();
         System.exit(1);
      }

      mTableData = new Object[experiments.size()][];
      mTableDataColumns = new Object[] {"name", "isolate count"};

      for (int rowNdx = 0; rowNdx < experiments.size(); rowNdx++) {
         Map<String, Object> expTuple = experiments.get(rowNdx);
         Object[] tupleData = new Object[expTuple.size()];

         for (int colNdx = 0; colNdx < mTableDataColumns.length; colNdx++) {
            tupleData[colNdx] = expTuple.get(String.valueOf(mTableDataColumns[colNdx]));
         }

         mTableData[rowNdx] = tupleData;
      }

      mTable = new JTable(mTableData, mTableDataColumns);
      JScrollPane isolateScrollPane = new JScrollPane(mTable);

      mTable.setAutoCreateRowSorter(true);
      mTable.setFillsViewportHeight(true);

      return isolateScrollPane;
   }
   */
}
