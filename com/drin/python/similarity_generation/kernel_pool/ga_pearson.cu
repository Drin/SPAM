#include <stdint.h>
#include <string.h>

#define 16S_LEN 95
#define 23S_LEN 93

__global__ void pearson(uint32_t num_isolates, float **isolates,
                        uint32_t isolate_A, uint32_t isolate_B) {
   // Calculate relative <i, j> coords within this tile.
   uint32_t i = blockIdx.x * blockDim.x + threadIdx.x; // column
   uint32_t j = blockIdx.y * blockDim.y + threadIdx.y; // row

   // Calculate the absolute <i, j> coords within the matrix.
   uint32_t i_abs = tile_col * tile_size + i;
   uint32_t j_abs = tile_row * tile_size + j;

   // We don't want to compare isolates with themselves, or any comparisons
   // of a lower-numberes isolate to a higher-numbered one. Each pair of 
   // isolates (order doesn't matter) will only be compared once. This will
   // cause divergence only in the warps that lie along the main diagonal
   // of the comparison matrix.
   if (i_abs <= j_abs)
      return;

   // Generate isolate |i_abs| and |j_abs|
   uint8_t i_allele_indices[kMaxAllelesPerIsolate]; 
   uint8_t j_allele_indices[kMaxAllelesPerIsolate]; 

   // Initialize accumulators and the result.
   float sum_x = 0, sum_y = 0, sum_x2 = 0, sum_y2 = 0, sum_xy = 0;

   // Compute the sums.
   for (int index = 0; index < length_alleles; ++index) {
      uint8_t x = 0, y = 0;

      x += alleles[i_allele_indices[alleleNdx] * length_alleles + index];
      y += alleles[j_allele_indices[alleleNdx] * length_alleles + index];

      sum_x += x;
      sum_y += y;
      sum_x2 += x * x;
      sum_y2 += y * y;
      sum_xy += x * y;
   }

   // Compute the Pearson coefficient using the "sometimes numerically
   // unstable" method because it's way more computationally efficient.
   float coeff = (length_alleles * sum_xy - sum_x * sum_y) /
      sqrtf((length_alleles * sum_x2 - sum_x * sum_x) * 
            (length_alleles * sum_y2 - sum_y * sum_y));

}
