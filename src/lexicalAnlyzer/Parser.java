package lexicalAnlyzer;

public class Parser {
    Token token;
    Lexer lexer;
    String funId = "";
    public Parser(Lexer scan) {
        lexer = scan;
        token = lexer.getToken();
    }
    private String match(Token t){
        String value = token.value();
        if (token == t) {
            token = lexer.getToken();
        }else{
            error(t);
        }
        return value;
    }
    void error(Token tok) {
        System.out.printf("Syntax error: " + tok + " --> " + token);
        //System.exit(1);
    }
    public Command command(){
        // <command> -> <decl> | <function> | <stmt>
        if(isType()){
            Decl d = decl();
            return d;
        }
        if (token == Token.FUN) {
            Function f = function();
            return f;
        }
    }
    private Type type(){
        return new Type();
    }
    private Decl decl(){
        Type t = type(); // 타입 이름 파싱
        String id = match(Token.ID); // 변수 이름(식별자) 매치
        Decl d = null;
        if (token == Token.ASSIGN) {
            match(Token.ASSIGN); // 대입 연산자 매치
            Expr e = expr(); // 초기화 수식 파싱
            d = new Decl(t, id, e);
        }
    }



    Stmt stmt(){
        Stmt s;
        switch (token){
            case FOR:
                s = forStmt();
                return s;
        }
    }
    For forStmt(){
        match(Token.FOR); // for 토큰 매치
        match(Token.LPAREN); // 왼쪽 괄호 매치
        Decl d = decl(); // Type type;Identifier id1;Expr expr1;
        Expr e = expr(); // Expr expr2;
        Assignment a = assignmenr(); // Identifier id;Expr expr3;
        match(Token.RPAREN); // 오른쪽 괄호 매치
        Stmt s = stmt(); // 본체 문장 파싱
        return new For(d, e, a, s);

    }







    public static void main(String[] args) {
        Parser parser = new Parser(new Lexer());
        System.out.println(">> ");
        do {
            Command command = parser.command();
            System.out.println("\n>>");
        } while (true);
    }
}
