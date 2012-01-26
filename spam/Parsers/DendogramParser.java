package spam.dataParser;

import java.io.File;

import javax.xml.ws;

public class DendogramParser {
   private File xmlFile = null;

   public DendogramParser(String dendogramFile) {
      xmlFile = new File(dendogramFile);
   }

   public static void main(String[] args) {
      if (args.length != 1) {
         System.out.println("Usage: DendogramParser <DendogramFileName>");
         System.exit(1);
      }

      DendogramParser parser = new DendogramParser(args[0]);
   }
}
