package spam.Types;

import spam.Types.Isolate;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Cluster {
   private List<Isolate> mIsolates = null;
   //private Dendogram mDendogram = null;

   private String mFecalSeries = null, mImmSeries = null,
    mLaterSeries = null, mDeepSeries = null, mBeforeSeries = null;

   public Cluster() {
      mIsolates = new ArrayList<Isolate>();
   }

   public Cluster(Isolate firstIsolate) {
      mIsolates = new ArrayList<Isolate>();
      mIsolates.add(firstIsolate);

      //mDendogram = new DendogramLeaf(firstIsolate);
   }

   public Cluster(Cluster copyCluster) {
      mIsolates = new ArrayList<Isolate>();

      for (Isolate isolate : copyCluster.mIsolates) {
         mIsolates.add(isolate);
      }

      //mDendogram = new Dendogram(copyCluster.mDendogram);
   }

   public int size() {
      return mIsolates.size();
   }

   public List<Isolate> getIsolates() {
      return mIsolates;
   }

   public Isolate getCentroid() {
      Isolate centroidIsolate = null;
      double maxAvgSimilarity = -1;

      for (int isoNdx_A = 0; isoNdx_A < mIsolates.size(); isoNdx_A++) {

         double avgSim = 0, length = 0;

         for (int isoNdx_B = isoNdx_A + 1; isoNdx_B < mIsolates.size(); isoNdx_B++) {
            avgSim += mIsolates.get(isoNdx_B).getSimilarity(isoNdx_A);
            length++;
         }

         avgSim /= length;

         if (avgSim > maxAvgSimilarity) {
            centroidIsolate = mIsolates.get(isoNdx_A);
            maxAvgSimilarity = avgSim;
         }
      }

      return centroidIsolate;
   }

   /*
    * Minimum Distance == Maximum Similarity
    */
   public double minDist() {
      double min = 0;

      for (int isoNdx_A = 0; isoNdx_A < mIsolates.size(); isoNdx_A++) {
         for (int isoNdx_B = isoNdx_A + 1; isoNdx_B < mIsolates.size(); isoNdx_B++) {
            min = Math.max(min, mIsolates.get(isoNdx_B).getSimilarity(isoNdx_A));
         }
      }

      return min;
   }

   /*
    * Maximum Distance == Minimum Similarity
    */
   public double maxDist() {
      double max = Double.MAX_VALUE;

      for (int isoNdx_A = 0; isoNdx_A < mIsolates.size(); isoNdx_A++) {
         for (int isoNdx_B = isoNdx_A + 1; isoNdx_B < mIsolates.size(); isoNdx_B++) {
            max = Math.min(max, mIsolates.get(isoNdx_B).getSimilarity(isoNdx_A));
         }
      }

      return max;
   }

   public double avgDist() {
      double simSum = 0, count = 0;

      for (int isoNdx_A = 0; isoNdx_A < mIsolates.size(); isoNdx_A++) {
         for (int isoNdx_B = 0; isoNdx_B < mIsolates.size(); isoNdx_B++) {
            if (!isoNdx_A.equals(isoNdx_B)) {
               simSum += mIsolates.get(isoNdx_A).getSimilarity(isoNdx_B);
               count++;
            }
         }
      }

      //System.out.println("are these values the same?");
      //System.out.printf("average using isolate size: %d using count %d\n",
       //(total/isolates.size()), (total/count));

      return simSum/count;
   }

   public Cluster addIsolate(Isolate newIsolate) {
      mIsolates.add(newIsolate);

      return this;
   }

   public Cluster unionWith(Cluster otherCluster) {
      Cluster newCluster = new Cluster(this);

      for (Isolate sample : otherCluster.mIsolates) {
         newCluster.mIsolates.add(sample);
      }

      return newCluster;
   }

   /*
    * Minimum similarity is Maximum distance
    */
   public double getMinSimilarity(Cluster otherCluster) {
      double minSim = Double.MAX_VALUE;

      for (int dataNdx = 0; dataNdx < mIsolates.size(); dataNdx++) {
         for (int otherNdx = 0; otherNdx < otherCluster.mIsolates.size(); otherNdx++) {
            double sim = mIsolates.get(dataNdx).getSimilarity(otherCluster.mIsolates.get(otherNdx));

            minSim = Math.min(sim, minSim);
         }
      }

      return minSim;
   }

   public double getMaxSimilarity(Cluster otherCluster) {
      double maxSim = -1;

      for (int dataNdx = 0; dataNdx < mIsolates.size(); dataNdx++) {
         for (int otherNdx = 0; otherNdx < otherCluster.mIsolates.size(); otherNdx++) {
            double sim = mIsolates.get(dataNdx).getSimilarity(otherCluster.mIsolates.get(otherNdx));

            maxSim = Math.max(sim, maxSim);
         }
      }

      return maxSim;
   }

   public double getAvgSimilarity(Cluster otherCluster) {
      double simSum = 0, totalSize = 0;

      for (int dataNdx = 0; dataNdx < mIsolates.size(); dataNdx++) {
         for (int otherNdx = 0; otherNdx < otherCluster.mIsolates.size(); otherNdx++) {
            simSum += mIsolates.get(dataNdx).getSimilarity(otherCluster.mIsolates.get(otherNdx));

            totalSize++;
         }
      }

      return totalSim/totalSize;
   }

   public double getSimilarity(Cluster otherCluster, DIST_TYPE type) {
      switch(type) {
         case SINGLE:
            return getMaxSimilarity(otherCluster);
            break;

         case COMPLETE:
            return getMinSimilarity(otherCluster);
            break;

         case AVERAGE:
            return getAvgSimilarity(otherCluster);
            break;

         default:
            System.err.println("Uncrecognized intercluster distance type");
            System.exit(1);
            break;
      }

      return -1;
   }

   /*
    * alpha is upper threshold
    * beta is lower threshold
    */
   public void recalculateDistance(Cluster otherCluster, DIST_TYPE type, double alpha, double beta) {
      for (Isolate isolate : mIsolates) {
         for (Isolate otherIsolate : otherCluster.mIsolates) {
            isolate.transformSimilarity(otherIsolate, alpha, beta);
         }
      }
   }

   public String toString() {
      String str = "";

      for (int clusterNdx = 0; clusterNdx < isolates.size(); clusterNdx++) {
         str += isolates.get(clusterNdx) + ", ";
      }

      return str;
   }

   public String getFecalSeries() {
      if (mFecalSeries == null) getSeriesCounts();
      return mFecalSeries;
   }

   public String getImmSeries() {
      if (mImmSeries == null) getSeriesCounts();
      return mImmSeries;
   }

   public String getLaterSeries() {
      if (mLaterSeries == null) getSeriesCounts();
      return mLaterSeries;
   }

   public String getDeepSeries() {
      if (mDeepSeries == null) getSeriesCounts();
      return mDeepSeries;
   }

   public String getBeforeSeries() {
      if (mBeforeSeries == null) getSeriesCounts();
      return mBeforeSeries;
   }

   public void getSeriesCounts() {
      String tempOutput = "";
      int numDays = 6;
      int technicianNdx = 0, groupNdx = 1, dayNdx = 2;


      //map representing day -> num isolates in the group for that day
      Map<Integer, Integer> fecalMap = new LinkedHashMap<Integer, Integer>();
      Map<Integer, Integer> immMap = new LinkedHashMap<Integer, Integer>();
      Map<Integer, Integer> laterMap = new LinkedHashMap<Integer, Integer>();
      Map<Integer, Integer> deepMap = new LinkedHashMap<Integer, Integer>();
      Map<Integer, Integer> beforeMap = new LinkedHashMap<Integer, Integer>();

      for (Isolate sample : isolates) {
         Map<Integer, Integer> sampleMap = null;

         String sampleName = sample.getName();
         //if isolate name is 'f14-1' then extract 14 as the day
         int day = Integer.parseInt(sampleName.substring(dayNdx, sampleName.indexOf("-")));
         //if isolate name is 'f14-1' then extract 1 as the isolateNum
         int isolateNum = Integer.parseInt(sampleName.substring(sampleName.indexOf("-") + 1, sampleName.length()));
         int isolateCount = 0;
         
         switch (sample.getSampleMethod()) {
            case FECAL:
               sampleMap = fecalMap;
               break;

            case IMM:
               sampleMap = immMap;
               break;

            case LATER:
               sampleMap = laterMap;
               break;

            case DEEP:
               sampleMap = deepMap;
               break;

            case BEFORE:
               sampleMap = beforeMap;
               break;
         }

         if (!sampleMap.containsKey(day)) {
            sampleMap.put(day, 1);
         }
         else {
            sampleMap.put(day, sampleMap.get(day) + 1);
         }
      }

      String fecalSeries = "", immSeries = "", laterSeries = "", deepSeries = "", beforeSeries = "";

      for (int day = 1; day <= numDays; day++) {
         fecalSeries += "," + (fecalMap.containsKey(day) ? fecalMap.get(day) : 0);
         immSeries += "," + (immMap.containsKey(day) ? immMap.get(day) : 0);
         laterSeries += "," + (laterMap.containsKey(day) ? laterMap.get(day) : 0);
         deepSeries += "," + (deepMap.containsKey(day) ? deepMap.get(day) : 0);
         beforeSeries += "," + (beforeMap.containsKey(day) ? beforeMap.get(day) : 0);
      }

      mFecalSeries = fecalSeries.substring(1);
      mImmSeries = immSeries.substring(1);
      mLaterSeries = laterSeries.substring(1);
      mDeepSeries = deepSeries.substring(1);
      mBeforeSeries = beforeSeries.substring(1);
   }

   public String toTemporalFormat(int clusterNum) {
      String tempOutput = "";
      int numDays = 14;
      int technicianNdx = 0, groupNdx = 1, dayNdx = 2;

      //will display Day:, 1, 2, 3, ... for csv formatted temporal diagram
      String diagramHeader = "Day:";
      for (int day = 1; day <= numDays; day++) {
         diagramHeader += ", " + day;
      }

      //map representing isolateNum -> {days -> String}
      Map<Integer, Map<Integer, String>> fecalMap = new LinkedHashMap<Integer, Map<Integer, String>>();
      Map<Integer, Map<Integer, String>> immMap = new LinkedHashMap<Integer, Map<Integer, String>>();
      Map<Integer, Map<Integer, String>> laterMap = new LinkedHashMap<Integer, Map<Integer, String>>();
      Map<Integer, Map<Integer, String>> deepMap = new LinkedHashMap<Integer, Map<Integer, String>>();
      Map<Integer, Map<Integer, String>> beforeMap = new LinkedHashMap<Integer, Map<Integer, String>>();

      for (Isolate sample : isolates) {
         Map<Integer, Map<Integer, String>> sampleMap = null;

         String sampleName = sample.getName();
         //if isolate name is 'f14-1' then extract 14 as the day
         int day = Integer.parseInt(sampleName.substring(dayNdx, sampleName.indexOf("-")));
         //if isolate name is 'f14-1' then extract 1 as the isolateNum
         int isolateNum = Integer.parseInt(sampleName.substring(sampleName.indexOf("-") + 1, sampleName.length()));
         String marker = ", X";
         
         //just so that we are adding to the correct map
         switch (sample.getSampleMethod()) {
            case FECAL:
               sampleMap = fecalMap;
               marker = ", F";
               break;
            case IMM:
               sampleMap = immMap;
               marker = ", I";
               break;
            case LATER:
               sampleMap = laterMap;
               marker = ", L";
               break;
            case DEEP:
               sampleMap = deepMap;
               marker = ", D";
               break;
            case BEFORE:
               sampleMap = beforeMap;
               marker = ", B";
               break;
         }

         if (!sampleMap.containsKey(isolateNum)) {
            Map<Integer, String> newTickMap = new LinkedHashMap<Integer, String>();

            for (int dayCol = 1; dayCol <= numDays; dayCol++) {
               newTickMap.put(dayCol, ", ");
            }

            sampleMap.put(isolateNum, newTickMap);
         }

         Map<Integer, String> tickMap = sampleMap.get(isolateNum);

         tickMap.put(day, marker);
      }

      tempOutput += String.format("%s\n%s\n%s%s%s%s%s\n", "cluster_" + clusterNum,
       diagramHeader, toIsolateTable(fecalMap), toIsolateTable(immMap), toIsolateTable(laterMap), toIsolateTable(deepMap), toIsolateTable(beforeMap));

      /*
       * auto generate some partially completed g.raphael bar chart code
      String barOptions = "{stacked: true, type: \"soft\"}).hoverColumn(fin2, fout2);";

      System.out.println(String.format("r.g.barchart(%d, %d, 400, 220, [%s], %s",
       (450 * (clusterNum % 2)), (50 + (220 * (clusterNum / 2))), toIsolateBars(fecalMap), barOptions));
      System.out.println(String.format("r.g.barchart(%d, %d, 400, 220, [%s], %s",
       (450 * (clusterNum % 2)), (50 + (220 * (clusterNum / 2))), toIsolateBars(immMap), barOptions));
      System.out.println(String.format("r.g.barchart(%d, %d, 400, 220, [%s], %s",
       (450 * (clusterNum % 2)), (50 + (220 * (clusterNum / 2))), toIsolateBars(laterMap), barOptions));
       */

      return tempOutput;
   }

   private String toIsolateTable(Map<Integer, Map<Integer, String>> sampleMap) {
      String tableOutput = "";

      //for (int level = 1; sampleMap.containsKey(level); level++) {
      for (int level : sampleMap.keySet()) {
         Map<Integer, String> tickMap = sampleMap.get(level);

         for (int day : tickMap.keySet()) {
            tableOutput += tickMap.get(day);
         }

         tableOutput += "\n";
      }

      //return hasOutput ? tableOutput : "";
      return tableOutput;
   }
   
   private String toIsolateBars(Map<Integer, Map<Integer, String>> sampleMap) {
      String barOutput = "";

      //for (int level = 1; sampleMap.containsKey(level); level++) {
      for (int level : sampleMap.keySet()) {
         //System.err.println("level: " + level);
         if (level > 1) {
            barOutput += ",\n";
         }

         Map<Integer, String> tickMap = sampleMap.get(level);
         barOutput += "[";
         for (int day: tickMap.keySet()) {
            if (day > 1) {
               barOutput += tickMap.get(day).equals(", X") ? ", 1" : ", 0";
            }
            else {
               barOutput += tickMap.get(day).equals(", X") ? "1" : "0";
            }
         }

         barOutput += "]";
      }
      
      return barOutput;
   }

   public enum DIST_TYPE {
      SINGLE,
      COMPLETE,
      AVERAGE,
      //CENTROID,
      WARD
   }
}
