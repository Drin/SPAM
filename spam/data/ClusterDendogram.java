package spam.data;

import spam.data.dendogram.Dendogram;
import spam.data.Cluster;

public class ClusterDendogram {
   private Cluster mCluster;
   private Dendogram mDendogram;

   public ClusterDendogram(Cluster cluster, Dendogram dendogram) {
      mCluster = cluster;
      mDendogram = dendogram;
   }

   public Cluster getCluster() {
      return mCluster;
   }

   public Dendogram getDendogram() {
      return mDendogram;
   }
}
