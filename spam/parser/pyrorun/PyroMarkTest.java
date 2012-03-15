package spam.dataParser.XMLParser;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import spam.dataParser.XMLParser.PyroMarkParser.PyroMarkParser;

public class PyroMarkTest {
	public static void main(String[] args) {
      if (args.length != 1) {
         System.out.println("usage: java PyroMarkTest <XML Directory>");
         System.exit(1);
      }

      /*
       * This grabs all xml files in the specified directory
       */
		File xmlDir = new File(args[0]);
      ArrayList<String> fileNames = new ArrayList<String>();
      ArrayList<PyroMarkParser> parserList = new ArrayList<PyroMarkParser>();

		for (int fileNdx = 0; fileNdx < xmlDir.list().length; fileNdx++) {
			if (!xmlDir.listFiles()[fileNdx].isFile() ||
			 !xmlDir.listFiles()[fileNdx].getName().contains("pyrorun"))
				continue;

         fileNames.add(xmlDir.listFiles()[fileNdx].getName());

         //extract xml file name from directory list
			File xmlFile = new File(xmlDir.getAbsolutePath() +
			 File.separator + xmlDir.listFiles()[fileNdx].getName());

         //construct a PyroMarkParser
			parserList.add(new PyroMarkParser(xmlFile));
		}
		
      /*
       * This uses all of the constructed xmlParsers to print out
       * data from each XML file
       */
		for (int pyroData = 0; pyroData < parserList.size(); pyroData++) {
         System.out.println("Parsing " + xmlDir.listFiles()[pyroData].getName() + "...");
         PyroMarkParser tmpParser = parserList.get(pyroData);

         Map<String, String> wellIds = tmpParser.getWellInfo();
         for (String wellId : wellIds.keySet()) {
            System.out.println("WellId: " + wellId + " contains Isolate: " +
             wellIds.get(wellId) + "\n");
         }


         for (String wellId : wellIds.keySet()) {
            String peakHeightStr = "", compensated = "", dropOffs = "";

            Map<Integer, Double> heightMap = tmpParser.getPeakHeights(wellId);
            Map<Integer, Double> compMap = tmpParser.getCompensatedValues(wellId);
            Map<Integer, List<Double>> dropOffMap = tmpParser.getDropOffCurves(wellId);

            peakHeightStr += String.format("Well %s: \n", wellId);
            compensated += String.format("Well %s: \n", wellId);

            for (Integer position : heightMap.keySet()) {
               peakHeightStr += heightMap.get(position) + ", ";
               compensated += compMap.get(position) + ", ";
            }

            peakHeightStr += "\n";
            compensated += "\n";
            
            for (Integer level : dropOffMap.keySet()) {
               dropOffs += String.format("level %d: \n", level);

               for (Double val : dropOffMap.get(level)) {
                  dropOffs += val + ", ";
               }
               dropOffs += "\n";
            }

            System.out.printf("PeakHeights: %s\nCompensatedValues:%s\nDropOffCurves:%s\n",
             peakHeightStr, compensated, dropOffs);
         }
		}
   }

}
