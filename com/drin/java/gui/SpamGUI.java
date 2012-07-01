package com.drin.java.gui;

import com.drin.java.gui.MainWindow;
import com.drin.java.util.Configuration;

import java.io.File;

public class SpamGUI {
   private static SpamGUI main = new SpamGUI();
   private static String PROPS_DIR = "com/drin/java/util/props";

   public static void main(String[] args) {
      main.loadConfFiles();
      main.initMainWindow();
   }

   public void loadConfFiles() {
      File propertiesDir = new File(PROPS_DIR);
      File[] propertyFiles = propertiesDir.listFiles();

      for (int fileNdx = 0; fileNdx < propertyFiles.length; fileNdx++) {
         File propertyFile = propertyFiles[fileNdx];

         if (propertyFile.isFile()) {
            Configuration.loadConfiguration(propertyFile);
         }
      }
   }

   public void initMainWindow() {
      MainWindow gui = MainWindow.getMainFrame();
      gui.init();
      gui.showWindow();
   }
}
