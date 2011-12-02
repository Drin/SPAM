package spam.gui.listeners.rawHandlers;

import spam.PyrogramComparer;

import spam.dataParser.SequenceParser;
import spam.dataParser.Polyparse;

import spam.gui.MainWindow;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ComparePyrogramsListener implements java.awt.event.ActionListener {
   private MainWindow mainFrame;
   private JTextArea displayedMetrics; 
   private Polyparse pyrosequencer;
   private SpringLayout dialogLayout;
   private JDialog sequenceDialog;
   //to be more dynamic include: seqIters and startPos;
   private JTextField firstText, dispSeq, forPrimer, revPrimerComp;
   private JButton okayButton, cancelButton, firstBrowse, sequenceBrowse;
   private JComboBox distanceOptions, comparisonOptions;
   private String recentlyAccessedDir = "";
   
   public void setOwner(MainWindow parentFrame) {
      this.mainFrame = parentFrame;
   }
   
   public void actionPerformed(java.awt.event.ActionEvent e) {
      //Need to store everything somehow
      //Need an association of some read sequence to a comparison matrix
      //Need each pyrogram (file/string format)
      //Need one comparison matrix per sequence
      //Once matrix is fully constructed pyrograms are no longer necessary.
      sequenceDialog = new JDialog(mainFrame, "Compute Pyrograms");
      dialogLayout = new SpringLayout();
      sequenceDialog.getContentPane().setLayout(dialogLayout);
      
      sequenceDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      sequenceDialog.setMinimumSize(new Dimension(475,350));
      sequenceDialog.setResizable(false);
      
      
      //Files (FASTA format) to compare
      JLabel firstLabel = new JLabel("Data (FASTA) Directory:");
      firstText = new JTextField();
      
      //file browse button
      firstBrowse = new JButton("Browse");
      firstBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            //Obtains the file name of the input from the file chooser
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (recentlyAccessedDir != "") {
               File curDir = new File(recentlyAccessedDir);
               chooser.setCurrentDirectory(curDir);
            }
            
            int returnVal = chooser.showOpenDialog(chooser);
            
            if (returnVal == JFileChooser.CANCEL_OPTION) {
               System.out.println("cancelled");
            }
            else if (returnVal == JFileChooser.APPROVE_OPTION) {
               firstText.setText(chooser.getSelectedFile().getAbsolutePath());
               recentlyAccessedDir = chooser.getSelectedFile().getPath();
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });
      
      //Dispensation sequences to use in analysis
      JLabel dispSeqLabel = new JLabel("Dispensation Sequence(s):");
      dispSeq = new JTextField("ATCG");
      
      //button to open a text file containing premade dispensation sequences
      sequenceBrowse = new JButton("Browse");
      sequenceBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            //Obtains the file name of the input from the file chooser
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
               String tmpDispSeq = Polyparse.parseSequence(
                chooser.getSelectedFile().getAbsoluteFile());
               dispSeq.setText(
                new SequenceParser(tmpDispSeq).getExpandedSequence());
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });
      
      JLabel forwardLabel = new JLabel("Forward Primer:");
      forPrimer = new JTextField();
      
      JLabel reverseLabel = new JLabel("Reverse Primer:");
      revPrimerComp = new JTextField();
      
      //3 options for comparisons
      //basic, moderate, advanced
      //d1, d2, d3
      JLabel distanceLabel = new JLabel("Choose Comparison Type:");
      distanceOptions = new JComboBox(new String[] {
            "Euclidean Distance", "Pearson Correlation", "Equality", "Direct Comparison"
      });
      
      JLabel comparisonLabel = new JLabel("Choose Comparison Mode:");
      comparisonOptions = new JComboBox(new String[] {
            "Forward", "Forward and Reverse"
      });
      
      //cancel and okay buttons at bottom
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            firstText.setText("");
            sequenceDialog.dispose();
            return;
         }
      });
      
      okayButton = new JButton("Okay");
      okayButton.addActionListener(new ActionListener(){
         @SuppressWarnings("unchecked")
         public void actionPerformed(ActionEvent e) {
            //basic validation
            //needs better validation
            if (firstText.getText().equals("") || dispSeq.getText().equals("")) {
               System.out.println("File and read sequence may not be empty");
               return;
            }
            
            System.out.println("making sequence finder...");
            
            //make sure that this correctly expands each read sequence
            //System.out.println("splitting disp seq yields " +
            //dispSeq.getText().split(","));
            String[] dispOrderList = dispSeq.getText().split(",");
            for (int strNdx = 0; strNdx < dispOrderList.length; strNdx++) {
               //System.out.println("parsing dispensation sequence.");
               dispOrderList[strNdx] =
                new SequenceParser(dispOrderList[strNdx]).getExpandedSequence();
            }
            
            /*
             * debug prints of dispensation sequences
            System.out.println("dispOrderList:");
            for (String s : dispOrderList)
               System.out.println(s);
            */
            
            //firstText is the directory of data files
            PyrogramComparer pyrogramComparer =
             new PyrogramComparer(firstText.getText(),
             dispOrderList, (String) distanceOptions.getSelectedItem());
            
            pyrogramComparer.setForwardPrimer(forPrimer.getText());
            pyrogramComparer.setReversePrimer(revPrimerComp.getText());

            String[] comparisonResults = null;
            if (comparisonOptions.getSelectedItem().equals("Forward")) {
               //false means it is FASTA files that are being parsed
               comparisonResults = pyrogramComparer.compareForwardSequences(false);
            }
            else {
               comparisonResults = pyrogramComparer.compareAllSequences();
            }
            
            //may happen if primers are invalid
            if (comparisonResults == null) {
               JOptionPane.showMessageDialog(mainFrame,
                "Primers overlap or were not found",
                "Invalid primers", JOptionPane.ERROR_MESSAGE);
            }

            displayedMetrics = new JTextArea();
            displayedMetrics.setTabSize(28);

            //Writer forwardWriter = null, reverseWriter = null;
            Writer writer = null;

            try {
               /*
                * File forwardFile = new File("pyroData_Forward.csv");
                * File reverseFile = new File("pyroData_Reverse.csv");
                */
               File outputFile = new File("pyroData.csv");
               writer = new BufferedWriter(new FileWriter(outputFile));
            }
            catch(Exception e1) {
               System.out.println("Could not write to data file.");
               return;
            }
            
            for (int metricNdx = 0; metricNdx < comparisonResults.length;
             metricNdx++) {
               try {
                  writer.write((comparisonResults[metricNdx]));
                  displayedMetrics.append("   " +
                   (comparisonResults[metricNdx].replace(",", "\t")));
                  /*
                  forwardWriter.write((resultMetrics[0]));
                  reverseWriter.write((resultMetrics[1]));
                  */
               }
               catch (Exception e1) {
                  System.out.println("Could not write to data file.");
                  return;
               }
            }
            
            try {
               if (writer != null) writer.close();
            }
            catch (Exception e1) {
               System.out.println("Could not close data file writer.");
            }
            System.out.println("");
            
            displayedMetrics.setVisible(true);
            JScrollPane metricPane = new JScrollPane(displayedMetrics);
            metricPane.setVisible(true);
            mainFrame.setContentPane(metricPane);
            mainFrame.validate();

            sequenceDialog.dispose();
         }
      });
      
      /*
       * Add all of the initialized components above to the dialog window
       */

      sequenceDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      sequenceDialog.add(firstLabel);
      sequenceDialog.add(firstText);
      sequenceDialog.add(firstBrowse);
      sequenceDialog.add(dispSeqLabel);
      sequenceDialog.add(dispSeq);
      sequenceDialog.add(sequenceBrowse);
      sequenceDialog.add(forwardLabel);
      sequenceDialog.add(forPrimer);
      sequenceDialog.add(reverseLabel);
      sequenceDialog.add(revPrimerComp);
      sequenceDialog.add(distanceLabel);
      sequenceDialog.add(distanceOptions);
      sequenceDialog.add(comparisonLabel);
      sequenceDialog.add(comparisonOptions);
      sequenceDialog.add(cancelButton);
      sequenceDialog.add(okayButton);
      
      dialogLayout.putConstraint(SpringLayout.WEST, firstLabel, 25,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, firstLabel, -10,
            SpringLayout.WEST, firstBrowse);
      dialogLayout.putConstraint(SpringLayout.SOUTH, firstLabel, -5,
            SpringLayout.NORTH, firstText);
      
      //position of first textField relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, firstText, 25,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, firstText, -10,
            SpringLayout.WEST, firstBrowse);
      dialogLayout.putConstraint(SpringLayout.NORTH, firstText, 25,
            SpringLayout.NORTH, sequenceDialog.getContentPane());
      
      //position of first browse Button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, firstBrowse, -25,
            SpringLayout.EAST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, firstBrowse, 23,
            SpringLayout.NORTH, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.WEST, firstBrowse, -95,
            SpringLayout.EAST, firstBrowse);
      
      //position of label for dispensation sequence field
      //relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, dispSeqLabel, 25,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, dispSeqLabel, 25,
            SpringLayout.SOUTH, firstText);
      
      //position dispSeq relative to pane and second textField
      dialogLayout.putConstraint(SpringLayout.WEST, dispSeq, 15,
            SpringLayout.EAST, dispSeqLabel);
      dialogLayout.putConstraint(SpringLayout.EAST, dispSeq, 120,
            SpringLayout.WEST, dispSeq);
      dialogLayout.putConstraint(SpringLayout.NORTH, dispSeq, 25,
            SpringLayout.SOUTH, firstText);
      
      dialogLayout.putConstraint(SpringLayout.EAST, sequenceBrowse, -25,
            SpringLayout.EAST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, sequenceBrowse, 25,
            SpringLayout.SOUTH, firstText);
      dialogLayout.putConstraint(SpringLayout.WEST, sequenceBrowse, 10,
            SpringLayout.EAST, dispSeq);
      
      dialogLayout.putConstraint(SpringLayout.WEST, forwardLabel, 25,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, forwardLabel, 25,
            SpringLayout.SOUTH, dispSeq);
      
      dialogLayout.putConstraint(SpringLayout.WEST, forPrimer, 25,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, forPrimer, 5,
            SpringLayout.SOUTH, forwardLabel);
      dialogLayout.putConstraint(SpringLayout.EAST, forPrimer, 150,
            SpringLayout.WEST, forPrimer);
      
      dialogLayout.putConstraint(SpringLayout.WEST, reverseLabel, 45,
            SpringLayout.EAST, forPrimer);
      dialogLayout.putConstraint(SpringLayout.NORTH, reverseLabel, 25,
            SpringLayout.SOUTH, dispSeq);
      
      dialogLayout.putConstraint(SpringLayout.WEST, revPrimerComp, 45,
            SpringLayout.EAST, forPrimer);
      dialogLayout.putConstraint(SpringLayout.NORTH, revPrimerComp, 5,
            SpringLayout.SOUTH, reverseLabel);
      dialogLayout.putConstraint(SpringLayout.EAST, revPrimerComp, 150,
            SpringLayout.WEST, revPrimerComp);
      
      dialogLayout.putConstraint(SpringLayout.WEST, distanceLabel, 25,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, distanceLabel, 25,
            SpringLayout.SOUTH, forPrimer);
      
      dialogLayout.putConstraint(SpringLayout.WEST, distanceOptions, 15,
            SpringLayout.EAST, distanceLabel);
      dialogLayout.putConstraint(SpringLayout.NORTH, distanceOptions, 23,
            SpringLayout.SOUTH, forPrimer);
      
      dialogLayout.putConstraint(SpringLayout.WEST, comparisonLabel, 25,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, comparisonLabel, 25,
            SpringLayout.SOUTH, distanceLabel);
      
      dialogLayout.putConstraint(SpringLayout.WEST, comparisonOptions, 15,
            SpringLayout.EAST, comparisonLabel);
      dialogLayout.putConstraint(SpringLayout.NORTH, comparisonOptions, 15,
            SpringLayout.SOUTH, distanceOptions);
      
      //Position Cancel button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, cancelButton, -50,
            SpringLayout.EAST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.SOUTH, cancelButton, -25,
            SpringLayout.SOUTH, sequenceDialog.getContentPane());
      
      //Position okay button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.WEST, okayButton, 50,
            SpringLayout.WEST, sequenceDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.SOUTH, okayButton, -25,
            SpringLayout.SOUTH, sequenceDialog.getContentPane());
      
      //show dialog
      sequenceDialog.setVisible(true);
   }
}
