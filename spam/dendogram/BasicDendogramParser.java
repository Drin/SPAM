package TemporalHIsolateing.dendogram;

import TemporalHIsolateing.dataTypes.Isolate;

import java.io.File;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class BasicDendogramParser {
   File dendogramFile;

   public BasicDendogramParser(String fileName) {
      dendogramFile = new File(fileName);
   }

   public List<List<Isolate>> parseDendogram(double threshold) {
      List<List<Isolate>> isolateMap = new ArrayList<List<Isolate>>;
      Scanner dendogramScanner = new Scanner(file);

      while (dendogramScanner.hasNext()) {
         isolateMap.add(inspectNode(dendogramScanner, threshold));
      }

      dendogramScanner.close();
   }

   private List<Isolate> inspectNode(Scanner dendogramScanner, double threshold) {
      List<Isolate> isolateList = null;

      while (dendogramScanner.hasNextLine()) {
         String tmpLine = dendogramScanner.nextLine();

         if (tmpLine.contains("node")) {
            Scanner lineScanner = new Scanner(tmpLine);
            double correlation = -1;

            if (isConnectedNode(lineScanner, threshold)) {
               newIsolate = extractNode(dendogramScanner);
            }

            lineScanner.close();
         }
      }
   }

   private List<Isolate> extractNode(Scanner dendogramScanner) {
      List<Isolate> isolateList = new ArrayList<Isolate>();

      while (dendogramScanner.hasNext()) {
         String tmpLine = dendogramScanner.nextLine();

         if (tmpLine.contains("<node")) {
            isolateList.addAll(extractNode(dendogramScanner));
         }
         else if (tmpLine.contains("<leaf")) {
         }
      }
   }

   private boolean isConnectedNode(Scanner lineScanner, threshold) {
      double correlation = -1;

      while (lineScanner.hasNext()) {
         String lineToken = lineScanner.next().replaceAll("\"", " ");

         try {
            correlation = Double.valueOf(lineToken);
         }

         catch(NumberFormatException castErr) {
            //This was not the correlation value
         }

         if (correlation != -1) break;
      }

      return correlation > threshold;
   }
}
