package BiologyTypes::Isolate;

use strict;
use warnings;

my @ISOLATE_ATTRS = qw{name_prefix name_suffix sampleID commonName userName
                       hostID is_generated};

sub new {
   my ($class, $params) = (@_);

   my $self = {
      pyroprints  => {},
      num_pyros   => 0,
   };

   for my $attr (@ISOLATE_ATTRS) {
      $self->{$attr} = $params->{$attr};
   }

   return bless($self, $class);
}

sub add_pyroprint {
   my ($self, $region, $pyroprint) = (@_);

   if (!$self->{pyroprints}->{$region}) {
      $self->{pyroprints}->{$region} = [];
   }

   push(@{$self->{pyroprints}->{$region}}, $pyroprint);
   $self->{num_pyros}++;
}

sub get_name {
   my ($self) = (@_);

   return "$self->{name_prefix}-$self->{name_suffix}";
}

sub similar_protocol {
   my ($is_same, $self, $other_iso) = (1, @_);
   my ($pyro_1, $pyro_2);

   for my $region (keys %{$self->{pyroprints}}) {
      if (!$other_iso->{pyroprints}->{$region}) {
         $is_same = 0;
         last;
      }

      $pyro_1 = $self->{pyroprints}->{$region}->[0];
      $pyro_2 = $other_iso->{pyroprints}->{$region}->[0];

      if (!$pyro_1->similar_protocol($pyro_2)) {
         $is_same = 0;
         last;
      }
   }

   return $is_same;
}

sub duplicate {
   my ($self, $config) = (@_);

   my $clone = {
      name_prefix => $self->{name_prefix},
      name_suffix => (++$config->{name_map}->{$self->{name_prefix}}),
      pyroprints  => {},
      num_pyros   => $self->{num_pyros},
      parent      => "$self->{name_prefix}-$self->{name_suffix}",
   };

   for my $region (keys %{$self->{pyroprints}}) {
      $clone->{pyroprints}->{$region} = [];

      for my $pyroprint (@{$self->{pyroprints}->{$region}}) {
         push (@{$clone->{pyroprints}->{$region}},
               $pyroprint->duplicate($config, $clone->{name_prefix}, $clone->{name_suffix}));
      }
   }

   $clone->{sampleID} = $self->{sampleID};
   $clone->{commonName} = $self->{commonName};
   $clone->{userName} = $self->{userName};
   $clone->{hostID} = $self->{hostID};
   $clone->{is_generated} = 1;

   return bless($clone);
}

sub crossover {
   my ($self, $config, $other_iso) = (@_);
   my $clone = $self->duplicate($config);
   my $is_double_cross = (rand() < 0.25);
   my ($cross_loc_1, $cross_loc_2, $other_pyro_ndx, $other_pyro);

   for my $region (keys %{$clone->{pyroprints}}) {
      for my $pyroprint (@{$clone->{pyroprints}->{$region}}) {
         $other_pyro_ndx = int(rand($#{$other_iso->{pyroprints}->{$region}}));
         $other_pyro = $other_iso->{pyroprints}->{$region}->[$other_pyro_ndx];
         $cross_loc_2 = $pyroprint->{pyro_length};
         $cross_loc_1 = $pyroprint->{pyro_length} -
                        int(rand($pyroprint->{pyro_length} / 4));

         if ($is_double_cross) {
            $cross_loc_2 = int(rand($pyroprint->{pyro_length} / 4)) +
                           ($pyroprint->{pyro_length} / 4);
         }

         $pyroprint->crossover($config, $other_pyro,
                               ($pyroprint->{pyro_length} / 4),
                               $cross_loc_1, $cross_loc_2);
      }
   }

   return $clone;
}

sub mutate {
   my ($self, $config) = (@_);
   my $clone = $self->duplicate($config);
   my ($pyro_ndx, $mutated_pyro_ndx) = (0, int(rand($clone->{num_pyros} - 1)));

   for my $region (keys %{$clone->{pyroprints}}) {
      for my $pyroprint (@{$clone->{pyroprints}->{$region}}) {
         if ($pyro_ndx == $mutated_pyro_ndx) {
            $pyroprint->mutate($config);
         }

         if (++$pyro_ndx > $mutated_pyro_ndx) { last; }
      }
   }

   return $clone;
}

sub to_string {
   my ($self, $prefix) = (@_);

   my $str = "${prefix}Isolate [$self->{name_prefix}-$self->{name_suffix}]:\n";

   for my $region (keys %{$self->{pyroprints}}) {
      $str .= "${prefix}Pyroprints of region $region:\n";

      for my $pyroprint (@{$self->{pyroprints}->{$region}}) {
         $str .= $pyroprint->to_string("\t$prefix")."\n";
      }
   }

   return $str;
}

sub get_attributes {
   my ($class) = (@_);

   return join(q{, }, @ISOLATE_ATTRS);
}

sub bulk_insert_str {
   my ($str, $self) = (q{}, @_);

   $str .= "'$self->{name_prefix}', ";
   $str .= "$self->{name_suffix}, ";
   $str .= "$self->{sampleID}, ";
   $str .= "'$self->{commonName}', ";
   $str .= "'$self->{userName}', ";
   $str .= "'$self->{hostID}', ";
   $str .= "$self->{is_generated}";

   return "($str)";
}

sub debug_info {
   my ($self) = (@_);

   print({*STDOUT} "Isolate [$self->{name_prefix}-$self->{name_suffix}]".
                   " has $self->{num_pyros} pyroprints\n");

   if ($self->{parent}) {
      print({*STDOUT} "\tparent isolate was [$self->{parent}]\n");
   }

#   print({*STDOUT} $self->to_string("\t")."\n");
}

1;
__END__
