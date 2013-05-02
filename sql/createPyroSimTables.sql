DROP TABLE IF EXISTS iso_sims;

CREATE TABLE IF NOT EXISTS iso_sims (
   iso_sim_id INT PRIMARY KEY AUTO_INCREMENT,
   test_isolate_id_1 INT,
   test_isolate_id_2 INT,
   similarity DEC(4, 4),
   UNIQUE (test_isolate_id_1, test_isolate_id_2),
   KEY isolate_index_1 (test_isolate_id_1),
   KEY isolate_index_2 (test_isolate_id_2),
   KEY similarity_index (similarity)
);
