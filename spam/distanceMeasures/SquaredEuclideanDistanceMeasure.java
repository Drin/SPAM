package TemporalHClustering.distanceMeasures;

import java.util.List;

public class SquaredEuclideanDistanceMeasure implements DistanceMeasure {
	@Override
	public double evaluateDistance(double[] p1, double[] p2) {
		double acc = 0;
		for (int i = 0; i < p1.length; i++)
			acc += (p1[i] - p2[i]) * (p1[i] - p2[i]);
		return acc;
	}

	@Override
	public void initialDataSweep(List<double[]> data) { }
}
