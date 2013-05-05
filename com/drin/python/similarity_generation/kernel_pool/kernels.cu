#include <stdint.h>
#include <string.h>

__device__ void dump_bucket(uint64_t *buckets,
      uint32_t num_ranges, uint32_t tile_size,
      uint32_t src_i, uint32_t src_j,
      uint32_t dest_i, uint32_t dest_j) {
   // Element-wise sum for each in 0 -> num_ranges.
   for (uint32_t k = 0; k < num_ranges; k++) {
      uint32_t src_index = (tile_size * tile_size * k) +
         (tile_size * src_i) + src_j;
      uint32_t dest_index = (tile_size * tile_size * k) +
         (tile_size * dest_i) + dest_j;
      buckets[dest_index] += buckets[src_index];
   }
}

__global__ void reduction(uint64_t *buckets, uint32_t num_ranges,
      uint32_t tile_size, uint32_t chunk_size) {
   // Calculate <i, j> coords within the tile.
   uint32_t i = blockIdx.x; // row
   uint32_t j = threadIdx.x * chunk_size; // column

   // Each chunk leader reduces its chunk.
   for (uint32_t k = 1; k < chunk_size; k++) {
      dump_bucket(buckets, num_ranges, tile_size, i, j + k, i, j);
   }

   // Wait for all the threads in this row to finish.
   __syncthreads();

   // Reduce each chunk leader into the zeroth element of the row.
   if (j == 0) {
      for (uint32_t k = 1; k < blockDim.x; k++) {
         dump_bucket(buckets, num_ranges, tile_size, i, k * chunk_size, i, 0);
      }
   }
}
