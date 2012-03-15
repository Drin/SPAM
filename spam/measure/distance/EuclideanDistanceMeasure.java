package spam.Measures;

import spam.Measures.SquaredEuclideanDistanceMeasure;

public class EuclideanDistanceMeasure extends SquaredEuclideanDistanceMeasure {

	@Override
	public static Double evaluateDistance(Pyroprint pyro_A, Pyroprint pyro_B) {
      Double squaredEuclideanDist = super.evaluateDistance(pyro_A, pyro_B);

      return squaredEuclideanDist == null ? null : Math.sqrt(squaredEuclideanDist);
	}
}
