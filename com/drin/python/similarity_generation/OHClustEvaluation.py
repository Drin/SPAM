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
# CUDA Workload
#
#############################################################################
def compute_similarity(num_isolates, isolate_data,
                       num_threads=NUM_THREADS, num_blocks=NUM_BLOCKS,
                       kernel_name='pearson', device_id=DEFAULT_DEVICE):
   # Convenience Variables
   tile_size = (num_threads * num_blocks)
   sim_matrix_size = num_isolates * (num_isolates - 1) / 2
   num_tiles = math.ceil(num_isolates / tile_size)

   if ('DEBUG' in os.environ):
      print("num isolates: %d" % num_isolates)
      print("num similarities: %d" % sim_matrix_size)

   ############################################################################
   #
   # Init CUDA
   #
   ############################################################################
   cuda_context = pycuda.driver.Device(device_id).make_context()
   cuda_kernel = load_cuda().get_function(kernel_name)

   ############################################################################
   #
   # Main Workload
   #
   ############################################################################

   sim_matrix_cpu = numpy.zeros(shape=(sim_matrix_size),
                                dtype=numpy.float32, order='C')
   sim_matrix_gpu = pycuda.gpuarray.to_gpu(sim_matrix_cpu)

   for tile_row in range(num_tiles):
      for tile_col in range(tile_row, num_tiles):
         cuda_kernel(numpy.uint32(num_isolates), numpy.uint32(tile_size),
                     numpy.uint32(tile_row), numpy.uint32(tile_col),
                     pycuda.driver.In(isolate_data),
                     sim_matrix_gpu.gpudata,
                     block = (num_threads, num_threads, 1),
                     grid  = (num_blocks,  num_blocks))
   sim_matrix_gpu.get(sim_matrix_cpu)

   ############################################################################
   #
   # Cleanup CUDA
   #
   ############################################################################
   pycuda.driver.Context.pop()

   #return (device_id, sim_matrix_gpu)
   return sim_matrix_cpu

#############################################################################
#
# Prepare Similarity Matrix and Isolate data on GPU
#
#############################################################################
def prep_gpu_data(iso_data_cpu, device_id=DEFAULT_DEVICE):
   # convenience variables
   cuda_context = pycuda.driver.Device(device_id).make_context()

   # gpu data
   iso_data_gpu = pycuda.gpuarray.to_gpu(iso_data_cpu)
   #sim_matrix_gpu = pycuda.gpuarray.to_gpu(sim_matrix_cpu)

   pycuda.driver.Context.pop()
   #return (device_id, iso_data_gpu, sim_matrix_gpu)
   return (device_id, iso_data_gpu)

#############################################################################
#
# Get Similarity matrix and Isolate data off of GPU. Not really suggested
#
#############################################################################
def get_gpu_data(iso_data_pair, sim_matrix_pair, device_id=DEFAULT_DEVICE):
   cuda_context = pycuda.driver.Device(device_id).make_context()

   iso_data_pair[1].get(iso_data_pair[0])
   sim_matrix_pair[1].get(sim_matrix_pair[0])

   pycuda.driver.Context.pop()
   return (iso_data_pair[0], sim_matrix_pair[0])


def fast_comparator(num_isolates, sim_matrix_gpu, clust_A, clust_B,
                    num_threads=NUM_THREADS, num_blocks=NUM_BLOCKS,
                    kernel_name='cached_pearson', device_id=DEFAULT_DEVICE):
   # Convenience Variables
   tile_size = (num_threads * num_blocks)
   num_tiles = math.ceil(num_isolates / tile_size)

   cuda_context = pycuda.driver.Device(device_id).make_context()
   cuda_kernel = load_cuda().get_function(kernel_name)

   ############################################################################
   #
   # Main Workload
   #
   ############################################################################

   clust_A_gpu = pycuda.gpuarray.to_gpu(clust_A.elements)
   clust_B_gpu = pycuda.gpuarray.to_gpu(clust_B.elements)

   cluster_sim = numpy.zeros(shape=(1), dtype=numpy.float32, order='C')
   cluster_sim_gpu = pycuda.gpuarray.to_gpu(cluster_sim)

   for tile_row in range(num_tiles):
      for tile_col in range(tile_row, num_tiles):
         cuda_kernel(numpy.uint32(num_isolates), iso_data_gpu.gpudata,
                     numpy.uint32(len(clust_A)), clust_A_gpu.gpudata,
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
# CUDA comparator for clusters
#
#############################################################################
def cluster_comparator(num_isolates, iso_data_gpu, clust_A, clust_B,
                       num_threads=NUM_THREADS, num_blocks=NUM_BLOCKS,
                       kernel_name='cluster_pearson', device_id=DEFAULT_DEVICE):
   # Convenience Variables
   tile_size = (num_threads * num_blocks)
   num_tiles = math.ceil(num_isolates / tile_size)

   #num_blocks = int(min(num_blocks, max(math.ceil(len(clust_A)/num_threads),
                                        #math.ceil(len(clust_B)/num_threads))))

   cuda_context = pycuda.driver.Device(device_id).make_context()
   cuda_kernel = load_cuda().get_function(kernel_name)

   ############################################################################
   #
   # Main Workload
   #
   ############################################################################

   clust_A_gpu = pycuda.gpuarray.to_gpu(clust_A.elements)
   clust_B_gpu = pycuda.gpuarray.to_gpu(clust_B.elements)

   cluster_sim = numpy.zeros(shape=(1), dtype=numpy.float32, order='C')
   cluster_sim_gpu = pycuda.gpuarray.to_gpu(cluster_sim)

   for tile_row in range(num_tiles):
      for tile_col in range(tile_row, num_tiles):
         cuda_kernel(numpy.uint32(num_isolates), iso_data_gpu.gpudata,
                     numpy.uint32(len(clust_A)), clust_A_gpu.gpudata,
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

   print("getting ids")
   start_t = time.time()
   #data_ids = conn.get_pyro_ids(ont=ontology, data_size=dataset_size)

   print("getting data")
   pyro_t = time.time()
   (iso_ids, iso_data) = conn.get_isolate_data(
      #pyro_ids=data_ids, data_size=(dataset_size)
      data_size=(dataset_size)
   )

   print("getting meta-data")
   meta_t = time.time()
   iso_labels = conn.get_meta_data(
      ont=ontology, ids=iso_ids, data_size=dataset_size
   )

   finish_t = time.time()

   out_file = open('database_access_times', 'a')
   out_file.write("%d[%d] isolates in (%ds, %ds, %ds, %ds)\n" % (
      len(iso_ids), len(iso_data), pyro_t - start_t,
      meta_t - pyro_t, finish_t - meta_t, finish_t - start_t
   ))
   out_file.close()

   return (iso_ids, iso_labels, iso_data)

def output_similarity_matrix(sim_matrix, num_isolates, isolate_ids):
   sim_ndx = 0
   for iso_A_ndx in range(num_isolates):
      iso_A = isolate_ids[iso_A_ndx]

      for iso_B_ndx in range(iso_A_ndx + 1, num_isolates):
         iso_B = isolate_ids[iso_B_ndx]

         print("(%s, %s): %.04f " % (iso_A, iso_B, sim_matrix_cpu[sim_ndx]))
         sim_ndx += 1

#############################################################################
#
# Main Function
#
#############################################################################
def main(iso_ids, iso_labels, iso_data_cpu, iso_sim_mapping, sim_matrix_ndx,
         clust_ontology=None, init_size=100, up_size=100, num_up=1):
   algorith_name = 'agglomerative'
   if (clust_ontology is not None):
      algorith_name = 'OHClust!'

   num_isolates = (init_size + (up_size * num_up))

   ############################################################################
   #
   # Clustering Section
   #
   ############################################################################

   threshold = 0.80
   Clusterer.Cluster._sim_matrix_ = sim_matrix_cpu
   Clusterer.Cluster._iso_mapping_ = iso_sim_mapping
   Clusterer.Cluster._threshold_ = threshold

   conn = CPLOP.connection()

   (start_ndx, end_ndx, curr_up) = (0, init_size, -1)
   (clusters, OHClusterer, perf_info, total_t) = (None, None, [], time.time())

   while (curr_up < num_up and end_ndx < num_isolates and start_ndx < end_ndx):
      clusters = []
      for iso_ndx in range(start_ndx, end_ndx):
         clusters.append(Clusterer.Cluster(data_point=iso_ndx,
                                           labels=iso_labels[iso_ndx]))

      OHClusterer = Clusterer.Clusterer(threshold, ontology=clust_ontology)

      cluster_t = time.time()
      OHClusterer.cluster_data(clusters)
      finish_t = time.time()

      perf_info.append((curr_up + 1, end_ndx, finish_t - cluster_t))

      #prepare update state for next iteration
      curr_up += 1
      start_ndx = end_ndx + 1
      end_ndx += min(up_size, num_isolates - end_ndx)

   print("%d clusters:" % len(clusters))
   if ('DEBUG' in os.environ):
      for cluster in clust_ontology.root.data:
         print("cluster [%d]:" % len(cluster))
         if (type(cluster) is not Clusterer.Cluster):
            print("non cluster: %s" % cluster)
         for element in cluster.elements:
            print("\t%s" % (iso_ids[element]))

      for tmp_ndx in range(10):
         print(sim_matrix_cpu[tmp_ndx])

      if ('VERBOSE' in os.environ):
         output_similarity_matrix(sim_matrix_cpu, len(iso_ids), iso_ids)

   conn.insert_new_run(clust_ontology.get_cluster_separation(),
                       time.time() - total_t, cluster_algorithm=algorith_name)

   test_run_id = conn.get_run_id()
   print("inserting data for run id %d" % test_run_id)
   conn.insert_run_perf(test_run_id, perf_info)
   conn.insert_clusters(test_run_id, iso_ids, clust_ontology.root.data, threshold)

if (__name__ == '__main__'):
   (clust_ontology, algorith_name) = (None, 'agglomerative')
   clust_ontology = Ontology.OntologyParser().parse_ontology('generic.ont')

   (iso_ids, iso_labels, iso_data_cpu) = get_data(
      #(2500 + (125 * 100)), ontology=clust_ontology
      (100 + (10 * 1)), ontology=clust_ontology
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

   cuda_start = time.time()
   sim_matrix_cpu = compute_similarity(len(iso_ids), iso_data_cpu)
   cuda_end = time.time()

   print("%ds to compute similarity matrix" % (cuda_end - cuda_start))

   ############################################################################
   #
   # Testing Section
   #
   ############################################################################

   '''
   configurations = ((100, 10, 100), (500, 50, 100),
                     (1000, 50, 100), (1000, 100, 100),
                     (2500, 125, 100))#, (2500, 250, 100))
   '''
   configurations = ((100, 10, 1),)

   for test_conf in configurations:
      for test_num in range(3):
         tmp_ontology = copy.deepcopy(clust_ontology)

         main(iso_ids, iso_labels, iso_data_cpu, iso_sim_mapping,
              sim_matrix_ndx, clust_ontology=tmp_ontology,
              init_size=test_conf[0], up_size=test_conf[1],
              num_up=test_conf[2])


   for test_conf_ndx in range(6):
      test_conf = configurations[test_conf_ndx]

      for test_num in range(3):
         main(iso_ids, iso_labels, iso_data_cpu, iso_sim_mapping,
              sim_matrix_ndx, init_size=test_conf[0], up_size=test_conf[1],
              num_up=test_conf[2])
