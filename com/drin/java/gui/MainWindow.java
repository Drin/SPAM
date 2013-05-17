package com.drin.java.gui;

import com.drin.java.gui.dialogs.InputDialog;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
   private static final int FRAME_WIDTH = 500,
                            FRAME_HEIGHT = 600;

   private static MainWindow mMainFrame = null;
   private static JTextArea mOutputCanvas = null;

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

      mOutputCanvas = new JTextArea();
      setContentPane(new JScrollPane(mOutputCanvas));
   }

   public boolean init() {
      mMenuBar = new JMenuBar();

      mMenuBar.add(initFileMenu());
      mMenuBar.add(initAnalysisMenu());

      setJMenuBar(mMenuBar);
      validate();

      return true;
   }

   /**
    * Initialize menu items that will be present in the 'File' menu, then
    * return the initialized 'File' menu
    */
   private JMenu initFileMenu() {
      JMenuItem exitProgram = new JMenuItem("Exit");
      exitProgram.addActionListener(new ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
            System.exit(0);
         }
      });


      JMenu fileMenu = new JMenu("File");
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

   public JTextArea getOutputCanvas() {
      return mOutputCanvas;
   }

   public void showWindow() {
      setVisible(true);
      repaint();
   }
}
