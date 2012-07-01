package com.drin.java.gui.dialogs;

import com.drin.java.parsers.MatrixParser;

import com.drin.java.types.Cluster;
import com.drin.java.types.Isolate;
import com.drin.java.types.IsolateCluster;
import com.drin.java.metrics.ClusterComparator;
import com.drin.java.metrics.IsolateComparator;
import com.drin.java.metrics.IsolateMetric;
import com.drin.java.metrics.IsolateMatrixMetric;
import com.drin.java.metrics.IsolateMaxMetric;
import com.drin.java.metrics.IsolateMinMetric;
import com.drin.java.metrics.IsolateAverageMetric;

import com.drin.java.analysis.clustering.HierarchicalClusterer;
import com.drin.java.analysis.clustering.AgglomerativeClusterer;
import com.drin.java.analysis.clustering.ClusterAnalyzer;

import com.drin.java.gui.MainWindow;
import com.drin.java.gui.components.AnalysisWorker;
import com.drin.java.gui.listeners.DataQueryButtonListener;
import com.drin.java.database.CPLOPConnection;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;

import javax.swing.JFrame;
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
public class ClusterParameterDialog extends JDialog {
   /*
    * CONSTANTS
    */
   private final static String DATABASE_ISOLATES = "Isolates",
                               DATABASE_PYROS = "Pyroprints",
                               DATABASE_EXPERIMENTS = "Experiments",
                               MATRIX_TYPE = "Matrix",
                               CLUSTERER_PACKAGE = "com.drin.java.analysis.clustering";
   private final static int TYPE_INDEX = 0, VALUE_INDEX = 1;
   private final static boolean DEBUG = false;

   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 500;
   private final String[] CLUSTER_METHODS = new String[] {"Agglomerative",
                                                          "PartitionedHierarchical",
                                                          "KMeans",
                                                          "DBSCAN"};
   private final String[] DATA_SOURCES = new String[] {DATABASE_ISOLATES,
                                                       DATABASE_PYROS,
                                                       DATABASE_EXPERIMENTS,
                                                       MATRIX_TYPE};
   /*
    * GUI Components
    */
   private Container mPane = null, mOwner = null;
   private JDialog mDialog = null;
   private JComboBox<String> mClusterMethod, mDataSource;
   private JTextField mDataSet;
   private String mRecentDir;
   private CPLOPConnection mConn;

   public ClusterParameterDialog(Frame owner, String title) {
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

      mRecentDir = null;
      mDataSet = new JTextField(20);

      mClusterMethod = new JComboBox<String>(CLUSTER_METHODS);
      mDataSource = new JComboBox<String>(DATA_SOURCES);

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

   public ClusterParameterDialog() {
      this(null, "");
   }

   public static void main(String[] args) {
      ClusterParameterDialog dialog = new ClusterParameterDialog();

      dialog.init();
      dialog.setVisible(true);
   }

   public void init() {
      JLabel dataSetLabel = new JLabel("Select Data Set:");
      JPanel dataSetField = prepareDataSetField(mDataSource, mDataSet);

      JLabel clusterMethodLabel = new JLabel("Select Clustering Method:");
      JPanel clusterMethodField = prepareClusterMethodSelect(mClusterMethod);

      mPane.add(dataSetLabel);
      mPane.add(dataSetField);

      mPane.add(clusterMethodLabel);
      mPane.add(clusterMethodField);

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

   private JPanel prepareClusterMethodSelect(JComboBox<String> clusterMethodOptions) {
      JPanel clusterMethodField = new JPanel();

      clusterMethodField.setLayout(new FlowLayout(FlowLayout.LEADING));

      clusterMethodField.add(clusterMethodOptions);

      return clusterMethodField;
   }

   private JButton prepareDataQueryButton(final JTextField dataSetField,
    final JComboBox<String> dataTypeOptions) {
      final JButton dataQueryButton = new JButton("Choose " + dataTypeOptions.getSelectedItem());

      dataTypeOptions.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            String selectedItem = String.valueOf(e.getItem());

            dataQueryButton.setText("Choose " + selectedItem);

            for (ActionListener listener : dataQueryButton.getActionListeners()) {
               dataQueryButton.removeActionListener(listener);
            }

            if (selectedItem.equals("Matrix")) {
               dataQueryButton.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                     JFileChooser fileChooser = null; 

                     if (mRecentDir == null) { fileChooser = new JFileChooser(); }
                     else { fileChooser = new JFileChooser(mRecentDir); }
               
                     int returnVal = fileChooser.showOpenDialog(mPane);
                     if (returnVal == JFileChooser.APPROVE_OPTION) {
                        mRecentDir = fileChooser.getSelectedFile().getAbsolutePath();
                        dataSetField.setText(mRecentDir);
                     }
                  }
               });
            }

            else {
               dataQueryButton.addActionListener(
                new DataQueryButtonListener(dataSetField, dataTypeOptions, mConn));
            }
         }
      });



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
            int clusterNum = 0;

            if (!mDataSet.getText().equals("")) {
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
