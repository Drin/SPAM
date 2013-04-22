use strict;
use warnings;

use DBI;

use BiologyTypes::Isolate;
use BiologyTypes::Pyroprint;

use constant DB_USER => q{drin};
use constant DB_PASS => q{};

use constant CROSSOVER_PROB => 0.8;

my $db_handle = DBI->connect(q{dbi:mysql:CPLOP:localhost:8906}, DB_USER, DB_PASS)
             or die ("Unable to connect to database");

#my $RAND_ISOLATE_QUERY = $db_handle->prepare("CALL getRandomIsolates(?)");
my $RAND_ISOLATE_QUERY = $db_handle->prepare(q{
   SELECT name_prefix, name_suffix
   FROM test_isolates
   ORDER BY RAND()
   LIMIT ?
});

sub output_records {
   my ($records) = (@_);

   for my $record (@{$records}) {
      for my $attr_col (keys %{$record}) {
         print({*STDOUT} "\t$attr_col -> $record->{$attr_col}\n");
      }

      print({*STDOUT} "\n");
   }
}

sub select_isolates {
   my ($num_records) = (@_);

   $RAND_ISOLATE_QUERY->bind_param(1, $num_records);
   $RAND_ISOLATE_QUERY->execute();

   return $RAND_ISOLATE_QUERY->fetchall_arrayref({});
}

sub get_isolate_info {
   my ($isolates, $isolate_keys) = ([], @_);
   my ($records, %name_prefixes, %name_suffixes) = ([], (), ());

   for my $iso_info (@{$isolate_keys}) {
      $name_prefixes{$iso_info->{name_prefix}} = 1;
      $name_suffixes{$iso_info->{name_suffix}} = 1;
   }

   #my $iso_id_str = join(q{, }, @iso_ids);
   my $prefixes = "name_prefix IN ('".join(q{', '}, keys %name_prefixes)."')";
   my $suffixes = "name_suffix IN (".join(q{, }, keys %name_suffixes).")";

   my $query_handle = $db_handle->prepare(q{
      SELECT name_prefix, name_suffix, pyroID, appliedRegion, wellID,
             pyroPrintedDate, sampleID, commonName, userName, hostID,
             nucleotide, pHeight
      FROM test_isolates JOIN
           test_pyroprints using (name_prefix, name_suffix) JOIN
           test_histograms using (pyroID)}.
      "WHERE $prefixes AND $suffixes ".
      "ORDER BY name_prefix, name_suffix, pyroID, position asc"
   );

   $query_handle->execute();
   $records = $query_handle->fetchall_arrayref({});

   my ($tmp_isolate, $tmp_pyroprint);
   for my $record (@{$records}) {

      if (!$tmp_isolate->{name_prefix} || !$tmp_isolate->{name_suffix} ||
          $tmp_isolate->{name_prefix} ne $record->{name_prefix} ||
          $tmp_isolate->{name_suffix} ne $record->{name_suffix}) {

         $tmp_isolate = BiologyTypes::Isolate->new($record);

         push(@{$isolates}, $tmp_isolate);
      }

      if (!$tmp_pyroprint->{pyroID} ||
          $tmp_pyroprint->{pyroID} ne $record->{pyroID}) {
         $tmp_pyroprint = BiologyTypes::Pyroprint->new($record);

         $tmp_isolate->add_pyroprint($record->{appliedRegion}, $tmp_pyroprint);
      }

      if (defined $tmp_pyroprint && $tmp_pyroprint) {
         $tmp_pyroprint->add_dispensation($record->{nucleotide},
                                          $record->{pHeight});
      }
   }

   return $isolates;
}

sub randomize_isolates {
   my ($new_isolates, $config, $isolates, $num_randomizations) = ([], @_);
   my ($num_isolates, $first_isolate, $second_isolate) = (scalar @{$isolates});

   print({*STDOUT} "num_randomizations: $num_randomizations\n");

   for my $num_generation (0..$num_randomizations) {
      $first_isolate = $isolates->[int(rand($num_isolates))];
      $second_isolate = $isolates->[int(rand($num_isolates))];
      my $new_isolate = undef;

      if (!$first_isolate->similar_protocol($second_isolate)) { next; }

      if ($first_isolate->get_name() eq $second_isolate->get_name()) {
         $new_isolate = $first_isolate->duplicate($config);
      }
      elsif (rand() < CROSSOVER_PROB) {
         $new_isolate = $first_isolate->crossover($config, $second_isolate);
      }
      elsif (int(rand())) { $new_isolate = $first_isolate->mutate($config); }
      else { $new_isolate = $second_isolate->mutate($config); }

      if ($new_isolate) {
         push(@{$new_isolates}, $new_isolate);
      }
   }

   return $new_isolates;
}

sub commit_random_data {
   my ($isolates) = (@_);
   my (@iso_strings, @pyro_strings, @hist_strings);

   for my $isolate (@{$isolates}) {
      push(@iso_strings, $isolate->bulk_insert_str());

      for my $region (keys %{$isolate->{pyroprints}}) {
         for my $pyroprint (@{$isolate->{pyroprints}->{$region}}) {
            push(@pyro_strings, $pyroprint->bulk_insert_str());

            for my $ndx (0..($pyroprint->{pyro_length} - 1)) {
               if (!$pyroprint->{peak_heights}->[$ndx]) {
                  last;
               }

               push(@hist_strings, "($pyroprint->{pyroID}, $ndx, ".
                                   "'$pyroprint->{dispensations}->[$ndx]', ".
                                   sprintf("%0.2f)", $pyroprint->{peak_heights}->[$ndx]));
            }
         }
      }
   }

   $db_handle->do(q{
      LOCK TABLES test_isolates WRITE, test_pyroprints WRITE,
                  test_histograms WRITE
   });

   my $iso_insert   = $db_handle->prepare(
      q{INSERT INTO test_isolates(}.
      BiologyTypes::Isolate->get_attributes().
      q{) VALUES }.
      join(q{, }, @iso_strings)
   );
   $iso_insert->execute();

   my $pyro_insert  = $db_handle->prepare(
      q{INSERT INTO test_pyroprints(}.
      BiologyTypes::Pyroprint->get_attributes().
      q{) VALUES }.
      join(q{, }, @pyro_strings)
   );
   $pyro_insert->execute();

   my $peaks_insert = $db_handle->prepare(
      q{INSERT INTO test_histograms(pyroID, position, nucleotide, pHeight) VALUES }.
      join(q{, }, @hist_strings)
   );
   $peaks_insert->execute();

   $db_handle->do(q{
      UNLOCK TABLES
   });
}

sub get_db_stats {
   my ($config) = (@_);

   $config->{name_map} = {};

   my $stats_query = $db_handle->prepare(q{
      SELECT name_prefix as prefix, max(name_suffix) as max_suffix
      FROM test_isolates
      GROUP BY name_prefix
   });

   $stats_query->execute();
   for my $stats_record (@{$stats_query->fetchall_arrayref({})}) {
      $config->{name_map}->{$stats_record->{prefix}} = $stats_record->{max_suffix};
   }

   # Update max pyro id
   my $stats_record = $db_handle->selectrow_hashref(q{
      SELECT max(pyroID) as max_pyro_id
      FROM test_pyroprints
   });

   $config->{max_pyro_id} = $stats_record->{max_pyro_id};
}

sub main {
   my ($config) = (@_);

   $db_handle->do(q{
      SET read_rnd_buffer_size=16777216
   });

   get_db_stats($config);

   for my $num_update (0..$config->{num_updates}) {
      print({*STDOUT} "${num_update}th iteration...\n");
      print({*STDOUT} "Elapsed time: ".(time() - $config->{start_time})." seconds\n");

      my $random_isolates = select_isolates($config->{size});
      print({*STDOUT} "Selected ".(scalar @{$random_isolates}).
                      " random isolates\n");

      my $isolates = get_isolate_info($random_isolates);
      print({*STDOUT} "got info for ".(scalar @{$isolates}).
                      " isolate records\n");

      my $new_isolates = randomize_isolates($config, $isolates,
                                            (scalar @{$random_isolates}));

      print({*STDOUT} "randomly generated ".(scalar @{$new_isolates}).
                      " isolate records\n");

      commit_random_data($new_isolates);
   }
}

main({
   size => 750,
   num_updates  => 1000,
   start_time   => time(),
});
