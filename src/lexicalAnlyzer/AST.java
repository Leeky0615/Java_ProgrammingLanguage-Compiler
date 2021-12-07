package lexicalAnlyzer;


import java.util.ArrayList;

abstract class Command{
    // Command = Decl | Function | Stmt
    Type type = Type.UNDEFINED;
    public void display(int l){}
}

class Decls extends ArrayList<Decl> {
    // Decls = Decl *
}
class Decl extends Command{
    // Decl = Type type; Identifier id;
    Identifier id;
    Expr expr = null;

    Decl(String s, Type t) {
        id = new Identifier(s);type = t;
    } // declaration

    Decl(String s, Type t, Expr expr) {
        id = new Identifier(s); type = t; expr = e;
    } // declaration
}

class Functions extends ArrayList<Function> {
    // Functions = Function*
}

class Function extends Command {
    // Function = Type type; Identifier id; Decls params; Stmt stmt;
    Identifier id;
    Decls params;
    Stmt stmt;

    Function(String s, Type type) {
        id = new Identifier(s);Type t;params = null; stmt = null;
    }
    public String toString() {
        return id.toString();
    }
}