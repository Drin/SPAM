package com.drin.java.database;

import com.drin.java.ontology.Ontology;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * A class for accessing the CPLOP Databse.
 * The connection is persisted over calls and is
 *  automatically reestablished if it becomes stale.
 */
public class CPLOPConnection {
   private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
   //private static final String DB_URL = "jdbc:mysql://cslvm96.csc.calpoly.edu/CPLOP?autoReconnect=true";
   //private static final String DB_INFO_URL = "jdbc:mysql://cslvm96.csc.calpoly.edu/information_schema?autoReconnect=true";
   private static final String DB_URL = "jdbc:mysql://localhost:8906/CPLOP?autoReconnect=true";
   private static final String DB_INFO_URL = "jdbc:mysql://localhost:8906/information_schema?autoReconnect=true";
   private static final String DB_USER = "drin";
   //private static final String DB_PASS = "ILoveData#";
   private static final String DB_PASS = "";

   private Connection conn, schemaConn;
   private static CPLOPConnection mCPLOPConnection = null;
   private static Map<String, String> mForeignKeys = null;

   /**
    * Test the connection.
    */
   public static void main(String[] args) {
      try {
         CPLOPConnection cplop = CPLOPConnection.getConnection();

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
   private CPLOPConnection() throws SQLException, DriverException {
      try {
         Class.forName(DB_DRIVER);

         conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
         //connect to information_schema database as well for selecting
         //attributes for data organization
         schemaConn = DriverManager.getConnection(DB_INFO_URL, DB_USER, DB_PASS);

         mForeignKeys = new HashMap<String, String>();
         mForeignKeys.put("Isolates", "isoID");
         mForeignKeys.put("Histograms", "pyroID");
         mForeignKeys.put("HostSpecies", "commonName");
         mForeignKeys.put("Host", "commonName, hostID");
         mForeignKeys.put("Samples", "sampleID, commonName, hostID");
         mForeignKeys.put("Experiments", "ExperimentID");
         mForeignKeys.put("ExperimentPyroPrintLink", "PyroprintID, ExperimentID");
      }
      catch (ClassNotFoundException classEx) {
         throw new DriverException("Unable to instantiate DB Driver: " + DB_DRIVER);
      }
   }

   public static CPLOPConnection getConnection() {
      if (mCPLOPConnection == null) {
         try {
            mCPLOPConnection = new CPLOPConnection();
         }

         catch (CPLOPConnection.DriverException driveErr) {
            System.out.println("Driver Exception:\n" + driveErr + "\nExiting...");
            //driveErr.printStackTrace();
            System.exit(1);
         }

         catch (java.sql.SQLException sqlErr) {
            System.out.println("SQL Exception:\n" + sqlErr + "\nExiting...");
            System.exit(1);
         }
      }

      return mCPLOPConnection;
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
                     "FROM Isolates i join " +
                           "Pyroprints p1 using (isoID) join " +
                           "Pyroprints p2 using (isoID) " +
                     "WHERE p1.appliedRegion != p2.appliedRegion and " +
                           "p1.pyroID != p2.pyroID and " +
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

   public List<Map<String, Object>> getExperimentDataSet() throws SQLException {
      List<Map<String, Object>> experimentMap = new ArrayList<Map<String, Object>>();
      Statement statement = null;
      ResultSet results = null;

      String query = "SELECT Name, count(*) as NumIsolates " +
                     "FROM Experiments e join ExperimentPyroPrintLink ep using (ExperimentID) join " +
                           "Pyroprints p on (PyroprintID = pyroID) " +
                     "GROUP BY Name";

      try {
         statement = conn.createStatement();
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

   private String constructOntologicalQuery(Ontology ont, String query) {
      String extraSelects = "", extraJoins = "", extraWheres = "";

      if (ont == null) {
         return String.format(query, extraSelects, extraJoins, extraWheres);
      }

      for (Map.Entry<String, Set<String>> tableCols : ont.getTableColumns().entrySet()) {
         if (!tableCols.getKey().equals("Isolates") &&
             !tableCols.getKey().equals("Pyroprints") &&
             !tableCols.getKey().equals("Histograms")) {
            extraJoins += String.format("join %s using (%s) ",
                                         tableCols.getKey(),
                                         mForeignKeys.get(tableCols.getKey())
            );
         }

         for (String colName : tableCols.getValue()) {
            if (colName.replace(" ", "").equals("")) { continue; }

            extraSelects += String.format(", %s", colName);
         }
      }

      for (Map.Entry<String, Set<String>> colPartitions : ont.getColumnPartitions().entrySet()) {
         if (!colPartitions.getKey().equals("pyroID") &&
             !colPartitions.getKey().equals("isoID") &&
             !colPartitions.getKey().equals("nucleotide") &&
             !colPartitions.getKey().equals("pHeight") &&
             !colPartitions.getValue().isEmpty()) {

            String partitionClause = String.format("%s in (", colPartitions.getKey());

            for (String partition : colPartitions.getValue()) {
               if (partition.equals("")) { continue; }

               partitionClause += String.format("'%s', ", partition);
            }

            int lastCommaIndex = partitionClause.lastIndexOf(", ");
            
            if (lastCommaIndex > -1) {
               extraWheres += partitionClause.substring(0, lastCommaIndex) + ") AND ";
            }
         }
      }
      
      if (!extraWheres.equals("")) {
         extraWheres = extraWheres.substring(0, extraWheres.length() - 4);
      }

      return String.format(query, extraSelects, extraJoins, extraWheres);
   }

   /**
    * Calls a stored procedure that stores random isolate IDs in a temporary
    * table.
    *
    * @throws SQLException if the query fails.
    */
   public void randomizeIsolates(int randSeed) throws SQLException  {
      CallableStatement stmt = null;

      try {
         stmt = conn.prepareCall("{CALL randomizeIsolates(?)}");
         stmt.setInt(1, randSeed);
         stmt.executeUpdate();
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally
      {
         if (stmt != null) { stmt.close(); }
      }
   }

   /**
    * Get all relevant random data.
    *
    * @return A list of hashes representing the data.
    *  Each row is a Map that maps attribute names to values.
    *  'pyroprint' -> pyroprint's name (of the form 'pyroprint <id>' (String).
    *  'isolate' -> isolate's name (String).
    *  'region' -> unique identifier for the region (String).
    *
    * @throws SQLException if the query fails.
    */
   public List<Map<String, Object>> getRandomIsolateData(Ontology ont, int pageSize,
                                                          int pageOffset) throws SQLException {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;
      String placeHolder = "%s", pagination = "";

      if (pageSize > 0 && pageOffset >= 0) {
         pagination = String.format("LIMIT %d OFFSET %d", pageSize, pageOffset);
      }

      String query = String.format(
          "SELECT name_prefix, name_suffix, pyroID, appliedRegion, wellID, pHeight, nucleotide%s " +
          "FROM test_isolates JOIN " +
               "test_pyroprints USING (name_prefix, name_suffix) JOIN " +
               "test_histograms USING (pyroID) %s JOIN " +
               "(SELECT random_id, test_isolate_id " +
               " FROM test_isolates_random " +
               " %s) rand_table USING (test_isolate_id) " +
          "WHERE %s " + 
          "ORDER BY random_id, pyroID, position asc ",
          placeHolder, placeHolder, pagination, placeHolder
      );

      query = constructOntologicalQuery(ont, query);

      System.out.printf("%s\n", query);

      try {
         stmt = conn.createStatement();
         results = stmt.executeQuery(query);

         while (results.next()) {
            Map<String, Object> tuple = new HashMap<String, Object>();
            tuple.put("isolate", String.format("%s-%s", results.getString(1),
                                               results.getString(2)));
            tuple.put("pyroprint", results.getString(3)); 
            tuple.put("region", results.getString(4)); 
            tuple.put("well", results.getString(5));
            tuple.put("pHeight", results.getString(6));
            tuple.put("nucleotide", results.getString(7));

            if (ont != null) {
               int colNdx = 8;

               for (Map.Entry<String, Set<String>> tableCols : ont.getTableColumns().entrySet()) {
                  for (String colName : tableCols.getValue()) {
                     if (colName.replace(" ", "").equals("")) { continue; }

                     tuple.put(colName, results.getString(colNdx++));
                  }
               }
            }

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
   private List<Map<String, Object>> getData(Ontology ont, String searchID,
                                             String searchSet) throws SQLException {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;
      String searchClause = "", placeHolder = "%s";

      if (searchID != null && searchSet != null) {
         searchClause = String.format("%s in (%s) and", searchID, searchSet);
      }

      String query = String.format(
          "SELECT pyroID, isoID, appliedRegion, wellID, pHeight, nucleotide%s " +
          "FROM Pyroprints join Isolates using (isoID) join Histograms using (pyroID) %s" +
          "WHERE %s %s" + 
          "ORDER BY isoID, pyroID, position asc",
          placeHolder, placeHolder, searchClause, placeHolder
      );

      query = constructOntologicalQuery(ont, query);

      System.err.println("query:\n" + query);
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

            if (ont != null) {
               int colNdx = 7;

               for (Map.Entry<String, Set<String>> tableCols : ont.getTableColumns().entrySet()) {
                  for (String colName : tableCols.getValue()) {
                     if (colName.replace(" ", "").equals("")) { continue; }

                     tuple.put(colName, results.getString(colNdx++));
                  }
               }
            }

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

   public List<Map<String, Object>> getDataByExperimentName(String experiments) throws SQLException
   {
      List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;

      String query = String.format(
       "SELECT pyroID, isoID, appliedRegion, wellID, pHeight, nucleotide " +
       "FROM Experiments e join ExperimentPyroPrintLink ep using (ExperimentID) " +
             "join Pyroprints p on (PyroprintID = pyroID) join Histograms using (pyroID) " +
       "WHERE e.name in (%s) and pyroID in (Select distinct pyroID from Histograms)" + 
       "ORDER BY isoID, pyroID, position asc", experiments);

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

   /*
    * Queries for constructing an appropriate ontology
    */
   public List<String> getDistinctValues(String tableName, String colName) throws SQLException {
      List<String> distinctValues = new ArrayList<String>();
      Statement stmt = null;
      ResultSet results = null;

      String query = String.format("SELECT distinct(%s) " +
                                   "FROM %s " +
                                   "WHERE %s IS NOT NULL",
                                   colName, tableName, colName);

      try {
         stmt = conn.createStatement();
         results = stmt.executeQuery(query);

         while (results.next()) {
            distinctValues.add(results.getString(1));
         }
      }
      catch (SQLException sqlEx) {
         //Rethrow the exception
         throw sqlEx;
      }
      finally {
         if (results != null) { results.close(); }
         if (stmt != null) { stmt.close(); }
      }

      return distinctValues;
   }

   /*
    * TODO this will remain commented out until a solution for retrieving a
    * single record for each pyroprint is determined in mysql.
    * Call a stored procedure so that pyroprint records do not have to be
    * parsed in memory by Java.
    *
   public List<Map<String, Object>> getPyroprintRecords(String isoIDs) throws SQLException {
      List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();
      Statement stmt = null;
      ResultSet results = null;

      try {
         stmt = conn.prepareCall("{call get_pyroprint_records(?)}");
         stmt.setString(1, isoIDs);

         results = stmt.executeQuery();

         while (results.next()) {
            //results.get
         }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally {
         if (results != null) { results.close(); }
         if (stmt != null) { stmt.close(); }
      }
      return null;
   }
   */

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
