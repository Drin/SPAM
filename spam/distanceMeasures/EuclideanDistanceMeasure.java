package TemporalHClustering.distanceMeasures;

public class EuclideanDistanceMeasure extends SquaredEuclideanDistanceMeasure {
	@Override
	public double evaluateDistance(double[] p1, double[] p2) {
		return Math.sqrt(super.evaluateDistance(p1, p2));
	}
}
