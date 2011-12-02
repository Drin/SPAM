package spam.gui.dialogs;

import spam.clustering.HClustering;
import spam.gui.MainWindow;

import java.io.File;

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

public class InputDialog extends JDialog {
   private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 415;
   private String recentlyAccessedDir = "", mArgDelim = null;
   private Container mPane = null, mOwner = null;
   private Map<File, String> dataFileMap;

   private ButtonGroup firstDataRegion, secondDataRegion;
   private JComboBox clusterRestrictions;
   private JTextField firstDataFile, secondDataFile, outputDataFile;
   private JTextField firstDataUpperThreshold, secondDataUpperThreshold;
   private JTextField firstDataLowerThreshold, secondDataLowerThreshold;

   public InputDialog() {
      super();
      mArgDelim = HClustering.getArgSeparator();

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dataFileMap = new HashMap<File, String>();
      firstDataRegion = newRadioSelection();
      secondDataRegion = newRadioSelection();

      firstDataFile = new JTextField(20);
      secondDataFile = new JTextField(20);
      outputDataFile = new JTextField(20);

      firstDataUpperThreshold = new JTextField("99.7", 20);
      firstDataLowerThreshold = new JTextField("95.0", 20);

      secondDataUpperThreshold = new JTextField("99.7", 20);
      secondDataLowerThreshold = new JTextField("95.0", 20);

      clusterRestrictions = new JComboBox(new String[] {"structure", "similarity"});
   }

   public InputDialog(Frame owner, String title) {
      super(owner, title);
      mArgDelim = HClustering.getArgSeparator();

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mOwner = owner;

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dataFileMap = new HashMap<File, String>();
      firstDataRegion = newRadioSelection();
      secondDataRegion = newRadioSelection();

      firstDataFile = new JTextField(20);
      secondDataFile = new JTextField(20);
      outputDataFile = new JTextField(20);

      firstDataUpperThreshold = new JTextField("99.7", 20);
      firstDataLowerThreshold = new JTextField("95.0", 20);

      secondDataUpperThreshold = new JTextField("99.7", 20);
      secondDataLowerThreshold = new JTextField("95.0", 20);

      clusterRestrictions = new JComboBox(new String[] {"structure", "similarity"});
   }

   public static void main(String[] args) {
      InputDialog dialog = new InputDialog();

      dialog.init();
      dialog.setVisible(true);
   }

   public void init() {
      JPanel labelField = new JPanel(), labelField2 = new JPanel();

      labelField.setLayout(new FlowLayout(FlowLayout.LEADING));
      labelField.add(new JLabel("Input Dataset"));

      labelField2.setLayout(new FlowLayout(FlowLayout.LEADING));
      labelField2.add(new JLabel("Input Dataset"));

      //mPane.add(newGlobalThreshold(globalThreshold));
      
      mPane.add(newHeaderField(outputDataFile, clusterRestrictions));
      //mPane.add(newOutputNameField(outputDataFile));

      mPane.add(labelField);
      mPane.add(new JSeparator());
      mPane.add(newFileField(firstDataRegion, firstDataFile, firstDataUpperThreshold, firstDataLowerThreshold, outputDataFile));

      mPane.add(labelField2);
      mPane.add(new JSeparator());
      mPane.add(newFileField(secondDataRegion, secondDataFile, secondDataUpperThreshold, secondDataLowerThreshold, outputDataFile));

      mPane.add(controls());

      mPane.validate();
   }

   /*
   public JPanel newGlobalThreshold(JTextField thresholdText) {
      JPanel thresholdField = new JPanel();

      //thresholdField.setLayout(new BoxLayout(thresholdField, BoxLayout.X_AXIS));
      thresholdField.setLayout(new FlowLayout(FlowLayout.LEADING));
      thresholdField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      thresholdField.add(new JLabel("Clustering Threshold:"));
      thresholdField.add(thresholdText);
      thresholdField.setAlignmentX(Component.CENTER_ALIGNMENT);

      return thresholdField;
   }
   */

   public JPanel newHeaderField(JTextField outputDataFileField, JComboBox restrictionType) {
      JPanel outputNameField = new JPanel(), preferenceField = new JPanel(), headerLayout = new JPanel();

      headerLayout.setLayout(new BoxLayout(headerLayout, BoxLayout.Y_AXIS));
      headerLayout.setAlignmentY(Component.CENTER_ALIGNMENT);

      outputNameField.setLayout(new FlowLayout(FlowLayout.LEADING));
      outputNameField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      outputNameField.add(new JLabel("Output file name:"));
      outputNameField.add(outputDataFileField);
      outputNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

      preferenceField.setLayout(new FlowLayout(FlowLayout.LEADING));
      preferenceField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      preferenceField.add(new JLabel("Cluster distance preference:"));
      preferenceField.add(restrictionType);
      preferenceField.setAlignmentX(Component.CENTER_ALIGNMENT);

      headerLayout.add(outputNameField);
      headerLayout.add(preferenceField);

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
               if (tmpOutputFile.getText().equals("")) {
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
            //basic validation

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
            ArrayList<String> args = new ArrayList<String>();

            try {
               //HClustering.setDistanceThreshold(Double.parseDouble(globalThreshold.getText()));
               HClustering.setOutputFileName(outputDataFile.getText());
               HClustering.setClusterPreference((String) clusterRestrictions.getSelectedItem());
               double firstUpperThreshold = Double.parseDouble(firstDataUpperThreshold.getText());
               double firstLowerThreshold = Double.parseDouble(firstDataLowerThreshold.getText());

               double secondUpperThreshold = Double.parseDouble(secondDataUpperThreshold.getText());
               double secondLowerThreshold = Double.parseDouble(secondDataLowerThreshold.getText());
               
               JRadioButton firstRegion = null, secondRegion = null;
               Enumeration regionSelection = firstDataRegion.getElements();

               while (regionSelection.hasMoreElements()) {
                  JRadioButton tmpRadio = (JRadioButton) regionSelection.nextElement();

                  if (tmpRadio.isSelected()) {
                     firstRegion = tmpRadio;
                  }
               }

               regionSelection = secondDataRegion.getElements();

               while (regionSelection.hasMoreElements()) {
                  JRadioButton tmpRadio = (JRadioButton) regionSelection.nextElement();

                  if (tmpRadio.isSelected()) {
                     secondRegion = tmpRadio;
                  }
               }

               if (firstRegion != null && secondRegion != null &&
                firstRegion.getText().equals(secondRegion.getText())) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid regions selected: Please select different regions for each input data",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);
                  return;
               }
               if (firstDataFile.getText().equals(secondDataFile.getText())) {
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
               
               if (firstUpperThreshold < 1 || secondUpperThreshold < 1) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid threshold values",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);
                  return;
               }

               if (!firstDataFile.getText().equals("") && firstRegion != null) {
                  args.add(String.format(firstDataFile.getText() + mArgDelim + firstRegion.getText() +
                   mArgDelim + "%.03f" + mArgDelim + "%.03f", firstLowerThreshold, firstUpperThreshold));
               }
               else if (!firstDataFile.getText().equals("") || firstRegion != null) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid parameters for first data input",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);

                  return;
               }

               if (secondRegion != null) {
                  if (!secondDataFile.getText().equals("") && secondRegion != null) {
                     args.add(String.format(secondDataFile.getText() + mArgDelim + secondRegion.getText() +
                      mArgDelim + "%.03f" + mArgDelim + "%.03f", secondLowerThreshold, secondUpperThreshold));
                  }
                  else if (!secondDataFile.getText().equals("") || secondRegion != null) {
                     JOptionPane.showMessageDialog(mOwner,
                      "Invalid parameters for second data input",
                      "Invalid Options", JOptionPane.ERROR_MESSAGE);

                     return;
                  }
               }
            }
            catch (NullPointerException emptyValErr) {
               //System.err.println("No file was selected");
               JOptionPane.showMessageDialog(mOwner,
                "No file was selected",
                "Invalid File", JOptionPane.ERROR_MESSAGE);
            }

            //Should write files somewhere
            String[] argsArr = new String[args.size()];
            args.toArray(argsArr);

            System.out.println("calling HClustering...");
            
            HClustering clusterer = new HClustering(args.size());

            if (clusterer.cluster(argsArr)) {
               JOptionPane.showMessageDialog(mOwner,
                "Clustering complete",
                "Clustering completed", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
               JOptionPane.showMessageDialog(mOwner,
                "Error occurred while clustering Data",
                "Clustering Error", JOptionPane.ERROR_MESSAGE);

               return;
            }

            System.out.println("HClustering completed");
            ((MainWindow) mOwner).saveState(clusterer.getClusterDendograms());
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
