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
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 550;
   private final String ALPHA_VAL = "99.5", BETA_VAL = "99.0", PYRO_LEN = "94";

   private final String[] ITS_REGIONS = new String[] { "16-23", "23-5" };
   private final String[] DATA_TYPE_VALUES = new String[] {"Isolates",
                                                           "Pyroprints",
                                                           "Experiments"};
   /*
    * GUI Components
    */
   private Container mPane = null, mOwner = null;
   private JDialog mDialog = null;
   private JComboBox<String> mDataSetType, mRegion_A, mRegion_B;
   private JTextField mDataSet, mOutFile, mOntology,
                      mAlpha_A, mAlpha_B, mBeta_A, mBeta_B,
                      mPyroLen_A, mPyroLen_B;
   private String mRecentDir;

   private CPLOPConnection mConn;
   private long startTime;

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

      mOutFile = new JTextField(20);
      mOntology = new JTextField(15);

      mRegion_A = new JComboBox<String>(ITS_REGIONS);
      mRegion_B = new JComboBox<String>(ITS_REGIONS);
      mRegion_B.setSelectedIndex(1);

      mAlpha_A = new JTextField(ALPHA_VAL, 20);
      mAlpha_B = new JTextField(ALPHA_VAL, 20);

      mBeta_A  = new JTextField(BETA_VAL, 20);
      mBeta_B  = new JTextField(BETA_VAL, 20);

      mPyroLen_A = new JTextField(PYRO_LEN, 10);
      mPyroLen_B = new JTextField(PYRO_LEN, 10);

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

      JLabel thresholdSetLabel = new JLabel("Input Region Thresholds:");
      JPanel thresholdField_A = inputField(mRegion_A, mPyroLen_A, mAlpha_A, mBeta_A);
      JPanel thresholdField_B = inputField(mRegion_B, mPyroLen_B, mAlpha_B, mBeta_B);

      mPane.add(dataSetLabel);
      mPane.add(dataSetField);

      mPane.add(thresholdSetLabel);
      mPane.add(thresholdField_A);
      mPane.add(thresholdField_B);

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

   public JPanel inputField(JComboBox<String> region, JTextField pyro_len, JTextField alpha, JTextField beta) {
      JPanel selection = new JPanel(), fileInput = new JPanel(), fileInputPanel = new JPanel();

      selection.setLayout(new BoxLayout(selection, BoxLayout.Y_AXIS));
      selection.add(new JLabel("ITS Region:"));
      selection.add(region);
      //pyro_len is how much of the pyroprint's dispensations to consider for
      //constructing a pyroprint
      selection.add(new JLabel("Disp Length:"));
      selection.add(pyro_len);
      selection.setAlignmentX(Component.LEFT_ALIGNMENT);

      fileInput.setLayout(new BoxLayout(fileInput, BoxLayout.Y_AXIS));
      fileInput.add(new JLabel("Thresholds:"));
      fileInput.add(alpha);
      fileInput.add(beta);
      fileInput.setAlignmentY(Component.CENTER_ALIGNMENT);

      fileInputPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
      fileInputPanel.add(selection);
      fileInputPanel.add(fileInput);
      fileInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

      return fileInputPanel;
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
      long queryStart = System.currentTimeMillis();

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

      else if (String.valueOf(mDataSetType.getSelectedItem()).equals(DATA_TYPE_VALUES[2])) {
         try {
            dataList = mConn.getDataByExperimentName(dataSet);
         }
         catch (java.sql.SQLException sqlErr) {
            System.out.println("SQLException:\nExiting...");
            sqlErr.printStackTrace();
            System.exit(1);
         }
      }

      System.out.println("time to query: " + (System.currentTimeMillis() - queryStart));

      return dataList;
   }

   private boolean doWork() {
      Ontology ontology = null;
      Clusterer clusterer = null;
      List<Cluster> clusters = new ArrayList<Cluster>();

      if (!mOntology.getText().equals("")) {
         ontology = Ontology.createOntology(mOntology.getText());
      }

      double alpha_A  = Double.parseDouble(mAlpha_A.getText());
      double beta_A   = Double.parseDouble(mBeta_A.getText());

      double alpha_B  = Double.parseDouble(mAlpha_B.getText());
      double beta_B   = Double.parseDouble(mBeta_B.getText());

      alpha_A = alpha_A > 1 ? alpha_A / 100 : alpha_A;
      beta_A  = beta_A  > 1 ? beta_A  / 100 : beta_A;
      alpha_B = alpha_B > 1 ? alpha_B / 100 : alpha_B;
      beta_B  = beta_B  > 1 ? beta_B  / 100 : beta_B;

      startTime = System.currentTimeMillis();

      Map<String, Isolate> isoMap = constructIsolates(queryData(), alpha_A, beta_A,
                                                      alpha_B, beta_B);
      
      ClusterAverageMetric clustMetric = new ClusterAverageMetric();

      for (Map.Entry<String, Isolate> isoEntry : isoMap.entrySet()) {
         Logger.debug(String.format("adding Isolate %s\n", isoEntry.getValue()));
         clusters.add(new HCluster(clustMetric, isoEntry.getValue()));
      }

      isoMap = null;


      if (ontology != null) {
         Logger.debug("OHClust!");
         List<Cluster> coreClusters = new ArrayList<Cluster>();
         List<Cluster> boundaryClusters = new ArrayList<Cluster>();
         Map<Integer, String> promotedClusters = new HashMap<Integer, String>();

         for (int ndx_A = 0; ndx_A < clusters.size(); ndx_A++) {
            Cluster clust_A = clusters.get(ndx_A);

            for (int ndx_B = ndx_A + 1; ndx_B < clusters.size(); ndx_B++) {
               Cluster clust_B = clusters.get(ndx_B);

               if (clust_A.compareTo(clust_B) > alpha_A) {
                  if (!promotedClusters.containsKey(new Integer(ndx_A))) {
                     coreClusters.add(clust_A);
                     promotedClusters.put(ndx_A, clust_A.getName());
                  }

                  if (!promotedClusters.containsKey(new Integer(ndx_B))) {
                     coreClusters.add(clust_B);
                     promotedClusters.put(ndx_B, clust_B.getName());
                  }
               }
            }
         }

         for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
            if (!promotedClusters.containsKey(clustNdx)) {
               boundaryClusters.add(clusters.get(clustNdx));
            }
         }

         clusters = null;
         clusterer = new OHClusterer(coreClusters, boundaryClusters,
                                     ontology, alpha_A, beta_A);
      }
      else if (ontology == null) {
         Logger.debug("Normal Clustering");
         clusterer = new AgglomerativeClusterer(clusters, beta_A);
      }

      AnalysisWorker worker = new AnalysisWorker(clusterer,
       MainWindow.getMainFrame().getOutputCanvas());

      worker.setOutputFile(mOutFile.getText());
      worker.execute();

      System.out.println("Time to prepare clusterer: " + (System.currentTimeMillis() - startTime));

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

   //TODO need this for when clustering pyroprints
   private Map<String, Pyroprint> constructPyroprints() {
      return null;
   }

   @SuppressWarnings("unchecked")
   //TODO construct hierarchy here by adding partition values as "tags" to
   //Isolate (implements Lableable).
   private Map<String, Isolate> constructIsolates(List<Map<String, Object>> dataList,
                                                  double alpha_A, double beta_A,
                                                  double alpha_B, double beta_B) {
      System.out.println("Constructing Isolates...");

      long constructStart = System.currentTimeMillis();

      Map<String, Isolate> isoMap = new HashMap<String, Isolate>();
      Map<String, double[]> threshMap = new HashMap<String, double[]>();
      Map<String, Map<Integer, Object[]>> pyroDataMap = new HashMap<String, Map<Integer, Object[]>>();

      double pyrolen_A = Double.parseDouble(mPyroLen_A.getText());
      double pyrolen_B = Double.parseDouble(mPyroLen_B.getText());

      threshMap.put(String.valueOf(mRegion_A.getSelectedItem()),
                                   new double[] {alpha_A, beta_A, pyrolen_A});

      threshMap.put(String.valueOf(mRegion_B.getSelectedItem()),
                                   new double[] {alpha_B, beta_B, pyrolen_B});

      //First pass over the data where ITSRegions and Isolates are constructed.
      String pyroList = "";
      for (Map<String, Object> dataMap : dataList) {
         String isoID = String.valueOf(dataMap.get("isolate"));
         String regName = String.valueOf(dataMap.get("region"));
         String wellID = String.valueOf(dataMap.get("well"));
         Integer pyroID = new Integer(String.valueOf(dataMap.get("pyroprint")));
         double peakHeight = Double.parseDouble(String.valueOf(dataMap.get("pHeight")));
         String nucleotide = String.valueOf(dataMap.get("nucleotide"));

         double alphaThresh = threshMap.get(regName)[0];
         double betaThresh  = threshMap.get(regName)[1];
         int pyro_len       = (int) threshMap.get(regName)[2];

         //Retrieve Isolate
         if (!isoMap.containsKey(isoID)) {
            isoMap.put(isoID, new Isolate(isoID, new HashSet<ITSRegion>(),
                                          new IsolateAverageMetric()));
         }

         isoMap.get(isoID).getData().add(new ITSRegion(regName, alphaThresh, betaThresh,
                                         new ITSRegionAverageMetric(alphaThresh, betaThresh)));

         //maintain pyroprint information based on isoID as we iterate over
         //tuples.

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
            if (String.valueOf(pyroData[2]).length() < pyro_len) {
               pyroData[2] = String.valueOf(pyroData[2]).concat(nucleotide);
            }
         }
         if (pyroData[3] instanceof List<?>) {
            List<Double> peakList = (List<Double>) pyroData[3];
            if (peakList.size() < pyro_len) {
               peakList.add(new Double(peakHeight));
            }
         }
      }

      //using the pyroprint information we have accumulated, we now know
      //we have iterated over all the data and so can add complete pyroprint
      //objects to isolates
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

      pyroDataMap = null;

      //now we must ensure that only "complete" isolates (i.e. has both regions
      //and no region is empty) are passed on to clustering
      Map<String, Isolate> finalIsoMap = new HashMap<String, Isolate>();

      Logger.debug("Isolates retrieved from CPLOP:");
      for (Map.Entry<String, Isolate> isoEntry : isoMap.entrySet()) {
         Logger.debug(String.format("%s: %s\n", isoEntry.getKey(),
                                    isoEntry.getValue().toString()));

         //have to have 2 regions
         boolean isCompleteIsolate = isoEntry.getValue().getData().size() == 2;

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

      System.out.println("Time to construct Isolates: " + (System.currentTimeMillis() - constructStart));

      return isoMap;
   }
}
