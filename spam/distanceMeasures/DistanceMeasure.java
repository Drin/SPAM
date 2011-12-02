package TemporalHClustering.distanceMeasures;

import java.util.List;

public interface DistanceMeasure {
	public void initialDataSweep(List<double[]> data);
	public double evaluateDistance(double[] p1, double[] p2);
}
