let
   int i = 1; int sum = 0; int n;
in
   print "1 + 2 + ... + n?";
   print "<INPUT 'n'>";
   read n;
   while (i <= n) {
       sum = sum + i;
       i = i + 1;
   }
   print "<RESULT 'sum'>";
   print sum;
end;
