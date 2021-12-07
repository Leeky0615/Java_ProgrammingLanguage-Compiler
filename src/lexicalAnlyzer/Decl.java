package lexicalAnlyzer;

public class Decl{
    Type type;
    Identifier id;
    Expr expr;

    Decl(Type type, Identifier id, Expr expr) {
        this.type = type;
        this.id = id;
        this.expr = expr;
    }
}