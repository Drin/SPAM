package spam.dataParser.XMLParser;

import java.io.File;
import java.util.ArrayList;

import spam.dataParser.XMLParser.PyroMarkParser.PyroMarkParser;
import spam.dataParser.XMLParser.PyroMarkParser.PyroRun;

public class ParseMain {

	public static void main(String[] args) {
		//PyroRun normalPyroData = PyroMarkParser.parsePyroRun(new File(args[0]), "Normal");
      if (args.length != 1) {
         System.out.println("usage: java ParseMain <XML Directory>");
         System.exit(1);
      }
		File xmlDir = new File(args[0]);
		CsvWriter assistant = new CsvWriter(new File("pyroParse.csv"));
		ArrayList<PyroRun> pyroRunData = new ArrayList<PyroRun>();
      ArrayList<String> fileNames = new ArrayList<String>();
		
		for (int fileNdx = 0; fileNdx < xmlDir.list().length; fileNdx++) {
			if (!xmlDir.listFiles()[fileNdx].isFile() ||
			 !xmlDir.listFiles()[fileNdx].getName().contains("pyrorun"))
				continue;

         fileNames.add(xmlDir.listFiles()[fileNdx].getName());

         //extract xml file name from directory list
			File xmlFile = new File(xmlDir.getAbsolutePath() +
			 File.separator + xmlDir.listFiles()[fileNdx].getName());

         //construct a PyroMarkParser
         PyroMarkParser parser = new PyroMarkParser(xmlFile);
         //tell PyroMarkParser to parse
			pyroRunData.add(parser.parsePyroRun());
		}
		
		for (int pyroData = 0; pyroData < pyroRunData.size(); pyroData++) {
			//assistant.writePyroCSVResult(pyroRunData.get(pyroData).getWellData());
			assistant.writeConcisePyroCSVResult(fileNames.get(pyroData), pyroRunData.get(pyroData).getWellData());
		}
		
		assistant.finishedWriting();
		
		//PyroRun analyzedPyroData = PyroMarkParser.parsePyroRun(new File(args[0]), "Analyzed");
		//assistant.writePyroCSVResult(analyzedPyroData.getWellData());
		
		/*
		System.out.println(normalPyroData);
		System.out.println(analyzedPyroData);
		*/
	}
}
