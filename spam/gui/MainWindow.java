package spam.gui;

import spam.gui.listeners.clusterListeners.ClusterSingleListener;
import spam.gui.listeners.dendogramListeners.DendogramListener;

import spam.dataTypes.ClusterDendogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.util.List;

public class MainWindow extends JFrame {
   private JMenuBar mainMenuBar;
   
   private List<ClusterDendogram> computedClusters = null;
   
   public MainWindow() {
      super("E.coli Clustering");

      mainMenuBar = new JMenuBar();
      
      //Menus that are on the Menu Bar
      JMenu fileMenu = new JMenu("File");
      JMenu clusterMenu = new JMenu("Cluster");
      JMenu dendogramMenu = new JMenu("Dendogram");

      JMenu libMenu = new JMenu("Library");
      JMenu pyroMenu = new JMenu("PyroSequencing");
      //JMenu pyroMenu = new JMenu("PyroSequencing");

      //width is loosely calculated as number of characters in JMenu * 10
      setSize(new Dimension(500, 600));
      //center window
      setLocationRelativeTo(null);
       
      //Items that will go into the File menu
      JMenuItem importDendogram = new JMenuItem("View dendogram file");
      //JMenuItem importPyrogram = new JMenuItem("Open Pyrogram file");
      JMenuItem saveFile = new JMenuItem("Save As...");
      JMenuItem exitProgram = new JMenuItem("Exit");
       
      //Items that will go into the cluster menu
      JMenuItem clusterSingle = new JMenuItem("E.coli data file");

      //Items that will go into the dendogram menu
      JMenuItem thresholdDendogram = new JMenuItem("Apply dendogram threshold");

      //Items that will go into the Library menu
      JMenuItem libDisplay = new JMenuItem("Display pyrograms");
      JMenuItem libCompare = new JMenuItem("Compare Pyrograms");
       
      //Items that will go into the PyroSequencing menu
      JMenuItem pyroDisplay = new JMenuItem("Display pyrograms");
      JMenuItem pyroCompare = new JMenuItem("Compare Pyrograms");
       
      //Listener for exit menu item
      exitProgram.addActionListener(new ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
            System.exit(0);
         }
      });
       
      //Listener for 'save as...' menu item
      SaveFileListener saveListener = new SaveFileListener();
      saveListener.setOwner(this);
      saveFile.addActionListener(saveListener);

      //Listener for importPyrogram menu item
       
      //Listener for clusterSingle menu item
      ClusterSingleListener clustSingleListener = new ClusterSingleListener(this);
      clusterSingle.addActionListener(clustSingleListener);

      //Listener for thresholdDendogram menu item
      DendogramListener dendThresholdListener = new DendogramListener(this);
      thresholdDendogram.addActionListener(dendThresholdListener);

      //Listener for libDisplay menu item
      DisplayPyrogramListener libDisplayListener = new DisplayPyrogramListener();
      libDisplayListener.setOwner(this);
      libDisplay.addActionListener(libDisplayListener);
       
      //Listener for pyroDisplay menu item
      DisplayXMLPyrogramListener pyroDisplayListener = new DisplayXMLPyrogramListener();
      pyroDisplayListener.setOwner(this);
      pyroDisplay.addActionListener(pyroDisplayListener);
       
      //Listener for libCompare menu item
      ComparePyrogramsListener libCompareListener = new ComparePyrogramsListener();
      libCompareListener.setOwner(this);
      libCompare.addActionListener(libCompareListener);

      //Listener for pyroCompare menu item
      CompareXMLPyrogramsListener pyroCompareListener = new CompareXMLPyrogramsListener();
      pyroCompareListener.setOwner(this);
      pyroCompare.addActionListener(pyroCompareListener);
       
      //Listener for clusterMany menu item
      /*
      DisplayXMLPyrogramListener pyroDisplayListener = new DisplayXMLPyrogramListener();
      pyroDisplayListener.setOwner(this);
      pyroDisplay.addActionListener(pyroDisplayListener);
      */
       
      //adding Items to their Menus
      fileMenu.add(importDendogram);
      fileMenu.add(saveFile);
      fileMenu.add(exitProgram);
       
      clusterMenu.add(clusterSingle);

      dendogramMenu.add(thresholdDendogram);

      libMenu.add(libDisplay);
      libMenu.add(libCompare);

      pyroMenu.add(pyroDisplay);
      pyroMenu.add(pyroCompare);

      //adding Menus to the MenuBar
      mainMenuBar.add(fileMenu);
      mainMenuBar.add(clusterMenu);
      mainMenuBar.add(dendogramMenu);
      mainMenuBar.add(libMenu);
      mainMenuBar.add(pyroMenu);
       
      setJMenuBar(mainMenuBar);
      //setSize(1000, 760);
      validate();
   }
   
   public void showWindow() {
      setVisible(true);
      repaint();
   }

   public void saveState(List<ClusterDendogram> clustDends) {
      computedClusters = clustDends;
   }
}
