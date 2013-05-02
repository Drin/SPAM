PACKAGE_PATH=com/drin/java
PACKAGE_PRFX=com.drin.java
JAVADOC=documentation

SPAM_MAIN=com.drin.java.ClusterInterface
SPAM_GUI_MAIN=com.drin.java.gui.SpamGUI
SPAM_TEST_MAIN=com.drin.java.test.SPAMEvaluation

CC = javac
ENGINE = java -Xmx8g
JFLAGS = -deprecation -Xlint
NICE = nice

compile: clean spam

spam: ClusterInterface.java
	@echo "-------------------------------"
	find $(PACKAGE_PATH) -name $^ | xargs $(CC) $(JFLAGS) $(CLASSES)
	@echo "-------------------------------"

spamTest: SPAMEvaluation.java
	@echo "-------------------------------"
	find $(PACKAGE_PATH) -name $^ | xargs $(CC) $(JFLAGS) $(CLASSES)
	@echo "-------------------------------"

runSpam:
	@echo "running..."
	$(ENGINE) $(SPAM_MAIN)
	@echo "finished running."

spamGUI: SpamGUI.java
	@echo "-------------------------------"
	find $(PACKAGE_PATH) -name $^ | xargs $(CC) $(JFLAGS) $(CLASSES)
	@echo "-------------------------------"

runGUI:
	@echo "running..."
	$(ENGINE) $(SPAM_GUI_MAIN)
	@echo "finished running."

runTest:
	@echo "running..."
	$(NICE) $(ENGINE) $(SPAM_TEST_MAIN)
	@echo "finished running."

document: docSpam

docSpam:
	javadoc -d ${JAVADOC} spam/dataParser/XMLParser/PyroMarkParser/PyroMarkParser.java -link http://download.oracle.com/javase/6/docs/api

#################################
############ Drivers ############
#################################

SpamGUI.java:

ClusterInterface.java:

SPAMEvaluation.java:

cleanDocs:
	rm -rf ${JAVADOC}/*

clean:
	@echo "-------------------------------"
	@echo "*** Cleaning Files..."
	find $(PACKAGE_PATH) -name "*.class" | xargs rm -f
	@echo "-------------------------------"
