package com.drin.java.util;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class Logger {
   private static final String DEFAULT_LOG_FILE = "spam",
                               LOG_FILE_EXT = ".log",
                               ERR_PREFIX = "*ERROR*",
                               DEBUG_PREFIX = "DEBUG",
                               WARNING_PREFIX = "WARNING";

   private static Logger mLogger = null;

   private File mLogFile;
   private FileWriter mWriter;
   private boolean mDebug;

   public static Logger getLogger() {
      if (mLogger == null) {
         mLogger = new Logger(DEFAULT_LOG_FILE + LOG_FILE_EXT);
      }

      return mLogger;
   }

   private Logger(String filename) {
      this(filename, true);
   }

   private Logger(String filename, boolean debugMode) {
      mDebug = debugMode;
      mLogFile = new File(filename);

      try {
         mWriter = new FileWriter(mLogFile);
      }
      catch (java.io.IOException ioErr) {
         System.err.printf("IO Error when opening file '%s'\n", filename);
         ioErr.printStackTrace();
      }
   }

   private static void writeString(String outString) {
      try {
         getLogger().mWriter.write(outString);
      }
      catch(java.io.IOException ioErr) {
         System.err.printf("IO Error when writing to log file.\n");
         ioErr.printStackTrace();
      }
   }

   public static void debug(String dbgString) {
      if (getLogger().mDebug) {
         writeString(String.format("(%s) %s: %s\n", new Date(),
                                   DEBUG_PREFIX, dbgString));
      }
   }

   public static void error(int errCode, String errString) {
      if (errCode != 0) {
         writeString(String.format("(%s) %s: %s\n", new Date(),
                                   ERR_PREFIX, errString));
      }
   }

   public static void warning(String warnString) {
      writeString(String.format("(%s) %s: %s\n", new Date(),
                                WARNING_PREFIX, warnString));
   }

   public static void terminate() {
      try {
         getLogger().mWriter.close();
      }
      catch(java.io.IOException ioErr) {
         System.err.printf("IO Error when closing log file\n");
         ioErr.printStackTrace();
      }
   }
}
