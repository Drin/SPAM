package BiologyTypes::Pyroprint;

use strict;
use warnings;

my @PYROPRINT_ATTRS = qw{pyroID name_prefix name_suffix appliedRegion wellID
                         pyroPrintedDate is_generated};

sub new {
   my ($class, $params) = (@_);

   my $self = {
      pyro_length   => 0,
      dispensations => [],
      peak_heights  => [],
   };

   for my $attr (@PYROPRINT_ATTRS) {
      $self->{$attr} = $params->{$attr};
   }

   return bless ($self, $class);
}

sub add_dispensation {
   my ($self, $dispensation, $peak_height) = (@_);

   $self->{pyro_length}++;
   push(@{$self->{dispensations}}, $dispensation);
   push(@{$self->{peak_heights}}, $peak_height);
}

sub similar_protocol {
   my ($self, $other_pyro) = (@_);

   return (join(q{}, @{$self->{dispensations}}) eq
           join(q{}, @{$other_pyro->{dispensations}}));
}

sub duplicate {
   my ($self, $config) = (@_);

   my $clone = {
      pyroID        => (++$config->{max_pyro_id}),
      pyro_length   => $self->{pyro_length},
      dispensations => [],
      peak_heights  => [],
   };

   for my $ndx (0..($self->{pyro_length} - 1)) {
      $clone->{dispensations}->[$ndx] = $self->{dispensations}->[$ndx];
      $clone->{peak_heights}->[$ndx] = $self->{peak_heights}->[$ndx];
   }

   $clone->{name_prefix} = $self->{name_prefix};
   $clone->{name_suffix} = $self->{name_suffix};
   $clone->{appliedRegion} = $self->{appliedRegion};
   $clone->{wellID} = $self->{wellID};
   $clone->{pyroPrintedDate} = $self->{pyroPrintedDate};
   $clone->{is_generated} = 1;

   return bless($clone);
}

sub crossover {
   my ($self, $config, $other_pyro, $cross_len, $cross_1, $cross_2) = (@_);

   for my $pyro_ndx ($cross_1..($self->{pyro_length} - 1)) {
      $self->{peak_heights}->[$pyro_ndx] = $other_pyro->{peak_heights}->[$pyro_ndx];
   }

   for my $pyro_ndx (0..$cross_len) {
      $self->{peak_heights}->[$cross_2 + $pyro_ndx] =
       $other_pyro->{peak_heights}->[$cross_2 + $pyro_ndx];
   }
}

sub mutate {
   my ($self, $config) = (@_);

   for my $ndx(0..($self->{pyro_length} - 1)) {
      $self->{peak_heights}->[$ndx] *= (1.15 - rand(0.30));
   }
}

sub to_string {
   my ($self, $prefix) = (@_);

   my $str = "${prefix}Pyroprint [$self->{pyroID}]:\n";
   $str .= $prefix.join(q{},   @{$self->{dispensations}})."\n";
   $str .= $prefix.join(q{, }, @{$self->{peak_heights}}) ."\n";

   return $str;
}

sub get_attributes {
   my ($class) = (@_);

   return join(q{, }, @PYROPRINT_ATTRS);
}

sub bulk_insert_str {
   my ($str, $self) = (q{}, @_);

   $str .= "$self->{pyroID}, ";
   $str .= "'$self->{name_prefix}', ";
   $str .= "$self->{name_suffix}, ";
   $str .= "'$self->{appliedRegion}', ";
   $str .= "'$self->{wellID}', ";
   $str .= "'$self->{pyroPrintedDate}', ";
   $str .= "$self->{is_generated}";

   return "($str)";
}

1;
__END__
