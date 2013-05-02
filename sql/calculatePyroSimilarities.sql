DELIMITER $$

DROP PROCEDURE IF EXISTS calculateSimilarities$$
CREATE PROCEDURE calculateSimilarities()
BEGIN
   DECLARE done INT DEFAULT 0;
   DECLARE iso_id_1, iso_id_2, iso_suffix_1, iso_suffix_2 INT DEFAULT NULL;
   DECLARE iso_prefix_1, iso_prefix_2 VARCHAR(15) DEFAULT NULL;

   DECLARE ISOLATE_SELECT CURSOR FOR
      SELECT i1.test_isolate_id, i1.name_prefix, i1.name_suffix,
             i2.test_isolate_id, i2.name_prefix, i2.name_suffix
      FROM test_isolates i1 join test_isolates i2 ON (i2.test_isolate_id > i1.test_isolate_id)
      WHERE i1.test_isolate_id not in (select distinct test_isolate_id_1 from iso_sims)
      LIMIT 1000;
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   OPEN ISOLATE_SELECT;

   SELECTION_LOOP: LOOP
      FETCH ISOLATE_SELECT INTO iso_id_1, iso_prefix_1, iso_suffix_1,
                                iso_id_2, iso_prefix_2, iso_suffix_2;

      CALL compareIsos(iso_id_1, iso_id_2,
                       iso_prefix_1, iso_suffix_1,
                       iso_prefix_2, iso_suffix_2);

      IF done THEN
         LEAVE SELECTION_LOOP;
      END IF;
   END LOOP;
END$$

DROP PROCEDURE IF EXISTS compareIsos$$
CREATE PROCEDURE compareIsos(IN isolate_id_1 INT, IN isolate_id_2 INT,
                             IN iso_desig_1 VARCHAR(15), IN iso_id_1 INT,
                             IN iso_desig_2 VARCHAR(15), IN iso_id_2 INT)
BEGIN
   DECLARE region_sim_1, region_sim_2 DEC(5, 4) DEFAULT 0;
   DECLARE iso_sim DEC(5, 4) DEFAULT 0;
   DECLARE reg_1_protocol, reg_2_protocol TINYINT DEFAULT 1;

   CALL compareRegion(iso_desig_1, iso_id_1, iso_desig_2, iso_id_2, '16-23', region_sim_1);
   CALL compareRegion(iso_desig_1, iso_id_1, iso_desig_2, iso_id_2, '23-5', region_sim_2);

   IF region_sim_1 < 0 OR region_sim_2 < 0 THEN
      SET iso_sim = -2;
   ELSE
      SET iso_sim = (region_sim_1 + region_sim_2) / 2;
   END IF;

   REPLACE INTO iso_sims(test_isolate_id_1, test_isolate_id_2, similarity)
                VALUES (isolate_id_1, isolate_id_2, iso_sim);
END$$

DROP PROCEDURE IF EXISTS compareRegion$$
CREATE PROCEDURE compareRegion(IN iso_desig_1 VARCHAR(15), IN iso_id_1 INT,
                               IN iso_desig_2 VARCHAR(15), IN iso_id_2 INT,
                               IN region VARCHAR(20), OUT region_sim DEC(5, 4))
BEGIN
   DECLARE done, pyro_id_1, pyro_id_2, compare_count, pyro_len INT DEFAULT 0;
   DECLARE pyro_sim DEC(5, 4) DEFAULT 0;
   DECLARE total_sim FLOAT DEFAULT 0;
   DECLARE pyro_protocol TINYINT DEFAULT 1;

   DECLARE COMPARE_CURSOR CURSOR FOR
      SELECT p1.pyroID, p2.pyroID
      FROM test_pyroprints p1 join test_pyroprints p2 on (
              p1.name_prefix = iso_desig_1 AND p1.name_suffix = iso_id_1 AND
              p2.name_prefix = iso_desig_2 AND p2.name_suffix = iso_id_2 AND
              p1.appliedRegion = region AND p2.appliedRegion = region AND
              p1.dsName = p2.dsName AND p1.forPrimer = p2.forPrimer AND
              p1.revPrimer = p2.revPrimer AND p1.seqPrimer = p2.seqPrimer
           );
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   OPEN COMPARE_CURSOR;

   PYRO_COMPARE_LOOP: LOOP
      FETCH COMPARE_CURSOR INTO pyro_id_1, pyro_id_2;

      IF region = '16-23' THEN
         SET pyro_len = 95;
      ELSE
         IF region = '23-5' THEN
            SET pyro_len = 93;
         END IF;
      END IF;

      CALL comparePyros(pyro_id_1, pyro_id_2, pyro_len, pyro_sim);

      IF pyro_sim IS NOT NULL THEN
         SET total_sim = total_sim + pyro_sim;
         SET compare_count = compare_count + 1;
      END IF;

      IF done THEN
         LEAVE PYRO_COMPARE_LOOP;
      END IF;

   END LOOP;

   IF total_sim IS NOT NULL AND compare_count > 0 THEN
      SET region_sim = total_sim / compare_count;
   ELSE
      SET region_sim = -1;
   END IF;
END$$

DROP PROCEDURE IF EXISTS comparePyros$$
CREATE PROCEDURE comparePyros(IN pyro_id_1 INT, IN pyro_id_2 INT, IN pyro_len INT,
                              OUT pyro_sim DEC(5, 4))
BEGIN
   DECLARE peak_count, done INT DEFAULT 0;
   DECLARE pyro_1_sum, pyro_2_sum, pyro_product FLOAT DEFAULT 0;
   DECLARE pyro_1_squared_sum, pyro_2_squared_sum FLOAT DEFAULT 0;
   DECLARE nucleotide_1, nucleotide_2 VARCHAR(1) DEFAULT NULL;
   DECLARE peak_height_1, peak_height_2 DEC(5, 4) DEFAULT 0;

   DECLARE COMPARE_CURSOR CURSOR FOR
      SELECT h1.nucleotide, h1.pHeight, h2.nucleotide, h2.pHeight
      FROM test_histograms h1 join test_histograms h2 on (
              h1.pyroID = pyro_id_1 AND h2.pyroID = pyro_id_2 AND
              h1.position = h2.position AND h1.position <= pyro_len AND
              h2.position < pyro_len
           )
      ORDER BY h1.pyroID, h1.position, h2.pyroID, h2.position;
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   OPEN COMPARE_CURSOR;

   PYRO_COMPARE_LOOP: LOOP
      FETCH COMPARE_CURSOR INTO nucleotide_1, peak_height_1,
                                nucleotide_2, peak_height_2;

      IF nucleotide_1 = nucleotide_2 THEN
         SET peak_count = peak_count + 1;

         SET pyro_1_sum = pyro_1_sum + peak_height_1;
         SET pyro_2_sum = pyro_2_sum + peak_height_2;

         SET pyro_1_squared_sum = pyro_1_squared_sum + (peak_height_1 * peak_height_1);
         SET pyro_2_squared_sum = pyro_2_squared_sum + (peak_height_2 * peak_height_2);

         SET pyro_product = pyro_product + (peak_height_1 * peak_height_2);
      END IF;

      IF done THEN
         LEAVE PYRO_COMPARE_LOOP;
      END IF;

   END LOOP;

   SET pyro_sim = ((peak_count * pyro_product) - (pyro_1_sum * pyro_2_sum)) /
                  SQRT(
                     ((peak_count * pyro_1_squared_sum) - (pyro_1_sum * pyro_1_sum)) *
                     ((peak_count * pyro_2_squared_sum) - (pyro_2_sum * pyro_2_sum))
                  );
END$$

DELIMITER ;


-- REPLACE INTO pyro_sims VALUES (pyro_id_1, pyro_id_2, total_sim);
-- REPLACE INTO tmp_vals(count) VALUES (compare_count);

-- REPLACE INTO region_sims VALUES(isolate_id_1, isolate_id_2, pyro_id_1, pyro_id_2, region, total_sim);
-- REPLACE INTO region_sims VALUES(isolate_id_1, isolate_id_2, region, pyro_sim);
