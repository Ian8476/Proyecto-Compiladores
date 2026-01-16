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
}
