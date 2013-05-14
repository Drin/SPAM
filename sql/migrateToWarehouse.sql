DROP TABLE IF EXISTS isolate_selection;
CREATE TABLE IF NOT EXISTS isolate_selection(
   test_isolate_id INT PRIMARY KEY UNIQUE,
   pyro_id_1 INT,
   pyro_id_2 INT,
   name_prefix VARCHAR(4),
   name_suffix INT,
   userName VARCHAR(46),
   commonName VARCHAR(22),
   hostID VARCHAR(22),
   sampleID INT,
   INDEX(pyro_id_1, pyro_id_2)
);

DELIMITER $$

DROP PROCEDURE IF EXISTS prepMigration$$
CREATE PROCEDURE prepMigration(IN num_isolates INT)
BEGIN

   INSERT IGNORE INTO isolate_selection (test_isolate_id, pyro_id_1, pyro_id_2,
                                         name_prefix, name_suffix, userName,
                                         commonName, hostID, sampleID)
   SELECT test_isolate_id, p1.pyroID, p2.pyroID,
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
   ORDER BY RAND()
   LIMIT num_isolates;
END$$

DELIMITER ;
