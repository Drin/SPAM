DROP TABLE IF EXISTS iso_sims;
CREATE TABLE iso_sims (
   iso_sim_id INT PRIMARY KEY AUTO_INCREMENT,
   test_isolate_id_1 INT,
   test_isolate_id_2 INT,
   similarity DEC(5, 4),
   UNIQUE (test_isolate_id_1, test_isolate_id_2),
   KEY isolate_index_1 (test_isolate_id_1),
   KEY isolate_index_2 (test_isolate_id_2),
   KEY similarity_index (similarity)
);

DROP TABLE IF EXISTS pyro_sims;
-- CREATE TABLE pyro_sims (
--    pyro_id_1 INT,
  --  pyro_id_2 INT,
  --  pyro_sim DEC(4, 4),
  --  PRIMARY KEY(pyro_id_1, pyro_id_2),
  --  KEY sim_index (pyro_sim)
-- );

DROP TABLE IF EXISTS region_sims;
-- CREATE TABLE region_sims (
  --  isolate_id_1 INT,
  --  isolate_id_2 INT,
  --  pyro_id_1 INT,
  --  pyro_id_2 INT,
  --  region_name VARCHAR(20),
  --  region_sim DEC(4, 4),
  --  PRIMARY KEY (isolate_id_1, isolate_id_2, pyro_id_1, pyro_id_2, region_name),
  --  KEY region_index (region_name),
  --  KEY sim_index (region_sim)
-- );

-- CREATE TABLE IF NOT EXISTS tmp_vals (
  --  tmp_val_id INT PRIMARY KEY AUTO_INCREMENT,
  --  count INT
-- );
