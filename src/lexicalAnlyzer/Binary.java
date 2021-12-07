package lexicalAnlyzer;

public class Binary extends Expr{
    Operator op;
    Expr expr1;
    Expr expr2;

    Binary(Operator op, Expr expr1, Expr expr2) {
        this.op = op;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
}
