package com.drin.java.gui;

import com.drin.java.gui.MainWindow;
import com.drin.java.util.Configuration;

import com.drin.java.util.InvalidPropertyException;

import java.io.File;

public class SpamGUI {
   private static SpamGUI main = new SpamGUI();
   private static String DEFAULT_FILE = "props-standard.cfg";

   public static void main(String[] args) {
      main.loadConfFiles();
      main.initMainWindow();
   }

   public void loadConfFiles() {
      File propertyFile = new File(DEFAULT_FILE);

      if (propertyFile.isFile()) {
         try {
            Configuration.loadConfiguration(propertyFile);
         }
         catch (InvalidPropertyException err) {
            System.out.println(err);
            System.exit(0);
         }
      }
   }

   public void initMainWindow() {
      MainWindow gui = MainWindow.getMainFrame();
      gui.init();
      gui.showWindow();
   }
}
