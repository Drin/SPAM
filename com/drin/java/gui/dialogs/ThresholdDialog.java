package com.drin.java.gui.dialogs;

import com.drin.java.gui.MainWindow;

import com.drin.javaclustering.HClustering;
import com.drin.javadataWriter.IsolateOutputWriter;

import com.drin.javadendogram.DendogramParser;
import com.drin.javadendogram.DendogramTree;

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

public class ThresholdDialog extends JDialog {
   private final int DIALOG_HEIGHT = 155, DIALOG_WIDTH = 400;
   private Container mPane = null, mOwner = null;

   private JTextField dendogramFileField, cutoffThresholdField;

   private String recentlyAccessedDir = "";

   public ThresholdDialog() {
      super();

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dendogramFileField = new JTextField(15);
      cutoffThresholdField = new JTextField("99.7", 15);
   }

   public ThresholdDialog(Frame owner, String title) {
      super(owner, title);

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);
      this.setLocationRelativeTo(null);

      mOwner = owner;

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dendogramFileField = new JTextField(15);
      cutoffThresholdField = new JTextField("99.7", 15);
   }

   public static void main(String[] args) {
      ThresholdDialog dialog = new ThresholdDialog();

      dialog.init();
      dialog.setVisible(true);
   }

   public void init() {
      mPane.add(dendogramFilePanel(dendogramFileField));

      mPane.add(thresholdFilePanel(cutoffThresholdField));

      mPane.add(controls());

      mPane.validate();
   }

   public JPanel dendogramFilePanel(JTextField fileField) {
      JPanel fileChooserPanel = new JPanel();

      fileChooserPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      fileChooserPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      fileChooserPanel.add(new JLabel("Dendogram File:"));
      fileChooserPanel.add(fileField);
      fileChooserPanel.add(newFileBrowseButton(fileField));

      return fileChooserPanel;
   }

   public JPanel thresholdFilePanel(JTextField thresholdField) {
      JPanel thresholdPanel = new JPanel();

      thresholdPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      thresholdPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      thresholdPanel.add(new JLabel("Cutoff Threshold:"));
      thresholdPanel.add(thresholdField);

      return thresholdPanel;
   }

   public JButton newFileBrowseButton(JTextField fileName) {
      final JTextField tmpFileField = fileName;
      JButton fileBrowse = new JButton("Browse");

      fileBrowse.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();

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
            String fileName = dendogramFileField.getText();
            String filePrefix = fileName.substring(0, fileName.indexOf(".xml"));

            System.out.println("constructing dendogram parser...");
            DendogramParser parser = new DendogramParser(dendogramFileField.getText());

            System.out.println("parsing dendogram..");
            DendogramTree tree = parser.parseDendogram(Double.valueOf(cutoffThresholdField.getText()));

            System.out.println("finished parsing dendogram...");
            IsolateOutputWriter.outputTemporalCharts(tree, filePrefix, cutoffThresholdField.getText());
            IsolateOutputWriter.outputClusters(tree, filePrefix);

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
      dialogControls.setAlignmentY(Component.BOTTOM_ALIGNMENT);

      return dialogControls;
   }
}
