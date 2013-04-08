package com.drin.java.gui;

import com.drin.java.gui.MainWindow;
import com.drin.java.util.Configuration;

import com.drin.java.util.InvalidPropertyException;

import java.io.File;

public class SpamGUI {
   private static SpamGUI main = new SpamGUI();

   public static void main(String[] args) {
      main.loadConfFiles();
      main.initMainWindow();
   }

   public void loadConfFiles() {
      Configuration.loadConfig();
   }

   public void initMainWindow() {
      MainWindow gui = MainWindow.getMainFrame();
      gui.init();
      gui.showWindow();
   }
}
