package com.drin.java.gui.dialogs;

import com.drin.java.biology.Pyroprint;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;

import com.drin.java.clustering.Clusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.ontology.Ontology;
import com.drin.java.ontology.OntologyParser;

import com.drin.java.metrics.DataMetric;

import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.OHClustering;

import com.drin.java.gui.MainWindow;
import com.drin.java.gui.components.AnalysisWorker;

import com.drin.java.parsers.MatrixParser;

import java.io.File;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import java.awt.Component;
import java.awt.Container;
import java.awt.ComponentOrientation;

import javax.swing.JDialog;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;

public class ClusterDialog extends JDialog {
   private final int DIALOG_HEIGHT = 550, DIALOG_WIDTH = 415;

   private String mRecentDir = "";
   private Container mPane = null, mOwner = null;

   private ButtonGroup mFirstRegionSel, mSecondRegionSel;
   private JTextField mFirstInput, mSecondInput, mOutFile, mOntology;
   private JTextField mFirstAlpha, mSecondAlpha;
   private JTextField mFirstBeta, mSecondBeta;

   public ClusterDialog(Frame owner, String title) {
      super(owner, title);

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mOwner = owner;

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      mFirstRegionSel = newRadioSelection();
      mSecondRegionSel = newRadioSelection();

      mFirstInput = new JTextField(20);
      mSecondInput = new JTextField(20);
      mOutFile = new JTextField(20);
      mOntology = new JTextField(20);

      mFirstAlpha = new JTextField("99.7", 20);
      mFirstBeta = new JTextField("99.0", 20);

      mSecondAlpha = new JTextField("99.7", 20);
      mSecondBeta = new JTextField("99.0", 20);
   }

   public static void main(String[] args) {
      ClusterDialog dialog = new ClusterDialog(null, "Cluster Dialog");

      dialog.init();
      dialog.setVisible(true);
   }

   public void init() {
      JPanel labelField = new JPanel(), labelField2 = new JPanel();

      labelField.setLayout(new FlowLayout(FlowLayout.LEADING));
      labelField.add(new JLabel("Input Dataset"));

      labelField2.setLayout(new FlowLayout(FlowLayout.LEADING));
      labelField2.add(new JLabel("Input Dataset"));

      
      mPane.add(newHeaderField(mOutFile, mOntology));

      mPane.add(labelField);
      mPane.add(new JSeparator());
      mPane.add(newFileField(mFirstRegionSel, mFirstInput, mFirstAlpha, mFirstBeta, mOutFile));

      mPane.add(labelField2);
      mPane.add(new JSeparator());
      mPane.add(newFileField(mSecondRegionSel, mSecondInput, mSecondAlpha, mSecondBeta, mOutFile));

      mPane.add(controls());

      mPane.validate();
   }

   public JPanel newHeaderField(JTextField outFileField, JTextField ontologyField) {

      JPanel outputNameField = new JPanel(), organizationField = new JPanel(),
             headerLayout = new JPanel();

      headerLayout.setLayout(new BoxLayout(headerLayout, BoxLayout.Y_AXIS));
      headerLayout.setAlignmentY(Component.CENTER_ALIGNMENT);

      outputNameField.setLayout(new FlowLayout(FlowLayout.LEADING));
      outputNameField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      outputNameField.add(new JLabel("Output file name:"));
      outputNameField.add(outFileField);
      outputNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

      organizationField.setLayout(new FlowLayout(FlowLayout.LEADING));
      organizationField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      organizationField.add(new JLabel("Experimental Organization"));
      organizationField.add(ontologyField);
      organizationField.setAlignmentX(Component.CENTER_ALIGNMENT);
      organizationField.add(newFileBrowseButton(ontologyField, null));

      headerLayout.add(outputNameField);
      headerLayout.add(organizationField);

      return headerLayout;
   }

   public JPanel newFileField(ButtonGroup regionSelection, JTextField fileNameField,
    JTextField upperThresholdField, JTextField lowerThresholdField, JTextField outputField) {
      JPanel radioSelection = new JPanel();
      JPanel fileInput = new JPanel();
      JPanel fileInputPanel = new JPanel();

      Enumeration radioButtons = regionSelection.getElements();

      radioSelection.setLayout(new BoxLayout(radioSelection, BoxLayout.Y_AXIS));

      radioSelection.add(new JLabel("ITS Region:"));
      while (radioButtons.hasMoreElements()) {
         radioSelection.add((JRadioButton) radioButtons.nextElement());
      }
      radioSelection.setAlignmentY(Component.CENTER_ALIGNMENT);

      fileInput.setLayout(new BoxLayout(fileInput, BoxLayout.Y_AXIS));

      fileInput.add(new JLabel("File:"));
      fileInput.add(fileNameField);
      fileInput.add(new JLabel("Threshold:"));
      fileInput.add(upperThresholdField);
      fileInput.add(lowerThresholdField);

      fileInput.setAlignmentY(Component.CENTER_ALIGNMENT);


      fileInputPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      fileInputPanel.add(radioSelection);
      fileInputPanel.add(fileInput);
      fileInputPanel.add(newFileBrowseButton(fileNameField, outputField));

      fileInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

      return fileInputPanel;
   }

   public ButtonGroup newRadioSelection() {
      ButtonGroup radioSelection = new ButtonGroup();

      JRadioButton ITS_16_23 = new JRadioButton("16s-23s");
      JRadioButton ITS_23_5 = new JRadioButton("23s-5s");

      radioSelection.add(ITS_16_23);
      radioSelection.add(ITS_23_5);

      return radioSelection;
   }

   public JButton newFileBrowseButton(JTextField fileName, JTextField outputFileName) {
      final JTextField tmpFileField = fileName, tmpOutputFile = outputFileName;
      JButton fileBrowse = new JButton("Browse");

      fileBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            //Obtains the file name of the input from the file chooser
            JFileChooser chooser = new JFileChooser();
            //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (recentlyAccessedDir != "") {
               File curDir = new File(recentlyAccessedDir);
               chooser.setCurrentDirectory(curDir);
            }
            
            int returnVal = chooser.showOpenDialog(chooser);
            
            if (returnVal == JFileChooser.CANCEL_OPTION) {
               System.out.println("cancelled");
            }
            else if (returnVal == JFileChooser.APPROVE_OPTION) {
               File dataFile = chooser.getSelectedFile();
               tmpFileField.setText(dataFile.getAbsolutePath());
               if (tmpOutputFile != null && tmpOutputFile.getText().equals("")) {
                  tmpOutputFile.setText(dataFile.getName().substring(0, dataFile.getName().indexOf(".csv")));
               }

               recentlyAccessedDir = chooser.getSelectedFile().getPath();
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });

      return fileBrowse;
   }

   public JPanel controls() {
      JPanel dialogControls = new JPanel();
      dialogControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
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

            /*
             * preparing arguments for clusterer
             */
            if (mOntology.getText().equals("") || mFirstAlpha.getText().equals("") ||
                mFirstBeta.getText().equals("") || mSecondAlpha.getText().equals("") ||
                mSecondBeta.getText().equals("")) {
               JOptionPane.showMessageDialog(mOwner, "Invalid input",
                                             "Invalid Input",
                                             JOptionPane.ERROR_MESSAGE);
               return;
            }

            Set<Cluster> clusterSet = new HashSet<Cluster>();
            Ontology ontology = Ontology.createOntology(mOntology.getText());


            double firstAlpha = Double.parseDouble(mFirstAlpha.getText());
            double firstBeta = Double.parseDouble(mFirstBeta.getText());

            double secondAlpha = Double.parseDouble(mSecondAlpha.getText());
            double secondBeta = Double.parseDouble(mSecondBeta.getText());
               
            JRadioButton firstRegion = null, secondRegion = null;
            Enumeration regionSelection = mFirstRegionSel.getElements();

            while (regionSelection.hasMoreElements()) {
               JRadioButton tmpRadio = (JRadioButton) regionSelection.nextElement();

               if (tmpRadio.isSelected()) {
                  firstRegion = tmpRadio;
               }
            }

            regionSelection = mSecondRegionSel.getElements();

            while (regionSelection.hasMoreElements()) {
               JRadioButton tmpRadio = (JRadioButton) regionSelection.nextElement();

               if (tmpRadio.isSelected()) {
                  secondRegion = tmpRadio;
               }
            }

            if (firstRegion != null && secondRegion != null &&
                firstRegion.getText().equals(secondRegion.getText())) {
               JOptionPane.showMessageDialog(mOwner,
                "Please select different regions for each input data",
                "Invalid Regions Selected", JOptionPane.ERROR_MESSAGE);
               return;
            }
            if (mFirstInput.getText().equals(mSecondInput.getText())) {
               int selectedOption = JOptionPane.showConfirmDialog(mOwner,
                "Same file input for both regions. Are you sure?",
                "Duplicate input confirmation", JOptionPane.YES_NO_OPTION);

               if (selectedOption == JOptionPane.NO_OPTION) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Clustering cancelled",
                   "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                  return;
               }
            }
               
            if (firstAlpha < 1 || secondAlpha < 1) {
               JOptionPane.showMessageDialog(mOwner,
                "Invalid threshold values",
                "Invalid Options", JOptionPane.ERROR_MESSAGE);
               return;
            }

            if (!mFirstInput.getText().equals("")) {
               MatrixParser parser = new MatrixParser(mFirstInput.getText());

               ITSRegion region = new ITSRegion(firstRegion.getText(),
                                  new ITSRegionAverageMetric(firstAlpha/100,
                                                             firstBeta/100));

               Map<String, Map<String, Double>> correlations = parser.parseData();

               //Only has to be done for each unique isolate id (aka once
               //per isolate dataset, not per region)
               for (String isoId : correlations.keySet()) {
                  Cluster element = new IsolateCluster(isoId, new Isolate(isoId));
                  clusterSet.add(element);
                  if (ontology != null) { ontology.addData(element); }
               }

            }
            else if (mFirstInput.getText().equals("")) {
               JOptionPane.showMessageDialog(mOwner,
                "Invalid parameters for first data input",
                "Invalid Options", JOptionPane.ERROR_MESSAGE);

               return;
            }

            if (secondRegion != null) {
               if (!mSecondInput.getText().equals("")) {
                  ITSRegion region = new ITSRegion(secondRegion.getText(),
                                     new ITSRegionAverageMetric(secondAlpha/100,
                                                                secondBeta/100));
               }
               else if (mSecondInput.getText().equals("")) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid parameters for second data input",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);

                  return;
               }
            }

            if (ontology != null) {
               clusterer = new OHClustering(ontology, clusterMetric);
            }
            else { clusterer = new OHClustering(clusterSet, clusterMetric); }

            if (clusterer != null) {
               AnalysisWorker worker = new AnalysisWorker(analyzer, clusterer,
                MainWindow.getMainFrame().getOutputCanvas());

               worker.setOutputFile(mOutFile.getText());
               worker.execute();
            }

            dispose();
         }
      });

      JButton cancelButton = new JButton("Cancel");

      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dispose();
            return;
         }
      });

      dialogControls.add(okayButton);
      dialogControls.add(cancelButton);
      dialogControls.setAlignmentX(Component.CENTER_ALIGNMENT);

      return dialogControls;
   }
}
