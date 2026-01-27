package util;

/**
 * Clase que representa un token con toda su información.
 * Almacena los datos asociados a cada símbolo identificado por el lexer.
 * @version 1.0
 * @author Duan Antonio Espinoza
 */
public class Token {
    private int id;                  // ID único del token
    private String tipo;             // Tipo de token (IDENT, INT_LIT, FLOAT_LIT, etc.)
    private String lexema;           // El valor/lexema del token
    private String valor;            // Valor interpretado (para literales)
    private int linea;               // Línea en el código fuente
    private int columna;             // Columna en el código fuente
    private String alcance;          // GLOBAL, LOCAL, PARAMETRO
    private String tipoVariable;     // El tipo de dato si es variable (int, float, bool, etc.)
    private boolean esArreglo;       // Si es un arreglo
    private String dimensiones;      // Dimensiones si es arreglo (ej: "10x20")
    private boolean inicializado;    // Si ha sido inicializado
    private boolean esConstante;     // Si es una constante
    private String observaciones;    // Observaciones adicionales
    
    // Constructor completo
    public Token(int id, String tipo, String lexema, String valor, 
                 int linea, int columna, String alcance) {
        this.id = id;
        this.tipo = tipo;
        this.lexema = lexema;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
        this.alcance = alcance;
        this.tipoVariable = "";
        this.esArreglo = false;
        this.dimensiones = "";
        this.inicializado = false;
        this.esConstante = false;
        this.observaciones = "";
    }
    
    // Constructor simplificado
    public Token(String tipo, String lexema, int linea, int columna) {
        this.id = -1;
        this.tipo = tipo;
        this.lexema = lexema;
        this.valor = lexema;
        this.linea = linea;
        this.columna = columna;
        this.alcance = "";
        this.tipoVariable = "";
        this.esArreglo = false;
        this.dimensiones = "";
        this.inicializado = false;
        this.esConstante = false;
        this.observaciones = "";
    }
    
    // Getters
    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public String getLexema() { return lexema; }
    public String getValor() { return valor; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public String getAlcance() { return alcance; }
    public String getTipoVariable() { return tipoVariable; }
    public boolean esArreglo() { return esArreglo; }
    public String getDimensiones() { return dimensiones; }
    public boolean estaInicializado() { return inicializado; }
    public boolean esConstante() { return esConstante; }
    public String getObservaciones() { return observaciones; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setValor(String valor) { this.valor = valor; }
    public void setAlcance(String alcance) { this.alcance = alcance; }
    public void setTipoVariable(String tipo) { this.tipoVariable = tipo; }
    public void setEsArreglo(boolean es) { this.esArreglo = es; }
    public void setDimensiones(String dims) { this.dimensiones = dims; }
    public void setInicializado(boolean init) { this.inicializado = init; }
    public void setEsConstante(boolean cons) { this.esConstante = cons; }
    public void setObservaciones(String obs) { this.observaciones = obs; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Token{")
          .append("id=").append(id)
          .append(", tipo='").append(tipo).append('\'')
          .append(", lexema='").append(lexema).append('\'')
          .append(", valor='").append(valor).append('\'')
          .append(", línea=").append(linea)
          .append(", columna=").append(columna)
          .append(", alcance='").append(alcance).append('\'')
          .append(", tipoVariable='").append(tipoVariable).append('\'')
          .append(", esArreglo=").append(esArreglo)
          .append(", dimensiones='").append(dimensiones).append('\'')
          .append(", inicializado=").append(inicializado)
          .append(", esConstante=").append(esConstante)
          .append('}');
        return sb.toString();
    }
    
    public String toDetailedString() {
        return String.format(
            "┌─ Token #%d\n" +
            "│  Tipo: %s\n" +
            "│  Lexema: '%s'\n" +
            "│  Valor: '%s'\n" +
            "│  Ubicación: línea %d, columna %d\n" +
            "│  Alcance: %s\n" +
            "│  Tipo Variable: %s\n" +
            "│  Es Arreglo: %s\n" +
            "│  Dimensiones: %s\n" +
            "│  Inicializado: %s\n" +
            "│  Es Constante: %s\n" +
            "│  Observaciones: %s\n" +
            "└─",
            id, tipo, lexema, valor, linea, columna, alcance, tipoVariable,
            esArreglo, dimensiones, inicializado, esConstante, observaciones
        );
    }
}
