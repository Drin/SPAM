package TestRun;

use strict;
use warnings;

sub new {
   my ($class, $params) = (@_);

   return bless({
      run_id       => $params->{run_id},
      data_size    => $params->{data_size},
      num_clusters => 0,
      clusters     => $params->{clusters} || [],
   }, $class);
}

sub add_cluster {
   my ($self, $new_clust) = (@_);

   push(@{$self->{clusters}}, $new_clust);
   $self->{num_clusters}++;
}

sub get_clusters {
   my ($self) = (@_);

   return $self->{clusters};
}

sub get_cluster {
   my ($self, $clust_id) = (@_);

   for my $clust (@{$self->{clusters}}) {
      if ($clust->{id} == $clust_id) {
         return $clust;
      }
   }

   return undef;
}

sub calc_clust_prob {
   my ($self, $clust) = (@_);

   return ($clust->{size} / $self->{data_size});
}

sub to_string {
   my ($self) = (@_);
   my $str = "test run $self->{run_id}:\n";

   for my $cluster (@{$self->{clusters}}) {
      $str .= $cluster->to_string()."\n";
   }

   return $str;
}

1;

__END__
