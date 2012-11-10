package com.drin.java.gui.dialogs;

import com.drin.java.parsers.MatrixParser;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.IsolateSimpleMetric;
import com.drin.java.metrics.ClusterAverageMetric;

import com.drin.java.clustering.Cluster;
import com.drin.java.clustering.HCluster;

import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.OHClustering;

import com.drin.java.ontology.Ontology;

import com.drin.java.gui.MainWindow;
import com.drin.java.gui.components.AnalysisWorker;

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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JSeparator;

import java.io.File;

import java.util.Map;
import java.util.Set;
import java.util.List;

import java.util.HashMap;
import java.util.HashSet;

import java.lang.reflect.Constructor;

@SuppressWarnings("serial")
public class ClusterFileDialog extends JDialog {
   /*
    * CONSTANTS
    */
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 500;
   private final String ALPHA_VAL = "99.5", BETA_VAL = "99.0";

   private final String[] ITS_REGIONS = new String[] { "16s-23s", "23s-5s" };
   /*
   private final String[] CLUSTER_METHODS = new String[] {"Hierarchical",
                                                          "OHClustering"};
   */

   /*
    * GUI Components
    */
   private String mRecentDir;
   private Container mPane = null;
   private JComboBox mMethod, mRegion_A, mRegion_B;
   private JTextField mData_A, mData_B, mOutFile, mOntology,
                      mAlpha_A, mAlpha_B, mBeta_A, mBeta_B;

   public static void main(String[] args) {
      ClusterFileDialog dialog = new ClusterFileDialog(null, "Cluster Input Data");

      dialog.init();
      dialog.setVisible(true);
   }

   public ClusterFileDialog(Frame owner, String title) {
      super(owner, title);

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      mRecentDir = "";

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      //mMethod = new JComboBox(CLUSTER_METHODS);
      mRegion_A = new JComboBox(ITS_REGIONS);
      mRegion_B = new JComboBox(ITS_REGIONS);

      mData_A = new JTextField(20);
      mData_B = new JTextField(20);
      mOutFile = new JTextField(20);
      mOntology = new JTextField(15);

      mAlpha_A = new JTextField(ALPHA_VAL, 20);
      mAlpha_B = new JTextField(ALPHA_VAL, 20);

      mBeta_A  = new JTextField(BETA_VAL, 20);
      mBeta_B  = new JTextField(BETA_VAL, 20);
   }

   public void init() {
      //mPane.add(headerSection(mOutFile, mOntology, mMethod));
      mPane.add(headerSection(mOutFile, mOntology));

      JPanel labelField = new JPanel();
      labelField.setLayout(new FlowLayout(FlowLayout.LEADING));
      labelField.add(new JLabel("Input Dataset"));

      mPane.add(labelField);
      mPane.add(new JSeparator());
      mPane.add(inputField(mRegion_A, mData_A, mAlpha_A, mBeta_A, mOutFile));

      mPane.add(labelField);
      mPane.add(new JSeparator());
      mPane.add(inputField(mRegion_B, mData_B, mAlpha_B, mBeta_B, mOutFile));

      mPane.add(controls());

      mPane.validate();
   }

   //public JPanel headerSection(JTextField outfile, JTextField ontology, JComboBox method) {
   public JPanel headerSection(JTextField outfile, JTextField ontology) {
      JPanel ontologySection = null, layout = new JPanel();

      layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

      ontologySection = horizontal_input("Experimental Organization", ontology);
      ontologySection.add(fileBrowseButton(ontology, null));

      layout.add(horizontal_input("Output file name:", outfile));
      //layout.add(horizontal_input("Clustering Method:", method));
      layout.add(ontologySection);
      layout.setAlignmentY(Component.CENTER_ALIGNMENT);

      return layout;
   }

   public JPanel inputField(JComboBox region, JTextField input,
    JTextField alpha, JTextField beta, JTextField outfile) {
      JPanel selection = new JPanel(), fileInput = new JPanel(),
             fileInputPanel = new JPanel();

      selection.setLayout(new BoxLayout(selection, BoxLayout.Y_AXIS));
      selection.add(new JLabel("ITS Region:"));
      selection.add(region);
      selection.setAlignmentX(Component.LEFT_ALIGNMENT);

      fileInput.setLayout(new BoxLayout(fileInput, BoxLayout.Y_AXIS));
      fileInput.add(new JLabel("File:"));
      fileInput.add(input);
      fileInput.add(new JLabel("Threshold:"));
      fileInput.add(alpha);
      fileInput.add(beta);
      fileInput.setAlignmentY(Component.CENTER_ALIGNMENT);

      fileInputPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
      fileInputPanel.add(selection);
      fileInputPanel.add(fileInput);
      fileInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
      fileInputPanel.add(fileBrowseButton(input, outfile));

      return fileInputPanel;
   }

   private JPanel controls() {
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
            if (mData_A.getText().equals("")  || mData_B.getText().equals("") ||
                mAlpha_A.getText().equals("") || mBeta_A.getText().equals("") ||
                mAlpha_B.getText().equals("") || mBeta_B.getText().equals("")) {
               JOptionPane.showMessageDialog(null, "Invalid input",
                                             "Invalid Input",
                                             JOptionPane.ERROR_MESSAGE);
               return;
            }

            boolean isValid = takeAction();

            if (isValid) { dispose(); }
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

   private boolean performSanityChecks(String region_A, String region_B,
                                       String data_A, String data_B,
                                       double alpha_A, double alpha_B) {
      if (region_A.equals(region_B)) {
         JOptionPane.showMessageDialog(null,
          "Please select different regions for each input data",
          "Invalid Regions Selected", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      if (data_A.equals(data_B)) {
         int selectedOption = JOptionPane.showConfirmDialog(null,
          "Same file input for both regions. Are you sure?",
          "Duplicate input confirmation", JOptionPane.YES_NO_OPTION);

         if (selectedOption == JOptionPane.NO_OPTION) {
            JOptionPane.showMessageDialog(null, "Clustering cancelled",
                                          "Cancelled",
                                          JOptionPane.INFORMATION_MESSAGE);
            return false;
         }
      }
         
      if (alpha_A < 1 || alpha_B < 1) {
         JOptionPane.showMessageDialog(null, "Invalid threshold values",
                                       "Invalid Options",
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }

      if (data_A.equals("")) {
         JOptionPane.showMessageDialog(null, "Invalid parameters for " +
                                       "first data input", "Invalid Options",
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }
      if (data_B.equals("")) {
         JOptionPane.showMessageDialog(null, "Invalid parameters for " +
                                       "second data input", "Invalid Options",
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }

      return true;
   }

   /*
    * args for clusterer consist of:
    *    String filename - a file name
    *    String regionname - name of the ITS region represented
    *    String lowerThreshold - a double value
    *    String upperThreshold - a double value
    *    one option from:
    *       Single
    *       Average
    *       Complete
    *       Ward
    * argument 1 (filename) is *MANDATORY*
    * argument 2 (region name) is *MANDATORY*
    * argument 2 defaults to 95% similarity
    * argument 3 defaults to 99.7% similarity
    * argument 4 defaults to Average similarity distance
    */
   private boolean takeAction() {
      Ontology ontology = null;
      Clusterer clusterer = null;
      Set<Cluster> clusters = new HashSet<Cluster>();
      Map<String, double[]> threshMap = new HashMap<String, double[]>();
      
      if (!mOntology.getText().equals("")) {
         ontology = Ontology.createOntology(mOntology.getText());
      }

      String region_A = String.valueOf(mRegion_A.getSelectedItem());
      String data_A   = mData_A.getText();
      double alpha_A  = Double.parseDouble(mAlpha_A.getText());
      double beta_A   = Double.parseDouble(mBeta_A.getText());

      String region_B = String.valueOf(mRegion_B.getSelectedItem());
      String data_B   = mData_B.getText();
      double alpha_B  = Double.parseDouble(mAlpha_B.getText());
      double beta_B   = Double.parseDouble(mBeta_B.getText());

      boolean isValid = performSanityChecks(region_A, region_B, data_A, data_B,
                                            alpha_A, alpha_B);
      if (!isValid) { return false; }

      alpha_A = alpha_A > 1 ? alpha_A / 100 : alpha_A;
      beta_A  = beta_A  > 1 ? beta_A  / 100 : beta_A;
      alpha_B = alpha_B > 1 ? alpha_B / 100 : alpha_B;
      beta_B  = beta_B  > 1 ? beta_B  / 100 : beta_B;

      Map<String, Map<String, Map<String, Double>>> regionMap =
                  new HashMap<String, Map<String, Map<String, Double>>>();

      Set<String> isoIds = null;
      //Build correlation map for region A
      MatrixParser parser = new MatrixParser(data_A);
      Map<String, Map<String, Double>> corrMap = parser.parseData();
      isoIds = corrMap.keySet();
      regionMap.put(region_A, corrMap);
      threshMap.put(region_A, new double[] {alpha_A, beta_A});

      //Build correlation map for region B
      parser = new MatrixParser(data_B);
      corrMap = parser.parseData();
      regionMap.put(region_B, corrMap);
      threshMap.put(region_B, new double[] {alpha_B, beta_B});

      //Build a metric that now encompasses both regions
      IsolateSimpleMetric isoMetric = new IsolateSimpleMetric(regionMap);

      if (isoIds != null) {
         for (String isoId : isoIds) {
            Cluster cluster = new HCluster(new ClusterAverageMetric(),
                                           new Isolate(isoId, threshMap, isoMetric));
            clusters.add(cluster);
         }
      }

      if (mOntology != null) { clusterer = new OHClustering(clusters, ontology); }
      else if (mOntology == null) { clusterer = new AgglomerativeClusterer(clusters); }

      AnalysisWorker worker = new AnalysisWorker(clusterer,
       MainWindow.getMainFrame().getOutputCanvas());

      worker.setOutputFile(mOutFile.getText());
      worker.execute();

      return true;
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
}
