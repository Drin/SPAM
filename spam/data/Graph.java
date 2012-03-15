package spam.data;

import java.util.List;

public class PyroprintGraph {
   //Graph components
   List<Pyroprint> mNodes = null;
   List<PyroprintEdge> mEdges = null;

   //Graph statistics
   Pyroprint centralNode = null;
   double meanEdgeWeight = -1, maxEdgeWeight = -1, minEdgeWeight = -1;

   public Graph(List<Pyroprint> initialNodes, List<PyroprintEdge> initialEdges) {
      mNodes = new ArrayList<Pyroprint>(initialNodes);
      mEdges = new ArrayList<PyroprintEdge>(initialEdges);
   }
}
