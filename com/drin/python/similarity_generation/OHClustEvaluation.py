import os
import sys
import time

import numpy
import CPLOP

DEBUG_PEEK_SIZE = 10
REGION_23_5_LEN = 93
REGION_16_23_LEN = 95
ISOLATE_LEN = 188

CUDA_KERNEL_DIR = 'kernel_pool'

try:
   import pycuda.driver
   import pycuda.compiler
   import pycuda.gpuarray

except:
   print("Could not load pyCUDA")
   sys.exit(1)

def load_cuda():
   cuda_src = ''

   for file_name in os.listdir(CUDA_KERNEL_DIR):
      cuda_file = open(os.path.join(CUDA_KERNEL_DIR, file_name), 'r')
      cuda_src += cuda_file.read()

   return pycuda.compiler.SourceModule(cuda_src)

if (__name__ == '__main__'):
   #(initialSize, updateSize, numUpdates) = (10000, 1000, 100)
   (initialSize, updateSize, numUpdates) = (500, 500, 1)
   dataset_size = initialSize + (updateSize * numUpdates)

   #############################################################################
   #
   # GET DATA FROM CPLOP
   #
   #############################################################################

   conn = CPLOP.connection()
   isolate_ids = conn.get_isolate_ids(db_seed=3, limit=dataset_size)

   if ('DEBUG' in os.environ and 'VERBOSE' in os.environ):
      print("isolate_ids:")
      for isolate_ndx in range(min(DEBUG_PEEK_SIZE, len(isolate_ids))):
         print("\t%s" % (isolate_ids[isolate_ndx]))

   start_time = time.time()
   (isolate_ids, isolate_data) = conn.get_isolate_data(pyro_ids=isolate_ids, limit=initialSize)
   finish_time = time.time() - start_time

   sim_matrix_size = (len(isolate_data) * len(isolate_data) - 1) / 2

   if ('DEBUG' in os.environ and 'VERBOSE' in os.environ):
      print("isolate_data:")
      for iso_ndx in range(len(isolate_ids)):
         print("\tisolate [%s]: %s" % (isolate_ids[iso_ndx], isolate_data[iso_ndx]))

   if ('DEBUG' in os.environ):
      print("grabbed data for %d isolates in %d minutes" %
            (len(isolate_data), finish_time / 60))

   #############################################################################
   #
   # BEGIN CUDA
   #
   #############################################################################

   pycuda.driver.init()
   (num_isolates, num_threads, num_blocks) = (len(isolate_data), 16, 32)
   tile_size = (num_threads * num_blocks)

   cuda_context = pycuda.driver.Device(0).make_context()
   cuda_kernel = load_cuda().get_function('pearson')

   isolate_data_cpu = numpy.zeros(shape=(len(isolate_data), ISOLATE_LEN),
                                  dtype=numpy.float32, order='C')
   for isolate_ndx in range(len(isolate_data)):
      numpy.put(isolate_data_cpu[isolate_ndx], range(ISOLATE_LEN),
                isolate_data[isolate_ndx])

   if ('DEBUG' in os.environ):
      print("%s : %s" % (isolate_ids[0], isolate_data_cpu[0]))

   sim_matrix_cpu = numpy.zeros(shape=(1, sim_matrix_size),
                                dtype=numpy.float32, order='C')
   sim_matrix_gpu = pycuda.gpuarray.to_gpu(sim_matrix_cpu)

   for tile_row in range(num_isolates / (tile_size + 1)):
      for tile_col in range(tile_row, num_isolates / (tile_size + 1)):

         cuda_kernel(numpy.uint32(num_isolates), numpy.uint32(tile_size),
                     numpy.uint32(tile_row), numpy.uint32(tile_col),
                     pycuda.driver.In(isolate_data_cpu),
                     sim_matrix_gpu.gpudata,
                     block = (num_threads, num_threads, 1),
                     grid  = (num_blocks,  num_blocks))

   sim_matrix_gpu.get(sim_matrix_cpu)

   pycuda.driver.Context.pop()

   #############################################################################
   #
   # CHECK RESULTS
   #
   #############################################################################

   sim_ndx = 0
   for iso_A_ndx in range(num_isolates):
      iso_A = isolate_ids[iso_A_ndx]

      for iso_B_ndx in range(iso_A_ndx + 1, num_isolates):
         iso_B = isolate_ids[iso_B_ndx]

         print("isolate [%s] and isolate [%s]: %.04f" %
               (iso_A, iso_B, sim_matrix_cpu[sim_ndx]))

         sim_ndx += 1
