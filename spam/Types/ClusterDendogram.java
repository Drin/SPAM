package spam.Types;

import spam.dendogram.Dendogram;
import spam.Types.Cluster;

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
