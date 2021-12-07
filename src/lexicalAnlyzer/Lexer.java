package lexicalAnlyzer;

import java.io.*;

public class Lexer {
    private char ch = ' ';
    private BufferedReader input;
    private final char eoInch = '\n';
    private final char eofCh = '\004';
    static boolean interactive = true;

    public Lexer(String fileName) {
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("File not found:  = " + fileName);
            System.exit(1);
        }
    }

    public Lexer() {
        input = new BufferedReader(new InputStreamReader(System.in));
    }

    public char getChar() {
        int c = 0;
        try {
            c = input.read();
            if (c == -1) c = eofCh;
        } catch (IOException e) {
            System.err.println(e);
        }
        return (char) c;
    }

    public Token getToken() { // Return next token
        do {
            if (Character.isLetter(ch)) { // ident or keyword
                String s = "";
                do {
                    s += ch;
                    ch = getChar();
                } while (Character.isLetter(ch) || Character.isDigit(ch));
                return Token.idORrkeyword(s);
            }

            if (Character.isDigit(ch)) { // number
                String s = "";
                do {
                    s += ch;
                    ch = getChar();
                } while (Character.isDigit(ch));
                return Token.NUMBER.setValue(s);
            }

            switch (ch) {
                case ' ':
                case '\t':
                case '\r':
                    ch = getChar();
                    break;
                case eoInch:
                    ch = getChar();
                    if (ch == '\r') ch = getChar(); // for Windows
                    if (ch == eoInch && interactive) return Token.EOF;
                    break;
                case '/': // divide
                    ch = getChar();
                    if (ch != '/') return Token.DIVIDE;
                    do {
                        ch = getChar();
                    } while (ch != eoInch);
                    ch = getChar();
                    break;
                case '\"':  // string literal
                    String s = "";
                    while ((ch = getChar()) != '\"') s += ch;
                    ch = getChar();
                    return Token.STRLITERAL.setValue(s);
                case eofCh: return Token.EOF;
                case '+':
                    ch = getChar();
                    return Token.PLUS;
                case '-':
                    ch = getChar();
                    return Token.MINUS;
                case '<':
                    ch = getChar();
                    if(ch !='=') return Token.LT;
                    else{
                        ch = getChar();
                        return Token.LTEQ;
                    }
                case '>':
                    ch = getChar();
                    if(ch !='=') return Token.GT;
                    else{
                        ch = getChar();
                        return Token.GTEQ;
                    }
                case '!':
                    ch = getChar();
                    if(ch != '=') return Token.NOT;
                    else{
                        ch = getChar();
                        return Token.NOTEQ;
                    }
                case '=':
                    ch = getChar();
                    if(ch != '=') return Token.ASSIGN;
                    else{
                        ch = getChar();
                        return Token.EQUAL;
                    }
            }// switch
        }while(true);
    }

    public void error(String msg) {
        System.err.println("Error: " + msg);
//        System.exit(1);
    }

    public static void main(String[] args) {
        Lexer lexer;
        if (args.length == 0) lexer = new Lexer();
        else lexer = new Lexer(args[0]);

        Token tok = lexer.getToken();
        while (tok != Token.EOF) {
            System.out.println(tok.toString());
            tok = lexer.getToken();
        }
    }// main
}
