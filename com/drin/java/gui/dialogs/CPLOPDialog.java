package com.drin.java.gui.dialogs;

import com.drin.java.SPAMMain;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.clustering.Clusterable;
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
import java.util.HashSet;

@SuppressWarnings("serial")
public class CPLOPDialog extends JDialog {
   /*
    * CONSTANTS
    */
   private final static String DEFAULT_TITLE = "Parameter Input Dialog";
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 550;
   private final String ALPHA_VAL = "99.5", BETA_VAL = "99.0",
                        PYRO_LEN_A = "94", PYRO_LEN_B = "96";

   private final String[] ITS_REGION_A = new String[] { "23-5" },
                          ITS_REGION_B = new String[] { "16-23" };
   private final String[] DATA_TYPE_VALUES = new String[] { "Isolates" };
   /*
    * GUI Components
    */
   private SPAMMain mSPAMInterface;
   private MainWindow mOwner;
   private Container mPane;
   private JComboBox<String> mDataSetType, mRegion_A, mRegion_B;
   private JTextField mDataSet, mOutFile, mOntology,
                      mAlpha_A, mAlpha_B, mBeta_A, mBeta_B,
                      mPyroLen_A, mPyroLen_B;
   private String mRecentDir;

   /*
    * Other Member Variables
    */
   private CPLOPConnection mConn;
   private long startTime;

   public CPLOPDialog(Frame owner, String title) {
      super(owner, title);

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      mDataSet = new JTextField(20);

      mOutFile = new JTextField(20);
      mOntology = new JTextField(15);

      mRegion_A = new JComboBox<String>(ITS_REGION_A);
      mRegion_B = new JComboBox<String>(ITS_REGION_B);

      mAlpha_A = new JTextField(ALPHA_VAL, 20);
      mAlpha_B = new JTextField(ALPHA_VAL, 20);

      mBeta_A  = new JTextField(BETA_VAL, 20);
      mBeta_B  = new JTextField(BETA_VAL, 20);

      mPyroLen_A = new JTextField(PYRO_LEN_A, 10);
      mPyroLen_B = new JTextField(PYRO_LEN_B, 10);

      mRecentDir = "";

      mDataSetType = new JComboBox<String>(DATA_TYPE_VALUES);

      try { mConn = new CPLOPConnection(); }
      catch(java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch(Exception err) { err.printStackTrace(); }

      mSPAMInterface = MainWindow.getMainFrame().getSPAMInterface();
   }

   public CPLOPDialog() {
      this(null, DEFAULT_TITLE);
   }

   public static void main(String[] args) {
      CPLOPDialog dialog = new CPLOPDialog();

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

   public JPanel inputField(JComboBox<String> region, JTextField pyro_len,
                            JTextField alpha, JTextField beta) {
      JPanel selection = new JPanel(), fileInput = new JPanel(), fileInputPanel = new JPanel();

      selection.setLayout(new BoxLayout(selection, BoxLayout.Y_AXIS));
      selection.add(new JLabel("ITS Region:"));
      selection.add(region);

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

   private JButton prepareDataQueryButton(JTextField dataSetField,
                                          JComboBox<String> dataTypeOptions) {
      final JButton dataQueryButton = new JButton("Choose " + dataTypeOptions.getSelectedItem());

      dataTypeOptions.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            String selectedItem = String.valueOf(e.getItem());

            dataQueryButton.setText("Choose " + selectedItem);
         }
      });

      dataQueryButton.addActionListener(
       new DataQueryButtonListener(dataSetField, mConn));

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

   private boolean doWork() {
      mSPAMInterface.setProgressWriter(MainWindow.getMainFrame().getOutputCanvas());

      if (!mOntology.getText().equals("")) {
         mSPAMInterface.setOntology(
            Ontology.createOntology(new File(mOntology.getText()))
         );
      }
      else { mSPAMInterface.setOntology(null); }

      startTime = System.currentTimeMillis();

      //Isolates
      if (mDataSet.getText() != null) {
         mSPAMInterface.clusterData(mDataSet.getText(), "Isolates");
      }

      mSPAMInterface.writeResults(mOutFile.getText());

      System.out.println("Time to prepare clusterer: " + (System.currentTimeMillis() - startTime));

      return true;
   }

   private void setParams() {
      mSPAMInterface.setAlpha(Float.parseFloat(mAlpha_A.getText()));
      mSPAMInterface.setBeta(Float.parseFloat(mBeta_A.getText()));
   }

   private JButton createOkayButton() {
      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            if (mDataSet.getText().equals("")) {
               System.out.printf("No data selected.\n");
               return;
            }

            setParams();
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

}
