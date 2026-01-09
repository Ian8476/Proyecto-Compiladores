package main;
import java.io.*;
import lexer.Lexer;
import lexer.sym;
import java_cup.runtime.Symbol;
import lexer.Parser;

public class Main {
    public static void main(String[] args) throws Exception {

        //Lecutra del archivo de entrada
        Reader reader = new FileReader("input/prueba.txt");
        Lexer lexer = new Lexer(reader);

        // Crear el directorio de salida si no existe
        // La salida es en la carpeta "output"
        java.io.File outDir = new java.io.File("output");
        if (!outDir.exists()) outDir.mkdirs();
        try (java.io.BufferedWriter w = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(outDir, "tokens.txt")))) {
            Symbol token;
            // Lectura de tokens hasta EOF con una iteracion    
            while ((token = lexer.next_token()).sym != sym.EOF) {
                String name = token.sym >= 0 && token.sym < sym.terminalNames.length
                        ? sym.terminalNames[token.sym]
                        : String.valueOf(token.sym);
                String lexema = token.value != null ? token.value.toString() : "";
                int linea = token.left;   // línea provista por el lexer (1-based)
                int columna = token.right; // columna provista por el lexer (1-based)
                String out = name + " (" + token.sym + ")" + ", " + lexema + ", " + linea + ", " + columna;
                w.write(out);
                w.newLine();
                System.out.println(out);
            }


            // Validacion sintactica
            w.newLine();
            w.write("Validación sintaxis:");
            w.newLine();

            // Recreacion del Lexer y Parser para esta validacion
            // Recrear Lexer y Parser para validación sintáctica
            try {
                Reader reader2 = new FileReader("input/prueba.txt");
                Lexer lexer2 = new Lexer(reader2);
                Parser parser = new Parser(lexer2);
                parser.parse(); // si no lanza excepción => aceptado
                //w.write("ACCEPTED");
                //w.newLine();
                System.out.println("\n=== VALIDACIÓN SINTÁCTICA ===");
                System.out.println("ACCEPTED");
            } catch (Exception e) {
                //w.write("REJECTED: " + e.getMessage());
                //w.newLine();
                System.err.println("REJECTED: " + e.getMessage());
            }





        }

    }
}