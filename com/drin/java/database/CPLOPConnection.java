package com.drin.java.database;

import com.drin.java.ontology.Ontology;

import com.drin.java.biology.Pyroprint;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.IsolateAverageMetric;
import com.drin.java.metrics.ITSRegionAverageMetric;
import com.drin.java.metrics.PyroprintUnstablePearsonMetric;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CPLOPConnection {
   private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
   private static final String DB_URL = "jdbc:mysql://localhost/CPLOP?autoReconnect=true";
   private static final String DB_USER = "";
   private static final String DB_PASS = "";
   private static final short DEFAULT_PAGE_SIZE = 10000;

   private Connection mConn;

   private static final String
      SCHEMA_QUERY = "SELECT distinct(%s) " +
                     "FROM %s " +
                     "WHERE %s IS NOT NULL",

      //DATA_QUERY = "SELECT i.isoID, p1.pyroID, p2.pyroID, h1.pHeight, " +
      //                    "h2.pHeight, h1.position " +
      DATA_QUERY = "SELECT i.isoID, p1.pyroID, p1.appliedRegion, p1.dsName, " +
                          "h1.pHeight, h1.position " +
                   "FROM Isolates i " +
                        "JOIN Pyroprints p1 ON ( " +
                           "i.isoID = p1.isoID " +
                           //"p1.appliedRegion = '23-5'" +
                        ") " +
                        /*
                        "JOIN Pyroprints p2 ON ( " +
                           "i.isoID = p2.pyroID AND " +
                           "p2.appliedRegion = '16-23'" +
                        ") " +
                        */
                        "JOIN Histograms h1 ON ( " +
                           "p1.pyroID = h1.pyroID AND " +
                           "h1.position < %d" +
                        ") " +
                        /*
                        "JOIN Histograms h2 ON ( " +
                           "p2.pyroID = h2.pyroID AND " +
                           "h2.position < %d AND" +
                           "h1.position = h2.position " +
                        ") " +
                        */
                   //"ORDER BY i.isoID, p1.pyroID, p2.pyroID, position " +
                   "ORDER BY i.isoID, p1.pyroID, position " +
                   "LIMIT %d OFFSET %d",

      META_QUERY = "SELECT distinct isoID %s " +
                   "FROM Isolates join Pyroprints using (isoID) " +
                   "WHERE isoID in (%s) " +
                   "ORDER BY isoID " +
                   "LIMIT %d OFFSET %d";

   public CPLOPConnection() throws SQLException, DriverException {
      try {
         Class.forName(DB_DRIVER);

         mConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
      }
      catch (ClassNotFoundException classEx) {
         throw new DriverException("Unable to instantiate DB Driver: " + DB_DRIVER);
      }
   }

   public List<String> getDistinctValues(String tableName, String colName) throws SQLException {
      List<String> distinctValues = new ArrayList<String>();
      Statement statement = null;
      ResultSet results = null;

      String query = String.format(SCHEMA_QUERY, colName, tableName, colName);

      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) { distinctValues.add(results.getString(1).trim()); }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally {
         if (results != null) { results.close(); }
         if (statement != null) { statement.close(); }
      }

      return distinctValues;
   }

   public List<Isolate> getIsolateData(int dataSize) throws SQLException {
      return getIsolateData(dataSize, DEFAULT_PAGE_SIZE);
   }

   public List<Isolate> getIsolateData(int dataSize, short pageSize) throws SQLException {
      Statement statement = null;
      ResultSet results = null;

      DataMetric<Isolate> isoMetric = new IsolateAverageMetric();
      DataMetric<ITSRegion> regionMetric = new ITSRegionAverageMetric();
      DataMetric<Pyroprint> pyroMetric = new PyroprintUnstablePearsonMetric();

      byte pyroLen = 95;
      List<Isolate> isoData = new ArrayList<Isolate>(dataSize);
      String isoId = null, tmpIsoId = null, regName = null, dsName = null;
      int peakDataSize = dataSize * pyroLen, pyroId = -1, tmpPyroId = -1;

      Isolate tmpIso = null;
      ITSRegion tmpRegion = null;
      Pyroprint tmpPyro = null;

      try {
         for (int pageNdx = 0; pageNdx < Math.ceil((float) peakDataSize / pageSize); pageNdx++) {
            statement = mConn.createStatement();
            //3 Data Query Variables:
            //    Length of Pyroprint
            //    Page size
            //    Page offset
            results = statement.executeQuery(String.format(DATA_QUERY,
               pyroLen,
               Math.min(pageSize, peakDataSize - (pageSize * pageNdx)),
               (pageSize * pageNdx)
            ));

            while (results.next()) {
               tmpIsoId = results.getString(1);
               tmpPyroId = results.getInt(2);
               regName = results.getString(3);
               dsName = results.getString(4);
               float pHeight = results.getFloat(5);
               byte position = results.getByte(6);

               byte dispLen = -1;
               if (regName.equals("16-23")) { dispLen = 95; }
               else if (regName.equals("23-5")) { dispLen = 93; }

               if (isoId == null || !tmpIsoId.equals(isoId)) {
                  isoId = tmpIsoId;
                  tmpIso = new Isolate(isoId, isoMetric);
                  isoData.add(tmpIso);
               }

               if (tmpIso != null) {
                  tmpIso.getData().add(new ITSRegion(regName, regionMetric));
               }

               if (tmpPyroId != pyroId) {
                  pyroId = tmpPyroId;
                  tmpPyro = new Pyroprint(String.valueOf(pyroId),
                                          dispLen, dsName, pyroMetric);
               }

               if (tmpPyro != null) {
                  if (!tmpPyro.addDispensation(position, pHeight)) {
                     if (position < dispLen) {
                        System.out.println("pyroprint peak mismatch!");
                        System.out.printf("Found peak %d, expected %d\n", position,
                                          tmpPyro.getData().size());
                     }
                  }
               }
            }
         }
      }
      catch (Exception err) { err.printStackTrace(); }
      finally {
         if (statement != null) { statement.close(); }
         if (results != null) { results.close(); }
      }

      return isoData;
   }

   public void getIsolateMetaData(List<Isolate> isoData, Ontology ont, int dataSize) {
      Statement statement = null;
      ResultSet results = null;

      String metaLabels[] = null;
      String metaIDs = "", metaColumns = "", isoId = null, tmpId = null;
      int isoNdx = -1, numColumns = ont.getNumCols();
      int pageSize = DEFAULT_PAGE_SIZE;
      byte colOffset = 2;

      for (String metaCol : ont.getColumns()) { metaColumns += "," + metaCol; }
      for (int ndx = 0; ndx < isoData.size(); ndx++) {
         metaIDs += ",'" + isoData.get(ndx).getName() + "'";
      }

      try {
         for (int pageNdx = 0; pageNdx < Math.ceil((float) dataSize / pageSize); pageNdx++) {
            System.out.println(String.format(META_QUERY,
               metaColumns, metaIDs.substring(1),
               Math.min(pageSize, dataSize - (pageNdx * pageSize)),
               pageNdx * pageSize
            ));
            statement = mConn.createStatement();
            results = statement.executeQuery(String.format(META_QUERY,
               metaColumns, metaIDs.substring(1),
               Math.min(pageSize, dataSize - (pageNdx * pageSize)),
               pageNdx * pageSize
            ));

            while (results.next()) {
               tmpId = results.getString(1);

               if (isoId == null || !tmpId.equals(isoId)) {
                  isoId = tmpId;
                  isoNdx++;

                  if (!isoData.get(isoNdx).getName().equals(isoId)) {
                     System.err.printf("meta data mismatch for isolate %s\n", isoId);
                     continue;
                  }
               }

               metaLabels = new String[numColumns];

               for (byte colNdx = 0; colNdx < numColumns; colNdx++) {
                  metaLabels[colNdx] =
                     String.valueOf(results.getObject(colNdx + colOffset)).trim();
               }

               isoData.get(isoNdx).setMetaData(metaLabels);
            }
         }
      }
      catch (java.sql.SQLException sqlErr) { sqlErr.printStackTrace(); }
   }

   public void insertRunPerf(String insertValues) throws SQLException {
      PreparedStatement insertSQL = null;

      try {
         insertSQL = mConn.prepareStatement(String.format(
            "INSERT IGNORE INTO test_run_performance(test_run_id, update_id, " +
                                             "update_size, run_time) " +
            "VALUES %s", insertValues
         ));
         insertSQL.executeUpdate();
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally { if (insertSQL != null) { insertSQL.close(); } }
   }

   public void insertIsolateAndStrainData(String strainValues, String isolateValues) throws SQLException {
      PreparedStatement strainSQL = null, isolateSQL = null;

      String strainInsert = "INSERT IGNORE INTO test_run_strain_link (test_run_id, cluster_id, " +
                                                              "cluster_threshold, strain_diameter, " +
                                                              "average_isolate_similarity, " +
                                                              "percent_similar_isolates, " +
                                                              "update_id) " +
                            "VALUES %s";
      String isolateInsert = "INSERT IGNORE INTO test_isolate_strains(test_run_id, cluster_id, " +
                                                               "cluster_threshold, test_isolate_id, " +
                                                               "update_id) " +
                             "VALUES %s";

      try {
         if (!strainValues.equals("")) {
            strainSQL = mConn.prepareStatement(String.format(
               strainInsert, strainValues
            ));
            strainSQL.executeUpdate();
         }

         if (!isolateValues.equals("")) {
            isolateSQL = mConn.prepareStatement(String.format(
               isolateInsert, isolateValues
            ));
            isolateSQL.executeUpdate();
         }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally {
         if (strainSQL != null) { strainSQL.close(); }
         if (isolateSQL != null) { isolateSQL.close(); }
      }
   }

   private String getElapsedTime(long clusterTime) {
      long hours = clusterTime / 3600000;
      long minutes = (clusterTime % 3600000) / 60000;
      long seconds = ((clusterTime % 3600000) % 60000) / 1000;

      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
   }

   public void insertTestRun(int runID, long runTime, String algorith, String ontName,
                             float interStrainSim, byte use_transform) throws SQLException {
      Timestamp runDate = new Timestamp(new Date().getTime());
      PreparedStatement insertSQL = null;

      String insertQuery = String.format(
         "INSERT IGNORE INTO test_runs (test_run_id, run_date, run_time, cluster_algorithm, " +
                                       "average_strain_similarity, use_transform, ontology) " +
         "VALUES (%d, ?, '%s', '%s', %.04f, %d, '%s')",
         runID, getElapsedTime(runTime), algorith, interStrainSim,
         use_transform, ontName
      );

      try {
         insertSQL = mConn.prepareStatement(insertQuery);
         insertSQL.setTimestamp(1, runDate);
         insertSQL.executeUpdate();
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally { if (insertSQL != null) { insertSQL.close(); } }
   }

   public void insertTestRun(long runTime, String algorith, float interStrainSim,
                             byte use_transform) throws SQLException {
      Timestamp runDate = new Timestamp(new Date().getTime());
      PreparedStatement insertSQL = null;

      String insertQuery = String.format(
         "INSERT IGNORE INTO test_runs (run_date, run_time, cluster_algorithm, " +
                                "average_strain_similarity, use_transform) " +
         "VALUES (?, '%s', '%s', %.04f, %d)",
         getElapsedTime(runTime), algorith, interStrainSim, use_transform
      );

      try {
         insertSQL = mConn.prepareStatement(insertQuery);
         insertSQL.setTimestamp(1, runDate);
         insertSQL.executeUpdate();
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally { if (insertSQL != null) { insertSQL.close(); } }
   }

   public int getLastRunId() throws SQLException {
      Statement stmt = null;
      ResultSet results = null;
      int newRunId = -1;

      String query = String.format("SELECT last_insert_id()");

      try {
         stmt = mConn.createStatement();
         results = stmt.executeQuery(query);
         int numRows = 0;

         while (results.next()) {
            newRunId = results.getInt(1);
            numRows++;
         }

         if (numRows > 1) {
            System.err.printf("Retrieving last insert id " +
                              "returned multiple rows\n");
         }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally
      {
         if (results != null) { results.close(); }
         if (stmt != null) { stmt.close(); }
      }

      return newRunId;
   }

   /**
    * Exception for when there is a problem with the DB driver.
    */
   @SuppressWarnings("serial")
   public class DriverException extends Exception
   {
      public DriverException(String msg)
      {
         super(msg);
      }
   }
}
