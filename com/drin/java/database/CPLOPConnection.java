package com.drin.java.database;

import com.drin.java.ontology.FastOntology;

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
   private static final short DEFAULT_PAGE_SIZE = 10000,
                              ISOLATE_LEN       = 188;
   private static final byte LEN_23S           = 93,
                             LEN_16S           = 95;

   private Connection mConn;

   private static final String
      SCHEMA_QUERY = "SELECT distinct(%s) " +
                     "FROM %s " +
                     "WHERE %s IS NOT NULL",
      DATA_QUERY = "SELECT i.test_isolate_id, h1.pHeight, h2.pHeight, h1.position " +
                   "FROM test_histograms h1 " +
                        "JOIN isolate_selection i ON ( " +
                           "h1.pyroID = i.pyro_id_1 AND " +
                           "h1.position < 95) " +
                        "JOIN test_histograms h2 ON ( " +
                           "h1.position = h2.position AND " +
                           "h2.pyroID = i.pyro_id_2) " +
                   "ORDER BY i.test_isolate_id " +
                   "LIMIT %d OFFSET %d",
      META_QUERY = "SELECT distinct test_isolate_id %s " +
                   "FROM test_isolates join test_pyroprints using (" +
                         "name_prefix, name_suffix) " +
                   "WHERE test_isolate_id in (%s) " +
                   "ORDER BY test_isolate_id " +
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

   public IsolateDataContainer getIsolateData(int dataSize) throws SQLException {
      return getIsolateData(dataSize, DEFAULT_PAGE_SIZE);
   }

   public IsolateDataContainer getIsolateData(int dataSize, short pageSize) throws SQLException {
      Statement statement = null;
      ResultSet results = null;

      int peakDataSize = dataSize * LEN_16S;

      float[] iso_data = new float[dataSize * ISOLATE_LEN];
      int[] iso_ids = new int[dataSize];
      int iso_id = -1, tmp_iso_id = -1;
      short peak_ndx = 0, isolate_ndx = -1;

      try {
         for (int pageNdx = 0; pageNdx < Math.ceil((float) peakDataSize / pageSize); pageNdx++) {
            statement = mConn.createStatement();
            results = statement.executeQuery(String.format(DATA_QUERY,
               Math.min(pageSize, peakDataSize - (pageSize * pageNdx)),
               (pageSize * pageNdx)
            ));

            while (results.next()) {
               tmp_iso_id = results.getInt(1);

               if (iso_id == -1 || tmp_iso_id != iso_id) {
                  isolate_ndx++;
                  iso_id = tmp_iso_id;
                  peak_ndx = 0;

                  if (isolate_ndx < iso_ids.length) {
                     iso_ids[isolate_ndx] = iso_id;
                  }
               }

               if (peak_ndx != results.getInt(4)) {
                  System.err.printf("peak mismatch! on %d, should be %d\n",
                                    results.getInt(4), peak_ndx);
                  System.exit(0);
               }

               if (peak_ndx < LEN_23S && isolate_ndx < dataSize) {
                  iso_data[isolate_ndx * ISOLATE_LEN + peak_ndx] = results.getFloat(2);
               }

               if (peak_ndx < LEN_16S && isolate_ndx < dataSize) {
                  iso_data[isolate_ndx * ISOLATE_LEN + LEN_23S + peak_ndx] = results.getFloat(3);
               }

               peak_ndx++;
            }
         }
      }
      catch (Exception err) {
         err.printStackTrace();
      }
      finally {
         if (statement != null) { statement.close(); }
         if (results != null) { results.close(); }
      }

      return new IsolateDataContainer(iso_ids, iso_data);
   }

   public String[][] getIsolateMetaData(int[] ids, FastOntology ont, int dataSize) {
      Statement statement = null;
      ResultSet results = null;

      String metaLabels[][] = new String[ids.length][], colArr[] = ont.getColumns();
      String metaIDs = "", metaColumns = "";
      int tmp_id = -1, isolateID = -1, numColumns = ont.getNumCols();
      int isolateNdx = -1, pageSize = DEFAULT_PAGE_SIZE;
      byte colOffset = 2;

      for (byte colNdx = 0; colNdx < ont.getNumCols(); colNdx++) {
            metaColumns += "," + colArr[colNdx];
      }

      for (int id_ndx = 0; id_ndx < ids.length; id_ndx++) { metaIDs += "," + ids[id_ndx]; }

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
               tmp_id = results.getInt(1);

               if (isolateID == -1 || tmp_id != isolateID) {
                  isolateID = tmp_id;
                  isolateNdx++;

                  if (ids[isolateNdx] != isolateID) {
                     System.err.printf("meta data mismatch for isolate %d\n",
                                       isolateID);
                     continue;
                  }
               }

               metaLabels[isolateNdx] = new String[numColumns];

               for (byte colNdx = 0; colNdx < numColumns; colNdx++) {
                  metaLabels[isolateNdx][colNdx] =
                     String.valueOf(results.getObject(colNdx + colOffset)).trim();
               }
            }
         }
      }
      catch (java.sql.SQLException sqlErr) {
         sqlErr.printStackTrace();
      }

      return metaLabels;
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
         "VALUES (%d, ?, '%s', '%s', %.04f, %d, %s)",
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

   public class IsolateDataContainer {
      public int[] isoIDs;
      public float[] isoData;
      public String[][] isoMeta;

      public IsolateDataContainer(int[] ids, float[] data) {
         isoIDs = ids;
         isoData = data;
      }
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
