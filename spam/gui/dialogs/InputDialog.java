package spam.gui.dialogs;

//import spam.gui.MainWindow;

import spam.database.CPLOPConnection;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JSeparator;
import javax.swing.BoxLayout;

import java.io.File;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

public class InputDialog extends JDialog {
   /*
    * CONSTANTS
    */
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 415;
   private final String[] DATA_TYPE_VALUES = new String[] {"Isolates",
                                                           "Pyroprints",
                                                           "Experiments"};
   /*
    * GUI Components
    */
   private Container mPane = null, mOwner = null;
   private JDialog mDialog = null;
   private JComboBox mDataSetType;
   private JTextField mDataSet, mDataHierarchy;

   public InputDialog() {
      super();

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mDialog = this;

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      mDataSet = new JTextField(20);
      mDataHierarchy = new JTextField(20);

      mDataSetType = new JComboBox(DATA_TYPE_VALUES);
   }

   public InputDialog(Frame owner, String title) {
      super(owner, title);

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mOwner = owner;
      mDialog = this;

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      mDataSet = new JTextField(20);
      mDataHierarchy = new JTextField(20);

      mDataSetType = new JComboBox(DATA_TYPE_VALUES);
   }

   public static void main(String[] args) {
      InputDialog dialog = new InputDialog();

      dialog.init();
      dialog.setVisible(true);
   }

   public void init() {
      JLabel dataSetLabel = new JLabel("Select Data Set:");
      JPanel dataSetField = prepareDataSetField(mDataSetType, mDataSet);

      JLabel dataHierarchyLabel = new JLabel("Set Data Clustering Hierarchy:");
      JPanel dataHierarchyField = prepareDataHierarchyField(mDataHierarchy);

      mPane.add(dataSetLabel);
      mPane.add(dataSetField);

      mPane.add(dataHierarchyLabel);
      mPane.add(dataHierarchyField);

      mPane.add(initControls());

      mPane.validate();
   }

   private JPanel prepareDataSetField(JComboBox dataTypeOptions, JTextField dataField) {
      JPanel dataSetField = new JPanel();

      dataSetField.setLayout(new FlowLayout(FlowLayout.LEADING));

      dataSetField.add(dataTypeOptions);
      dataSetField.add(dataField);
      dataSetField.add(prepareDataQueryButton(dataTypeOptions));

      return dataSetField;
   }

   private JPanel prepareDataHierarchyField(JTextField dataHierarchy) {
      JPanel dataHierarchyField = new JPanel();

      dataHierarchyField.setLayout(new FlowLayout(FlowLayout.LEADING));

      dataHierarchyField.add(dataHierarchy);
      //dataHierarchyField.add(prepareHierarchyQueryButton("Choose Parameters"));

      return dataHierarchyField;
   }

   private JButton prepareDataQueryButton(final JComboBox dataTypeOptions) {
      final JButton dataQueryButton = new JButton("Choose " + dataTypeOptions.getSelectedItem());

      dataTypeOptions.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            String selectedItem = (String) e.getItem();

            dataQueryButton.setText("Choose " + selectedItem);
         }
      });

      dataQueryButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            List<Map<String, Object>> isolates = null;

            try {
               CPLOPConnection cplopConn = new CPLOPConnection();
               isolates = cplopConn.getIsolateDataSet();
            }

            catch (java.sql.SQLException sqlErr) {
               System.out.println("SQLException\nExiting...");
               System.exit(1);
            }

            catch (CPLOPConnection.DriverException driveErr) {
               System.out.println("Driver Exception:\n" + driveErr + "\nExiting...");
               driveErr.printStackTrace();
               System.exit(1);
            }

            //String dataType = (String) dataTypeOptions.getSelectedItem();

            /*
             * use the CPLOPConnection class to retrieve all isolates.
             * Use the isolates retrieved to populate a JTable
             */
            Object[][] tableData = new Object[isolates.size()][];
            Object[] columns = new Object[] {"id", "name", "host", "sample",
                                             "stored", "pyroprinted"};

            for (int rowNdx = 0; rowNdx < isolates.size(); rowNdx++) {
               Map<String, Object> isoTuple = isolates.get(rowNdx);
               Object[] tupleData = new Object[isoTuple.size()];

               for (int colNdx = 0; colNdx < columns.length; colNdx++) {
                  tupleData[colNdx] = isoTuple.get((String) columns[colNdx]);
               }

               tableData[rowNdx] = tupleData;
            }

            /*
             * Prepare the dialog window which displays isolate data the user
             * will choose from
             */
            final JDialog isolateDataWindow = new JDialog(mDialog, "Isolate Data");
            isolateDataWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            Container isolatePane = isolateDataWindow.getContentPane();
            isolatePane.setLayout(new BoxLayout(isolatePane, BoxLayout.Y_AXIS));
            //isolatePane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

            /*
             * Prepare a JTable to be used in a dialog window
             */
            JTable isolateTable = new JTable(tableData, columns);
            JScrollPane isolateScrollPane = new JScrollPane(isolateTable);

            isolateTable.setAutoCreateRowSorter(true);
            isolateTable.setFillsViewportHeight(true);

            /*
             * Prepare a cancel button
             */
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  isolateDataWindow.dispose();
               }
            });

            isolatePane.add(isolateScrollPane);
            isolatePane.add(cancelButton);
            isolatePane.validate();

            isolateDataWindow.setVisible(true);
         }
      });

      return dataQueryButton;
   }

   private JButton prepareHierarchyQueryButton(String buttonText) {
      JButton dataHierarchyButton = new JButton(buttonText);

      dataHierarchyButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         }
      });

      return dataHierarchyButton;
   }

   public JPanel initControls() {
      JPanel dialogControls = new JPanel();

      dialogControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      dialogControls.add(createOkayButton());
      dialogControls.add(createCancelButton());

      dialogControls.setAlignmentX(Component.CENTER_ALIGNMENT);

      return dialogControls;
   }

   private JButton createOkayButton() {
      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            try {
            }
            catch (NullPointerException emptyValErr) {
               /*
               JOptionPane.showMessageDialog(mOwner,
                "No file was selected",
                "Invalid File", JOptionPane.ERROR_MESSAGE);
                */
            }

            dispose();
         }
      });

      return okayButton;
   }

   private JButton createCancelButton() {
      JButton cancelButton = new JButton("Cancel");

      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dispose();
            return;
         }
      });

      return cancelButton;
   }
}
