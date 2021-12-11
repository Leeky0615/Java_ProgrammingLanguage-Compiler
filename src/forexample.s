let
   int sum = 0; int n;
in
   print "1 + 2 + ... + n?";
   print "<INPUT 'n'>";
   read n;
   for (int i = 1; i < n + 1; i = i + 1;) {
       sum = sum + i;
   }
   print "<RESULT 'sum'>";
   print sum;
end;