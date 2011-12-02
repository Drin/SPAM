package spam.outputHandlers;

//TODO the data in this class must be cleared between comparisons because otherwise everytime pyrograms
//are compared there is a "stacking" effect

import java.util.ArrayList;
import java.io.File;

public class MetricsDisplay {
	private static ArrayList<ArrayList<double[]>> comparisonMatrices = new ArrayList<ArrayList<double[]>>();
	private static ArrayList<ArrayList<double[]>> revComparisonMatrices = new ArrayList<ArrayList<double[]>>();
	private static ArrayList<ArrayList<int[]>> differenceMatrices = new ArrayList<ArrayList<int[]>>();
	private static ArrayList<ArrayList<int[]>> revDifferenceMatrices = new ArrayList<ArrayList<int[]>>();
	private static ArrayList<String> dispSeqs = new ArrayList<String>();
	private static ArrayList<Double> forwardMin = new ArrayList<Double>();
	private static ArrayList<Double> forwardMax = new ArrayList<Double>();
	private static ArrayList<Double> forwardMean = new ArrayList<Double>();
	private static ArrayList<Double> reverseMin = new ArrayList<Double>();
	private static ArrayList<Double> reverseMax = new ArrayList<Double>();
	private static ArrayList<Double> reverseMean = new ArrayList<Double>();
	private static ArrayList<ArrayList<String>> dataPoints = new ArrayList<ArrayList<String>>();
	private static File[] dnaFiles = null;
	private static final String newLine = System.getProperty("line.separator");
	private static double totalPearson = 0;
	private static int totalSize = 0;
	
	public static void addDispSeq(String seq) {
		dispSeqs.add(seq);
	}
	
	public static void storeComparisonMatrix(ArrayList<double[]> compMatrix) {
		comparisonMatrices.add(compMatrix);
      System.out.println("comparison matrix list is now of length: " + comparisonMatrices.size());
	}
	
	public static void storeRevComparisonMatrix(ArrayList<double[]> compMatrix) {
		revComparisonMatrices.add(compMatrix);
	}
	
	public static void storeDifferenceMatrix(ArrayList<int[]> diffMatrix) {
		differenceMatrices.add(diffMatrix);
	}
	
	public static void storeRevDifferenceMatrix(ArrayList<int[]> diffMatrix) {
		revDifferenceMatrices.add(diffMatrix);
	}
	
	public static void setDNAFiles(File[] files) {
		dnaFiles = files;
	}
	
	public static void outputMetrics(File metricsFile) {
		//not yet implemented
	}
	
	public static String[] displayMetrics(String mode) {
		String[] metricStrings = new String[2];
		totalSize = 0; totalPearson = 0;
		//double forwardAverage = 0, reverseAverage = 0;
		int strStart = 2;

		String forwardData = calculateMetrics(comparisonMatrices, differenceMatrices, "forward");
		
		//add an extra comma to accomodate the "reverse" column
		for (ArrayList<String> dispRow : dataPoints) {
			dispRow.add(", ");
		}
		
		//forwardAverage = totalSize/totalPearson;
		totalSize = 0; totalPearson = 0;
		
		String reverseData = calculateMetrics(revComparisonMatrices, revDifferenceMatrices, "reverse");
		//reverseAverage = totalSize/totalPearson;

		//necessary strings for building the metrics for each dispensation at top of csv
		String disps = "", minStrs = "", maxStrs = "",
		 meanStrs = "", revFileName = newLine, revMinStrs = newLine,
		 revMaxStrs = newLine, revMeanStrs = newLine, fileName = "";

		//TODO debug statement here
		System.out.println("first dispSeq: " + dispSeqs.get(0) + " last dispSeq is: " + dispSeqs.get(dispSeqs.size() - 1));
		
		for (int metricNdx = 0; metricNdx < dispSeqs.size(); metricNdx++) {
			disps += ", " + dispSeqs.get(metricNdx);
		
			//TODO this is where an index out of bounds error comes from Kathryn's machine
			System.out.println("metricNdx is: " + metricNdx + " and dispSeqs size is: " + dispSeqs.size());
			
			fileName += ", " + dnaFiles[metricNdx].getName().replace(".txt", "");
			if (mode.equals("Forward and Reverse"))
				revFileName = ", " + dnaFiles[metricNdx].getName().replace(".txt", "") + "(reverse)" + revFileName;
			
         //should format as follows:
         //pyrogram length: %d
         //min: %d, %d, ...
         //max: %d, %d, ...
         //mean: %d, %d, ...
         //
         //pyrogram length: %d
         //min: %d, %d, ...
         //max: %d, %d, ...
         //mean: %d, %d, ...
         //
         //this means should have a list of mins, maxs, means where the outermost index is the length of the pyrogram
         //
         //for (int stepSizeNdx = 0; stepSizeNdx < numPyrogramLengthSteps; stepSizeNdx++)
			minStrs = String.format(", min: %.3f", forwardMin.get(metricNdx)) + minStrs;
			maxStrs = String.format(", max: %.3f", forwardMax.get(metricNdx)) + maxStrs;
			meanStrs = String.format(", mean: %.3f", forwardMean.get(metricNdx)) + meanStrs;

			if (mode.equals("Forward and Reverse")) {
				revMinStrs = String.format(", min: %.3f", reverseMin.get(metricNdx)) + revMinStrs;
				revMaxStrs = String.format(", max: %.3f", reverseMax.get(metricNdx)) + revMaxStrs;
				revMeanStrs = String.format(", mean: %.3f", reverseMean.get(metricNdx)) + revMeanStrs;
			}
			
		}
		
		//this will be where the metrics are
		metricStrings[0] = "Forward, " + disps.substring(strStart) + ", Reverse " + disps + newLine;
		
		//this traverses every pearson correlation and outputs them in the format provided by Dr. Kitts
		for (int dispCol = 0; dispCol < dataPoints.get(0).size(); dispCol++) {
			for (int dispRow = 0; dispRow < dataPoints.size() &&
			 dispCol < dataPoints.get(dispRow).size(); dispRow++) {
				//to try and insert a blank field for the "reverse" attribute
				if (dispRow == comparisonMatrices.size()) {
					metricStrings[0] += ", ";
				}
				metricStrings[0] += dataPoints.get(dispRow).get(dispCol);
			}
			metricStrings[0] += newLine;
		}
		

		/*
		metricStrings[0] +=
		 fileName.substring(strStart) + revFileName +
		 minStrs.substring(strStart) + revMinStrs +
		 maxStrs.substring(strStart) + revMaxStrs +
		 meanStrs.substring(strStart) + revMeanStrs;

		
		metricStrings[0] += String.format(
			newLine + "Forward Average Pearson Correlation: %.3f" +
			newLine + "Reverse Average Pearson Correlation: %.3f" +
			newLine + newLine, forwardAverage, reverseAverage);
			
		 */
		
		//this will be where the raw data in table format is
		metricStrings[1] = forwardData + reverseData;
		
		return metricStrings;
	}
	
	private static String calculateMetrics(ArrayList<ArrayList<double[]>> matrix,
	 ArrayList<ArrayList<int[]>> diffMatrix, String dir) {
		String output = "";
		int dataRowOffset = dir.equals("forward") ? 0 : dataPoints.size();
		
		for (int matrixNdx = 0; matrixNdx < matrix.size(); matrixNdx++) {
			double min = 0, max = 0, size = 0, total = 0, differences = 0;
			if (matrixNdx + dataRowOffset >= dataPoints.size())
				dataPoints.add(new ArrayList<String>());
			ArrayList<double[]> corrMatrix = matrix.get(matrixNdx);
			
			//header for each comparison matrix
			output += newLine + "Dispensation Sequence: " + dispSeqs.get(matrixNdx) +
				newLine + dir +	newLine + newLine;

			for (int colNdx = 0; colNdx < corrMatrix.size() && colNdx < dnaFiles.length; colNdx++) {
				output += ", " + dnaFiles[colNdx].getName().replace(".txt", "");
			}
			
			for (int rowNdx = 0; rowNdx < corrMatrix.size() && rowNdx < dnaFiles.length; rowNdx++) {

				if (dnaFiles != null) {
					String tmpFileName = dnaFiles[rowNdx].getName().replace(".txt", "");
					output += (tmpFileName + ", ");
				}
				else {
					output += String.format("%3d | ", (rowNdx + 1));
				}
				
				//System.out.printf("%3d | ", (rowNdx + 1));
				for (int colNdx = 0; colNdx < corrMatrix.get(rowNdx).length; colNdx++) {
					double tmpVal = (corrMatrix.get(rowNdx)[colNdx]);
					                                  
					if (colNdx < rowNdx) {
						output += String.format("%.3f, ", tmpVal);

						dataPoints.get(matrixNdx + dataRowOffset).add(String.format(", %.3f", tmpVal));
						min = min == 0 ? tmpVal : Math.min(min, tmpVal);
						max = Math.max(max, tmpVal);
						total += tmpVal;
						totalPearson += tmpVal;
						size++;
						totalSize++;
					}
				}
				output = output.substring(0, output.length() - 2);
				output += newLine;
				//System.out.println("");
			}
			if (dir.equals("forward")) {
				forwardMin.add(min);
				forwardMax.add(max);
				forwardMean.add(total/size);
				/*
				forwardMetrics += String.format(
					newLine + "Dispensation: %s" +
					newLine + "Min: %.3f" +
					newLine + "Max: %.3f" +
					newLine + "Mean: %.3f" +
					newLine + newLine, dispSeqs.get(matrixNdx), min, max, total/size);
				*/
			}
			else if (dir.equals("reverse")) {
				reverseMin.add(min);
				reverseMax.add(max);
				reverseMean.add(total/size);
				/*
				reverseMetrics += String.format(
					newLine + "Min: %.3f" +
					newLine + "Max: %.3f" +
					newLine + "Mean: %.3f" +
					newLine + newLine, min, max, total/size);
				*/
			}
			
			ArrayList<int []> diffs = diffMatrix.get(matrixNdx);
			
			output += newLine;
			for (int diffCol = 0; diffCol < diffs.size() && diffCol < dnaFiles.length; diffCol++) {
				output += ", " + dnaFiles[diffCol].getName().replace(".txt", "");
			}
			output += newLine;
			
			for (int diffRow= 0; diffRow < diffs.size() && diffRow < dnaFiles.length; diffRow++) {
				if (dnaFiles != null) {
					String tmpFileName = dnaFiles[diffRow].getName().replace(".txt", "");
					output += (tmpFileName + ", ");
				}
				else {
					output += String.format("%3d | ", (diffRow + 1));
				}
				
				for (int colNdx = 0; colNdx < diffs.get(diffRow).length; colNdx++) {
					int tmpVal = (diffs.get(diffRow)[colNdx]);
					                                  
					if (colNdx < diffRow) {
						output += String.format("%d, ", tmpVal);
						//System.out.printf("  %.3f   ", tmpVal);

						differences += tmpVal;
					}
				}
				
				output = output.substring(0, output.length() - 2);
				output += newLine;
			}
			output += String.format(
					newLine + "Total Differences: %.3f" +
					newLine + newLine, differences);
		}
		//System.out.printf("Min: %.3f\nMax: %.3f\nMean: %.3f\n", min, max, total/size);

		return output;
	}
}
