package com.drin.java.gui.dialogs;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.ClusterAverageMetric;
import com.drin.java.metrics.IsolateAverageMetric;
import com.drin.java.metrics.ITSRegionAverageMetric;
import com.drin.java.metrics.PyroprintUnstablePearsonMetric;

import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;

import com.drin.java.gui.MainWindow;
import com.drin.java.gui.listeners.DataQueryButtonListener;
import com.drin.java.gui.components.AnalysisWorker;
import com.drin.java.database.CPLOPConnection;

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
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JSeparator;
import javax.swing.BoxLayout;

import java.io.File;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Enumeration;

@SuppressWarnings("serial")
public class InputDialog extends JDialog {
   /*
    * CONSTANTS
    */
   private final static String DEFAULT_TITLE = "Parameter Input Dialog";
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 500;
   private final String[] DATA_TYPE_VALUES = new String[] {"Isolates",
                                                           "Pyroprints",
                                                           "Experiments"};
   /*
    * GUI Components
    */
   private Container mPane = null, mOwner = null;
   private JDialog mDialog = null;
   private JComboBox<String> mDataSetType;
   private JTextField mDataSet, mDataHierarchy;

   private CPLOPConnection mConn;

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

      mDataSetType = new JComboBox<String>(DATA_TYPE_VALUES);

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

   public InputDialog() {
      this(null, DEFAULT_TITLE);
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

   private JPanel prepareDataSetField(JComboBox<String> dataTypeOptions, JTextField dataField) {
      JPanel dataSetField = new JPanel();

      dataSetField.setLayout(new FlowLayout(FlowLayout.LEADING));

      dataSetField.add(dataTypeOptions);
      dataSetField.add(dataField);
      dataSetField.add(prepareDataQueryButton(dataField, dataTypeOptions));

      return dataSetField;
   }

   private JPanel prepareDataHierarchyField(JTextField dataSchema) {
      JPanel dataHierarchyField = new JPanel();

      dataHierarchyField.setLayout(new FlowLayout(FlowLayout.LEADING));

      dataHierarchyField.add(dataSchema);
      dataHierarchyField.add(prepareSchemaButton(dataSchema, "Choose Parameters"));

      return dataHierarchyField;
   }

   private JButton prepareDataQueryButton(JTextField dataSetField, JComboBox<String> dataTypeOptions) {
      final JButton dataQueryButton = new JButton("Choose " + dataTypeOptions.getSelectedItem());

      dataTypeOptions.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            String selectedItem = String.valueOf(e.getItem());

            dataQueryButton.setText("Choose " + selectedItem);
         }
      });

      dataQueryButton.addActionListener(
       new DataQueryButtonListener(dataSetField, dataTypeOptions, mConn));

      return dataQueryButton;
   }

   private JButton prepareSchemaButton(final JTextField schemaField, String buttonText) {
      JButton dataHierarchyButton = new JButton(buttonText);

      dataHierarchyButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            DataOrganizationDialog newDialog = new DataOrganizationDialog(schemaField);

            newDialog.init();
            newDialog.setVisible(true);
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

   private List<Map<String, Object>> queryData() {
      List<Map<String, Object>> dataList = null;
      String dataSet = mDataSet.getText();

      if (String.valueOf(mDataSetType.getSelectedItem()).equals(DATA_TYPE_VALUES[0])) {
         try {
            dataList = mConn.getDataByIsoID(dataSet);
         }
         catch (java.sql.SQLException sqlErr) {
            System.out.println("SQLException:\nExiting...");
            sqlErr.printStackTrace();
            System.exit(1);
         }
      }

      else if (String.valueOf(mDataSetType.getSelectedItem()).equals(DATA_TYPE_VALUES[1])) {
         try {
            dataList = mConn.getDataByPyroID(dataSet);
         }
         catch (java.sql.SQLException sqlErr) {
            System.out.println("SQLException:\nExiting...");
            sqlErr.printStackTrace();
            System.exit(1);
         }
      }

      return dataList;
   }

   private boolean doWork() {
      Ontology ontology = null;
      Clusterer clusterer = null;
      Set<Cluster> clusters = new HashSet<Cluster>();

      Map<String, Isolate> isoMap = constructIsolates(queryData());

      //TODO

      for (Map.Entry<String, Isolate> isoEntry : isoMap.entrySet()) {
         clusters.add(new IsolateCluster(isoEntry.getKey(), isoEntry.getValue()));
      }

      clusterer = new AgglomerativeClusterer(clusters);

      AnalysisWorker worker = new AnalysisWorker(clusterer,
       MainWindow.getMainFrame().getOutputCanvas());

      worker.setOutputFile("TestOut");
      worker.execute();

      return true;
   }

   private JButton createOkayButton() {
      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            if (mDataSet.getText().equals("")) {
               System.out.printf("No data selected.\n");
               return;
            }

            doWork();

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

   /*
    * TODO:
    * do a single pass over the data. This should allow for ITSRegions and
    * Isolates to be constructed.
    * Then do 1 large query to the database (instead of 1 per pyroprint).
    * The pyroprint data will then be scanned and pyroprints will be
    * constructed, and added to the appropriate ITS region. 
    * Then a map of isolate IDs to isolates will be returned where each isolate
    * has a set of ITSRegions (different) and each region has a set of
    * pyroprints.
    */
   private Map<String, Isolate> constructIsolates(List<Map<String, Object>> dataList) {
      Map<String, Isolate> isoMap = new HashMap<String, Isolate>();
      double tmp_alpha = .99, tmp_beta = .995;

      DataMetric<Isolate> isoMetric = new IsolateAverageMetric();
      DataMetric<ITSRegion> regionMetric = new ITSRegionAverageMetric(tmp_alpha, tmp_beta);

      Set<ITSRegion> isolateRegions = new HashSet<ITSRegion>();

      for (Map<String, Object> dataMap : dataList) {
         String isoID = String.valueOf(dataMap.get("isolate"));
         String regName = String.valueOf(dataMap.get("region"));
         String wellID = String.valueOf(dataMap.get("well"));
         int pyroID = Integer.parseInt(String.valueOf(dataMap.get("pyroprint")));

         isolateRegions.add(new ITSRegion(regName, tmp_alpha, tmp_beta,
                                          regionMetric));

         //Retrieve Isolate
         if (!isoMap.containsKey(isoID)) {
            isoMap.put(isoID, new Isolate(isoID, isolateRegions, isoMetric));
         }

         /*
          * Construct Pyroprint
          */
         List<Double> peakList = new ArrayList<Double>(104);
         String peakDisp = "";
         List<Map<String, Object>> peakMapList = null;

         try {
            peakMapList = mConn.getHistogram(pyroID);
         }
         catch (java.sql.SQLException sqlErr) {
            System.out.println("SQLException:\nExiting...");
            sqlErr.printStackTrace();
            System.exit(1);
         }

         for (Map<String, Object> peakMap : peakMapList) {
            peakList.add((Double) peakMap.get("peakHeight"));
            peakDisp += String.valueOf(peakMap.get("nucleotide"));
         }

         Pyroprint tmpPyro = new Pyroprint(pyroID, wellID, peakDisp, peakList);

         //add pyroprint to current ITSRegion
         tmpRegion.getPyroprints().add(tmpPyro);
      }

      return isoMap;
   }
}
