package spam.dataParser.XMLParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;

import spam.dataParser.XMLParser.PyroMarkParser.PeakValue;
import spam.dataParser.XMLParser.PyroMarkParser.WellData;

/*
 * will write data structure contents in a given orientation
 */
public class CsvWriter {
	private File csv = null;
	private BufferedWriter csvAssistant = null;
	private String newLine = System.getProperty("line.separator");
	
	public CsvWriter(File csvFile) {
		csv = csvFile;
		
		try {
			csvAssistant = new BufferedWriter(new FileWriter(csv));
		}
		catch (java.io.IOException err) {
			System.out.println("Error creating csvAssistant");
			return;
		}
	}
	
	public File getFile() { return csv; }
	
	public void writeConcisePyroCSVResult(HashMap<String, WellData> wellData) {
      System.out.println("Writing Pyro CSV Results...");
		String peakData = "";		

		for (String wellName : wellData.keySet()) {
			WellData data = wellData.get(wellName);
			List<Double> normalizedValues = data.getNormalizedValues();
         LinkedHashMap<Integer, String> wellDispMoments = data.getMoments();

			peakData += newLine + "Well:, " + wellName + newLine + "Strain:";

         for (int dispNdx = 0; dispNdx < normalizedValues.size(); dispNdx++) {
            peakData += ", disp " + dispNdx + ":";
         }

         peakData += newLine + data.getName() + ", ";

			for (int dispNdx = 0; dispNdx < normalizedValues.size(); dispNdx++) {
				peakData += String.format("%.2f, ", normalizedValues.get(dispNdx));
         }

         String momentData = newLine + ", ";
         String dispData = newLine + ", ";

         for (Integer momentKey : wellDispMoments.keySet()) {
            momentData += String.format("moment %d:, ", momentKey);
            dispData += String.format("%s, ", wellDispMoments.get(momentKey));
         }

         peakData += momentData + dispData;
      }

      peakData += newLine;

		try {
			csvAssistant.write(peakData);
			//System.out.println(peakData);
		}
		catch (java.io.IOException writeErr) {
			System.out.println("csvAssistant could not write to file");
			return;
		}
   }

	public void writeConcisePyroCSVResult(String sourceFile, HashMap<String, WellData> wellData) {
		String peakData = "";		

		for (String wellName : wellData.keySet()) {
			WellData data = wellData.get(wellName);
			List<Double> normalizedValues = data.getNormalizedValues();
         LinkedHashMap<Integer, String> wellDispMoments = data.getMoments();

			peakData += newLine + "File:, " + sourceFile + ", Well:, " + wellName + newLine + "Strain:";

         for (int dispNdx = 0; dispNdx < normalizedValues.size(); dispNdx++) {
            peakData += ", disp " + dispNdx + ":";
         }

         peakData += newLine + data.getName() + ", ";

			for (int dispNdx = 0; dispNdx < normalizedValues.size(); dispNdx++) {
				peakData += String.format("%.2f, ", normalizedValues.get(dispNdx));
         }

         String momentData = newLine + ", ";
         String dispData = newLine + ", ";

         for (Integer momentKey : wellDispMoments.keySet()) {
            momentData += String.format("moment %d:, ", momentKey);
            dispData += String.format("%s, ", wellDispMoments.get(momentKey));
         }

         peakData += momentData + dispData;
      }

      peakData += newLine;

		try {
			csvAssistant.write(peakData);
			//System.out.println(peakData);
		}
		catch (java.io.IOException writeErr) {
			System.out.println("csvAssistant could not write to file");
			return;
		}
   }

	public void writeVerbosePyroCSVResult(HashMap<String, WellData> wellData) {
		String peakData = "";		
		
		for (String wellName : wellData.keySet()) {
			WellData data = wellData.get(wellName);
			List<PeakValue> wellPeaks = data.getPeaks();
			List<Double> normalizedValues = data.getNormalizedValues();
			HashMap<Integer, List<Double>> dropOffs = data.getDropOffCurves();
			
			peakData += newLine + "Sequence:, " + data.getName() + newLine +
						"Well:, " + wellName + newLine + newLine;
			
			peakData += "Moment:, Substance:, SignalValue:, PeakArea:, PeakWidth:, " +
			 "Baseline Offset:, SignalToNoise:, NormalizedSingleValues:," +
			 "DropOffCurve 1:, DropOffCurve 2:, DropOffCurve 3:, DropOffCurve 4: DropOffCurve 5:" +
			 newLine;
			for (int dispNdx = 0; dispNdx < wellPeaks.size(); dispNdx++) {
				PeakValue peak = wellPeaks.get(dispNdx);
				peakData += String.format("%d, %s, %.2f, %.2f, %.2f, " + //Moment, Substance, Signal, Peak Area, Width
							"%.2f, %.2f, ", //baselineOff, signalToNoise, normalizedVals
							peak.getMoment(), data.getSubstance(peak.getMoment()),
							peak.getSignalVal(), peak.getPeakArea(), peak.getPeakWidth(),
							peak.getBaselineOff(), peak.getSigToNoise());
				
				if (normalizedValues.size() > 0)
					peakData += String.format("%.2f, ", normalizedValues.get(dispNdx));
				
				for (int dropOffLevel : dropOffs.keySet()) {
					peakData += String.format("%.2f, ", //DropOffCurves)
							dropOffs.get(dropOffLevel).get(dispNdx));
				}
				
				peakData += newLine;
			}
			
		}
		
		//write to file: momentRow + substanceRow + peakData
		
		try {
			csvAssistant.write(peakData);
			//System.out.println(peakData);
		}
		catch (java.io.IOException writeErr) {
			System.out.println("csvAssistant could not write to file");
			return;
		}
	}
	
	public void finishedWriting() {
		try {
			csvAssistant.close();
		}
		catch (java.io.IOException closeErr) {
			System.out.println("csvAssistant could not close");
			return;
		}
	}
}
