package XMLParser.PyroMarkParser;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class PyroRun {
	private HashMap<String, WellData> wellData = null;
	private final String newLine = System.getProperty("line.separator");
	
	public PyroRun() {
		wellData = new HashMap<String, WellData>();
	}

	public void addWellData(String wellNote, WellData data) { wellData.put(wellNote, data); }
	public void setEDisp(String name, int val) { wellData.get(name).setEDisp(val); }
	public void setSDisp(String name, int val) { wellData.get(name).setSDisp(val); }
	
	public void setDisps(String name, LinkedHashMap<Integer, String> newDisps) {
		wellData.get(name).setMoments(newDisps);
	}
	
	public void setPeakVals(String name, List<PeakValue> wellPeaks) {
		wellData.get(name).setPeaks(wellPeaks);
	}
	
	public void setNormalizedVals(String name, List<Double> normalVals) {
		wellData.get(name).setNormalizedVals(normalVals);
	}
	
	public void setDropOffCurves(String name, HashMap<Integer, List<Double>> dropOffs) {
		wellData.get(name).setDropOffCurves(dropOffs);
	}
	
	public void setOriginalClassifications(String name, List<Integer> classifications) {
		wellData.get(name).setOrigClassifications(classifications);
	}
	
	public void setCurrentClassifications(String name, List<Integer> classifications) {
		wellData.get(name).setCurrClassifications(classifications);
	}
	
	public Set<String> getWells() {
		return wellData.keySet();
	}
	
	public HashMap<String, WellData> getWellData() { return wellData; }
	
	public String toString() {
		String strForm = "";
		
		for (String wellName : wellData.keySet()) {
			strForm += wellName + ": " + wellData.get(wellName) + newLine;
		}
		
		return strForm + newLine;
	}
}
