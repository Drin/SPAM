DROP TABLE IF EXISTS clustering_metrics;
CREATE TABLE clustering_metrics (
   test_run_id_1 INT,
   test_run_id_2 INT,
   jaccard_index FLOAT,
   dice_coefficient FLOAT,
   rand_index FLOAT,
   variation_of_information FLOAT,
   PRIMARY KEY(test_run_id_1, test_run_id_2),
   INDEX(jaccard_index),
   INDEX(dice_coefficient),
   INDEX(rand_index),
   INDEX(variation_of_information)
);
