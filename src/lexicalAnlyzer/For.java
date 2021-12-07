package lexicalAnlyzer;

public class For {
    Decl decl;
    Expr expr;
    Assignment assignment;
    Stmt stmt;

    public For(Decl decl, Expr expr, Assignment assignment, Stmt stmt) {
        this.decl = decl;
        this.expr = expr;
        this.assignment = assignment;
        this.stmt = stmt;
    }
}


