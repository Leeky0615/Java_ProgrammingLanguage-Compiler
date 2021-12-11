let 
    int i = 0;
in 
    let
       int i = 1; int j = 2; 
    in
       print "<Before Command 'IF' Local Variable : 'i'>";
       print i;
       if (i>0)
          then i=i+j; 
          else i=i-j;
       print "<After Command 'IF' Local Variable : 'i'>";
       print i;
    end;

    let 
        int k = 3;
    in
        i = k;
    end;
    print "<Global Variable : 'i'>";
    print i;
end;
