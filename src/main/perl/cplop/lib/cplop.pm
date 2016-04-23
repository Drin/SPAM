package cplop;

use Dancer ':syntax';
use Dancer::Plugin::Ajax;
use Dancer::Plugin::Database;

our $VERSION = '0.1';

my $query_isolates = database('data')->prepare(<<SQL_QUERY
   SELECT isoID, commonName, hostID, sampleID, dateStored, pyroprintDate
   FROM Isolates
SQL_QUERY
);

get '/' => sub {
   template 'index';
};

ajax '/construct_ontology' => sub {
   my ($schema_hash) = ({});

   my @cplop_columns = database('schema')->quick_select(
      'COLUMNS',
      { TABLE_SCHEMA => 'cplop',
        TABLE_NAME => [qw{Isolates Pyroprints Host Samples}]},
      { columns => [qw(TABLE_NAME COLUMN_NAME)] }
   );

   for my $cplop_hash (@cplop_columns) {
      if (!$schema_hash->{$cplop_hash->{TABLE_NAME}}) {
         $schema_hash->{$cplop_hash->{TABLE_NAME}} = [];
      }

      push(@{$schema_hash->{$cplop_hash->{TABLE_NAME}}},
           $cplop_hash->{COLUMN_NAME});
   }

   print ({*STDERR} "cplop_schema\n\n");
   for my $table (keys %{$schema_hash}) {
      print({*STDERR} "$table:\n");

      for my $col (@{$schema_hash->{$table}}) {
         print({*STDERR} "\t$col\n");
      }
   }

   return $schema_hash;
};

ajax '/browse_data/:data' => sub {
   my $tuples = [];

   if (params->{data} =~ m/Isolates|Pyroprints/) {
      $query_isolates->execute();
      @{$tuples} = $query_isolates->fetchall_arrayref({});
   }

   return $tuples;
};

post '/' => sub {
   print({*STDOUT} "submitted parameters!\n");
};

true;
