package spam;

/**
 * A class to store the result of a single iteration.
 */
public class IterationResult
{
   public int tupleLength;

   public double sumOfSquaredError;

   public double[][] centroids;
   public int[] clusterCounts;
   public double[] clusterSumOfSquaredError;
   public double[] minDistance;
   public double[] maxDistance;
   public double[] averageDistance;

   public IterationResult(int tupleLength, int k) {
      this.tupleLength = tupleLength;
      sumOfSquaredError = 0.0;

      // Initialize collections.
      centroids = new double[k][];
      clusterCounts = new int[k];
      clusterSumOfSquaredError = new double[k];
      minDistance = new double[k];
      maxDistance = new double[k];
      averageDistance = new double[k];
      for (int i = 0; i < k; i++) {
         centroids[i] = new double[tupleLength];
         clusterCounts[i] = 0;
         clusterSumOfSquaredError[i] = 0.0;
         minDistance[i] = Double.MAX_VALUE;
         maxDistance[i] = 0.0;
         averageDistance[i] = 0.0;
      }
   }

   public String clusterStats(int clusterNdx) {
      String outStr = "";

      outStr += "Centroid: ";
      for (int centroidNdx = 0; centroidNdx < centroids[clusterNdx].length; centroidNdx++) {
         outStr += String.format("%.2f, ", centroids[clusterNdx][centroidNdx]);
      }

      outStr = outStr.substring(0, outStr.length() - 2) + "\n";
      outStr += String.format("Max Distance to Centroid: %.4f\n", maxDistance[clusterNdx]); 
      outStr += String.format("Min Distance to Centroid: %.4f\n", minDistance[clusterNdx]); 
      outStr += String.format("Average Distance to Centroid: %.4f\n", averageDistance[clusterNdx]); 
      outStr += String.format("%d points:\n", clusterCounts[clusterNdx]); 

      return outStr;
   }
}
