import os
import sys
import math
import random

import numpy

USING_PY3 = True
if (sys.version.find('3.3') == -1):
   import MySQLdb
   USING_PY3 = False
else:
   import pymysql

NDX_23_5 = 0
NDX_16_23 = 1

LEN_23_5 = 93
LEN_16_23 = 95
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
   LIMIT %d OFFSET %d
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
   LIMIT %d OFFSET %d
'''

class connection(object):
   CPLOP_CONNECTION = None

   def __init__(self, host='localhost', port=8906, db='CPLOP'):
      if (self.CPLOP_CONNECTION is None):
         if (USING_PY3):
            self.CPLOP_CONNECTION = pymysql.connect(host=host, port=port, db=db,
                                                    user='drin', passwd='')
         else:
            self.CPLOP_CONNECTION = MySQLdb.connect(host=host, port=port, db=db,
                                                    user='drin', passwd='')

   def get_pyro_ids(self, db_seed=random.randrange(9999),
                       data_size=2000, page_size=2000):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      pyros_1 = numpy.zeros(shape=(data_size), dtype=numpy.uint32, order='C')
      pyros_2 = numpy.zeros(shape=(data_size), dtype=numpy.uint32, order='C')

      for page_ndx in range(math.ceil(data_size/page_size)):
         cplop_cursor.execute(ID_QUERY % (
               db_seed,
               min(page_size, data_size - (page_ndx * page_size)),
               page_ndx * page_size
         ))

         data_ndx = 0
         for iso_tuple in cplop_cursor.fetchall():
            # attribute index is shifted by one because of test_isolate_id
            pyros_1[data_ndx] = iso_tuple[NDX_23_5 + ID_QUERY_OFF]
            pyros_2[data_ndx] = iso_tuple[NDX_16_23 + ID_QUERY_OFF]

            data_ndx += 1

      cplop_cursor.close()
      return (pyros_1, pyros_2) 

   def get_isolate_data(self, pyro_ids, data_size=2000, page_size=2000):
      (cplop_cursor, ids, isolate_ndx) = (self.CPLOP_CONNECTION.cursor(), [], 0)
      data = numpy.zeros(shape=(data_size * ISOLATE_LEN),
                         dtype=numpy.float32, order='C')

      for page_ndx in range(math.ceil(data_size/page_size)):
         cplop_cursor.execute(DATA_QUERY % (
            ','.join([str(val) for val in pyro_ids[NDX_23_5]]),
            ','.join([str(val) for val in pyro_ids[NDX_16_23]]),
            min(page_size, data_size - (page_ndx * page_size)),
            (page_ndx * page_size)
         ))

         (isolate_id, peak_ndx) = (None, 0)
         for data_tuple in cplop_cursor.fetchall():
            tmp_isolate_id = "%s%s" % (data_tuple[0], str(data_tuple[1]))

            if (tmp_isolate_id != isolate_id):
               (isolate_id, peak_ndx) = (tmp_isolate_id, 0)
               isolate_ndx += 1
               ids.append(isolate_id)

            if (peak_ndx < LEN_23_5):
               offset_23_5 = isolate_ndx * ISOLATE_LEN + peak_ndx
               data[offset_23_5] = data_tuple[NDX_23_5 + DATA_QUERY_OFF]

            if (LEN_23_5 + peak_ndx < ISOLATE_LEN):
               offset_16_23 = isolate_ndx * ISOLATE_LEN + LEN_23_5 + peak_ndx
               data[offset_16_23] = data_tuple[NDX_16_23 + DATA_QUERY_OFF]

            peak_ndx += 1

      cplop_cursor.close()
      return (ids, data)
