// Interpreter for S
import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) {
        if (!state.empty()) {
            Pair p = state.peek();
            if (p.val.type() == Type.RAISEDEXC) {
                state.pop();
            }
        }

	    if (c instanceof Decl) {
	        Decls decls = new Decls();
	        decls.add((Decl)c);
	        return allocate(decls, state);
	    }

	    if (c instanceof Function) {
	        return allocate(null, new Functions((Function) c),state);
	    }

	    if (c instanceof Stmt)
	        return Eval((Stmt)c, state);

	throw new IllegalArgumentException("no command");
    }

    State Eval(Stmt s, State state) {
        if (!state.empty()) {
            Pair p = state.peek();
            if (p.val.type() == Type.RAISEDEXC)
                return state;
        }

        if (s instanceof Empty) return Eval((Empty)s, state);
        if (s instanceof Assignment) return Eval((Assignment)s, state);
        if (s instanceof If) return Eval((If)s, state);
        if (s instanceof While) return Eval((While)s, state);
        if (s instanceof Stmts) return Eval((Stmts)s, state);
	    if (s instanceof Let) return Eval((Let)s, state);
	    if (s instanceof Read) return Eval((Read)s, state);
	    if (s instanceof Print) return Eval((Print)s, state);
        if (s instanceof Call) return Eval((Call)s, state);
	    if (s instanceof Return) return Eval((Return)s, state);
	    if (s instanceof Raise) return Eval((Raise)s, state);
	    if (s instanceof Try) return Eval((Try)s, state);
	    if (s instanceof For) return Eval((For)s, state);
        throw new IllegalArgumentException("no statement");
    }

    // call without return value
    State Eval(Call c, State state) {
	    Value v = state.get(c.fid);      // find the function
        Function f = v.funValue();
        State s = newFrame(state, c, f); // new the frame on the stack
        System.out.println("Calling " + c.fid);
        s = Eval(f.stmt, s); 		// interpret the call
        System.out.println("Returning " + c.fid);
        s = deleteFrame(s, c, f);	// delete the frame
    	return s;
    }

    // value-returning call 
    Value V (Call c, State state) {
	    Value v = state.get(c.fid);		// find function
        Function f = v.funValue();
        State s = newFrame(state, c, f); // new the frame on the stack
        System.out.println("Calling " + c.fid);
        s = Eval(f.stmt, s); 			// interpret the call
        System.out.println("Returning " + c.fid);
	    v = s.pop().val;   				// remove the return value
        s = deleteFrame(s, c, f); 		// delete the frame from the stack
    	return v;
    }

    State Eval(Return r, State state) {
        Value v = V(r.expr, state);
        return state.push(r.fid, v);
    }

    State newFrame (State state, Call c, Function f) {
        if (c.args.size() == 0) return state;
	    Value val[] = new Value[f.params.size()];
        int i = 0;
        for (Expr e : c.args)
            val[i++] = V(e,state);

        state.push(new Identifier("barrier"), null); // barrier 
	    // activate a new stack frame in the stack
        state = allocate(f.params, new Functions(f), state);
        i = 0;
        for (Decl d : f.params) { // pass by value
            Identifier v = d.id;
            state.set(v, val[i++]);
        }
        return state;
    }

    State deleteFrame (State state, Call c, Function f) {
	// free a stack frame from the stack
        state = free(f.params, new Functions(f), state);
        state.pop(); // pop barrier
        return state;
    }

    State Eval(Empty s, State state) {
        return state;
    }


    /**
     * (1) Assignment Eval Implementation
     * Syntax : id = <expr>;
     * V 함수를 통해 값을 계산 후 상태를 변환
     * @return State
     */
    State Eval(Assignment a, State state) {
        Value v = V(a.expr, state); // assignment의 수식 계산.
        return state.set(a.id, v); // 스택탑에서 id를 찾아 값을 넣어줌
    }

    /**
     * (2) If Eval Implementation
     * Syntax : if (<expr>) then <stmt> [else <stmt>]
     * If의 expr의 boolean값을 받아와
     *  참이면 then의 stmt1을
     *  거짓이면 else의 stmt2를 리턴
     * @return State
     */
    State Eval(If c, State state) {
        if(V(c.expr,state).boolValue()) //  expr값 가져와 비교
            return Eval(c.stmt1, state); // stmt1 리턴
        else return Eval(c.stmt2, state); // stmt2 리턴
    }

    /**
     * (3) While Eval Implementation
     * Syntax : while '('<expr>')' <stmt>
     * while의 expr의 boolean값을 받아와
     * 조건에 맞으면 안에 stmt문을 실행한뒤 변이된
     * 상태를 다시 재귀적 호출
     * 아니면 바뀐 상태 리턴
     * @return State
     */
    State Eval(While l, State state) {
        if(V(l.expr, state).boolValue()) // expr 값 비교
            return Eval(l, Eval(l.stmt, state)); // stmt 실행 뒤 상태를 다시 재귀적으로 호출
        else return state; // 변이된 상태 리턴
    }

    /**
     * (4) Let Eval Implementation
     * Syntax : let <decls> in <stmts> end
     * let -> 현재 상태에 decls 선언문에 있는 id를 allocate함.
     * in -> 다음 stmts로 상태 변환
     * end -> 마지막으로 let문에서 선언된 decls를 상태에서 제거
     * @return State
     */
    State Eval(Let l, State state) {
        State s = allocate(l.decls, state); // decls를 allocate함.
        s = Eval(l.stmts, s); // 내부 stmts로 상태 변환
        return free(l.decls,s); // 선언된 decls stack에서 해제
    }

    /**
     * (5) Read Eval Implementation
     * Syntax : read id
     * 들어온 Read 클래스의 타입을 읽어
     * 타입에 맞게 scanner에서 읽은 뒤
     * 스택에 id, value로 설정
     * @return State
     */
    State Eval(Read r, State state) {
        if (r.id.type == Type.INT) { // int 타입인 경우
            int i = sc.nextInt(); // int 읽기
            state.set(r.id, new Value(i)); // 스택에 id, value 설정
        }
        if (r.id.type == Type.BOOL) { // boolean 타입인 경우
            boolean b = sc.nextBoolean(); // boolean 읽기
            state.set(r.id, new Value(b)); // 스택에 id, value 설정
        }
        return state; // 변환된 상태 리턴
    }

    /**
     * (6) Print Eval Implementation
     * Syntax : Print <expr>;
     * 수식의 값을 계산한 뒤에 출력.
     * @return State
     */
    State Eval(Print p, State state) {
        System.out.println(V(p.expr, state)); // 현재 상태에서 수식 계산 후 출력
        return state; // 상태 리턴
    }


    State Eval(Stmts ss, State state) {
        for (Stmt stmt : ss.stmts) {
            state = Eval(stmt, state);
        }
        return state;
    }


    /**
     * (7) Allocate Function Implementation
     * 매개변수로 들어온 선언문들을 loop돌면서 하나씩 확인
     * 선언문의 초기화값이 없다면 해당 타입에 맞는 default값으로 Value생성
     *  id와 생성된 value를 stack top에 넣는다.
     * 초기화 값이 있다면 뒤의 수식을 계산한뒤 id와 value값을 넣는다.
     * @return State
     */
    State allocate (Decls ds, State state) {
        if(ds != null) // Decls가 있다면
            for (Decl d : ds) { // decl을 반목문으로 돌림
                if(d.expr == null) // decl의 expr이 없다면
                    state.push(d.id, V(new Value(d.type),state)); // decl의 타입에 맞는 value를 생성해 stack에 push
                else  // expr이 있다면
                    state.push(d.id, V(d.expr,state)); // state에 expr을 계산한 값 push
            } // 반복문 종료
        return state; // 변환된 상태 리턴
    }

    /**
     * (8) Free Function Implementation
     * 매개변수로 들어온 선언문들의 개수만큼 반복문 실행
     * state top의 값을 빼냄.
     * @return State
     */
    State free (Decls ds, State state) {
        if(ds != null) // Decls가 있다면
            for (Decl d : ds) state.pop(); // 반복문을 돌면서 state에서 pop
        return state;
    }

    /**
     * (9) For Eval Implementation
     * Syntax : for '('<decl>;<expr>;<assignment>')'<stmt>
     * for문에서 사용되는 변수는 하나이므로 decls 클래스에 담아 생성.
     * 변수와 상태를 stack에 할당한다.
     * 반복문을 돌면서 연산값이 참이면 내부 수식과 대입문으로 상태변환
     * for문 종료 후 선언된 변수 stack에서 해제
     * @return State
     */
    State Eval(For f, State state){
        Decls decls = new Decls(f.decl); // for문에 있는 decl을 decls에 담음
        State s = allocate(decls, state); // decl을 allocate 한 뒤 상태 저장
        while (V(f.expr,s).boolValue()) { // for문의 expr이 참이면
            s = Eval(f.stmt, s); // stmt문으로 상태변환
            s = Eval(f.assignment, s); // assignment문으로 상태변환
        }
        return free(decls, s); // decls 해제
    }

    /**
     * Allocate for Function Implementation (Optional)
     * 함수에서 사용되는 allocate
     * 1. 함수 정의
     *   매개변수로 Functions,state만 들어온다. -> Decls == null
     * 2. 함수 호출 -> Frame 생성
     *   매개변수로 Decls,Functions,state가 들어온다. -> Decls == Function.params
     * 먼저 들어온 함수리스트가 null이 아닌지 확인한 다음 모든 함수에 대해서
     * 선언문(Decls)가 null 이라면 1번 함수정의인 경우 => 함수의 id와 AST를 push해준다.
     * 선언문(Decls)가 null이 아니고 선언문과 들어온 함수의 params가 같다면 2번 함수호출인 경우
     *   => 다시 자기자신(Sint.class)의 선언문할당(allocate)함수를 호출한다.
     *   => 이때 들어가는 매개변수 decls은 가독성을 위해 ds대신 f.params로 넣는다.
     * 마지막으로 변환된 상태를 리턴한다.
     * @return State
     */
    State allocate (Decls ds, Functions fs, State state) {
        if (fs != null) // Function이 있다면
            for (Function f : fs) { // 모든 Function에 대해서
                // 함수 정의용 allocate (decls가 null로 들어옴)
                if (ds == null) state.push(f.id, new Value(f)); // 함수 id와 AST를 넣음

                // 함수 호출용 allocate :: decls가 null이 아니고, Function의 params와 같다면
                // Sint.class의 allocate 호출(parameter = f.params(ds))
                if (ds!= null && ds.equals(f.params)) state = this.allocate(f.params, state);
            }
        return state; // 변환된 상태 리턴
    }

    /**
     * Free for Function Implementation (Optional)
     * 함수에서 사용되는 free
     * delete frame인 경우
     * -> 매개변수로 들어온 Functions가 null이 아닌지 확인 후 모든 함수에 대해서 체크한다.
     *    매개변수로 들어온 Decls와 함수의 params가 같다면
     *    다시 자기자신(Sint.class)의 free 함수를 호출한다.
     * => 이때 들어가는 매개변수 decls은 가독성을 위해 ds대신 f.params로 넣는다.
     * 마지막으로 변환된 상태를 리턴한다.
     * @return State
     */
    State free (Decls ds, Functions fs, State state) {
        if(fs != null) // Function이 있다면
            for (Function f : fs) { // 모든 Function에 대해서
                if(ds!= null && ds.equals(f.params)) // decls가 null이 아니고, Function의 params와 같다면
                    state = this.free(f.params, state); // Sint.class의 free 호출(parameter = f.params(ds))
            }
        return state; // 변환된 상태 리턴
    }



    State Eval(Raise r, State state) {
        Value v = V(r.eid, state);
        return state.push(r.eid, new Value(Type.RAISEDEXC));
    }

    State Eval(Try t, State state) {
        state = Eval(t.stmt1, state);
        Pair p = state.peek();
        if (p.val.type() == Type.RAISEDEXC)
           if (p.id.equals(t.eid)) {  	// caught
               state.pop();
               state = Eval(t.stmt2, state);
           }
        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef, "reference to undef value");
	    switch (op.val) {
	    case "+":
            return new Value(v1.intValue() + v2.intValue());
        case "-":
            return new Value(v1.intValue() - v2.intValue());
        case "*":
            return new Value(v1.intValue() * v2.intValue());
        case "/":
            return new Value(v1.intValue() / v2.intValue());
        case "==":
            return new Value(v1.intValue() == v2.intValue());
        case "!=":
            return new Value(v1.intValue() != v2.intValue());
        case "<":
            return new Value(v1.intValue() < v2.intValue());
        case "<=":
            return new Value(v1.intValue() <= v2.intValue());
        case ">":
            return new Value(v1.intValue() > v2.intValue());
        case ">=":
            return new Value(v1.intValue() >= v2.intValue());
        case "&":
            return new Value(v1.boolValue() && v2.boolValue());
        case "|":
            return new Value(v1.boolValue() || v2.boolValue());
	    default:
	        throw new IllegalArgumentException("no operation");
	    }
    }

    Value unaryOperation(Operator op, Value v) {
        check(!v.undef, "reference to undef value");
	    switch (op.val) {
        case "!":
            return new Value(!v.boolValue( ));
	    case "-":
            return new Value(-v.intValue( ));
        default:
            throw new IllegalArgumentException("no operation: " + op.val);
        }
    }

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
        if (e instanceof Value)
            return (Value) e;
        if (e instanceof Identifier) {
            Identifier v = (Identifier) e;
            return (Value)(state.get(v));
	    }
        if (e instanceof Array) {
	        Array ar = (Array) e;
            Value i = V(ar.expr, state);
            Value v = (Value) state.get(ar.id);
            Value[] vs = v.arrValue();
            return (vs[i.intValue()]);
	    }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation (b.op, v1, v2);
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v);
        }
        if (e instanceof Call)
    	    return V((Call)e, state);
        throw new IllegalArgumentException("no operation");
    }

    public static void main(String args[]) {
	    if (args.length == 0) {
	        Sint sint = new Sint(); Lexer.interactive = true;
            System.out.println("Language S Interpreter 1.0");
            System.out.print(">> ");
	        Parser parser  = new Parser(new Lexer());

	        do { // Program = Command*
	            if (parser.token == Token.EOF)
		        parser.token = parser.lexer.getToken();

	            Command command=null;
                try {
	                command = parser.command();
                    command.type = TypeChecker.Check(command);
                } catch (Exception e) {
                    System.out.println(e);
		            System.out.print(">> ");
                    continue;
                }

	            if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting..." );
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                         System.err.println(e);
                    }
                }
		    System.out.print(">> ");
	        } while (true);
	    }
        else {
	        System.out.println("Begin parsing... " + args[0]);
	        Command command = null;
	        Parser parser  = new Parser(new Lexer(args[0]));
	        Sint sint = new Sint();

	        do {	// Program = Command*
	            if (parser.token == Token.EOF)
                    break;

                try {
		            command = parser.command();
                    command.type = TypeChecker.Check(command);
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }

	            if (command.type!=Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
	        } while (command != null);
        }
    }
}