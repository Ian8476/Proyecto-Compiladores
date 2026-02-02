package main;
import java.io.*;
import lexer.Lexer;
import lexer.sym;
import java_cup.runtime.Symbol;
import lexer.Parser;
import arbolSintactico.arbol;
import util.ErrorHandler;
import util.SymbolTable;
import util.Token;
import util.CodeGenerator;

/**
 * Clase principal para ejecutar el análisis léxico y sintáctico.
 * Lee un archivo de entrada, genera tokens, valida la sintaxis,
 * @author Duan Antonio Espinoza
 * @author Ian Canales Rodriguez
 * 
 */
public class Main {
    // Tabla de símbolos compartida en toda la compilación
    private static SymbolTable tablaSimbolos;
    public static void main(String[] args) throws Exception {
        
        // Limpiar errores previos
        ErrorHandler.limpiar();
        
        // ========================================
        // INICIALIZAR TABLA DE SÍMBOLOS
        // ========================================
        tablaSimbolos = new SymbolTable();
        System.out.println(" Tabla de símbolos inicializada\n");

        // Determinar archivo de entrada
        String archivoEntrada = "input/prueba.txt";
        if (args.length > 0) {
            archivoEntrada = args[0];
            System.out.println("Usando archivo de entrada: " + archivoEntrada + "\n");
        }

        //Lectura del archivo de entrada
        Reader reader = new FileReader(archivoEntrada);
        Lexer lexer = new Lexer(reader);

        // Crear el directorio de salida si no existe
        // La salida es en la carpeta "output"
        java.io.File outDir = new java.io.File("output");
        if (!outDir.exists()) outDir.mkdirs();
        
        try (java.io.BufferedWriter w = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(outDir, "tokens.txt")))) {
            Symbol token;
            
            // ========================================
            // FASE 1: ANÁLISIS LÉXICO
            // ========================================
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("FASE 1: ANÁLISIS LÉXICO");
            System.out.println("═══════════════════════════════════════════════════════════\n");
            
            // Lectura de tokens hasta EOF con una iteración    
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
                
                // Registrar identificadores en la tabla de símbolos
                if (name.equals("IDENT")) {
                    registrarIdentificador(lexema, linea, columna);
                }
            }

            // ========================================
            // FASE 2: ANÁLISIS SINTÁCTICO
            // ========================================
            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("FASE 2: ANÁLISIS SINTÁCTICO");
            System.out.println("═══════════════════════════════════════════════════════════\n");
            
            // Recrear Lexer y Parser para validación sintáctica
            try (Reader reader2 = new FileReader(archivoEntrada)) {
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
                    // También imprime en consola
                    System.out.println(" Análisis sintáctico completado: ACCEPTED");
                    
                    // Mostrar el árbol sintáctico
                    if (result != null && result.value instanceof arbol) {
                        arbol ast = (arbol) result.value;
                        System.out.println("\n═══════════════════════════════════════════════════════════");
                        System.out.println("ÁRBOL SINTÁCTICO");
                        System.out.println("═══════════════════════════════════════════════════════════\n");
                        System.out.println(ast.toString());
                        
                            // Verificar si hay errores léxicos o sintácticos
                            if (ErrorHandler.getTotalErrores() > 0) {
                                System.out.println("\n  SE ENCONTRARON ERRORES EN FASES ANTERIORES");
                                System.out.println("No se procederá con análisis semántico ni generación de código.");
                                ErrorHandler.generarReporte("output/reporte_errores.txt");
                                ErrorHandler.mostrarResumen();
                            } else {
                                // ========================================
                                // FASE 3: ANÁLISIS SEMÁNTICO
                                // ========================================
                                System.out.println("\n═══════════════════════════════════════════════════════════");
                                System.out.println("FASE 3: ANÁLISIS SEMÁNTICO");
                                System.out.println("═══════════════════════════════════════════════════════════\n");
                            
                                analizarSemantico(ast);
                            
                                // Verificar si hay errores semánticos
                                if (ErrorHandler.getErroresSemanticos() > 0) {
                                    System.out.println("\n  SE ENCONTRARON ERRORES SEMÁNTICOS");
                                    System.out.println("No se procederá con generación de código MIPS.");
                                    ErrorHandler.generarReporte("output/reporte_errores.txt");
                                    ErrorHandler.mostrarResumen();
                                } else {
                                    // ========================================
                                    // FASE 4: GENERACIÓN DE CÓDIGO MIPS
                                    // ========================================
                                    System.out.println("\n═══════════════════════════════════════════════════════════");
                                    System.out.println("FASE 4: GENERACIÓN DE CÓDIGO MIPS");
                                    System.out.println("═══════════════════════════════════════════════════════════\n");
                                
                                    generarCodigoMIPS(ast, outDir);
                                }
                            }
                        
                                // Guardar el árbol en el archivo de salida solo si no hay errores sintácticos
                                if (ErrorHandler.getTotalErrores() == 0) {
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
                                        System.out.println("\n JSON guardado en output/arbol.json");
                                        // Después de guardar el árbol como JSON
                                        generarHTMLArbol(ast, "output/arbol_interactivo.html");
                                        System.out.println(" Gráfico HTML generado en output/arbol_interactivo.html");
                                    }
                                }
                    }
                    
                    // ========================================
                    // GENERAR REPORTES DE TABLA DE SÍMBOLOS
                    // ========================================
                    System.out.println("\n═══════════════════════════════════════════════════════════");
                    System.out.println("TABLA DE SÍMBOLOS GENERADA");
                    System.out.println("═══════════════════════════════════════════════════════════\n");
                    
                        if (ErrorHandler.getTotalErrores() == 0) {
                            generarReportesTablaSimbolos(outDir, w);
                            System.out.println("\n La Compilación está sin errores");
                        } else {
                            ErrorHandler.generarReporte("output/reporte_errores.txt");
                    }
                } catch (Exception e) {
                    // Escritura del resultado en el archivo de salida
                    w.write("REJECTED: " + e.getMessage());
                    w.newLine();
                    w.flush();
                    // También imprimir en consola
                    System.err.println("REJECTED: " + e.getMessage());
                    
                    // Generar reporte de errores
                    ErrorHandler.generarReporte("output/reporte_errores.txt");
                    ErrorHandler.mostrarResumen();
                    
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
    
    // Método auxiliar para agregar indentación
    private static void agregarIndentacion(StringBuilder sb, int nivel) {
        for (int i = 0; i < nivel; i++) {
            sb.append("  ");
        }
    }
    
// *******************************************
/**
 *  metodo para la generación grafica del árbol sintáctico, mediante html
 * @param raiz El árbol sintáctico a graficar
 * @param archivoSalida Ruta del archivo HTML de salida
 * 
 */
    public static void generarHTMLArbol(arbol raiz, String archivoSalida) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida))) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("    <title>Árbol Sintáctico - Compilador</title>\n");
            writer.write("    <script src=\"https://d3js.org/d3.v7.min.js\"></script>\n");
            writer.write("    <style>\n");
            writer.write("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("        .node circle { fill: #6baed6; stroke: #3182bd; stroke-width: 1.5px; }\n");
            writer.write("        .node text { font-size: 12px; font-weight: bold; }\n");
            writer.write("        .link { fill: none; stroke: #ccc; stroke-width: 1.5px; }\n");
            writer.write("        .node rect { fill: #4CAF50; stroke: #388E3C; stroke-width: 2px; rx: 5; ry: 5; }\n");
            writer.write("        .node text { fill: white; font-weight: bold; }\n");
            writer.write("        .node.leaf rect { fill: #2196F3; }\n");
            writer.write("        .node.root rect { fill: #FF9800; }\n");
            writer.write("        .controls { margin: 20px 0; }\n");
            writer.write("        button { padding: 10px 15px; margin: 5px; background: #4CAF50; color: white; border: none; cursor: pointer; border-radius: 4px; }\n");
            writer.write("        button:hover { background: #45a049; }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("    <h1> Árbol Sintáctico Generado</h1>\n");
            writer.write("    <div class=\"controls\">\n");
            writer.write("        <button onclick=\"zoomIn()\">+ Zoom In</button>\n");
            writer.write("        <button onclick=\"zoomOut()\">- Zoom Out</button>\n");
            writer.write("        <button onclick=\"resetZoom()\"> Reset</button>\n");
            writer.write("        <button onclick=\"downloadSVG()\">Descarga del grafico</button>\n");
            writer.write("    </div>\n");
            writer.write("    <div id=\"tree-container\"></div>\n");
            writer.write("    <script>\n");
            
            // Generar los datos del árbol en formato JSON
            writer.write("        const treeData = ");
            writer.write(convertirArbolAJSON(raiz));
            writer.write(";\n\n");
            
            writer.write("        const margin = { top: 40, right: 120, bottom: 40, left: 120 };\n");
            writer.write("        const width = 2200 - margin.left - margin.right;\n");
            writer.write("        const height = 1200 - margin.top - margin.bottom;\n\n");
            
            writer.write("        const svg = d3.select('#tree-container')\n");
            writer.write("            .append('svg')\n");
            writer.write("            .attr('width', width + margin.left + margin.right)\n");
            writer.write("            .attr('height', height + margin.top + margin.bottom)\n");
            writer.write("            .append('g')\n");
            writer.write("            .attr('transform', `translate(${margin.left},${margin.top})`);\n\n");
            
            writer.write("        let zoom = d3.zoom()\n");
            writer.write("            .scaleExtent([0.1, 3])\n");
            writer.write("            .on('zoom', (event) => {\n");
            writer.write("                svg.attr('transform', event.transform);\n");
            writer.write("            });\n\n");
            
            writer.write("        d3.select('svg').call(zoom);\n\n");
            
            writer.write("        const root = d3.hierarchy(treeData);\n");
            writer.write("        const treeLayout = d3.tree()\n");
            writer.write("            .nodeSize([80, 220])\n");
            writer.write("            .separation((a, b) => a.parent === b.parent ? 1.5 : 2);\n");
            writer.write("        treeLayout(root);\n\n");
            
            writer.write("        // Enlaces\n");
            writer.write("        svg.selectAll('.link')\n");
            writer.write("            .data(root.links())\n");
            writer.write("            .enter()\n");
            writer.write("            .append('path')\n");
            writer.write("            .attr('class', 'link')\n");
            writer.write("            .attr('d', d3.linkHorizontal()\n");
            writer.write("                .x(d => d.y)\n");
            writer.write("                .y(d => d.x));\n\n");
            
            writer.write("        // Nodos\n");
            writer.write("        const node = svg.selectAll('.node')\n");
            writer.write("            .data(root.descendants())\n");
            writer.write("            .enter()\n");
            writer.write("            .append('g')\n");
            writer.write("            .attr('class', d => 'node ' + (d.children ? 'internal' : 'leaf') + (d.depth === 0 ? ' root' : ''))\n");
            writer.write("            .attr('transform', d => `translate(${d.y},${d.x})`);\n\n");
            
            writer.write("        // Rectángulos para nodos\n");
            writer.write("        node.append('rect')\n");
            writer.write("            .attr('width', d => Math.max(120, d.data.name.length * 9))\n");
            writer.write("            .attr('height', 45)\n");
            writer.write("            .attr('x', d => -Math.max(120, d.data.name.length * 9) / 2)\n");
            writer.write("            .attr('y', -22);\n\n");

            
            writer.write("        node.append('text')\n");
            writer.write("            .attr('dy', '0.35em')\n");
            writer.write("            .attr('text-anchor', 'middle')\n");
            writer.write("            .call(wrap, 110)\n");

            writer.write("            .text(d => {\n");
            writer.write("                let text = d.data.name;\n");
            writer.write("                if (d.data.value && d.data.value !== 'null') {\n");
            writer.write("                    text += '\\n' + d.data.value;\n");
            writer.write("                }\n");
            writer.write("                return text;\n");
            writer.write("            })\n");
            writer.write("            .attr('font-size', '11px')\n");
            writer.write("            .attr('fill', 'white');\n\n");
            
            writer.write("        // Funciones de control\n");
            writer.write("        function zoomIn() {\n");
            writer.write("            d3.select('svg').transition().duration(300).call(zoom.scaleBy, 1.3);\n");
            writer.write("        }\n");
            writer.write("        function zoomOut() {\n");
            writer.write("            d3.select('svg').transition().duration(300).call(zoom.scaleBy, 0.7);\n");
            writer.write("        }\n");
            writer.write("        function resetZoom() {\n");
            writer.write("            d3.select('svg').transition().duration(300).call(zoom.transform, d3.zoomIdentity);\n");
            writer.write("        }\n");
            writer.write("        function downloadSVG() {\n");
            writer.write("            const svgData = new XMLSerializer().serializeToString(document.querySelector('svg'));\n");
            writer.write("            const blob = new Blob([svgData], {type: 'image/svg+xml'});\n");
            writer.write("            const url = URL.createObjectURL(blob);\n");
            writer.write("            const a = document.createElement('a');\n");
            writer.write("            a.href = url;\n");
            writer.write("            a.download = 'arbol_sintactico.svg';\n");
            writer.write("            document.body.appendChild(a);\n");
            writer.write("            a.click();\n");
            writer.write("            document.body.removeChild(a);\n");
            writer.write("            URL.revokeObjectURL(url);\n");
            writer.write("        }\n");
            writer.write("        function wrap(text, width) {\n");
            writer.write("            text.each(function () {\n");
            writer.write("                const textSel = d3.select(this);\n");
            writer.write("                const words = textSel.text().split(/\\s+/).reverse();\n");
            writer.write("                let word;\n");
            writer.write("                let line = [];\n");
            writer.write("                let lineNumber = 0;\n");
            writer.write("                const lineHeight = 1.1;\n");
            writer.write("                const y = textSel.attr('y');\n");
            writer.write("                const dy = parseFloat(textSel.attr('dy'));\n");
            writer.write("                let tspan = textSel.text(null).append('tspan').attr('x', 0).attr('y', y).attr('dy', dy + 'em');\n");
            writer.write("                while (word = words.pop()) {\n");
            writer.write("                    line.push(word);\n");
            writer.write("                    tspan.text(line.join(' '));\n");
            writer.write("                    if (tspan.node().getComputedTextLength() > width) {\n");
            writer.write("                        line.pop();\n");
            writer.write("                        tspan.text(line.join(' '));\n");
            writer.write("                        line = [word];\n");
            writer.write("                        tspan = textSel.append('tspan').attr('x', 0).attr('y', y).attr('dy', ++lineNumber * lineHeight + dy + 'em').text(word);\n");
            writer.write("                    }\n");
            writer.write("                }\n");
            writer.write("            });\n");
            writer.write("        }\n");

            writer.write("    </script>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
            
            System.out.println("grafico generado: " + archivoSalida);
            
            
        } catch (IOException e) {
            System.err.println("Error al generar HTML: " + e.getMessage());
        }
    }

    // Convierte el árbol sintáctico a formato JSON
    public static String convertirArbolAJSON(arbol nodo) {
        StringBuilder json = new StringBuilder();
        json.append("{\"name\":\"");
        // campo "tipo" como nombre del nodo
        json.append(nodo.tipo.replace("\"", "\\\""));
        json.append("\", \"value\":\"");
        // campo "valor" si no es null
        json.append(nodo.valor != null ? nodo.valor.replace("\"", "\\\"") : "");
        json.append("\"");
        
        if (!nodo.hijos.isEmpty()) {
            json.append(", \"children\": [");
            for (int i = 0; i < nodo.hijos.size(); i++) {
                if (i > 0) json.append(", ");
                json.append(convertirArbolAJSON(nodo.hijos.get(i)));
            }
            json.append("]");
        }
        json.append("}");
        return json.toString();
    }

    // ========================================
    // MÉTODOS PARA TABLA DE SÍMBOLOS
    // ========================================

    /**
     * Registra un identificador en la tabla de símbolos.
     */
    private static void registrarIdentificador(String nombre, int linea, int columna) {
        Token token = new Token("IDENT", nombre, linea, columna);
        
        // Si no existe, agregarlo
        if (!tablaSimbolos.existeEnCualquierAlcance(nombre)) {
            tablaSimbolos.agregar(nombre, token);
            System.out.println("  [TABLA] Registrado: " + nombre);
        }
    }

    /**
     * Realiza análisis semántico básico del árbol sintáctico.
     */
    private static void analizarSemantico(arbol nodo) {
        if (nodo == null) return;
        
        // Verificar declaraciones y usos de variables
        if (nodo.tipo.equals("DECL_GLOBAL") || nodo.tipo.equals("DECL_LOCAL")) {
            String nombre = nodo.valor;
            Token token = tablaSimbolos.buscar(nombre);
            
            if (token != null) {
                // Obtener tipo de la declaración
                if (nodo.hijos.size() > 0) {
                    arbol tipoNodo = nodo.hijos.get(0);
                    if (tipoNodo.tipo.equals("TIPO")) {
                        token.setTipoVariable(tipoNodo.valor);
                        System.out.println("  [SEMÁNTICA] Declaración: " + nombre + " : " + tipoNodo.valor);
                    }
                }
                
                // Si hay inicialización (segundo hijo)
                if (nodo.hijos.size() > 1) {
                    token.setInicializado(true);
                    System.out.println("  [SEMÁNTICA] Inicialización: " + nombre);
                    // Validar compatibilidad de tipos entre la declaración y la expresión
                    arbol expr = nodo.hijos.get(1);
                    String tipoExpr = obtenerTipoExpresion(expr);
                    String tipoDecl = token.getTipoVariable();
                    if (!esAsignable(tipoDecl, tipoExpr)) {
                        String msg = String.format("Error de asignación: no se puede asignar %s a %s en '%s' (línea %d)",
                                tipoExpr, tipoDecl, nombre, nodo.linea);
                        ErrorHandler.agregarErrorSemantico(msg, nodo.linea, nodo.columna);
                    }
                }
            }
        }
        
        // Verificar uso de variables (IDENT en expresiones)
        if (nodo.tipo.equals("IDENT")) {
            String nombre = nodo.valor;
            Token token = tablaSimbolos.buscar(nombre);
            
            if (token == null) {
                System.err.println("  [ERROR] Variable no declarada: " + nombre + " (línea " + nodo.linea + ")");
                ErrorHandler.agregarErrorSemantico("Variable no declarada: " + nombre + " (línea " + nodo.linea + ")", nodo.linea, 0);
            }
        }
        
        // Procesar recursivamente los hijos
        for (arbol hijo : nodo.hijos) {
            analizarSemantico(hijo);
        }
    }

    // Determina el tipo de una expresión simple del AST
    private static String obtenerTipoExpresion(arbol nodo) {
        if (nodo == null) return "unknown";
        switch (nodo.tipo) {
            case "LITERAL_INT": return "int";
            case "LITERAL_FLOAT": return "float";
            case "LITERAL_BOOL": return "bool";
            case "LITERAL_CHAR": return "char";
            case "LITERAL_STRING": return "string";
            case "IDENT": {
                String nombre = nodo.valor;
                Token t = tablaSimbolos.buscar(nombre);
                if (t != null && t.getTipoVariable() != null && !t.getTipoVariable().isEmpty()) {
                    return t.getTipoVariable();
                }
                ErrorHandler.agregarErrorSemantico("Variable no declarada o sin tipo: " + nombre, nodo.linea, nodo.columna);
                return "unknown";
            }
            case "OPERACION": {
                if (nodo.hijos.size() == 0) return "unknown";
                String op = nodo.valor != null ? nodo.valor : "";
                // operadores lógicos/relacionales -> bool
                if (op.equals("OR") || op.equals("AND") || op.equals("==") || op.equals("!=") ||
                    op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=") || op.equals("NOT")) {
                    return "bool";
                }
                // unarios
                if (op.equals("-") || op.equals("++") || op.equals("--")) {
                    return obtenerTipoExpresion(nodo.hijos.get(0));
                }
                // binarios aritméticos: devolver tipo izquierdo (promover int->float if needed)
                if (nodo.hijos.size() >= 2) {
                    String izq = obtenerTipoExpresion(nodo.hijos.get(0));
                    String der = obtenerTipoExpresion(nodo.hijos.get(1));
                    if (izq.equals("float") || der.equals("float")) return "float";
                    return izq;
                }
                return "unknown";
            }
            default:
                return "unknown";
        }
    }

    // Comprueba si un valor de tipo src puede asignarse a dest
    private static boolean esAsignable(String dest, String src) {
        if (dest == null || src == null) return false;
        if (dest.equals(src)) return true;
        // permitir int -> float
        if (dest.equals("float") && src.equals("int")) return true;
        // permitir asignar a arreglos solo si ambos son arreglos del mismo base
        if (dest.endsWith("[]") && src.endsWith("[]")) {
            String b1 = dest.replace("[]", "");
            String b2 = src.replace("[]", "");
            return b1.equals(b2);
        }
        return false;
    }

    /**
     * Genera reportes de la tabla de símbolos.
     */
    private static void generarReportesTablaSimbolos(java.io.File outDir, java.io.BufferedWriter wPrincipal) throws IOException {
        
        // Mostrar reporte en consola
        //Opcional
        System.out.println(tablaSimbolos.generarReporte());
        
        // Reporte en texto
        try (java.io.BufferedWriter w = new java.io.BufferedWriter(
                new java.io.FileWriter(new java.io.File(outDir, "symbol_table.txt")))) {
            w.write(tablaSimbolos.generarReporte());
            System.out.println(" Reporte de tabla de símbolos (texto): output/symbol_table.txt");
        }
        
        // Reporte en JSON
        try (java.io.BufferedWriter w = new java.io.BufferedWriter(
                new java.io.FileWriter(new java.io.File(outDir, "symbol_table.json")))) {
            w.write(tablaSimbolos.generarReporteJSON());
            System.out.println(" Reporte de tabla de símbolos (JSON): output/symbol_table.json");
        }
    }

    /**
     * Genera código MIPS a partir del árbol sintáctico.
     * Esta es una versión básica para demostración.
     */
    private static void generarCodigoMIPS(arbol ast, java.io.File outDir) {
        try {
            System.out.println("  Iniciando generación de código MIPS...\n");
            
            // Crear el generador de código
            CodeGenerator generador = new CodeGenerator(tablaSimbolos);
            
            // Generar código a partir del AST
            generador.generarCodigo(ast);
            
            // Guardar el código MIPS en un archivo
            String rutaMIPS = new java.io.File(outDir, "codigo.asm").getAbsolutePath();
            generador.guardarCodigo(rutaMIPS);
            
            // Mostrar resumen
            System.out.println("\n  ═══════════════════════════════════════════════════════════");
            System.out.println("  Código MIPS generado exitosamente");
            System.out.println("  Archivo: output/codigo.asm");
            System.out.println("  ═══════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("  [ERROR] Fallo en generación de código MIPS: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}