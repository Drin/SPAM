package spam.Measures;

//TODO NOT FINISHED!
public class PearsonSimilarityMeasure implements PyroprintMeasure {

   @Override
   public static Double evaluateDistance(Pyroprint pyro_A, Pyroprint pyro_B) {
      double pyro_A_Mean = pyro_A.getMeanPeak(), pyro_B_Mean = pyro_B.getMeanPeak();
      double pyro_A_StdDev = 0, pyro_B_StdDev = 0;
      double pearsonSum = 0;

      List<Double> pyrogram_A = pyro_A.getPeaks();
      List<Double> pyrogram_B = pyro_B.getPeaks();

      if (pyro_A.getLength() != pyro_B.getLength()) {
         return null;
      }

      for (int peakNdx = 0; peakNdx < pyro_A.getLength(); peakNdx++) {
         double peak_A_Residual = pyrogram_A.get(peakNdx) - pyro_A_Mean;
         double peak_B_Residual = pyrogram_B.get(peakNdx) - pyro_B_Mean;

         pyro_A_StdDev += peak_A_Residual * peak_A_Residual;
         pyro_B_StdDev += peak_B_Residual * peak_B_Residual;

         pearsonSum += peak_A_Residual * peak_B_Residual;
      }

      return pearsonSum / (pyro_A.getLength() *
       Math.sqrt(pyro_A_StdDev) * Math.sqrt(pyro_B_StdDev));
   }

   public static MeasureType getMeasureType() {
      return MeasureType.SIM;
   }
}
