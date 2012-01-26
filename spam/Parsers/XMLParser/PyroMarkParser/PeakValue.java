package spam.dataParser.XMLParser.PyroMarkParser;

public class PeakValue {
	private int moment = 0;
	private double signalVal = 0, peakArea = 0, peakWidth = 0,
	 baselineOffset = 0, signalToNoise = 0;
	private final String newLine = System.getProperty("line.separator");
	
	public PeakValue(int newMoment) { moment = newMoment; }
	
	public void setSignalVal(double val) { signalVal = val; }
	public void setPeakArea(double val) { peakArea = val; }
	public void setPeakWidth(double val) { peakWidth = val; }
	public void setBaselineOff(double val) { baselineOffset = val; }
	public void setSigToNoise(double val) { signalToNoise = val; }
	
	public int getMoment() { return moment; }
	public double getSignalVal() { return signalVal; }
	public double getPeakArea() { return peakArea; }
	public double getPeakWidth() { return peakWidth; }
	public double getBaselineOff() { return baselineOffset; }
	public double getSigToNoise() { return signalToNoise; }
	
	public String toString() {
		String tabs = "\t\t";
		String str = tabs + "Moment: " + moment + newLine;
		
		str += tabs + "Signal Value: " + signalVal + newLine + 
			   tabs + "Peak Area: " + peakArea + newLine +
			   tabs + "Peak Width: " + peakWidth + newLine +
			   tabs + "Baseline Offset: " + baselineOffset + newLine +
			   tabs + "Signal To Noise: " + signalToNoise + newLine;
		
		return str;
	}
}
