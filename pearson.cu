#include <stdint.h>
#include <string.h>

#define ISOLATE_LEN 188
#define NUM_REGIONS 2
#define LEN_23S 93
#define LEN_16S 95

extern "C" {

__global__ void pearson(uint32_t tileRowSize, float *isoDataRow,
                        uint32_t tileColSize, float *isoDataCol,
                        uint32_t tileRow, uint32_t tileCol,
                        uint32_t numIsolates, uint32_t simMatrixSize,
                        float *simMatrix) {
   //uint32_t iso_A_ndx = blockIdx.y * blockDim.y + threadIdx.y; // row
   //uint32_t iso_B_ndx = blockIdx.x * blockDim.x + threadIdx.x; // column

   // We don't want to compare isolates with themselves, or any comparisons
   // of a lower-numberes isolate to a higher-numbered one. Each pair of 
   // isolates (order doesn't matter) will only be compared once. This will
   // cause divergence only in the warps that lie along the main diagonal
   // of the comparison matrix.
   /*
   if ((tileRowSize * tileRow) + iso_A_ndx > 0 ||
       (tileColSize * tileCol) + iso_B_ndx > 0) { return;}
   if ((tileRowSize * tileRow) + iso_A_ndx >=
       (tileColSize * tileCol) + iso_B_ndx ||
       iso_A_ndx >= tileRowSize ||
       iso_B_ndx >= tileColSize) { return; }
       */

   /*
   // Initialize accumulators and the result.
   float pearson_sum = 0.0f;
   float peak_height_A = 0.0f, peak_height_B = 0.0f;
   float sum_A = 0.0f, sum_B = 0.0f, sum_AB = 0.0f,
         sum_A_squared = 0.0f, sum_B_squared = 0.0f;

   // Compute the sums for the 23-5 region (first 93).
   for (uint8_t ndx = 0; ndx < LEN_23S; ndx++) {
      peak_height_A = isoDataRow[iso_A_ndx * ISOLATE_LEN + ndx];
      peak_height_B = isoDataCol[iso_B_ndx * ISOLATE_LEN + ndx];

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
      peak_height_A = isoDataRow[iso_A_ndx * ISOLATE_LEN + LEN_23S + ndx];
      peak_height_B = isoDataCol[iso_B_ndx * ISOLATE_LEN + LEN_23S + ndx];

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
   uint32_t result_ndx = (tileRowSize * tileRow) + iso_B_ndx - 1;
   for (uint32_t row_num = 1; row_num <= iso_A_ndx; row_num++) {
      result_ndx += (numIsolates - 1) - row_num;
   }

   if (result_ndx < simMatrixSize) {
      simMatrix[result_ndx] = pearson_sum / 2;
   }
   */

   printf("%d", 78);
   for (int i = 0; i < 10; i++) {
      simMatrix[i] = 99;
   }
   __syncthreads();
}

}
