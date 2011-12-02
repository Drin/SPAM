package spam.dataParser.XMLParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import spam.dataParser.XMLParser.PyroMarkParser.PeakValue;
import spam.dataParser.XMLParser.PyroMarkParser.WellData;

/*
 * will write data structure contents in a given orientation
 */
public class PolyPyroFormatWriter {
	private File csv = null;
	private BufferedWriter csvAssistant = null;
	private String newLine = System.getProperty("line.separator");
	
	public PolyPyroFormatWriter(File csvFile) {
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
	
	public void writePyroCSVResult(HashMap<String, WellData> wellData, String wellId) {
		String peakData = "";		
		
		for (String wellName : wellData.keySet()) {
			if (!wellName.equals(wellId))
				continue;
			
			WellData data = wellData.get(wellName);
			ArrayList<PeakValue> wellPeaks = data.getPeaks();
			//mostly just need normalized Values
			ArrayList<Double> normalizedValues = data.getNormalizedValues();
			
			/*
			peakData += newLine + "Sequence:, " + data.getName() + newLine +
						"Well:, " + wellName + newLine + newLine;
			*/
			
			for (int dispNdx = 0; dispNdx < wellPeaks.size(); dispNdx++) {
				PeakValue peak = wellPeaks.get(dispNdx);
				peakData += String.format("%s, ", data.getSubstance(peak.getMoment()));
				
				
			}
			
			peakData += newLine;
			
			for (int dispNdx = 0; dispNdx < wellPeaks.size(); dispNdx++) {
				peakData += String.format("%.2f, ", normalizedValues.get(dispNdx));
				
			}
			
			peakData += newLine;
			
		}
		
		try {
			csvAssistant.write(peakData);
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
