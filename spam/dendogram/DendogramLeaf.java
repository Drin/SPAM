package TemporalHClustering.dendogram;

import TemporalHClustering.dataStructures.IsolateSimilarityMatrix;

import TemporalHClustering.dataTypes.Cluster;
import TemporalHClustering.dataTypes.Isolate;

import java.util.List;

public class DendogramLeaf implements Dendogram {
   private double mCorrelation;
   private Isolate mIsolate;
   private Dendogram mLeft, mRight;

   public DendogramLeaf(Isolate sample) {
      mCorrelation = 100;
      mIsolate = sample;
      mLeft = null;
      mRight = null;
   }

   public double getCorrelation() {
      return mCorrelation;
   }

   public Isolate getIsolate() {
      return mIsolate;
   }

   public Dendogram getLeft() {
      return mLeft;
   }

   public Dendogram getRight() {
      return mRight;
   }

   public Cluster toCluster(IsolateSimilarityMatrix matrix) {
      return new Cluster(matrix, mIsolate);
   }

   public String toString() {
      return String.format("%s", mIsolate);
   }

   public String getXML() {
      String xmlStr = String.format("<tree correlation = \"%.02f\" >\n",
       mCorrelation);

      xmlStr += String.format("\t<leaf correlation = \"%.02f\" " +
       "isolate = \"%s\"/>\n", mCorrelation, toString());

      xmlStr += "</tree>\n";
      return xmlStr;
   }

   /*
    * moving this stuff to cluster output.
    *
   public String getCytoscapeFormat() {
      //column names
      String column[] = new String[] {"e.coli_A", "e.coli_B", "interaction", "correlation"};
      String interactionType = "pp";

      //first row of output
      String cytoStr = String.format("%s,\t%s,\t%s,\t%s\n",
       column[0], column[1], column[2], column[3]);

      cytoStr += String.format("%s,\t%s,\t%s,\t%.03f",
       toString(), "", interactionType, mCorrelation);

      return cytoStr;
   }

   /*
    * This will return 3 things represented in an object:
    *    The name of this isolate and it's correlation.. >__>
    *       This goes at the top of the list to be returned
    *    The interactions between this isolate and isolates in the children of this tree
    *       This goes at the bottom of the list to be returned
    *    The size of the tree including this one
    *       This will be used to know how many 
   public String toCytoscapeFormat() {
      
   }
    */


   public String toXML(String spacing) {
      String xmlStr = String.format("%s<leaf correlation = \"%.02f\"" +
       " isolate = \"%s\"/>\n", spacing, mCorrelation, toString());

      return xmlStr;
   }

   public String toClusterGraph(String spacing) {
      return spacing + mIsolate;
   }

   public String defaultStyle(String spacing) {
      String style = "style=filled;";
      String color = "color=lightgrey;";
      String nodeStyle = "node [style=filled, color=white];";

      return String.format("%s%s\n%s%s\n%s%s\n", spacing, style,
       spacing, color, spacing, nodeStyle);
   }
}





   /*
   public String getGXL() {
      String xmlStr = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
      xmlStr += "<gxl>\n";
      xmlStr += String.format("\t<graph id=\"%s\" edgeids=\"true\" " +
       "edgemode = \"undirected\">\n", mIsolate.getName() + "_Cluster");

      xmlStr += String.format("\t\t<node id=\"%s\">\n", mIsolate.getName());

      xmlStr += "</gxl>\n";
      return xmlStr;
   }
   */

   /*
   public String toGXL(String spacing) {
      String xmlStr = String.format("%s<node id=\"%s\">\n", spacing, mIsolate.getName());

      return xmlStr;
   }
   */
