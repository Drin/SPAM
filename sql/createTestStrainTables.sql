-- Test Tables

-- --------------------------------------------------------

-- Drop Tables

DROP TABLE IF EXISTS test_run_performance;
DROP TABLE IF EXISTS test_isolate_strains;
DROP TABLE IF EXISTS test_run_strain_link;
DROP TABLE IF EXISTS test_runs;

-- --------------------------------------------------------

--
-- Table structure for table test_runs
--

CREATE TABLE IF NOT EXISTS test_runs (
  test_run_id INT NOT NULL auto_increment,
  run_date DATETIME NOT NULL,
  run_time TIME NOT NULL,
  cluster_algorithm VARCHAR(100) NOT NULL,
  average_strain_similarity FLOAT NOT NULL,
  total_size INT DEFAULT -1,
  use_transform TINYINT NOT NULL,
  PRIMARY KEY  (test_run_id),
  KEY date_index (run_date),
  KEY algorithm_index (cluster_algorithm),
  KEY transform_index (use_transform)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table test_run_strain_link
--

CREATE TABLE IF NOT EXISTS test_run_strain_link (
  test_run_id INT NOT NULL,
  cluster_id INT NOT NULL,
  cluster_threshold FLOAT NOT NULL,
  strain_diameter FLOAT NOT NULL,
  average_isolate_similarity FLOAT NOT NULL,
  percent_similar_isolates FLOAT NOT NULL,
  PRIMARY KEY (test_run_id, cluster_id, cluster_threshold),
  FOREIGN KEY (test_run_id) REFERENCES test_runs (test_run_id),
  KEY diameter_index (strain_diameter),
  KEY isolate_similarity_index (average_isolate_similarity)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table test_isolate_strains
--

CREATE TABLE IF NOT EXISTS test_isolate_strains (
  test_run_id INT NOT NULL,
  cluster_id INT NOT NULL,
  cluster_threshold FLOAT NOT NULL,
  name_prefix VARCHAR(15) NOT NULL,
  name_suffix INT NOT NULL,
  PRIMARY KEY (test_run_id, cluster_id, cluster_threshold, name_prefix, name_suffix),
  FOREIGN KEY (test_run_id, cluster_id, cluster_threshold) REFERENCES test_run_strain_link (test_run_id, cluster_id, cluster_threshold),
  FOREIGN KEY (name_prefix, name_suffix) REFERENCES test_isolates (name_prefix, name_suffix),
  KEY cluster_id_index (test_run_id, cluster_id, cluster_threshold),
  KEY isolate_id_index (name_prefix, name_suffix)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table test_run_performance
--

CREATE TABLE IF NOT EXISTS test_run_performance (
  test_run_id INT NOT NULL,
  update_id INT NOT NULL,
  update_size INT NOT NULL,
  run_time INT NOT NULL,
  PRIMARY KEY (test_run_id, update_id),
  FOREIGN KEY (test_run_id) REFERENCES test_runs (test_run_id),
  KEY update_size_index (update_size),
  KEY run_time_index (run_time)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
