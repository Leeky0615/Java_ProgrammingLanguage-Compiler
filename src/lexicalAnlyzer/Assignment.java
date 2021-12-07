package lexicalAnlyzer;

public class Assignment extends Stmt {
    Identifier id;
    Expr expr;

    Assignment(Identifier t, Expr e) {
        this.id = t;
        this.expr = e;
    }
}
