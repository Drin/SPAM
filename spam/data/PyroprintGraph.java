package spam.data;

import spam.data.PyroprintNode;
import spam.data.PyroprintEdge;

import java.util.List;
import java.util.ArrayList;

public class PyroprintGraph {
   //Graph components
   List<PyroprintNode> mNodes = null;
   List<PyroprintEdge> mEdges = null;

   //Graph statistics
   PyroprintNode centralNode = null;
   double mMeanEdgeWeight = -1, mMaxEdgeWeight = -1, mMinEdgeWeight = -1;

   public PyroprintGraph(List<PyroprintNode> initialNodes, List<PyroprintEdge> initialEdges) {
      mNodes = new ArrayList<PyroprintNode>(initialNodes);
      mEdges = new ArrayList<PyroprintEdge>(initialEdges);
   }

   /*=====================================================*/
   /*==================Getter Methods=====================*/
   /*=====================================================*/
   /**
    * Get the {@link List} of {@link PyroprintNode} nodes in this graph.
    * @return A {@link List} of {@link PyroprintNode}s.
    */
   public List<PyroprintNode> getNodes() { return mNodes; }

   /**
    * Get the {@link List} of {@link PyroprintEdge}s in this graph.
    * @return A {@link List} of {@link PyroprintEdge}s.
    */
   public List<PyroprintEdge> getEdges() { return mEdges; }

   /**
    * Get the average edge weight for all edges in this graph.
    * @return A double representing the average edge weight.
    */
   public double getMeanEdgeWeight() { return mMeanEdgeWeight; }

   /**
    * Get the maximum edge weight for all edges in this graph.
    * @return A double representing the maximum edge weight.
    */
   public double getMaxEdgeWeight() { return mMaxEdgeWeight; }

   /**
    * Get the minimum edge weight for all edges in this graph.
    * @return A double representing the minimum edge weight.
    */
   public double getMinEdgeWeight() { return mMinEdgeWeight; }


   /*======================================================*/
   /*==========Methods for Adding to the Graph=============*/
   /*======================================================*/
   /**
    * Add a node and a list of associated edges.
    * @param newNode A {@link PyroprintNode} to be added to the graph as a node.
    * @param nodeEdges A {@link List} of {@link PyroprintEdge}s to be added to
    * the graph as edges.
    * @return The {@link PyroprintGraph} object being called.
    */
   public PyroprintGraph addNode(PyroprintNode newNode, List<PyroprintEdge> nodeEdges) {
      mNodes.add(newNode);
      mEdges.add(nodeEdges);

      return this;
   }

   /**
    * Add a node and a list of associated edges.
    * @param newNode A {@link pyroprint} to be added to the graph as a node.
    * @return The {@link PyroprintGraph} object being called.
    */
   public PyroprintGraph addNode(PyroprintNode newNode) {
      mNodes.add(newNode);

      return this;
   }

   /**
    * Add an edge and a list of associated nodes.
    * @param newEdge A {@link PyroprintEdge} to be added to the graph as an edge.
    * @param edgeNodes A {@link List} of {@link PyroprintNode}s to be added to the
    * graph as nodes.
    * @return The {@link PyroprintGraph} object being called.
    */
   public PyroprintGraph addEdge(PyroprintEdge newEdge, List<PyroprintNode> edgeNodes) {
      mEdges.add(newEdge);
      mNodes.add(edgeNodes);

      return this;
   }

   /**
    * Add a node and a list of associated edges.
    * @param newEdge A {@link PyroprintEdge} to be added to the graph as an edge.
    * @return The {@link PyroprintGraph} object being called.
    */
   public PyroprintGraph addEdge(PyroprintEdge newEdge) {
      mEdges.add(newEdge);

      return this;
   }
}
