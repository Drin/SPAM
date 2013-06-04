package com.drin.java.analysis.clustering;

import com.drin.java.database.DBConnection;

import java.sql.SQLException;

import java.util.List;

public class ClusteringComparator {
   private static DBConnection mConn = null;

   public static void main(String[] args) {
      ClusteringComparator main = new ClusteringComparator();
      main.testConnection();
   }

   public ClusteringComparator() {
      try {
         mConn = new DBConnection("localhost", 8906, "CPLOP", "drin", "");
      }
      catch (SQLException sqlErr) {
         sqlErr.printStackTrace();
         mConn = null;
      }
      catch (com.drin.java.database.DBConnection.DriverException err) {
         err.printStackTrace();
         mConn = null;
      }
   }

   private void testConnection() {
      try {
         List<String> results = mConn.testConnection();

         System.out.println("results:");
         for (String result : results) {
            System.err.printf("\t%s\n", result);
         }
      }
      catch(SQLException sqlErr) {
         sqlErr.printStackTrace();
      }
   }
}
