import os
import sys
import random
import pymysql

ID_QUERY = '''
   SELECT test_isolate_id FROM test_isolates ORDER BY RAND(%d) LIMIT %d
'''

DATA_QUERY = '''
   SELECT test_isolate_id, h1.position, region1, region2,
          h1.pHeight, h2.pHeight
   FROM (SELECT test_isolate_id, p1.pyroID as pyro1, p2.pyroID as pyro2,
                p1.appliedRegion as region1, p2.appliedRegion as region2
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
         WHERE test_isolate_id in (%s)
         GROUP BY t.name_prefix, t.name_suffix
        ) pyros
        JOIN test_histograms h1 on (h1.pyroID = pyro1)
        JOIN test_histograms h2 on (h2.pyroID = pyro2)
'''

class connection(object):
   CPLOP_CONNECTION = None

   def __init__(self, host='localhost', port=8906, db='CPLOP'):
      if (self.CPLOP_CONNECTION is None):
         self.CPLOP_CONNECTION = pymysql.connect(host=host, port=port, db=db,
                                                 user='drin', passwd='')

   def get_isolate_ids(self, db_seed=random.randrange(9999), limit=110000):
      cplop_cursor = self.CPLOP_CONNECTION.cursor()
      cplop_cursor.execute(ID_QUERY % (db_seed, limit))
      results = [str(iso_tuple[0]) for iso_tuple in cplop_cursor.fetchall()]
      cplop_cursor.close()

      return results

   def get_isolate_data(self, data=None, limit=110000):
      if (data is None): return

      cplop_cursor = self.CPLOP_CONNECTION.cursor()

      if ('DEBUG' in os.environ): print("querying isolate data...")
      cplop_cursor.execute(DATA_QUERY % str(', '.join(data)))
      if ('DEBUG' in os.environ): print("finished querying isolate data")

      results = cplop_cursor.fetchall()
      cplop_cursor.close()

      return results
