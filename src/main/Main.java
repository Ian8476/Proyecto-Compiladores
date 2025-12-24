package main;
import java.io*;
import lexer.Lexer;
import java_cup.runtime.Symbol;

public class Main {
    public static void main(String[] args) throws Exception {

        Reader reader =new FileReader("input/prueba.txt");
        Lexer lexer = new Lexer(reader);

        Symbol token;
        while ((token = lexer.next_token()).sym != Symbol.EOF) {
            System.out.println("Token: " + token.sym);
        }

    }
}