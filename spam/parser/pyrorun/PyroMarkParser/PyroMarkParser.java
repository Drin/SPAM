package spam.dataParser.XMLParser.PyroMarkParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class PyroMarkParser extends DefaultHandler {
   private Document mDom = null;
   private PyroRun mPyroRun = null;

   /*
    * Tags for relevant data
    */
   private final String TAG_WELL_INFO = "WellInfo";
   private final String TAG_WELL_DATA = "WellData";
   private final String TAG_WELL_ANALYSIS = "WellAnalysisMethodResults";
   private final String TAG_NOTE = "Note";
   private final String TAG_DISPENSATION = "Dispensation";
   private final String TAG_NORMALIZED = "NormalizedSingelValues";
   private final String TAG_ORIG_CLASS = "OriginalClassification";
   private final String TAG_CURR_CLASS = "CurrentClassification";
   private final String TAG_DROPOFF = "DropOffCurve";
   private final String TAG_INTENSITY = "Intensity";

   /**
    * Class constructor that will initialize Document object from File parameter.
    */
   public PyroMarkParser(File file) {
      mPyroRun = new PyroRun();

      try {
         //System.out.println("setting document...");
         mDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
         //System.out.println("document set");
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

   public PyroRun parsePyroRun() {
      if (mDom == null) {
         System.out.println("dom not built.");
         return null;
      }

      populateWellNames(mPyroRun);
      analyzeWellDatum(mPyroRun);
      analyzeWellAnalysis(mPyroRun);

      return mPyroRun;
   }

   /**
    * Parses WellInfo tags to associate a well identifier (i.e. "A1") with a host organism.
    * Host organism name is contained in the Note tag.
    *
    * @return A mapping from well Ids to their contained isolate Ids
    */
   public HashMap<String, String> getWellInfo() {
      if (mDom == null) {
         System.out.println("dom not built.");
         return null;
      }

      HashMap<String, String> headerMap = new HashMap<String, String>();
      NodeList initialWells = mDom.getElementsByTagName(TAG_WELL_INFO);
      
      for (int nodeNdx = 0; nodeNdx < initialWells.getLength(); nodeNdx++) {
         if (initialWells.item(nodeNdx).getNodeType() == Node.ELEMENT_NODE) {
            Element wellElement = (Element) initialWells.item(nodeNdx);
            NodeList organismNodes = wellElement.getElementsByTagName(TAG_NOTE);
            Node organismNode = (Node) organismNodes.item(0).getChildNodes().item(0);

            String wellDataName = wellElement.getAttribute("WellNr");
            String hostOrganism = organismNode.getNodeValue();

            if ((wellDataName != null || wellDataName != "") && hostOrganism != null)
               headerMap.put(wellDataName, hostOrganism);
         }
      }

      return headerMap;
   }
   
   /**
    * Consolidates pyrogram data in a well's "WellData" node.
    *
    * @param pyroData An object which maintains all of a well's data.
    */
   public void analyzeWellDatum(PyroRun pyroData) {
      NodeList wellDataList = mDom.getElementsByTagName(TAG_WELL_DATA);

      for (int nodeNdx = 0; nodeNdx < wellDataList.getLength(); nodeNdx++) {
         Element wellDataElement = (Element) wellDataList.item(nodeNdx);
         String wellName = wellDataElement.getAttribute("WellNr");
         
         Node sDispNode = getNode(getNode(wellDataElement, "SDispensation"), "Moment");
         int sDisp = Integer.parseInt(getNodeValue(sDispNode));
         
         //oddly enough it seemed that combining these two node methods together caused errors
         Node eDispNode = getNode(getNode(wellDataElement, "EDispensation"), "Moment");
         int eDisp = Integer.parseInt(getNodeValue(eDispNode));
         
         LinkedHashMap<Integer, String> dispMoments = getDispensationMoments(wellDataElement);
         List<PeakValue> momentPeaks = getMomentPeaks(wellDataElement);
         
         pyroData.setSDisp(wellName, sDisp);
         pyroData.setEDisp(wellName, eDisp);
         pyroData.setDisps(wellName, dispMoments);
         pyroData.setPeakVals(wellName, momentPeaks);

         //pyroRun.setIntensities(wellName, intensityArr);
      }
   }
   
   /**
    * Consolidates important pyrogram data in a well's "WellAnalysisMethodResults" node.
    *
    * @param pyroData An object which maintains all of a well's data.
    */
   public void analyzeWellAnalysis(PyroRun pyroData) {
      NodeList wellDataList = mDom.getElementsByTagName(TAG_WELL_ANALYSIS);
      
      for (int nodeNdx = 0; nodeNdx < wellDataList.getLength(); nodeNdx++) {
         Element wellAnalysisElement = (Element) wellDataList.item(nodeNdx);
         String wellName = wellAnalysisElement.getAttribute("WellNr");
      
         List<Double> normalizedList = getNormalizedSingleValues(wellAnalysisElement);
         List<Integer> originalClassList = getOriginalClassifications(wellAnalysisElement);
         List<Integer> currentClassList = getCurrentClassifications(wellAnalysisElement);
         HashMap<Integer, List<Double>> dropOffCurves = getDropOffCurves(wellAnalysisElement);
   
         pyroData.setNormalizedVals(wellName, normalizedList);
         pyroData.setDropOffCurves(wellName, dropOffCurves);
         pyroData.setOriginalClassifications(wellName, originalClassList);
         pyroData.setCurrentClassifications(wellName, currentClassList);
      }
   }
   
   /**
    * Populates the well names of a PyroRun object.
    *
    * @param pyroData An object which maintains all of a well's data.
    */
   private void populateWellNames(PyroRun pyroData) {
      NodeList initialWells = mDom.getElementsByTagName(TAG_WELL_INFO);

      for (int nodeNdx = 0; nodeNdx < initialWells.getLength(); nodeNdx++) {
         if (initialWells.item(nodeNdx).getNodeType() == Node.ELEMENT_NODE) {
            Element wellElement = (Element) initialWells.item(nodeNdx);

            String wellDataName = wellElement.getAttribute("WellNr");
            Node noteNode = wellElement.getElementsByTagName(TAG_NOTE).item(0);
            Node noteContentNode = noteNode.getChildNodes().item(0);

            WellData well = new WellData(noteContentNode.getNodeValue());
            //System.out.println("\nwell is: " + well);
            pyroData.addWellData(wellDataName, well);
         }
      }
   }
   
   /**
    * Constructs a {@link LinkedHashmap} of {Moment, Substance} pairings
    * representing each dispensation for each well; only present in analyzed pyrorun files.
    *
    * @param wellDataElem A particular well's "WellData" node in the XML file.
    * @return A {@link LinkedHashMap} containing {Moment, Substance} mappings representing the
    * "Moment" node value assigned to a dispensation,
    * paired with a "Substance" node value (nucleotide) assigned to the "Moment".
    */
   public LinkedHashMap<Integer, String> getDispensationMoments(Element wellDataElem) {
      final int momentNdx = 1, substanceNdx = 3, valueNdx = 0;
      LinkedHashMap<Integer, String> dispMap = new LinkedHashMap<Integer,String>();
      NodeList dispNodes = wellDataElem.getElementsByTagName(TAG_DISPENSATION);
      
      for (int nodeNdx = 0; nodeNdx < dispNodes.getLength(); nodeNdx++) {
         //it seems that some adjacent nodes are full of nothing?
         Node moment = dispNodes.item(nodeNdx).getChildNodes().item(momentNdx);
         Node substance = dispNodes.item(nodeNdx).getChildNodes().item(substanceNdx);
         
         int momentVal = Integer.parseInt(moment.getChildNodes().item(valueNdx).getNodeValue());
         String substanceVal = substance.getChildNodes().item(valueNdx).getNodeValue(); 
         
         dispMap.put(momentVal, substanceVal);
      }
      
      return dispMap;
   }

   /**
    * Convenience method for retrieving each well's dispensation
    *
    * @param wellId The string ID of a well in a pyrorun i.e. "A1".
    * @return A mapping of each index of a dispensation to the nucleotide at that index for the given well.
    */
   public Map<Integer, String> getWellDispensations(String wellId) {
      Map<Integer, String> wellDispMap = new HashMap<Integer, String>();
      NodeList wellDataList = mDom.getElementsByTagName(TAG_WELL_DATA);

      for (int nodeNdx = 0; nodeNdx < wellDataList.getLength(); nodeNdx++) {
         Element wellDataElement = (Element) wellDataList.item(nodeNdx);
         String wellName = wellDataElement.getAttribute("WellNr");

         if (wellName.equals(wellId)) {
            Map<Integer, String> dispMoments = getDispensationMoments(wellDataElement);
            int dispNdx = 0;

            for (Integer moment : dispMoments.keySet()) {
               wellDispMap.put(dispNdx++, dispMoments.get(moment));
            }

            return wellDispMap;
         }

      }

      return null;
   }
   
   /**
    * Constructs a list of ${@link PeakValue} objects from the XML file for a given well.
    * Only present in analyzed pyrorun files.
    *
    * @param wellDataElem A particular well's "WellData" node in the XML file.
    * @return A list of ${@link PeakValue} Objects representing the "PeakValues" child node
    * of a "Dispensation" node.
    */
   public List<PeakValue> getMomentPeaks(Element wellDataElem) {
      //no idea why peakNdx is 5, these values must have been grabbed while using the debugger
      final int momentNdx = 1, peakNdx = 5, valueNdx = 0;
      List<PeakValue> momentPeaks = new ArrayList<PeakValue>();
      NodeList dispNodes = wellDataElem.getElementsByTagName(TAG_DISPENSATION);
      
      for (int nodeNdx = 0; nodeNdx < dispNodes.getLength(); nodeNdx++) {
         //it seems that some adjacent nodes are full of nothing?
         Node moment = dispNodes.item(nodeNdx).getChildNodes().item(momentNdx);
         Node peakVals = dispNodes.item(nodeNdx).getChildNodes().item(peakNdx);
         
         int momentVal = Integer.parseInt(moment.getChildNodes().item(valueNdx).getNodeValue());
         
         PeakValue peakVal = new PeakValue(momentVal);
         ///this represents the "PeakValues" child node of a "Dispensation" node.
         NodeList peakNodes = peakVals.getChildNodes();

         //this grabs the "SignalValue", "PeakArea", "PeakWidth", "BaselineOffset",
         //"SignalToNoise" child node values of the "PeakValues" node and constructs
         //a PeakValue object containing grabbed data.
         for (int peakNode = 0; peakNode < peakNodes.getLength(); peakNode ++) {
            Node tmpNode = peakNodes.item(peakNode);

            if (tmpNode.getNodeName().equals("#text"))
               continue;
            
            double nodeVal = Double.parseDouble(getNodeValue(tmpNode));
            
            //could probably be changed to use reflection and be more concise
            if (tmpNode.getNodeName().equals("SignalValue"))
               peakVal.setSignalVal(nodeVal);

            else if (tmpNode.getNodeName().equals("PeakArea"))
               peakVal.setPeakArea(nodeVal);

            else if (tmpNode.getNodeName().equals("PeakWidth"))
               peakVal.setPeakWidth(nodeVal);

            else if (tmpNode.getNodeName().equals("BaselineOffset"))
               peakVal.setBaselineOff(nodeVal);

            else if (tmpNode.getNodeName().equals("SignalToNoise"))
               peakVal.setSigToNoise(nodeVal);
         } 
         
         momentPeaks.add(peakVal);
      }
      
      return momentPeaks;
   }

   /**
    * Convenience method for retrieving a mapping of wellIds to Peak Heights.
    *
    * @param wellId The string ID of a well in a pyrorun i.e. "A1".
    * @return A mapping of each position to its peak height for that well.
    */
   public Map<Integer, Double> getPeakHeights(String wellId) {
      Map<Integer, Double> peakHeightMap = new LinkedHashMap<Integer, Double>();
      NodeList wellDataList = mDom.getElementsByTagName(TAG_WELL_DATA);
      
      for (int nodeNdx = 0; nodeNdx < wellDataList.getLength(); nodeNdx++) {
         Element wellAnalysisElement = (Element) wellDataList.item(nodeNdx);
         String wellName = wellAnalysisElement.getAttribute("WellNr");

         if (wellName.equals(wellId)) {
            List<PeakValue> peakList = getMomentPeaks(wellAnalysisElement);

            for (int peakNdx = 0; peakNdx < peakList.size(); peakNdx++) {
               peakHeightMap.put(peakNdx, peakList.get(peakNdx).getSignalVal());
            }
         }
      }

      return peakHeightMap;
   }

   /**
    * Constructs a list of light intensity values (I think?) from the XML file for a given well.
    * Only present in non analyzed pyrorun files.
    *
    * @param wellDataElem A particular well's "WellData" node in the XML file.
    * @return A list of Doubles representing light intensity values.
    */
   public List<Double> getIntensities(Element wellDataElem) {
      List<Double> intensities = new ArrayList<Double>();
      NodeList intenseNodes = wellDataElem.getElementsByTagName(TAG_INTENSITY);
      
      //number of nodes is 1, checked in debugger
      for (int nodeNdx = 0; nodeNdx < intenseNodes.getLength(); nodeNdx++) {
         Element intensity = (Element) intenseNodes.item(nodeNdx);
         
         String intensityList = intensity.getChildNodes().item(0).getNodeValue();
         
         String[] intensityValues = intensityList.split(";");
         
         for (int strNdx = 0; strNdx < intensityValues.length; strNdx++)
            intensities.add(Double.parseDouble(intensityValues[strNdx]));
      }
      
      return intensities;
   }
   
   /**
    * Constructs a list of normalized peak values from the XML file for a given well.
    * Only present in analyzed pyrorun files.
    *
    * @param wellAnalysis A particular well's "WellAnalysisMethodResults" node in the XML file.
    * @return The normalized/compensated peak values as an ArrayList.
    */
   public List<Double> getNormalizedSingleValues(Element wellAnalysis) {
      List<Double> normalizedVals = new ArrayList<Double>();
      NodeList normalizedSingleVals = wellAnalysis.getElementsByTagName(TAG_NORMALIZED);
 
      //number of nodes is 1, checked in debugger
      for (int nodeNdx = 0; nodeNdx < normalizedSingleVals.getLength(); nodeNdx++) {
         Element normalizedElement = (Element) normalizedSingleVals.item(nodeNdx);
         
         if (normalizedElement.getChildNodes().item(0) == null)
            continue;
         
         String normalizedValStr = normalizedElement.getChildNodes().item(0).getNodeValue();
         
         String[] normalizedValArr = normalizedValStr.split(";");
         
         for (int strNdx = 0; strNdx < normalizedValArr.length; strNdx++)
            normalizedVals.add(Double.parseDouble(normalizedValArr[strNdx]));
      }
      
      return normalizedVals;
   }

   /**
    * Convenience method for retrieving compensated peak values (inferred number of
    * nucleotides given a peak height).
    *
    * @param wellId A string Id of a well in a given pyrorun i.e. "A1".
    * @return A mapping of each position in a pyroprint's list of compensated values
    * to its compensated value.
    */
   public Map<Integer, Double> getCompensatedValues(String wellId) {
      Map<Integer, Double> compensatedMap = new LinkedHashMap<Integer, Double>();
      NodeList wellDataList = mDom.getElementsByTagName(TAG_WELL_ANALYSIS);
      
      for (int nodeNdx = 0; nodeNdx < wellDataList.getLength(); nodeNdx++) {
         Element wellAnalysisElement = (Element) wellDataList.item(nodeNdx);
         String wellName = wellAnalysisElement.getAttribute("WellNr");

         if (wellName.equals(wellId)) {
            List<Double> compensatedList = getNormalizedSingleValues(wellAnalysisElement);

            for (int valNdx = 0; valNdx < compensatedList.size(); valNdx++) {
               compensatedMap.put(valNdx, compensatedList.get(valNdx));
            }
         }
      }

      return compensatedMap;
   }
   
   /**
    * Constructs a map of drop off curve values from the XML file for a given well.
    * A drop off curve value is a value calculated by the PyroMark sequencer and is
    * applied to a peak value to determine a compensated value.
    * Only present in analyzed pyrorun files.
    *
    * @param wellAnalysis A particular well's "WellAnalysisMethodResults" node in the XML file.
    * @return A mapping of drop off levels (what the compensated value should be) to a list of
    * drop off values.
    */
   public HashMap<Integer, List<Double>> getDropOffCurves(Element wellAnalysis) {
      HashMap<Integer, List<Double>> dropOffs = new LinkedHashMap<Integer, List<Double>>();
      NodeList dropOffCurves = wellAnalysis.getElementsByTagName(TAG_DROPOFF);
      
      for (int nodeNdx = 0; nodeNdx < dropOffCurves.getLength(); nodeNdx++) {
         Element dropOffCurve = (Element) dropOffCurves.item(nodeNdx);
         String dropOffCurveValList = dropOffCurve.getAttribute("Values");
         String[] vals = dropOffCurveValList.split(";");
         List<Double> dropOffList = new ArrayList<Double>(vals.length);
         
         for (int valNdx = 0; valNdx < vals.length; valNdx++) {
            dropOffList.add(Double.parseDouble(vals[valNdx]));
         }
         
         dropOffs.put(Integer.parseInt(dropOffCurve.getAttribute("Level")), dropOffList);
      }
      
      return dropOffs;
   }

   /**
    * Convenience method for retrieving drop off curve values for a given wellId and level
    *
    * @param wellId A string id for a well in a given pyrorun i.e. "A1".
    * @return a mapping of normalization level of a drop off curve (which drop off curve)
    * to its list of drop off curves.
    */
   public Map<Integer, List<Double>> getDropOffCurves(String wellId) {
      Map<Integer, List<Double>> dropOffMap = null;
      NodeList wellDataList = mDom.getElementsByTagName(TAG_WELL_ANALYSIS);
      
      for (int nodeNdx = 0; nodeNdx < wellDataList.getLength(); nodeNdx++) {
         Element wellAnalysisElement = (Element) wellDataList.item(nodeNdx);
         String wellName = wellAnalysisElement.getAttribute("WellNr");

         if (wellName.equals(wellId)) {
            dropOffMap = getDropOffCurves(wellAnalysisElement);
         }
      }

      return dropOffMap;
   }
   
   /**
    * Constructs a list of original classifications from the XML file for a given well.
    * Unfortunately I am not sure what an "original classification" is, but it is obtained
    * from the "OriginalClassification" node for each well analysis.
    * Only present in analyzed pyrorun files.
    *
    * @param wellAnalysis A particular well's "WellAnalysisMethodResults" node in the XML file.
    * @return A list of Integers representing original classifications.
    */
   public List<Integer> getOriginalClassifications(Element wellAnalysis) {
      List<Integer> classificationsList = new ArrayList<Integer>();
      NodeList origClasses = wellAnalysis.getElementsByTagName(TAG_ORIG_CLASS);
      
      //number of nodes is 1, checked in debugger
      for (int nodeNdx = 0; nodeNdx < origClasses.getLength(); nodeNdx++) {
         Element intensity = (Element) origClasses.item(nodeNdx);
         
         if (intensity.getChildNodes().item(0) == null)
            continue;
         
         String origClassStr = intensity.getChildNodes().item(0).getNodeValue();
         
         String[] classificationArr = origClassStr.split(";");
         
         for (int strNdx = 0; strNdx < classificationArr.length; strNdx++)
            classificationsList.add(Integer.parseInt(classificationArr[strNdx]));
      }
      
      return classificationsList;
   }
   
   /**
    * Constructs a list of current classifications from the XML file for a given well.
    * Unfortunately I am not sure what a "current classification" is, but it is obtained
    * from the "CurrentClassification" node for each well analysis.
    * Only present in analyzed pyrorun files.
    *
    * @param wellAnalysis A particular well's "WellAnalysisMethodResults" node in the XML file.
    * @return A list of Integers representing current classifications.
    */
   public List<Integer> getCurrentClassifications(Element wellAnalysis) {
      List<Integer> classificationList = new ArrayList<Integer>();
      NodeList currClasses = wellAnalysis.getElementsByTagName(TAG_CURR_CLASS);
      
      //number of nodes is 1, checked in debugger
      for (int nodeNdx = 0; nodeNdx < currClasses.getLength(); nodeNdx++) {
         Element classElement = (Element) currClasses.item(nodeNdx);
         
         if (classElement.getChildNodes().item(0) == null)
            continue;
         
         String classificationStr = classElement.getChildNodes().item(0).getNodeValue();
         
         String[] classifications = classificationStr.split(";");
         
         for (int strNdx = 0; strNdx < classifications.length; strNdx++)
            classificationList.add(Integer.parseInt(classifications[strNdx]));
      }
      
      return classificationList;   
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

   public Document getDom() {
      return mDom;
   }
}
