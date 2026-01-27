package util;

import java.util.*;

/**
 * Clase que gestiona las tablas de símbolos para el compilador.
 * Mantiene múltiples tablas de símbolos organizadas por alcance (GLOBAL, LOCAL, etc.)
 * Cada tabla asocia identificadores con tokens que contienen información detallada.
 * @version 1.0
 * @author Duan Antonio Espinoza
 */
public class SymbolTable {
    private Map<String, Table> tablas;           // Tablas organizadas por alcance
    private String alcanceActual;                // Alcance actual
    private Stack<String> pilaAlcances;          // Pila para manejar alcances anidados
    private int contadorTokens;                  // Contador para IDs únicos de tokens
    private List<String> historialOperaciones;   // Historial de operaciones
    
    /**
     * Clase interna que representa una tabla de símbolos para un alcance específico
     */
    public static class Table {
        private String nombre;                   // Nombre del alcance (GLOBAL, LOCAL, etc.)
        private Map<String, Token> simbolos;     // Mapa de identificador -> Token
        private int nivel;                       // Nivel de anidamiento
        private String tipoAlcance;              // GLOBAL, LOCAL, PARAMETRO, FUNCION
        private String observaciones;            // Información adicional
        
        public Table(String nombre, int nivel, String tipoAlcance) {
            this.nombre = nombre;
            this.nivel = nivel;
            this.tipoAlcance = tipoAlcance;
            this.simbolos = new LinkedHashMap<>();
            this.observaciones = "";
        }
        
        public void agregar(String id, Token token) {
            simbolos.put(id, token);
        }
        
        public Token buscar(String id) {
            return simbolos.get(id);
        }
        
        public boolean existe(String id) {
            return simbolos.containsKey(id);
        }
        
        public void eliminar(String id) {
            simbolos.remove(id);
        }
        
        public Collection<Token> obtenerTodos() {
            return simbolos.values();
        }
        
        public int obtenerSize() {
            return simbolos.size();
        }
        
        public String getNombre() { return nombre; }
        public int getNivel() { return nivel; }
        public String getTipoAlcance() { return tipoAlcance; }
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String obs) { this.observaciones = obs; }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n╔════════════════════════════════════╗\n");
            sb.append(String.format("║ TABLA: %s (Nivel %d - %s)\n", nombre, nivel, tipoAlcance));
            sb.append("╠════════════════════════════════════╣\n");
            
            if (simbolos.isEmpty()) {
                sb.append("║ (Tabla vacía)\n");
            } else {
                for (Map.Entry<String, Token> entry : simbolos.entrySet()) {
                    Token token = entry.getValue();
                    String linea = String.format("║ %s : %s (línea %d)\n",
                        entry.getKey(), token.getTipo(), token.getLinea());
                    sb.append(linea);
                }
            }
            sb.append("╚════════════════════════════════════╝");
            return sb.toString();
        }
    }
    
    // Constructor
    public SymbolTable() {
        this.tablas = new LinkedHashMap<>();
        this.alcanceActual = "GLOBAL";
        this.pilaAlcances = new Stack<>();
        this.contadorTokens = 0;
        this.historialOperaciones = new ArrayList<>();
        
        // Crear tabla global
        Table tablaGlobal = new Table("GLOBAL", 0, "GLOBAL");
        tablas.put("GLOBAL", tablaGlobal);
        pilaAlcances.push("GLOBAL");
    }
    
    /**
     * Crea un nuevo alcance (para funciones, bloques, etc.)
     */
    public void crearAlcance(String nombre, String tipo) {
        int nivel = pilaAlcances.size();
        Table tabla = new Table(nombre, nivel, tipo);
        tablas.put(nombre, tabla);
        pilaAlcances.push(nombre);
        alcanceActual = nombre;
        historialOperaciones.add("CREAR ALCANCE: " + nombre + " (Nivel: " + nivel + ")");
    }
    
    /**
     * Sale del alcance actual
     */
    public void salirAlcance() {
        if (pilaAlcances.size() > 1) {
            String anterior = pilaAlcances.pop();
            alcanceActual = pilaAlcances.peek();
            historialOperaciones.add("SALIR ALCANCE: " + anterior + " -> " + alcanceActual);
        }
    }
    
    /**
     * Agrega un token a la tabla actual
     */
    public void agregar(String id, Token token) {
        Table tabla = tablas.get(alcanceActual);
        if (tabla != null) {
            token.setId(contadorTokens++);
            token.setAlcance(alcanceActual);
            tabla.agregar(id, token);
            historialOperaciones.add("AGREGAR: " + id + " en " + alcanceActual);
        }
    }
    
    /**
     * Busca un token en el alcance actual y padres
     */
    public Token buscar(String id) {
        // Busca en el alcance actual hacia arriba en la pila
        Stack<String> pila_temp = (Stack<String>) pilaAlcances.clone();
        while (!pila_temp.isEmpty()) {
            String alcance = pila_temp.pop();
            Table tabla = tablas.get(alcance);
            if (tabla != null) {
                Token token = tabla.buscar(id);
                if (token != null) {
                    return token;
                }
            }
        }
        return null;
    }
    
    /**
     * Busca solo en el alcance actual
     */
    public Token buscarLocal(String id) {
        Table tabla = tablas.get(alcanceActual);
        if (tabla != null) {
            return tabla.buscar(id);
        }
        return null;
    }
    
    /**
     * Verifica si existe un token en el alcance actual
     */
    public boolean existe(String id) {
        return buscarLocal(id) != null;
    }
    
    /**
     * Verifica si existe un token en cualquier alcance
     */
    public boolean existeEnCualquierAlcance(String id) {
        return buscar(id) != null;
    }
    
    /**
     * Obtiene la tabla del alcance especificado
     */
    public Table obtenerTabla(String alcance) {
        return tablas.get(alcance);
    }
    
    /**
     * Obtiene la tabla actual
     */
    public Table obtenerTablaActual() {
        return tablas.get(alcanceActual);
    }
    
    /**
     * Obtiene el alcance actual
     */
    public String obtenerAlcanceActual() {
        return alcanceActual;
    }
    
    /**
     * Obtiene todas las tablas
     */
    public Collection<Table> obtenerTodasLasTablas() {
        return tablas.values();
    }
    
    /**
     * Imprime un resumen de todas las tablas
     */
    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════\n");
        sb.append("                    REPORTE DE TABLAS DE SÍMBOLOS\n");
        sb.append("═══════════════════════════════════════════════════════════\n");
        
        for (Table tabla : tablas.values()) {
            sb.append(tabla.toString()).append("\n");
        }
        
        sb.append("\n═════════════════════════════════════════════════════════════\n");
        sb.append("                    HISTORIAL DE OPERACIONES\n");
        sb.append("═════════════════════════════════════════════════════════════\n");
        
        for (int i = 0; i < historialOperaciones.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, historialOperaciones.get(i)));
        }
        
        sb.append("\n═════════════════════════════════════════════════════════════\n");
        sb.append(String.format("Total de tokens registrados: %d\n", contadorTokens));
        sb.append(String.format("Total de tablas: %d\n", tablas.size()));
        sb.append(String.format("Alcance actual: %s\n", alcanceActual));
        sb.append("═════════════════════════════════════════════════════════════\n");
        
        return sb.toString();
    }
    
    /**
     * Genera un reporte en formato JSON
     */
    public String generarReporteJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"tablas_simbolos\": [\n");
        
        List<Table> listaTablas = new ArrayList<>(tablas.values());
        for (int i = 0; i < listaTablas.size(); i++) {
            Table tabla = listaTablas.get(i);
            sb.append("    {\n");
            sb.append("      \"nombre\": \"").append(tabla.nombre).append("\",\n");
            sb.append("      \"nivel\": ").append(tabla.nivel).append(",\n");
            sb.append("      \"tipo\": \"").append(tabla.tipoAlcance).append("\",\n");
            sb.append("      \"simbolos\": [\n");
            
            List<Map.Entry<String, Token>> entradas = new ArrayList<>(tabla.simbolos.entrySet());
            for (int j = 0; j < entradas.size(); j++) {
                Map.Entry<String, Token> entry = entradas.get(j);
                Token token = entry.getValue();
                sb.append("        {\n");
                sb.append("          \"id\": \"").append(entry.getKey()).append("\",\n");
                sb.append("          \"tipo_token\": \"").append(token.getTipo()).append("\",\n");
                sb.append("          \"lexema\": \"").append(token.getLexema()).append("\",\n");
                sb.append("          \"valor\": \"").append(token.getValor()).append("\",\n");
                sb.append("          \"linea\": ").append(token.getLinea()).append(",\n");
                sb.append("          \"columna\": ").append(token.getColumna()).append(",\n");
                sb.append("          \"tipo_variable\": \"").append(token.getTipoVariable()).append("\",\n");
                sb.append("          \"es_arreglo\": ").append(token.esArreglo()).append(",\n");
                sb.append("          \"inicializado\": ").append(token.estaInicializado()).append("\n");
                sb.append("        }");
                if (j < entradas.size() - 1) sb.append(",");
                sb.append("\n");
            }
            
            sb.append("      ]\n");
            sb.append("    }");
            if (i < listaTablas.size() - 1) sb.append(",");
            sb.append("\n");
        }
        
        sb.append("  ],\n");
        sb.append("  \"total_tokens\": ").append(contadorTokens).append(",\n");
        sb.append("  \"alcance_actual\": \"").append(alcanceActual).append("\"\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * Limpia todos los datos
     */
    public void limpiar() {
        tablas.clear();
        alcanceActual = "GLOBAL";
        pilaAlcances.clear();
        contadorTokens = 0;
        historialOperaciones.clear();
        
        Table tablaGlobal = new Table("GLOBAL", 0, "GLOBAL");
        tablas.put("GLOBAL", tablaGlobal);
        pilaAlcances.push("GLOBAL");
    }
}
