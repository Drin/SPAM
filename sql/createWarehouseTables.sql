CREATE TABLE IF NOT EXISTS isolate_selection(
   test_isolate_id INT PRIMARY KEY,
   pyro_id_1 INT,
   pyro_id_2 INT,
   name_prefix VARCHAR(4),
   name_suffix INT,
   userName VARCHAR(46),
   commonName VARCHAR(22),
   hostID VARCHAR(22),
   sampleID INT
);

CREATE TABLE IF NOT EXISTS isolate_warehouse (
   name_prefix VARCHAR(15) NOT NULL,
   name_suffix INT NOT NULL,
   commonName VARCHAR(50) NOT NULL,
   userName VARCHAR(50) NOT NULL,
   sampleID INT(11) DEFAULT NULL,
   hostID VARCHAR(50) NOT NULL,
   peaks_23_5 VARCHAR(1024) NOT NULL,
   peaks_16_23 VARCHAR(1024) NOT NULL,
   PRIMARY KEY(name_prefix, name_suffix)
);
