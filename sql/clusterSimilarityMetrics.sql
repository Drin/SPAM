DELIMITER $$

DROP PROCEDURE IF EXISTS updateRunSize$$
CREATE PROCEDURE updateRunSize()
BEGIN
   UPDATE test_runs t1 join (SELECT test_run_id, sum(update_size) AS total_size
                             FROM test_run_performance
                             GROUP BY test_run_id) t2 USING (test_run_id)
   SET t1.total_size = t2.total_size;
END$$

DROP PROCEDURE IF EXISTS calcClustMetrics$$
CREATE PROCEDURE calcClustMetrics(IN test_run_1 INT, IN test_run_2 INT,
                                  OUT jaccard DEC(5, 4), OUT dice DEC(5, 4),
                                  OUT rand DEC(5, 4))
BEGIN
   DECLARE done, isolate_id_1, isolate_id_2 INT DEFAULT 0;
   DECLARE rand_sum, jaccard_sum INT DEFAULT 0;
   DECLARE rand_count, jaccard_count INT DEFAULT 0;
   DECLARE clust_id_1, clust_id_2 INT DEFAULT -1;

   DECLARE CLUST_SET_1 CURSOR FOR
      SELECT distinct c1.test_isolate_id, c3.test_isolate_id,
             IF(c1.cluster_id = c3.cluster_id, 1, -1)
      FROM test_isolate_strains c1
           JOIN test_isolate_strains c2 ON (
              c1.test_run_id = test_run_1 AND
              c2.test_run_id = test_run_2 AND
              c1.test_isolate_id = c2.test_isolate_id
           )
           JOIN test_isolate_strains c3 ON (
              c3.test_run_id = c1.test_run_id AND
              c3.test_isolate_id > c1.test_isolate_id
           )
      ORDER BY c1.test_isolate_id, c3.test_isolate_id;

   DECLARE CLUST_SET_2 CURSOR FOR
      SELECT distinct c1.test_isolate_id, c3.test_isolate_id,
             IF(c2.cluster_id = c3.cluster_id, 1, -1)
      FROM test_isolate_strains c1
           JOIN test_isolate_strains c2 ON (
              c1.test_run_id = test_run_1 AND
              c2.test_run_id = test_run_2 AND
              c1.test_isolate_id = c2.test_isolate_id
           )
           JOIN test_isolate_strains c3 ON (
              c3.test_run_id = c2.test_run_id AND
              c3.test_isolate_id > c1.test_isolate_id
           )
      ORDER BY c1.test_isolate_id, c3.test_isolate_id;

   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   OPEN CLUST_SET_1;
   OPEN CLUST_SET_2;

   COMPARE_LOOP: LOOP
      FETCH CLUST_SET_1 INTO isolate_id_1, isolate_id_2, clust_id_1;
      FETCH CLUST_SET_2 INTO isolate_id_1, isolate_id_2, clust_id_2;

      IF done THEN
         LEAVE COMPARE_LOOP;
      END IF;

      IF clust_id_1 = clust_id_2 THEN
         SET rand_sum = rand_sum + 2;
      END IF;

      IF clust_id_1 = 1 and clust_id_2 = 1 THEN
         SET jaccard_sum = jaccard_sum + 1;
      END IF;

      IF clust_id_1 = 1 OR clust_id_2 = 1 THEN
         SET jaccard_count = jaccard_count + 1;
      END IF;

      SET rand_count = rand_count + 1;
   END LOOP;

   IF rand_sum > 0 or jaccard_sum > 0 THEN
      SET rand = rand_sum / (rand_count * (rand_count - 1));
      SET jaccard = jaccard_sum / jaccard_count;
      SET dice = (2 * jaccard_sum) / (rand_count * (rand_count - 1));
   ELSE
      SET rand = -1;
      SET jaccard = -1;
      SET dice = -1;
   END IF;

END$$

DROP PROCEDURE IF EXISTS calcMetrics$$
CREATE PROCEDURE calcMetrics()
BEGIN
   DECLARE done, test_run_1, test_run_2 INT DEFAULT 0;
   DECLARE jaccard_index, dice_coefficient, rand_index DEC(5, 4) DEFAULT -1;

   DECLARE CLUSTER_PART CURSOR FOR
      SELECT t1.test_run_id, t2.test_run_id
      FROM test_runs t1 join test_runs t2 ON (
              t1.cluster_algorithm = 'OHClust!' AND
              t2.cluster_algorithm = 'agglomerative' AND
              t1.total_size = t2.total_size
           );
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   OPEN CLUSTER_PART;

   CLUST_COMPARISONS: LOOP
      FETCH CLUSTER_PART INTO test_run_1, test_run_2;

      IF done THEN
         LEAVE CLUST_COMPARISONS;
      END IF;

      CALL calcClustMetrics(test_run_1, test_run_2,
                            jaccard_index, dice_coefficient, rand_index);

      IF jaccard_index > 0 OR dice_coefficient > 0 OR rand_index > 0 THEN
         INSERT IGNORE INTO clustering_metrics(test_run_id_1, test_run_id_2,
                                               jaccard_index, dice_coefficient,
                                               rand_index)
         VALUES (test_run_1, test_run_2, jaccard_index, dice_coefficient, rand_index);
      END IF;

   END LOOP;
END$$

DELIMITER ;
