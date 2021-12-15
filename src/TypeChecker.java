// Static type checker for S

public class TypeChecker {

    static TypeEnv tenv = new TypeEnv();

    public static Type Check(Command p) {
        if (p instanceof Decl) {
            Decl d = (Decl) p;
	        if (tenv.contains(d.id)) error(d, "duplicate variable declaration");
	        else return Check(d,tenv);
        }

	    if (p instanceof Function) {
            Function f = (Function) p;
            if (tenv.contains(f.id)) error(f, "duplicate function definition");
	        else return Check(f,tenv);
        }

	    if (p instanceof Stmt) return Check((Stmt) p, tenv);

	    throw new IllegalArgumentException("undefined command");
    }

    public static Type Check(Decl decl, TypeEnv te) {
        if (decl.arraySize == 0 && decl.expr != null)
	    if (decl.type != Check(decl.expr, te)) error(decl, "type error: incorrect initialization to " + decl.id);
        te.push (decl.id, decl.type);
        return decl.type;
    }

    public static Type Check(Function f, TypeEnv te) {
	     te.push(f.id, new ProtoType(f.type, f.params));
         for (Decl d : f.params)
             te.push (d.id, d.type);

	     Type t = Check(f.stmt, te); // type check the function body
	     System.out.println(t);
	     if (t != f.type)
	         error(f, "incorrect return type");

         for (Decl d : f.params)
             te.pop();
         te.pop();                   // pop and push prototype type
	     te.push(f.id, new ProtoType(f.type, f.params));
         return f.type;
    }

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    static void error(Command c, String msg) {
        c.type = Type.ERROR;
        System.err.println("\n"+msg);
    }

    static Type Check(Expr e, TypeEnv te) {
        if (e instanceof Value) {
            Value v = (Value)e;
            return v.type;
        }

        if (e instanceof Identifier) {
            Identifier id = (Identifier) e;
            if (!te.contains(id)) error(id, "undeclared variable: " + id);
	        else id.type = te.get(id);
            return id.type;
        }

        if (e instanceof Array) {
            Array ar = (Array) e;
            if (!te.contains(ar.id))
                error(ar, "undeclared variable: " + ar.id);
	        else if (Check(ar.expr, te) == Type.INT)
		        ar.type = te.get(ar.id);
	        else
		        error(ar, "non-int index: " + ar.expr);
            return ar.type;
        }

        if (e instanceof Binary)
            return Check((Binary) e, te);

        if (e instanceof Unary)
            return Check((Unary) e, te);

        if (e instanceof Call)
            return Check((Call) e, te);

        throw new IllegalArgumentException("undefined operator");
    }


    /**
     * (1) Binary Type Check Implementation
     * 체크 함수를 통해 수식에 맞는 타입을 불러옴
     * 이항 연산자 확인
     *   사칙연산의 경우 두 왼쪽, 오른쪽 수식의 타입이 모두 int타입
     *     참이면 이항연산의 타입 = int
     *   비교연산의 경우 두 왼쪽, 오른쪽 수식의 타입이 같아야 함
     *     같으면 이항연산의 타입 = boolean
     *   and,or의 경우 두 왼쪽, 오른쪽 수식의 타입이 모두 booelan타입
     *     같으면 이항연산의 타입 = boolean
     *   아닐 경우에는 에러 호출
     * @return Type
     */
    static Type Check(Binary b, TypeEnv te) {
        Type t1 = Check(b.expr1, te); // 수식1의 타입
        Type t2 = Check(b.expr2, te); // 수식2의 타입
        switch (b.op.val) { // 이항연산의 연산자 비교
            case "+": case "-": case "*": case "/": // 사칙연산
                if(t1==Type.INT && t2==Type.INT) b.type = Type.INT; // 비교 후 참이면 int타입 대입
                else error(b, "type error for " + b.op); // 거짓이면 에러
                break;
            case "<": case "<=":case ">":case ">=":case "==":case "!=": // 비교연산
                if(t1 == t2) b.type = Type.BOOL; // 비교 후 참이면 boolean타입 대입
                else error(b, "type error for " + b.op); // 거짓이면 에러
                break;
            case "&": case "|": // AND, OR
                if(t1==Type.BOOL && t2==Type.BOOL) b.type = Type.BOOL; // 비교 후 참이면 boolean타입 대입
                else error(b, "type error for " + b.op); // 거짓이면 에러
                break;
        }
        return b.type; // 이항연산의 타입 리턴
    }

    /**
     * (2) Unary Type Check Implementation
     * 체크 함수를 통해 수식에 맞는 타입을 불러옴
     * 단항 연산자 확인
     *   NOT 연산자의 경우 수식의 타입이 boolean 이어야함.
     *     참이면 boolean타입 대입
     *   음수(-) 연산자의 경우 수식의 타입이 int 이어야함
     *     참이면 int타입 대입
     *   아닐 경우에는 에러 호출
     * @return Type
     */
    static Type Check(Unary u, TypeEnv te) {
        Type t = Check(u.expr, te); // 수식의 타입
        switch (u.op.val) { // 단항연산의 연산자 비교
            case "!": // NOT
                if(t==Type.BOOL) u.type = Type.BOOL; // 타입비교 후 참이면 boolean타입 대입
                else error(u, "! has non-bool operand"); // 거짓이면 에러
                break;
            case "-": // 음수
                if(t==Type.INT) u.type = Type.INT; // 타입비교 후 참이면 int타입 대입
                else error(u, "Unary -has non-int operand"); // 거짓이면 에러
                break;
        }
        return u.type; // 단항연산의 타입 리턴
    }


    public static Type Check(Stmt s, TypeEnv te) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Empty)
	         return Type.VOID;
        if (s instanceof Assignment)
            return Check((Assignment) s, te);
	    if (s instanceof Read)
            return Check((Read) s, te);
        if (s instanceof Print)
            return Check((Print) s, te);
        if (s instanceof If)
           return Check((If) s, te);
        if (s instanceof While)
           return Check((While) s, te);
        if (s instanceof Stmts)
           return Check((Stmts) s, te);
        if (s instanceof Let)
           return Check((Let) s, te);
        if (s instanceof Call)
           return Check((Call) s, te);
	    if (s instanceof Return)
           return Check((Return) s, te);
	    if (s instanceof Raise)
           return Check((Raise) s, te);
	    if (s instanceof Try)
           return Check((Try) s, te);
	    if (s instanceof For)
           return Check((For) s, te);
        throw new IllegalArgumentException("undefined statement");
    }

    static Type Check(Print p, TypeEnv te) {
        Type t = Check(p.expr,te);
        if (t != Type.ERROR)
	    p.type = Type.VOID;
        else
            error(p, "type error in expr: " + p.expr);
        return p.type;
    }

    static Type Check(Read r, TypeEnv te) {
	    Type t = Check(r.id, te);
        if ( t == Type.INT || t == Type.BOOL || t==Type.STRING)
	        r.type = Type.VOID;
	    else
	        error(r, " undefined variable in read: " + r.id);
        return r.type;
     }

     static Type Check(Return r, TypeEnv te) {
        Type t = Check(r.expr,te);
        if (t == Type.ERROR)
            error(r, "type error in expr: " + r.expr);
        else
	        r.type = t;
        return r.type;
    }

    /**
     * (3) Assignment Type Check Implementation
     * 먼저 대입문의 id가 stack(TypeEnv)에 없다면 에러 호출
     * 대입문에 사용되는 id와 수식의 타입을 가져옴
     * 두 개의 타입이 같다면 대입문의 타입을 void로 설정
     * 아니라면 에러를 호출한다.
     * 마지막으로 대입문의 타입을 리턴한다.
     * @return Type
     */
    static Type Check(Assignment a, TypeEnv te) {
        if (!te.contains(a.id)) { // 대입문의 id가 typeEnv에 없다면
            error(a, "undefined variable in assignment: " + a.id); // 에러
            return Type.ERROR; // 에러 타입 리턴
        }
        Type t1 = te.get(a.id); // 대입문 id에 해당하는 타입 가져옴
        Type t2 = Check(a.expr, te); // 대입문의 수식의 타입을 체크
        if(t1==t2) a.type = Type.VOID; // 두 타입이 같다면 대입문의 타입에 void 대입
        else error(a, "mixed mode assignment to " + a.id); // 아니라면 에러 호출
        return a.type; // 대입문의 타입 리턴
    }

    /**
     * (4) If Type Check Implementation
     * 비교문, 실행될 복합문 세 개의 타입을 받아옴.
     * 비교문의 타입이 boolean타입인지 확인
     *   참이면 두 복합문의 타입이 같은지 확인
     *     참이면 if문의 타입을 stmt의 타입으로 설정
     *     거짓이면 에러 호출
     *   거짓이면 에러 호출
     * if문의 타입 리턴
     * @return Type
     */
    static Type Check(If c, TypeEnv te) {
        Type t = Check(c.expr, te); // 비교문의 타입
        Type t1 = Check(c.stmt1, te); // 실행될 stmt 타입
        Type t2 = Check(c.stmt2, te); // 실행될 stmt 타입
        if (t == Type.BOOL) { // t의 타입 비교
            if(t1 == t2) c.type = t1; // t1,t2가 같은지 비교, 참이면 if문 타입 설정
            else error(c, "non-equal type in two branches"); // 아니면 에러호출
        } else error(c, "non-bool test in condition"); // 아니면 에러호출
        return c.type; // 설정된 if문 타입 리턴
    }

    /**
     * (5) While Type Check Implementation
     * 비교문, 실행될 복합문 두 개의 타입을 받아옴.
     * 비교문의 타입이 boolean타입인지 확인
     *   참이면 복합문의 타입이 void인지 확인
     *     참이면 while문의 타입에 대입
     *     거짓이면 에러 호출
     *   거짓이면 에러 호출
     * while 문의 타입 리턴
     * @return Type
     */
    static Type Check(While l, TypeEnv te) {
        Type t = Check(l.expr, te); // 비교문의 타입
        Type t1 = Check(l.stmt, te); // 실행될 stmt 타입
        if (t == Type.BOOL) { // t의 타입비교
            if(t1 == Type.VOID) l.type = t1; // t1의 타입비교, 참이면 while문 타입 설정
            else error(l,"return in loop.."); // 아니면 에러 호출
        } else error(l, "non-bool test in loop"); // 아니면 에러 호출
        return l.type; // 설정된 while문 타입 리턴
    }

    /**
     * [PLUS] For Type Check Implementation
     * for문에서 사용되는 변수는 하나이므로 decls 클래스에 담아 생성.
     * for문에서 사용되는 타입 추가 및 다른 문 의 타입 체크
     * 비교문(수식)의 타입이 boolean타입인지 확인
     *   참이면 대입문의 타입이 void인지 확인
     *     참이면 복합문의 타입이 void인지 확인
     *       참이면 for의 타입에 대입
     *       거짓이면 에러 호출
     *     거짓이면 에러 호출
     *   거짓이면 에러 호출
     * for문에서 사용된 변수 타입 stack에서 제거
     * for문의 타입 리턴
     * @return Type
     */
    static Type Check(For f, TypeEnv te) {
        Decls decls = new Decls(f.decl); // for문에 있는 decl을 decls에 담음
        addType(decls, te); // 선언문의 타입 stack에 저장
        Type t = Check(f.expr, te); // 수식의 타입 체크
        Type t1 = Check(f.assignment, te); // 대입문 타입 체크
        Type t2 = Check(f.stmt, te); // 복합문 타입 체크
        if (t == Type.BOOL) { // 수식의 타입 체크
            if(t1 == Type.VOID) { // 대입문 타입 체크
                if(t2 == Type.VOID) f.type = t2; // 복합문 타입체크 후 for문에 타입 대입
                else error(f,"return in loop.."); // 거짓이면 에러호출
            }else error(f,"undefined variable in assignment"); // 거짓이면 에러호출
        } else error(f, "non-bool test in loop"); // 거짓이면 에러호출
        deleteType(decls, te); // for문에서 사용된 타입 stack에서 제거
        return f.type;
    }
    /**
     * (6) Stmts Type Check Implementation
     * 먼저 void타입 선언 뒤 파라미터로 들어온 복합문들의 타입을 하나씩 비교
     * 복합문의 타입이 Void이고 인덱스가 올바른지 확인
     *   아니라면 에러 호출
     * 반복문들의 타입이 에러 타입이 아니라면 stmts타입에 void 타입 설정
     * @return Type
     */
    static Type Check(Stmts ss, TypeEnv te) {
        Type t = Type.VOID; // Void 타입 선언
        for (int i = 0; i < ss.stmts.size(); i++) { // stmt의 개수만큼 반복
            t = Check(ss.stmts.get(i), te); // index에 해당하는 stmt 타입가져옴
            if(t!= Type.VOID && i != ss.stmts.size()-1) error(ss, "return in Stmts");// 비교 후 에러 호출
        } // 반복문 종료
        if(ss.type != Type.ERROR) ss.type = t; // stmts의 타입이 에러가 아니라면 먼저 선언된 타입 대입
        return ss.type; // stmts의 타입 리턴
    }

    /**
     * (7) Let Type Check Implementation
     * 대입문에 사용되는 타입을 stack(TypeEnv)에 추가한다.
     * let문의 타입은 안에 있는 복합문의 타입을 체크한뒤 넣는다.
     * 선언문 종료 시 선언된 타입을 제거한다.
     * @return Type
     */
    static Type Check(Let l, TypeEnv te) {
        addType(l.decls, te); // 선언문 타입 추가
        l.type = Check(l.stmts, te); // 복합문 타입으로 설정 void
        deleteType(l.decls, te); // 선언된 타입 제거
        return l.type; // let문 타입 리턴
    }


    static Type Check(Raise r, TypeEnv te) {
        Type t = Check(r.eid,te);
        if (t == Type.EXC)
	    r.type = Type.VOID;
        else
            error(r, "type error in exception: " + r.eid);
        return r.type;
    }

    static Type Check(Try t, TypeEnv te) {
        Type t0 = Check(t.eid,te);
        if (t0 != Type.EXC)
            error(t, "type error in exception : " + t.eid);

        Type t1 = Check(t.stmt1, te);
        Type t2 = Check(t.stmt2, te);
        if (t1 == Type.VOID && t2 == Type.VOID)
	        t.type = Type.VOID;
        else
            error(t, "type error in try-catch ");

        return t.type;
    }

    static Type Check(Call c, TypeEnv te) {
       if (!te.contains(c.fid)) {
           error(c, "undefined function: " + c.fid);
           return c.type;
       }
       Exprs args = c.args;
       ProtoType p = (ProtoType)te.get(c.fid);
       c.type = p.result;
       // check arguments against the ProtoType
       if (args.size() == p.params.size())
           for (int i=0; i<args.size(); i++) {  // match arg types with param types
                Expr e = (Expr)args.get(i);
                Type t1 = Check(e,te);
                Type t2 = ((Decl)p.params.get(i)).type;
                if (t1 != t2)
                    error(c, "argument type does not match parameter");
           }
       else
           error(c, "do not match numbers of arguments and params");

       return c.type;
    }

    public static TypeEnv addType (Decls ds, TypeEnv te) {
        // put the variable decls into a symbol table(TypeEnv) 
        if (ds != null)
            for (Decl decl : ds)
                Check(decl, te);
        return te;
    }

    public static TypeEnv addType (Decls ds, Functions fs, TypeEnv te) {
        // put the variable decls into a symbol table(TypeEnv) 
        if (ds != null)
            for (Decl decl : ds)
                Check(decl, te);

        if (fs != null)
            for (Function f : fs)
                Check(f, te);

        return te;
    }

    static TypeEnv deleteType(Decls ds, TypeEnv te) {
        if (ds != null)
            for (Decl decl : ds)
                te.pop();

        return te;
    }

    static TypeEnv deleteType(Decls ds, Functions fs, TypeEnv te) {
        if (ds != null)
            for (Decl decl : ds)
                te.pop();

        if (fs != null)
            for (Function f: fs)
                te.pop();

        return te;
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            Sint sint = new Sint(); Lexer.interactive = true;
            System.out.println("Begin parsing... ");
            System.out.print(">> ");
            Parser parser  = new Parser(new Lexer());

            do { // Program = Command*
                Command command = null;
                if (parser.token==Token.EOF) {
                    parser.token = parser.lexer.getToken();
                }

                try {
                    command = parser.command();
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                    System.out.print(">> ");
                    continue;
                }

                System.out.println("\nType checking...");
                try {
                    TypeChecker.Check(command);
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                }
                System.out.println("\nCommand type :" + command.type);
                System.out.print(">> ");
            } while(true);
        }
        else {
            Command command = null;
    	    System.out.println("Begin parsing... " + args[0]);
            Parser parser  = new Parser(new Lexer(args[0]));
            do {
			    if (parser.token == Token.EOF)
                    break;

                try {
                    command = parser.command();
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                    continue;
                }

                System.out.println("\nType checking...");
                try {
                    TypeChecker.Check(command);
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                }
                System.out.println("\nCommand type :" + command.type);
            } while (command != null);
        }
    } //main
}