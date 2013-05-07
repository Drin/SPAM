import os
import sys
import time
import math

import numpy
import CPLOP

try:
   import pycuda.driver
   import pycuda.compiler
   import pycuda.gpuarray

except:
   print("Could not load pyCUDA")
   sys.exit(1)

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

   return pycuda.compiler.SourceModule(cuda_src)

#############################################################################
#
# Retrieve CPLOP Data
#
#############################################################################
def get_data(initial_size=10000, update_size=1000, num_updates=1):
   conn = CPLOP.connection()

   dataset_size = initial_size + (update_size * num_updates)
   data_ids = conn.get_pyro_ids(db_seed=3, data_size=dataset_size)

   start_time = time.time()

   (iso_ids, iso_data) = conn.get_isolate_data(
      pyro_ids=data_ids, data_size=(dataset_size)
   )

   if ('DEBUG' in os.environ):
      print("%d[%d] isolates in %ds" % (len(iso_ids), len(iso_data),
                                        time.time() - start_time))

   return (iso_ids, iso_data)

#############################################################################
#
# CUDA Workload
#
#############################################################################
def compute_similarity(num_isolates, isolate_data, num_threads=16, num_blocks=32,
                       kernel_name='pearson', device_id=0):
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
   pycuda.driver.init()
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

   return sim_matrix_cpu

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
   (isolate_ids, isolate_data) = get_data(initial_size=100, update_size=100)
   sim_matrix = compute_similarity(len(isolate_ids), isolate_data)

   if ('DEBUG' in os.environ):
      for tmp_ndx in range(10):
         print(sim_matrix[tmp_ndx])

      if ('VERBOSE' in os.environ): output_similarity_matrix(sim_matrix)

if (__name__ == '__main__'):
   main()
