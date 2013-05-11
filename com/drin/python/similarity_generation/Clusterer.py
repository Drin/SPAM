import os
import sys
import numpy
import time

import Ontology

try:
   import pycuda.driver
   import pycuda.compiler
   import pycuda.gpuarray

except:
   print("Could not load pyCUDA")
   sys.exit(1)

#############################################################################
#
# Cluster Analysis Class
#
#############################################################################
class Clusterer(object):
   def __init__(self, threshold, ontology=None):
      self.ontology = ontology
      self.threshold = threshold
      self.average_inter_similarity = 0

   def cluster_data(self, clusters):
      if (self.ontology is not None):
         for cluster in clusters:
            self.ontology.add_data(cluster)

         self.cluster_ontology(self.ontology.root, self.threshold)

      else:
         self.cluster_dataset(clusters, self.threshold)

   def cluster_ontology(self, ontology_term, threshold):
      if (ontology_term is None or ontology_term.new_data is False):
         return

      clusters = ontology_term.data
      for term in ontology_term.children.items():
         if (term[1] is None): continue

         if (term[1].new_data):
            self.cluster_ontology(term[1], threshold)

         clusters.append(term[1].data)
         #self.cluster_dataset(clusters, threshold)

      self.cluster_dataset(clusters, threshold)

   def cluster_dataset(self, clusters, threshold):
      while(len(clusters) > 1):
         cluster_time = time.time()

         close_clusters = self.find_close_clusters(clusters, threshold)

         if (close_clusters[0] != -1 and close_clusters[1] != -1):
            self.combine_clusters(clusters, close_clusters)
         else:
            break

   def find_close_clusters(self, clusters, threshold):
      close_clusters = (-1, -1, 0)
      (average_inter_similarity, count) = (0, 0)

      for ndx_A in range(len(clusters)):
         clust_A = clusters[ndx_A]

         for ndx_B in range(ndx_A + 1, len(clusters)):
            clust_B = clusters[ndx_B]
            
            if (type(clust_A) is not Cluster or type(clust_B) is not Cluster):
               continue

            clust_sim = clust_A.compare_to(clust_B)
            average_inter_similarity += clust_sim
            count += 1

            if (clust_sim > threshold and clust_sim > close_clusters[2]):
               close_clusters = (ndx_A, ndx_B, clust_sim)

      if (count > 0):
         self.average_inter_similarity = average_inter_similarity/count

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

   def __init__(self, data_point=-1, labels=None):
      self.elements = numpy.zeros(shape=(0), dtype=numpy.uint32, order='C')
      self.labels = labels
      (self.total_sim, self.total_intra_sim) = (0, 0)
      (self.total_count, self.count) = (0, 0)
      (self.diameter, self.max_dist) = (2, 2)
      self.other_clust = -1

      if (data_point != -1):
         self.elements = numpy.append(self.elements, data_point)

   def incorporate(self, other_cluster):
      self.compare_to(other_cluster)
      self.elements = numpy.append(self.elements, other_cluster.elements)

      if (self.other_clust == other_cluster.elements[0]):
         self.total_intra_sim += self.total_sim
         self.total_count += self.count
         if (self.max_dist < self.diameter):
            self.diameter = self.max_dist

   def get_intra_similarity(self):
      if (self.total_count == 0):
         return 1

      return self.total_intra_sim / self.total_count

   def compare_to(self, other_cluster):
      if (Cluster.sSim_matrix is not None):
         (self.total_sim, self.count, self.other_clust, self.max_dist) = (
            0, 0, other_cluster.elements[0], 2
         )

         for iso_ndx_A in self.elements:
            for iso_ndx_B in other_cluster.elements:
               self.count += 1

               clust_sim = 0
               if (iso_ndx_B > iso_ndx_A):
                  clust_sim += Cluster.sSim_matrix[1][
                     Cluster.sSim_matrix[0][iso_ndx_A][iso_ndx_B]
                  ]
               elif (iso_ndx_A > iso_ndx_B):
                  clust_sim += Cluster.sSim_matrix[1][
                     Cluster.sSim_matrix[0][iso_ndx_B][iso_ndx_A]
                  ]

               self.total_sim += clust_sim
               if (clust_sim < self.max_dist):
                  self.max_dist = clust_sim

         if (self.count == 0):
            return 0

         return (self.total_sim / self.count)

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
