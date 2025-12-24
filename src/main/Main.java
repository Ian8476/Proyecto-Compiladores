package main;
import java.io.*;
import lexer.Lexer;
import lexer.sym;
import java_cup.runtime.Symbol;

public class Main {
    public static void main(String[] args) throws Exception {

        Reader reader =new FileReader("input/prueba.txt");
        Lexer lexer = new Lexer(reader);

        Symbol token;
        while ((token = lexer.next_token()).sym != sym.EOF) {
            System.out.println("Token: " + token.sym);
        }

    }
}