import os
import CPLOP

DEBUG_PEEK_SIZE = 10

def debug(db_tuples):
   for tuple_ndx in range(min(DEBUG_PEEK_SIZE, len(db_tuples))):
      print("\t%s" % (db_tuples[tuple_ndx]))

if (__name__ == '__main__'):
   #(initialSize, updateSize, numUpdates) = (10000, 1000, 100)
   (initialSize, updateSize, numUpdates) = (1000, 1000, 1)
   dataset_size = initialSize + (updateSize * numUpdates)

   conn = CPLOP.connection()
   isolate_ids = conn.get_isolate_ids(db_seed=3, limit=dataset_size)

   if ('DEBUG' in os.environ):
      print("isolate_ids:")
      debug(isolate_ids)

   isolate_data = conn.get_isolate_data(data=isolate_ids, limit=initialSize)

   if ('DEBUG' in os.environ):
      print("isolate_data:")
      debug(isolate_data)
