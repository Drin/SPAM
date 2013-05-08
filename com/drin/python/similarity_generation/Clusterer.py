import os
import sys
import numpy

try:
   import pycuda.driver
   import pycuda.compiler
   import pycuda.gpuarray

except:
   print("Could not load pyCUDA")
   sys.exit(1)

#import Ontology

# TODO: Ontology

#############################################################################
#
# Cluster Analysis Class
#
#############################################################################
class Clusterer(object):
   def __init__(self, thresholds, ontology=None):
      self.ontology = ontology
      self.thresholds = thresholds

   def cluster_data(self, clusters):
      for threshold in self.thresholds:
         print("clustering using threshold: %.04f" % threshold)
         self.cluster_dataset(clusters, threshold)

   def cluster_dataset(self, clusters, threshold):
      while(len(clusters) > 1):
         close_clusters = self.find_close_clusters(clusters, threshold)

         if (close_clusters[0] != -1 and close_clusters[1] != -1):
            self.combine_clusters(clusters, close_clusters)
         else:
            break

   def find_close_clusters(self, clusters, threshold):
      close_clusters = (-1, -1, 0)

      for ndx_A in range(len(clusters)):
         clust_A = clusters[ndx_A]

         for ndx_B in range(ndx_A + 1, len(clusters)):
            clust_B = clusters[ndx_B]
            
            clust_sim = clust_A.compare_to(clust_B)

            if (clust_sim > threshold and clust_sim > close_clusters[2]):
               close_clusters = (ndx_A, ndx_B, clust_sim)

      return close_clusters

   def combine_clusters(self, clusters, close_clusters):
      clusters[close_clusters[0]].incorporate(clusters[close_clusters[1]])
      del clusters[close_clusters[1]]

#############################################################################
#
# Cluster Class
#
#############################################################################
class Cluster(object):
   sSim_matrix = None
   sClust_comparator = None

   def __init__(self, data_point=-1):
      self.elements = numpy.zeros(shape=(0), dtype=numpy.uint32, order='C')

      if (data_point != -1):
         self.elements = numpy.append(self.elements, data_point)

   def incorporate(self, other_cluster):
      self.elements = numpy.append(self.elements, other_cluster.elements)

   def compare_to(self, other_cluster):
      if (Cluster.sSim_matrix is not None):
         (total_sim, count) = (0, 0)

         for iso_ndx_A in self.elements:
            for iso_ndx_B in other_cluster.elements:
               count += 1

               if (iso_ndx_B > iso_ndx_A):
                  total_sim += Cluster.sSim_matrix[1][
                     Cluster.sSim_matrix[0][iso_ndx_A][iso_ndx_B]
                  ]
               elif (iso_ndx_A > iso_ndx_B):
                  total_sim += Cluster.sSim_matrix[1][
                     Cluster.sSim_matrix[0][iso_ndx_B][iso_ndx_A]
                  ]

         return (total_sim / count)

      elif (Cluster.sClust_comparator is not None):
         return Cluster.sClust_comparator(self, other_cluster)

      else:
         print("clust comparator not set")
         sys.exit(0)


   def __str__(self):
      return ', '.join([str(val) for val in self.elements])

   def __repr__(self):
      return ', '.join([str(val) for val in self.elements])

   def __len__(self):
      return len(self.elements)
