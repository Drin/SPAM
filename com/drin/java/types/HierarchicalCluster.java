package com.drin.java.types;

import com.drin.java.types.Cluster;
import com.drin.java.types.DendogramNode;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterStatistics;

import java.util.Map;

public class HierarchicalCluster extends Cluster<DataObject> {
   private static final String MIN_KEY = "min",
                               MAX_KEY = "max",
                               MEAN_KEY = "mean";
   private DendogramNode mDend;

   public HierarchicalCluster(String name) {
      super(name);
   }

   public HierarchicalCluster(String name, Cluster<DataObject> clust_A,
    Cluster<DataObject> clust_B) {
      this(name);

      mElements.add(clust_A);
      mElements.add(clust_B);

      DendogramNode leftNode = null, rightNode = null;

      if (clust_A instanceof HierarchicalCluster) {
         leftNode = ((HierarchicalCluster) clust_A).mDend;
      }
      else { leftNode = new DendogramNode(clust_A.getName()); }

      if (clust_B instanceof HierarchicalCluster) {
         rightNode = ((HierarchicalCluster) clust_B).mDend;
      }
      else { rightNode = new DendogramNode(clust_B.getName()); }

      mDend = new DendogramNode(name, leftNode, rightNode);
   }

   public HierarchicalCluster(Cluster<DataObject> clust_A, Cluster<DataObject> clust_B) {
      this(clust_A.getName(), clust_A, clust_B);
   }

   public HierarchicalCluster(HierarchicalCluster originalCluster) {
      this(originalCluster.getName());
   }

   @Override
   public Cluster<DataObject> union(Cluster<DataObject> otherCluster) {
      HierarchicalCluster newClust = new HierarchicalCluster(this.getName(), this, otherCluster);

      return newClust;
   }

   @Override
   public String getDendogram(DataMetric dataMetric) {
      return getDendogram(this, dataMetric, "   ");
   }

   public String getDendogram(HierarchicalCluster cluster, DataMetric dataMetric, String prefix) {
      Map<String, Double> statMap = ClusterStatistics.calcStats(dataMetric, cluster.getElements());

      String dendogram = String.format("%s<Cluster name=\"%s\" maxCorr=\"%.04f\"" +
       " minCorr=\"%.04f\" avgCorr=\"%.04f\"/>\n", prefix, cluster.getName(),
       statMap.get(MAX_KEY), statMap.get(MIN_KEY), statMap.get(MEAN_KEY));

      for (int elemNdx = 0; elemNdx < cluster.mElements.size(); elemNdx++) {
         DataObject element = cluster.mElements.get(elemNdx);

         if (element instanceof HierarchicalCluster) {
            dendogram += getDendogram((HierarchicalCluster) element, dataMetric, prefix + "   ");
         }

         else {
            dendogram += String.format("%s<Isolate name=\"%s\"/>\n",
             prefix + "   ", element.getName());
         }
      }

      dendogram += String.format("%s</Cluster>\n", prefix);

      return dendogram;
   }

   @Override
   public String prettyPrint(String prefix) {
      String str = "";

      for (DataObject element : mElements) {
         str += element.toString();
      }

      return str;
   }
}
