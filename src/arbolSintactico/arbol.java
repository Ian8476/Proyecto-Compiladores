package arbolSintactico;
import java.util.ArrayList;
import java.util.List;

public class arbol {
    public String tipo;              // tipo de nodo (PROGRAM, DECL_GLOBAL, TIPO, etc)
    public String valor;             // valor (identificador, literal, etc)
    public List<arbol> hijos;        // nodos hijos
    public int linea, columna;       // posición en código fuente (opcional)
    
    // Constructor básico
    public arbol(String tipo) {
        this.tipo = tipo;
        this.valor = null;
        this.hijos = new ArrayList<>();
        this.linea = 0;
        this.columna = 0;
    }
    
    // Constructor con valor
    public arbol(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.hijos = new ArrayList<>();
        this.linea = 0;
        this.columna = 0;
    }
    
    // Constructor con linea y columna
    public arbol(String tipo, String valor, int linea, int columna) {
        this.tipo = tipo;
        this.valor = valor;
        this.hijos = new ArrayList<>();
        this.linea = linea;
        this.columna = columna;
    }
    
    // Agregar hijo
    public void agregarHijo(arbol hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }
    
    // Convertir a string con indentación
    @Override
    public String toString() {
        return toString(0);
    }
    
    public String toString(int nivel) {
        String indent = "  ".repeat(nivel);
        String s = indent + tipo + (valor != null ? " [" + valor + "]" : "");
        if (linea > 0) {
            s += " (" + linea + "," + columna + ")";
        }
        s += "\n";
        
        for (arbol hijo : hijos) {
            s += hijo.toString(nivel + 1);
        }
        return s;
    }
    
    // Convertir a JSON
    public String toJSON() {
        StringBuilder json = new StringBuilder();
        json.append("{\"tipo\":\"").append(escaparJSON(tipo)).append("\"");
        
        if (valor != null && !valor.isEmpty()) {
            json.append(",\"valor\":\"").append(escaparJSON(valor)).append("\"");
        }
        
        if (!hijos.isEmpty()) {
            json.append(",\"hijos\":[");
            for (int i = 0; i < hijos.size(); i++) {
                json.append(hijos.get(i).toJSON());
                if (i < hijos.size() - 1) json.append(",");
            }
            json.append("]");
        }
        
        json.append("}");
        return json.toString();
    }
    
    // Escapar caracteres especiales para JSON
    private String escaparJSON(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
