import os
import sys
import time

import CPLOP

DEBUG_PEEK_SIZE = 10

if (__name__ == '__main__'):
   (initialSize, updateSize, numUpdates) = (10000, 1000, 100)
   dataset_size = initialSize + (updateSize * numUpdates)

   conn = CPLOP.connection()
   isolate_ids = conn.get_isolate_ids(db_seed=3, limit=dataset_size)

   if ('DEBUG' in os.environ and 'VERBOSE' in os.environ):
      print("isolate_ids:")
      for isolate_ndx in range(min(DEBUG_PEEK_SIZE, len(isolate_ids))):
         print("\t%s" % (isolate_ids[isolate_ndx]))

   start_time = time.time()
   isolate_data = conn.get_isolate_data(data=isolate_ids, limit=initialSize)
   finish_time = time.time() - start_time

   if ('DEBUG' in os.environ and 'VERBOSE' in os.environ):
      print("isolate_data:")
      for isolate_item in isolate_data.items():
         print("\tisolate [%s]: %s" % (isolate_item[0], isolate_item[1]))

   if ('DEBUG' in os.environ):
      print("grabbed data for %d isolates in %d minutes" %
            (len(isolate_data.items()), finish_time / 60))
