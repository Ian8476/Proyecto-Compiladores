package main;
import java.io.*;
import lexer.Lexer;
import lexer.sym;
import java_cup.runtime.Symbol;
import lexer.Parser;
import arbolSintactico.arbol;

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
            //w.newLine(); es para escribir en el archivo de salida
            //w.newLine();
            //w.write("Validación sintaxis:");
            //w.newLine();

            // Recreacion del Lexer y Parser para esta validacion
            // Recrear Lexer y Parser para validación sintáctica
            try (Reader reader2 = new FileReader("input/prueba.txt")) {
                Lexer lexer2 = new Lexer(reader2);
                // Crear el parser
                @SuppressWarnings("deprecation")
                java_cup.runtime.SymbolFactory sf = new java_cup.runtime.DefaultSymbolFactory();
                Parser parser = new Parser(lexer2, sf);
                try {
                    Symbol result = parser.parse(); // Captura el AST
                    // Escritura del resultado en el archivo de salida
                    w.write("ACCEPTED");
                    w.newLine();
                    w.flush();
                    // También imprimir en consola
                    System.out.println("\n=== VALIDACIÓN SINTÁCTICA ===");
                    System.out.println("ACCEPTED");
                    
                    // Mostrar el árbol sintáctico
                    if (result != null && result.value instanceof arbol) {
                        arbol ast = (arbol) result.value;
                        System.out.println("\n=== ÁRBOL SINTÁCTICO ===");
                        System.out.println(ast.toString());
                        
                        // Guardar el árbol en el archivo de salida
                        w.newLine();
                        w.write("ÁRBOL SINTÁCTICO:");
                        w.newLine();
                        w.write(ast.toString());
                        w.flush();
                        
                        // Guardar el árbol como JSON
                        try (java.io.BufferedWriter wJson = new java.io.BufferedWriter(
                                new java.io.FileWriter(new java.io.File(outDir, "arbol.json")))) {
                            String jsonFormateado = formatearJSON(ast.toJSON());
                            wJson.write(jsonFormateado);
                            wJson.flush();
                            System.out.println("\n=== JSON guardado en output/arbol.json ===");
                        }
                    }
                } catch (Exception e) {
                    // Escritura del resultado en el archivo de salida
                    w.write("REJECTED: " + e.getMessage());
                    w.newLine();
                    w.flush();
                    // También imprimir en consola
                    System.err.println("REJECTED: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }





        }

    }
    
    // Método para formatear JSON con indentación
    public static String formatearJSON(String json) {
        StringBuilder resultado = new StringBuilder();
        int indentacion = 0;
        boolean enString = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            char prev = i > 0 ? json.charAt(i - 1) : ' ';
            
            // Manejar strings
            if (c == '"' && prev != '\\') {
                enString = !enString;
                resultado.append(c);
            } else if (!enString) {
                switch (c) {
                    case '{':
                    case '[':
                        resultado.append(c).append("\n");
                        indentacion++;
                        agregarIndentacion(resultado, indentacion);
                        break;
                    case '}':
                    case ']':
                        resultado.append("\n");
                        indentacion--;
                        agregarIndentacion(resultado, indentacion);
                        resultado.append(c);
                        break;
                    case ',':
                        resultado.append(c).append("\n");
                        agregarIndentacion(resultado, indentacion);
                        break;
                    case ':':
                        resultado.append(c).append(" ");
                        break;
                    case ' ':
                        // Ignorar espacios en blanco fuera de strings
                        break;
                    default:
                        resultado.append(c);
                }
            } else {
                resultado.append(c);
            }
        }
        
        return resultado.toString();
    }
    
    private static void agregarIndentacion(StringBuilder sb, int nivel) {
        for (int i = 0; i < nivel; i++) {
            sb.append("  ");
        }
    }
}