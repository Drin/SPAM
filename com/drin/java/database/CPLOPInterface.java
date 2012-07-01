package com.drin.java.database;

import com.drin.java.database.CPLOPConnection;

import com.drin.java.dataTypes.Pyroprint;
import com.drin.java.dataTypes.PyroprintCorrelation;
import com.drin.java.dataTypes.Region;
import com.drin.java.dataTypes.Connectivity;

import com.drin.java.dataStructures.PyroprintSimilarityMatrix;

import java.io.File;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public class CPLOPInterface {
   private CPLOPConnection mCplopConn = null;
   private double mLowerThreshold = -1, mUpperThreshold = -1;
   private boolean mTransform = true;

   private final static double DEFAULT_BETA = .95, DEFAULT_ALPHA = .997;

   public CPLOPInterface(double lowerThresh, double upperThresh) {
      mLowerThreshold = lowerThresh;
      mUpperThreshold = upperThresh;

      mCplopConn = new CPLOPConnection();
   }

   public CPLOPInterface() {
      this(DEFAULT_BETA, DEFAULT_ALPHA);
   }

   public Map<Connectivity, PyroprintSimilarityMatrix> getSimMatrices(List<Integer> pyroIdList) {
      Map<Connectivity, PyroprintSimilarityMatrix> simMatrices =
       new HashMap<Connectivity, PyroprintSimilarityMatrix>();

      simMatrices.put(Connectivity.STRONG, buildMatrix(Connectivity.STRONG, pyroIdList));
      simMatrices.put(Connectivity.WEAK, buildMatrix(Connectivity.WEAK, pyroIdList));

      return simMatrices;
   }

   public PyroprintSimilarityMatrix buildMatrix(Connectivity conn, List<Integer> pyroIdList) {
      Map<Integer, Map<Integer, Double>> pyroSimMap = new HashMap<Integer, Map<Integer, Double>>();

      //construct similarity matrix in memory
      for (Integer pyroID : pyroIDList) {
         if (conn == Connectivity.STRONG) {
            pyroSimMap.put(pyroID, mCplopConn.getPyroSimRange(pyroID, mUpperThreshold, 1));
         }
         else if (conn == Connectivity.WEAK) {
            pyroSimMap.put(pyroID, mCplopConn.getPyroSimRange(pyroID, mLowerThreshold, mUpperThreshold));
         }
      }
   }

   public List<Region> getRegions() {
      List<Region> regionList = new ArrayList<Region>();
      Map<String, Map<String, Double>> regThrMap = mCplopConn.getRegionThresholds();

      String alphaThreshold = "alphaThreshold", betaThreshold = "betaThreshold";

      for (String regionName : regThrMap) {
         Double betaCorr = regThrMap.get(regionName).get(betaThreshold);
         Double alphaCorr = regThrMap.get(regionName).get(alphaThreshold);

         regionList.add(new Region(regionName, betaCorr, alphaCorr));
      }

      return regionList;
   }
}
