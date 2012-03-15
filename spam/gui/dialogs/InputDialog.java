package spam.gui.dialogs;

//import spam.gui.MainWindow;

import spam.database.CPLOPConnection;
import spam.gui.listeners.DataQueryButtonListener;
import spam.gui.listeners.DataSchemaButtonListener;

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
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 500;
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
      dataSetField.add(prepareDataQueryButton(dataField, dataTypeOptions));

      return dataSetField;
   }

   private JPanel prepareDataHierarchyField(JTextField dataHierarchy) {
      JPanel dataHierarchyField = new JPanel();

      dataHierarchyField.setLayout(new FlowLayout(FlowLayout.LEADING));

      dataHierarchyField.add(dataHierarchy);
      dataHierarchyField.add(prepareHierarchyQueryButton("Choose Parameters"));

      return dataHierarchyField;
   }

   private JButton prepareDataQueryButton(JTextField dataSetField, JComboBox dataTypeOptions) {
      final JButton dataQueryButton = new JButton("Choose " + dataTypeOptions.getSelectedItem());

      dataTypeOptions.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            String selectedItem = String.valueOf(e.getItem());

            dataQueryButton.setText("Choose " + selectedItem);
         }
      });

      dataQueryButton.addActionListener(
       new DataQueryButtonListener(dataSetField, dataTypeOptions));

      return dataQueryButton;
   }

   private JButton prepareHierarchyQueryButton(String buttonText) {
      JButton dataHierarchyButton = new JButton(buttonText);

      dataHierarchyButton.addActionListener(new DataSchemaButtonListener());

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
