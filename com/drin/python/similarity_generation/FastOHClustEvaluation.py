import os
import sys
import time
import math

import numpy
import copy

import CPLOP
import Clusterer
import Ontology

try:
   import pycuda.driver
   import pycuda.compiler
   import pycuda.gpuarray

except:
   print("Could not load pyCUDA")
   sys.exit(1)

DEFAULT_DEVICE = 0
NUM_THREADS = 16
NUM_BLOCKS = 32

DEBUG_PEEK_SIZE = 10
CUDA_KERNEL_DIR = 'kernel_pool'

#############################################################################
#
# Load CUDA Kernel
#
#############################################################################
def load_cuda():
   cuda_src = ''

   for file_name in os.listdir(CUDA_KERNEL_DIR):
      cuda_file = open(os.path.join(CUDA_KERNEL_DIR, file_name), 'r')
      cuda_src += cuda_file.read()
      cuda_file.close()

   return pycuda.compiler.SourceModule(cuda_src)

#############################################################################
#
# CUDA comparator for clusters
#
#############################################################################
def cluster_comparator(clust_A, clust_B,
                       num_threads=NUM_THREADS, num_blocks=NUM_BLOCKS,
                       kernel_name='cluster_pearson', device_id=DEFAULT_DEVICE):
   # Convenience Variables
   tile_size = (num_threads * num_blocks)
   num_tiles = math.ceil(max(len(clust_A), len(clust_B))/ tile_size)

   cuda_context = pycuda.driver.Device(device_id).make_context()
   cuda_kernel = load_cuda().get_function(kernel_name)

   ############################################################################
   #
   # Main Workload
   #
   ############################################################################

   clust_A_gpu = pycuda.gpuarray.to_gpu(clust_A)
   clust_B_gpu = pycuda.gpuarray.to_gpu(clust_B)

   cluster_sim = numpy.zeros(shape=(1), dtype=numpy.float32, order='C')
   cluster_sim_gpu = pycuda.gpuarray.to_gpu(cluster_sim)

   for tile_row in range(num_tiles):
      for tile_col in range(tile_row, num_tiles):
         cuda_kernel(numpy.uint32(len(clust_A)), clust_A_gpu.gpudata,
                     numpy.uint32(len(clust_B)), clust_B_gpu.gpudata,
                     numpy.uint32(tile_size), numpy.uint32(tile_row),
                     numpy.uint32(tile_col), cluster_sim_gpu.gpudata,
                     block = (num_threads, num_threads, 1),
                     grid  = (num_blocks,  num_blocks))
   cluster_sim_gpu.get(cluster_sim)

   ############################################################################
   #
   # Cleanup CUDA
   #
   ############################################################################
   pycuda.driver.Context.pop()

   return cluster_sim[0]

#############################################################################
#
# Retrieve CPLOP Data
#
#############################################################################
def get_data(dataset_size, ontology=None):
   conn = CPLOP.connection()

   print("getting data")

   start_t = time.time()
   (iso_ids, iso_data) = conn.get_fast_isolate_data(data_size=(dataset_size))

   print("getting meta-data")

   meta_t = time.time()
   iso_labels = conn.get_meta_data(ont=ontology, ids=iso_ids,
                                   data_size=dataset_size)
   finish_t = time.time()

   out_file = open('database_access_times', 'a')
   out_file.write("%d[%d] isolates in (%ds, %ds, %ds)\n" % (
      len(iso_ids), len(iso_data), meta_t - start_t,
      finish_t - meta_t, finish_t - start_t
   ))
   out_file.close()

   return (iso_ids, iso_labels, iso_data)

#############################################################################
#
# Main Function
#
#############################################################################
def main(iso_ids, iso_labels, iso_data_cpu,
         incremental=False, clust_ontology=None,
         init_size=100, up_size=100, num_up=1):
   num_isolates = (init_size + (up_size * num_up))

   ############################################################################
   #
   # Clustering Section
   #
   ############################################################################

   conn = CPLOP.connection()

   threshold = 0.80
   Clusterer.Cluster._threshold_ = threshold

   (start_ndx, end_ndx, curr_up) = (0, init_size, -1)
   (persist_clusters, tmp_clusters, OHClusterer) = ([], None, None)
   (total_t, perf_info, backup_clusters) = (time.time(), [], [])

   print("num data points: %d" % len(iso_data_cpu))

   while (curr_up < num_up and start_ndx < end_ndx and start_ndx < num_isolates):
      print("end_ndx - start_ndx = %d" % (end_ndx - start_ndx))
      (iso_ndx, tmp_clusters) = (0, [])

      while (iso_ndx < (end_ndx - start_ndx)):
         print("iso_ndx: %d" % iso_ndx)
         tmp_clusters.append(Clusterer.FastCluster(
            isolate_data=iso_data_cpu.pop(0), labels=iso_labels[iso_ndx],
            comparator=(lambda x,y: cluster_comparator(x, y))
         ))
         iso_ndx += 1

      OHClusterer = Clusterer.FastClusterer(threshold, ontology=clust_ontology)
      cluster_t = time.time()

      if (clust_ontology is not None):
         persist_clusters = tmp_clusters

      elif (incremental):
         persist_clusters.extend(tmp_clusters)

      else:
         backup_clusters.extend(copy.deepcopy(tmp_clusters))
         persist_clusters.extend(copy.deepcopy(backup_clusters))

      OHClusterer.cluster_data(persist_clusters)
      finish_t = time.time()

      perf_info.append((curr_up + 1, end_ndx, finish_t - cluster_t))

      #prepare update state for next iteration
      curr_up += 1
      start_ndx = end_ndx
      end_ndx += up_size

   print("%d clusters:" % len(persist_clusters))
   if ('DEBUG' in os.environ):
      if (clust_ontology is not None):
         for cluster in persist_clusters:
            print("cluster [%d]:" % len(cluster))
            for element in cluster.elements:
               print("\t%s" % (iso_ids[element]))

      for tmp_ndx in range(10):
         print(sim_matrix_cpu[tmp_ndx])

      if ('VERBOSE' in os.environ):
         output_similarity_matrix(sim_matrix_cpu, len(iso_ids), iso_ids)
   '''
   clust_sep = -2
   if (clust_ontology is not None):
      clust_sep = clust_ontology.get_cluster_separation()
      persist_clusters = clust_ontology.root.data
   else:
      (clust_sep, count) = (0, 0)

      for ndx_A in range(len(persist_clusters)):
         for ndx_B in range(ndx_A + 1, len(persist_clusters)):
            if (type(persist_clusters[ndx_A]) is Clusterer.Cluster):
               count += 1
               clust_sep += persist_clusters[ndx_A].compare_to(
                            persist_clusters[ndx_B])

      if (count > 0):
         clust_sep = (clust_sep / count)

   conn.insert_new_run(clust_sep, time.time() - total_t,
                       cluster_algorithm=algorith_name)

   test_run_id = conn.get_run_id()
   print("inserting data for run id %d" % test_run_id)
   conn.insert_run_perf(test_run_id, perf_info)
   conn.insert_clusters(test_run_id, iso_ids, persist_clusters, threshold)
   '''

if (__name__ == '__main__'):
   (clust_ontology, algorith_name) = (None, 'agglomerative')
   clust_ontology = Ontology.OntologyParser().parse_ontology('generic.ont')

   (iso_ids, iso_labels, iso_data_cpu) = get_data(
      #(2500 + (125 * 100)), ontology=clust_ontology
      (100 + (10 * 1)), ontology=clust_ontology
      #(500 + (50 * 100)), ontology=clust_ontology
   )

   (iso_sim_mapping, sim_matrix_ndx) = (dict(), 0)
   for ndx_A in range(len(iso_ids)):
      for ndx_B in range(ndx_A + 1, len(iso_ids)):
         if (iso_sim_mapping.get(ndx_A) is None):
            iso_sim_mapping[ndx_A] = dict()
         iso_sim_mapping[ndx_A][ndx_B] = sim_matrix_ndx
         sim_matrix_ndx += 1

   ############################################################################
   #
   # CUDA Section
   #
   ############################################################################
   pycuda.driver.init()

   '''
   configurations = ((100, 10, 100), (500, 50, 100))
                     #(1000, 50, 100), (1000, 100, 100),
                     #(2500, 125, 100))#, (2500, 250, 100))
   '''
   configurations = ((100, 10, 1),)

   for test_conf in configurations:
      for test_num in range(3):
         tmp_ontology = copy.deepcopy(clust_ontology)

         print("OHclustering")
         main(iso_ids, iso_labels, copy.deepcopy(iso_data_cpu),
              clust_ontology=tmp_ontology, init_size=test_conf[0],
              up_size=test_conf[1], num_up=test_conf[2])


   for test_conf_ndx in range(len(configurations)):
      test_conf = configurations[test_conf_ndx]

      for test_num in range(3):
         print("agglom clustering")
         main(iso_ids, iso_labels, copy.deepcopy(iso_data_cpu),
              init_size=test_conf[0], up_size=test_conf[1],
              num_up=test_conf[2])
