package spam.gui.listeners.xmlHandlers;

import spam.gui.MainWindow;

import spam.gui.Pyrograph;

import spam.dataParser.XMLParser.PolyPyroDriver;
import spam.dataParser.Polyparse;
import spam.dataParser.SequenceParser;

import spam.dataTypes.Pyrogram;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class DisplayXMLPyrogramListener implements java.awt.event.ActionListener {
   private MainWindow mainFrame;
   private Polyparse pyrosequencer;
   private SpringLayout dialogLayout;
   private JDialog openDialog;
   private JTextField firstText, firstWell, secondText, secondWell, nucleOrder, numDisps, startPos;
   private JCheckBox enableSecond;
   private JButton okayButton, cancelButton, firstBrowse, firstWellBrowse, secondBrowse, secondWellBrowse, sequenceBrowse;
   private String recentlyAccessedDir = "", tempAccessedDir = "";
   
   public void setOwner(MainWindow parentFrame) {
      this.mainFrame = parentFrame;
   }
   
   public void actionPerformed(java.awt.event.ActionEvent e) {      
      dialogLayout = new SpringLayout();
      openDialog = new JDialog(mainFrame, "Open File");
      openDialog.getContentPane().setLayout(dialogLayout);

      openDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      openDialog.setMinimumSize(new Dimension(500,400));
      openDialog.setResizable(false);      

      JLabel firstLabel = new JLabel("File 1:");
      firstText = new JTextField();

      JLabel firstWellLabel = new JLabel("Well:");
      firstWell = new JTextField();

      JLabel secondLabel = new JLabel("File 2:");
      secondText = new JTextField();

      JLabel secondWellLabel = new JLabel("Well:");
      secondWell = new JTextField();

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
               tempAccessedDir = chooser.getSelectedFile().getPath();
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });

      firstWellBrowse = new JButton("Choose Well");
      firstWellBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               File xmlFile = new File(firstText.getText());
               HashMap<String, String> headerMap = PolyPyroDriver.parseXMLHeaders(xmlFile);
               final String[] wellStrArr = new String[24];
               int wellStrNdx = 0, maxWidth = 0, fontWidth = 10;

               for (int rowNdx = 0; rowNdx < 3; rowNdx++) {
                  String wellLetter = Character.toString((char)((int)'A' + rowNdx));

                  for (int colNdx = 1; colNdx < 9; colNdx++) {
                     String tmpStr = wellLetter + colNdx + ": " + headerMap.get("A" + colNdx);

                     wellStrArr[wellStrNdx++] = tmpStr;
                     maxWidth = Math.max(maxWidth, tmpStr.length());

                     //System.out.println("parsed: " + tmpStr);
                  }

               }

               final JList wellList = new JList(wellStrArr);
               wellList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
               wellList.setLayoutOrientation(JList.VERTICAL_WRAP);
               wellList.setVisibleRowCount(8);
               wellList.setFixedCellWidth(maxWidth * fontWidth);

               SpringLayout wellWindowLayout = new SpringLayout();
               final JDialog wellWindow = new JDialog(openDialog, "Well Options");
               wellWindow.setSize(new Dimension(355, 240));
               wellWindow.setLayout(wellWindowLayout);

               JButton wellCancelButton = new JButton("Cancel");
               wellCancelButton.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                     wellWindow.dispose();
                  }
               });

               JButton wellOkayButton = new JButton("Okay");
               wellOkayButton.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                     String wellName = wellStrArr[wellList.getSelectedIndex()];
                     String wellPos = wellName.substring(0, 2);
                     String wellStrain = wellName.substring(5, wellName.length());
                     firstWell.setText(wellPos); 

                     wellWindow.dispose();
                  }
               });

               wellWindow.add(wellList);
               wellWindow.add(wellOkayButton);
               wellWindow.add(wellCancelButton);

               wellWindowLayout.putConstraint(SpringLayout.NORTH, wellList, 10,
                SpringLayout.NORTH, wellWindow.getContentPane());
               wellWindowLayout.putConstraint(SpringLayout.WEST, wellList, 10,
                SpringLayout.WEST, wellWindow.getContentPane());
               wellWindowLayout.putConstraint(SpringLayout.EAST, wellList, -10,
                SpringLayout.EAST, wellWindow.getContentPane());

               wellWindowLayout.putConstraint(SpringLayout.NORTH, wellOkayButton, 15,
                SpringLayout.SOUTH, wellList);
               wellWindowLayout.putConstraint(SpringLayout.WEST, wellOkayButton, 72,
                SpringLayout.WEST, wellWindow.getContentPane());

               wellWindowLayout.putConstraint(SpringLayout.NORTH, wellCancelButton, 15,
                SpringLayout.SOUTH, wellList);
               wellWindowLayout.putConstraint(SpringLayout.WEST, wellCancelButton, 50,
                SpringLayout.EAST, wellOkayButton);
               wellWindowLayout.putConstraint(SpringLayout.EAST, wellCancelButton, -72,
                SpringLayout.EAST, wellWindow.getContentPane());

               wellWindow.setVisible(true);
            }
            catch (NullPointerException Err) {
               JOptionPane.showMessageDialog(mainFrame, "No File Selected");
               return;
            }

            //TODO have some way to parse/select the wells
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

      secondWellBrowse = new JButton("Choose Well");
      secondWellBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         }
      });
      
      //sequenceBrowse is the browse button for choosing a file containing a dispensation order
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
               String nucleSeq = Polyparse.parseSequence(chooser.getSelectedFile().getAbsoluteFile());
               nucleOrder.setText(new SequenceParser(nucleSeq).getExpandedSequence());
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
      secondWell.setEnabled(!enableSecond.isSelected());
      secondWellBrowse.setEnabled(!enableSecond.isSelected());

      enableSecond.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            secondText.setEnabled(!enableSecond.isSelected());
            secondBrowse.setEnabled(!enableSecond.isSelected());

            secondWell.setEnabled(!enableSecond.isSelected());
            secondWellBrowse.setEnabled(!enableSecond.isSelected());
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
            Pyrograph pyro = null;

            if (!firstText.getText().equals("") && firstText.getText().contains(".pyrorun")) {
               //TODO placeholder needs to be replaces with a well name
               PolyPyroDriver xmlDriver = new PolyPyroDriver();
               pyro = new Pyrograph(xmlDriver.parseXMLFile(firstText.getText(), firstWell.getText()));
            }
            else if (firstText.getText().equals("")) {
               //error
               JOptionPane.showMessageDialog(mainFrame, "First File Not Chosen");
               return;
            }
            else if (!firstText.getText().contains(".pyrorun")) {
               //error
               JOptionPane.showMessageDialog(mainFrame, "Data File Must be a pyrorun File");
               return;
            }

            if (secondText.isEnabled() && !secondText.getText().equals("")) {
               //TODO prepare 2nd pyrogram
            }
            else if (secondText.isEnabled() && secondText.getText().equals("")) {
               //error
               JOptionPane.showMessageDialog(mainFrame, "Second File Not Chosen");
               return;
            }

            Dimension firstDim = pyro.getPreferredSize();
            //Dimension secondDim = graphTwo.getPreferredSize();

            JScrollPane pane = new JScrollPane(pyro);
            pane.validate();

            mainFrame.setContentPane(pane);
            mainFrame.setPreferredSize(new Dimension((int) firstDim.getWidth(),
             (int) firstDim.getHeight()));
            mainFrame.validate();

            openDialog.dispose();
         }
      });
      
      openDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      openDialog.add(firstLabel);
      openDialog.add(firstText);
      openDialog.add(firstWellLabel);
      openDialog.add(firstWell);
      openDialog.add(firstBrowse);
      openDialog.add(firstWellBrowse);
      openDialog.add(enableSecond);
      openDialog.add(secondLabel);
      openDialog.add(secondText);
      openDialog.add(secondWellLabel);
      openDialog.add(secondWell);
      openDialog.add(secondBrowse);
      openDialog.add(secondWellBrowse);
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
      //dialogLayout.putConstraint(SpringLayout.SOUTH, firstBrowse, 20,
         //   SpringLayout.NORTH, firstBrowse);

      //position of first well text label relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, firstWellLabel, 25,
            SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, firstWellLabel, 25,
            SpringLayout.NORTH, firstText);

      //position of first well text relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, firstWell, 25,
            SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, firstWell, -10,
            SpringLayout.WEST, firstWellBrowse);
      dialogLayout.putConstraint(SpringLayout.NORTH, firstWell, 25,
            SpringLayout.NORTH, firstWellLabel);

      //position of first well browse Button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, firstWellBrowse, -25,
            SpringLayout.EAST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, firstWellBrowse, 25,
            SpringLayout.NORTH, firstWellLabel);

      //position of enabling checkbox for second file relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.WEST, enableSecond, 5,
            SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, enableSecond, 5,
            SpringLayout.SOUTH, firstWell);
      
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
      //dialogLayout.putConstraint(SpringLayout.SOUTH, secondBrowse, 20,
         //   SpringLayout.NORTH, secondBrowse);

      //position of first well text label relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, secondWellLabel, 25,
            SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, secondWellLabel, 25,
            SpringLayout.NORTH, secondText);

      //position of second well text relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, secondWell, 25,
            SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, secondWell, -10,
            SpringLayout.WEST, secondWellBrowse);
      dialogLayout.putConstraint(SpringLayout.NORTH, secondWell, 25,
            SpringLayout.NORTH, secondWellLabel);

      //position of second well browse Button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, secondWellBrowse, -25,
            SpringLayout.EAST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, secondWellBrowse, 25,
            SpringLayout.NORTH, secondWellLabel);
      
      //position of label for nucleotide field relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, nucleotideLabel, 25,
            SpringLayout.WEST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, nucleotideLabel, 25,
            SpringLayout.SOUTH, secondWell);
      
      //position nucleOrder relative to pane and second textField
      dialogLayout.putConstraint(SpringLayout.WEST, nucleOrder, 15,
            SpringLayout.EAST, nucleotideLabel);
      dialogLayout.putConstraint(SpringLayout.EAST, nucleOrder, 120,
            SpringLayout.WEST, nucleOrder);
      dialogLayout.putConstraint(SpringLayout.NORTH, nucleOrder, 25,
            SpringLayout.SOUTH, secondWell);
      
      //position sequence Browse button next to nucleOrder
      dialogLayout.putConstraint(SpringLayout.WEST, sequenceBrowse, 10,
            SpringLayout.EAST, nucleOrder);
      dialogLayout.putConstraint(SpringLayout.EAST, sequenceBrowse, -25,
            SpringLayout.EAST, openDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, sequenceBrowse, 25,
            SpringLayout.SOUTH, secondWell);
      dialogLayout.putConstraint(SpringLayout.SOUTH, sequenceBrowse, 20,
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
