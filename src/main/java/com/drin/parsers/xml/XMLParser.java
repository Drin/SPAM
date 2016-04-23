package com.drin.parsers.xml;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.xml.sax.helpers.DefaultHandler;

import java.io.File;

public class XMLParser {
   private SAXParserFactory mSAXFactory;
   private SAXParser mSAXParser;

   public XMLParser() {
      mSAXFactory = SAXParserFactory.newInstance();
      try {
         mSAXParser = mSAXFactory.newSAXParser();
      }
      catch (javax.xml.parsers.ParserConfigurationException err) {
         System.err.println("Error creating SAX Parser");
         err.printStackTrace();
      }
      catch (org.xml.sax.SAXException saxErr) {
         System.err.println("SAX parsing exception:");
         saxErr.printStackTrace();
      }
   }

   public void parse(File xmlFile, DefaultHandler handler) {
      try {
         mSAXParser.parse(xmlFile, handler);
      }

      catch (IllegalArgumentException argErr) {
         System.err.printf("File '%s' is null\n", xmlFile);
         argErr.printStackTrace();
      }

      catch (java.io.IOException ioErr) {
         System.err.printf("IOException while parsing %s\n", xmlFile);
      }

      catch (org.xml.sax.SAXException saxErr) {
         System.err.println("SAX parsing exception:");
         saxErr.printStackTrace();
      }
   }
}
