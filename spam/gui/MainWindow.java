package spam.gui;

/*
import spam.gui.listeners.clusterListeners.ClusterSingleListener;
import spam.gui.listeners.dendogramListeners.DendogramListener;
import spam.gui.listeners.rawHandlers.ComparePyrogramsListener;
import spam.gui.listeners.rawHandlers.DisplayPyrogramListener;
import spam.gui.listeners.xmlHandlers.CompareXMLPyrogramsListener;
import spam.gui.listeners.xmlHandlers.DisplayXMLPyrogramListener;
import spam.gui.listeners.SaveFileListener;
*/
import spam.gui.dialogs.InputDialog;

//import spam.dataTypes.ClusterDendogram;
//import spam.dataTypes.Pyrogram;

import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainWindow extends JFrame {
   private static final int FRAME_WIDTH = 500,
                            FRAME_HEIGHT = 600;

   private static MainWindow mMainFrame = null;
   private JMenuBar mMenuBar;

   public static MainWindow getMainFrame() {
      if (mMainFrame == null) {
         mMainFrame = new MainWindow();
      }

      return mMainFrame;
   }
   
   private MainWindow() {
      super("SPAM - Suite of Pyroprint Analysis Methods");

      setSize(FRAME_WIDTH, FRAME_HEIGHT);
      setLocationRelativeTo(null);
   }

   public boolean init() {
      mMenuBar = new JMenuBar();

      mMenuBar.add(initFileMenu());
      mMenuBar.add(initAnalysisMenu());
      mMenuBar.add(initClusterMenu());
      mMenuBar.add(initDendogramMenu());
      mMenuBar.add(initLibMenu());
      mMenuBar.add(initPyroMenu());

      setJMenuBar(mMenuBar);
      validate();

      return true;
   }

   /**
    * Initialize menu items that will be present in the 'File' menu, then
    * return the initialized 'File' menu
    */
   private JMenu initFileMenu() {
      JMenuItem importDendogram = new JMenuItem("View dendogram file");
      //JMenuItem importPyrogram = new JMenuItem("Open Pyroprint file");

      JMenuItem saveFile = new JMenuItem("Save As...");
      /*
      SaveFileListener saveListener = new SaveFileListener();
      saveListener.setOwner(this);
      saveFile.addActionListener(saveListener);
      */

      JMenuItem exitProgram = new JMenuItem("Exit");
      exitProgram.addActionListener(new ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
            System.exit(0);
         }
      });


      JMenu fileMenu = new JMenu("File");
      fileMenu.add(importDendogram);
      //fileMenu.add(saveFile);
      fileMenu.add(exitProgram);

      return fileMenu;
   }

   private JMenu initAnalysisMenu() {
      JMenu analysisMenu = new JMenu("Analysis");
      JMenu strainIdentifySubMenu = new JMenu("Identify strains from...");

      JMenuItem cplopDatabaseItem = new JMenuItem("CPLOP database");
      cplopDatabaseItem.addActionListener(new ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            InputDialog dataSelectDialog = new InputDialog(mMainFrame, "CPLOP Data Selection");
            dataSelectDialog.init();
            dataSelectDialog.setVisible(true);
         }
      });

      strainIdentifySubMenu.add(cplopDatabaseItem);
      analysisMenu.add(strainIdentifySubMenu);

      return analysisMenu;
   }

   private JMenu initClusterMenu() {
      JMenuItem clusterSingle = new JMenuItem("E.coli data file");
      /*
      ClusterSingleListener clustSingleListener = new ClusterSingleListener(this);
      clusterSingle.addActionListener(clustSingleListener);
      */

      JMenu clusterMenu = new JMenu("Cluster");
      clusterMenu.add(clusterSingle);

      return clusterMenu;
   }

   private JMenu initDendogramMenu() {
      JMenuItem thresholdDendogram = new JMenuItem("Apply dendogram threshold");
      /*
      DendogramListener dendThresholdListener = new DendogramListener(this);
      thresholdDendogram.addActionListener(dendThresholdListener);
      */

      JMenu dendogramMenu = new JMenu("Dendogram");
      dendogramMenu.add(thresholdDendogram);

      return dendogramMenu;
   }

   private JMenu initLibMenu() {
      JMenuItem libDisplay = new JMenuItem("Display Pyroprints");
      JMenuItem libCompare = new JMenuItem("Compare Pyroprints");
      /*
      DisplayPyrogramListener libDisplayListener = new DisplayPyrogramListener();
      libDisplayListener.setOwner(this);
      libDisplay.addActionListener(libDisplayListener);

      ComparePyrogramsListener libCompareListener = new ComparePyrogramsListener();
      libCompareListener.setOwner(this);
      libCompare.addActionListener(libCompareListener);
      */

      JMenu libMenu = new JMenu("Library");
      libMenu.add(libDisplay);
      libMenu.add(libCompare);

      return libMenu;
   }

   private JMenu initPyroMenu() {
      JMenuItem pyroDisplay = new JMenuItem("Display Pyroprints");
      JMenuItem pyroCompare = new JMenuItem("Compare Pyroprints");
      /*
      DisplayXMLPyrogramListener pyroDisplayListener = new DisplayXMLPyrogramListener();
      pyroDisplayListener.setOwner(this);
      pyroDisplay.addActionListener(pyroDisplayListener);

      CompareXMLPyrogramsListener pyroCompareListener = new CompareXMLPyrogramsListener();
      pyroCompareListener.setOwner(this);
      pyroCompare.addActionListener(pyroCompareListener);
      */

      JMenu pyroMenu = new JMenu("PyroSequencing");
      pyroMenu.add(pyroDisplay);
      pyroMenu.add(pyroCompare);

      return pyroMenu;
   }
   
   public void showWindow() {
      setVisible(true);
      repaint();
   }
}
