import os
import sys
import re

import copy
import CPLOP
import Clusterer

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
      self.column_parts = dict()

   def get_cluster_separation(self):
      (clust_separation, count) = (0, 0)

      for ndx_A in range(len(self.root.data)):
         for ndx_B in range(ndx_A + 1, len(self.root.data)):
            if (type(self.root.data[ndx_A]) is Clusterer.Cluster or
                type(self.root.data[ndx_A]) is Clusterer.FastCluster):
               clust_separation += self.root.data[ndx_A].compare_to(self.root.data[ndx_B])
               count += 1

      if (count > 0):
         return (clust_separation / count)

      print("count should not be 0!")
      return -2

   def add_term(self, table_name, new_term):
      if (self.table_columns.get(table_name) is None):
         self.table_columns[table_name] = []
      self.table_columns[table_name].append(new_term.col_name)

      for partition in new_term.children.items():
         if (self.column_parts.get(new_term.col_name) is None):
            self.column_parts[new_term.col_name] = []
         self.column_parts[new_term.col_name].append(partition[0])

      if (self.root is not None):
         self.root.add_term(new_term)
      else:
         self.root = new_term

   def add_data(self, cluster):
      data_added = False
      if (self.root is not None):
         data_added = self.root.add_data(cluster)

      return data_added

   def __str__(self):
      return 'root:\n' + self.root.print_ontology('\t')

class OntologyTerm(object):
   def __init__(self, data=None, col_name=None, partitions=None, options=None):
      self.data = []
      self.children = dict()
      self.options = dict()
      self.col_name = col_name

      if (data is not None and len(data) > 0):
         self.data.append(data)
      self.new_data = (len(self.data) > 0)

      if (partitions is not None):
         for partition in partitions:
            if (partition.strip() == ''): continue
            self.children[partition.strip()] = None

      if (options is not None):
         for option in options:
            self.options[option] = True

   def add_term(self, new_term):
      for child in self.children.items():
         if (child[1] is not None):
            child[1].add_term(new_term)
         else:
            self.children[child[0]] = copy.deepcopy(new_term)

   def add_data(self, cluster):
      if (type(cluster) is not Clusterer.Cluster or
          type(cluster) is not Clusterer.FastCluster):
         self.new_data = False
         return

      for label in cluster.labels:
         child_node = self.children.get(label, 'nomatch')

         if (child_node == 'nomatch'):
            continue
         elif (child_node is None):
            self.children[label] = OntologyTerm(data=cluster)
         else:
            child_node.add_data(cluster)
         break
      else:
         if (len(cluster) > 0):
            self.data.append(cluster)

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
      (parsed_ont, conn) = (Ontology(), CPLOP.connection())

      ont_file = open(ontology_file, 'r')
      ont_format = ont_file.readlines()
      ont_file.close()

      for line in ont_format:
         pattern_match = re.search(self.pattern, line)
         if (pattern_match is None):
            continue

         table_match = pattern_match.group(1)
         col_match = pattern_match.group(2)
         option_match = pattern_match.group(3).split(',')
         partition_match = None

         if (pattern_match.group(4) == ''):
            partition_match = conn.get_distinct_values(table_match, col_match)
         else:
            partition_match = pattern_match.group(4).split(',')

         new_term = OntologyTerm(col_name=col_match,
                                 partitions=partition_match,
                                 options=option_match)

         parsed_ont.add_term(table_match, new_term)

      return parsed_ont