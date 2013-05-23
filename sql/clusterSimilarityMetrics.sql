DELIMITER $$

DROP PROCEDURE IF EXISTS calcVariationOfInfo$$
CREATE PROCEDURE calcVariationOfInfo(IN test_run_1 INT, IN test_run_2 INT,
                                     OUT info FLOAT, OUT ent_1 FLOAT, OUT ent_2 FLOAT)
BEGIN
   DECLARE done, data_size_1, data_size_2, overlap INT DEFAULT 0;
   DECLARE clust_id_1, clust_size_1, clust_id_2, clust_size_2 INT DEFAULT 0;
   DECLARE var_info, entr_1, entr_2 FLOAT DEFAULT 0;

   DECLARE MUTUAL_INFO_CURSOR CURSOR FOR
      SELECT t1.cluster_id, t1.cluster_size,
             t2.cluster_id, t2.cluster_size
      FROM test_run_strain_link t1 JOIN test_run_strain_link t2
      WHERE t1.test_run_id = test_run_1 AND
            t2.test_run_id = test_run_2;
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   drop temporary table if exists debugging;
   create temporary table debugging (clust_id_1 INT primary key);

   SELECT total_size INTO data_size_1
   FROM test_runs
   WHERE test_run_id = test_run_1;

   SELECT total_size INTO data_size_2
   FROM test_runs
   WHERE test_run_id = test_run_2;

   SET @test_1 = 0;

   OPEN MUTUAL_INFO_CURSOR;

   IF data_size_1 = data_size_2 THEN
      INFOLOOP: LOOP
         FETCH MUTUAL_INFO_CURSOR INTO clust_id_1, clust_size_1, clust_id_2, clust_size_2;

         IF done THEN
            LEAVE INFOLOOP;
         END IF;

         INSERT IGNORE INTO debugging(clust_id_1) VALUES (clust_id_1);

         SELECT count(*) INTO overlap
         FROM test_isolate_strains s1
              JOIN test_isolate_strains s2 USING (test_isolate_id)
         WHERE s1.test_run_id = test_run_1 AND
               s2.test_run_id = test_run_2 AND
               s1.cluster_id = clust_id_1 AND
               s2.cluster_id = clust_id_1;

         IF ((overlap / data_size_1) /
             ((clust_size_1 / data_size_1) *
              (clust_size_2 / data_size_1))) > 0 THEN
            SET var_info = var_info + (
               (overlap / data_size_1) *
               LOG2(
                  (overlap / data_size_1) /
                  ((clust_size_1 / data_size_1) *
                   (clust_size_2 / data_size_1))
               )
            );
         END IF;

         SET entr_1 = entr_1 + (
            (clust_size_1 / data_size_1) *
            LOG2(
               (clust_size_1 / data_size_1)
            )
         );

         SET entr_2 = entr_2 + (
            (clust_size_2 / data_size_1) *
            LOG2(
               (clust_size_2 / data_size_1)
            )
         );
      END LOOP;

      SET info = var_info;
      SET ent_1 = entr_1 * -1;
      SET ent_2 = entr_2 * -1;
   ELSE
      SET info = 0;
      SET ent_1 = 0;
      SET ent_2 = 0;
   END IF;
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
         SET rand_sum = rand_sum + 1;
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
      SET rand = rand_sum / rand_count;
      SET jaccard = jaccard_sum / jaccard_count;
      SET dice = (2 * jaccard_sum) / rand_count;
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
   DECLARE ent_1, ent_2, variation FLOAT DEFAULT 0;

   DECLARE CLUSTER_PART CURSOR FOR
      SELECT t1.test_run_id, t2.test_run_id
      FROM test_runs t1 join test_runs t2 ON (
              t1.cluster_algorithm = 'OHClust!' AND
              t2.cluster_algorithm = 'Agglomerative' AND
              t1.total_size = t2.total_size
           );
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   OPEN CLUSTER_PART;

   CLUST_COMPARISONS: LOOP
      FETCH CLUSTER_PART INTO test_run_1, test_run_2;

      IF done THEN
         LEAVE CLUST_COMPARISONS;
      END IF;

      CALL calcVariationOfInfo(test_run_1, test_run_2,
                               variation, ent_1, ent_2);

      CALL calcClustMetrics(test_run_1, test_run_2,
                            jaccard_index, dice_coefficient, rand_index);

      INSERT IGNORE INTO clustering_metrics(test_run_id_1, test_run_id_2,
                                            jaccard_index, dice_coefficient,
                                            rand_index, entropy_1, entropy_2,
                                            variation_of_information)
      VALUES (test_run_1, test_run_2, jaccard_index, dice_coefficient, rand_index,
              ent_1, ent_2, variation);

   END LOOP;
END$$

DELIMITER ;
