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
   ORDER BY p1.name_prefix, p1.name_suffix, h1.position
   LIMIT %d OFFSET %d
'''

META_QUERY = '''
   SELECT name_prefix, name_suffix %s
   FROM test_isolates join test_pyroprints using (name_prefix, name_suffix)
   WHERE name_prefix in (%s) AND name_suffix in (%s)
   ORDER BY name_prefix, name_suffix
   LIMIT %d OFFSET %d
'''

TEST_RUN_ID_QUERY = '''
   SELECT last_insert_id()
'''

INSERT_TEST_RUN = '''
   INSERT INTO test_runs(run_date, run_time, cluster_algorithm,
                         average_strain_similarity, use_transform)
   VALUES (NOW(), '%s', '%s', %.04f, %d)
'''

INSERT_RUN_PERF = '''
   INSERT INTO test_run_performance(test_run_id, update_id,
                                    update_size, run_time)
   VALUES (%d, %d, %d, %d)
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

   # data_size is in isolates
   def get_isolate_data(self, pyro_ids, data_size=2000, page_size=10000):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      data = numpy.zeros(shape=(data_size * ISOLATE_LEN),
                         dtype=numpy.float32, order='C')

      peak_data_size = (data_size * max(LEN_23_5, LEN_16_23))

      (ids, isolate_id, peak_ndx, isolate_ndx) = ([], None, 0, -1)
      for page_ndx in range(math.ceil(peak_data_size/page_size)):
         cplop_cursor.execute(DATA_QUERY % (
            ','.join([str(val) for val in pyro_ids[NDX_23_5]]),
            ','.join([str(val) for val in pyro_ids[NDX_16_23]]),
            min(page_size, peak_data_size - (page_ndx * page_size)),
            (page_ndx * page_size)
         ))

         for data_tuple in cplop_cursor.fetchall():
            tmp_isolate_id = (data_tuple[0], str(data_tuple[1]))

            if (isolate_id is None or
                (tmp_isolate_id[0] != isolate_id[0] and
                 tmp_isolate_id[1] != isolate_id[1])):
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

      print("num isolates: %d" % isolate_ndx)
      cplop_cursor.close()
      return (ids, data)

   def get_meta_data(self, ids, ont=None, data_size=2000, page_size=10000):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      (table_cols, id_prefixes, id_suffixes) = ([], [], [])

      for table_col in ont.table_columns.items():
         table_cols.append(str(table_col[1]))

      for val in ids:
         id_prefixes.append(str(val[0]))
         id_suffixes.append(str(va[1]))

      (iso_labels, isolate_id, isolate_ndx) = ([], None, -1)
      for page_ndx in range(math.ceil(peak_data_size/page_size)):
         cplop_cursor.execute(DATA_QUERY % (
            ', %s' % (', '.join(table_cols)),
            ','.join(id_prefixes),
            ','.join(id_suffixes),
            min(page_size, peak_data_size - (page_ndx * page_size)),
            (page_ndx * page_size)
         ))

         for data_tuple in cplop_cursor.fetchall():
            tmp_isolate_id = (data_tuple[0], str(data_tuple[1]))

            if (isolate_id is None or
                (tmp_isolate_id[0] != isolate_id[0] and
                 tmp_isolate_id[1] != isolate_id[1])):
               isolate_id = tmp_isolate_id
               isolate_ndx += 1
               if (ids[isolate_ndx][0] != isolate_id[0] and
                   ids[isolate_ndx][1] != isolate_id[1]):
                  print("meta data mismatch for isolate %s-%s" % (isolate_id))

            iso_labels.append([attr for attr in range(2, len(data_tuple))])

      cplop_cursor.close()
      return iso_labels

   def get_run_id():
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      cplop_cursor.execute(TEST_RUN_ID_QUERY)

      (test_run_id, num_tuples) = (-1, 0)
      for data_tuple in cplop_cursor.fetchall():
         num_tuples += 1
         test_run_id = data_tuple[0]

      if (num_tuples > 1):
         print("got multiple tuples when querying test_run_id?")

      cplop_cursor.close()
      return test_run_id

   def insert_new_run(average_similarity, run_time,
                      cluster_algorithm='OHClust!', use_transform=0):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      run_time_str = '%02d:%02d:%02d' % (
         run_time / 3600000, (run_time % 3600000) / 60000,
         ((run_time % 3600000) % 60000) / 1000
      )

      cplop_cursor.execute(INSERT_TEST_RUN % (
         run_time_str, cluster_algorithm, average_similarity, use_transform
      ))

      self.CPLOP_CONNECTION.commit()
      cplop_cursor.close()

   def insert_run_perf(test_run_id, up_num, up_size, run_time):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      cplop_cursor.execute(INSERT_TEST_RUN % (
         test_run_id, up_num, up_size, run_time
      ))

      self.CPLOP_CONNECTION.commit()
      cplop_cursor.close()
