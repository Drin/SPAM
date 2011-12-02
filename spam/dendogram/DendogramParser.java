package spam.dendogram;

import spam.dataTypes.Isolate;
import spam.dendogram.DendogramTree;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class provides an API for parsing XML files produced by
 * PyroMark pyrosequencers.
 * @author amontana
 * @version 1
 */
public class DendogramParser extends DefaultHandler {
   private Document mDom = null;

   /*
    * Tags for relevant data
    */
   private final String DENDOGRAM_ROOT = "IsolateClusters";
   private final String TAG_DENDOGRAM_TREE = "tree";
   private final String TAG_NODE = "node";
   private final String TAG_LEAF = "leaf";

   /**
    * Class constructor that will initialize Document object from File parameter.
    */
   public DendogramParser(String fileName) {
      File file = new File(fileName);

      try {
         mDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
      }
      catch (SAXException e) {
         e.printStackTrace();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      catch (ParserConfigurationException e) {
         System.err.println("Invalid parser configuration");
      }
      catch (Exception err) {
         System.err.println("unknown error: " + err);
      }

      mDom.getDocumentElement().normalize();
   }

   public DendogramTree parseDendogram(double threshold) {
      DendogramTree dendogram = new DendogramTree();

      if (mDom == null) {
         System.out.println("dom not built.");
         return null;
      }


      NodeList treeList = mDom.getElementsByTagName(TAG_DENDOGRAM_TREE);

      for (int treeNdx = 0; treeNdx < treeList.getLength(); treeNdx++) {
         System.out.println("checking " + treeNdx + "th tree");
         for (List<Isolate> isolateList : parseNodeChildNode((Element) treeList.item(treeNdx), threshold)) {
            dendogram.addIsolateList(isolateList);
         }
      }

      return dendogram;
   }

   public List<List<Isolate>> parseNodeChildNode(Element clusterElem, double threshold) {
      double correlation = Double.valueOf(clusterElem.getAttribute("correlation"));
      List<List<Isolate>> isolateLists = new ArrayList<List<Isolate>>();

      if (correlation >= threshold) {
         List<Isolate> isolateList = new ArrayList<Isolate>();
         NodeList isolateNodes = clusterElem.getElementsByTagName(TAG_LEAF);

         //System.out.println("extracting " + isolateNodes.getLength() + " leaves...");
         for (int isolateNdx = 0; isolateNdx < isolateNodes.getLength(); isolateNdx++) {
            Isolate isolate = getIsolate(isolateNodes.item(isolateNdx));

            if (isolate != null) {
               //System.out.println("extracting isolate " + isolate + "...");
               isolateList.add(isolate);
            }
         }
         //System.out.println("finished extracting leaves...");

         isolateLists.add(isolateList);
      }

      else {
         NodeList nodeList = clusterElem.getChildNodes();

         for (int nodeNdx = 0; nodeNdx < nodeList.getLength(); nodeNdx++) {
            if (!(nodeList.item(nodeNdx) instanceof Element)) {
               continue;
            }

            Element childElement = (Element) nodeList.item(nodeNdx);
            if (childElement.getTagName().equals(TAG_NODE)) {
               for (List<Isolate> isolateList : parseNodeChildNode(childElement, threshold)) {
                  isolateLists.add(isolateList);
               }
            }
            else if (childElement.getTagName().equals(TAG_LEAF)) {
               List<Isolate> isolateList = new LinkedList<Isolate>();
               isolateList.add(getIsolate(childElement));

               isolateLists.add(isolateList);
            }

            //System.out.println("correlation for current node: " + correlation);
            //printIsolateList(tmpIsolateList);
         }
      }
      return isolateLists;
   }

   private Isolate getIsolate(Element isolateElement) {
      String isolateName = isolateElement.getAttribute("isolate");

      if (isolateName == null || isolateName.equals("")) {
         isolateName = isolateElement.getAttribute("data");

         if (isolateName == null || isolateName.equals("")) {
            return null;
         }
      }

      return new Isolate(isolateName);
   }

   private Isolate getIsolate(Node isolateNode) {
      return getIsolate((Element) isolateNode);
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

   private void printIsolateList(List<Isolate> isolateList) {
      System.out.println("==== list of isolates in a cluster ====");

      for (Isolate isolate : isolateList) {
         System.out.println("\t" + isolate);
      }

   }

   public Document getDom() {
      return mDom;
   }
}
