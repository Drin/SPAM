package XMLParser.PyroMarkParser;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class WellData {
   private String name = "";
   private final String newLine = System.getProperty("line.separator");
   private int eDisp, sDisp;
   private LinkedHashMap<Integer, String> moments = null;
   private List<PeakValue> peakVals = null;
   private List<Double> intensities = null, normalizedValues = null;
   private List<Integer> originalClassifications = null, currentClassifications = null;
   private HashMap<Integer, List<Double>> dropOffCurves;

   public WellData(String dnaName) {
      name = dnaName;
      eDisp = -1;
      sDisp = -1;
   }
   
   public String getName() {
      return name;
   }
   
   public int getEDisp() {
      return eDisp;
   }
   
   public int getSDisp() {
      return sDisp;
   }
   
   public List<PeakValue> getPeaks() {
      return peakVals;
   }
   
   public LinkedHashMap<Integer, String> getMoments() {
      return moments;
   }
   
   public List<Double> getIntensities() {
      return intensities;
   }
   
   public List<Double> getNormalizedValues() {
      return normalizedValues;
   }
   
   public HashMap<Integer, List<Double>> getDropOffCurves() {
      return dropOffCurves;
   }
   
   public List<Integer> getOriginalClassifications() {
      return originalClassifications;
   }
   
   public List<Integer> getCurrentClassifications() {
      return currentClassifications;
   }
   
   public String getSubstance(int moment) {
      for (Integer momentKey : moments.keySet()) {
         if (momentKey.equals(moment)) {
            return moments.get(momentKey);
         }
      }
      
      return "";
   }
   
   public void setEDisp(int val) {
      eDisp = val;
   }
   
   public void setSDisp(int val) {
      sDisp = val;
   }
   
   public void setMoments(LinkedHashMap<Integer, String> newMoments) {
      moments = newMoments;
   }
   
   public void setPeaks(List<PeakValue> newPeaks) {
      peakVals = newPeaks;
   }
   
   public void setIntensities(List<Double> newIntensities) {
      intensities = newIntensities;
      
      System.out.println("numIntensities per moment: " + (intensities.size()/ moments.size()));
   }
   
   public void setNormalizedVals(List<Double> normalVals) {
      normalizedValues = normalVals;
   }

   public void setDropOffCurves(HashMap<Integer, List<Double>> dropOffs) {
      dropOffCurves = dropOffs;
   }
   
   public void setOrigClassifications(List<Integer> classifications) {
      originalClassifications = classifications;
   }
   
   public void setCurrClassifications(List<Integer> classifications) {
      currentClassifications = classifications;
   }
   
   public String toString() {
      String str = name + ":" + newLine;
      
      for (Integer momentKey : moments.keySet()) {
         str += "\tMoment: " + momentKey + " Substance: " + moments.get(momentKey) + newLine;
      }
      
      if (peakVals != null) {
         for (PeakValue peak : peakVals) {
            str += "\tPeak Data: " + newLine + peak + newLine;
         }
      }
      
      str += "\tSDispensation Moment: " + sDisp + newLine;
      str += "\tEDispensation Moment: " + eDisp + newLine; 
      
      return str;
   }
}
