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
DATA_QUERY_OFF = 1

DEFAULT_PAGE_SIZE = 2000
DEFAULT_DATA_SIZE = 2000

SCHEMA_QUERY = '''
   SELECT distinct(%s)
   FROM %s
   WHERE %s IS NOT NULL
'''

ID_QUERY = '''
   SELECT *
   FROM (SELECT test_isolate_id, pyro_id_1, pyro_id_2
         FROM isolate_selection
         %s
         LIMIT %d OFFSET %d
        ) ids
   ORDER BY test_isolate_id
'''

DATA_QUERY = '''
   SELECT i.test_isolate_id, h1.pHeight, h2.pHeight, h1.position
   FROM test_histograms h1
        JOIN isolate_selection i ON (
           h1.pyroID = i.pyro_id_1 AND
           h1.position < 95
        )
        JOIN test_histograms h2 ON (
           h1.position = h2.position AND
           h2.pyroID = i.pyro_id_2
        )
   ORDER BY i.test_isolate_id
   LIMIT %d OFFSET %d
'''

META_QUERY = '''
   SELECT test_isolate_id %s
   FROM test_isolates join test_pyroprints using (name_prefix, name_suffix)
   WHERE test_isolate_id in (%s)
   ORDER BY test_isolate_id
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
   VALUES %s
'''

INSERT_STRAIN = '''
   INSERT INTO test_run_strain_link (
      test_run_id, cluster_id, cluster_threshold, strain_diameter,
      average_isolate_similarity, percent_similar_isolates
   )
   VALUES %s
'''

INSERT_ISOLATES = '''
   INSERT INTO test_isolate_strains (
      test_run_id, cluster_id, cluster_threshold, test_isolate_id
   )
   VALUES %s
'''

class connection(object):
   CPLOP_CONNECTION = None

   def __init__(self, host='localhost', port=3306, db='CPLOP'):
      if (self.CPLOP_CONNECTION is None):
         if (USING_PY3):
            self.CPLOP_CONNECTION = pymysql.connect(host=host, port=port, db=db,
                                                    user='', passwd='')
         else:
            self.CPLOP_CONNECTION = MySQLdb.connect(host=host, port=port, db=db,
                                                    user='', passwd='')

   def get_distinct_values(self, table_name, col_name):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      ont_partitions = []

      cplop_cursor.execute(SCHEMA_QUERY % (col_name, table_name, col_name))

      for data_tuple in cplop_cursor.fetchall():
         ont_partitions.append(data_tuple[0].strip())

      cplop_cursor.close()
      return ont_partitions

   def get_pyro_ids(self, ont=None, db_seed=random.randrange(9999),
                       data_size=DEFAULT_DATA_SIZE, page_size=DEFAULT_PAGE_SIZE):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      pyros_1 = numpy.zeros(shape=(data_size), dtype=numpy.uint32, order='C')
      pyros_2 = numpy.zeros(shape=(data_size), dtype=numpy.uint32, order='C')

      where_clause = ''
      if (ont is not None):
         clauses = []
         for part_col in ont.column_parts.items():
            clauses.append('%s in (%s)' % (
               part_col[0], ', '.join(["'%s'" % part for part in part_col[1]])
            ))
         where_clause = 'where ' + (' and '.join(clauses))

      cplop_cursor.execute("CALL prepMigration(%d)" % data_size)

      '''
      for page_ndx in range(math.ceil(data_size/page_size)):
         cplop_cursor.execute(ID_QUERY % (
            where_clause,
            db_seed,
            min(page_size, data_size - (page_ndx * page_size) - 1),
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
      '''

   # data_size is in isolates
   def get_isolate_data(self, pyro_ids=None, data_size=DEFAULT_DATA_SIZE,
                        page_size=DEFAULT_PAGE_SIZE):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      data = numpy.zeros(shape=(data_size * ISOLATE_LEN),
                         dtype=numpy.float32, order='C')

      peak_data_size = (data_size * max(LEN_23_5, LEN_16_23))

      ids = numpy.zeros(shape=(data_size), dtype=numpy.uint32, order='C')
      (isolate_id, peak_ndx, isolate_ndx) = (None, 0, -1)
      for page_ndx in range(math.ceil(peak_data_size/page_size)):
         cplop_cursor.execute(DATA_QUERY % (
            #','.join([str(val) for val in pyro_ids[NDX_23_5]]),
            #','.join([str(val) for val in pyro_ids[NDX_16_23]]),
            min(page_size, peak_data_size - (page_ndx * page_size)),
            (page_ndx * page_size)
         ))

         for data_tuple in cplop_cursor.fetchall():
            tmp_isolate_id = data_tuple[0]

            if (isolate_id is None or tmp_isolate_id != isolate_id):
               isolate_ndx += 1
               (isolate_id, peak_ndx) = (tmp_isolate_id, 0)
               if (isolate_ndx < len(ids)):
                  ids[isolate_ndx] = isolate_id

            if (peak_ndx != data_tuple[3]):
               print("peak mismatch! on %d, should be %d" % (data_tuple[3],
                     peak_ndx))

            if (peak_ndx < LEN_23_5):
               offset_23_5 = isolate_ndx * ISOLATE_LEN + peak_ndx
               if (offset_23_5 < len(data)):
                  data[offset_23_5] = data_tuple[NDX_23_5 + DATA_QUERY_OFF]

            if (LEN_23_5 + peak_ndx < ISOLATE_LEN):
               offset_16_23 = isolate_ndx * ISOLATE_LEN + LEN_23_5 + peak_ndx
               if (offset_16_23 < len(data)):
                  data[offset_16_23] = data_tuple[NDX_16_23 + DATA_QUERY_OFF]

            peak_ndx += 1

      cplop_cursor.close()
      return (ids, data)

   def get_fast_isolate_data(self, pyro_ids=None, data_size=DEFAULT_DATA_SIZE,
                             page_size=DEFAULT_PAGE_SIZE):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      peak_data_size = (data_size * max(LEN_23_5, LEN_16_23))
      ids = numpy.zeros(shape=(data_size), dtype=numpy.uint32, order='C')
      data = [numpy.zeros(shape=(ISOLATE_LEN), dtype=numpy.float32, order='C')
              for i in range(data_size)]

      (isolate_id, peak_ndx, isolate_ndx) = (None, 0, -1)
      for page_ndx in range(math.ceil(peak_data_size/page_size)):
         cplop_cursor.execute(DATA_QUERY % (
            min(page_size, peak_data_size - (page_ndx * page_size)),
            (page_ndx * page_size)
         ))

         for data_tuple in cplop_cursor.fetchall():
            tmp_isolate_id = data_tuple[0]

            if (isolate_id is None or tmp_isolate_id != isolate_id):
               isolate_ndx += 1
               (isolate_id, peak_ndx) = (tmp_isolate_id, 0)
               if (isolate_ndx < len(ids)):
                  ids[isolate_ndx] = isolate_id

            if (peak_ndx != data_tuple[3]):
               print("peak mismatch! on %d, should be %d" % (data_tuple[3],
                     peak_ndx))

            if (peak_ndx < LEN_23_5):
               data[isolate_ndx][peak_ndx] = data_tuple[NDX_23_5 + DATA_QUERY_OFF]

            if (peak_ndx < LEN_16_23 and
                LEN_23_5 + peak_ndx < len(data[isolate_ndx]):
               data[isolate_ndx][LEN_23_5 + peak_ndx] = data_tuple[NDX_16_23 + DATA_QUERY_OFF]

            peak_ndx += 1

      cplop_cursor.close()
      return (ids, data)

   def get_meta_data(self, ids, ont=None, data_size=DEFAULT_DATA_SIZE,
                     page_size=DEFAULT_PAGE_SIZE):
      (cplop_cursor, table_cols) = (self.CPLOP_CONNECTION.cursor(), [])

      for table_col in ont.table_columns.items():
         table_cols.append(', '.join(table_col[1]))

      (iso_labels, isolate_id, isolate_ndx) = ([], None, -1)
      for page_ndx in range(math.ceil(data_size/page_size)):
         cplop_cursor.execute(META_QUERY % (
            ', %s' % (', '.join(table_cols)),
            ', '.join([str(pyro_id) for pyro_id in ids]),
            min(page_size, data_size - (page_ndx * page_size)),
            (page_ndx * page_size)
         ))

         for data_tuple in cplop_cursor.fetchall():
            tmp_isolate_id = data_tuple[0]

            if (isolate_id is None or tmp_isolate_id != isolate_id):
               isolate_id = tmp_isolate_id
               isolate_ndx += 1
               if (ids[isolate_ndx] != isolate_id):
                  print("meta data mismatch for isolate %d" % isolate_id)
                  continue

            iso_labels.append([data_tuple[attr].strip() for attr in range(1, len(data_tuple))])

      cplop_cursor.close()
      return iso_labels

   def get_run_id(self):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      cplop_cursor.execute(TEST_RUN_ID_QUERY)

      (test_run_id, num_tuples) = (-1, 0)
      for data_tuple in cplop_cursor.fetchall():
         num_tuples += 1
         test_run_id = data_tuple[0]

      cplop_cursor.close()
      return test_run_id

   def insert_new_run(self, average_similarity, run_time,
                      cluster_algorithm='OHClust!', use_transform=0):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      run_time_str = '%02d:%02d:%02d' % (
         run_time / 3600, (run_time % 3600) / 60,
         ((run_time % 3600) % 60)
      )

      cplop_cursor.execute(INSERT_TEST_RUN % (
         run_time_str, cluster_algorithm, average_similarity, use_transform
      ))

      self.CPLOP_CONNECTION.commit()
      cplop_cursor.close()

   def insert_run_perf(self, test_run_id, perf_info):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      run_perf_values = ["(%d, %d, %d, %d)" % (test_run_id, perf[0], perf[1], perf[2])
                         for perf in perf_info]

      cplop_cursor.execute(INSERT_RUN_PERF % (
         ', '.join(run_perf_values)
      ))

      self.CPLOP_CONNECTION.commit()
      cplop_cursor.close()

   def insert_clusters(self, test_run_id, ids, clusters, threshold,
                       page_size=DEFAULT_PAGE_SIZE):
      import Clusterer
      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      (clust_id, strain_inserts, isolate_inserts) = (0, [], [])

      for cluster in clusters:
         if (type(cluster) is not Clusterer.Cluster):
            continue

         strain_inserts.append("(%d, %d, %.04f, %.04f, %.04f, %.04f)" % (
            test_run_id, clust_id, threshold, cluster.diameter,
            cluster.average_similarity, float(0.0)
         ))

         for iso_ndx in cluster.elements:
            isolate_inserts.append("(%d, %d, %.04f, %d)" % (
               test_run_id, clust_id, threshold, ids[iso_ndx]
            ))

         clust_id += 1

         if (clust_id % page_size == 0):
            cplop_cursor.execute(INSERT_STRAIN % (', '.join(strain_inserts)))
            cplop_cursor.execute(INSERT_ISOLATES % (', '.join(isolate_inserts)))
            del strain_inserts[:]
            del isolate_inserts[:]
            print("inserting clusters")
            self.CPLOP_CONNECTION.commit()

      else:
         if (len(strain_inserts) > 0):
            cplop_cursor.execute(INSERT_STRAIN % (', '.join(strain_inserts)))
            del strain_inserts[:]

         if (len(isolate_inserts) > 0):
            cplop_cursor.execute(INSERT_ISOLATES % (', '.join(isolate_inserts)))
            del isolate_inserts[:]

         print("inserting clusters")
         self.CPLOP_CONNECTION.commit()

      cplop_cursor.close()
