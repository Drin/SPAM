package com.drin.java.gui.dialogs;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;

import com.drin.java.metrics.ClusterAverageMetric;
import com.drin.java.metrics.IsolateAverageMetric;
import com.drin.java.metrics.ITSRegionAverageMetric;
import com.drin.java.metrics.PyroprintUnstablePearsonMetric;

import com.drin.java.ontology.Ontology;
import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import com.drin.java.gui.MainWindow;
import com.drin.java.gui.listeners.DataQueryButtonListener;
import com.drin.java.gui.components.AnalysisWorker;
import com.drin.java.database.CPLOPConnection;

import com.drin.java.util.Logger;

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
import javax.swing.JFileChooser;
import javax.swing.JComponent;

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
   private JTextField mDataSet, mDataHierarchy, mOutFile, mOntology;
   private String mRecentDir;

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

      mOutFile = new JTextField(20);
      mOntology = new JTextField(15);

      mRecentDir = "";

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
      mPane.add(headerSection(mOutFile, mOntology));

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

   private JPanel headerSection(JTextField outfile, JTextField ontology) {
      JPanel ontologySection = null, layout = new JPanel();

      layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

      ontologySection = horizontal_input("Experimental Hierarchy", ontology);
      ontologySection.add(fileBrowseButton(ontology, null));

      layout.add(horizontal_input("Output file name:", outfile));
      //layout.add(horizontal_input("Clustering Method:", method));
      layout.add(ontologySection);
      layout.setAlignmentY(Component.CENTER_ALIGNMENT);

      return layout;
   }

   private JButton fileBrowseButton(final JTextField infile, final JTextField outfile) {
      JButton fileBrowse = new JButton("Browse");

      fileBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (mRecentDir != "") {
               File curDir = new File(mRecentDir);
               chooser.setCurrentDirectory(curDir);
            }
            
            int returnVal = chooser.showOpenDialog(chooser);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
               File dataFile = chooser.getSelectedFile();
               infile.setText(dataFile.getAbsolutePath());

               if (outfile != null && outfile.getText().equals("")) {
                  outfile.setText(dataFile.getName().substring(0,
                                  dataFile.getName().indexOf(".csv")));
               }

               mRecentDir = chooser.getSelectedFile().getPath();
            }
         }
      });

      return fileBrowse;
   }

   private JPanel horizontal_input(String label, JComponent component) {
      JPanel panel = new JPanel();

      panel.setLayout(new FlowLayout(FlowLayout.LEADING));
      panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
      panel.add(new JLabel(label));
      panel.add(component);
      panel.setAlignmentX(Component.CENTER_ALIGNMENT);

      return panel;
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

      if (!mOntology.getText().equals("")) {
         ontology = Ontology.createOntology(mOntology.getText());
      }

      Map<String, Isolate> isoMap = constructIsolates(queryData());
      
      ClusterAverageMetric clustMetric = new ClusterAverageMetric();

      for (Map.Entry<String, Isolate> isoEntry : isoMap.entrySet()) {
         clusters.add(new HCluster(clustMetric, isoEntry.getValue()));
      }

      if (mOntology != null) { clusterer = new OHClusterer(clusters, ontology); }
      else if (mOntology == null) { clusterer = new AgglomerativeClusterer(clusters); }

      AnalysisWorker worker = new AnalysisWorker(clusterer,
       MainWindow.getMainFrame().getOutputCanvas());

      worker.setOutputFile(mOutFile.getText());
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

   @SuppressWarnings("unchecked")
   private Map<String, Isolate> constructIsolates(List<Map<String, Object>> dataList) {
      Map<String, Isolate> isoMap = new HashMap<String, Isolate>();
      Map<String, Map<Integer, Object[]>> pyroDataMap =
         new HashMap<String, Map<Integer, Object[]>>();

      double tmp_alpha = .99, tmp_beta = .995;

      //First pass over the data where ITSRegions and Isolates are constructed.
      String pyroList = "";
      for (Map<String, Object> dataMap : dataList) {
         String isoID = String.valueOf(dataMap.get("isolate"));
         String regName = String.valueOf(dataMap.get("region"));
         String wellID = String.valueOf(dataMap.get("well"));
         Integer pyroID = new Integer(String.valueOf(dataMap.get("pyroprint")));
         double peakHeight = Double.parseDouble(String.valueOf(dataMap.get("pHeight")));
         String nucleotide = String.valueOf(dataMap.get("nucleotide"));


         //Retrieve Isolate
         if (!isoMap.containsKey(isoID)) {
            isoMap.put(isoID, new Isolate(isoID, new HashSet<ITSRegion>(),
                                          new IsolateAverageMetric()));
         }

         isoMap.get(isoID).getData().add(new ITSRegion(regName, tmp_alpha, tmp_beta,
                                         new ITSRegionAverageMetric(tmp_alpha, tmp_beta)));

         if (!pyroDataMap.containsKey(isoID)) {
            pyroDataMap.put(isoID, new HashMap<Integer, Object[]>());
         }

         Map<Integer, Object[]> pyroMap = pyroDataMap.get(isoID);

         if (!pyroMap.containsKey(pyroID)) {
            pyroMap.put(pyroID, new Object[] {pyroID, wellID, "",
                                              new ArrayList<Double>(),
                                              regName});
         }

         Object[] pyroData = pyroMap.get(pyroID);

         if (pyroData[2] instanceof String) {
            pyroData[2] = String.valueOf(pyroData[2]).concat(nucleotide);
         }
         if (pyroData[3] instanceof List<?>) {
            List<Double> peakList = (List<Double>) pyroData[3];
            peakList.add(new Double(peakHeight));
         }
      }

      for (Map.Entry<String, Map<Integer, Object[]>> pyroDataEntry : pyroDataMap.entrySet()) {

         for (Map.Entry<Integer, Object[]> pyroEntry : pyroDataEntry.getValue().entrySet()) {
            Object[] pyroData = pyroEntry.getValue();

            Pyroprint newPyro = new Pyroprint(Integer.parseInt(String.valueOf(pyroData[0])),
                                              String.valueOf(pyroData[1]),
                                              String.valueOf(pyroData[2]),
                                              (List<Double>) pyroData[3],
                                              new PyroprintUnstablePearsonMetric());

            for (ITSRegion region : isoMap.get(pyroDataEntry.getKey()).getData()) {
               if (region.getName().equals(pyroData[4])) {
                  region.getData().add(newPyro);
               }
            }
         }

      }

      Map<String, Isolate> finalIsoMap = new HashMap<String, Isolate>();

      Logger.debug("Isolates retrieved from CPLOP:");
      for (Map.Entry<String, Isolate> isoEntry : isoMap.entrySet()) {
         Logger.debug(String.format("%s: %s\n", isoEntry.getKey(),
                                    isoEntry.getValue().toString()));

         boolean isCompleteIsolate = true;

         for (ITSRegion region : isoEntry.getValue().getData()) {
            if (region.getData().isEmpty()) {
               Logger.debug(String.format("%s[%s] has no pyroprints\n",
                                          isoEntry.getValue().getName(),
                                          region.getName()));
               isCompleteIsolate = false;
            }
         }

         if (isCompleteIsolate) {
            finalIsoMap.put(isoEntry.getKey(), isoEntry.getValue());
         }
      }

      return isoMap;
   }
}
