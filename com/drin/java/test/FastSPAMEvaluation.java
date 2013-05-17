package com.drin.java.test;

import static jcuda.driver.JCudaDriver.*;

import com.drin.java.database.SpecialCPLOPConnection;
import com.drin.java.database.SpecialCPLOPConnection.IsolateDataContainer;

import com.drin.java.ontology.Ontology;

import com.drin.java.analysis.clustering.Clusterer;
import com.drin.java.analysis.clustering.OHClusterer;

import java.io.*;
import jcuda.*;
import jcuda.driver.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

import java.io.File;

public class FastSPAMEvaluation {
   private SpecialCPLOPConnection mConn;
   private Clusterer mClusterer;
   private Ontology mOntology;
   private Random mRand;

   private static final int ISOLATE_LEN = 188,
                            DEFAULT_DEVICE = 3;

   public FastSPAMEvaluation(Ontology ontology) {
      mRand = new Random();
      mConn = null;
      mOntology = ontology;
      List<Double> threshList = new ArrayList<Double>();
      threshList.add(0.85);
      threshList.add(0.75);

      mClusterer = new OHClusterer(ontology, threshList);

      try {
         mConn = new SpecialCPLOPConnection();
      }
      catch (Exception err) {
         err.printStackTrace();
      }
   }

   public static void main(String[] args) {
      FastSPAMEvaluation runner = new FastSPAMEvaluation(null);
      Ontology clust_ont = Ontology.createOntology(
         new File("ontologies/specific.ont")
      );

      int initSize = 100, upSize = 10, numUps = 1;
      Map<Integer, Map<Integer, Integer>> simMapping;

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

         //do whatevers
         int valsOnLine = container.isoIDs.length, valNdx = 0;
         for (simNdx = 0; simNdx < simMatrix.length; simNdx++) {
            if (valNdx++ == valsOnLine) {
               System.out.printf("\n");
               valsOnLine--;
               break;

               /*
               for (int fillNdx = 0; fillNdx < container.isoIDs.length - valsOnLine; fillNdx++) {
                  System.out.print("\t,");
               }
               */
            }

            System.out.printf("\t%.04f,", simMatrix[simNdx]);
         }

         System.out.println("");
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public IsolateDataContainer getIsolateData(Ontology ont, int initSize, int upSize, int numUps) {
      int dataSize = initSize + (upSize * numUps);
      IsolateDataContainer dataContainer = null;

      try {
         dataContainer = mConn.getIsolateData(dataSize);
         dataContainer.isoMeta = mConn.getIsolateMetaData(dataContainer.isoIDs, ont, dataSize);
      }
      catch (java.sql.SQLException sqlErr) {
         sqlErr.printStackTrace();
      }
      catch (Exception err) {
         err.printStackTrace();
      }

      return dataContainer;
   }

   public float[] calculateSimMatrix(int[] isoIDs, float[] isoData) {
      int gridDim[] = new int[] {64, 64, 1};
      int blockDim[] = new int[] {32, 32, 1};
      int tSize[] = new int[] {(isoIDs.length / gridDim[0] * blockDim[0]) + 1,
                               (isoIDs.length / gridDim[1] * blockDim[1]) + 1};

      int sharedMemSize = 0, simMatrixSize = (isoIDs.length * (isoIDs.length - 1)) / 2;
      int isoRowLen = tSize[0] * ISOLATE_LEN, isoColLen = tSize[1] * ISOLATE_LEN;

      float isoDataRowCPU[] = new float[isoRowLen],
            isoDataColCPU[] = new float[isoColLen],
            simMatrixCPU[]  = new float[simMatrixSize];

      CUfunction cudaKernel = loadModuleFunction("pearson.cu", "pearson");

      //iso data on device for a tile for the row and column
      CUdeviceptr isoDataRowGPU = new CUdeviceptr(), isoDataColGPU = new CUdeviceptr();
      cuMemAlloc(isoDataRowGPU, isoRowLen * Sizeof.FLOAT);
      cuMemAlloc(isoDataColGPU, isoColLen * Sizeof.FLOAT);

      //simMatrix on device
      CUdeviceptr simMatrixGPU = new CUdeviceptr();
      cuMemAlloc(simMatrixGPU, simMatrixSize * Sizeof.FLOAT);

      for (int tRow = 0; tRow < (isoIDs.length / tSize[0]) + 1; tRow++) {
         for (int rowPeak = 0; rowPeak < isoRowLen; rowPeak++) {
            isoDataRowCPU[rowPeak] = isoData[tRow * tSize[0] + rowPeak];
            /*
            System.out.printf("[%d] copying row val: %.04f\n", tRow,
                              isoData[tRow * tSize[0] + rowPeak]);
                              */
         }

         cuMemcpyHtoD(isoDataRowGPU, Pointer.to(isoDataRowCPU),
                      isoRowLen * Sizeof.FLOAT);

         for (int tCol = tRow; tCol < (isoIDs.length / tSize[1]) + 1; tCol++) {
            for (int colPeak = 0; colPeak < isoColLen; colPeak++) {
               isoDataColCPU[colPeak] = isoData[tCol * tSize[1] + colPeak];
            }

            cuMemcpyHtoD(isoDataColGPU, Pointer.to(isoDataColCPU),
                         isoColLen * Sizeof.FLOAT);

            Pointer kernelParams = Pointer.to(
               Pointer.to(new int[] {tSize[0]}),
               Pointer.to(isoDataRowGPU),
               Pointer.to(new int[] {tSize[1]}),
               Pointer.to(isoDataColGPU),
               Pointer.to(new int[] {tRow}),
               Pointer.to(new int[] {tCol}),
               Pointer.to(new int[] {isoIDs.length}),
               Pointer.to(new int[] {simMatrixSize}),
               Pointer.to(simMatrixGPU)
            );

            //no shared memory stream or extra params
            cuLaunchKernel(cudaKernel,
               gridDim[0], gridDim[1], gridDim[2],
               blockDim[0], blockDim[1], blockDim[2],
               sharedMemSize, null, kernelParams, null
            );

            //wait for cuda to finish computing
            cuCtxSynchronize();
            break;
         }
      }

      //copy data back from device
      cuMemcpyDtoH(Pointer.to(simMatrixCPU), simMatrixGPU, simMatrixSize * Sizeof.FLOAT);

      for (int i = 0; i < 10; i++) {
         System.out.println(simMatrixCPU[0]);
      }

      //free cuda memory
      cuMemFree(isoDataRowGPU);
      cuMemFree(isoDataColGPU);
      cuMemFree(simMatrixGPU);

      return simMatrixCPU;
   }

   private void prepJCuda(int deviceNum) {
      JCudaDriver.setExceptionsEnabled(true);

      cuInit(0);
      CUcontext cudaContext = new CUcontext();
      CUdevice cudaDev = new CUdevice();

      if (deviceNum == -1) {
         cuDeviceGet(cudaDev, DEFAULT_DEVICE);
      }
      else if (deviceNum != -1) {
         int[] devNumPtr = new int[1];
         cuDeviceGetCount(devNumPtr);

         if (deviceNum < devNumPtr[0]) { cuDeviceGet(cudaDev, deviceNum); }
      }

      cuCtxCreate(cudaContext, 0, cudaDev);
   }

   private CUfunction loadModuleFunction(String moduleName, String kernelName) {
      CUmodule cudaModule = null;
      CUfunction cudaKernel = null;

      try {
         String ptxFileName = preparePtxFile(moduleName);

         cudaModule = new CUmodule();
         cudaKernel = new CUfunction();

         cuModuleLoad(cudaModule, ptxFileName);
         cuModuleGetFunction(cudaKernel, cudaModule, kernelName);
      }
      catch(IOException err) {
         err.printStackTrace();
         System.exit(0);
      }

      return cudaKernel;
   }

   private String preparePtxFile(String cuFileName) throws IOException {
      int endIndex = cuFileName.lastIndexOf('.');
      if (endIndex == -1) {
         endIndex = cuFileName.length() - 1;
      }
      String ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx";

      File cuFile = new File(cuFileName);
      if (!cuFile.exists()) { throw new IOException(cuFileName + " not found."); }

      String command = String.format(
         "nvcc -arch=sm_30 -v -m%s -ptx %s -o %s",
         System.getProperty("sun.arch.data.model"),
         cuFile.getPath(), ptxFileName
      );

      System.out.println("Executing\n" + command);
      Process process = Runtime.getRuntime().exec(command);

      String errMsg = new String(toByteArray(process.getErrorStream()));
      String outputMsg = new String(toByteArray(process.getInputStream()));

      int exitValue = 0;
      try { exitValue = process.waitFor(); }
      catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new IOException ("Interrupted; waiting for nvcc output", e);
      }

      if (exitValue != 0) {
         System.out.printf("nvcc process exitValue %s\n" +
                           "errorMessage:\n%s\n" +
                           "outputMessage:\n%s\n",
                           exitValue, errMsg, outputMsg);
         throw new IOException("Could not create .ptx file: " + errMsg);
      }

      System.out.println("Finished creating PTX file");
      return ptxFileName;
   }

   private static byte[] toByteArray(InputStream inputStream) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte buffer[] = new byte[8192];
      while (true) {
         int read = inputStream.read(buffer);
         if (read == -1) {
            break;
         }
         baos.write(buffer, 0, read);
      }

      return baos.toByteArray();
   }
}
