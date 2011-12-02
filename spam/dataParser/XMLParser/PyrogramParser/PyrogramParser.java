package spam.dataParser.XMLParser.PyrogramParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PyrogramParser extends DefaultHandler {
   Document pyrogramDom = null;

   public PyrogramParser(File xmlFile) {
      pyrogramDom = buildDom(xmlFile);
   }

   public String getDispensation() {
      NodeList pyrogramNode = pyrogramDom.getElementsByTagName("pyrogram");

      if (pyrogramNode.item(0).getNodeType() == Node.ELEMENT_NODE) {
         Element pyrogramElement = (Element) pyrogramNode.item(0);

         String dispSeq = pyrogramElement.getAttribute("sequence");
         return dispSeq;
      }

      return null;
   }

   public ArrayList<Double> getData() {
      ArrayList<Double> data = new ArrayList<Double>();
      NodeList dispNodes = pyrogramDom.getElementsByTagName("dispensation");

      for (int dispNdx = 0; dispNdx < dispNodes.getLength(); dispNdx++) {
         if (dispNodes.item(dispNdx).getNodeType() == Node.ELEMENT_NODE) {
            Element dispElement = (Element) dispNodes.item(dispNdx);

            data.add(Double.parseDouble(getNodeValue(dispElement)));
         }
      }

      return data;
   }

	private Document buildDom(File file) {
		Document dom = null;
		try {
			dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			System.out.println("Invalid parser configuration");
			return null;
			//e.printStackTrace();
		}
		dom.getDocumentElement().normalize();
		return dom;
	}

	private Node getNode(Node root, String name) {
		if (root.getNodeName().equals("name"))
			return root;
		
		else if (root != null) {
			NodeList children = root.getChildNodes();
			for (int nodeNdx = 0; nodeNdx < children.getLength(); nodeNdx++) {
				Node testNode = children.item(nodeNdx);
				
				if (testNode.getNodeName().equals(name)) {
					return testNode;
				}
			}
			
			for (int nodeNdx = 0; nodeNdx < children.getLength(); nodeNdx++) {
				Element testNode = (Element) getNode(children.item(nodeNdx), name);
				
				if (testNode.getNodeName().equals(name)) {
					return testNode;
				}
			}
		}
		
		return null;
	}
	
	private String getNodeValue(Node node) {
      if (node == null) {
         System.out.println("null Node Value");
      }
		if (node.getChildNodes() != null) {
			NodeList children = node.getChildNodes();
			
			for (int nodeNdx = 0; nodeNdx < children.getLength(); nodeNdx++) {
				if (children.item(nodeNdx) != null) {
					if (children.item(nodeNdx).getNodeValue() != null) {
						return children.item(nodeNdx).getNodeValue();
					}
				}
			}
		}
		
		return null;
	}
}
