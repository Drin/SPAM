DROP TABLE IF EXISTS test_histograms;
DROP TABLE IF EXISTS test_pyroprints;
DROP TABLE IF EXISTS test_isolates;

CREATE TABLE IF NOT EXISTS test_isolates(
   test_isolate_id INT PRIMARY KEY AUTO_INCREMENT,
   name_prefix VARCHAR(15) NOT NULL,
   name_suffix INT NOT NULL,
   sampleID INT(11) DEFAULT NULL,
   commonName VARCHAR(50) NOT NULL,
   userName VARCHAR(50) NOT NULL,
   hostID VARCHAR(50) NOT NULL,
   is_generated TINYINT NOT NULL DEFAULT 0,
   UNIQUE(name_prefix, name_suffix),
   INDEX(name_prefix, name_suffix),
   INDEX(sampleID),
   INDEX(commonName),
   INDEX(hostID)
);

CREATE TABLE IF NOT EXISTS test_pyroprints(
   pyroID INT(11) PRIMARY KEY AUTO_INCREMENT,
   name_prefix VARCHAR(15) NOT NULL,
   name_suffix INT NOT NULL,
   appliedRegion VARCHAR(20) NOT NULL,
   wellID VARCHAR(5) NOT NULL,
   dsName VARCHAR(205) DEFAULT NULL,
   forPrimer VARCHAR(40) DEFAULT NULL,
   revPrimer VARCHAR(40) DEFAULT NULL,
   seqPrimer VARCHAR(40) DEFAULT NULL,
   pyroPrintedDate DATE DEFAULT NULL,
   is_generated TINYINT NOT NULL DEFAULT 0,
   FOREIGN KEY(name_prefix, name_suffix) REFERENCES test_isolates(name_prefix, name_suffix),
   INDEX(name_prefix, name_suffix),
   INDEX(appliedRegion),
   INDEX(dsName, forPrimer, revPrimer, seqPrimer),
   INDEX(pyroPrintedDate)
);

CREATE TABLE IF NOT EXISTS test_histograms(
   pyroID INT(11) NOT NULL,
   position INT(11) NOT NULL,
   pHeight DECIMAL(8,4) NOT NULL,
   nucleotide VARCHAR(1) NOT NULL,
   PRIMARY KEY(pyroID, position),
   FOREIGN KEY(pyroID) REFERENCES test_pyroprints(pyroID)
);
