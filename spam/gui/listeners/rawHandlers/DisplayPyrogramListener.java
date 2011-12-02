package spam.gui.listeners.rawHandlers;

import spam.gui.MainWindow;
import spam.gui.Pyrograph;

import spam.dataTypes.Pyrogram;

import spam.dataParser.Polyparse;
import spam.dataParser.SequenceParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DisplayPyrogramListener implements java.awt.event.ActionListener {
   private MainWindow mainFrame;
   private SpringLayout dialogLayout;
   private JDialog openDialog;
   private JTextField firstText, secondText, nucleOrder, numDisps, startPos;
   private JCheckBox enableSecond;
   private JButton okayButton, cancelButton, firstBrowse, secondBrowse, sequenceBrowse;
   private String recentlyAccessedDir = "";
   
   public void setOwner(MainWindow parentFrame) {
     this.mainFrame = parentFrame;
   }
   
   public void actionPerformed(java.awt.event.ActionEvent e) {
      /*
        Setup Dialog Window
      */
      dialogLayout = new SpringLayout();
      openDialog = new JDialog(mainFrame, "Choose Raw Data");
      openDialog.getContentPane().setLayout(dialogLayout);
      
      openDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      openDialog.setMinimumSize(new Dimension(485,315));
      openDialog.setResizable(false);      
      
      JLabel firstLabel = new JLabel("File 1:");
      firstText = new JTextField();
      
      JLabel secondLabel = new JLabel("File 2:");
      secondText = new JTextField();
      
      JLabel nucleotideLabel = new JLabel("Dispensation Order:");
      nucleOrder = new JTextField("ATCG");
      nucleOrder.setColumns(8);
      
      JLabel startLabel = new JLabel("Start Position:");
      startPos = new JTextField("1");
      nucleOrder.setColumns(8);
      
      JLabel lengthLabel = new JLabel("Number of Dispensations:");
      numDisps = new JTextField("104");
      nucleOrder.setColumns(8);
      
      firstBrowse = new JButton("Browse Files");
      firstBrowse.addActionListener(new ActionListener() {
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
               firstText.setText(chooser.getSelectedFile().getAbsolutePath());
               recentlyAccessedDir = chooser.getSelectedFile().getPath();
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });
      
      secondBrowse = new JButton("Browse Files");
      secondBrowse.addActionListener(new ActionListener() {
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
               secondText.setText(chooser.getSelectedFile().getAbsolutePath());
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });
      
      //sequenceBrowse is the browse button for choosing a file containing
      //a dispensation order
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
               //file containing dispensation sequence
               File seqFile = chooser.getSelectedFile().getAbsoluteFile();
               String nucleSeq = Polyparse.parseSequence(seqFile);
               nucleOrder.setText(nucleSeq);
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });
      
      enableSecond = new JCheckBox("Disable", true);
      secondText.setEnabled(!enableSecond.isSelected());
      secondBrowse.setEnabled(!enableSecond.isSelected());

      enableSecond.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            secondText.setEnabled(!enableSecond.isSelected());
            secondBrowse.setEnabled(!enableSecond.isSelected());
         }
      });

      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            firstText.setText("");
            secondText.setText("");
            openDialog.dispose();
            return;
         }
      });
      
      okayButton = new JButton("Okay");
      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            if (!(nucleOrder.getText().contains("A") &&
               nucleOrder.getText().contains("T") &&
               nucleOrder.getText().contains("C") &&
               nucleOrder.getText().contains("G"))) {
                  JOptionPane.showMessageDialog(mainFrame,
                   "Nucleotide Order Must Contain Each Nucleotide");
                  return;
               }
            SequenceParser seqParser = new SequenceParser(nucleOrder.getText());
            Polyparse pyrosequencer = new Polyparse(seqParser.getExpandedSequence(),
             Integer.parseInt(startPos.getText()));
            ArrayList<String> arr = null, arr2 = null;
            
            //Parses the input DNA into the array list of strings
            try {
               if (!firstText.equals("")) {
                  arr = pyrosequencer.parse(firstText.getText());
               }
               else if (firstText.equals("")) {
                  JOptionPane.showMessageDialog(mainFrame,
                   "You must choose a file");
                  return;
               }
               
               if (secondText.isEnabled() && !secondText.equals("")) {
                  arr2 = pyrosequencer.parse(secondText.getText());
               }
               else if (secondText.isEnabled() && secondText.equals("")) {
                  JOptionPane.showMessageDialog(mainFrame,
                   "Second File Not Chosen");
                  return;
               }
               
            }
            catch (IOException e1) {
               System.err.println("Unable to parse " + firstText.getText() +
                ". " + e1);
               return;
            }

            Pyrogram firstPyrogram = null, secondPyrogram = null;
            Pyrograph graphOne = null, graphTwo = null;
            JPanel graphsPanel = new JPanel();
            graphsPanel.setLayout(new BorderLayout());

            //Use the parsed DNA to create the Histogram
            if (arr != null) {
               if (!numDisps.getText().equals("") && Integer.parseInt(numDisps.getText()) > 0) {
                  firstPyrogram = new Pyrogram(pyrosequencer.getOrder(),
                   arr, Integer.parseInt(numDisps.getText()));
                  graphOne = new Pyrograph(firstPyrogram);
                  graphsPanel.add(graphOne, BorderLayout.NORTH);
               }
               else if (numDisps.getText().equals("") || Integer.parseInt(numDisps.getText()) < 1) {
                  JOptionPane.showMessageDialog(mainFrame,
                   "Invalid number of sequence iterations");
                  return;
               }
            }
            else if (arr == null) {
               //TODO error creating first Pyrogram
            }
            if (arr2 != null) {
               if (!numDisps.getText().equals("") && Integer.parseInt(numDisps.getText()) > 0) {
                  secondPyrogram = new Pyrogram(pyrosequencer.getOrder(),
                   arr2, Integer.parseInt(numDisps.getText()));
                  graphTwo = new Pyrograph(secondPyrogram);
                  graphsPanel.add(graphTwo, BorderLayout.SOUTH);
               }
               else if (numDisps.getText().equals("") || Integer.parseInt(numDisps.getText()) < 1) {
                  JOptionPane.showMessageDialog(mainFrame,
                   "Invalid number of sequence iterations");
                  return;
               }
            }
            else if (arr2 == null) {
               //TODO error creating second Pyrogram
            }

            //TODO display pyrograms
            mainFrame.setPyrograms(firstPyrogram, secondPyrogram);

            Dimension firstDim = graphOne.getPreferredSize();
            //Dimension secondDim = graphTwo.getPreferredSize();


            graphsPanel.revalidate();
            JScrollPane pane = new JScrollPane(graphsPanel);
            pane.revalidate();

            mainFrame.setContentPane(pane);
            mainFrame.setPreferredSize(new Dimension((int) firstDim.getWidth(),
             (int) firstDim.getHeight()));
            mainFrame.validate();

            /*
            //pyroGraphs.add(graphTwo, null);

            /*
            pyroGraphs.setPreferredSize(new Dimension(
             (int) Math.max(firstDim.getWidth(), secondDim.getWidth()),
             (int) Math.max(firstDim.getHeight(), secondDim.getHeight())));

            pyroGraphs.validate();

            */
            
            openDialog.dispose();
         }
      });

      /*
       *
       * Add the components to the dialog window then show the dialog window
       *
       */
      
      openDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      openDialog.add(firstLabel);
      openDialog.add(firstText);
      openDialog.add(firstBrowse);
      openDialog.add(enableSecond);
      openDialog.add(secondLabel);
      openDialog.add(secondText);
      openDialog.add(secondBrowse);
      openDialog.add(nucleotideLabel);
      openDialog.add(nucleOrder);
      openDialog.add(sequenceBrowse);
      openDialog.add(startLabel);
      openDialog.add(startPos);
      openDialog.add(lengthLabel);
      openDialog.add(numDisps);
      openDialog.add(cancelButton);
      openDialog.add(okayButton);
      
      //position of first file label relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, firstLabel, 25,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, firstLabel, -10,
       SpringLayout.WEST, firstBrowse);
      dialogLayout.putConstraint(SpringLayout.SOUTH, firstLabel, -5,
       SpringLayout.NORTH, firstText);
      
      //position of first textField relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, firstText, 25,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, firstText, -10,
       SpringLayout.WEST, firstBrowse);
      dialogLayout.putConstraint(SpringLayout.NORTH, firstText, 25,
       SpringLayout.NORTH, openDialog.getContentPane());
      
      //position of first browse Button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, firstBrowse, -25,
       SpringLayout.EAST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, firstBrowse, 25,
       SpringLayout.NORTH, openDialog.getContentPane());

      //position of enabling checkbox for second file relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.WEST, enableSecond, 5,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, enableSecond, 5,
       SpringLayout.SOUTH, firstText);
      
      //position of second file label relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, secondLabel, 25,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, secondLabel, -10,
       SpringLayout.WEST, secondBrowse);
      dialogLayout.putConstraint(SpringLayout.SOUTH, secondLabel, -5,
       SpringLayout.NORTH, secondText);
      
      //position of second textField relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, secondText, 25,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, secondText, -10,
       SpringLayout.WEST, secondBrowse);
      dialogLayout.putConstraint(SpringLayout.NORTH, secondText, 25,
       SpringLayout.SOUTH, enableSecond);
      
      //position second browse Button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, secondBrowse, -25,
       SpringLayout.EAST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, secondBrowse, 25,
       SpringLayout.SOUTH, enableSecond);
      
      //position of label for nucleotide field relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, nucleotideLabel, 25,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, nucleotideLabel, 25,
       SpringLayout.SOUTH, secondText);
      
      //position nucleOrder relative to pane and second textField
      dialogLayout.putConstraint(SpringLayout.WEST, nucleOrder, 15,
       SpringLayout.EAST, nucleotideLabel);
      dialogLayout.putConstraint(SpringLayout.EAST, nucleOrder, 120,
       SpringLayout.WEST, nucleOrder);
      dialogLayout.putConstraint(SpringLayout.NORTH, nucleOrder, 25,
       SpringLayout.SOUTH, secondText);
      
      //position sequence Browse button next to nucleOrder
      dialogLayout.putConstraint(SpringLayout.WEST, sequenceBrowse, 15,
       SpringLayout.EAST, nucleOrder);
      dialogLayout.putConstraint(SpringLayout.EAST, sequenceBrowse, -20,
       SpringLayout.EAST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, sequenceBrowse, 25,
       SpringLayout.SOUTH, secondText);
      dialogLayout.putConstraint(SpringLayout.SOUTH, sequenceBrowse, 25,
       SpringLayout.NORTH, sequenceBrowse);
      
      //position of label for start position relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, startLabel, 25,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, startLabel, 25,
       SpringLayout.SOUTH, nucleOrder);
      
      //position of startPos text field relative to pane and second textField
      dialogLayout.putConstraint(SpringLayout.WEST, startPos, 15,
       SpringLayout.EAST, startLabel);
      dialogLayout.putConstraint(SpringLayout.EAST, startPos, 50,
       SpringLayout.WEST, startPos);
      dialogLayout.putConstraint(SpringLayout.NORTH, startPos, 25,
       SpringLayout.SOUTH, nucleOrder);
      
      //position first textField for first Browse File relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, lengthLabel, 25,
       SpringLayout.EAST, startPos);
      dialogLayout.putConstraint(SpringLayout.NORTH, lengthLabel, 25,
       SpringLayout.SOUTH, nucleOrder);
      
      //position nucleOrder relative to pane and second textField
      dialogLayout.putConstraint(SpringLayout.WEST, numDisps, 15,
       SpringLayout.EAST, lengthLabel);
      dialogLayout.putConstraint(SpringLayout.EAST, numDisps, 50,
       SpringLayout.WEST, numDisps);
      dialogLayout.putConstraint(SpringLayout.NORTH, numDisps, 25,
       SpringLayout.SOUTH, nucleOrder);
      
      //Position Cancel button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, cancelButton, -50,
       SpringLayout.EAST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.SOUTH, cancelButton, -25,
       SpringLayout.SOUTH, openDialog.getContentPane());
      
      //Position okay button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.WEST, okayButton, 50,
       SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.SOUTH, okayButton, -25,
       SpringLayout.SOUTH, openDialog.getContentPane());
      
      openDialog.setVisible(true);
   }
}

/**
 * integrate this stuff into this class for proper decoupling of graph display
 *
   public void displayPyrographs() {
      setBackground(Color.white);
      setPreferredSize(new Dimension(1000, 760));
       
      int preferredWidth = 5000, preferredHeight = 1000;
       
      JPanel mainView = new JPanel();
      mainView.setLayout(new BoxLayout(mainView, BoxLayout.Y_AXIS));
      mainView.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
      mainView.setSize(new Dimension(preferredWidth, preferredHeight));
      

      if (!firstHistSet) {
         System.err.println("first file not parsed");
         return;
      }
      
      if (secondHistSet) {
         Pyrograph pyroPyrograph = new Pyrograph(secondDataFile);
         preferredHeight = 500;
          
         pyroPyrograph.setAlignmentY(BOTTOM_ALIGNMENT);
         pyroPyrograph.setOpaque(false);
         pyroPyrograph.setLowerBound(mainView.getHeight());
         pyroPyrograph.setSize(new Dimension(5000, 500));
         mainView.add(pyroPyrograph);
         mainView.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
      }
      
      if (firstHistSet) {
         Pyrograph content = new Pyrograph(firstDataFile);
         
         if (secondHistSet) {
            content.setAlignmentY(TOP_ALIGNMENT);
            content.setLowerBound(mainView.getHeight()/2);
         }
         
         content.setOpaque(false);
         content.setSize(new Dimension(5000, 500));
          
         mainView.add(content);
         mainView.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
         
      }
      
      mainView.revalidate();       
      JScrollPane scrollView = new JScrollPane();
   */
