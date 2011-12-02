package spam.guiListeners;

import spam.gui.MainWindow;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ImportDataListener implements ActionListener {
   private MainWindow mainFrame;
   private JDialog importDialog;
   private JTextField firstText, secondText;
   private JCheckBox enableSecond;
   private JButton okayButton, cancelButton, firstBrowse, secondBrowse;
   private SpringLayout dialogLayout;
   
   public void setOwner(MainWindow parentFrame) {
      mainFrame = parentFrame;
   }
   
   public void actionPerformed(java.awt.event.ActionEvent e) {
      importDialog = new JDialog(mainFrame, "Import File");
      dialogLayout = new SpringLayout();
      importDialog.getContentPane().setLayout(dialogLayout);
      
      importDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      importDialog.setMinimumSize(new Dimension(500,250));
      importDialog.setResizable(false);
      
      JLabel firstLabel = new JLabel("File 1:");
      firstText = new JTextField();
      
      JLabel secondLabel = new JLabel("File 2:");
      secondText = new JTextField();
      
      firstBrowse = new JButton("Browse Files");
      firstBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(chooser);
            
            if (returnVal == JFileChooser.CANCEL_OPTION) {
               System.out.println("cancelled");
            }
            else if (returnVal == JFileChooser.APPROVE_OPTION) {
               //Obtains the file name of the input from the file chooser
               firstText.setText(chooser.getSelectedFile().getAbsolutePath());
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
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(chooser);
            
            if (returnVal == JFileChooser.CANCEL_OPTION) {
               System.out.println("cancelled");
            }
            else if (returnVal == JFileChooser.APPROVE_OPTION) {
               //Obtains the file name of the input from the file chooser
               secondText.setText(chooser.getSelectedFile().getAbsolutePath());
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });
      
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            importDialog.dispose();
            return;
         }
      });
      
      okayButton = new JButton("Okay");
      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            if (!firstText.getText().equals("")) {
               //TODO create pyrogram from file
            }
            else if (firstText.getText().equals("")) {
               JOptionPane.showMessageDialog(mainFrame, "You must choose a file");
               return;
            }
                  
            if (secondText.isEnabled() && !secondText.getText().equals("")) {
               //TODO create pyrogram from file
            }
            else if (secondText.isEnabled() && secondText.getText().equals("")) {
               JOptionPane.showMessageDialog(mainFrame, "You must choose a file");
               return;
            }

            //TODO tell Pyrograph to display pyrograms
            importDialog.dispose();
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
      
      importDialog.add(firstLabel);
      importDialog.add(firstText);
      importDialog.add(firstBrowse);
      importDialog.add(enableSecond);
      importDialog.add(secondLabel);
      importDialog.add(secondText);
      importDialog.add(secondBrowse);
      importDialog.add(cancelButton);
      importDialog.add(okayButton);
      
      //position of first file label relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, firstLabel, 25,
       SpringLayout.WEST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, firstLabel, -10,
       SpringLayout.WEST, firstBrowse);
      dialogLayout.putConstraint(SpringLayout.SOUTH, firstLabel, -5,
       SpringLayout.NORTH, firstText);
      
      //position of first textField relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, firstText, 25,
       SpringLayout.WEST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, firstText, -10,
       SpringLayout.WEST, firstBrowse);
      dialogLayout.putConstraint(SpringLayout.NORTH, firstText, 25,
       SpringLayout.NORTH, importDialog.getContentPane());
      
      //position of first browse Button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, firstBrowse, -25,
       SpringLayout.EAST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, firstBrowse, 25,
       SpringLayout.NORTH, importDialog.getContentPane());

      //position of enabling checkbox for second file relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.WEST, enableSecond, 5,
       SpringLayout.WEST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, enableSecond, 5,
       SpringLayout.SOUTH, firstText);
      
      //position of second file label relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, secondLabel, 25,
       SpringLayout.WEST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, secondLabel, -10,
       SpringLayout.WEST, secondBrowse);
      dialogLayout.putConstraint(SpringLayout.SOUTH, secondLabel, -5,
       SpringLayout.NORTH, secondText);
      
      //position of second textField relative to pane and browse button
      dialogLayout.putConstraint(SpringLayout.WEST, secondText, 25,
       SpringLayout.WEST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, secondText, -10,
       SpringLayout.WEST, secondBrowse);
      dialogLayout.putConstraint(SpringLayout.NORTH, secondText, 25,
       SpringLayout.SOUTH, enableSecond);
      
      //position second browse Button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, secondBrowse, -25,
       SpringLayout.EAST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.NORTH, secondBrowse, 25,
       SpringLayout.SOUTH, enableSecond);
      
      //Position Cancel button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.EAST, cancelButton, -50,
       SpringLayout.EAST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.SOUTH, cancelButton, -25,
       SpringLayout.SOUTH, importDialog.getContentPane());
      
      //Position okay button relative to dialog pane
      dialogLayout.putConstraint(SpringLayout.WEST, okayButton, 50,
       SpringLayout.WEST, importDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.SOUTH, okayButton, -25,
       SpringLayout.SOUTH, importDialog.getContentPane());
      
      importDialog.setVisible(true);
   }

}
