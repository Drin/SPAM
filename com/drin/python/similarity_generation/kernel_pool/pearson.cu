#include <stdint.h>
#include <string.h>

#define ISOLATE_LEN 188
#define LEN_16S 95
#define LEN_23S 93

__global__ void pearson(uint32_t num_isolates, uint32_t tile_size,
                        uint32_t tile_row, uint32_t tile_col,
                        float *isolates, float *sim_matrix) {
   uint32_t iso_B_tile_ndx = blockIdx.x * blockDim.x + threadIdx.x; // column
   uint32_t iso_A_tile_ndx = blockIdx.y * blockDim.y + threadIdx.y; // row

   uint32_t iso_B_ndx = tile_row * tile_size + iso_B_tile_ndx;
   uint32_t iso_A_ndx = tile_col * tile_size + iso_A_tile_ndx;

   // We don't want to compare isolates with themselves, or any comparisons
   // of a lower-numberes isolate to a higher-numbered one. Each pair of 
   // isolates (order doesn't matter) will only be compared once. This will
   // cause divergence only in the warps that lie along the main diagonal
   // of the comparison matrix.
   if (iso_A_ndx >= iso_B_ndx ||
       iso_A_ndx >= num_isolates ||
       iso_B_ndx >= num_isolates) { return; }

   // Initialize accumulators and the result.
   float pearson_sum = 0.0f;
   float peak_height_A = 0.0f, peak_height_B = 0.0f;
   float sum_A = 0.0f, sum_B = 0.0f, sum_AB = 0.0f,
         sum_A_squared = 0.0f, sum_B_squared = 0.0f;

   // Compute the sums for the 23-5 region (first 93).
   for (uint8_t ndx = 0; ndx < LEN_23S; ndx++) {
      peak_height_A = isolates[iso_A_ndx * ISOLATE_LEN + ndx];
      peak_height_B = isolates[iso_B_ndx * ISOLATE_LEN + ndx];

      sum_A += peak_height_A;
      sum_B += peak_height_B;
      sum_A_squared += peak_height_A * peak_height_A;
      sum_B_squared += peak_height_B * peak_height_B;
      sum_AB += peak_height_A * peak_height_B;
   }

   pearson_sum = (LEN_23S * sum_AB - sum_A * sum_B) /
                  sqrtf((LEN_23S * sum_A_squared - sum_A * sum_A) * 
                        (LEN_23S * sum_B_squared - sum_B * sum_B));

   peak_height_A = 0.0f, peak_height_B = 0.0f;
   sum_A = 0.0f, sum_B = 0.0f, sum_AB = 0.0f;
   sum_A_squared = 0.0f, sum_B_squared = 0.0f;

   // Compute the sums for the 16-23 region (last 95).
   for (uint8_t ndx = 0; ndx < LEN_16S; ndx++) {
      peak_height_A = isolates[iso_A_ndx * ISOLATE_LEN + LEN_23S + ndx];
      peak_height_B = isolates[iso_B_ndx * ISOLATE_LEN + LEN_23S + ndx];

      sum_A += peak_height_A;
      sum_B += peak_height_B;
      sum_A_squared += peak_height_A * peak_height_A;
      sum_B_squared += peak_height_B * peak_height_B;
      sum_AB += peak_height_A * peak_height_B;
   }

   pearson_sum += (LEN_16S * sum_AB - sum_A * sum_B) /
                   sqrtf((LEN_16S * sum_A_squared - sum_A * sum_A) * 
                         (LEN_16S * sum_B_squared - sum_B * sum_B));

   // Compute the index to store the result (a single dimensional array that is
   // a linear, packed representation of the upper half of the sim matrix)
   uint32_t result_ndx = iso_B_ndx - 1;
   for (uint32_t row_num = 1; row_num <= iso_A_ndx; row_num++) {
      result_ndx += (num_isolates - 1) - row_num;
   }

   if (result_ndx < (num_isolates * num_isolates - 1) / 2) {
      sim_matrix[result_ndx] = pearson_sum / 2;
   }
}
