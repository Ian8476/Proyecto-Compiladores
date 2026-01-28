package util;

import arbolSintactico.arbol;
import java.util.*;
import java.io.*;

/**
 * Generador de código MIPS básico.
 * Realiza un análisis semántico simple y genera código MIPS de ejemplo.
 * Esta es una versión MUY básica para demostración.
 * 
 * @author Duan Antonio Espinoza
 * @version 0.1
 */
public class CodeGenerator {
    private StringBuilder codigo;
    private int registroContador = 1;           // Contador para registros temporales
    private Stack<Integer> registrosDisponibles; // Pila de registros disponibles
    private int labelContador = 0;              // Contador para labels
    private SymbolTable tablaSimbolos;
    private int offsetMemoria = 0;              // Offset para variables en la pila
    
    public CodeGenerator(SymbolTable tablaSimbolos) {
        this.codigo = new StringBuilder();
        this.tablaSimbolos = tablaSimbolos;
        this.registrosDisponibles = new Stack<>();
        inicializarRegistros();
        generarPrologo();
    }
    
    /**
     * Inicializa los registros disponibles (del $t0 al $t9)
     */
    private void inicializarRegistros() {
        for (int i = 9; i >= 0; i--) {
            registrosDisponibles.push(i);
        }
    }
    
    /**
     * Genera el prólogo del programa MIPS
     */
    private void generarPrologo() {
        emitir(".data");
        emitir("    # Sección de datos - variables globales irían aquí");
        emitir("    newline: .asciiz \"\\n\"");
        emitir("");
        emitir(".text");
        emitir("    .globl main");
        emitir("    main:");
        emitir("        # Prólogo: guardar registros y ajustar stack pointer");
        emitir("        addi $sp, $sp, -4          # Reservar espacio en la pila");
        emitir("        sw $ra, 0($sp)             # Guardar dirección de retorno");
        emitir("");
    }
    
    /**
     * Genera el epílogo del programa MIPS
     */
    private void generarEpilogo() {
        emitir("");
        emitir("        # Epílogo: restaurar registros y retornar");
        emitir("        lw $ra, 0($sp)             # Restaurar dirección de retorno");
        emitir("        addi $sp, $sp, 4           # Liberar espacio en la pila");
        emitir("        jr $ra                     # Retornar al sistema operativo");
        emitir("");
        emitir("    # Fin del programa");
    }
    
    /**
     * Método principal que genera código para un árbol completo
     */
    public void generarCodigo(arbol nodo) {
        if (nodo == null) return;
        
        System.out.println("  [GENERACIÓN] Procesando nodo: " + nodo.tipo);
        
        switch (nodo.tipo) {
            case "PROGRAM":
                generarProgram(nodo);
                break;
            case "DECL_GLOBAL":
                generarDeclGlobal(nodo);
                break;
            case "MAIN":
                generarMain(nodo);
                break;
            case "OPERACION":
                generarOperacion(nodo);
                break;
            case "ASIGNACION":
                generarAsignacion(nodo);
                break;
            case "LITERAL_INT":
                generarLiteralInt(nodo);
                break;
            case "IDENT":
                generarIdent(nodo);
                break;
            default:
                // Para otros nodos, procesamos recursivamente
                for (arbol hijo : nodo.hijos) {
                    generarCodigo(hijo);
                }
        }
    }
    
    /**
     * Genera código para el programa completo
     */
    private void generarProgram(arbol nodo) {
        System.out.println("  [SEMÁNTICA] Analizando PROGRAM");
        
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
        
        generarEpilogo();
    }
    
    /**
     * Genera código para declaraciones globales
     */
    private void generarDeclGlobal(arbol nodo) {
        String nomVariable = nodo.valor;
        System.out.println("  [SEMÁNTICA] Declaración global de: " + nomVariable);
        
        emitir("    # Declaración global: " + nomVariable);
        
        if (nodo.hijos.size() > 0) {
            arbol tipo = nodo.hijos.get(0);
            System.out.println("    - Tipo: " + tipo.valor);
            
            // Si hay inicialización (segundo hijo)
            if (nodo.hijos.size() > 1) {
                arbol inicializacion = nodo.hijos.get(1);
                System.out.println("    - Con inicialización");
                generarCodigo(inicializacion);
                emitir("    # Fin inicialización de " + nomVariable);
            }
        }
    }
    
    /**
     * Genera código para la función main
     */
    private void generarMain(arbol nodo) {
        System.out.println("  [SEMÁNTICA] Analizando MAIN");
        
        // Procesar los hijos (normalmente un BLOQUE)
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
    }
    
    /**
     * Genera código para operaciones binarias y unarias
     */
    private void generarOperacion(arbol nodo) {
        String operador = nodo.valor;
        System.out.println("  [SEMÁNTICA] Operación: " + operador);
        
        if (nodo.hijos.size() == 2) {
            // Operación binaria
            arbol izq = nodo.hijos.get(0);
            arbol der = nodo.hijos.get(1);
            
            System.out.println("    - Operación binaria: " + izq.tipo + " " + operador + " " + der.tipo);
            
            switch (operador) {
                case "+":
                    emitir("    # Operación: suma");
                    generarCodigo(izq);
                    generarCodigo(der);
                    emitir("    add $t0, $t0, $t1");
                    break;
                case "-":
                    emitir("    # Operación: resta");
                    generarCodigo(izq);
                    generarCodigo(der);
                    emitir("    sub $t0, $t0, $t1");
                    break;
                case "*":
                    emitir("    # Operación: multiplicación");
                    generarCodigo(izq);
                    generarCodigo(der);
                    emitir("    mult $t0, $t1");
                    emitir("    mflo $t0");
                    break;
                case "/":
                    emitir("    # Operación: división");
                    generarCodigo(izq);
                    generarCodigo(der);
                    emitir("    div $t0, $t1");
                    emitir("    mflo $t0");
                    break;
                case "==":
                    emitir("    # Operación: igualdad");
                    generarCodigo(izq);
                    generarCodigo(der);
                    emitir("    beq $t0, $t1, " + generarLabel());
                    break;
                case "<":
                    emitir("    # Operación: menor que");
                    generarCodigo(izq);
                    generarCodigo(der);
                    emitir("    blt $t0, $t1, " + generarLabel());
                    break;
                default:
                    emitir("    # Operación: " + operador);
                    break;
            }
        } else if (nodo.hijos.size() == 1) {
            // Operación unaria
            arbol operando = nodo.hijos.get(0);
            System.out.println("    - Operación unaria: " + operador);
            
            switch (operador) {
                case "-":
                    emitir("    # Operación: negación");
                    generarCodigo(operando);
                    emitir("    neg $t0, $t0");
                    break;
                case "NOT":
                    emitir("    # Operación: NOT lógico");
                    generarCodigo(operando);
                    emitir("    seqz $t0, $t0");
                    break;
                default:
                    emitir("    # Operación unaria: " + operador);
                    break;
            }
        }
    }
    
    /**
     * Genera código para asignaciones
     */
    private void generarAsignacion(arbol nodo) {
        String variable = nodo.valor;
        System.out.println("  [SEMÁNTICA] Asignación a: " + variable);
        
        if (nodo.hijos.size() > 0) {
            arbol expresion = nodo.hijos.get(0);
            System.out.println("    - Expresión: " + expresion.tipo);
            
            emitir("    # Asignación: " + variable + " = ...");
            generarCodigo(expresion);
            emitir("    # Resultado en $t0, guardando en variable: " + variable);
            offsetMemoria += 4;
            emitir("    sw $t0, " + (-offsetMemoria) + "($sp)   # Guardar valor de " + variable);
        }
    }
    
    /**
     * Genera código para literales enteros
     */
    private void generarLiteralInt(arbol nodo) {
        String valor = nodo.valor;
        System.out.println("  [SEMÁNTICA] Literal INT: " + valor);
        
        emitir("    li $t0, " + valor + "          # Cargar literal: " + valor);
    }
    
    /**
     * Genera código para identificadores
     */
    private void generarIdent(arbol nodo) {
        String variable = nodo.valor;
        System.out.println("  [SEMÁNTICA] Referencia a variable: " + variable);
        
        // Verificar en tabla de símbolos (análisis semántico básico)
        SymbolTable.Table tablasGlobales = tablaSimbolos.obtenerTabla("GLOBAL");
        if (tablasGlobales != null && tablasGlobales.existe(variable)) {
            Token token = tablasGlobales.buscar(variable);
            System.out.println("    - Variable encontrada en tabla de símbolos");
            System.out.println("    - Tipo: " + token.getTipo());
        } else {
            System.out.println("    [ADVERTENCIA] Variable '" + variable + "' no encontrada en tabla de símbolos");
        }
        
        emitir("    lw $t0, 0($sp)                 # Cargar variable: " + variable);
    }
    
    /**
     * Emite una línea de código MIPS
     */
    private void emitir(String linea) {
        codigo.append(linea).append("\n");
    }
    
    /**
     * Genera un nuevo label único
     */
    private String generarLabel() {
        return "L" + (labelContador++);
    }
    
    /**
     * Obtiene el registro temporal siguiente
     */
    private int obtenerRegistro() {
        if (registrosDisponibles.isEmpty()) {
            registroContador++;
            return registroContador - 1;
        }
        return registrosDisponibles.pop();
    }
    
    /**
     * Obtiene el código MIPS generado
     */
    public String obtenerCodigo() {
        return codigo.toString();
    }
    
    /**
     * Guarda el código MIPS en un archivo
     */
    public void guardarCodigo(String rutaArchivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            writer.write(obtenerCodigo());
            System.out.println("\n  Código MIPS guardado en: " + rutaArchivo);
        }
    }
}
