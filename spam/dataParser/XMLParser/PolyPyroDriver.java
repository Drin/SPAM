package spam.dataParser.XMLParser;

import polypyro.dataTypes.Pyrogram;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import spam.dataParser.XMLParser.PyroMarkParser.PyroMarkParser;
import spam.dataParser.XMLParser.PyroMarkParser.PyroRun;
import spam.dataParser.XMLParser.PyroMarkParser.WellData;
import spam.dataParser.XMLParser.PyroMarkParser.PeakValue;

public class PolyPyroDriver {


   public static HashMap<String, String> parseXMLHeaders(File xmlFile) {
      PyroMarkParser xmlParser = new PyroMarkParser(xmlFile);

      HashMap<String, String> headerMap = xmlParser.getWellInfo();
      /*
      System.out.println("Parsing XML Headers");
      for (String wellLabel : headerMap.keySet()) {
         System.out.println(wellLabel + ": " + headerMap.get(wellLabel));
      }
      */

      return headerMap;
   }

   /*
   public void parseXMLDir(String dirName, String wellName) {
      File xmlDir = new File(dirName);

      ArrayList<PyroRun> pyroRunData = new ArrayList<PyroRun>();

      for (int fileNdx = 0; fileNdx < xmlDir.list().length; fileNdx++) {
         if (!xmlDir.listFiles()[fileNdx].isFile() || !xmlDir.listFiles()[fileNdx].getName().contains("pyrorun"))
            continue;

         File xmlFile = new File(xmlDir.getAbsolutePath() + File.separator + xmlDir.listFiles()[fileNdx].getName());
         pyroRunData.add(PyroMarkParser.parsePyroRun(xmlFile, "Analyzed"));
      }

      for (int pyroData = 0; pyroData < pyroRunData.size(); pyroData++) {
         HashMap<String, WellData> wellMap = pyroRunData.get(pyroData).getWellData();
         if (!wellMap.containsKey(wellName)) continue;

         WellData well = wellMap.get(wellName);

         String tmpSeq = "";
         ArrayList<Double> data = new ArrayList<Double>();
         ArrayList<Double> peakVals = well.getNormalizedValues();

         for (HashMap<Integer, String> momentPair : well.getMoments()) {
            for (Integer momentNdx : momentPair.keySet()) {
               System.out.println("momentNdx: " + momentNdx);
               tmpSeq += momentPair.get(momentNdx);
               data.add(peakVals.get(momentNdx));
            }
         }
      }
   }
   */

   public static Pyrogram parseXMLFile(String fileName, String wellName) {
      File xmlFile = new File(fileName);
      PyroMarkParser xmlParser = new PyroMarkParser(xmlFile);

      PyroRun pyroRunData = xmlParser.parsePyroRun();
      

      HashMap<String, WellData> wellMap = pyroRunData.getWellData();

      if (!wellMap.containsKey(wellName))
         return null;

      WellData well = wellMap.get(wellName);

      String tmpSeq = "";
      ArrayList<Double> data = new ArrayList<Double>();
      List<Double> peakVals = well.getNormalizedValues();
      List<PeakValue> wellPeaks = well.getPeaks();

      for (int dispNdx = 0; dispNdx < wellPeaks.size(); dispNdx++) {
         PeakValue peak = wellPeaks.get(dispNdx);

         //build pyrogram
         tmpSeq += well.getSubstance(peak.getMoment());
         data.add(peakVals.get(dispNdx));

         //System.out.println("momentNdx: " + peak.getMoment());
      }

      return new Pyrogram(tmpSeq, data);
   }
}
