package spam.dataTypes;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class PyroprintNode {
   Pyroprint mPyroprint = null;
   Map<PyroprintNode, PyroprintEdge> mNeighborhood = null;

   public PyroprintNode(Pyroprint pyroprint) {
      mPyroprint = pyroprint;
      mNeighborhood = new HashMap<PyroprintNode, PyroprintEdge>();
   }

   public Set<PyroprintNode> getNeighbors() {
      return mNeighborhood.keySet();
   }

   public PyroprintEdge getEdge(PyroprintNode neighbor) {
      return mNeighborhood.get(neighbor);
   }

   public Map<PyroprintNode, PyroprintEdge> getNeighborhood() {
      return mNeighborhood;
   }

   public PyroprintNode addEdge(PyroprintNode newNeighbor, PyroprintEdge newEdge) {
      mNeighborhood.put(newNeighbor, newEdge);

      return this;
   }

   public int hashCode() {
      return mPyroprint.hashCode();
   }
}
