DELIMITER $$

DROP PROCEDURE IF EXISTS prepMigration$$
CREATE PROCEDURE prepMigration(IN num_isolates INT)
BEGIN

   INSERT IGNORE INTO isolate_selection (test_isolate_id, pyro_id_1, pyro_id_2,
                                         name_prefix, name_suffix, userName,
                                         commonName, hostID, sampleID)
   SELECT distinct test_isolate_id, p1.pyroID, p2.pyroID,
                   t.name_prefix, t.name_suffix, t.userName,
                   t.commonName, t.hostID, t.sampleID
   FROM test_isolates t
        JOIN test_pyroprints p1 on (
           t.name_prefix = p1.name_prefix AND
           t.name_suffix = p1.name_suffix AND
           p1.appliedRegion = '23-5'
        )
        JOIN test_pyroprints p2 on (
           t.name_prefix = p2.name_prefix AND
           t.name_suffix = p2.name_suffix AND
           p2.appliedRegion = '16-23'
        )
   WHERE t.name_prefix IS NOT NULL
   ORDER BY RAND()
   LIMIT num_isolates;
END$$

DROP PROCEDURE IF EXISTS migrateData$$
CREATE PROCEDURE migrateData()
BEGIN
   DECLARE done, name_suffix, sample_id, isolate_id INT DEFAULT 0;
   DECLARE name_prefix VARCHAR(4);
   DECLARE user_name VARCHAR(46);
   DECLARE common_name, host_id VARCHAR(22);
   DECLARE peaks_16, peaks_23 VARCHAR(1024);

   DECLARE ISOLATE_SELECT CURSOR FOR
      SELECT test_isolate_id, name_prefix, name_suffix,
             userName, commonName, hostID, sampleID,
             GROUP_CONCAT(h1.pHeight) AS peaks_23_5,
             GROUP_CONCAT(h2.pHeight) AS peaks_16_23
      FROM test_histograms h1
           JOIN isolate_selection i ON (
              h1.pyroID = i.pyro_id_1 AND
              h1.position < 95
           )
           JOIN test_histograms h2 ON (
              h1.position = h2.position AND
              h2.pyroID = i.pyro_id_2
           )
      GROUP BY test_isolate_id
      ORDER BY test_isolate_id, h1.position;
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   OPEN ISOLATE_SELECT;

   SELECTION_LOOP: LOOP
      FETCH ISOLATE_SELECT INTO isolate_id, name_prefix, name_suffix,
                                user_name, common_name, host_id, sample_id,
                                peaks_23, peaks_16;

      IF name_prefix IS NOT NULL THEN
         INSERT INTO isolate_warehouse(name_prefix, name_suffix, commonName,
                                       userName, sampleID, hostID,
                                       peaks_23_5, peaks_16_23)
         VALUES(name_prefix, name_suffix, user_name, common_name, host_id,
                sample_id, peaks_23, peaks_16);
      END IF;

      IF done THEN
         LEAVE SELECTION_LOOP;
      END IF;
   END LOOP;
END$$

DELIMITER ;
