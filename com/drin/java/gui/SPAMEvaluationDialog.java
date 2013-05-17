package com.drin.java.gui.dialogs;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterAverageMetric;
import com.drin.java.metrics.IsolateAverageMetric;
import com.drin.java.metrics.ITSRegionAverageMetric;
import com.drin.java.metrics.ITSRegionMedianMetric;
import com.drin.java.metrics.PyroprintUnstablePearsonMetric;

import com.drin.java.ontology.Ontology;
import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import com.drin.java.gui.MainWindow;
import com.drin.java.gui.listeners.DataQueryButtonListener;
import com.drin.java.gui.components.AnalysisWorker;
import com.drin.java.gui.components.AnalysisWorker.TaskResult;
import com.drin.java.database.CPLOPConnection;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JComponent;

import java.io.File;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

public class SPAMEvaluationDialog extends JDialog {
   /*
    * CONSTANTS
    */
   private final static String DEFAULT_TITLE = "Parameter Input Dialog";
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 550;

   private final static String DEFAULT_REGION_A = "16-23",
                               DEFAULT_REGION_B = "23-5";

   private final static String ONT_DIR    = "ontologies",
                               USER_DIR   = System.getProperty("user.dir"),
                               FILE_SEP   = System.getProperty("file.separator");

   private final String[] ITS_REGIONS = new String[] { DEFAULT_REGION_A, DEFAULT_REGION_B };
   private final String[] DATA_TYPE_VALUES = new String[] {"Isolates",
                                                           "Pyroprints",
                                                           "Experiments"};
   /*
    * GUI Components
    */
   private Container mPane = null;
   private JComboBox<String> mDataSetType, mRegion_A, mRegion_B;
   private JTextField mDataSet, mOutFile, mOntology,
                      mAlpha_A, mAlpha_B, mBeta_A, mBeta_B,
                      mPyroLen_A, mPyroLen_B;
   private String mRecentDir;

   private Configuration mConf;
   private CPLOPConnection mConn;
   private long startTime;

   public SPAMEvaluationDialog(Frame owner, String title) {
      super(owner, title);

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      mConf = Configuration.loadConfig();
      mConn = CPLOPConnection.getConnection();

      mDataSet = new JTextField(20);

      mOutFile = new JTextField(20);
      mOntology = new JTextField(String.format("%s%s%s%s%s",
         USER_DIR, FILE_SEP, ONT_DIR, FILE_SEP, mConf.getAttr("ontology")
      ), 15);

      mRegion_A = new JComboBox<String>(ITS_REGIONS);
      mRegion_B = new JComboBox<String>(ITS_REGIONS);
      mRegion_B.setSelectedIndex(1);

      mAlpha_A = new JTextField(mConf.getRegionAttr(DEFAULT_REGION_A, Configuration.ALPHA_KEY), 20);
      mAlpha_B = new JTextField(mConf.getRegionAttr(DEFAULT_REGION_B, Configuration.ALPHA_KEY), 20);

      mBeta_A  = new JTextField(mConf.getRegionAttr(DEFAULT_REGION_A, Configuration.BETA_KEY), 20);
      mBeta_B  = new JTextField(mConf.getRegionAttr(DEFAULT_REGION_B, Configuration.BETA_KEY), 20);

      mPyroLen_A = new JTextField(mConf.getRegionAttr(DEFAULT_REGION_A, Configuration.LENGTH_KEY), 10);
      mPyroLen_B = new JTextField(mConf.getRegionAttr(DEFAULT_REGION_B, Configuration.LENGTH_KEY), 10);

      mRecentDir = "";

      mDataSetType = new JComboBox<String>(DATA_TYPE_VALUES);
   }

   public SPAMEvaluationDialog() {
      this(null, DEFAULT_TITLE);
   }

   public static void main(String[] args) {
      SPAMEvaluationDialog dialog = new SPAMEvaluationDialog();

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

   private List<Map<String, Object>> queryData(Ontology ont) {
      long queryStart = System.currentTimeMillis();

      List<Map<String, Object>> dataList = null;
      String dataSet = mDataSet.getText();

      try {
         String selection = String.valueOf(mDataSetType.getSelectedItem());

         //Isolates : 0
         //Pyroprints : 1
         //Experiments : 2

         dataList = selection.equals(DATA_TYPE_VALUES[0]) ? mConn.getDataByIsoID(ont, dataSet) :
                    selection.equals(DATA_TYPE_VALUES[1]) ? mConn.getDataByPyroID(ont, dataSet) :
                    selection.equals(DATA_TYPE_VALUES[2]) ? mConn.getDataByExperimentName(dataSet) :
                                                            null;
      }
      catch (java.sql.SQLException sqlErr) {
         System.out.println("SQLException:\nExiting...");
         sqlErr.printStackTrace();
         System.exit(1);
      }

      System.out.println("time to query: " + (System.currentTimeMillis() - queryStart));

      return dataList;
   }

   private void updateConfig() {
      String region_A  = String.valueOf(mRegion_A.getSelectedItem());
      String region_B  = String.valueOf(mRegion_B.getSelectedItem());

      mConf.setRegionAttr(region_A, Configuration.LENGTH_KEY, mPyroLen_A.getText());
      mConf.setRegionAttr(region_A, Configuration.ALPHA_KEY, mAlpha_A.getText());
      mConf.setRegionAttr(region_A, Configuration.BETA_KEY, mBeta_A.getText());

      mConf.setRegionAttr(region_B, Configuration.LENGTH_KEY, mPyroLen_B.getText());
      mConf.setRegionAttr(region_B, Configuration.ALPHA_KEY, mAlpha_B.getText());
      mConf.setRegionAttr(region_B, Configuration.BETA_KEY, mBeta_B.getText());

      if (!mOntology.getText().equals("")) {
         mConf.setAttr(Configuration.ONT_KEY, mOntology.getText());
      }
      else {
         mConf.setAttr(Configuration.ONT_KEY, null);
      }
   }

   private boolean doWork() {
      List<Cluster> clusters = new ArrayList<Cluster>();
      Clusterer clusterer = null;
      Ontology ontology = null;
      updateConfig();

      if (mConf != null && mConf.getAttr(Configuration.ONT_KEY) != null) {
         ontology = Ontology.createOntology(new File(mConf.getAttr(Configuration.ONT_KEY)));
      }

      int use_transform = 0;
      if (Boolean.parseBoolean(mConf.getAttr(Configuration.TRANSFORM_KEY))) {
         use_transform = 1;
      }

      ClusterAverageMetric clustMetric = new ClusterAverageMetric();

      startTime = System.currentTimeMillis();

      List<Double> thresholds = new ArrayList<Double>();
      thresholds.add(new Double(mConf.getRegionAttr("16-23", Configuration.ALPHA_KEY)));
      thresholds.add(new Double(mConf.getRegionAttr("16-23", Configuration.BETA_KEY)));

      List<Clusterable<?>> dataList = null;
      String selection = String.valueOf(mDataSetType.getSelectedItem());

      dataList = selection.equals(DATA_TYPE_VALUES[0]) ? constructEntities(queryData(ontology), ontology) :
                 selection.equals(DATA_TYPE_VALUES[1]) ? constructPyroprints(queryData(ontology), ontology) :
                 selection.equals(DATA_TYPE_VALUES[2]) ? constructPyroprints(queryData(ontology), ontology) :
                                                         null;

      Cluster.resetClusterIDs();
      for (Clusterable<?> entity : dataList) {
         clusters.add(new HCluster(clustMetric, entity));
      }

      int initialSize = clusters.size();

      clusterer = new OHClusterer(ontology, thresholds);
      clusterer.setProgressCanvas(MainWindow.getMainFrame().getOutputCanvas());

      AnalysisWorker worker = new AnalysisWorker(clusterer, clusters,
       MainWindow.getMainFrame().getOutputCanvas());

      worker.setOutputFile(mOutFile.getText());

      worker.execute();
      TaskResult result = null;
      try {
         result = worker.get();
      }
      catch(Exception err) {
         err.printStackTrace();
      }

      /*
       * store results
       */
      try {
         mConn.insertNewRun(String.format(
            "INSERT INTO test_runs(run_date, run_time, cluster_algorithm," +
                                  "average_strain_similarity, use_transform) " +
            "VALUES (?, '%s', '%s', %.04f, %d)",
            getElapsedTime(result.mElapsedTime), clusterer.getName(),
            clusterer.getInterClusterSimilarity(), use_transform
         ));

         int runID = mConn.getTestRunId();

         String performanceInsert = String.format(
               "INSERT INTO test_run_performance(" +
               "test_run_id, update_id, update_size, run_time) " +
               "VALUES (%d, %d, %d, %d)",
               runID, 0, initialSize, result.mElapsedTime
         );
         mConn.executeInsert(performanceInsert);

         if (runID != -1) {
            for (String sqlQuery : getSQLInserts(runID, result.mClusterData)) {
               if (sqlQuery != null) {
                  System.out.printf("%s\n", sqlQuery);

                  try { mConn.executeInsert(sqlQuery); }
                  catch(java.sql.SQLException sqlErr) {
                     sqlErr.printStackTrace();
                  }
               }
            }
         }
      }
      catch (java.sql.SQLException sqlErr) {
         sqlErr.printStackTrace();
      }

      Logger.debug(String.format("Time to do all that stuff: %d ms",
                   (System.currentTimeMillis() - startTime)));

      return true;
   }

   private String[] getSQLInserts(int clusterRun, Map<Double, List<Cluster>> clusters) {
      String[] sqlInserts = new String[100];
      int isoId = -1, delimNdx = -1, clustNum = 0, isolateNum = 0, limit = 1000, sqlNdx = 0;
      String strainInsert = "INSERT INTO test_run_strain_link(" +
                             "test_run_id, cluster_id, cluster_threshold, " +
                             "strain_diameter, average_isolate_similarity, " +
                             "percent_similar_isolates) VALUES ";
      String isolateInsert = "INSERT INTO test_isolate_strains(" +
                              "test_run_id, cluster_id, cluster_threshold, " +
                              "name_prefix, name_suffix) VALUES ";
      String run_strain_link = strainInsert, isolate_strain = isolateInsert, elementName, isoDesignation;

      for (Map.Entry<Double, List<Cluster>> clusterData : clusters.entrySet()) {
         for (Cluster cluster : clusterData.getValue()) {

            if (sqlNdx >= sqlInserts.length - 2) {
               String[] newArr = new String[sqlInserts.length * 2];
               for (int ndx = 0; ndx < sqlInserts.length; ndx++) {
                  newArr[ndx] = sqlInserts[ndx];
               }
            }

            if (clustNum > 0 && clustNum++ % limit == 0) {
               sqlInserts[sqlNdx++] = run_strain_link.substring(0, run_strain_link.length() - 2);
               System.out.printf("sqlInsert: \n\t%s\n", sqlInserts[sqlNdx - 1]);

               run_strain_link = strainInsert;
            }

            run_strain_link += String.format(
               "(%d, %d, %.04f, %.04f, %.04f, %.04f), ",
               clusterRun, cluster.getId(), clusterData.getKey().doubleValue(),
               cluster.getDiameter(), cluster.getPercentSimilar(), cluster.getMean()
            );

            for (Clusterable<?> element : cluster.getElements()) {
               elementName = element.getName();
               delimNdx = elementName.indexOf("-");
               isoDesignation = elementName.substring(0, delimNdx);
               isoId = Integer.parseInt(elementName.substring(delimNdx + 1, elementName.length()));

               if (isolateNum > 0 && isolateNum++ % limit == 0) {
                  sqlInserts[sqlNdx++] = isolate_strain.substring(0, isolate_strain.length() - 2);
                  System.out.printf("sqlInsert: \n\t%s\n", sqlInserts[sqlNdx - 1]);

                  isolate_strain = isolateInsert;
               }

               isolate_strain += String.format(
                  "(%d, %d, %.04f, '%s', %d), ",
                  clusterRun, cluster.getId(), clusterData.getKey().doubleValue(),
                  isoDesignation, isoId
               );
            }
         }
      }

      if (!run_strain_link.equals(strainInsert)) {
         sqlInserts[sqlNdx++] = run_strain_link.substring(0, run_strain_link.length() - 2);
      }

      if (!isolate_strain.equals(isolateInsert)) {
         sqlInserts[sqlNdx++] = isolate_strain.substring(0, isolate_strain.length() - 2);
      }

      return sqlInserts;
   }

   private String getElapsedTime(long clusterTime) {
      long hours = clusterTime / 3600000;
      long minutes = (clusterTime % 3600000) / 60000;
      long seconds = ((clusterTime % 3600000) % 60000) / 1000;

      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private List<Clusterable<?>> constructPyroprints(List<Map<String, Object>> dataList, Ontology ont) {
      long constructStart = System.currentTimeMillis();
      List<Clusterable<?>> entityList = new ArrayList<Clusterable<?>>();

      DataMetric<Pyroprint> pyroMetric = null;

      try {
         pyroMetric = (DataMetric) Class.forName(mConf.getMetric(Configuration.PYROPRINT_KEY)).newInstance();
      }
      catch(Exception err) {
         err.printStackTrace();
         return null;
      }

      Pyroprint tmpPyro = null;
      for (Map<String, Object> dataMap : dataList) {
         String wellID = String.valueOf(dataMap.get("well"));
         String regName = String.valueOf(dataMap.get("region"));

         Integer pyroID = new Integer(String.valueOf(dataMap.get("pyroprint")));

         String nucleotide = String.valueOf(dataMap.get("nucleotide"));
         double peakHeight = Double.parseDouble(String.valueOf(dataMap.get("pHeight")));

         String pyroName = String.format("%d (%s)", pyroID.intValue(), wellID);

         if (tmpPyro == null || !tmpPyro.getName().equals(pyroName)) {
            if (tmpPyro != null) {
               entityList.add(tmpPyro);
            }

            tmpPyro = new Pyroprint(pyroID.intValue(), wellID, pyroMetric);

            if (ont != null) {
               for (Map.Entry<String, Set<String>> tableCols : ont.getTableColumns().entrySet()) {
                  for (String colName : tableCols.getValue()) {
                     if (colName.replace(" ", "").equals("")) { continue; }

                     tmpPyro.addLabel(colName, String.valueOf(dataMap.get(colName)).trim());
                  }
               }
            }
         }

         if (tmpPyro.getName().equals(pyroName) && tmpPyro.getDispLen() <
             Integer.parseInt(mConf.getRegionAttr(regName, Configuration.LENGTH_KEY))) {
               tmpPyro.addDispensation(nucleotide, peakHeight);
         }

      }

      Logger.debug(String.format("Time to construct Pyroprints: %d ms",
                   (System.currentTimeMillis() - constructStart)));

      return entityList;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private List<Clusterable<?>> constructEntities(List<Map<String, Object>> dataList, Ontology ont) {
      long constructStart = System.currentTimeMillis();
      List<Clusterable<?>> entityList = new ArrayList<Clusterable<?>>();

      DataMetric<Isolate> isoMetric = null;
      DataMetric<ITSRegion> regionMetric = null;
      DataMetric<Pyroprint> pyroMetric = null;

      try {
         isoMetric = (DataMetric) Class.forName(mConf.getMetric(Configuration.ISOLATE_KEY)).newInstance();
         regionMetric = (DataMetric) Class.forName(mConf.getMetric(Configuration.ITSREGION_KEY)).newInstance();
         pyroMetric = (DataMetric) Class.forName(mConf.getMetric(Configuration.PYROPRINT_KEY)).newInstance();
      }
      catch(Exception err) {
         err.printStackTrace();
         return null;
      }

      Isolate tmpIso = null;
      Pyroprint tmpPyro = null;
      for (Map<String, Object> dataMap : dataList) {
         String wellID = String.valueOf(dataMap.get("well"));
         String isoID = String.valueOf(dataMap.get("isolate"));
         String regName = String.valueOf(dataMap.get("region"));
         Integer pyroID = new Integer(String.valueOf(dataMap.get("pyroprint")));

         String nucleotide = String.valueOf(dataMap.get("nucleotide"));
         double peakHeight = Double.parseDouble(String.valueOf(dataMap.get("pHeight")));

         String pyroName = String.format("%d (%s)", pyroID.intValue(), wellID);

         if (tmpIso == null || !tmpIso.getName().equals(isoID)) {
            tmpIso = new Isolate(isoID, isoMetric);

            if (ont != null) {
               for (Map.Entry<String, Set<String>> tableCols : ont.getTableColumns().entrySet()) {
                  for (String colName : tableCols.getValue()) {
                     if (colName.replace(" ", "").equals("")) { continue; }

                     tmpIso.addLabel(colName, String.valueOf(dataMap.get(colName)).trim());
                  }
               }
            }

            entityList.add(tmpIso);
         }

         if (tmpIso != null) {
            tmpIso.getData().add(new ITSRegion(regName, regionMetric));
         }

         if (tmpPyro == null || !tmpPyro.getName().equals(pyroName)) {
            tmpPyro = new Pyroprint(pyroID.intValue(), wellID, pyroMetric);
            tmpIso.getRegion(regName).add(tmpPyro);
         }

         if (tmpPyro.getName().equals(pyroName) && tmpPyro.getDispLen() <
             Integer.parseInt(mConf.getRegionAttr(regName, Configuration.LENGTH_KEY))) {
               tmpPyro.addDispensation(nucleotide, peakHeight);
         }
      }

      Logger.debug(String.format("Time to construct Isolates: %d ms",
                   (System.currentTimeMillis() - constructStart)));

      return entityList;
   }
}
