package lexicalAnlyzer;

public enum Token {
    BOOL("bool"), TRUE("true"), FALSE("false"),
    IF("if"), THEN("then"), ELSE("else"),
    INT("int"), STRING("string"),
    WHILE("while"), FOR("for"),VOID("void"), FUN("fun"), RETURN("return"),
    LET("let"), IN("in"), END("end"), READ("read"), PRINT("print"),
    EOF("<<EOF>>"),
    LBRACE("{"), RBRACE("}"), LBRACKET("["), RBRACKET("]"),
    LPAREN("("), RPAREN(")"), SEMICOLON(";"), COMMA(","),
    ASSIGN("="), EQUAL("=="), LT("<"), LTEQ("<="), GT(">"),
    GTEQ(">="), NOT("!"), NOTEQ("!="), PLUS("+"), MINUS("-"),
    MULTIPLY("*"), DIVIDE("/"), AND("&"), OR("|"),
    ID(""), NUMBER(""), STRLITERAL("");
    private String value;

    private Token(String v) {
        value = v;
    }

    public static Token idORrkeyword(String name) {
        for (Token token : Token.values()) {
            if(token.value().equals(name)) return token;
            if(token == Token.EOF) break;
        }
        return ID.setValue(name);
    } // keyword or ID

    public String value() {
        return value;
    }

    public Token setValue(String v) {
        this.value = v;
        return this;
    }
}
