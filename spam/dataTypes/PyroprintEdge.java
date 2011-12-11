package spam.dataTypes;

public class PyroprintEdge {
   private IsolateRegion mRegion = null;
   private double mWeight = -1;

   public PyroprintEdge(IsolateRegion region, double weight) {
      mRegion = region;
      mWeight = weight;
   }

   public IsolateRegion getRegion() {
      return mRegion;
   }

   public double getWeight() {
      return mWeight;
   }
}
