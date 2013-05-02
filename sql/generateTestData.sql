-- Biggest initial size is 100,000
-- largest update size is 10%
-- largest number of updates is 1,000
-- maximum number of data points in a single run will be 1,100,000

DELIMITER $$

-- DROP PROCEDURE IF EXISTS prepareTestTables$$
CREATE PROCEDURE prepareTestTables()
BEGIN
   INSERT IGNORE INTO test_isolates(name_prefix, name_suffix, sampleID, commonName, userName, hostID)
   SELECT SUBSTR(isoID, 1, LOCATE('-', isoID) - 1), SUBSTR(isoID, LOCATE('-', isoID) + 1),
          sampleID, commonName, userName, hostID
   FROM Isolates
   WHERE isoID LIKE '%-%';

   INSERT IGNORE INTO test_pyroprints(pyroID, name_prefix, name_suffix,
                                      appliedRegion, wellID, pyroPrintedDate,
                                      dsName, forPrimer, revPrimer, seqPrimer)
   SELECT pyroID, SUBSTR(isoID, 1, LOCATE('-', isoID) - 1), SUBSTR(isoID, LOCATE('-', isoID) + 1),
          appliedRegion, wellID, str_to_date(pyroPrintedDate, '%m/%d/%Y') AS pyroPrintedDate,
          dsName, forPrimer, revPrimer, seqPrimer
   FROM Pyroprints p JOIN test_isolates i on (i.name_prefix = SUBSTR(isoID, 1, LOCATE('-', isoID) - 1)
                                          AND i.name_suffix = SUBSTR(isoID, LOCATE('-', isoID) + 1))
   WHERE isoID LIKE '%-%';

   INSERT IGNORE INTO test_histograms(pyroID, position, pHeight, nucleotide)
   SELECT pyroID, position, pHeight, nucleotide
   FROM Histograms
   WHERE pyroID IN (SELECT DISTINCT pyroID FROM test_pyroprints);
END$$

-- DROP PROCEDURE IF EXISTS randomizeIsolates$$
CREATE PROCEDURE randomizeIsolates(IN randSeed INT)
BEGIN
   DROP TEMPORARY TABLE IF EXISTS test_isolates_random;
   CREATE TEMPORARY TABLE test_isolates_random(random_id INT PRIMARY KEY AUTO_INCREMENT,
                                               test_isolate_id INT);

   INSERT INTO test_isolates_random(test_isolate_id)
   SELECT test_isolate_id
   FROM test_isolates JOIN test_pyroprints USING (name_prefix, name_suffix) 
   ORDER BY RAND(randSeed);
END$$

DELIMITER ;
