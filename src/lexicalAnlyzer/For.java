package lexicalAnlyzer;

class For extends Stmt {
    // For = Decl decl; Expr expr; Assignment assignment, Stmt stmt
    Decl decl;
    Expr expr;
    Assignment assignment;
    Stmt stmt;

    public For(Decl d, Expr e, Assignment a, Stmt s) {
        this.decl = d;
        this.expr = e;
        this.assignment = a;
        this.stmt = s;
    }
}


