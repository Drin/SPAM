package kNearest;

import java.io.File;
import java.util.Scanner;
import java.util.HashMap;

public class CsvParser {
	File file = null;

	public CsvParser(String parseFile) {
		file = new File(parseFile);
	}

	public String[] parseColumns() {
		Scanner fileParser = null;

		try {
			fileParser = new Scanner(file);
		}
		catch (java.io.FileNotFoundException fileErr) {
			System.out.println("could not find file: " + file);
			return null;
		}

      return fileParser.nextLine().replaceAll("\"", "").split(",");
   }

	public HashMap<String, HashMap<String, Double>> extractData() {
		HashMap<String, HashMap<String, Double>> csvData = new HashMap<String, HashMap<String, Double>>();

      String[] strains = parseColumns();

		Scanner fileParser = null;

		try {
			fileParser = new Scanner(file);
         fileParser.nextLine();
		}
		catch (java.io.FileNotFoundException fileErr) {
			System.out.println("could not find file: " + file);
			return null;
		}
		
		while (fileParser.hasNextLine()) {
			String[] strArr = fileParser.nextLine().split(",");
         String strainKey = strArr[0].replaceAll("\"", "");

			for (int dataNdx = 1; dataNdx < strArr.length; dataNdx++) {
            HashMap<String, Double> subMap = null;

            if (csvData.containsKey(strains[dataNdx])) {
               subMap = csvData.get(strains[dataNdx]);
            }
            else {
               subMap = new HashMap<String, Double>();
            }

            subMap.put(strainKey, Double.parseDouble(strArr[dataNdx]));
            
            csvData.put(strains[dataNdx], subMap);
         }
		}

		return csvData;
	}
	
}
