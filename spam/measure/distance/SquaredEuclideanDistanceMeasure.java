package spam.Measures;

import spam.Measures.MeasureType;

import java.util.List;

public class SquaredEuclideanDistanceMeasure implements PyroprintMeasure {

	@Override
	public static Double evaluateDistance(Pyroprint pyroprint_A, Pyroprint pyroprint_B) {
		Double sumSquaredDiffs = 0;

      List<Double> pyrogram_A = pyroprint_A.getPeaks();
      List<Double> pyrogram_B = pyroprint_B.getPeaks();

      if (pyroprint_A.getLength() != pyroprint_B.getLength()) {
         return null;
      }

		for (int pyroNdx = 0; pyroNdx < pyroprint_A.getLength(); pyroNdx++) {
         double squaredDifference = (pyrogram_A.get(pyroNdx) - pyrogram_B.get(pyroNdx));
			sumSquaredDiffs += squaredDifference * squaredDifference;
      }

		return sumSquaredDiffs;
	}

   @Override
   public static MeasureType getMeasureType() {
      return MeasureType.DIST;
   }
}
