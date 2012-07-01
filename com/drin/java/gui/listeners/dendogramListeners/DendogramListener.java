package com.drin.java.gui.listeners.dendogramListeners;

import com.drin.javaclustering.HClustering;
import com.drin.java.gui.MainWindow;
import com.drin.java.gui.dialogs.ThresholdDialog;

import java.io.File;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

public class DendogramListener implements ActionListener {
   private MainWindow mainFrame;
   private ThresholdDialog thresholdDialog;

   public DendogramListener(MainWindow parentFrame) {
      super();
      this.mainFrame = parentFrame;
   }
   
   public void actionPerformed(ActionEvent e) {
      thresholdDialog = new ThresholdDialog(mainFrame, "Apply dendogram threshold");

      thresholdDialog.init();
      thresholdDialog.setVisible(true);
   }
}
