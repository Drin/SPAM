package spam.gui.listeners.clusterListeners;

import spam.clustering.HClustering;
import spam.gui.MainWindow;
import spam.gui.dialogs.InputDialog;

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

public class ClusterSingleListener implements ActionListener {
   private MainWindow mainFrame;
   private InputDialog clusterDialog;

   public ClusterSingleListener(MainWindow parentFrame) {
      super();
      this.mainFrame = parentFrame;
   }
   
   public void actionPerformed(ActionEvent e) {
      clusterDialog = new InputDialog(mainFrame, "Cluster Single File");

      clusterDialog.init();
      clusterDialog.setVisible(true);
   }
}
