package spam.Types;

import java.util.Map;
import java.util.HashMap;

public class SystemParameters {
   private String mOutputFileName = null;
   private Map<String, Map<String, String>> mFileParamMap = null;

   private static final String ALPHA_KEY = "alpha",
                               BETA_KEY = "beta",
                               ITS_REGION_KEY = "ITSRegion";

   public SystemParameters(String outputFile) {
      super();

      mOutputFileName = outputFile;
      mParamMap = new Map<String, String>();
   }

   /*
    * Setter Methods
    */
   public boolean addFile(String fileName) {
      if (fileName != null) {
         return mParamMap.put(fileName, new HashMap<String, String>()) == null;
      }

      return null;
   }

   public boolean setAlpha(String fileName, double alphaThreshold) {
      if (mFileParamMap.containsKey(fileName)) {
         mFileParamMap.get(fileName).put(ALPHA_KEY, alphaThreshold);
         return true;
      }

      return false;
   }

   public boolean setBeta(String fileName, double betaThreshold) {
      if (mFileParamMap.containsKey(fileName)) {
         mFileParamMap.get(fileName).put(BETA_KEY, betaThreshold);
         return true;
      }

      return false;
   }

   public boolean setITSRegion(String fileName, String itsRegion) {
      if (mFileParamMap.containsKey(fileName)) {
         mFileParamMap.get(fileName).put(ITS_REGION_KEY, itsRegion);
         return true;
      }

      return false;
   }

   /*
    * Getter Methods
    */
   public Map<String, String> getFileParams(String fileName) {
      if (mFileParamMap.containsKey(fileName)) {
         return mFileParamMap.get(fileName);
      }

      return null;
   }

   /*
    * Utility Methods
    */
   public boolean hasFile(String fileName) {
      return mFileParamMap.containsKey(fileName);
   }

   public int fileCount() {
      return mFileParamMap.size();
   }
}
