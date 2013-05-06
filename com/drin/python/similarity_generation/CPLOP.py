import os
import sys
import array
import random

import pymysql

REGION_23_5 = 0
REGION_16_23 = 1

REGION_23_5_LEN = 93
REGION_16_23_LEN = 95
ISOLATE_LEN = 188

ID_QUERY_OFF = 1
DATA_QUERY_OFF = 2

ID_QUERY = '''
   SELECT distinct test_isolate_id, p1.pyroID, p2.pyroID
   FROM test_isolates t
        JOIN test_pyroprints p1 on (
           t.name_prefix = p1.name_prefix AND
           t.name_suffix = p1.name_suffix AND
           p1.appliedRegion = '23-5'
        )
        JOIN test_pyroprints p2 on (
           t.name_prefix = p2.name_prefix AND
           t.name_suffix = p2.name_suffix AND
           p2.appliedRegion = '16-23'
        )
   ORDER BY RAND(%d)
   LIMIT %d
'''

DATA_QUERY = '''
   SELECT p1.name_prefix, p1.name_suffix, h1.pHeight, h2.pHeight
   FROM test_histograms h1
        JOIN test_pyroprints p1 ON (
           h1.pyroID = p1.pyroID AND
           h1.position < 95 AND
           p1.pyroID IN (%s)
        )
        JOIN test_histograms h2 USING (position)
        JOIN test_pyroprints p2 on (
           p1.name_prefix = p2.name_prefix AND
           p1.name_suffix = p2.name_suffix AND
           h2.pyroID = p2.pyroID AND
           p2.pyroID IN (%s)
        )
'''

class connection(object):
   CPLOP_CONNECTION = None

   def __init__(self, host='localhost', port=8906, db='CPLOP'):
      if (self.CPLOP_CONNECTION is None):
         self.CPLOP_CONNECTION = pymysql.connect(host=host, port=port, db=db,
                                                 user='drin', passwd='')

   def get_isolate_ids(self, db_seed=random.randrange(9999), limit=110000):
      pyro_id_list_1 = array.array('L', (0,) * limit)
      pyro_id_list_2 = array.array('L', (0,) * limit)

      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      cplop_cursor.execute(ID_QUERY % (db_seed, limit))

      data_ndx = 0
      for iso_tuple in cplop_cursor.fetchall():
         # attribute index is shifted by one because of test_isolate_id
         pyro_id_list_1[data_ndx] = iso_tuple[REGION_23_5 + ID_QUERY_OFF]
         pyro_id_list_2[data_ndx] = iso_tuple[REGION_16_23 + ID_QUERY_OFF]

         data_ndx += 1

      cplop_cursor.close()
      return (pyro_id_list_1, pyro_id_list_2) 

   def get_isolate_data(self, data=None, limit=110000):
      if (data is None): return

      isolate_data = dict()
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      cplop_cursor.execute(DATA_QUERY % (
         ','.join([str(val) for val in data[REGION_23_5]]),
         ','.join([str(val) for val in data[REGION_16_23]])
      ))

      (isolate_id, peak_ndx) = (None, 0)
      for data_tuple in cplop_cursor.fetchall():
         tmp_isolate_id = "%s%s" % (data_tuple[0], str(data_tuple[1]))

         if (tmp_isolate_id != isolate_id):
            (isolate_id, peak_ndx) = (tmp_isolate_id, 0)
            isolate_data[isolate_id] = array.array('f', (0,) * ISOLATE_LEN)

         isolate_arr = isolate_data[isolate_id]

         if (peak_ndx < REGION_23_5_LEN):
            isolate_arr[peak_ndx] = data_tuple[REGION_23_5 + DATA_QUERY_OFF]

         if (REGION_23_5_LEN + peak_ndx < ISOLATE_LEN):
            isolate_arr[REGION_23_5_LEN + peak_ndx] = data_tuple[REGION_16_23 + DATA_QUERY_OFF]

         peak_ndx += 1

      cplop_cursor.close()
      return isolate_data
