package com.drin.java.analysis;

import com.drin.java.parsers.IsolateFileParser;

import com.drin.java.types.FileSettings;
import com.drin.java.types.Cluster;
import com.drin.java.types.ClusterDendogram;
import com.drin.java.types.Isolate;
import com.drin.java.types.IsolateRegion;
import com.drin.java.types.Connectivity;

import com.drin.java.dataStructures.IsolateSimilarityMatrix;

import com.drin.java.dendogram.Dendogram;
import com.drin.java.dendogram.DendogramNode;
import com.drin.java.dendogram.DendogramLeaf;

import com.drin.java.dataWriter.IsolateOutputWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.awt.Point;

/*
 * TODO: redo this for integration into SPAM
 */

public class HClustering {
   private Cluster.distType mClusterDistType;
   private IsolateRegion mRegion;
   private File mDataFile = null;
   private Map<File, FileSettings> dataFileMap;
   private Map<Connectivity, IsolateSimilarityMatrix> mIsolateNetworks;
   private List<ClusterDendogram> mClustDends = null;

   private double mLowerThreshold, mUpperThreshold;
   private double mThresholding = -1;
   private int mNumRegions;

   private static final String ARG_SEPARATOR = "&";
   private static double defaultThreshold = 99.7;
   private static long mRunTime = -1;
   private static String mOutputFileName = "", mClusterPreference = "structure";

   public HClustering(int numRegions) {
      super();

      mNumRegions = numRegions;
      mThresholding = defaultThreshold; //(defaultThreshold * mNumRegions);

      mLowerThreshold = 95;
      mUpperThreshold = 99.7;
      dataFileMap = new HashMap<File, FileSettings>();
   }

   public boolean cluster(String[] args) {
      boolean success = false;
      //handle command line arguments; sets dataFile and threshold
      success = parseArgs(args);

      Map<String, Map<Integer, List<Isolate>>> dataMap = new HashMap<String, Map<Integer, List<Isolate>>>();

      /*
       * Each isolate similarity matrix holds a correlation for both regions
       */
      IsolateSimilarityMatrix partialCorrelations = new IsolateSimilarityMatrix();
      Map<Connectivity, IsolateSimilarityMatrix> isolateNetworks =
       new HashMap<Connectivity, IsolateSimilarityMatrix>();

      isolateNetworks.put(Connectivity.STRONG, new IsolateSimilarityMatrix());
      isolateNetworks.put(Connectivity.WEAK, new IsolateSimilarityMatrix());

      Cluster.distType type = null;
      String dataFileName = null;
      double lowerThreshold = -1, upperThreshold = -1;

      for (File dataFile : dataFileMap.keySet()) {
         FileSettings settings = dataFileMap.get(dataFile);

         dataFileName = dataFile.getName();
         IsolateRegion region = settings.getRegion();
         type = settings.getDistanceType();
         lowerThreshold = settings.getLowerThreshold();
         upperThreshold = settings.getUpperThreshold();

         if (dataFile != null) {
            IsolateFileParser parser = new IsolateFileParser(dataFile, settings);

            //MARKER old code
            //isolateMap = parser.extractData(similarityMatrix);
            //mIsolateNetworks = parser.extractData();
            parser.extractData(isolateNetworks, partialCorrelations, dataFileMap.size());
         }

         //System.out.println("strong Network size: " + isolateNetworks.get(Connectivity.STRONG).size());
      }

      //each point is a cluster, and we will combine two clusters in each iteration
      long startTime = System.nanoTime();

      List<ClusterDendogram> clustDends = clusterIsolates(type, isolateNetworks);

      long endTime = System.nanoTime();
      mRunTime = endTime - startTime;
      //System.err.println("clustDends length: " + clustDends.size());

      //if the isolates yielded NO clusters (wtf data disappear?) or
      //if clusterIsolates returned null, then this was *NOT* a success
      success = clustDends != null && !clustDends.isEmpty();

      String outputFileDir = String.format("ClusterResults/%s_%.02f_%.02f", type, lowerThreshold, upperThreshold);

      /*
      String outputFileName = String.format("%s/%s", outputFileDir,
       dataFile.getName().substring(0,
       dataFile.getName().indexOf(".csv")));
      */

      String origFileName = HClustering.mOutputFileName.equals("") ?
       dataFileName.substring(0, dataFileName.indexOf(".csv")) : HClustering.mOutputFileName;
      System.out.println("Writing to file " + origFileName + " even though outputFilename should be " + HClustering.mOutputFileName);
      String outputFileName = String.format("%s/%s", outputFileDir, origFileName);

      IsolateOutputWriter.outputClusters(clustDends, outputFileDir, outputFileName + ".xml");
      IsolateOutputWriter.outputCytoscapeFormat(clustDends, outputFileName);
      IsolateOutputWriter.outputTemporalClusters(clustDends, outputFileName);
      IsolateOutputWriter.outputTemporalCharts(clustDends, outputFileName);

      mClustDends = clustDends;

      return success;
   }

   private List<ClusterDendogram> clusterIsolates(Cluster.distType type,
    Map<Connectivity, IsolateSimilarityMatrix> isolateNetworks) {
      /*
      IsolateRegion region = settings.getRegion();
      double distanceThreshold = settings.getDistanceThreshold();
      double lowerThreshold = settings.getLowerThreshold();
      double upperThreshold = settings.getUpperThreshold();
      Cluster.distType type = settings.getDistanceType();
      */

      //mappings represent days to isolates
      IsolateSimilarityMatrix similarityMatrix = null;
      Map<String, Map<Integer, List<Isolate>>> technicianIsolateMap = null;
      //list of all constructed clusters
      List<ClusterDendogram> clusters = new ArrayList<ClusterDendogram>();
      List<ClusterDendogram> technicianClusters = new ArrayList<ClusterDendogram>();

      /*
       * Marker
       */

      //MARKER new code. get the isolateMap from the similarity matrix now
      //also, multiple similarity matrices are stored in the isolate networks
      //map so that it is possible to iterate on correlations based on their
      //strength instead of just going all willy nilly
      if (isolateNetworks.containsKey(Connectivity.STRONG)) {
         similarityMatrix = isolateNetworks.get(Connectivity.STRONG);
         technicianIsolateMap = similarityMatrix.getIsolateMap();

         for (String technician : technicianIsolateMap.keySet()) {
            //System.out.printf("clustering technician %s's dataset...\n", technician);
            Map<Integer, List<Isolate>> isolateMap = technicianIsolateMap.get(technician);

            for (int sampleDay : isolateMap.keySet()) {
               //System.out.printf("clustering day %d...\n", sampleDay);
               //Cluster the list of isolates in this day
               List<ClusterDendogram> currClusters = clusterIsolateList(similarityMatrix,
                isolateMap.get(sampleDay), type);

               IsolateOutputWriter.outputClustersByDay(similarityMatrix, sampleDay, currClusters);
               /*
               System.err.println("currClusters length: " + currClusters.size());
               /*
               for (ClusterDendogram clustDend : currClusters) {
                  System.out.println(clustDend.getDendogram().getXML());
               }
               */

               //System.err.printf("on day %d there are a total of %d clusters", sampleDay, clusters.size());
               //Cluster all previous days with this day
               clusters = clusterToDate(clusters, currClusters, type);
            }

            technicianClusters.addAll(clusters);
            clusters = new ArrayList<ClusterDendogram>();
         }

         clusters = clusterGroup(technicianClusters, type);
      }

      System.out.printf("\n\n================\nFINISHED STAGE 1. CLUSTERING SQUISHIES\n" +
       "===================\n\n");
      
      if (isolateNetworks.containsKey(Connectivity.WEAK)) {
         similarityMatrix = isolateNetworks.get(Connectivity.WEAK);
         technicianIsolateMap = similarityMatrix.getIsolateMap();

         /*
          * this is so that when doing the second pass through all of the clusters
          * squishy correlations will also be known
          */
         for (ClusterDendogram clust : clusters) {
            clust.getCluster().setSimilarityMatrix(similarityMatrix);
         }

         for (String technician : technicianIsolateMap.keySet()) {
            System.out.printf("clustering technician %s's dataset...\n", technician);
            Map<Integer, List<Isolate>> isolateMap = technicianIsolateMap.get(technician);

            for (int sampleDay : isolateMap.keySet()) {
               //System.out.printf("clustering day %d...\n", sampleDay);

               for (Isolate isolate : isolateMap.get(sampleDay)) {
                  //clusters = clusterWeakIsolates(similarityMatrix, clusters, isolate, type);

                  if (!isolate.hasBeenClustered()) {
                     Cluster newCluster = new Cluster(similarityMatrix, isolate);
                     Dendogram newDendogram = new DendogramLeaf(isolate);
                     clusters.add(new ClusterDendogram(newCluster, newDendogram));
                  }

               }
            }
         }

         clusters = clusterGroup(clusters, type);

      }

      System.out.printf("\n\n==================\nFINISHED STAGE 2.\n======================\n\n");
      return clusters;
   }

   private List<ClusterDendogram> clusterIsolateList(IsolateSimilarityMatrix similarityMatrix,
    List<Isolate> isolates, Cluster.distType type) {
      //represent fecal samples
      List<ClusterDendogram> clusterB = new ArrayList<ClusterDendogram>();
      //represent fecal samples
      List<ClusterDendogram> clusterF = new ArrayList<ClusterDendogram>();
      //represent immediate (after) samples
      List<ClusterDendogram> clusterI = new ArrayList<ClusterDendogram>();
      //represent later samples
      List<ClusterDendogram> clusterL = new ArrayList<ClusterDendogram>();
      List<ClusterDendogram> clusterD = new ArrayList<ClusterDendogram>();

      //clusters resulting from clustering the above clusters will be placed in
      //clusters and this will prevent me from having to refactor the rest of this
      //method.
      List<ClusterDendogram> clusters = new ArrayList<ClusterDendogram>();

      for (Isolate sample : isolates) {
         Cluster newCluster = new Cluster(similarityMatrix, sample);
         Dendogram newDendogram = new DendogramLeaf(sample);

         switch(sample.getSampleMethod()) {
            case FECAL:
               clusterF.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            case IMM:
               clusterI.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            case LATER:
               clusterL.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            case DEEP:
               clusterD.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            case BEFORE:
               clusterB.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            default:
               System.err.println("serious error here");
               break;
         }
      }
      //System.out.printf("clusterList size: %d\n", clusters.size());

      //cluster within each group
      clusterF = clusterGroup(clusterF, type);
      clusterI = clusterGroup(clusterI, type);
      clusterL = clusterGroup(clusterL, type);
      clusterD = clusterGroup(clusterD, type);
      clusterB = clusterGroup(clusterB, type);


      //cluster each group together:
      //F and I together first since they are the closest in time
      //F_I and L next since they are the next closest in time

      //was going to use "clusterAcrossGroup" but there seemed to be a lot of
      //logical traps such as where to put clusters that are combined and all of
      //the problems that followed from that
      clusters.addAll(clusterF);
      clusters.addAll(clusterD);
      clusters = clusterGroup(clusters, type);

      clusters.addAll(clusterI);
      clusters = clusterGroup(clusters, type);

      clusters.addAll(clusterL);
      clusters = clusterGroup(clusters, type);

      clusters.addAll(clusterB);
      clusters = clusterGroup(clusters, type);

      //clusters within all the day's clusters
      //based on the above clusterGroup call this would likely be repetitive
      //clusters = clusterGroup(clusters, type);

      return clusters;
   }

   private List<ClusterDendogram> clusterGroup(List<ClusterDendogram> clusters, Cluster.distType type) {
      //System.out.printf("***clustering new group***\n");
      Point closeClusters = new Point(-1, -1);
      //double minDist = Double.MAX_VALUE;
      double maxSimilarity = 0;
      boolean hasChanged;

      do {
         //System.out.printf("entering clustering loop...\n");
         hasChanged = false;

         for (int clustOne = 0; clustOne < clusters.size(); clustOne++) {
            for (int clustTwo = clustOne + 1; clustTwo < clusters.size(); clustTwo++) {
               Cluster cluster_A = clusters.get(clustOne).getCluster();
               Cluster cluster_B = clusters.get(clustTwo).getCluster();

               //System.out.printf("\n\ncluster A:\n\t%s\n\ncluster B:\n\t%s\n\n", cluster_A, cluster_B);
               //double clustDist = cluster_A.distance(cluster_B, type);

               //this will ensure that i'm only comparing based on correlations
               double clustDist = cluster_A.corrDistance(cluster_B, type);
               //System.out.printf("clustDist: %.03f\n", clustDist);
               /*
               if (clustDist > 1) {
                  System.err.println("cluster group clustDist: " + clustDist + " between " + cluster_A + " and " + cluster_B);
               }
               */

               //System.out.println("ward's distance: " + clustDist);
               //if (clustDist < minDist && clustDist > 99.7 ) {
               //if (clustDist < minDist && clustDist < .03 ) {
               //if (clustDist < minDist && clustDist > 99.7 ) { this
               //corresponds to results used in paper
               //TODO  investigate the results for when you use '> minDist'
               //if (/*clustDist > minDist &&*/ clustDist > 99.7 ) {
               //System.out.printf("clustDist: %.03f\n", clustDist);

               //if (clustDist > minDist && clustDist > mThresholding ) {
               //System.out.printf("is %.03f > %.03f? %s\n\n", clustDist, maxSimilarity, (clustDist > maxSimilarity));
               //System.out.printf("mThreshold = %.03f\n", mThresholding);
               //if (clustDist > maxSimilarity && clustDist > mThresholding) {
               if (mClusterPreference.equals("structure")) {
                  if (clustDist > maxSimilarity && !cluster_A.isDifferent(cluster_B)) {
                     maxSimilarity = clustDist;
                     //System.out.printf("maxSimilarity: %.03f\n", maxSimilarity);
                     closeClusters = new Point(clustOne, clustTwo);
                  }
               }
               else if (mClusterPreference.equals("similarity")) {
                  if (clustDist > maxSimilarity && cluster_A.isSimilar(cluster_B)) {
                     maxSimilarity = clustDist;
                     //System.out.printf("maxSimilarity: %.03f\n", maxSimilarity);
                     closeClusters = new Point(clustOne, clustTwo);
                  }
               }
            }
         }

         /*
          * if newCluster list is a different sized then clearly two clusters were
          * combined. In this case set hasChanges to true and set the cluster list to
          * the new cluster list
          */
         List<ClusterDendogram> newClusters = combineClusters(clusters, closeClusters, maxSimilarity, type);

         if (newClusters.size() != clusters.size()) {
            hasChanged = true;
            clusters = newClusters;
            //System.out.println("recalculating cluster distances...");
            recalculateClusterDistances(clusters, type);
            //System.out.println("finished recalculating cluster distances...");
         }

         //reset various variables
         closeClusters = new Point(-1, -1);
         maxSimilarity = 0;
         //System.out.printf("finishing clustering iteration...\n");

         //continue clustering until clusters do not change
      } while (hasChanged);

      //System.out.printf("***Finished clustering group***\n");

      return clusters;
   }
   public void recalculateClusterDistances(List<ClusterDendogram> clusters, Cluster.distType type) {
      for (int clustOne = 0; clustOne < clusters.size(); clustOne++) {
         for (int clustTwo = clustOne + 1; clustTwo < clusters.size(); clustTwo++) {
            Cluster cluster_A = clusters.get(clustOne).getCluster();
            Cluster cluster_B = clusters.get(clustTwo).getCluster();

            cluster_A.recalculateDistanceTo(cluster_B, type, mUpperThreshold, mLowerThreshold);
            cluster_B.recalculateDistanceTo(cluster_A, type, mUpperThreshold, mLowerThreshold);
         }
      }
   }

   /*
    * move clusters to new cluster list, when one of the clusters that will be merged
    * is found, merge it with the other cluster, then add to new cluster list
    * only do this for one cluster to be merged to avoid duplicates
    */
   private List<ClusterDendogram> combineClusters(List<ClusterDendogram> clusters, Point minNdx, double correlation, Cluster.distType type) {
      //System.out.printf("minNdx for cluster combining is <%f, %f>\n", minNdx.getX(), minNdx.getY());
      ArrayList<ClusterDendogram> newClusters = new ArrayList<ClusterDendogram>();
      //ArrayList<Dendogram> newDendogram = new ArrayList<Dendogram>();

      for (int clusterNdx = 0; clusterNdx < clusters.size(); clusterNdx++) {
         if (clusterNdx != (int) minNdx.getX() && clusterNdx != (int) minNdx.getY()) {
            newClusters.add(clusters.get(clusterNdx));
            //newDendogram.add(dendogram.get(clusterNdx));
         }

         else if (clusterNdx == (int) minNdx.getX()) {
            //using minNdx for cluster one for consistency and readability
            //System.out.printf("minNdx X: %d minNdx Y: %d clustersLength: %d", (int) minNdx.getX(), (int) minNdx.getY(), clusters.size());
            Cluster clusterOne = clusters.get((int) minNdx.getX()).getCluster();
            Cluster clusterTwo = clusters.get((int) minNdx.getY()).getCluster();

            //System.out.printf("combining clusters:\n===\n\n%s\n and \n%s\nwith maxSimiliarty: %.03f\n\n===", clusterOne, clusterTwo, correlation);

            Cluster combinedCluster = new Cluster(clusterOne.unionWith(clusterTwo));

            //using minNdx for dendogram one for consistency and readability
            Dendogram leftDend = clusters.get((int) minNdx.getX()).getDendogram();
            Dendogram rightDend = clusters.get((int) minNdx.getY()).getDendogram();
            //Dendogram newDendogram = new DendogramNode(clusterOne.actualDistance(clusterTwo, type), leftDend, rightDend);
            Dendogram newDendogram = new DendogramNode(clusterOne.corrDistance(clusterTwo, type), leftDend, rightDend);

            newClusters.add(new ClusterDendogram(combinedCluster, newDendogram));
         }
      }

      //make the new cluster set into the current cluster set for next iteration
      return newClusters;
      //clusterer.dendogram = newDendogram;
   }

   //TODO check clusters vs dailyClusters, there's a problem in this method
   private List<ClusterDendogram> clusterToDate(List<ClusterDendogram> clusters,
    List<ClusterDendogram> dailyClusters, Cluster.distType type) {
      //System.out.printf("Clustering clusters (%d) with new day's clusters (%d)\n", clusters.size(), dailyClusters.size());

      //outer for loop loops over clusters in a day
      //inner for loop loops over clusters built up to the current day
      /*
       * clustering between days uses just correlations
       */
      //System.out.printf("\n***clustering to date***\n");
      for (ClusterDendogram newClusterDend : dailyClusters) {
         Cluster newCluster = newClusterDend.getCluster();

         //double minDist = Double.MAX_VALUE;
         double maxSimilarity = 0;
         int closeClusterNdx = -1;

         for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
            Cluster currClust = clusters.get(clustNdx).getCluster();
            double clustDist = newCluster.corrDistance(currClust, type);

            /*
            System.out.printf("\n\nnewCluster: \n\t%s\n\ncurrClust:\n\t%s\n\n", newCluster, currClust);
            System.out.printf("clustDist: %.03f\n", clustDist);
            */

            //if (clustDist < minDist && clustDist < mLowerThreshold) {
            //System.out.println("cluster to date ward's distance: " + clustDist);
            //if (clustDist < minDist && clustDist >= mUpperThreshold) {
            /*
            System.out.printf("is %.03f > %.03f? %s\n\n", clustDist, maxSimilarity, (clustDist > maxSimilarity));
            System.out.printf("mThreshold = %.03f\n", mThresholding);
            */

            //if (clustDist > maxSimilarity && clustDist > mThresholding) {
            if (mClusterPreference.equals("structure")) {
               if (clustDist > maxSimilarity && !newCluster.isDifferent(currClust)) {
                  maxSimilarity = clustDist;
                  //System.out.printf("maxSimilarity: %.03f\n", maxSimilarity);
                  closeClusterNdx = clustNdx;
               }
            }
            else if (mClusterPreference.equals("similarity")) {
               if (clustDist > maxSimilarity && newCluster.isSimilar(currClust)) {
                  maxSimilarity = clustDist;
                  //System.out.printf("maxSimilarity: %.03f\n", maxSimilarity);
                  closeClusterNdx = clustNdx;
               }
            }
         }

         //replace the cluster closest to the new Cluster with the
         //oldCluster U newCluster
         if (closeClusterNdx != -1) {
            Cluster closeCluster = clusters.get(closeClusterNdx).getCluster();

            Dendogram newDendogram = new DendogramNode(maxSimilarity, newClusterDend.getDendogram(),
             clusters.get(closeClusterNdx).getDendogram());
            ClusterDendogram newClustDend = new ClusterDendogram(
             closeCluster.unionWith(newCluster), newDendogram);

            clusters.set(closeClusterNdx, newClustDend);
         }
         else {
            clusters.add(newClusterDend);
         }
      }

      return clusters;
   }

   private boolean parseArgs(String[] args) {
      if (args.length < 1) {
         System.out.println("Usage: java hclustering (<filename>:<16s-23s|23s-5s>:[<lowerThreshold>]:"+
          "[<upperThreshold>]:[<single|average|complete|ward>])+");
         System.exit(1);
      }

      try {
         for (String arg : args) {
            System.out.println("arg: " + arg);

            File dataFile;
            FileSettings currFileSettings = new FileSettings();

            String[] subArgs = arg.split(ARG_SEPARATOR);
            
            /*
             * subArg indices:
             *    0 - filename
             *    1 - region
             *    3 - lowerthreshold
             *    4 - upperthreshold
             *    5 - distanceType
             */

            dataFile = new File(subArgs[0]);
            currFileSettings.setRegion(IsolateRegion.getRegion(subArgs[1]));

            currFileSettings.setLowerThreshold(subArgs.length >= 3 ?
             Double.parseDouble(subArgs[2]) : mLowerThreshold);

            currFileSettings.setUpperThreshold(subArgs.length >= 4 ?
             Double.parseDouble(subArgs[3]) : mUpperThreshold);

            //use reflection for distance measure
            /*
            distanceMode = args.length >= 3 ? 
             (DistanceMeasure) Class.forName(args[2]).newInstance() :
             new EuclideanDistanceMeasure();
             */

            currFileSettings.setDistanceType(subArgs.length >= 5 ?
             Cluster.distType.valueOf(subArgs[4].toUpperCase()) : Cluster.distType.AVERAGE);

            dataFileMap.put(dataFile, currFileSettings);
         }
      }
      catch (NumberFormatException formatErr) {
         System.out.printf("Invalid threshold values: %d or %d\n", args[2], args[3]);
         System.exit(1);
      }

      return true;
   }

   public static String getArgSeparator() {
      return ARG_SEPARATOR;
   }

   public static void setDistanceThreshold(double distThreshold) {
      defaultThreshold = distThreshold;
   }

   public static void setOutputFileName(String fileName) {
      System.out.println("===============\nSetting File name to " + fileName + "\n===============");
      mOutputFileName = fileName;
   }

   public static void setClusterPreference(String clusterPreference) {
      System.out.println("===============\nSetting cluster distance preference to " + clusterPreference + "\n===============");
      mClusterPreference = clusterPreference;
   }

   public List<ClusterDendogram> getClusterDendograms() {
      return mClustDends;
   }

   public static long getRunTime() {
      return mRunTime;
   }
}
