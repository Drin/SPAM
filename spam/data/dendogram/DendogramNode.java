package spam.dendogram;

import spam.dataStructures.IsolateSimilarityMatrix;

import spam.dataTypes.Cluster;

import java.util.List;

public class DendogramNode implements Dendogram {
   private static int id = 0;

   private String mNodeName = null;

   private double mCorrelation;

   private Dendogram mLeft, mRight;
   
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

   public DendogramNode(Dendogram otherDendogram) {
      if (otherDendogram instanceof DendogramNode) {
         DendogramNode dendNode = (DendogramNode) otherDendogram;
         super(dendNode.mNodeName, dendNode.mCorrelation, dendNode.mLeft, dendNode.mRight);
      }

      else {
         System.err.println("unable to make dendogram node from " + otherDendogram);
         System.exit(1);
      }
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
