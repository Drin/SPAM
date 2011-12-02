package TemporalHClustering.dendogram;

import TemporalHClustering.dataStructures.IsolateSimilarityMatrix;

import TemporalHClustering.dataTypes.Cluster;

import java.util.List;

public class DendogramNode implements Dendogram {
   private String mNodeName = null;
   private double mCorrelation;
   private Dendogram mLeft, mRight;
   private static int id = 0;
   
   public DendogramNode(double corr, Dendogram left, Dendogram right) {
      mNodeName = "" + (id++);
      mCorrelation = corr;
      mLeft = left;
      mRight = right;
   }

   public DendogramNode(String name, double corr, Dendogram left, Dendogram right) {
      mNodeName = name;
      mCorrelation = corr;
      mLeft = left;
      mRight = right;
   }

   public double getCorrelation() {
      return mCorrelation;
   }

   public Dendogram getLeft() {
      return mLeft;
   }

   public Dendogram getRight() {
      return mRight;
   }

   public Cluster toCluster(IsolateSimilarityMatrix matrix) {
      return mLeft.toCluster(matrix).unionWith(mRight.toCluster(matrix));
   }

   /*
    * TODO gotta figure out how to make a diff format usable.
    * specifically just CSV format with every node in a cluster to every node in.. OH do it in CLUSTER
    */
   public String getXML() {
      String xmlStr = String.format("<tree correlation = \"%.02f\" >\n",
       mCorrelation);

      xmlStr += mLeft.toXML("\t");
      xmlStr += mRight.toXML("\t");

      xmlStr += "</tree>\n";
      return xmlStr;
   }

   public String toXML(String spacing) {
      String xmlStr = String.format("%s<node correlation = \"%.02f\">\n",
       spacing, mCorrelation);

      xmlStr += mLeft.toXML(spacing + "\t");
      xmlStr += mRight.toXML(spacing + "\t");

      xmlStr += spacing + "</node>\n";
      return xmlStr;
   }

   public static String toUAGDot(List<Dendogram> dendClusters) {
      String dotStr = "Graph E_ColiCluster {\n";

      for (Dendogram subGraph : dendClusters) {
         dotStr += subGraph.toClusterGraph("\t");
      }

      return dotStr + "}";
   }

   public String toClusterGraph(String spacing) {
      String header = "subgraph cluster_" + mNodeName;
      String subGraph = String.format("%s%s {\n", spacing, header);
      subGraph += defaultStyle(spacing + "\t");

      if (mCorrelation >= 99.7) {
         subGraph += mLeft.toClusterGraph(spacing) + "\n";
         subGraph += mRight.toClusterGraph(spacing) + "\n";
      }

      subGraph += spacing + "}\n";

      if (mCorrelation > 95 && mCorrelation < 99.7) {
         subGraph += mLeft.toClusterGraph(spacing) + "\n";
         subGraph += mRight.toClusterGraph(spacing) + "\n";
      }
      else {
         subGraph += mLeft.toClusterGraph(spacing) + "\n";
         subGraph += mRight.toClusterGraph(spacing) + "\n";
      }

      return subGraph;
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
   public String getGXL(List<Dendogram> graph) {
      String xmlStr = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
      xmlStr += "<gxl>\n";
      xmlStr += String.format("\t<graph id=\"%s\" edgeids=\"true\" " +
       "edgemode = \"undirected\">\n", "ClusterGraph");

      for (Dendogram subGraph : graph) {
         subGraph.getGXLSubGraph("\t\t");
      }

      xmlStr += "\t</graph>\n";
      xmlStr += "</gxl>\n";
      return xmlStr;
   }

   public String getGXLSubGraph(String spacing) {
      String xmlStr = String.format("%s<node id=\"N_%s\">\n" +
       "%s\t<graph id=\"%s\" edgeids=\"true\" edgemode=\"undirected\">\n",
       spacing, mNodeName, spacing, mNodeName);

      xmlStr += getGXLSettings(spacing + "\t\t");

      xmlStr += mLeft.toGXL(spacing + "\t\t")[0];
      xmlStr += mRight.toGXL(spacing + "\t\t")[0];

   }
   */
   /*
    * This method returns a String[] containing two nodes to be connected
    * via an edge (temporarily undirected)
    */
   /*
   public List<String> toGXL(String spacing) {
      List<String> weakNodes = new LinkedList<String>();
      String xmlStr = "";

      List<String> leftNode += mLeft.toGXL(spacing);
      List<String> rightNode += mRight.toGXL(spacing);

      xmlStr += leftNode.get(0); //This is the node tag of the left tree
      xmlStr += rightNode.get(0); //This is the node tag of the right tree

      //get all the weakly connected nodes and make 1 list out of them
      for (int nodeNdx = 1; nodeNdx < leftNode.size(); nodeNdx++) {
         weakNodes.add(leftNode.get(nodeNdx));
         weakNodes.add(rightNode.get(nodeNdx));
      }

      //this will create all the edge tags between weakly connected isolates
      for (int leftNdx = 0; leftNdx < weakNodes.size(); leftNdx++) {
         for (int rightNdx = leftNdx + 1; rightNdx < weakNodes.size(); rightNdx++) {
            //or 99.7 if data is not transformed
            if (mCorrelation < 100) {
               xmlStr += String.format("%s<edge from=\"%s\" to=\"%s\" " +
                "isdirected=\"false\" id=\"%s--%s\">\n</edge>\n", spacing,
                weakNodes.get(leftNdx), weakNodes.get(rightNdx),
                weakNodes.get(leftNdx), weakNodes.get(rightNdx));
            }
         }
      }

      return xmlStr;
   }
   */
