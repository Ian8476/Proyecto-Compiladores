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
                            // Después de guardar el árbol como JSON
                            generarHTMLArbol(ast, "output/arbol_interactivo.html");
                            System.out.println("\n=== grafico html generado ===");
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
            writer.write("    <h1>🌳 Árbol Sintáctico Generado</h1>\n");
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
            writer.write("        const width = 1600 - margin.left - margin.right;\n");
            writer.write("        const height = 800 - margin.top - margin.bottom;\n\n");
            
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
            writer.write("        const treeLayout = d3.tree().size([height, width - 200]);\n");
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
            writer.write("            .attr('width', d => Math.max(100, d.data.name.length * 8))\n");
            writer.write("            .attr('height', 40)\n");
            writer.write("            .attr('x', -50)\n");
            writer.write("            .attr('y', -20);\n\n");
            
            writer.write("        // Texto de nodos\n");
            writer.write("        node.append('text')\n");
            writer.write("            .attr('dy', '.35em')\n");
            writer.write("            .attr('text-anchor', 'middle')\n");
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
            writer.write("    </script>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
            
            System.out.println("grafico generado: " + archivoSalida);
            
            
        } catch (IOException e) {
            System.err.println("Error al generar HTML: " + e.getMessage());
        }
    }

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




}