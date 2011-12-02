FILES=Main.java
JAVADOC=documentation

compile: clean SpamGUI

run:
	java spam/gui/SpamGUI

SpamGUI:
	javac -deprecation spam/gui/SpamGUI.java

parseXML:
	java XMLParser/ParseMain ${XMLDIR}

xmlParser: ParseMain.class

ParseMain.class:
	javac -deprecation spam/dataParser/XMLParser/ParseMain.java

xmlTester: PyroMarkTest.class

PyroMarkTest.class:
	javac -deprecation spam/dataParser/XMLParser/PyroMarkTest.java #-d bin

testXML:
	java spam/dataParser/XMLParser/PyroMarkTest data/pyroprints/pyroruns/QualityControlRuns/plates_5-8/

pyroMarkParser: PyroMarkParser.class

PyroMarkParser.class:
	javac -deprecation spam/dataParser/XMLParser/PyroMarkParser/PyroMarkParser.java # -d xmlClasses

dataParser:
	javac spam/dataParser/ParserDriver.java

parseData: dataParser
	java spam/dataParser/ParserDriver data/PilotCorrelation.csv

clean: cleanPyro

document: docSpam

docSpam:
	javadoc -d ${JAVADOC} spam/dataParser/XMLParser/PyroMarkParser/PyroMarkParser.java -link http://download.oracle.com/javase/6/docs/api

cleanPyro:
	rm -rf spam/*.class
	rm -rf spam/*/*.class
	rm -rf spam/*/*/*.class
	rm -rf spam/*/*/*/*.class

cleanDocs:
	rm -rf ${JAVADOC}/*
