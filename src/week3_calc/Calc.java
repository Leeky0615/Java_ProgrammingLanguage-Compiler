package week3_calc;

import java.io.*;

class Calc {
    int token;
    int value;
    int ch;
    private PushbackInputStream input;
    final int NUMBER = 256;

    Calc(PushbackInputStream is) {
        input = is;
    }

    int getToken() { // 다음 토큰(수 혹은 문자)을 읽어서 리턴
        while (true) {
            try {
                ch = input.read();
                if (ch == ' ' || ch == '\t' || ch == '\r') ;
                else if (Character.isDigit(ch)) {
                    value = number();
                    input.unread(ch);
                    return NUMBER;
                } else return ch;
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private int number() {    // number -> digit { digit }
        int result = ch - '0';
        try {
            ch = input.read();
            while (Character.isDigit(ch)) {
                result = 10 * result + ch - '0';
                ch = input.read();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }

    void error() {
        System.out.printf("parse error : %d\n", ch);
        //System.exit(1);
    }

    void match(int c) {            // 현재 토큰 확인 후 다음 토큰 읽기
        if (token == c)
            token = getToken();
        else error();
    }


    //////////////////
    // Assignment 3 //
    //////////////////

    void command() {  // command -> expr '\n'
        int result = expr();
        if (token == '\n') System.out.println(result);
        else error();
    }

    int expr() { // expr -> term { '+' term }
        int result = term();
        while (token == '+') {
            match('+');
            result += term();
        }
        return result;
    }

    int term() {  //term -> factor { '*' factor }
        int result = factor();
        while (token == '*') {
            match('*');
            result *= factor();
        }
        return result;
    }

    int factor() {  // factor -> '(' expr ')' | number
        int result = 0;
        if (token == '(') {
            match('(');
            result = expr();
            match(')');
        } else if(token == NUMBER){
            result = value;
            match(NUMBER);
        }
        return result;
    }

    //////////////////
    //     End      //
    //////////////////

    void parse() {
        token = getToken();    // 첫 번째 토큰을 가져옴
        command();            // parsing command 호출
    }

    public static void main(String args[]) {
        Calc calc = new Calc(new PushbackInputStream(System.in));
        while (true) {
            System.out.print(">> ");
            calc.parse();
        }
    }

}