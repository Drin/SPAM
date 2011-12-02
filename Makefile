FILES=Main.java
JAVADOC=documentation

compile: clean Main.class

run:
	java polypyro/Main

Main.class:
	javac -deprecation polypyro/Main.java

parseXML:
	java XMLParser/ParseMain ${XMLDIR}

xmlParser: ParseMain.class

ParseMain.class:
	javac -deprecation XMLParser/ParseMain.java

xmlTester: PyroMarkTest.class

PyroMarkTest.class:
	javac -deprecation XMLParser/PyroMarkTest.java #-d bin

testXML:
	java XMLParser/PyroMarkTest sampleData/pyroruns/QualityControlRuns/plates_5-8/

pyroMarkParser: PyroMarkParser.class

PyroMarkParser.class:
	javac -deprecation XMLParser/PyroMarkParser/PyroMarkParser.java # -d xmlClasses

clean: cleanPyro cleanXML

document: docPolyPyro docXMLParser

docPolyPyro:

docXMLParser:
	javadoc -d ${JAVADOC} XMLParser/PyroMarkParser/PyroMarkParser.java -link http://download.oracle.com/javase/6/docs/api

cleanPyro:
	rm -f polypyro/*.class
	rm -f polypyro/*/*.class
	rm -f src/polypyro/*.class
	rm -f src/polypyro/*/*.class

cleanXML:
	rm -f XMLParser/*.class
	rm -f XMLParser/*/*.class

cleanDocs:
	rm -rf ${JAVADOC}/*
