package com.drin.java.database;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * A class for accessing the CPLOP Databse.
 * The connection is persisted over calls and is
 *  automatically reestablished if it becomes stale.
 */
public class CPLOPConnection {
   private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
   private static final String DB_URL = "jdbc:mysql://cslvm96.csc.calpoly.edu/CPLOP?autoReconnect=true";
   private static final String DB_INFO_URL = "jdbc:mysql://cslvm96.csc.calpoly.edu/information_schema?autoReconnect=true";
   private static final String DB_USER = "amontana";
   private static final String DB_PASS = "ILoveData#";

   private Connection conn, schemaConn;

   /**
    * Test the connection.
    */
   public static void main(String[] args) {
      try {
         CPLOPConnection cplop = new CPLOPConnection();

         List<Map<String, Object>> res;
         
         res = cplop.getHistogram(2377);
         for (Map<String, Object> pyroprint : res) {
            System.out.println(pyroprint);
         }
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   /**
    * @throws SQLException if the DriverManager can't get a connection.
    * @throws DriverException if there is a problem instantiating the DB driver.
    */
   public CPLOPConnection() throws SQLException, DriverException {
      try {
         Class.forName(DB_DRIVER);

         conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
         //connect to information_schema database as well for selecting
         //attributes for data organization
         schemaConn = DriverManager.getConnection(DB_INFO_URL, DB_USER, DB_PASS);
      }
      catch (ClassNotFoundException classEx) {
         throw new DriverException("Unable to instantiate DB Driver: " + DB_DRIVER);
      }
   }

   public Map<String, List<String>> getCPLOPSchema() throws SQLException {
      Map<String, List<String>> schemaMapping = new LinkedHashMap<String, List<String>>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT TABLE_NAME, COLUMN_NAME" +
       " FROM COLUMNS" +
       " WHERE TABLE_SCHEMA = 'cplop'";

      try {
         statement = schemaConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) {
            String tableName = results.getString(1);
            String attributeName = results.getString(2);

            if (!schemaMapping.containsKey(tableName)) {
               schemaMapping.put(tableName, new ArrayList<String>());
            }

            schemaMapping.get(tableName).add(attributeName);
         }
      }

      catch (SQLException sqlEx) {
         throw sqlEx;
      }

      return schemaMapping;
   }

   /**
    * Get all the different isolates and the number of 
    *  pyroprints that are in the database fop each.
    * Note that the number is not the number of times the isolate
    *  was pyroprinted, but the number of pyroprints that are in the DB.
    *
    * @return A list of hashes representing the isolates and their ids.
    *  Each row is a Map that maps attribute names to values.
    *  'isolate' maps to the isolate id (String).
    *  'count' maps to the number of pyroprints for the isolate (Integer).
    *
    * @throws SQLException if the query fails.
    */
   public List<Map<String, Object>> getIsolates() throws SQLException {
      List<Map<String, Object>> isoIdMap = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT isoID, COUNT(*)" + 
       " FROM Isolates i JOIN Pyroprints p USING(isoID)" + 
       " WHERE isoID IN (SELECT DISTINCT(isoID) FROM Histograms)" +
       " GROUP BY isoID" +
       " ORDER BY count(*) DESC, isoID";

      try {
         statement = conn.createStatement();

         results = statement.executeQuery(query);

         while (results.next()) {
            Map<String, Object> tuple = new HashMap<String, Object>();

            tuple.put("isolate", results.getString(1)); 
            tuple.put("count", new Integer(results.getInt(2))); 

            isoIdMap.add(tuple);
         }
      }

      catch (SQLException sqlEx) {
         //Rethrow the exception
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

      return isoIdMap;
   }

   /**
    * Retrieve each pairwise pyroprint comparison
    *
    * @return A mapping of pyroprint ids to isolate ids.
    *  The map is keyed by pyroprint ID (Integer) and value mappings are
    *  isolate ids (String).
    *
    * @throws SQLException if the query fails.
    */
   public Map<String, Integer> getIsolatePyroprints() throws SQLException {
      Map<String, Integer> isoPyroMap = new HashMap<String, Integer>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT isoID, pyroID" + 
       " FROM Isolates i JOIN Pyroprints p USING(isoID)" + 
       " WHERE pyroID IN (SELECT DISTINCT(pyroID) FROM Histograms)" +
       " GROUP BY isoID" +
       " ORDER BY isoID";

      try {
         statement = conn.createStatement();

         results = statement.executeQuery(query);

         while (results.next()) {
            String isoID = results.getString(1);
            Integer pyroID = new Integer(results.getInt(2));

            isoPyroMap.put(isoID, pyroID);
         }
      }

      catch (SQLException sqlEx) {
         //Rethrow the exception
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

      return isoPyroMap;
   }

   /**
    * Given a pyroprint, retrieve each other pyroprint (with a higher pyroID)
    * and the similarity between the two pyroprints.
    *
    * @return A Map representing a paired pyroprint and the similarity
    * between the given and retrieved pyroprint pair.
    * The map is keyed by pyroprint id (Integer) and value mappings are pearson
    * correlations (Double)
    *
    * @throws SQLException if the query fails.
    */
   public Map<Integer, Double> getPyroSims(Integer pyro1) throws SQLException {
      Map<Integer, Double> pyroSimMap = new HashMap<Integer, Double>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT pyroID2, pearson" + 
       " FROM pyro_similarities" + 
       " WHERE pyroID1 = " + pyro1 +
       " ORDER BY pyroID2 asc;";

      try {
         statement = conn.createStatement();

         results = statement.executeQuery(query);

         while (results.next()) {
            Integer pyroID = new Integer(results.getInt(1));
            Double pyroSim = new Double(results.getDouble(2));

            pyroSimMap.put(pyroID, pyroSim);
         }
      }

      catch (SQLException sqlEx) {
         //Rethrow the exception
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

      return pyroSimMap;
   }


   /**
    * Given a pyroprint, retrieve each other pyroprint (with a higher pyroID)
    * and the similarity between the two pyroprints.
    *
    * @return A Map representing a paired pyroprint and the similarity
    * between the given and retrieved pyroprint pair.
    * The map is keyed by pyroprint id (Integer) and value mappings are pearson
    * correlations (Double)
    *
    * @throws SQLException if the query fails.
    */
   public Map<Integer, Double> getPyroSimRange(Integer pyro1,
    Double beta, Double alpha) throws SQLException {
      Map<Integer, Double> pyroSimMap = new HashMap<Integer, Double>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT pyroID2, pearson" + 
       " FROM pyro_similarities" + 
       " WHERE pyroID1 = " + pyro1 +
       " AND pearson between " + beta + " AND " + alpha +
       " ORDER BY pyroID2 asc;";

      try {
         statement = conn.createStatement();

         results = statement.executeQuery(query);

         while (results.next()) {
            Integer pyroID = new Integer(results.getInt(1));
            Double pyroSim = new Double(results.getDouble(2));

            pyroSimMap.put(pyroID, pyroSim);
         }
      }

      catch (SQLException sqlEx) {
         //Rethrow the exception
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

      return pyroSimMap;
   }

   /**
    * Retrieve a mapping of all regions to a list of associated thresholds.
    *
    * @return A Map keyed by region name that maps to an inner map. The inner
    * map is keyed by threshold name (alphaThreshold or betaThreshold) and maps
    * to the threshold value described by the threshold name.
    *
    * @throws SQLException if the query fails.
    */
   public Map<String, Map<String, Double>> getRegionThresholds() throws SQLException {
      Map<String, Map<String, Double>> regThrMap = new HashMap<String, Map<String, Double>>();
      String alphaThreshold = "alphaThreshold", betaThreshold = "betaThreshold";
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT * FROM region_thresholds";

      try {
         statement = conn.createStatement();

         results = statement.executeQuery(query);

         while (results.next()) {
            String regionName = results.getString(1);
            Double alphaVal = new Double(results.getDouble(2));
            Double betaVal = new Double(results.getDouble(3));

            if (!regThrMap.containsKey(regionName)) {
               regThrMap.put(regionName, new HashMap<String, Double>());
            }

            regThrMap.get(regionName).put(alphaThreshold, alphaVal);
            regThrMap.get(regionName).put(betaThreshold, betaVal);
         }
      }

      catch (SQLException sqlEx) {
         //Rethrow the exception
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

      return regThrMap;
   }

   /**
    * Retrieves all isolates from the database.
    *
    * @return A list of hashes representing isolate attributes.
    *  Each item is a Map that maps attribute names to values.
    *  'id' -> isolate's id (String).
    *  'name' -> common name of E. coli host. (String).
    *  'host' -> id of host, differentiating between hosts of the same species (String).
    *  'sample' -> id of E. coli culture sample for the given host (String).
    *  'stored' -> date that the isolate was stored in the database (String).
    *  'pyroprinted' -> date that the isolate was pyroprinted (String).
    *
    * @throws SQLException if the query fails.
    */
   public List<Map<String, Object>> getIsolateDataSet() throws SQLException {
      List<Map<String, Object>> isolateMap = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT isoID, commonName, hostID, sampleID, " +
                     "dateStored, pyroprintDate " +
                     "FROM Isolates ";

      try {
         statement = conn.createStatement();
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

   public List<Map<String, Object>> getIsolateDataSetWithBothRegions() throws SQLException {
      List<Map<String, Object>> isolateMap = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT i.isoID, i.commonName, i.hostID, i.sampleID, " +
                     "i.dateStored, i.pyroprintDate " +
                     "FROM Isolates i join Pyroprints p1 on (i.isoID = p1.isoID) join Pyroprints p2 on (i.isoID = p2.isoID and p1.pyroID != p2.pyroID) " +
                     "WHERE p1.appliedRegion != p2.appliedRegion and p1.pyroID and " +
                     "p1.pyroID in (SELECT DISTINCT pyroID FROM Histograms) and " +
                     "p2.pyroID in (SELECT DISTINCT pyroID FROM Histograms) " +
                     "GROUP BY i.isoID";

      try {
         statement = conn.createStatement();
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

   /**
    * Retrieve the data for all pyroprints in the databse.
    *
    * @return A list of hashes representing the pyroprints.
    *  Each row is a Map that maps attribute names to values.
    *  'pyroprint' -> pyroprint's id (Integer).
    *  'isolate' -> id of isolate which the pyroprint represents (String).
    *  'region' -> unique identifier for the region. (String).
    *  'forwardPrimer' -> unique identifier for the forward primer (String).
    *  'reversePrimer' -> unique identifier for the reverse primer (String).
    *  'sequencePrimer' -> unique identifier for the sequence primer (String).
    *  'dispensation' -> unique identifier for the dispensation sequence (String).
    *  'well' -> well location of pyroprint in pyromark plate (String).
    *
    * @throws SQLException if the query fails.
    */
   public List<Map<String, Object>> getPyroprintDataSet() throws SQLException {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;

      String query = String.format("SELECT pyroID, isoID, appliedRegion, " +
                                   "dsName, forPrimer, revPrimer, seqPrimer, " +
                                   "wellID " +
                                   "FROM Pyroprints");
      try {
         stmt = conn.createStatement();
         results = stmt.executeQuery(query);

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

         if (stmt != null) {
            stmt.close();
         }
      }

      return rtn;
   }

   /**
    * Get all relevant data given a list of pyroIDs.
    *
    * @param pyroID The primary key of a pyroprint.
    *
    * @return A list of hashes representing the data.
    *  Each row is a Map that maps attribute names to values.
    *  'pyroprint' -> pyroprint's name (of the form 'pyroprint <id>' (String).
    *  'isolate' -> isolate's name (String).
    *  'region' -> unique identifier for the region (String).
    *
    * @throws SQLException if the query fails.
    */
   private List<Map<String, Object>> getData(String searchID, String searchSet) throws SQLException {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;

      String query = String.format(
       "SELECT pyroID, isoID, appliedRegion, wellID, pHeight, nucleotide " +
       "FROM Pyroprints join Isolates using (isoID) join Histograms using (pyroID) " +
       "WHERE %s in (%s) and pyroID in (Select distinct pyroID from Histograms)" + 
       "ORDER BY isoID, pyroID, position asc",
       searchID, searchSet);

      //System.err.println("query:\n" + query);
      try {
         stmt = conn.createStatement();
         results = stmt.executeQuery(query);

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
         if (stmt != null) { stmt.close(); }
      }

      return rtn;
   }

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

   /**
    * Given an isolate (isolate id), give the data for all pyroprints in the
    *  the databse.
    *
    * @param isoId The islate id. The same one given from getIsolate.
    *
    * @return A list of hashes representing the pyroprints.
    *  Each row is a Map that maps attribute names to values.
    *  'pyroprint' -> pyroprint's id (Integer).
    *  'region' -> unique identifier for the region. (String).
    *  'forwardPrimer' -> unique identifier for the forward primer (String).
    *  'reversePrimer' -> unique identifier for the reverse primer (String).
    *  'sequencePrimer' -> unique identifier for the sequence primer (String).
    *  'dispensation' -> unique identifier for the dispensation sequence (String).
    *
    * @throws SQLException if the query fails.
    */
   public List<Map<String, Object>> getPyroprints(String isoId) throws SQLException
   {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;

      String query = String.format(
       "SELECT pyroID, appliedRegion, dsName, forPrimer, revPrimer, seqPrimer" +
       " FROM Pyroprints" +
       " WHERE isoID = '%s'",
       isoId);

      try
      {
         stmt = conn.createStatement();

         results = stmt.executeQuery(query);

         while (results.next())
         {
            Map<String, Object> tuple = new HashMap<String, Object>();
            tuple.put("pyroprint", new Integer(results.getInt(1))); 
            tuple.put("region", results.getString(2)); 
            tuple.put("dispensation", results.getString(3)); 
            tuple.put("forwardPrimer", results.getString(4)); 
            tuple.put("reversePrimer", results.getString(5)); 
            tuple.put("sequencePrimer", results.getString(6)); 

            rtn.add(tuple);
         }
      }
      catch (SQLException sqlEx)
      {
         //Rethrow the exception
         throw sqlEx;
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }

         if (stmt != null)
         {
            stmt.close();
         }
      }

      return rtn;
   }

   /**
    * Given a pyroprint (pyroprint id), give the histogram.
    *
    * @param pyroId The pyroprint id. The same one given from getPyroprints.
    *
    * @return A list of hashes representing the histogram.
    *  Each row is a Map that maps attribute names to values.
    *  'peakHeight' -> the peak height for this position. (Double)
    *  'peakArea' -> the peak area for this position. (Double)
    *  'peakWidth' -> the width of the peak for this position. (Double)
    *  'compensatedPeakHeight' -> the compensated peak height for this position. (Double)
    *  'nucleotide' - The nucleotide at this position. (String)
    *
    * @throws SQLException if the query fails.
    */
   public List<Map<String, Object>> getHistogram(int pyroId) throws SQLException
   {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;

      String query = String.format(
       "SELECT pHeight, PeakArea, PeakWidth, cPeakHeight, nucleotide" +
       " FROM Histograms" + 
       " WHERE pyroID = %d" +
       " ORDER BY position",
       pyroId);

      try
      {
         stmt = conn.createStatement();

         results = stmt.executeQuery(query);

         while (results.next())
         {
            Map<String, Object> tuple = new HashMap<String, Object>();
            tuple.put("peakHeight", new Double(results.getDouble(1)));
            tuple.put("peakArea", new Double(results.getDouble(2)));
            tuple.put("peakWidth", new Double(results.getDouble(3)));
            tuple.put("compensatedPeakHeight", new Double(results.getDouble(4)));
            tuple.put("nucleotide", results.getString(5));

            rtn.add(tuple);
         }
      }
      catch (SQLException sqlEx)
      {
         //Rethrow the exception
         throw sqlEx;
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }

         if (stmt != null)
         {
            stmt.close();
         }
      }

      return rtn;
   }


   /**
    * Perform just a general query.
    * Make sure to close your result set after you are finished with it.
    *
    * @throws SQLException if the query generates an error.
    *
    * @return the ResultSet that came from the query.
    */
   public ResultSet generalQuery(String query) throws SQLException
   {
      Statement stmt = conn.createStatement();

      return stmt.executeQuery(query);
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
