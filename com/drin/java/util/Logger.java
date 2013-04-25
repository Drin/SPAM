package com.drin.java.util;

import com.drin.java.util.Configuration;

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
   private static Configuration mConfig = null;

   private File mLogFile;
   private FileWriter mWriter;

   public static Logger getLogger() {
      if (mConfig == null) {
         mConfig = Configuration.loadConfig();
      }

      if (mLogger == null) {
         mLogger = new Logger(DEFAULT_LOG_FILE + LOG_FILE_EXT);
      }

      return mLogger;
   }

   private Logger(String filename) {
      mLogFile = new File(filename);

      try {
         mWriter = new FileWriter(mLogFile);
         
         if (!mLogFile.exists()) {
            mLogFile.createNewFile();
            mWriter.write(
               String.format("[%s] Log file initialized\n", new Date())
            );
         }
      }
      catch (java.io.IOException ioErr) {
         System.err.printf("IO Error when opening file '%s'\n", filename);
         ioErr.printStackTrace();
      }
   }

   private static void writeString(String outString) {
      try {
         getLogger().mWriter.write(outString);
         getLogger().mWriter.flush();
      }
      catch(java.io.IOException ioErr) {
         System.err.printf("IO Error when writing to log file.\n");
         ioErr.printStackTrace();
      }
   }

   public static void debug(String dbgString) {
      Configuration config = Configuration.getConfig();
      
      if (config != null && Boolean.parseBoolean(config.getAttr("debug"))) {
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
