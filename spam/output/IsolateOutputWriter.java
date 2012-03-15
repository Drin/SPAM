package spam.dataWriter;

import spam.clustering.HClustering;

import spam.dataTypes.Cluster;
import spam.dataTypes.ClusterDendogram;
import spam.dataTypes.Isolate;

import spam.dataStructures.IsolateSimilarityMatrix;

import spam.distanceMeasures.IsolateSimilarity;

import spam.dendogram.Dendogram;
import spam.dendogram.DendogramTree;
import spam.dendogram.TreeNode;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

public class IsolateOutputWriter {

   public static void outputTemporalCharts(DendogramTree tree, String filePrefix, String thresholdValue) {
      List<String> graphContainers = new ArrayList<String>();
      List<String> graphCharts = new ArrayList<String>();
      String outFileName = filePrefix + "_thresholdedClusters_" + thresholdValue + ".html";
      String chartFormat = "";
      
      String fecalSeries = "", immSeries = "", laterSeries = "", deepSeries = "", beforeSeries = "";

      int numCluster = 0;
      for (TreeNode treeNode : tree.getTree()) {
         graphContainers.add(String.format("<div id='cluster%d' class='highcharts-container'></div>", numCluster));

         fecalSeries = treeNode.getFecalSeries();
         immSeries = treeNode.getImmSeries();
         laterSeries = treeNode.getLaterSeries();
         deepSeries = treeNode.getDeepSeries();
         beforeSeries = treeNode.getBeforeSeries();

         graphCharts.add(newGraphChart(String.format("cluster%d", numCluster++), fecalSeries, immSeries, laterSeries, deepSeries, beforeSeries));
      }

      String htmlStr = buildChartHtml(graphContainers, buildChartJs(graphCharts));

      try {
         File outputFile = new File(outFileName);
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(htmlStr);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputTemporalCharts(List<ClusterDendogram> clustDends, String filePrefix) {
      List<String> graphContainers = new ArrayList<String>();
      List<String> graphCharts = new ArrayList<String>();
      String outFileName = filePrefix + "_temporalCharts.html";
      String chartFormat = "";
      
      String fecalSeries = "", immSeries = "", laterSeries = "", deepSeries = "", beforeSeries = "";

      int numCluster = 0;
      for (ClusterDendogram clustDend : clustDends) {
         Cluster tmpClust = clustDend.getCluster();
         graphContainers.add(String.format("<div id='cluster%d' class='highcharts-container'></div>", numCluster));

         fecalSeries = tmpClust.getFecalSeries();
         immSeries = tmpClust.getImmSeries();
         laterSeries = tmpClust.getLaterSeries();
         deepSeries = tmpClust.getDeepSeries();
         beforeSeries = tmpClust.getBeforeSeries();

         graphCharts.add(newGraphChart(String.format("cluster%d", numCluster++), fecalSeries, immSeries, laterSeries, deepSeries, beforeSeries));
      }

      String htmlStr = buildChartHtml(graphContainers, buildChartJs(graphCharts));

      try {
         File outputFile = new File(outFileName);
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(htmlStr);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   private static String buildChartHtml(List<String> graphContainers, String graphCharts) {
      String htmlStr =
       "<!DOCTYPE html>\n" +
       "<html>\n" +
       "\t<head>\n" +
       "\t\t<title> temporalClusters </title>\n" +
       "\t\t<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>\n" +
       "\t\t<script type=\"text/javascript\" src=\"http://users.csc.calpoly.edu/~amontana/TemporalHClustering/js/highcharts.js\"></script>\n" +
       "\t\t<script type=\"text/javascript\">\n" + graphCharts + "</script>\n" +
       "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"css/clusters.css\"/>\n" +
       "\t</head>\n" +
       "\t<body>\n";

      for (String graphContainer : graphContainers) {
         htmlStr += "\t\t" + graphContainer + "\n";
      }

      return htmlStr + "\t</body>\n</html>\n";
   }
   
   private static String buildChartJs(List<String> graphCharts) {
      String javascriptStr = "$(document).ready(function(){\n";

      for (String graphChart : graphCharts) {
         javascriptStr += graphChart + "\n";
      }

      return javascriptStr + "});";
   }

   private static String newGraphChart(String clusterName, String fecalSeries, String immSeries, String laterSeries, String deepSeries, String beforeSeries) {
      return String.format(
         "var %s = new Highcharts.Chart({\n" +
            "chart: {\n" +
               "renderTo: '%s',\n" +
               "defaultSeriesType: 'column'\n" +
            "},\n" +
            "title: {\n" +
               "text: '%s'\n" +
            "},\n" +
            "xAxis: {\n" +
               "categories: ['Day 1', 'Day 2', 'Day 3', 'Day 4', 'Day 5', 'Day 6',\n" +
               "'Day 7', 'Day 8', 'Day 9', 'Day 10', 'Day 11', 'Day 12', 'Day 13',\n" +
               "'Day 14']\n" +
            "},\n" +
            "yAxis: {\n" +
               "min: 0,\n" +
               "title: {\n" +
                  "text: 'Number of pyrograms in cluster'\n" +
               "},\n" +
               "stackLabels: {\n" +
                  "enabled: true,\n" +
                  "style: {\n" +
                     "fontWeight: 'bold',\n" +
                     "color: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'gray'\n" +
                  "}\n" +
               "},\n" +
               "tickInterval: 2\n" +
            "},\n" +
            "legend: {\n" +
               "align: 'right',\n" +
               "x: -100,\n" +
               "verticalAlign: 'top',\n" +
               "y: 20,\n" +
               "floating: true,\n" +
               "backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',\n" +
               "borderColor: '#CCC',\n" +
               "borderWidth: 1,\n" +
               "shadow: false\n" +
            "},\n" +
            "tooltip: {\n" +
               "formatter: function() {\n" +
                  "return '<b>' + this.x + '</b><br />' +\n" +
                         "this.series.name + ': ' + this.y + '<br />' +\n" +
                         "'Total: ' + this.point.stackTotal;\n" +
               "}\n" +
            "},\n" +
            "plotOptions: {\n" +
               "column: {\n" +
                  "stacking: 'normal',\n" +
                  "dataLabels: {\n" +
                     "enabled: true,\n" +
                     "color: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white'\n" +
                  "}\n" +
               "}\n" +
            "},\n" +
            "series: [{\n" +
               "name: 'Fecal',\n" +
               "data: [%s]\n" +
            "}, {\n" +
               "name: 'Immediate',\n" +
               "data: [%s]\n" +
            "}, {\n" +
               "name: 'Later',\n" +
               "data: [%s]\n" +
            "}, {\n" +
               "name: 'Deep',\n" +
               "data: [%s]\n" +
            "}, {\n" +
               "name: 'Before',\n" +
               "data: [%s]\n" +
            "}]\n" +
         "});", clusterName, clusterName, clusterName, fecalSeries, immSeries, laterSeries, deepSeries, beforeSeries);
   }

   public static void outputTemporalClusters(List<ClusterDendogram> clustDends, String filePrefix) {
      String outFileName = filePrefix + "_temporalDiagram.csv";
      String cytoFormat = "";

      int clusterNum = -1;
      for (ClusterDendogram clustDend : clustDends) {
         clusterNum++;
         //the new lines are to obviate the separation between clusters
         cytoFormat += clustDend.getCluster().toTemporalFormat(clusterNum);
      }

      try {
         File outputFile = new File(outFileName);
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(cytoFormat);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputCytoscapeFormat(List<ClusterDendogram> clustDends, String filePrefix) {
      String outFileName = filePrefix + "_cytoscapeNetwork.txt";
      String cytoFormat = Cluster.cytoscapeFormatHeader();

      int clusterNum = -1;
      for (ClusterDendogram clustDend : clustDends) {
         clusterNum++;
         //the new lines are to obviate the separation between clusters
         cytoFormat += "\n\n\n";
         cytoFormat += clustDend.getCluster().toCytoscapeCluster("Cluster_" + clusterNum);
         //cytoFormat += clustDend.getCluster().toCytoscapeFormat();
         cytoFormat += "\n\n\n";
      }

      try {
         File outputFile = new File(outFileName);
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(cytoFormat);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputClustersByDay(IsolateSimilarityMatrix similarityMatrix, int sampleDay, List<ClusterDendogram> clustDends) {
      String outFileName = "ClusterDays/clustersByDay" + sampleDay + ".xml";
      String separator = "\n====================\n";
      String clusterOutput = "";

      for (ClusterDendogram clustDend : clustDends) {
         clusterOutput += clustDend.getDendogram().getXML() + separator;
      }

      clusterOutput += "\n\n\n\n";
      List<Isolate> isolateList = new ArrayList<Isolate>();

      for (ClusterDendogram clustDend : clustDends) {
         isolateList.addAll(clustDend.getCluster().getIsolates());
      }

      clusterOutput += "\t";
      for (int ndx = 0; ndx < isolateList.size(); ndx++) {
         clusterOutput += "\t" + isolateList.get(ndx);
      }
      clusterOutput += "\n";

      for (int ndxOne = 0; ndxOne < isolateList.size(); ndxOne++) {
         clusterOutput += isolateList.get(ndxOne);
         for (int ndxTwo = 0; ndxTwo < isolateList.size(); ndxTwo++) {
            clusterOutput += "\t" + similarityMatrix.getCorrelationVal(
             isolateList.get(ndxOne), isolateList.get(ndxTwo));
         }
         clusterOutput += "\n";
      }

      try {
         File outputFile = new File(outFileName);
         if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
         }
         if (!outputFile.exists()) {
            outputFile.createNewFile();
         }
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(clusterOutput);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputClusters(DendogramTree tree, String filePrefix) {
      BufferedWriter isolateWriter = null;
      String isolateOutput = "";

      int clusterNum = 0;
      for (TreeNode node : tree.getTree()) {
         isolateOutput += "Cluster " + (clusterNum++) + ",\n";
         for (Isolate isolate : node.getIsolateList()) {
            isolateOutput += ",\t" + isolate + "\n";
         }
      }

      //outputs xml and cluster data

      try {
         File isolateFile = new File(filePrefix + "_thresholdedIsolates.csv");

         isolateWriter = new BufferedWriter(new FileWriter(isolateFile));

         isolateWriter.write("Cluster Isolate Listing\n");
         isolateWriter.write(isolateOutput);
         isolateWriter.close();
      }
      catch(Exception e1) {
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputClusters(List<ClusterDendogram> clustDends,
    String outFileDir, String outputFileName) {
      BufferedWriter xmlWriter = null, isolateWriter = null;
      String xmlOutput = "", isolateOutput = "";

      List<Dendogram> dendogramList = new ArrayList<Dendogram>();
      for (ClusterDendogram clusterDend : clustDends) {
         dendogramList.add(clusterDend.getDendogram());
         //System.out.println(clusterDend.getCluster());
      }

      //System.out.println(DendogramNode.toUAGDot(dendogramList));

      int clusterNum = 0;
      for (ClusterDendogram clustDend : clustDends) {
         xmlOutput += clustDend.getDendogram().getXML() + "\n";
         isolateOutput += "Cluster " + (clusterNum++) + ",\n";

         for (Isolate isolate : clustDend.getCluster().getIsolates()) {
            isolateOutput += ",\t" + isolate + "\n";
         }
         //System.out.println("dendogram: " + clustDend.getDendogram().getXML());
      }

      //outputs xml and cluster data
      File xmlOutDir = null;

      try {
         xmlOutDir = new File(outFileDir);

         if(!xmlOutDir.isDirectory()) {
            if (!xmlOutDir.mkdirs()) {
               System.out.println("error creating directory");
               System.exit(1);
            }
         }

         xmlWriter = new BufferedWriter(new FileWriter(
          new File(outputFileName)));

         xmlWriter.write("<IsolateClusters>\n");
         xmlWriter.write(xmlOutput);
         xmlWriter.write("</IsolateClusters>");
         xmlWriter.close();


         isolateWriter = new BufferedWriter(new FileWriter(
          new File(outputFileName.replace(".xml", "_isolates.csv"))));

         isolateWriter.write("Cluster Isolate Listing\n");
         isolateWriter.write(isolateOutput);
         isolateWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }

      System.out.println("clustering runtime was " + HClustering.getRunTime());
   }
}
