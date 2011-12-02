package spam.gui;

import spam.gui.MainWindow;
import spam.dataTypes.Pyrogram;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Pyrograph extends JPanel {
   private ArrayList<Double> seqPeaks = null;
   private Pyrogram pyro;
   private String seqOrder = null;

   private double verticalBase = 660, verticalIncr = 20, maxHeight = 0, maxWidth = 0;
   private double maxFreq = 0;
   private int horizontalBase = 40, horizontalIncr = 11;

   
   public Pyrograph(Pyrogram pyro) {
      super();

      this.pyro = pyro;
      seqOrder = pyro.getSequence();
      seqPeaks = pyro.getData();

      this.setPreferredSize(new Dimension(
       pyro.getLength() * horizontalIncr, (int) (pyro.getMaxPeak() * verticalIncr))
      );

      /*
      System.out.println("going to draw sequence: " + seqOrder +
       "and peakVals: " + seqPeaks);
       */
      maxFreq = pyro.getMaxPeak();
   }

   public Pyrogram getPyrogram() {
      return pyro;
   }
   
   private Color getColor(char nucleotide) {
      return nucleotide == 'A' ? Color.GREEN :
       nucleotide == 'T' ? Color.BLUE :
       nucleotide == 'C' ? Color.RED :
       nucleotide == 'G' ? Color.BLACK :
       null;
   }
   
   /**
    * The paintComponent method.
    */
   public void paintComponent(Graphics brush) {
      Graphics2D brush2D = (Graphics2D) brush;


      /**
       * Set the color of each number in the histogram so that each is different
       * than the one next to it; Then the actual rectangle is drawn as well as
       * hashmarks and letters denoting if it is A, T, C, or G.
       */

      //pyroNdx represents the index in the pyrosequence
      for(int pyroNdx = 0; pyroNdx < seqPeaks.size(); pyroNdx++) {
         double barPeak = verticalBase - (seqPeaks.get(pyroNdx) * 20);
         double barHeight = seqPeaks.get(pyroNdx) * 20;
         double horizontalPos = horizontalBase + (horizontalIncr * pyroNdx);
         //TODO change the width to be about 18ish
         int barWidth = 10;

         brush.setColor(getColor(seqOrder.charAt(pyroNdx)));

         //draws the histogram bar
         brush2D.drawRect((int) horizontalPos, (int) barPeak, barWidth, (int) barHeight);
         //colors the histogram bar
         brush2D.fill(new Rectangle2D.Double(horizontalPos, barPeak, barWidth, barHeight));

         brush2D.setColor(Color.BLACK);
         //draws the "peakValue" of the dispensation
         brush2D.drawString(seqPeaks.get(pyroNdx) + "", (float) horizontalPos, (float) (barPeak - 2));
      }
   
      Font font = new Font("Dialog", Font.PLAIN, 10);
      brush2D.setFont(font);
      brush2D.setColor(Color.BLACK);
   
      //Line2D constructor: double, double, double, double
      //this draws the scale on the y-axis
      brush2D.draw(new Line2D.Double(10, verticalBase, 10, verticalBase - (maxFreq * 20)));

      //draws and labels intervals on the y-axis scale
      for (int peak = 0; peak <= maxFreq; peak++) {
         double verticalPos = verticalBase - verticalIncr * peak;

         //draws hash marks for each increment
         brush2D.draw(new Line2D.Double(10, verticalPos, 20, verticalPos));
      
         //draws peak value corresponding to hash mark
         brush2D.drawString("" + peak,(float) 25, (float) verticalPos);
      }
      
      for (int pyroNdx = 0; pyroNdx < seqPeaks.size(); pyroNdx++) {
         double horizontalPos = horizontalBase + (horizontalIncr * pyroNdx);
         double letterOffset = verticalBase + 10;
         double ndxOffset = letterOffset + 10;
         int ndxInterval = 10;

         //g2d.drawString(str, x, y)
         //prints the letter at the bottom (col = charArray)
         brush2D.drawString(seqOrder.charAt(pyroNdx) + "", (float) horizontalPos, (float) letterOffset);
      
         if (pyroNdx % ndxInterval == 0) {
            //writes the spot in the dispensation whenever the cycle starts over
            brush2D.drawString(pyroNdx + "", (float) horizontalPos, (float) ndxOffset);
         }
      }

      repaint();
   }
}
