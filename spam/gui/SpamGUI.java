package spam.gui;

import spam.gui.MainWindow;

public class SpamGUI {
   public static void main(String[] args) {
      MainWindow gui = MainWindow.getMainFrame();
      gui.init();
      gui.showWindow();
   }
}
