use strict;
use warnings;

use DBI;

use constant DB_HANDLE => DBI->connect(q{dbi:mysql:CPLOP});

sub test {
   my $records = DB_HANDLE->selectall_arrayref(
      'SELECT isoID, pyroPrintedDate, date_pyroPrintedDate '.
      'FROM Pyroprints',
      { Slice => {}, MaxRows => 10 }
   );

   print("records:\n\n");
   for my $col (keys %{$records->[0]}) {
      print("$col\t");
   }

   print("\n");
   for my $record (@{$records}) {
      for my $col (keys %{$record}) {
         if ($record->{$col}) {
            print("$record->{$col}\t");
         }
         else { print("-\t"); }
      }

      print("\n");
   }

   return $records;
}

test();
