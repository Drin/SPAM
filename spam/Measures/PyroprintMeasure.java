package spam.Measures;

import java.util.List;

public interface PyroprintMeasure {
	public static Double evaluateDistance(Pyroprint pyroprint_A, Pyroprint pyroprint_B);
   public static MeasureType getMeasureType();
}
