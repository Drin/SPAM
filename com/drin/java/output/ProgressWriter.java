package com.drin.java.output;

import javax.swing.JTextArea;

public class ProgressWriter {
   private JTextArea mCanvas;

   public ProgressWriter(JTextArea canvas) {
      mCanvas = canvas;
   }

   public JTextArea getCanvas() { return mCanvas; }

   public void writeProgress(float progressPct) {
      mCanvas.setText(String.format("clustering is %.02f%% complete...",
                                    progressPct * 100));
   }

   public void writeText(String text) {
      mCanvas.setText(text);
   }
}
