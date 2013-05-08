import os
import sys
import re

import copy

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

   def add_term(self, table_name, new_term):
      if (self.root is not None):
         self.root.add_term(new_term)
      else:
         self.root = new_term

      self.table_columns[table_name] = new_term.col_name

   def add_data(self, cluster):
      if (self.root is not None):
         self.root.add_data(cluster)
         return True
      return False

   def __str__(self):
      return 'root:\n' + self.root.print_ontology('\t')

class OntologyTerm(object):
   def __init__(self, data=None, col_name=None, partitions=None, options=None):
      self.data = []
      self.children = dict()
      self.options = dict()
      self.col_name = col_name

      if (data is not None): self.data.append(data)
      self.new_data = (data is not None)

      for partition in partitions:
         self.children[partition.strip()] = None
      for option in options:
         self.options[option] = True

   def add_term(self, new_term):
      for child in self.children.items():
         if (child[1] is not None):
            child[1].add_term(new_term)
         else:
            self.children[child[0]] = new_term

   def add_data(self, cluster):
      child_node = self.children.get(cluster.labels[self.col_name], 'nomatch')

      if (child_node is None): child_node = OntologyTerm(data=cluster)
      elif (child_node == 'nomatch'): self.data.append(cluster)
      else: child_node.add_data(cluster)

      self.new_data = True

   def print_ontology(self, prefix):
      term_str = ''

      if (len(self.data) > 0):
         term_str += ', '.join([val for val in self.data]) + '\n'

      for child in self.children.items():
         term_str += prefix + child[0] + '\n'
         if (child[1] is not None):
            term_str += child[1].print_ontology(prefix + '\t')

      return term_str

class OntologyParser(object):
   def __init__(self):
      self.pattern = r'(^[^#].*)[.]([^#].*)\((.*)\):(.*);'

   def parse_ontology(self, ontology_file):
      parsed_ont = Ontology()

      ont_file = open(ontology_file, 'r')
      ont_format = ont_file.readlines()
      ont_file.close()

      for line in ont_format:
         pattern_match = re.search(self.pattern, line)
         table_match = pattern_match.group(1)
         col_match = pattern_match.group(2)
         option_match = pattern_match.group(3).split(',')
         partition_match = pattern_match.group(4).split(',')

         new_term = OntologyTerm(col_name=col_match, partitions=partition_match,
                                 options=option_match)

         parsed_ont.add_term(table_match, new_term)

      return parsed_ont
