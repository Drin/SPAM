package spam.dendogram;

import spam.dataStructures.IsolateSimilarityMatrix;

import spam.Types.Cluster;
import spam.Types.Isolate;

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

   public DendogramLeaf(Dendogram otherDendogram) {
      if (otherDendogram instanceof DendogramLeaf) {
         DendogramLeaf dendLeaf = (DendogramLeaf) otherDendogram;
         super(dendLeaf.mIsolate);
      }

      else {
         System.err.println("unable to make dendogram leaf from " + otherDendogram);
         System.exit(1);
      }
   }

   /*
    * Getter Methods
    */
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

   /*
    * Utility Methods
    */
   public String getXML() {
      String xmlStr = String.format("<tree correlation = \"%.02f\" >\n",
       mCorrelation);

      xmlStr += String.format("\t<leaf correlation = \"%.02f\" " +
       "isolate = \"%s\"/>\n", mCorrelation, toString());

      xmlStr += "</tree>\n";
      return xmlStr;
   }

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

   /*
    * Overridden Methods
    */

   public String toString() {
      return String.format("%s", mIsolate);
   }
}
