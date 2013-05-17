package com.drin.java.database;

import com.drin.java.ontology.Ontology;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CPLOPConnection {
   private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
   private static final String DB_URL = "jdbc:mysql://localhost:8906/CPLOP?autoReconnect=true";
   private static final String DB_USER = "drin";
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
      ID_QUERY = "SELECT * " +
                 "FROM (SELECT test_isolate_id, pyro_id_1, pyro_id_2 " +
                       "FROM isolate_selection %s " +
                       "LIMIT %d OFFSET %d) ids " +
                 "ORDER BY test_isolate_id",
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
      META_QUERY = "SELECT test_isolate_id %s " +
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

      String query = String.format(
         "SELECT distinct(%s) FROM %s WHERE %s IS NOT NULL",
         colName, tableName, colName
      );

      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) { distinctValues.add(results.getString(1)); }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally {
         if (results != null) { results.close(); }
         if (statement != null) { statement.close(); }
      }

      return distinctValues;
   }

   public IsolateDataContainer getIsolateData(short dataSize) throws SQLException {
      return getIsolateData(dataSize, DEFAULT_PAGE_SIZE);
   }

   public IsolateDataContainer getIsolateData(short dataSize, short pageSize) throws SQLException {
      Statement statement = null;
      ResultSet results = null;

      int peakDataSize = dataSize * LEN_16S;

      float[] iso_data = new float[dataSize * ISOLATE_LEN];
      int[] iso_ids = new int[dataSize];
      int iso_id = -1, tmp_iso_id = -1;
      short peak_ndx = 0, isolate_ndx = -1;

      try {
         for (short pageNdx = 0; pageNdx < Math.ceil((float) peakDataSize / pageSize); pageNdx++) {
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
                  System.err.printf("%s, %s, %s, %s, %d\n",
                                    String.valueOf(results.getObject(1)),
                                    String.valueOf(results.getObject(2)),
                                    String.valueOf(results.getObject(3)),
                                    String.valueOf(results.getObject(4)),
                                    peak_ndx);
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

   public String[][] getIsolateMetaData(int[] ids, Ontology ont, short dataSize) throws SQLException {
      return getIsolateMetaData(ids, ont, dataSize, DEFAULT_PAGE_SIZE);
   }

   public String[][] getIsolateMetaData(int[] ids, Ontology ont, short dataSize, short pageSize) {
      Statement statement = null;
      ResultSet results = null;
      String metaColumns = "", metaIDs = "";
      String metaLabels[][] = new String[ids.length][];
      int tmp_id = -1, isolateID = -1;
      short isolateNdx = -1;
      byte numColumns = 0;

      for (Map.Entry<String, Set<String>> ont_entry : ont.getTableColumns().entrySet()) {
         for (String col_name : ont_entry.getValue()) {
            metaColumns += "," + col_name;
            numColumns++;
         }
      }

      for (int id_ndx = 0; id_ndx < ids.length; id_ndx++) {
         metaIDs += "," + ids[id_ndx];
      }

      try {
         for (int pageNdx = 0; pageNdx < Math.ceil((float) dataSize / pageSize); pageNdx++) {
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
               for (byte colNdx = 2; colNdx <= numColumns; colNdx++) {
                  if (colNdx >= metaLabels[isolateNdx].length) {
                     String[] newArr = new String[metaLabels[isolateNdx].length * 2];
                     for (byte metaNdx = 0; metaNdx < metaLabels[isolateNdx].length; metaNdx++) {
                        newArr[metaNdx] = metaLabels[isolateNdx][metaNdx];
                     }
                  }

                  metaLabels[isolateNdx][colNdx - 2] = String.valueOf(results.getObject(colNdx));
               }
            }
         }
      }
      catch (java.sql.SQLException sqlErr) {
         sqlErr.printStackTrace();
      }

      return metaLabels;
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

   /*
    * non ontology legacy methods
    */
   public List<Map<String, Object>> getDataByIsoID(String isoIds) throws SQLException
   {
      String searchID = "isoID";
      return getData(searchID, isoIds);
   }

   public List<Map<String, Object>> getDataByPyroID(String pyroIds) throws SQLException
   {
      String searchID = "pyroID";
      return getData(searchID, pyroIds);
   }

   /*
    * Legacy methods
   public List<Map<String, Object>> getDataByIsoID(Ontology ont, String isoIds)
   throws SQLException
   {
      String searchID = "isoID";
      return getData(ont, searchID, isoIds);
   }

   public List<Map<String, Object>> getDataByPyroID(Ontology ont, String pyroIds)
   throws SQLException
   {
      String searchID = "pyroID";
      return getData(ont, searchID, pyroIds);
   }
    */

   public List<Map<String, Object>> getDataByExperimentName(String experiments) throws SQLException
   {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = String.format(
       "SELECT pyroID, isoID, appliedRegion, wellID, pHeight, nucleotide " +
       "FROM Experiments e join ExperimentPyroPrintLink ep using (ExperimentID) " +
             "join Pyroprints p on (PyroprintID = pyroID) join Histograms using (pyroID) " +
       "WHERE e.name in (%s) and pyroID in (Select distinct pyroID from Histograms)" + 
       "ORDER BY isoID, pyroID, position asc", experiments);

      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) {
            Map<String, Object> tuple = new HashMap<String, Object>();

            tuple.put("pyroprint", results.getString(1)); 
            tuple.put("isolate", results.getString(2)); 
            tuple.put("region", results.getString(3)); 
            tuple.put("well", results.getString(4));
            tuple.put("pHeight", results.getString(5));
            tuple.put("nucleotide", results.getString(6));

            rtn.add(tuple);
         }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally
      {
         if (results != null) { results.close(); }
         if (statement != null) { statement.close(); }
      }

      return rtn;
   }

   public List<Map<String, Object>> getExperimentDataSet() throws SQLException {
      List<Map<String, Object>> experimentMap = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT Name, count(*) as NumIsolates " +
                     "FROM Experiments e join ExperimentPyroPrintLink ep using (ExperimentID) join " +
                           "Pyroprints p on (PyroprintID = pyroID) " +
                     "GROUP BY Name";

      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) {
            Map<String, Object> tuple = new HashMap<String, Object>();

            tuple.put("name", results.getString(1));
            tuple.put("isolate count", new Integer(results.getInt(2)));

            experimentMap.add(tuple);
         }
      }

      catch (SQLException sqlEx) {
         throw sqlEx;
      }

      finally {
         if (results != null) {
            results.close();
         }

         if (statement != null) {
            statement.close();
         }
      }

      return experimentMap;
   }

   private List<Map<String, Object>> getData(String searchID, String searchSet) throws SQLException {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = String.format(
       "SELECT pyroID, isoID, appliedRegion, wellID, pHeight, nucleotide " +
       "FROM Pyroprints join Isolates using (isoID) join Histograms using (pyroID) " +
       "WHERE %s in (%s) and pyroID in (Select distinct pyroID from Histograms)" + 
       "ORDER BY isoID, pyroID, position asc",
       searchID, searchSet);

      //System.err.println("query:\n" + query);
      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) {
            Map<String, Object> tuple = new HashMap<String, Object>();
            tuple.put("pyroprint", results.getString(1)); 
            tuple.put("isolate", results.getString(2)); 
            tuple.put("region", results.getString(3)); 
            tuple.put("well", results.getString(4));
            tuple.put("pHeight", results.getString(5));
            tuple.put("nucleotide", results.getString(6));

            rtn.add(tuple);
         }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally
      {
         if (results != null) { results.close(); }
         if (statement != null) { statement.close(); }
      }

      return rtn;
   }

   public List<Map<String, Object>> getIsolateDataSetWithBothRegions() throws SQLException {
      List<Map<String, Object>> isolateMap = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT i.isoID, i.commonName, i.hostID, i.sampleID, " +
                     "i.dateStored, i.pyroprintDate " +
                     "FROM Isolates i join " +
                           "Pyroprints p1 using (isoID) join " +
                           "Pyroprints p2 using (isoID) " +
                     "WHERE p1.appliedRegion != p2.appliedRegion and " +
                           "p1.pyroID != p2.pyroID and " +
                           "p1.pyroID in (SELECT DISTINCT pyroID FROM Histograms) and " +
                           "p2.pyroID in (SELECT DISTINCT pyroID FROM Histograms) " +
                     "GROUP BY i.isoID";

      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) {
            Map<String, Object> tuple = new HashMap<String, Object>();

            tuple.put("id", results.getString(1));
            tuple.put("name", results.getString(2));
            tuple.put("host", results.getString(3));
            tuple.put("sample", new Integer(results.getInt(4)));
            tuple.put("stored", results.getString(5));
            tuple.put("pyroprinted", results.getString(6));

            isolateMap.add(tuple);
         }
      }

      catch (SQLException sqlEx) {
         throw sqlEx;
      }

      finally {
         if (results != null) {
            results.close();
         }

         if (statement != null) {
            statement.close();
         }
      }

      return isolateMap;
   }

   public List<Map<String, Object>> getPyroprintDataSet() throws SQLException {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = String.format("SELECT pyroID, isoID, appliedRegion, " +
                                   "dsName, forPrimer, revPrimer, seqPrimer, " +
                                   "wellID " +
                                   "FROM Pyroprints");
      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) {
            Map<String, Object> tuple = new HashMap<String, Object>();
            tuple.put("pyroprint", new Integer(results.getInt(1))); 
            tuple.put("isolate", results.getString(2));
            tuple.put("region", results.getString(3)); 
            tuple.put("dispensation", results.getString(4)); 
            tuple.put("forwardPrimer", results.getString(5)); 
            tuple.put("reversePrimer", results.getString(6)); 
            tuple.put("sequencePrimer", results.getString(7)); 
            tuple.put("well", results.getString(8));

            rtn.add(tuple);
         }
      }

      catch (SQLException sqlEx) {
         throw sqlEx;
      }

      finally {
         if (results != null) {
            results.close();
         }

         if (statement != null) {
            statement.close();
         }
      }

      return rtn;
   }

   public void executeInsert(String insertQuery) throws SQLException {
      PreparedStatement insertSQL = null;

      try {
         System.out.printf("preparing insert:\n'%s'\n", insertQuery);
         insertSQL = mConn.prepareStatement(insertQuery);
         insertSQL.executeUpdate();
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally
      {
         if (insertSQL != null) { insertSQL.close(); }
      }
   }

   public int getTestRunId() throws SQLException {
      Statement stmt = null;
      ResultSet results = null;
      int newRunId = -1;

      String query = String.format(
          "SELECT last_insert_id()"
      );

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

   public void insertNewRun(String insertQuery) throws SQLException {
      Timestamp runDate = new Timestamp(new Date().getTime());
      PreparedStatement insertSQL = null;

      System.out.printf("%s\n", insertQuery);

      try {
         insertSQL = mConn.prepareStatement(insertQuery);
         insertSQL.setTimestamp(1, runDate);
         insertSQL.executeUpdate();
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally
      {
         if (insertSQL != null) { insertSQL.close(); }
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
