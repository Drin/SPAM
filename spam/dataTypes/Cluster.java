package spam.dataTypes;

import spam.dataTypes.Isolate;
import spam.dataTypes.SampleMethod;
import spam.dataTypes.IsolateRegion;

import spam.dataStructures.IsolateSimilarityMatrix;

//import spam.distanceMeasures.IsolateSimilarity;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

//TODO it is possible to represent each data point (isolateSample)
//as a point in n-dimensional space where n is the number of other isolates
//it has correlations (or any distance) to. This would use a full
//correlation matrix as opposed to the half correlation matrix that is
//provided in the data file. But certainly something to keep in mind.
public class Cluster {
   private List<Isolate> isolates;
   private double sumSquaredError, minDist, maxDist, avgDist;
   private IsolateSimilarityMatrix similarityMatrix;

   private String mFecalSeries = null, mImmSeries = null,
    mLaterSeries = null, mDeepSeries = null, mBeforeSeries = null;

   public Cluster(IsolateSimilarityMatrix matrix) {
      similarityMatrix = matrix;
      isolates = new ArrayList<Isolate>();
      //sumSquaredError = 0;
   }

   public Cluster(IsolateSimilarityMatrix matrix, Isolate firstIsolate) {
      similarityMatrix = matrix;
      isolates = new ArrayList<Isolate>();
      isolates.add(firstIsolate);
      firstIsolate.setClustered(true);
      //setCentroid(firstIsolate);
   }

   public Cluster(Cluster copyCluster) {
      similarityMatrix = copyCluster.similarityMatrix;
      isolates = new ArrayList<Isolate>();

      for (Isolate sample : copyCluster.isolates) {
         this.isolates.add(sample);
      }

      //this.centroid = copyCluster.centroid;
   }

   public int size() {
      return isolates.size();
   }

   public List<Isolate> getIsolates() {
      return isolates;
   }

   public void setSimilarityMatrix(IsolateSimilarityMatrix similarityMatrix) {
      this.similarityMatrix = similarityMatrix;
   }

   /*
   public double[] getCentroid() {
      return centroid;
   }

   public void setCentroid(double[] centroid) {
      this.centroid = centroid;

      sumSquaredError = 0;
      for (double[] dataPoint : isolates) {
         double squaredError = 0;
         for (int dataNdx = 0; dataNdx < dataPoint.length; dataNdx++) {
            squaredError += Math.pow(dataPoint[dataNdx] - centroid[dataNdx], 2);
         }
         sumSquaredError += squaredError;
      }
   }
   */

   public double minDist() {
      double min = 0;

      for (int sampleOne = 0; sampleOne < isolates.size(); sampleOne++) {
         for (int sampleTwo = sampleOne + 1; sampleTwo < isolates.size(); sampleTwo++) {
            Isolate isolateOne = isolates.get(sampleOne);
            Isolate isolateTwo = isolates.get(sampleTwo);

            double correlation = similarityMatrix.getCorrelationVal(isolateOne, isolateTwo);
            min = Math.max(min, correlation);
         }
      }

      return min;
   }

   public double maxDist() {
      double max = Double.MAX_VALUE;

      for (int sampleOne = 0; sampleOne < isolates.size(); sampleOne++) {
         for (int sampleTwo = sampleOne + 1; sampleTwo < isolates.size(); sampleTwo++) {
            Isolate isolateOne = isolates.get(sampleOne);
            Isolate isolateTwo = isolates.get(sampleTwo);

            double correlation = similarityMatrix.getCorrelationVal(isolateOne, isolateTwo);
            max = Math.min(max, correlation);
         }
      }

      return max;
   }

   public double avgDist() {
      double total = 0, count = 0;

      for (int sampleOne = 0; sampleOne < isolates.size(); sampleOne++) {
         for (int sampleTwo = sampleOne + 1; sampleTwo < isolates.size(); sampleTwo++) {
            Isolate isolateOne = isolates.get(sampleOne);
            Isolate isolateTwo = isolates.get(sampleTwo);

            double correlation = similarityMatrix.getCorrelationVal(isolateOne, isolateTwo);
            total += correlation;
            count++;
         }
      }

      //System.out.println("are these values the same?");
      //System.out.printf("average using isolate size: %d using count %d\n",
       //(total/isolates.size()), (total/count));

      return total/count;
   }

   /*
    * not sure these apply
   public void setSSE(double error) {
      sumSquaredError = error;
   }

   public double getSSE() {
      return sumSquaredError;
   }
   */

   public Cluster addIsolate(Isolate newSample) {
      isolates.add(newSample);

      return this;
   }

   public Cluster unionWith(Cluster otherCluster) {
      Cluster newCluster = new Cluster(this);

      for (Isolate sample : otherCluster.isolates) {
         newCluster.isolates.add(sample);
      }

      /*
      if (this.centroid != null && otherCluster.centroid != null) {
         newCluster.setCentroid(getNewCentroid(this.centroid, otherCluster.centroid));
      }
      */

      return newCluster;
   }

   //closest distances are being calculated using "distance" and then recalculated
   //to be the correlation between those two distances. I think this is correct
   public double actualDistance(Cluster otherCluster, distType type) {
      double closestDist = -1;
      int closestNdxOne = -1, closestNdxTwo = -1;

      switch(type) {

         case SINGLE:
            closestDist = Double.MAX_VALUE;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //double dist = IsolateSimilarity.getDistance(this.isolates.get(dataNdx),
                   //otherCluster.isolates.get(otherNdx));
                  double dist = similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  if (dist < closestDist) {
                     closestDist = dist;
                     closestNdxOne = dataNdx;
                     closestNdxTwo = otherNdx;
                  }
               }
            }

            /*
            closestDist = IsolateSimilarity.getCorrelationVal(
             this.isolates.get(closestNdxOne), otherCluster.isolates.get(closestNdxTwo));
             */
            closestDist = similarityMatrix.getCorrelationVal(this.isolates.get(closestNdxOne),
             otherCluster.isolates.get(closestNdxTwo));

            break;

         case COMPLETE:
            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //double dist = IsolateSimilarity.getDistance(this.isolates.get(dataNdx),
                   //otherCluster.isolates.get(otherNdx));
                  double dist = similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  if (dist > closestDist) {
                     closestDist = dist;
                     closestNdxOne = dataNdx;
                     closestNdxTwo = otherNdx;
                  }
               }
            }

            /*
            closestDist = IsolateSimilarity.getCorrelationVal(
             this.isolates.get(closestNdxOne), otherCluster.isolates.get(closestNdxTwo));
             */
            closestDist = similarityMatrix.getCorrelationVal(this.isolates.get(closestNdxOne),
             otherCluster.isolates.get(closestNdxTwo));

            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //double dist = IsolateSimilarity.findCorrelation(this.isolates.get(dataNdx),
                   //otherCluster.isolates.get(otherNdx));
                  double dist = similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  totalDist += dist;

                  totalSize++;
               }
            }

            closestDist = totalDist/totalSize;
            break;

         case WARD:
            double wardDist = 0, wardSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  wardDist += similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  wardSize++;
               }
            }

            double avgCorrelation = wardDist/wardSize;

            closestDist = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double correlation = similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  closestDist += (correlation - avgCorrelation) * (correlation - avgCorrelation);
               }
            }
            break;
      }

      return closestDist;
   }

   public boolean debugSimilar(Cluster otherCluster) {
      for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
         for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
            IsolateCorrelation isolateCorr = similarityMatrix.getCorrelation(
             this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));

            if (isolateCorr == null) {
               isolateCorr = similarityMatrix.getCorrelation(
                otherCluster.isolates.get(otherNdx), this.isolates.get(dataNdx));
            }

            if (isolateCorr == null || !isolateCorr.isSimilar()) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean isSimilar(Cluster otherCluster) {
      for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
         for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
            IsolateCorrelation isolateCorr = similarityMatrix.getCorrelation(
             this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));

            if (isolateCorr == null) {
               isolateCorr = similarityMatrix.getCorrelation(
                otherCluster.isolates.get(otherNdx), this.isolates.get(dataNdx));
            }

            if (isolateCorr == null || !isolateCorr.isSimilar()) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean isDifferent(Cluster otherCluster) {
      for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
         for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
            IsolateCorrelation isolateCorr = similarityMatrix.getCorrelation(
             this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));

            if (isolateCorr == null) {
               isolateCorr = similarityMatrix.getCorrelation(
                otherCluster.isolates.get(otherNdx), this.isolates.get(dataNdx));
            }

            if (isolateCorr == null || isolateCorr.isDifferent()) {
               return true;
            }
         }
      }

      return false;
   }

   public double corrDistance(Cluster otherCluster, distType type) {
      double closestDist = -1;
      switch(type) {

         //this used to be SINGLE but then I realized that with correlations being 100 based
         //instead of 0 based, it is actually COMPLETE 
         case COMPLETE:
            closestDist = Double.MAX_VALUE;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double corrVal = similarityMatrix.getCorrelationVal(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));

                  if (corrVal == 0) {
                     corrVal = otherCluster.similarityMatrix.getCorrelationVal(
                      this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));
                  }

                  closestDist = Math.min(closestDist, corrVal);
               }
            }

            break;

         //this used to be COMPLETE but then I realized that with correlations being 100 based
         //instead of 0 based, it is actually SINGLE
         case SINGLE:
            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double corrVal = similarityMatrix.getCorrelationVal(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));

                  if (corrVal == 0) {
                     corrVal = otherCluster.similarityMatrix.getCorrelationVal(
                      this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));
                  }

                  closestDist = Math.max(closestDist, corrVal);
               }
            }

            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;

            //System.err.println("calculating everage...\n");

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //totalDist += 100 - IsolateSimilarity.findCorrelation(this.isolates.get(dataNdx),
                  double corrVal = similarityMatrix.getCorrelationVal(
                   otherCluster.isolates.get(otherNdx), this.isolates.get(dataNdx));

                  if (corrVal == 0) {
                     corrVal = similarityMatrix.getCorrelationVal(
                      this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx));

                     //System.out.printf("*reverse*\ncorrelation between %s and %s: %.03f\n",
                      //this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx), corrVal);
                  }

                  /*
                  System.out.printf("correlation between %s and %s: %.03f\n",
                   otherCluster.isolates.get(otherNdx), this.isolates.get(dataNdx), corrVal);
                   */

                  totalDist += corrVal;
                  totalSize++;

                  //System.err.println("totalDist: " + totalDist);
               }
            }

            //System.err.println(totalDist + "/" + totalSize + " = " + (totalDist/totalSize));

            closestDist = totalDist/totalSize;
            break;

         case WARD:
            double wardDist = 0, wardSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  wardDist += similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  wardSize++;
               }
            }

            double avgCorrelation = wardDist/wardSize;

            closestDist = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //double correlation = IsolateSimilarity.findCorrelation(this.isolates.get(dataNdx),
                   //otherCluster.isolates.get(otherNdx));
                  double correlation = similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  closestDist += (correlation - avgCorrelation) * (correlation - avgCorrelation);
               }
            }
            break;

         /*
          * this case doesn't apply since there is no centroid
         case CENTROID:
            closestDist = distMode.evaluateDistance(
             this.centroid,
             otherCluster.centroid);
            break;

         */
      }

      return closestDist;
   }

   public double distance(Cluster otherCluster, distType type) {
      double closestDist = -1;
      switch(type) {

         case SINGLE:
            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //closestDist = Math.min(closestDist, IsolateSimilarity.getDistance(
                   //this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
                  closestDist = Math.max(closestDist, similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         case COMPLETE:
            closestDist = Double.MAX_VALUE;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //closestDist = Math.max(closestDist, IsolateSimilarity.getDistance(
                   //this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
                  closestDist = Math.min(closestDist, similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //totalDist += IsolateSimilarity.getDistance(this.isolates.get(dataNdx),
                   //otherCluster.isolates.get(otherNdx));
                  totalDist = similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  totalSize++;
               }
            }

            closestDist = totalDist/totalSize;
            break;

         case WARD:
            double wardDist = 0, wardSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //wardDist += similarityMatrix.getDistance(this.isolates.get(dataNdx),
                   //otherCluster.isolates.get(otherNdx));

                  wardDist += similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  wardSize++;
               }
            }

            double avgCorrelation = wardDist/wardSize;

            closestDist = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //double correlation = IsolateSimilarity.getDistance(this.isolates.get(dataNdx),
                   //otherCluster.isolates.get(otherNdx));
                  double correlation = similarityMatrix.getCorrelationVal(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  closestDist += (correlation - avgCorrelation) * (correlation - avgCorrelation);
               }
            }
            break;
         /*
          * Neither of these cases apply since there is no centroid
         case CENTROID:
            closestDist = distMode.evaluateDistance(
             this.centroid,
             otherCluster.centroid);
            break;

         case WARD:
            closestDist = this.unionWith(otherCluster).sumSquaredError -
             (this.sumSquaredError + otherCluster.sumSquaredError);
            break;
         */
      }

      return closestDist;
   }

   public void recalculateDistanceTo(Cluster otherCluster, distType type, double upperThreshold, double lowerThreshold) {
      double clustDist = this.corrDistance(otherCluster, type);
      if (clustDist < lowerThreshold || clustDist > upperThreshold) {

         for (Isolate isolate : isolates) {
            for (Isolate otherIsolate : otherCluster.isolates) {

               if (similarityMatrix.hasCorrelation(isolate, otherIsolate)) {
                  similarityMatrix.transformCorrelation(isolate, otherIsolate, upperThreshold, lowerThreshold);
               }
               else if (similarityMatrix.hasCorrelation(otherIsolate, isolate)) {
                  similarityMatrix.transformCorrelation(otherIsolate, isolate, upperThreshold, lowerThreshold);
               }
               else {
                  //System.err.println("recalculating distance for a nonexistent correlation...");
               }

            }
         }

      }
   }

   /*
   public double[] getNewCentroid(double[] centroidOne, double[] centroidTwo) {
      double[] newCentroid = new double[centroidOne.length];

      if (centroidOne == null && centroidTwo != null)
         newCentroid = centroidTwo;
      else if (centroidOne != null && centroidTwo == null)
         newCentroid = centroidOne;
      else {
         for (int ndx = 0; ndx < centroidOne.length; ndx++) {
            newCentroid[ndx] = (centroidOne[ndx] + centroidTwo[ndx]) / 2;
         }
      }

      return newCentroid;
   }
   */

   public String toString() {
      String str = "";

      for (int clusterNdx = 0; clusterNdx < isolates.size(); clusterNdx++) {
         str += isolates.get(clusterNdx) + ", ";
      }

      return str;
   }

   public static String cytoscapeFormatHeader() {
      String[] column = new String[] {"e.coli_A", "e.coli_B", "interaction", "correlation"};
      String cytoFormat = String.format("%s\t%s\t%s\t%s\n",
       column[0], column[1], column[2], column[3]);

      return cytoFormat;
   }

   public String toCytoscapeCluster(String clusterName) {
      String cytoFormat = "";

      for (Isolate sample : isolates) {
         cytoFormat += String.format("%s\t%s\n",
          clusterName, sample);
      }

      return cytoFormat;
   }

   public String toCytoscapeFormat() {
      String cytoFormat = "";
      String interactionType = "pp";

      if (isolates.size() == 1) {
         return cytoFormat + String.format("%s\t%s\t%s\t%d\n",
          isolates.get(0), isolates.get(0), interactionType, -1);
      }

      for (int srcNdx = 0; srcNdx < isolates.size(); srcNdx++) {
         for (int dstNdx = srcNdx + 1; dstNdx < isolates.size(); dstNdx++) {
            double isolateCorr = -1;

            Isolate srcIsolate = isolates.get(srcNdx);
            Isolate dstIsolate = isolates.get(dstNdx);

            //if (isolates.get(srcNdx).hasCorr(isolates.get(dstNdx))) {
            if (similarityMatrix.hasCorrelation(srcIsolate, dstIsolate)) {
               //use source isolate's correlation map
               //Map<Isolate, Double> corrMap = isolates.get(srcNdx).getCorrMap();
               
               cytoFormat += String.format("%s\t%s\t%s\t%.03f\n",
                isolates.get(srcNdx), isolates.get(dstNdx), interactionType, 
                similarityMatrix.getCorrelationVal(srcIsolate, dstIsolate));
            }
            else if (similarityMatrix.hasCorrelation(dstIsolate, srcIsolate)) {
               //use destination isolate's correlation map
               //Map<Isolate, Double> corrMap = isolates.get(dstNdx).getCorrMap();

               cytoFormat += String.format("%s\t%s\t%s\t%.03f\n",
                isolates.get(dstNdx), isolates.get(srcNdx), interactionType, similarityMatrix.getCorrelationVal(dstIsolate, srcIsolate));
            }

         }
      }

      return cytoFormat;
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
         
         //just so that we are adding to the correct map
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

      //TODO replace the data string with the counts from above mappings. may
      //move this to IsolateOutputWriter and simply have this method return the
      //string of isolate counts
      //
      //String series = "{ name: 'Fecal', data: [4, 2, 4, 3, 2, 4, 0, 4, 1, 4, 4, 0, 0, 2] }";

      //tempOutput += String.format("%s\n%s\n%s%s%s%s\n", "cluster_" + clusterNum,
       //diagramHeader, toIsolateTable(fecalMap), toIsolateTable(immMap), toIsolateTable(laterMap), toIsolateTable(deepMap));

      //will display Day:, 1, 2, 3, ... for csv formatted temporal diagram
      
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

   public enum distType {
      SINGLE,
      COMPLETE,
      AVERAGE,
      //CENTROID,
      WARD
   }
}
