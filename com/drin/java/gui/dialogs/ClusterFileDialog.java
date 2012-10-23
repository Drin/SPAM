package com.drin.java.gui.dialogs;

import com.drin.java.parsers.MatrixParser;

import com.drin.java.types.Cluster;
import com.drin.java.types.Isolate;
import com.drin.java.types.ITSRegion;
import com.drin.java.types.Pyroprint;

import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;

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

import java.io.File;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import java.lang.reflect.Constructor;

@SuppressWarnings("serial")
public class ClusterFileDialog extends JDialog {
   /*
    * CONSTANTS
    */
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 500;

   private final String[] CLUSTER_METHODS = new String[] {"Hierarchical",
                                                          "OHClustering"};

   /*
    * GUI Components
    */
   private Container mPane = null;
   private JTextField mDataSet;
   private String mRecentDir;

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

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      mRecentDir = null;
      mDataSet = new JTextField(20);
      mMethod = new JComboBox<String>(CLUSTER_METHODS);
   }

   public void init() {
      JLabel dataLabel = new JLabel("Select Input Data:");
      JPanel dataField = prepareDataInput(mDataSet);

      JLabel methodLabel = new JLabel("Select Clustering Method:");
      JPanel methodField = prepareMethodSelect(mMethod);

      mPane.add(methodLabel);
      mPane.add(methodField);

      mPane.add(initControls());

      mPane.validate();
   }

   private JPanel prepareDataInput(JTextField dataInput) {
      JPanel dataField = new JPanel();

      dataField.setLayout(new FlowLayout(FlowLayout.LEADING));

      dataField.add(dataInput);

      return dataField;
   }

   private JPanel prepareMethodSelect(JComboBox<String> methodOptions) {
      JPanel methodField = new JPanel();

      methodField.setLayout(new FlowLayout(FlowLayout.LEADING));

      methodField.add(methodOptions);

      return methodField;
   }

   public JPanel initControls() {
      JPanel dialogControls = new JPanel();

      dialogControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      dialogControls.add(createOkayButton());
      dialogControls.add(createCancelButton());

      dialogControls.setAlignmentX(Component.CENTER_ALIGNMENT);

      return dialogControls;
   }

   private void takeAction() {
      /*
            HierarchicalClusterer clusterer = null;
            String dataSourceType = String.valueOf(mDataSource.getSelectedItem());

            if (dataSourceType.equals(MATRIX_TYPE)) {
               //Do for each region
               MatrixParser parser = new MatrixParser(mDataSet.getText());
               Map<String, Map<String, Double>> correlations = parser.parseData();
               Set<Cluster> clusterSet = new HashSet<Cluster>();

               //Since clusterSet is just a set of isolates, and isolates
               //are only necessary for their isolate IDs this can be done
               //for a single region
               for (String isoId : correlations.keySet()) {
                  clusterSet.add(new IsolateCluster(isoId, new Isolate(isoId)));
               }

               if (DEBUG) {
                  System.out.printf("correlation matrix: \n");

                  for (String iso_A : correlations.keySet()) {
                     Map<String, Double> corrMap = correlations.get(iso_A);
                     for (String iso_B : corrMap.keySet()) {
                        System.out.printf("%s : %s -> %.04f\n",
                         iso_A, iso_B, corrMap.get(iso_B).doubleValue());
                     }
                  }
               }

               ClusterComparator clusterComp = new ClusterComparator(); 
               IsolateMatrixMetric isoMetric = new IsolateMatrixMetric(correlations);

               clusterer = new AgglomerativeClusterer(clusterSet, isoMetric, clusterComp);
               ClusterAnalyzer analyzer = new ClusterAnalyzer(isoMetric);

               if (clusterer != null) {
                  AnalysisWorker worker = new AnalysisWorker(analyzer, clusterer,
                   MainWindow.getMainFrame().getOutputCanvas());

                  worker.execute();
               }
            }
            */
      System.out.println("derp de derp");
   }

   private JButton createOkayButton() {
      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            int clusterNum = 0;

            if (mDataSet.getText().equals("")) {
               return;
            }

            takeAction();

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
