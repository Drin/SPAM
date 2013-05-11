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
            if (clust_sim > threshold and clust_sim > close_clusters[2]):
               close_clusters = (ndx_A, ndx_B, clust_sim)

            else:
               average_inter_similarity += clust_sim
               count += 1

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
   _sim_matrix_ = None
   _iso_mapping_ = None
   _threshold_ = -1
   
   def __init__(self, data_point, labels=None):
      self.labels = labels
      self.elements = numpy.zeros(shape=(0), dtype=numpy.uint32, order='C')
      self.elements = numpy.append(self.elements, data_point)

   def get_similarity(self, ndx_A, ndx_B):
      sim_ndx = -1

      if (iso_ndx_B > iso_ndx_A):
         sim_ndx = Cluster._iso_mapping_[iso_ndx_A][iso_ndx_B]
      elif (iso_ndx_A > iso_ndx_B):
         sim_ndx = Cluster._iso_mapping_[iso_ndx_B][iso_ndx_A]

      if (sim_ndx != -1):
         return Cluster._sim_matrix_[sim_ndx]

      return -1

   def get_statistics(self):
      (self.diameter, count, clust_sim) = (1, 0, 0)
      (self.percent_similar, self.average_similarity) = (0, 0)

      for iso_ndx_A in range(len(self.elements)):
         for iso_ndx_B in range(iso_ndx_A + 1, len(other_cluster.elements)):
            clust_sim = self.get_similarity(iso_ndx_A, iso_ndx_B)

            count += 1
            self.average_similarity += clust_sim
            self.diameter = min(self.diameter, clust_sim)
            if (clust_sim > _threshold_):
               self.percent_similar += 1

      if (count > 0):
         self.average_similarity = self.average_similarity / count
         self.percent_similar = self.percent_similar / count
      elif (len(self.elements) != 1):
         print("cluster with %d elements and %d count. wtf." %
               (len(self.elements), count))

   def incorporate(self, other_cluster):
      self.elements = numpy.append(self.elements, other_cluster.elements)
      self.get_statistics()

   def compare_to(self, other_cluster):
      (clust_sim, count) = (0, 0)

      for iso_ndx_A in self.elements:
         for iso_ndx_B in other_cluster.elements:
            count += 1
            clust_sim += self.get_similarity(iso_ndx_A, iso_ndx_B)

      if (count > 0):
         return (clust_sim / count)

      print("count is 0 when comparing. wtf")
      return -2

   def __str__(self):
      return ', '.join([str(val) for val in self.elements])

   def __repr__(self):
      return ', '.join([str(val) for val in self.elements])

   def __len__(self):
      return len(self.elements)
