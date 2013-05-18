package Cluster;

use strict;
use warnings;

sub new {
   my ($class, $params) = (@_);

   return bless({
      id       => $params->{id},
      size     => $params->{size},
      isolates => [],
   }, $class);
}

sub add_isolate {
   my ($self, $new_isolate) = (@_);

   push(@{$self->{isolates}}, $new_isolate);
   $self->{size}++;
}

sub get_overlap {
   my ($count, $self, $other_cluster) = (0, @_);

   for my $iso_A (@{$self->{isolates}}) {
      for my $iso_B (@{$other_cluster->{isolates}}) {
         if ($iso_A == $iso_B) { $count++; }
      }
   }

   return $count;
}

sub to_string {
   my ($line_limit, $self) = (10, @_);

   my $str = "cluster #$self->{id} [$self->{size}]:\n";
   my $curr_ndx = 1;
   for my $iso_id (@{$self->{isolates}}) {
      $str .= "\t$iso_id";

      if (!($curr_ndx++ % $line_limit)) {
         $str .= "\n";
         $curr_ndx = 1;
      }
   }

   return $str;
}

1;

__END__
