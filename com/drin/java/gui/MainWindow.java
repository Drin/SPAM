package com.drin.java.gui;

import com.drin.java.SPAMMain;
import com.drin.java.gui.dialogs.CPLOPDialog;
import com.drin.java.output.ProgressWriter;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
   private static final int FRAME_WIDTH = 500,
                            FRAME_HEIGHT = 600;

   private static MainWindow mMainFrame = null;
   private static SPAMMain mSPAMInterface = null;
   private static JTextArea mOutputCanvas = null;

   private JMenuBar mMenuBar;

   public static MainWindow getMainFrame() {
      if (mMainFrame == null) { mMainFrame = new MainWindow(); }
      return mMainFrame;
   }

   private MainWindow() {
      super("SPAM - Suite of Pyroprint Analysis Methods");
      mOutputCanvas = new JTextArea();
      mSPAMInterface = new SPAMMain(new ProgressWriter(mOutputCanvas));

      setLocationRelativeTo(null);
      setSize(FRAME_WIDTH, FRAME_HEIGHT);
      setContentPane(new JScrollPane(mOutputCanvas));
   }

   public static void main(String[] args) {
      MainWindow main = MainWindow.getMainFrame();

      main.init();
      main.showWindow();
   }

   public boolean init() {
      mMenuBar = new JMenuBar();

      mMenuBar.add(initFileMenu());
      mMenuBar.add(initAnalysisMenu());

      setJMenuBar(mMenuBar);
      validate();

      return true;
   }

   public void showWindow() {
      setVisible(true);
      repaint();
   }

   public SPAMMain getSPAMInterface() { return mSPAMInterface; }
   public JTextArea getOutputCanvas() { return mOutputCanvas; }

   /**
    * Initialize menu items that will be present in the 'File' menu, then
    * return the initialized 'File' menu
    */
   private JMenu initFileMenu() {
      JMenuItem exitProgram = new JMenuItem("Exit");
      exitProgram.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) { System.exit(0); }
      });

      JMenu fileMenu = new JMenu("File");
      fileMenu.add(exitProgram);

      return fileMenu;
   }

   private JMenu initAnalysisMenu() {
      JMenu analysisMenu = new JMenu("Analysis");

      JMenu strainIdentifySubMenu = new JMenu("Identify strains from...");

      JMenuItem cplopDatabaseItem = new JMenuItem("CPLOP");
      cplopDatabaseItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent actionEvent) {
            CPLOPDialog cplopDiag = new CPLOPDialog(mMainFrame, "CPLOP Data Selection");
            cplopDiag.init();
            cplopDiag.setVisible(true);
         }
      });

      JMenuItem strainMatchItem = new JMenuItem("Match isolates against strains");

      strainIdentifySubMenu.add(cplopDatabaseItem);

      analysisMenu.add(strainMatchItem);
      analysisMenu.add(strainIdentifySubMenu);

      return analysisMenu;
   }
}
