// Parser for language S

import static Token.*;

public class Parser {
    Token token;          // current token 
    Lexer lexer;
    String funId = "";

    public Parser(Lexer scan) {
        lexer = scan;
        token = lexer.getToken(); // get the first token
    }

    private String match(Token t) {
        String value = token.value();
        if (token == t)
            token = lexer.getToken();
        else
            error(t);
        return value;
    }

    private void error(Token tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token = lexer.getToken();
    }

    private void error(String tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token = lexer.getToken();
    }

    public Command command() {
        // <command> ->  <decl> | <function> | <stmt>
        if (isType()) {
            Decl d = decl();
            return d;
        }
        if (token == FUN) {
            Function f = function();
            return f;
        }
        if (token != EOF) {
            Stmt s = stmt();
            return s;
        }
        return null;
    }

    private Decl decl() {
        // <decl>  -> <type> id [=<expr>];
        Type t = type();
        String id = match(ID);
        Decl d = null;
        if (token == LBRACKET) { // array
            match(LBRACKET);
            Value v = literal();
            d = new Decl(id, t, v.intValue());
            match(RBRACKET);
        } else if (token == ASSIGN) {
            match(ASSIGN);
            Expr e = expr();
            d = new Decl(id, t, e);
        } else
            d = new Decl(id, t);
        match(SEMICOLON);
        return d;
    }

    private Decls decls() {
        // <decls> -> {<decl>}
        Decls ds = new Decls();
        while (isType()) {
            Decl d = decl();
            ds.add(d);
        }
        return ds;
    }

    private Functions functions() {
        // <functions> -> { <function> }
        Functions fs = new Functions();
        while (token == FUN) {
            Function f = function();
            fs.add(f);
        }
        return fs;
    }

    private Function function() {
        // <function>  -> fun <type> id(<params>) <stmt>
        match(FUN);
        Type t = type();
        String str = match(ID);
        funId = str;
        Function f = new Function(str, t);
        match(LPAREN);
        if (token != RPAREN) f.params = params();
        match(RPAREN);
        Stmt s = stmt();
        f.stmt = s;
        return f;
    }

    private Decls params() {
        Decls params = new Decls();
        Type t = type();
        String id = match(ID);
        params.add(new Decl(id, t));
        while (token == COMMA) {
            match(COMMA);
            t = type();
            id = match(ID);
            params.add(new Decl(id, t));
        }
        return params;
    }

    private Type type() {
        // <type>  ->  int | bool | void | string | exc
        Type t = null;
        switch (token) {
            case INT:
                t = Type.INT;
                break;
            case BOOL:
                t = Type.BOOL;
                break;
            case VOID:
                t = Type.VOID;
                break;
            case STRING:
                t = Type.STRING;
                break;
            case EXC:
                t = Type.EXC;
                break;
            default:
                error("int | bool | void | string | exc");
        }
        match(token);
        return t;
    }

    private Stmt stmt() {
        // <stmt> -> <block> | <assignment> | <ifStmt> | <whileStmt> | ...
        Stmt s = new Empty();
        switch (token) {
            case SEMICOLON:
                match(token.SEMICOLON);
                return s;
            case LBRACE:
                match(LBRACE);
                s = stmts();
                match(RBRACE);
                return s;
            case IF:    // if statement
                s = ifStmt();
                return s;
            case WHILE:      // while statement
                s = whileStmt();
                return s;
            case DO:      // do statement
                s = doStmt();
                return s;
            case FOR:      // for statement (for implementation)
                s = forStmt();
                return s;
            case ID:    // assignment
                s = assignment();
                return s;
            case LET:    // let statement
                s = letStmt();
                return s;
            case READ:    // read statement
                s = readStmt();
                return s;
            case PRINT:    // print statment
                s = printStmt();
                return s;
            case RETURN:    // return statement
                s = returnStmt();
                return s;
            case TRY:    // try statement
                s = tryStmt();
                return s;
            case RAISE:    // raise statement
                s = raiseStmt();
                return s;
            default:
                error("Illegal stmt");
                return null;
        }
    }

    private Stmts stmts() {
        // <stmts> -> {<stmt>}
        Stmts ss = new Stmts();
        while ((token != RBRACE) && (token != END))
            ss.stmts.add(stmt());
        return ss;
    }


    //(6) Let Parser Implementation
    private Let letStmt() {
        // <letStmt> -> let <decls> in <block> end
        // Let Implementation
        match(LET);
        Decls ds = decls();
        match(IN);
        Stmts ss = stmts();
        match(END);
        match(SEMICOLON);
        return new Let(ds, null, ss);
    }

    //(7) Read Parser Implementation
    private Read readStmt() {
        // <readStmt> -> read id;
        match(READ);
        Identifier id = new Identifier(match(ID));
        return new Read(id);
    }

    //(8) Print Parser Implementation
    private Print printStmt() {
        // <printStmt> -> print <expr>;
        // Print Implementation
        match(PRINT);
        Expr e = expr();
        match(SEMICOLON);
        return new Print(e);
    }

    private Return returnStmt() {
        // <returnStmt> -> return <expr>;
        match(RETURN);
        Expr e = expr();
        match(SEMICOLON);
        return new Return(funId, e);
    }


    // (3) Assignment Parser Implementation
    private Stmt assignment() {
        // <assignment> -> id = <expr>;
        Identifier id = new Identifier(match(ID));
        match(ASSIGN);
        Expr e = expr();
        match(SEMICOLON);
        return new Assignment(id, e);
    }


    private Call call(Identifier id) {
        // <call> -> id(<expr>{,<expr>});
        match(LPAREN);
        Call c = new Call(id, arguments());
        match(RPAREN);
        match(SEMICOLON);
        return c;
    }


    // (4) If Parser Implementation
    private If ifStmt() {
        // <ifStmt> -> if (<expr>) then <stmt> [else <stmt>]
        match(IF);
        match(LPAREN);
        Expr e = expr();
        match(RPAREN);
        Stmt s1 = stmts();
        Stmt s2 = new Empty();
        if (token == ELSE) {
            match(ELSE);
            s2 = stmt();
        }
        return new If(e, s1, s2);
    }

    // (5) While Parser Implementation
    private While whileStmt() {
        // <whileStmt> -> while (<expr>) <stmt>
        match(WHILE);
        match(LPAREN);
        Expr e = expr();
        match(RPAREN);
        Stmt s = stmts();
        return new While(e,s);
    }


    private Stmts doStmt() {
        // <whileStmt> -> do <stmt> while (<expr>)
        match(DO);
        Stmt s = stmt();
        match(WHILE);
        match(LPAREN);
        Expr e = expr();
        match(RPAREN);
        Stmts ss = new Stmts(s);
        ss.stmts.add(new While(e, s));
        return ss;
    }

    private Try tryStmt() {
        // <tryStmt> -> try <stmt> {catch(id) <stmt>}
        match(TRY);
        Stmt s1 = stmt();
        match(CATCH);
        match(LPAREN);
        Identifier id = new Identifier(match(ID));
        match(RPAREN);
        Stmt s2 = stmt();
        s1 = new Try(id, s1, s2);
        while (token == CATCH) {
            match(CATCH);
            match(LPAREN);
            id = new Identifier(match(ID));
            match(RPAREN);
            s2 = stmt();
            s1 = new Try(id, s1, s2);
        }
        return (Try) s1; // new Try(id, s1, s2);
    }

    private Raise raiseStmt() {
        // <raiseStmt> -> raise id;
        match(RAISE);
        Identifier id = new Identifier(match(ID));
        match(SEMICOLON);
        return new Raise(id);
    }
    private For forStmt() {
        // <forStmt> -> Decl decl; Expr expr; Assignment assignment; Stmt stmt;
        match(FOR);
        match(LPAREN);
        Decl decl = decl();
        match(SEMICOLON);
        Expr expr1 = expr();
        match(SEMICOLON);
        Expr expr2 = expr();
        match(RPAREN);
        Stmt stmt = stmt();
        return null;
    }

    private Expr expr() {
        // <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false
        switch (token) {
            case NOT:
                Operator op = new Operator(match(token));
                Expr e = expr();
                return new Unary(op, e);
            case TRUE:
                match(TRUE);
                return new Value(true);
            case FALSE:
                match(FALSE);
                return new Value(false);
        }

        Expr e = bexp();
        while (token == AND || token == OR) {
            Operator op = new Operator(match(token));
            Expr b = bexp();
            e = new Binary(op, e, b);
        }
        return e;
    }

    private Expr bexp() {
        // <bexp> -> <aexp> [ (< | <= | > | >= | == | !=) <aexp> ]
        Expr e = aexp();
        switch (token) {
            case LT:
            case LTEQ:
            case GT:
            case GTEQ:
            case EQUAL:
            case NOTEQ:
                Operator op = new Operator(match(token));
                Expr a = aexp();
                e = new Binary(op, e, a);
        }
        return e;
    }


    // (1) Expr Implementation 1 (aexp -> "+" & "-")
    private Expr aexp() {
        // <aexp> -> <term> { + <term> | - <term> }
        // aexp implementation
        Expr e = term(); // 첫번째 항(term) 파싱
        while (token == PLUS || token == MINUS) {  // + or -
            Operator op = new Operator(match(token));  // 연산자 매치
            Expr t = term(); // 다음 항(term) 파싱
            e = new Binary(op, e, t); // 수식 AST 구성
        }
        return e;  // 수식 AST 리턴
    }

    // (2) Expr Implementation 2 (term -> "*" & "/")
    private Expr term() {
        // <term> -> <factor> { * <factor> | / <factor>}
        Expr t = factor();  // 첫번째 인수 파싱
        while (token == MULTIPLY || token == DIVIDE) {
            Operator op = new Operator(match(token));  // 연산자 매칭
            Expr f = factor();  // 두번째 인수 파싱
            t = new Binary(op, t, f); // 항의 AST 구성
        }
        return t;  // 항의 AST 리턴
    }


    private Expr factor() {
        // <factor> -> [-](id | <call> | literal | '('<aexp> ')')
        Operator op = null;
        if (token == MINUS)
            op = new Operator(match(MINUS));

        Expr e = null;
        switch (token) {
            case ID:
                Identifier v = new Identifier(match(ID));
                e = v;
                if (token == LPAREN) {  // function call
                    match(LPAREN);
                    Call c = new Call(v, arguments());
                    match(RPAREN);
                    e = c;
                } else if (token == LBRACKET) {  // array
                    match(LBRACKET);
                    Array a = new Array(v, expr());
                    match(RBRACKET);
                    e = a;
                }

                break;
            case NUMBER: case STRLITERAL:
                e = literal(); break;
            case LPAREN:
                match(LPAREN);
                e = aexp();
                match(RPAREN);
                break;
            default:
                error("Identifier | Literal");
        }

        if (op != null) return new Unary(op, e);
        else return e;
    }

    private Exprs arguments() {
        // arguments -> [ <expr> {, <expr> } ]
        Exprs es = new Exprs();
        while (token != RPAREN) {
            es.add(expr());
            if (token == COMMA)
                match(COMMA);
            else if (token != RPAREN)
                error("Exprs");
        }
        return es;
    }

    private Value literal() {
        String s = null;
        switch (token) {
            case NUMBER:
                s = match(NUMBER);
                return new Value(Integer.parseInt(s));
            case STRLITERAL:
                s = match(STRLITERAL);
                return new Value(s);
        }
        throw new IllegalArgumentException("no literal");
    }

    private boolean isType() {
        switch (token) {
            case INT:
            case BOOL:
            case STRING:
            case EXC:
                return true;
            default:
                return false;
        }
    }

    public static void main(String args[]) {
        Parser parser;
        if (args.length == 0) {
            System.out.println("Begin parsing... ");
            System.out.print(">> ");
            Lexer.interactive = true;
            parser = new Parser(new Lexer());
            do {
                if (parser.token == EOF) {
                    parser.token = parser.lexer.getToken();
                }
                Command command = null;
                try {
                    command = parser.command();
                } catch (Exception e) {
                    System.err.println(e);
                }
                System.out.print("\n>> ");
            } while (true);
        } else {
            System.out.println("Begin parsing... " + args[0]);
            parser = new Parser(new Lexer(args[0]));
            Command command = null;
            do {
                if (parser.token == EOF)
                    break;

                try {
                    command = parser.command();
                } catch (Exception e) {
                    System.err.println(e);
                }
            } while (command != null);
        }
    } //main
} // Parser