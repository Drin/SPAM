import os
import sys

USING_PY3 = True
if (sys.version.find('3.3') == -1):
   import MySQLdb
   USING_PY3 = False
else:
   import pymysql

class Ontology(object):
   def __init__(self):
      self.root = None
      self.table_columns = dict()

   def add_term(self, new_term):
      if (self.root is not None):
         for child in self.root.children.items():
            child[1].add_term(new_term)
      else:
         self.root = new_term

   def add_data(self, cluster):
      if (self.root is not None):
         self.root.add_data(cluster)
         return True
      return False

class OntologyTerm(object):
   def __init__(self, data=None, label=None):
      self.label = label
      self.children = dict()
      self.data = []

      if (data is not None): self.data.append(data)
      self.new_data = (data is not None)

   def add_data(self, cluster):
      for label in cluster.labels:
         if (self.children.get(label) is not None):
            self.children[label].add_data(cluster)
            break
      else:
         self.data.append(cluster)
         self.new_data = True
