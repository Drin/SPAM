DELIMITER $$

DROP PROCEDURE IF EXISTS updateRunSize$$
CREATE PROCEDURE updateRunSize()
BEGIN
   UPDATE test_runs t1 join (SELECT test_run_id, SUM(update_size) AS total_size
                             FROM test_run_performance
                             GROUP BY test_run_id) t2 USING (test_run_id)
   SET t1.total_size = t2.total_size;
END$$

DROP PROCEDURE IF EXISTS updateClusterEntropies$$
CREATE PROCEDURE updateClusterEntropies()
BEGIN
   UPDATE test_run_strain_link s1 JOIN (SELECT test_run_id, cluster_id,
                                               count(*) as clust_size
                                        FROM test_isolate_strains
                                        GROUP BY test_run_id, cluster_id) t1 USING (test_run_id, cluster_id)
                                  JOIN (SELECT test_run_id, SUM(update_size) AS total_size
                                        FROM test_run_performance
                                        GROUP BY test_run_id) t2 USING (test_run_id)
   SET s1.cluster_size = t1.clust_size, s1.cluster_entropy = t1.clust_size/t2.total_size;

   UPDATE test_run_strain_link s1 JOIN (SELECT test_run_id, cluster_id,
                                               count(*) as clust_size
                                        FROM real_isolate_strains
                                        GROUP BY test_run_id, cluster_id) t1 USING (test_run_id, cluster_id)
                                  JOIN (SELECT test_run_id, SUM(update_size) AS total_size
                                        FROM test_run_performance
                                        GROUP BY test_run_id) t2 USING (test_run_id)
   SET s1.cluster_size = t1.clust_size, s1.cluster_entropy = t1.clust_size/t2.total_size;
END$$

DELIMITER ;
