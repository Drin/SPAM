package com.drin.java.database;

import com.drin.java.ontology.Ontology;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SpecialCPLOPConnection {
   private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
   private static final String DB_URL = "jdbc:mysql://localhost/CPLOP?autoReconnect=true";
   private static final String DB_USER = "amontana";
   private static final String DB_PASS = "4ldr1n*(";
   private static final int DEFAULT_PAGE_SIZE = 10000,
                            ISOLATE_LEN       = 188,
                            LEN_23S           = 93,
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

   public SpecialCPLOPConnection() throws SQLException, DriverException {
      try {
         Class.forName(DB_DRIVER);

         mConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
      }
      catch (ClassNotFoundException classEx) {
         throw new DriverException("Unable to instantiate DB Driver: " + DB_DRIVER);
      }
   }

   
   public IsolateDataContainer getIsolateData(int dataSize) throws SQLException {
      return getIsolateData(dataSize, DEFAULT_PAGE_SIZE);
   }

   public IsolateDataContainer getIsolateData(int dataSize, int pageSize) throws SQLException {
      Statement statement = null;
      ResultSet results = null;

      int peakDataSize = dataSize * LEN_16S;

      float[] iso_data = new float[dataSize * ISOLATE_LEN];
      int[] iso_ids = new int[dataSize];
      int iso_id = -1, tmp_iso_id = -1, peak_ndx = 0, isolate_ndx = -1;

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

   public String[][] getIsolateMetaData(int[] ids, Ontology ont, int dataSize) throws SQLException {
      return getIsolateMetaData(ids, ont, dataSize, DEFAULT_PAGE_SIZE);
   }

   public String[][] getIsolateMetaData(int[] ids, Ontology ont, int dataSize, int pageSize) {
      Statement statement = null;
      ResultSet results = null;
      String metaColumns = "", metaIDs = "";
      String metaLabels[][] = new String[ids.length][];
      int tmp_id = -1, isolateID = -1, isolateNdx = -1, numColumns = 0;

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
               for (int colNdx = 2; colNdx <= numColumns; colNdx++) {
                  if (colNdx >= metaLabels[isolateNdx].length) {
                     String[] newArr = new String[metaLabels[isolateNdx].length * 2];
                     for (int metaNdx = 0; metaNdx < metaLabels[isolateNdx].length; metaNdx++) {
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
