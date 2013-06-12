use strict;
use warnings;

use DBI;

use TestRun;
use Cluster;

my $dbi_handle = DBI->connect(q{dbi:mysql:CPLOP:localhost}, q{}, q{});

my $CLUSTER_META_QUERY = q{
   SELECT cluster_id, count(*) as clustCount
   FROM test_isolate_strains
   WHERE test_run_id = %d
   GROUP BY cluster_id
};

my $CLUSTER_ISOLATE_QUERY = q{
   SELECT cluster_id, test_isolate_id
   FROM test_isolate_strains
   WHERE test_run_id = %d
};

sub get_clust_isolate_info {
   my ($cluster_partitions) = (@_);

   for my $run_id (keys %{$cluster_partitions}) {
      my $isolate_query = $dbi_handle->prepare(
         sprintf($CLUSTER_ISOLATE_QUERY, $run_id)
      );

      $isolate_query->execute();

      for my $record (@{$isolate_query->fetchall_arrayref({})}) {
         my $clust_obj = $cluster_partitions->{$run_id}->get_cluster(
            $record->{cluster_id}
         );

         if ($clust_obj) {
            $clust_obj->add_isolate($record->{test_isolate_id});
         }
         else { print({*STDOUT} "wtf cluster wasn't found?\n"); }
      }
   }
}

sub get_clust_meta_info {
   my ($first_run, $second_run) = (@_);
   my ($cluster_partitions, $clust_list, $num_isolates) = ({}, [], 0);

   for my $run_id (($first_run, $second_run)) {
      my $info_query = $dbi_handle->prepare(
         sprintf($CLUSTER_META_QUERY, $run_id)
      );

      $info_query->execute();

      for my $record (@{$info_query->fetchall_arrayref({})}) {
         push(@{$clust_list}, Cluster->new({
            id   => $record->{cluster_id},
            size => $record->{clustCount},
         }));

         $num_isolates += $record->{clustCount};
      }

      $cluster_partitions->{$run_id} = TestRun->new({
         run_id       => $run_id,
         data_size    => $num_isolates,
         num_clusters => scalar @{$clust_list},
         clusters     => $clust_list
      });

      ($clust_list, $num_isolates) = ([], 0);
   }

   if ($cluster_partitions->{$first_run}->{data_size} !=
       $cluster_partitions->{$second_run}->{data_size}) {
       print({*STDOUT} "mismatching test run sizes... hmm...\n");
    }

   return $cluster_partitions;
}

sub log_2 {
   my ($num) = (@_);

   if (!$num) { return 0; }
   return log($num)/log(2);
}

sub get_variation_of_information {
   my ($run_1, $run_2) = (@_);

   my $cluster_partitions = get_clust_meta_info($run_1, $run_2);
   get_clust_isolate_info($cluster_partitions);

   my $test_run_1 = $cluster_partitions->{$run_1};
   my $test_run_2 = $cluster_partitions->{$run_2};

   my ($p_ij, $p_i, $p_j);
   my ($entropy_1, $entropy_2, $mutual_info) = (0, 0, 0);

   for my $clust_A (@{$test_run_1->get_clusters()}) {
      for my $clust_B (@{$test_run_2->get_clusters()}) {

         $p_ij = $clust_A->get_overlap($clust_B);
         $p_i = $test_run_1->calc_clust_prob($clust_A);
         $p_j = $test_run_2->calc_clust_prob($clust_B);

         $mutual_info += $p_ij * log_2($p_ij / ($p_i * $p_j));

         $entropy_1 += $p_i * log_2($p_i);
         $entropy_2 += $p_j * log_2($p_j);
      }
   }

   $entropy_1 *= -1;
   $entropy_2 *= -1;

   my $variation_of_info = ($entropy_1 - $mutual_info) +
                           ($entropy_2 - $mutual_info);

   return ($entropy_1, $entropy_2, $mutual_info, $variation_of_info);
}

my $comparisons = [[130, 131], [132, 133], [134, 135]];
my ($H_c, $H_c2, $I_c, $V_c);

for my $id_pair (@{$comparisons}) {
   ($H_c, $H_c2, $I_c, $V_c) = get_variation_of_information(
      $id_pair->[0], $id_pair->[1]
   );

   print({*STDOUT} "entropy 1: $H_c\nentropy 2: $H_c2\n".
                   "mutual information: $I_c\nVariation: $V_c\n");
}
