// AST for S

import java.util.ArrayList;

abstract class Command {
    // Command = Decl | Function | Stmt
    Type type = Type.UNDEF;
    public void display(int l) {}
}

class Decls extends ArrayList<Decl> {
    // Decls = Decl*
    Decls() {super();}
    Decls(Decl d) {this.add(d);}
}

class Decl extends Command {
    // Decl = Type type; Identifier id
    Type type;
    Identifier id;
    Expr expr = null;
    int arraySize = 0;

    Decl(String s, Type t) {
        id = new Identifier(s);
        type = t;
    } // declaration 

    Decl(String s, Type t, int n) {
        id = new Identifier(s);
        type = t;
        arraySize = n;
    } // array declaration 

    Decl(String s, Type t, Expr e) {
        id = new Identifier(s);
        type = t;
        expr = e;
    } // declaration 
}

class Functions extends ArrayList<Function> {
    // Functions = Function*
}

class Function extends Command {
    // Function = Type type; Identifier id; Decls params; Stmt stmt
    Type type;
    Identifier id;
    Decls params;
    Stmt stmt;

    Function(String s, Type t) {
        id = new Identifier(s);
        type = t;
        params = null;
        stmt = null;
    }

    public String toString() {
        return id.toString() + params.toString();
    }
}

class Type {
    // Type = int | bool | string | fun | array | except | void
    final static Type INT = new Type("int");
    final static Type BOOL = new Type("bool");
    final static Type STRING = new Type("string");
    final static Type VOID = new Type("void");
    final static Type FUN = new Type("fun");
    final static Type ARRAY = new Type("array");
    final static Type EXC = new Type("exc");
    final static Type RAISEDEXC = new Type("raisedexc");
    final static Type UNDEF = new Type("undef");
    final static Type ERROR = new Type("error");

    protected String id;

    protected Type(String s) {
        id = s;
    }

    public String toString() {
        return id;
    }
}

class ProtoType extends Type {
    // defines the type of a function and its parameters
    Type result;
    Decls params;

    ProtoType(Type t, Decls ds) {
        super(t.id);
        result = t;
        params = ds;
    }
}

abstract class Stmt extends Command {
    // Stmt = Empty | Stmts | Assignment | If  | While | Let | Read | Print
}

class Empty extends Stmt {}

class Stmts extends Stmt {
    // Stmts = Stmt*
    public ArrayList<Stmt> stmts = new ArrayList<>();

    Stmts() {
        super();
    }

    Stmts(Stmt s) {
        stmts.add(s);
    }
}

// (3) Assignment AST Implementation
class Assignment extends Stmt {
    // Assignment = Identifier id; Expr expr
    Identifier id;
    Expr expr;

    Assignment(Identifier t, Expr e) {
        this.id = t;
        this.expr = e;
    }
}

// (4) If AST Implementation
class If extends Stmt {
    // If = Expr expr; Stmt stmt1, stmt2;
    Expr expr;
    Stmt stmt1, stmt2;

    public If(Expr e, Stmt s1, Stmt s2) {
        this.expr = e;
        this.stmt1 = s1;
        this.stmt2 = s2;
    }
}

// (5) While AST Implementation
class While extends Stmt {
    // While = Expr expr; Stmt stmt;
    Expr expr;
    Stmt stmt;

    public While(Expr e, Stmt s) {
        this.expr = e;
        this.stmt = s;
    }
}

// (6) Let AST Implementation
class Let extends Stmt {
    // Let = Decls decls; Functions funs; Stmts stmts;
    Decls decls;
    Functions funs;
    Stmts stmts;

    public Let(Decls decls, Functions funs, Stmts stmts) {
        this.decls = decls;
        this.funs = funs;
        this.stmts = stmts;
    }
}

// (7) Read AST Implementation
class Read extends Stmt {
    // Read = Identifier id
    Identifier id;
    public Read(Identifier id) {this.id = id;}
}

// (8) Print AST Implementation
class Print extends Stmt {
    // Print =  Expr expr
    Expr expr;
    public Print(Expr e) {this.expr = e;}
}
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

class Return extends Stmt {
    Identifier fid;
    Expr expr;

    Return(String s, Expr e) {
        fid = new Identifier(s);
        expr = e;
    }
}

class Try extends Stmt {
    // Try = Identifier id; Stmt stmt1; Stmt stmt2; 
    Identifier eid;
    Stmt stmt1;
    Stmt stmt2;

    Try(Identifier id, Stmt s1, Stmt s2) {
        eid = id;
        stmt1 = s1;
        stmt2 = s2;
    }
}

class Raise extends Stmt {
    Identifier eid;

    Raise(Identifier id) {
        eid = id;
    }
}

class Exprs extends ArrayList<Expr> {
    // Exprs = Expr*
}

abstract class Expr extends Stmt {
    // Expr = Identifier | Value | Binary | Unary | Call

}

class Call extends Expr {
    Identifier fid;
    Exprs args;

    Call(Identifier id, Exprs a) {
        fid = id;
        args = a;
    }
}

class Identifier extends Expr {
    // Identifier = String id
    private String id;

    Identifier(String s) {id = s;}

    public String toString() {return id;}

    public boolean equals(Object obj) {
        String s = ((Identifier) obj).id;
        return id.equals(s);
    }
}

class Array extends Expr {
    // Array = Identifier id; Expr expr
    Identifier id;
    Expr expr = null;

    Array(Identifier s, Expr e) {id = s; expr = e;}

    public String toString( ) { return id.toString(); }

    public boolean equals (Object obj) {
        String s = ((Array) obj).id.toString();
        return id.equals(s);
    }
}


class Value extends Expr {
    // Value = int | bool | string | array | function 
    protected boolean undef = true;
    Object value = null; // Type type;

    Value(Type t) {
        type = t;
        if (type == Type.INT) value = new Integer(0);
        if (type == Type.BOOL) value = new Boolean(false);
        if (type == Type.STRING) value = "";
        undef = false;
    }

    Value(Object v) {
        if (v instanceof Integer) type = Type.INT;
        if (v instanceof Boolean) type = Type.BOOL;
        if (v instanceof String) type = Type.STRING;
        if (v instanceof Function) type = Type.FUN;
        if (v instanceof Value[]) type = Type.ARRAY;
        value = v;
        undef = false;
    }

    Object value() {
        return value;
    }

    int intValue() {
        if (value instanceof Integer)
            return ((Integer) value).intValue();
        else return 0;
    }

    boolean boolValue() {
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue();
        else return false;
    }

    String stringValue() {
        if (value instanceof String)
            return (String) value;
        else return "";
    }

    Function funValue() {
        if (value instanceof Function)
            return (Function) value;
        else return null;
    }

    Value[] arrValue() {
        if (value instanceof Value[])
            return (Value[]) value;
        else return null;
    }

    Type type() {
        return type;
    }

    public String toString() {
        //if (undef) return "undef";
        if (type == Type.INT) return "" + intValue();
        if (type == Type.BOOL) return "" + boolValue();
        if (type == Type.STRING) return "" + stringValue();
        if (type == Type.FUN) return "" + funValue();
        if (type == Type.ARRAY) return "" + arrValue();
        return "undef";
    }
}

// (1) Binary AST Implementation
class Binary extends Expr {
    // Binary = Operator op; Expr expr1; Expr expr2;
    Operator op;
    Expr expr1, expr2;

    Binary(Operator o, Expr e1, Expr e2) {
        this.op = o;
        this.expr1 = e1;
        this.expr2 = e2;
    }
}

// (2) Unary AST Implementation
class Unary extends Expr {
    // Unary = Operator op; Expr expr
    Operator op;
    Expr expr;

    public Unary(Operator o, Expr e) {
        this.op = o;
        this.expr = e;
    }
}

class Operator {
    String val;

    Operator(String s) {
        val = s;
    }

    public String toString() {
        return val;
    }

    public boolean equals(Object obj) {
        return val.equals(obj);
    }
}
