// Parser for language S
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
        if (token == t) token = lexer.getToken();
        else error(t);
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
        if (token == Token.FUN) {
            Function f = function();
            return f;
        }
        if (token != Token.EOF) {
            Stmt s = stmt();
            return s;
        }
        return null;
    }

    private Decl decl() {
        // <decl>  -> <type> id [=<expr>];
        Type t = type();
        String id = match(Token.ID);
        Decl d = null;
        if (token == Token.LBRACKET) { // array
            match(Token.LBRACKET);
            Value v = literal();
            d = new Decl(id, t, v.intValue());
            match(Token.RBRACKET);
        } else if (token == Token.ASSIGN) {
            match(Token.ASSIGN);
            Expr e = expr();
            d = new Decl(id, t, e);
        } else
            d = new Decl(id, t);
        match(Token.SEMICOLON);
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
        while (token == Token.FUN) {
            Function f = function();
            fs.add(f);
        }
        return fs;
    }

    private Function function() {
        // <function>  -> fun <type> id(<params>) <stmt>
        match(Token.FUN);
        Type t = type();
        String str = match(Token.ID);
        funId = str;
        Function f = new Function(str, t);
        match(Token.LPAREN);
        if (token != Token.RPAREN) f.params = params();
        match(Token.RPAREN);
        Stmt s = stmt();
        f.stmt = s;
        return f;
    }

    private Decls params() {
        Decls params = new Decls();
        Type t = type();
        String id = match(Token.ID);
        params.add(new Decl(id, t));
        while (token == Token.COMMA) {
            match(Token.COMMA);
            t = type();
            id = match(Token.ID);
            params.add(new Decl(id, t));
        }
        return params;
    }

    private Type type() {
        // <type>  ->  int | bool | void | string | exc
        Type t = null;
        switch (token) {
            case INT: t = Type.INT; break;
            case BOOL: t = Type.BOOL; break;
            case VOID: t = Type.VOID; break;
            case STRING: t = Type.STRING; break;
            case EXC: t = Type.EXC; break;
            default: error("int | bool | void | string | exc");
        }
        match(token);
        return t;
    }

    private Stmt stmt() {
        // <stmt> -> <block> | <assignment> | <ifStmt> | <whileStmt> | ...
        Stmt s = new Empty();
        switch (token) {
            case SEMICOLON: match(Token.SEMICOLON); return s;
            case LBRACE:
                match(Token.LBRACE);
                s = stmts();
                match(Token.RBRACE);
                return s;
            case IF:    // if statement
                s = ifStmt(); return s;
            case WHILE:      // while statement
                s = whileStmt(); return s;
            case DO:      // do statement
                s = doStmt(); return s;
            case FOR:      // for statement
                s = forStmt(); return s;
            case ID:    // assignment
                s = assignment(); return s;
            case LET:    // let statement
                s = letStmt(); return s;
            case READ:    // read statement
                s = readStmt(); return s;
            case PRINT:    // print statment
                s = printStmt(); return s;
            case RETURN:    // return statement
                s = returnStmt(); return s;
            case TRY:    // try statement
                s = tryStmt(); return s;
            case RAISE:    // raise statement
                s = raiseStmt(); return s;
            default: error("Illegal stmt"); return null;
        }
    }

    private Stmts stmts() {
        // <stmts> -> {<stmt>}
        Stmts ss = new Stmts();
        while ((token != Token.RBRACE) && (token != Token.END))
            ss.stmts.add(stmt());
        return ss;
    }

    /**
     * (6) Let Parser Implementation
     * Syntax : <letStmt> -> let <decls> in <block> end
     * @return Let.class ['new Let(ds, null, ss)']
     */
    private Let letStmt() {
        match(Token.LET); // 'let' 토큰 매칭
        Decls ds = decls(); // let 문에서 사용될 변수 선언을 위해 decls 파싱
        match(Token.IN); // 'in' 토큰 매칭
        Stmts ss = stmts(); // let 문 안에서 사용될 stmts 파싱
        match(Token.END); // 'end' 토큰 매칭
        match(Token.SEMICOLON); // ';' 토큰 매칭
        return new Let(ds, null, ss); // Let AST 리턴
    }

    /**
     * (7) Read Parser Implementation
     * Syntax : <readStmt> -> read id
     * @return Read.class ['new Read(id)']
     */
    private Read readStmt() {
        match(Token.READ); // 'read' 토큰 매칭
        Identifier id = new Identifier(match(Token.ID)); // 읽을 ID 생성 ('id' 토큰 매칭)
        match(Token.SEMICOLON); // ';' 토큰 매칭
        return new Read(id); // 읽을 id를 담아 Read AST 리턴
    }

    /**
     * (8) Print Parser Implementation
     * Syntax : <printStmt> -> print <expr>
     * @return Print.class ['new Print(e)']
     */
    private Print printStmt() {
        match(Token.PRINT); // 'print' 토큰 매칭
        Expr e = expr(); // print 할 expr 파싱
        match(Token.SEMICOLON); // ';' 토큰 매칭
        return new Print(e); // Print AST 리턴
    }

    private Return returnStmt() {
        // <returnStmt> -> return <expr>;
        match(Token.RETURN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Return(funId, e);
    }


    /**
     * (3) Assignment Parser Implementation
     * Syntax : id = <expr>
     * @return Assignment.class ['new Assignment(id, e)']
     */
    private Assignment assignment() {
        Identifier id = new Identifier(match(Token.ID)); // ID 토큰 매칭, 선언
        match(Token.ASSIGN); // Assignment 토큰 매칭
        Expr e = expr(); // 대입에 사용될 expr 파싱
        match(Token.SEMICOLON); // Semicolon 토큰 매칭
        return new Assignment(id, e); // Assignment AST 생성해 리턴
    }


    private Call call(Identifier id) {
        // <call> -> id(<expr>{,<expr>});
        match(Token.LPAREN);
        Call c = new Call(id, arguments());
        match(Token.RPAREN);
        match(Token.SEMICOLON);
        return c;
    }

    /**
     * (4) If Parser Implementation
     * Syntax : <ifStmt> -> if (<expr>) then <stmt> [else <stmt>]
     * @return If.class ['new If(e, s1, s2)']
     */
    private If ifStmt() {
        match(Token.IF); // 'if' 토큰 매칭
        match(Token.LPAREN); // '(' 토큰 매칭
        Expr e = expr(); // 비교에 사용될 expr 파싱
        match(Token.RPAREN); // ')' 토큰 매칭
        Stmt s1 = new Empty(); // 조건 1('then')에 사용될 빈 stmt 생성
        if(token == Token.THEN){
            match(Token.THEN); // 'then' 토큰 매칭
            s1 = stmt(); // 조건 1 stmt 파싱
        }
        Stmt s2 = new Empty(); // 조건 2('else')에 사용될 빈 stmt 생성
        if (token == Token.ELSE) {
            match(Token.ELSE); // 'else' 토큰 매칭
            s2 = stmt(); // 조건 2 stmt 파싱
        }
        return new If(e, s1, s2); // IF문 AST 를 생성해 리턴
    }

    /**
     * (5) While Parser Implementation
     * Syntax : <whileStmt> -> while (<expr>) <stmt>
     * @return While.class ['new While(e,s)']
     */
    private While whileStmt() {
        match(Token.WHILE); // 'while' 토큰 매칭
        match(Token.LPAREN); // '(' 토큰 매칭
        Expr e = expr(); // 반복을 위한 비교문에 사용될 expr 파싱
        match(Token.RPAREN); // ')' 토큰 매칭
        Stmt s = stmt(); // 조건에 만족 할 경우 수행될 stmt 파싱
        return new While(e,s); // while문 AST를 생성해 리턴
    }


    private Stmts doStmt() {
        // <doStmt> -> do <stmt> while (<expr>)
        match(Token.DO);
        Stmt s = stmt();
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
        Stmts ss = new Stmts(s);
        ss.stmts.add(new While(e, s));
        return ss;
    }

    private Try tryStmt() {
        // <tryStmt> -> try <stmt> {catch(id) <stmt>}
        match(Token.TRY);
        Stmt s1 = stmt();
        match(Token.CATCH);
        match(Token.LPAREN);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.RPAREN);
        Stmt s2 = stmt();
        s1 = new Try(id, s1, s2);
        while (token == Token.CATCH) {
            match(Token.CATCH);
            match(Token.LPAREN);
            id = new Identifier(match(Token.ID));
            match(Token.RPAREN);
            s2 = stmt();
            s1 = new Try(id, s1, s2);
        }
        return (Try) s1; // new Try(id, s1, s2);
    }

    private Raise raiseStmt() {
        // <raiseStmt> -> raise id;
        match(Token.RAISE);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.SEMICOLON);
        return new Raise(id);
    }
    private For forStmt() {
        // <forStmt> -> Decl decl; Expr expr; Assignment assignment; Stmt stmt;
        match(Token.FOR);
        match(Token.LPAREN);
        Decl d = decl();
        Expr e = expr();
        match(Token.SEMICOLON);
        Assignment a = assignment();
        match(Token.RPAREN);
        Stmt s = stmt();
        return new For(d,e,a,s);
    }

    private Expr expr() {
        // <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false
        switch (token) {
            case NOT:
                Operator op = new Operator(match(token));
                Expr e = expr();
                return new Unary(op, e);
            case TRUE:
                match(Token.TRUE);
                return new Value(true);
            case FALSE:
                match(Token.FALSE);
                return new Value(false);
        }

        Expr e = bexp();
        while (token == Token.AND || token == Token.OR) {
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

    /**
     * (1) Expr Implementation 1 (aexp -> "+" & "-")
     * Syntax : <aexp> -> <term> { + <term> | - <term> }
     * Implementation
     * - Expr의 첫째항을 파싱
     * - Operator 토큰 확인 ('+' or '-')
     *   - Operator를 매치하고 다음 항 파싱
     *   - Binary 수식 AST 구성
     * - 수식 AST 리턴
     * @return Expr.class;
     */
    private Expr aexp() {
        Expr e = term(); // 첫번째 항(term) 파싱
        while (token == Token.PLUS || token == Token.MINUS) {  // + or -
            Operator op = new Operator(match(token));  // 연산자 매치
            Expr t = term(); // 다음 항(term) 파싱
            e = new Binary(op, e, t); // 수식 AST 구성
        }
        return e;  // 수식 AST 리턴
    }

    /**
     * (2) Expr Implementation 2 (term -> "*" & "/")
     * Syntax : <term> -> <factor> { * <factor> | / <factor>}
     * Implementation
     * - Expr의 첫번째 인수 파싱
     * - Operator 토큰 확인 ('*' or '/')
     *   - Operator를 매치하고 두번째 인수 파싱
     *   - Binary로 항의 AST 구성
     * - 항의 AST 리턴
     * @return Expr.class;
     */
    private Expr term() {
        Expr t = factor();  // 첫번째 인수 파싱
        while (token == Token.MULTIPLY || token == Token.DIVIDE) {
            Operator op = new Operator(match(token));  // 연산자 매칭
            Expr f = factor();  // 두번째 인수 파싱
            t = new Binary(op, t, f); // 항의 AST 구성
        }
        return t;  // 항의 AST 리턴
    }


    private Expr factor() {
        // <factor> -> [-](id | <call> | literal | '('<aexp> ')')
        Operator op = null;
        if (token == Token.MINUS)
            op = new Operator(match(Token.MINUS));

        Expr e = null;
        switch (token) {
            case ID:
                Identifier v = new Identifier(match(Token.ID));
                e = v;
                if (token == Token.LPAREN) {  // function call
                    match(Token.LPAREN);
                    Call c = new Call(v, arguments());
                    match(Token.RPAREN);
                    e = c;
                } else if (token == Token.LBRACKET) {  // array
                    match(Token.LBRACKET);
                    Array a = new Array(v, expr());
                    match(Token.RBRACKET);
                    e = a;
                }

                break;
            case NUMBER: case STRLITERAL:
                e = literal(); break;
            case LPAREN:
                match(Token.LPAREN);
                e = aexp();
                match(Token.RPAREN);
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
        while (token != Token.RPAREN) {
            es.add(expr());
            if (token == Token.COMMA)
                match(Token.COMMA);
            else if (token != Token.RPAREN)
                error("Exprs");
        }
        return es;
    }

    private Value literal() {
        String s = null;
        switch (token) {
            case NUMBER:
                s = match(Token.NUMBER);
                return new Value(Integer.parseInt(s));
            case STRLITERAL:
                s = match(Token.STRLITERAL);
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
                if (parser.token == Token.EOF) {
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
                if (parser.token == Token.EOF)
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