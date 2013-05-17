package com.drin.java.test;

import com.drin.java.database.CPLOPConnection;
import com.drin.java.database.CPLOPConnection.IsolateDataContainer;

import com.drin.java.ontology.Ontology;

import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

import java.io.File;

public class FastSPAMEvaluationCPU {
   private final ExecutorService mThreadPool;
   private CPLOPConnection mConn;
   private Clusterer mClusterer;
   private Ontology mOntology;
   private Random mRand;

   private static final int ISOLATE_LEN = 188,
                            LEN_23S     = 93,
                            LEN_16S     = 95;

   private static int REGION_LENS[] = new int[] {LEN_23S, LEN_16S};
   private static int REGION_OFFSETS[] = new int[] {0, LEN_23S};

   public FastSPAMEvaluationCPU(Ontology ontology) {
      mThreadPool = Executors.newFixedThreadPool(64);
      mRand = new Random();
      mConn = null;
      mOntology = ontology;
      List<Double> threshList = new ArrayList<Double>();
      threshList.add(0.85);
      threshList.add(0.75);

      mClusterer = new OHClusterer(ontology, threshList);

      try {
         mConn = new CPLOPConnection();
      }
      catch (Exception err) {
         err.printStackTrace();
      }
   }

   public static void main(String[] args) {
      FastSPAMEvaluationCPU runner = new FastSPAMEvaluationCPU(null);
      Ontology clust_ont = Ontology.createOntology(
         new File("ontologies/specific.ont")
      );

      System.out.println(clust_ont);

      int initSize = 10000, upSize = 100, numUps = 100;
      Map<Integer, Map<Integer, Integer>> simMapping;

      long start = System.currentTimeMillis();

      try {
         IsolateDataContainer container = runner.getIsolateData(clust_ont, initSize, upSize, numUps);

         //create a lookup table for isolate IDs to a spot in the packed similarity matrix
         simMapping = new HashMap<Integer, Map<Integer, Integer>>(container.isoIDs.length);
         int simNdx = 0;

         for (int ndxA = 0; ndxA < container.isoIDs.length; ndxA++) {
            Map<Integer, Integer> tmpMap = new HashMap<Integer, Integer>(
                  container.isoIDs.length - (ndxA + 1)
            );
            simMapping.put(ndxA, tmpMap);

            for (int ndxB = ndxA + 1; ndxB < container.isoIDs.length; ndxB++) {
               tmpMap.put(ndxB, simNdx++);
            }
         }

         //populate the packed similarity matrix
         float simMatrix[] = runner.calculateSimMatrix(container.isoIDs, container.isoData);
      }
      catch (Exception ex) { ex.printStackTrace(); }

      System.out.printf("took about %d ms\n", System.currentTimeMillis() - start);
   }

   public IsolateDataContainer getIsolateData(Ontology ont, int initSize, int upSize, int numUps) {
      int dataSize = initSize + (upSize * numUps);
      IsolateDataContainer dataContainer = null;

      try {
         dataContainer = mConn.getIsolateData(dataSize);
         dataContainer.isoMeta = mConn.getIsolateMetaData(dataContainer.isoIDs, ont, dataSize);
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
      catch (Exception err) { err.printStackTrace(); }

      return dataContainer;
   }

   public float[] calculateSimMatrix(int[] isoIDs, float[] isoData) {
      private int simMatrixSize = (isoIDs.length * (isoIDs.length - 1)) / 2, simNdx = 0;
      private float simMatrix[]  = new float[simMatrixSize];

      try {
         for (int isoNdxA = 0; isoNdxA < isoIDs.length; isoNdxA++) {
            for (int isoNdxB = isoNdxA + 1; isoNdxB < isoIDs.length; isoNdxB++) {
               new PearsonCalculator() implements Runnable {
                  private float pearsonSum = 0.0f;
                  private float peakHeightA = 0.0f, peakHeightB = 0.0f;
                  private float sumA = 0.0f, sumB = 0.0f, sumAB = 0.0f,
                                sumASquared = 0.0f, sumBSquared = 0.0f;

                  public void run() {
                     pearsonSum = 0.0f;

                     for (int regNdx = 0; regNdx < 2; regNdx++) {
                        sumA = 0.0f;
                        sumB = 0.0f;
                        sumAB = 0.0f;
                        sumASquared = 0.0f;
                        sumBSquared = 0.0f;

                        for (int peakNdx = 0; peakNdx < REGION_LENS[regNdx]; peakNdx++) {

                           peakHeightA = isoData[isoNdxA * ISOLATE_LEN + peakNdx +
                                                 REGION_OFFSETS[regNdx]];

                           peakHeightB = isoData[isoNdxB * ISOLATE_LEN + peakNdx +
                                                 REGION_OFFSETS[regNdx]];

                           sumA += peakHeightA;
                           sumB += peakHeightB;
                           sumASquared += peakHeightA * peakHeightA;
                           sumBSquared += peakHeightB * peakHeightB;
                           sumAB += peakHeightA * peakHeightB;

                        }

                        pearsonSum += (REGION_LENS[regNdx] * sumAB - sumA * sumB) /
                            Math.sqrt((REGION_LENS[regNdx] * sumASquared - sumA * sumA) * 
                                      (REGION_LENS[regNdx] * sumBSquared - sumB * sumB));
                     }

                     simMatrix[simNdx++] = pearsonSum / 2;
                  }
               }
            }
         }
      }
      catch (IOException ioErr) {
         ioErr.printStackTrace();
      }

      return simMatrix;
   }
}
