package com.drin.java.database;

import com.drin.java.ontology.Ontology;

import com.drin.java.biology.Pyroprint;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;

import com.drin.java.util.Configuration;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CPLOPConnection {
   private static final String DB_DRIVER = "java.sql.Driver";
   //TODO figure out how to connect to database now
   private static final String DB_URL = "jdbc:mysql://localhost:9906/CPLOP?autoReconnect=true";
   private static final String DB_USER = "drin";
   private static final String DB_PASS = "";
   /*
   private static final String DB_URL = "jdbc:mysql://cslvm96.csc.calpoly.edu/CPLOP?autoReconnect=true";
   private static final String DB_USER = "amontana";
   private static final String DB_PASS = "ILoveData#";
   */
   private static final int DEFAULT_PAGE_SIZE = 50000;
   private static final String REGION_16_23 = "16-23",
                               REGION_23_5  = "23-5",
                               PYRO_LEN     = "pyroprint length";

   private Connection mConn;

   private static final int m16sDispLen = Configuration.getInt(REGION_16_23, PYRO_LEN),
                            m23sDispLen = Configuration.getInt(REGION_23_5, PYRO_LEN);

   private String
      SCHEMA_QUERY = "SELECT distinct %s " +
                     "FROM %s " +
                     "%s " +
                     "ORDER BY %s",

      ISOLATE_TABLE_QUERY = "SELECT %s " +
                            "FROM Isolates join Pyroprints using (isoID) " +
                                          "join Samples using (commonName, hostID, sampleID)",

      //DATA_QUERY = "SELECT i.isoID, p1.pyroID, p2.pyroID, h1.pHeight, " +
      //                    "h2.pHeight, h1.position " +
      DATA_QUERY = "SELECT i.isoID, " +
                          "p1.pyroID, p1.appliedRegion, p1.dsName, " +
                          "h1.pHeight, h1.position " +
                   "FROM Isolates i " +
                        "JOIN Pyroprints p1 ON ( " +
                           "i.isoID = p1.isoID " +
                           //"i.name_prefix = p1.name_prefix AND " +
                           //"i.name_suffix = p1.name_suffix " +
                           //"p1.appliedRegion = '23-5'" +
                        ") " +
                        /*
                        "JOIN Pyroprints p2 ON ( " +
                           "i.isoID = p2.pyroID AND " +
                           "p2.appliedRegion != p1.appliedRegion" +
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
                   "WHERE i.test_isolate_id in (SELECT test_isolate_id " +
                                               "FROM isolate_selection) " +
                   "ORDER BY i.test_isolate_id, p1.pyroID, position " +
                   "LIMIT %d OFFSET %d",

      DIRECTED_DATA_QUERY = 
                   "SELECT i.isoID, p1.pyroID, p1.appliedRegion, p1.dsName, " +
                          "h1.pHeight, h1.position, i.hostID, i.userName, " +
                          "s.location, s.dateCollected " +
                   "FROM Isolates i " +
                        "JOIN Pyroprints p1 ON ( " +
                           "i.isoID = p1.isoID " +
                           //"p1.appliedRegion = '23-5'" +
                        ") " +
                        "JOIN Samples s ON ( " +
                           "s.hostID = i.hostID AND " +
                           "s.sampleID = i.sampleID AND " +
                           "s.commonName = i.commonName " +
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
                   "WHERE i.isoID in (%s) " +
                   "ORDER BY i.isoID, p1.pyroID, position " +
                   "LIMIT %d OFFSET %d",

      //The join with samples is necessary because there are some isolates that
      //don't have an entry in the samples table
      FULL_DATA_QUERY = 
                   "SELECT i.isoID, p1.pyroID, p1.appliedRegion, p1.dsName, " +
                          "h1.pHeight, h1.position, i.hostID, i.userName, " +
                          "s.location, s.dateCollected " +
                   "FROM Isolates i join Samples using (hostID, commonName, sampleID) " +
                        "JOIN Pyroprints p1 ON ( " +
                           "i.isoID = p1.isoID " +
                           //"p1.appliedRegion = '23-5'" +
                        ") " +
                        "JOIN Samples s ON ( " +
                           "s.hostID = i.hostID AND " +
                           "s.sampleID = i.sampleID AND " +
                           "s.commonName = i.commonName " +
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
                   "WHERE i.isoID in (SELECT distinct t.isoID " +
                                     "FROM Isolates t " +
                                          "JOIN Pyroprints t1 ON ( " +
                                              "t.isoID = t1.isoID AND " +
                                              "t1.appliedRegion = '16-23') " +
                                          "JOIN Pyroprints t2 ON ( " +
                                              "t.isoID = t2.isoID AND " +
                                              "t2.appliedRegion = '23-5') " +
                                          "JOIN Histograms t3 ON ( " +
                                             "t1.pyroID = t3.pyroID" +
                                          ") " +
                                          "JOIN Histograms t4 ON ( " +
                                             "t2.pyroID = t4.pyroID" +
                                          ") " +
                                     ") AND " +
                         "i.isoID not LIKE 'ES-%%' AND " +
                         "i.isoID not LIKE 'STEC%%' " +
                         "AND i.isoID not LIKE 'Pp-%%' " +
                         /*
                         "i.isoID not in (SELECT t.isoID " +
                                         "FROM Isolates t " +
                                         "WHERE t.isoID LIKE 'ES-%%' AND " +
                                               "right(t.isoID, 3) <= 448) " +
                         */
                   "ORDER BY i.isoID, p1.pyroID, h1.position " +
                   "LIMIT %d OFFSET %d",

      META_QUERY = "SELECT distinct test_isolate_id, CONCAT(name_prefix, '-', name_suffix) as isoID %s " +
                   "FROM test_isolates " +
                        "JOIN test_pyroprints using (" +
                           "name_prefix, name_suffix " +
                        ") " +
                        "JOIN Samples using ( " +
                           "hostID, commonName, sampleID " +
                        ") " +
                        "JOIN test_histograms using (pyroID) " +
                   "WHERE test_isolate_id in (%s) " +
                   "ORDER BY test_isolate_id",
                   //"ORDER BY isoID ";
                   //
      JOSH_META_QUERY = "SELECT isoID, %s " +
                        "FROM Samples JOIN Isolates using (hostID, commonName, sampleID) " +
                        "WHERE isoID in (%s) " +
                        "ORDER BY isoID ",

      CREEK_META_QUERY = "SELECT isoID, %s " +
                         "FROM Samples JOIN Isolates using (hostID, commonName, sampleID) " +
                                      "JOIN creek_meta using (isoID) " +
                         "WHERE isoID in (%s) " +
                         "ORDER BY isoID ",

      EMILY_META_QUERY = "SELECT isoID, %s " +
                         "FROM emily_meta " +
                         "JOIN Isolates using (isoID) " +
                         "WHERE isoID in (%s) ";


   public CPLOPConnection() throws SQLException, DriverException {
      try {
         Class.forName(DB_DRIVER);

         mConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
      }
      catch (ClassNotFoundException classEx) {
         throw new DriverException("Unable to instantiate DB Driver: " + DB_DRIVER);
      }
   }

   public Map<String, Set<String>> getDistinctValues(String tableName, List<String> colNames,
                                                     List<String> filterValues) throws SQLException {
      Map<String, Set<String>> distinctValues = new HashMap<String, Set<String>>();
      Statement statement = null;
      ResultSet results = null;

      String colNameString = "";
      for (String ontCol : colNames) {
         colNameString += "," + ontCol;
      }

      String whereClause = "";
      if (!filterValues.isEmpty()) {
         String whereFilter = "";
         whereClause = "WHERE " + colNames.get(colNames.size() - 1) +
                       " IN (%s) ";

         for (String filterVal : filterValues) {
            whereFilter += ",'" + filterVal + "'";
         }

         whereClause = String.format(whereClause, whereFilter.substring(1));
      }

      String query = String.format(SCHEMA_QUERY,
         colNameString.substring(1), tableName, whereClause,
         colNameString.substring(1)
      );

      System.out.println(query);

      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(query);

         while (results.next()) {
            if (colNames.size() == 1) {
               if (results.getString(1) != null) {
                  String colName = results.getString(1).trim();
                  distinctValues.put(colName, null);
               }
            }

            for (int colNdx = 1; colNdx < colNames.size(); colNdx++) {
               String colName = "";
               
               for (int prevCol = 1; prevCol <= colNdx; prevCol++) {
                  if (results.getString(prevCol) != null) {
                     colName += ":" + results.getString(prevCol).trim();
                  }
               }

               if (results.getString(colNdx + 1) != null && colName.length() > 1) {
                  String subColName = results.getString(colNdx + 1).trim();

                  if (!distinctValues.containsKey(colName.substring(1))) {
                     distinctValues.put(colName.substring(1), new HashSet<String>());
                  }

                  distinctValues.get(colName.substring(1)).add(subColName);
               }
            }
         }
      }
      catch (SQLException sqlEx) { throw sqlEx; }
      finally {
         if (results != null) { results.close(); }
         if (statement != null) { statement.close(); }
      }

      /*
      System.out.println("distinct values...");
      for (Map.Entry<String, Set<String>> values : distinctValues.entrySet()) {
         System.out.println("\t" + values.getKey() + ":");

         if (values.getValue() != null) {
            for (String strVal : values.getValue()) {
               System.out.println("\t\t" + strVal);
            }
         }
      }
      */

      return distinctValues;
   }

   public Object[][] getIsolateDataTableView(String[] colList) throws SQLException {
      ArrayList<Object[]> dataTable = new ArrayList<Object[]>();
      Object[] tmpDataRow = null;

      Statement statement = null;
      ResultSet results = null;

      String concatColList = null;
      if (colList != null) {
         for (String colName : colList) {
            if (colName != null) { concatColList += colName; }
         }
      }

      if (concatColList == null) { return null; }

      try {
         statement = mConn.createStatement();
         results = statement.executeQuery(
            String.format(ISOLATE_TABLE_QUERY, concatColList)
         );

         while (results.next()) {
            tmpDataRow = new Object[colList.length];

            for (int colNdx = 0; colNdx < colList.length; colNdx++) {
               tmpDataRow[colNdx] = results.getObject(colNdx + 1);
            }

            dataTable.add(tmpDataRow);
         }
      }
      catch (Exception err) { err.printStackTrace(); }
      finally {
         if (statement != null) { statement.close(); }
         if (results != null) { results.close(); }
      }

      Object[][] isoDataTable = null;
      return dataTable.toArray(isoDataTable);
   }

   public List<Isolate> getIsolateData(int dataSize, String isoIdList) throws SQLException {
      return getIsolateData(dataSize, DEFAULT_PAGE_SIZE, isoIdList);
   }

   public List<Isolate> getIsolateData(int dataSize, int pageSize, String isoIdList) throws SQLException {
      Statement statement = null;
      ResultSet results = null;

      byte pyroLen = 96;
      int pyroId = -1, tmpPyroId = -1;
      List<Isolate> isoData = new ArrayList<Isolate>(dataSize);
      String isoId = null, tmpIsoId = null, regName = null, dsName = null;
      //Extra metadata
      String hostId = null, source = null, location = null, date = null;

      Isolate tmpIso = null;
      ITSRegion tmpRegion = null;
      Pyroprint tmpPyro = null;

      int pageNdx = 0;
      boolean hasMoreData;

      try {
         do {
            hasMoreData = false;
            statement = mConn.createStatement();
            //3 Data Query Variables:
            //    Length of Pyroprint
            //    Page size
            //    Page offset

            /*
            System.out.println(String.format(DATA_QUERY,
               pyroLen,
               Math.min(pageSize, peakDataSize - (pageSize * pageNdx)),
               (pageSize * pageNdx)
            ));

            results = statement.executeQuery(String.format(DATA_QUERY,
               pyroLen, 
               Math.min(pageSize, peakDataSize - (pageSize * pageNdx)),
               (pageSize * pageNdx)
            ));
            */

            if (isoIdList != null) {
               System.out.println("Executing directed data query");
               System.out.println(String.format(DIRECTED_DATA_QUERY,
                  pyroLen, isoIdList, pageSize,
                  (pageSize * pageNdx)
               ));
               results = statement.executeQuery(String.format(DIRECTED_DATA_QUERY,
                  pyroLen, isoIdList, pageSize,
                  (pageSize * pageNdx)
               ));
            }
            else if (isoIdList == null) {
               System.out.println("Executing full data query");
               System.out.println(String.format(FULL_DATA_QUERY,
                  pyroLen, pageSize,
                  (pageSize * pageNdx)
               ));
               results = statement.executeQuery(String.format(FULL_DATA_QUERY,
                  pyroLen, pageSize,
                  (pageSize * pageNdx)
               ));
            }

            if (results.next()) {
               hasMoreData = true;
               pageNdx++;
               results.previous();
            }

            while (results.next()) {
               //int isoIdNum = results.getInt(1);
               tmpIsoId = results.getString(1);
               tmpPyroId = results.getInt(2);
               regName = results.getString(3);
               dsName = results.getString(4);
               float pHeight = results.getFloat(5);
               byte position = results.getByte(6);

               /*
                * Extra metadata
                */
               hostId = results.getString(7);
               source = results.getString(8);
               location = results.getString(9);
               date = results.getString(10);

               byte dispLen = -1;
               if (regName.equals("16-23")) { dispLen = (byte) m16sDispLen; }
               else if (regName.equals("23-5")) { dispLen = (byte) m23sDispLen; }

               if (isoId == null || !tmpIsoId.equals(isoId)) {
                  isoId = tmpIsoId;
                  //System.out.println("new Isolate: " + isoId);

                  tmpIso = new Isolate(isoId);
                  tmpRegion = new ITSRegion(regName);

                  //System.err.println("first region: " + regName);

                  tmpIso.getData().add(tmpRegion);

                  isoData.add(tmpIso);

                  //Extra metadata
                  tmpIso.setHost(hostId);
                  tmpIso.setSource(source);
                  tmpIso.setLoc(location);
                  tmpIso.setDate(date);
               }

               if (tmpRegion == null || !tmpRegion.getName().equals(regName)) {
                  tmpRegion = new ITSRegion(regName);
                  //System.err.println("new Region: " + regName);

                  if (tmpIso != null) { tmpIso.getData().add(tmpRegion); }
               }

               if (tmpPyroId != pyroId) {
                  pyroId = tmpPyroId;

                  tmpPyro = new Pyroprint(String.valueOf(pyroId), dispLen, dsName);

                  tmpRegion.getData().add(tmpPyro);
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

            //if (isoIdList == null) { break; }
         } while (hasMoreData);

      }
      catch (Exception err) { err.printStackTrace(); }
      finally {
         if (statement != null) { statement.close(); }
         if (results != null) { results.close(); }
      }

      List<Isolate> finalIsoData = new ArrayList<Isolate>(isoData.size());

      for (Isolate iso : isoData) {
         if (iso.getData().size() != 2) {
            System.err.printf("Isolate %s has %d regions\n",
               iso.getName(), iso.getData().size());
         }
         else if (iso.getData().size() >= 2) {
            finalIsoData.add(iso);
         }
      }

      return finalIsoData;
   }

   //TODO
   public void getIsolateMetaData(List<Isolate> isoData, Ontology ont, int dataSize) {
      Statement statement = null;
      ResultSet results = null;

      String metaLabels[] = null;
      String metaIDs = "", metaColumns = "", isoId = null, tmpId = null;
      int isoNdx = -1, numColumns = ont.getNumCols();
      int pageSize = DEFAULT_PAGE_SIZE;
      //TODO also leftover from thesis testing
      //byte colOffset = 3;
      byte colOffset = 2;

      for (String metaCol : ont.getColumns()) { metaColumns += "," + metaCol; }
      for (int ndx = 0; ndx < isoData.size(); ndx++) {
         //when using test_isolate_id
         //metaIDs += "," + isoData.get(ndx).getIdNum();

         //when using isoID
         metaIDs += ",'" + isoData.get(ndx).getName() + "'";
      }

      try {
         for (int pageNdx = 0; pageNdx < Math.ceil((float) dataSize / pageSize); pageNdx++) {
            /*
            System.out.println(String.format(META_QUERY,
               metaColumns, metaIDs.substring(1)
            ));
            System.out.println("using emily query!");
            System.out.println(String.format(EMILY_META_QUERY,
               metaColumns.substring(1), metaIDs.substring(1)
            ));
            System.out.println(String.format(CREEK_META_QUERY,
               metaColumns.substring(1), metaIDs.substring(1)
            ));
            */
            System.out.println(String.format(JOSH_META_QUERY,
               metaColumns.substring(1), metaIDs.substring(1)
            ));
            statement = mConn.createStatement();
            /*
            results = statement.executeQuery(String.format(META_QUERY,
               metaColumns, metaIDs.substring(1)
            ));
            results = statement.executeQuery(String.format(EMILY_META_QUERY,
               metaColumns.substring(1), metaIDs.substring(1)
            ));
            results = statement.executeQuery(String.format(CREEK_META_QUERY,
               metaColumns.substring(1), metaIDs.substring(1)
            ));
            */
            results = statement.executeQuery(String.format(JOSH_META_QUERY,
               metaColumns.substring(1), metaIDs.substring(1)
            ));

            while (results.next()) {
               //TODO wtf why was this ever 2? maybe because i used to have
               //the auto incrememnt id when doing tests?
               //tmpId = results.getString(2);
               tmpId = results.getString(1);

               if (isoId == null || !tmpId.equals(isoId)) {
                  isoId = tmpId;
                  isoNdx++;

                  if (!isoData.get(isoNdx).getName().equals(isoId)) {
                     System.err.printf("meta data mismatch:\n");
                     System.err.printf("found '%s' expected '%s'\n", isoId,
                                       isoData.get(isoNdx).getName());
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
                                                              "average_isolate_similarity) " +
                            "VALUES %s";
      String isolateInsert = "INSERT IGNORE INTO real_isolate_strains(test_run_id, cluster_id, " +
                                                               "cluster_threshold, real_isolate_id) " +
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
