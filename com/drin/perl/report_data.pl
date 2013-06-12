use strict;
use warnings;

use DBI;

use constant DB_USER => q{};
use constant DB_PASS => q{};

use constant PERF_FILE => q{performance.csv};
use constant SIM_FILE  => q{similarities.csv};

my $db_handle = DBI->connect(q{dbi:mysql:CPLOP:localhost}, DB_USER, DB_PASS)
             or die ("Unable to connect to database");

################################################################################
#
# CLUSTER QUERIES
#
################################################################################

my $CLUSTER_SIZES = $db_handle->prepare(q{
   SELECT test_run_id, cluster_id, count(*) as clust_size
   FROM test_isolate_strains
        JOIN clustering_metrics ON (
           test_run_id = test_run_id_1 OR
           test_run_id = test_run_id_2
        )
   GROUP BY test_run_id, cluster_id
});

my $CLUSTER_METRICS = $db_handle->prepare(q{
   SELECT r1.cluster_algorithm as alg_1, r1.test_run_id as run_id_1,
          r1.total_size as size_1, r1.use_transform as trans_1,
          r1.average_strain_similarity as avg_sim_1,
          TIME_TO_SEC(r1.run_time) as time_1,
          r2.cluster_algorithm as alg_2, r2.test_run_id as run_id_2,
          r2.total_size as size_2, r2.use_transform as trans_2,
          r2.average_strain_similarity as avg_sim_2,
          TIME_TO_SEC(r2.run_time) as time_2,
          jaccard_index, dice_coefficient,
          rand_index, variation_of_information
   FROM clustering_metrics
        JOIN test_runs r1 ON (
           r1.test_run_id = test_run_id_1
        )
        JOIN test_runs r2 ON (
           r2.test_run_id = test_run_id_2
        )
});

################################################################################
#
# PERFORMANCE QUERIES
#
################################################################################

my $RUN_TIMES = $db_handle->prepare(q{
   SELECT test_run_id, update_id, update_size, TIME_TO_SEC(run_time)
   FROM test_run_performance
});

sub get_cluster_info {
   my ($clust_info_map, $clust_comp_map, $run_id_filter) = (@_);
   my ($run_id, $tmp_clust_map);

   $CLUSTER_METRICS->execute();
   for my $clust_record (@{$CLUSTER_METRICS->fetchall_arrayref({})}) {

      for my $clust_num ((q{1}, q{2})) {
         $tmp_clust_map = {
            algorithm => $clust_record->{"alg_$clust_num"},
            size      => $clust_record->{"size_$clust_num"},
            transform => $clust_record->{"trans_$clust_num"},
            avg_strain_sim => $clust_record->{"avg_sim_$clust_num"},
            run_time  => $clust_record->{"time_$clust_num"},
            clusters  => [], #populated in subroutine 'get_cluster_sizes'
            perf      => []
         };

         $run_id = $clust_record->{"run_id_$clust_num"};
         $clust_info_map->{$run_id} = $tmp_clust_map;
      }

      if (!$clust_comp_map->{$clust_record->{'run_id_1'}}) {
         $clust_comp_map->{$clust_record->{'run_id_1'}} = {};
      }
      $tmp_clust_map = $clust_comp_map->{$clust_record->{'run_id_1'}};
      $tmp_clust_map->{$clust_record->{'run_id_2'}} = {
         jaccard => $clust_record->{jaccard_index},
         dice    => $clust_record->{dice_coefficient},
         rand    => $clust_record->{rand_index},
         varinfo => $clust_record->{variation_of_information}
      };
   }
}

sub get_cluster_sizes {
   my ($clust_info_map) = (@_);
   my ($run_id, $clust_id, $clust_size) = (-1, -1, -1);

   $CLUSTER_SIZES->execute();
   for my $clust_record (@{$CLUSTER_SIZES->fetchall_arrayref({})}) {
      $run_id = $clust_record->{test_run_id};
      $clust_id = $clust_record->{cluster_id};
      $clust_size = $clust_record->{clust_size};

      push(@{$clust_info_map->{$run_id}->{clusters}}, {
         clust_id => $clust_id,
         clust_size => $clust_size
      });
   }
}

sub get_performance {
   my ($run_id, $clust_info_map, $id_filter) = (-1, @_);

   $RUN_TIMES->execute();
   for my $perf_record (@{$RUN_TIMES->fetchall_arrayref({})}) {
      $run_id = $perf_record->{test_run_id};

      push(@{$clust_info_map->{$run_id}->{perf}}, {
         update_id   => $perf_record->{update_id},
         update_size => $perf_record->{update_size},
         run_time    => $perf_record->{run_time}
      });
   }
}

sub write_performance {
   my ($general_fd, $detailed_fd, $clust_info) = (@_);
   my ($avg_run_time, $update_size, $count, $time) = (0, 0, 0, 0);
   my ($algorithm_name, $total_time, $total_size) = (q{-}, 0, 0);

   if ($clust_info->{run_time}) {
      $total_time = $clust_info->{run_time};
   }

   if ($clust_info->{size}) {
      $total_size = $clust_info->{size};
   }

   if ($clust_info->{algorithm}) {
      $algorithm_name = $clust_info->{algorithm};
   }

   print({$general_fd} sprintf("%s, %d, %d,,,",
      $algorithm_name, $total_size, $total_time
   ));

   for my $perf (@{$clust_info->{perf}}) {
      $time = 0;
      if ($perf->{run_time}) { $time = $perf->{run_time}; }

      if ($perf->{update_id} > 0) {
         $count++;
         $avg_run_time += $time;

         if ($update_size == 0) { $update_size = $perf->{update_size}; }
      }

      print({$detailed_fd} sprintf("%s, %d, %d,\n",
         $algorithm_name, $perf->{update_size}, $time,
      ));
   }

   if (!$count) { $count = 1; }

   print({$general_fd} sprintf("%s, %d, %.04f,,,",
      $algorithm_name, $update_size, ($avg_run_time / $count)
   ));

   print({$general_fd} "\n");

}

sub write_similarities {
   my ($sim_fd, $clust_info, $comp_map) = (@_);
   my ($transform_str, $clust_sims);

   for my $run_id (keys %{$comp_map}) {
      $clust_sims = $comp_map->{$run_id};

      if ($clust_info->{transform}) { $transform_str = q{yes}; }
      else { $transform_str = q{no}; }

      print({$sim_fd} sprintf("%s, %d, %.04f, %.04f, %.04f, %.04f,\n",
         $transform_str, $clust_info->{size} || 0,
         $clust_sims->{jaccard} || 0, $clust_sims->{dice} || 0,
         $clust_sims->{rand} || 0, $clust_sims->{varinfo} || 0
      ));
   }
}

sub write_output {
   my ($clust_info_map, $clust_comp_map) = (@_);
   my ($perf_file, $perf_file_detailed, $sim_file);

   open($perf_file, q{>}, PERF_FILE) or die (q{could not open file }.PERF_FILE);
   open($sim_file, q{>}, SIM_FILE) or die (q{could not open file }.SIM_FILE);
   open($perf_file_detailed, q{>}, q{detailed-}.PERF_FILE) or
          die (q{could not open file detailed-}.PERF_FILE);

   print({$perf_file} sprintf("%s, %s, %s,,, %s, %s, %s,,,\n",
      q{cluster algorithm}, q{dataset size}, q{total run time},
      q{cluster algorithm}, q{update size}, q{average run time}
   ));

   print({$perf_file_detailed} sprintf("%s, %s, %s,\n",
      q{cluster algorithm}, q{update size}, q{run time}
   ));

   print({$sim_file} sprintf("%s, %s, %s, %s, %s, %s,\n",
      q{used transform}, q{dataset size}, q{jaccard}, q{dice}, q{rand},
      q{variation of information}
   ));

   for my $run_id (keys %{$clust_info_map}) {
      write_performance($perf_file, $perf_file_detailed, $clust_info_map->{$run_id});
      write_similarities($sim_file, $clust_info_map->{$run_id}, $clust_comp_map);
   }

   close($sim_file) or die (q{could not close file }.SIM_FILE);
   close($perf_file) or die (q{could not close file }.PERF_FILE);
   close($perf_file_detailed) or die (q{could not close file detailed-}.PERF_FILE);
}

sub main {
   my ($clust_info_map, $clust_comp_map) = ({}, {});

   get_cluster_info($clust_info_map, $clust_comp_map, [95, 96]);
   get_cluster_sizes($clust_info_map, [95, 96]);
   get_performance($clust_info_map, [95, 96]);

   write_output($clust_info_map, $clust_comp_map);
}

main();
