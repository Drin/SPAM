import os
import sys
import time
import math

import numpy
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
def get_data(initial_size=10000, update_size=1000, num_updates=1):
   conn = CPLOP.connection()

   dataset_size = initial_size + (update_size * num_updates)

   start_t = time.time()
   data_ids = conn.get_pyro_ids(db_seed=3, data_size=dataset_size)

   checkpoint_t = time.time()
   (iso_ids, iso_data) = conn.get_isolate_data(
      pyro_ids=data_ids, data_size=(dataset_size)
   )


   finish_t = time.time()

   if ('DEBUG' in os.environ):
      print("%d[%d] isolates in (%ds, %ds, %ds)" % (
         len(iso_ids), len(iso_data), checkpoint_t - start_t,
         finish_t - checkpoint_t, finish_t - start_t
      ))

   return (iso_ids, iso_data)

def output_similarity_matrix(sim_matrix):
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
def main():
   (iso_ids, iso_data_cpu) = get_data(initial_size=10000, update_size=1000)

   (iso_sim_mapping, sim_matrix_ndx) = (dict(), 0)
   for ndx_A in range(len(iso_ids)):
      for ndx_B in range(ndx_A + 1, len(iso_ids)):
         if (iso_sim_mapping.get(ndx_A) is None):
            iso_sim_mapping[ndx_A] = dict()
         iso_sim_mapping[ndx_A][ndx_B] = sim_matrix_ndx
         sim_matrix_ndx += 1

   num_isolates = len(iso_ids)
   if (num_isolates != len(iso_data_cpu) / 188):
      print("invalid iso_id length")

   '''
   sim_matrix_size = num_isolates * (num_isolates - 1) / 2
   sim_matrix_cpu = numpy.zeros(shape=(sim_matrix_size),
                                dtype=numpy.float32, order='C')
   '''

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
   #(gpu_device, sim_matrix_gpu) = compute_similarity(len(iso_ids), iso_data)
   #(gpu_device, iso_data_gpu) = prep_gpu_data(iso_data_cpu, device_id=0)

   '''
   clust_comparator = lambda clust_A, clust_B: cluster_comparator(
      num_isolates, iso_data_gpu, clust_A, clust_B, device_id=gpu_device
   )
   '''

   Clusterer.Cluster.sSim_matrix = (iso_sim_mapping, sim_matrix_cpu)
   #Clusterer.Cluster.sClust_comparator = clust_comparator

   clust_ontology = Ontology.OntologyParser().parse_ontology('specific.ont')
   clusters = [Clusterer.Cluster(val) for val in range(num_isolates)]

   for cluster in clusters:
      clust_ontology.add(cluster)

   OHClusterer = Clusterer.Clusterer(0.80, ontology=clust_ontology)

   OHClusterer.cluster_data(clusters)

   for cluster in clusters:
      print("cluster:")
      for element in cluster.elements:
         print("\t%s" % (iso_ids[element]))

   if ('DEBUG' in os.environ):
      for tmp_ndx in range(10):
         print(sim_matrix_cpu[tmp_ndx])

      if ('VERBOSE' in os.environ): output_similarity_matrix(sim_matrix)

if (__name__ == '__main__'):
   main()
