package spam.data;

import spam.data.PyroprintRegion;
import spam.data.Pyroprint;
import spam.data.PyroprintCorrelation;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class PyroprintSimilarityMatrix {
   private Map<Pyroprint, Map<Pyroprint, PyroprintCorrelation>> mSimilarityMatrix = null;

   //mPyroprintMappingping maps days to the list of isolates collected on that day
   private Map<String, Map<Integer, List<Pyroprint>>> mPyroprintMapping = null;

   public PyroprintSimilarityMatrix() {
      super();

      mSimilarityMatrix = new HashMap<Pyroprint, Map<Pyroprint, PyroprintCorrelation>>();
      mPyroprintMapping = new HashMap<String, Map<Integer, List<Pyroprint>>>();
   }

   public int size() {
      return mSimilarityMatrix.size();
   }

   public Map<String, Map<Integer, List<Pyroprint>>> getPyroprintMap() {
      return mPyroprintMapping;
   }

   public Map<Pyroprint, Map<Pyroprint, PyroprintCorrelation>> getSimilarityMatrix() {
      return mSimilarityMatrix;
   }

   public PyroprintCorrelation getCorrelation(Pyroprint pyro1, Pyroprint pyro2) {
      if (!mSimilarityMatrix.containsKey(pyro1) ||
       !mSimilarityMatrix.get(pyro1).containsKey(pyro2)) {
         return null;
      }

      return mSimilarityMatrix.get(pyro1).get(pyro2);
   }

   public double getCorrelationVal(Pyroprint pyro1, Pyroprint pyro2) {
      PyroprintCorrelation correlation = getCorrelation(pyro1, pyro2);

      if (correlation == null) {
         correlation = getCorrelation(pyro2, pyro1);
      }

      return correlation != null ? correlation.getCorrelation() : 0;
   }

   public void transformCorrelation(Pyroprint pyro1, Pyroprint pyro2, double upperThreshold, double lowerThreshold) {
      PyroprintCorrelation correlation = getCorrelation(pyro1, pyro2);

      if (correlation == null) {
         correlation = getCorrelation(pyro2, pyro1);
      }

      if (correlation != null) {
         if (correlation.getCorrelation() > upperThreshold) {
            correlation.set16_23(100);
            correlation.set23_5(100);
         }
         else if (correlation.getCorrelation() < lowerThreshold) {
            correlation.set16_23(0);
            correlation.set23_5(0);
         }
      }
   }

   public boolean hasCorrelation(Pyroprint pyro_A, Pyroprint pyro_B) {
      if ((mSimilarityMatrix.containsKey(pyro_A) &&
       mSimilarityMatrix.get(pyro_A).containsKey(pyro_B)) ||
       (mSimilarityMatrix.containsKey(pyro_B) &&
       mSimilarityMatrix.get(pyro_B).containsKey(pyro_A))) {
         return true;
      }

      return false;
   }

   public void addCorrelation(PyroprintCorrelation correlation) {
      Pyroprint pyro_A = correlation.getPyroprintOne();
      Pyroprint pyro_B = correlation.getPyroprintTwo();

      if (!mSimilarityMatrix.containsKey(pyro_A)) {
         mSimilarityMatrix.put(pyro_A, new HashMap<Pyroprint, PyroprintCorrelation>());
      }

      mSimilarityMatrix.get(pyro_A).put(pyro_B, correlation);

      addPyroprint(pyro_A);
      addPyroprint(pyro_B);
   }

   public PyroprintCorrelation removeCorrelation(Pyroprint pyro_A, Pyroprint pyro_B) {
      PyroprintCorrelation removedCorr = null;

      if (mSimilarityMatrix.containsKey(pyro_A) && mSimilarityMatrix.get(pyro_A).containsKey(pyro_B)) {
         removedCorr = mSimilarityMatrix.get(pyro_A).remove(pyro_B);
      }
      if (mSimilarityMatrix.containsKey(pyro_B) && mSimilarityMatrix.get(pyro_B).containsKey(pyro_A)) {
         mSimilarityMatrix.get(pyro_B).remove(pyro_A);
      }

      return removedCorr;
   }
}
