package spam.dataTypes;

import spam.dataTypes.SampleMethod;

import java.io.File;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;

public class Isolate {
   private String mIsolateName;
   private String mTechnician;
   private SampleMethod mGroup;
   private List<File> mFileListing_16_23, mFileListing_23_5;
   private int day;
   private boolean isClustered;
   //private Map<Isolate, Double> mCorrMap; //mapping from isolateName to correlation
   private static int TECH_NDX = 0, GRP_NDX = 1, DAY_NDX = 2;

   public Isolate(String name) {
      mIsolateName = name;
      mTechnician = String.valueOf(name.charAt(TECH_NDX));
      mGroup = SampleMethod.getMethod(mIsolateName.charAt(GRP_NDX));
      day = Integer.parseInt(mIsolateName.substring(DAY_NDX, mIsolateName.indexOf('-')));
      //mCorrMap = null;
      
      mFileListing_16_23 = new LinkedList<File>();
      mFileListing_23_5 = new LinkedList<File>();

      isClustered = false;
   }

   public Isolate(String name, Map<Isolate, Double> newCorrMap) {
      mIsolateName = name;
      mGroup = SampleMethod.getMethod(mIsolateName.charAt(GRP_NDX));
      day = Integer.parseInt(String.valueOf(mIsolateName.charAt(DAY_NDX)));
      //mCorrMap = newCorrMap;

      isClustered = false;
   }

   public String getName() {
      return mIsolateName;
   }

   public String getTechnician() {
      return mTechnician;
   }

   public SampleMethod getSampleMethod() {
      return mGroup;
   }

   public int getDay() {
      return day;
   }

   public boolean hasBeenClustered() {
      return isClustered;
   }

   public void setClustered(boolean status) {
      isClustered = status;
   }

   public void addFileListing_16_23(File file) {
      mFileListing_16_23.add(file);
   }

   public void addFileListing_23_5(File file) {
      mFileListing_23_5.add(file);
   }

   public List<File> getFileListing_16_23() {
      return mFileListing_16_23;
   }

   public List<File> getFileListing_23_5() {
      return mFileListing_23_5;
   }

   /*
   public boolean hasCorr(Isolate otherIsolate) {
      return mCorrMap.containsKey(otherIsolate);
   }

   public Map<Isolate, Double> getCorrMap() {
      return mCorrMap;
   }

   public void setCorrMap(Map<Isolate, Double> newCorrMap) {
      mCorrMap = newCorrMap;
   }
   */

   /*
   public double compareTo(Isolate otherIsolate) {
      /*
       * Try using IsolateDistance for now
       *
      double correlation = 0;
      //System.out.printf("comparing '%s' to '%s'\n", mIsolateName, otherIsolate.getName());
      //System.out.printf("am I contained in the other's correlation? %s\n", (otherIsolate.getCorrMap().containsKey(this)));
      if (mCorrMap.containsKey(otherIsolate)) {
         correlation = 100 - mCorrMap.get(otherIsolate);
      }
      else {
         correlation = 100 - otherIsolate.getCorrMap().get(this);
      }

      return correlation + mGroup.dist(otherIsolate.getSampleMethod());
      *
      return IsolateDistance.getDistance(this, otherIsolate);
   }
   */

   public boolean isSameIsolate(Isolate otherIsolate) {
      return mIsolateName.equals(otherIsolate.mIsolateName);
   }

   public boolean equals(Object otherIsolate) {
      if (otherIsolate instanceof Isolate) {
         //System.out.println("Checking if " + this.toString() + " is equal to " + otherIsolate);
         return isSameIsolate((Isolate) otherIsolate);
      }

      return false;
   }

   /*
   public String printCorrs() {
      String corrMap = "";

      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += ",\t" + sample;
      }
      corrMap += "\n" + mIsolateName + "";

      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += ",\t" + mCorrMap.get(sample);
      }

      return corrMap;
   }
   *
   public String printCorrs() {
      String corrMap = "";

      /*
      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += ",\t" + sample;
      }
      corrMap += "\n" + mIsolateName + "";
      *

      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += "," + mCorrMap.get(sample);
      }

      return corrMap;
   }
   */

   public int hashCode() {
      //System.out.println("hashcode of " + mIsolateName + ": " + mIsolateName.hashCode());
      return mIsolateName.hashCode();
   }

   public String toString() {
      String printStr = String.format("%s", mIsolateName);
      /*
      printStr += String.format("\n\t\t* %s *", mGroup);

      for (Isolate sample : mCorrMap.keySet()) {
         printStr += String.format("\n\t\t\t'%s' : %.02f",
          sample.getName(), mCorrMap.get(sample));
      }
      */

      return printStr;
   }
}
