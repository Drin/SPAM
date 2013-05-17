use strict;
use warnings;

use DBI;

my $dbi_handle = DBI->connect(q{dbi:mysql:CPLOP:localhost}, q{amontana},
   q{4ldr1n*(});

sub get_possible_values {
   my ($table, $independent, $attribute) = (@_);
   my $possible_values = {};

   my $query = $dbi_handle->prepare("
      SELECT distinct $independent, $attribute
      FROM $table
      WHERE $attribute != ''
   ");

   $query->execute();

   for my $record (@{$query->fetchall_arrayref({})}) {
      my ($key, $val) = ($record->{$independent}, $record->{$attribute});

      if (!$possible_values->{$key}) {
         $possible_values->{$key} = [];
      }

      push(@{$possible_values->{$key}},  $record->{$attribute});
   }

   return $possible_values;
}

sub get_empty_records {
   my ($table, $attribute) = (@_);

   my $query = $dbi_handle->prepare("
      SELECT name_prefix, name_suffix, $attribute
      FROM $table
      where $attribute = ''
   ");

   $query->execute();
   return $query->fetchall_arrayref({});
}

sub update_common_names {
   my $common_names = get_possible_values(q{test_isolates}, q{name_prefix},
                                          q{commonName});

   if ($ENV{DEBUG}) {
      print({*STDOUT} "common names ");

      for my $name_prefix (keys %{$common_names}) {
         print({*STDOUT} "for $name_prefix:\n");
         print({*STDOUT} "\t".join(q{, }, @{$common_names->{$name_prefix}})."\n");
      }
   }

   my $bulk_update = {};
   for my $record (@{get_empty_records(q{test_isolates}, q{commonName})}) {
      my $rand_pick = int(rand(scalar @{$common_names->{$record->{name_prefix}}}));
      my $rand_common_name = $common_names->{$record->{name_prefix}}->[$rand_pick];

      if ($ENV{DEBUG}) {
         print({*STDOUT} "$rand_pick : $rand_common_name\n");
         print({*STDOUT} "[$record->{name_prefix}, $record->{name_suffix}]: ".
                         "$rand_common_name\n");
      }

      if (!$bulk_update->{$rand_common_name}) {
         $bulk_update->{$rand_common_name} = [];
      }

      push(@{$bulk_update->{"$rand_common_name:$record->{name_prefix}"}},
           $record->{name_suffix});
   }

   for my $common_name_prefix (keys %{$bulk_update}) {
      if ($common_name_prefix =~ m/([a-zA-Z ]+):(\w+)/) {
         my ($common_name, $name_prefix) = ($1, $2);

         my $query = $dbi_handle->prepare("
            UPDATE test_isolates
            SET commonName = '$common_name'
            WHERE name_prefix = '$name_prefix' AND
                  name_suffix IN (".
                  join(q{, }, @{$bulk_update->{$common_name_prefix}}).
         ")");

         $query->execute();
      }
   }
}

update_common_names();
