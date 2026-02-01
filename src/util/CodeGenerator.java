package util;

import arbolSintactico.arbol;
import java.util.*;
import java.io.*;

/**
 * Generador de código MIPS mejorado.
 * Realiza análisis semántico y genera código MIPS con manejo de:
 * - Variables globales y locales
 * - Funciones y calls
 * - Operaciones aritméticas y lógicas
 * - Arreglos bidimensionales
 * - Estructuras de control (decide, loop, for)
 * 
 * @author Duan Antonio Espinoza
 * @version 0.2
 */
public class CodeGenerator {
    private StringBuilder codigo;
    private StringBuilder datosGlobales;        // Sección .data
    private int registroContador = 1;           // Contador para registros temporales
    private Stack<Integer> registrosDisponibles; // Pila de registros disponibles
    private int labelContador = 0;              // Contador para labels
    private SymbolTable tablaSimbolos;
    private int offsetMemoria = 0;              // Offset para variables en la pila
    private Map<String, Integer> offsetVariables; // Mapeo variable -> offset en stack
    private int profundidadBloque = 0;          // Para manejo de variables locales
    
    public CodeGenerator(SymbolTable tablaSimbolos) {
        this.codigo = new StringBuilder();
        this.datosGlobales = new StringBuilder();
        this.tablaSimbolos = tablaSimbolos;
        this.registrosDisponibles = new Stack<>();
        this.offsetVariables = new HashMap<>();
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
        // Sección de datos
        datosGlobales.append(".data\n");
        datosGlobales.append("    # Sección de datos - variables globales\n");
        datosGlobales.append("    newline: .asciiz \"\\n\"\n");
        datosGlobales.append("    space: .asciiz \" \"\n");
        datosGlobales.append("    prompt_write: .asciiz \"Valor: \"\n");
        
        // Sección de código
        emitir(".text");
        emitir("    .globl main");
        emitir("    main:");
        emitir("        # Prólogo: guardar registros y ajustar stack pointer");
        emitir("        addi $sp, $sp, -8          # Reservar espacio en la pila");
        emitir("        sw $ra, 4($sp)             # Guardar dirección de retorno");
        emitir("        sw $fp, 0($sp)             # Guardar frame pointer");
        emitir("        addi $fp, $sp, 8           # Establecer nuevo frame pointer");
        emitir("");
    }
    
    /**
     * Genera el epílogo del programa MIPS
     */
    private void generarEpilogo() {
        emitir("");
        emitir("        # Epílogo: restaurar registros y retornar");
        emitir("        lw $ra, 4($sp)             # Restaurar dirección de retorno");
        emitir("        lw $fp, 0($sp)             # Restaurar frame pointer");
        emitir("        addi $sp, $sp, 8           # Liberar espacio en la pila");
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
            case "GLOBALES":
                generarGlobales(nodo);
                break;
            case "DECL_GLOBAL":
                generarDeclGlobal(nodo);
                break;
            case "FUNCIONES":
                generarFunciones(nodo);
                break;
            case "FUNCION":
                generarFuncion(nodo);
                break;
            case "MAIN":
                generarMain(nodo);
                break;
            case "BLOQUE":
                generarBloque(nodo);
                break;
            case "SENTENCIAS":
                generarSentencias(nodo);
                break;
            case "DECL_LOCAL":
                generarDeclLocal(nodo);
                break;
            case "ASIGNACION":
                generarAsignacion(nodo);
                break;
            case "ASIGNACION_ARRAY":
                generarAsignacionArray(nodo);
                break;
            case "OPERACION":
                generarOperacion(nodo);
                break;
            case "LITERAL_INT":
                generarLiteralInt(nodo);
                break;
            case "LITERAL_FLOAT":
                generarLiteralFloat(nodo);
                break;
            case "LITERAL_BOOL":
                generarLiteralBool(nodo);
                break;
            case "IDENT":
                generarIdent(nodo);
                break;
            case "LLAMADA":
                generarLlamada(nodo);
                break;
            case "ARRAY_ACCESS":
                generarArrayAccess(nodo);
                break;
            case "DECIDE":
                generarDecide(nodo);
                break;
            case "LOOP":
                generarLoop(nodo);
                break;
            case "FOR":
                generarFor(nodo);
                break;
            case "RETURN":
                generarReturn(nodo);
                break;
            case "BREAK":
                emitir("    j " + generarLabel());
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
     * Genera literales enteros
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
     * Obtiene el código MIPS generado (combinando datos y código)
     */
    public String obtenerCodigo() {
        return datosGlobales.toString() + "\n" + codigo.toString();
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
    
    // ===== MÉTODOS PARA MANEJO DE GLOBALES =====
    
    /**
     * código para el bloque de globales
     */
    private void generarGlobales(arbol nodo) {
        System.out.println("  [GENERACIÓN] Analizando declaraciones globales");
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
    }
    
    // ===== MÉTODOS PARA MANEJO DE FUNCIONES =====
    
    /**
     *  código para el bloque de funciones
     */
    private void generarFunciones(arbol nodo) {
        System.out.println("  [GENERACIÓN] Analizando funciones definidas");
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
    }
    
    /**
     * código para una función individual
     */
    private void generarFuncion(arbol nodo) {
        String nombreFunc = nodo.valor;
        System.out.println("  [GENERACIÓN] Función: " + nombreFunc);
        
        emitir("");
        emitir("    # Función: " + nombreFunc);
        emitir("    " + nombreFunc + ":");
        emitir("        addi $sp, $sp, -4");
        emitir("        sw $ra, 0($sp)");
        
        // Procesar bloque de función
        for (arbol hijo : nodo.hijos) {
            if (hijo.tipo.equals("BLOQUE")) {
                generarCodigo(hijo);
            }
        }
        
        emitir("        # Retorno de función");
        emitir("        lw $ra, 0($sp)");
        emitir("        addi $sp, $sp, 4");
        emitir("        jr $ra");
    }
    
    /**
     * Genera código para bloques de código
     */
    private void generarBloque(arbol nodo) {
        System.out.println("  [GENERACIÓN] Abriendo bloque");
        profundidadBloque++;
        
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
        
        profundidadBloque--;
    }
    
    /**
     * Genera código para lista de sentencias
     */
    private void generarSentencias(arbol nodo) {
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
    }
    
    /**
     * Genera código para declaraciones locales
     */
    private void generarDeclLocal(arbol nodo) {
        String nomVariable = nodo.valor;
        System.out.println("  [SEMÁNTICA] Declaración local: " + nomVariable);
        
        // Registrar offset de variable local
        offsetVariables.put(nomVariable, offsetMemoria);
        offsetMemoria += 4;
        
        emitir("    # Declaración local: " + nomVariable);
        
        if (nodo.hijos.size() > 1) {
            // Tiene inicialización
            arbol inicializacion = nodo.hijos.get(1);
            System.out.println("    - Con inicialización");
            generarCodigo(inicializacion);
            emitir("    sw $t0, " + (-offsetVariables.get(nomVariable)) + "($fp)");
        }
    }
    
    /**
     * Genera código para asignaciones mejorado
     */
    private void generarAsignacion(arbol nodo) {
        String variable = nodo.valor;
        System.out.println("  [SEMÁNTICA] Asignación a: " + variable);
        
        if (nodo.hijos.size() > 0) {
            arbol expresion = nodo.hijos.get(0);
            System.out.println("    - Expresión: " + expresion.tipo);
            
            emitir("    # Asignación: " + variable + " = ...");
            generarCodigo(expresion);
            
            if (offsetVariables.containsKey(variable)) {
                // Variable local
                emitir("    sw $t0, " + (-offsetVariables.get(variable)) + "($fp)   # Guardar " + variable);
            } else {
                // Variable global
                emitir("    sw $t0, -4($gp)            # Guardar variable global: " + variable);
            }
        }
    }
    
    /**
     * Genera código para asignaciones de arreglos
     */
    private void generarAsignacionArray(arbol nodo) {
        System.out.println("  [SEMÁNTICA] Asignación a arreglo");
        emitir("    # Asignación a arreglo");
        // Procesar los hijos recursivamente
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
    }
    
    /**
     * Genera código para literales flotantes
     */
    private void generarLiteralFloat(arbol nodo) {
        String valor = nodo.valor;
        System.out.println("  [SEMÁNTICA] Literal FLOAT: " + valor);
        emitir("    li.s $f0, " + valor + "        # Cargar literal float: " + valor);
    }
    
    /**
     * Genera código para literales booleanos
     */
    private void generarLiteralBool(arbol nodo) {
        String valor = nodo.valor;
        System.out.println("  [SEMÁNTICA] Literal BOOL: " + valor);
        int valorInt = valor.equalsIgnoreCase("true") ? 1 : 0;
        emitir("    li $t0, " + valorInt + "          # Cargar literal bool: " + valor);
    }
    
    /**
     * Genera código para llamadas a función
     */
    private void generarLlamada(arbol nodo) {
        String nombreFunc = nodo.valor;
        System.out.println("  [SEMÁNTICA] Llamada a función: " + nombreFunc);
        
        emitir("    # Llamada a función: " + nombreFunc);
        emitir("    jal " + nombreFunc);
        emitir("    # Valor retornado en $v0");
    }
    
    /**
     * Genera código para acceso a arregos
     */
    private void generarArrayAccess(arbol nodo) {
        String nombreArr = nodo.valor;
        System.out.println("  [SEMÁNTICA] Acceso a arreglo: " + nombreArr);
        
        emitir("    # Acceso a arreglo: " + nombreArr);
        // Procesar índices
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }
        emitir("    # Índices en $t0 y $t1");
    }
    
    /**
     * Genera código para decide (switch)
     */
    private void generarDecide(arbol nodo) {
        String labelSalida = generarLabel();
        System.out.println("  [GENERACIÓN] DECIDE - label salida: " + labelSalida);
        
        emitir("    # Estructura DECIDE");
        for (arbol hijo : nodo.hijos) {
            if (hijo.tipo.equals("CASOS")) {
                generarCasos(hijo, labelSalida);
            } else if (hijo.tipo.equals("ELSE")) {
                generarCodigo(hijo.hijos.get(0));
            }
        }
        
        emitir("    " + labelSalida + ":");
    }
    
    /**
     * Genera código para casos del decide
     */
    private void generarCasos(arbol nodo, String labelSalida) {
        for (arbol caso : nodo.hijos) {
            if (caso.tipo.equals("CASO")) {
                String labelSiguiente = generarLabel();
                emitir("    # Caso");
                generarCodigo(caso.hijos.get(0));  // condición
                emitir("    beq $t0, $zero, " + labelSiguiente);
                generarCodigo(caso.hijos.get(1));  // bloque
                emitir("    j " + labelSalida);
                emitir("    " + labelSiguiente + ":");
            }
        }
    }
    
    /**
     * Genera código para loop
     */
    private void generarLoop(arbol nodo) {
        String labelInicio = generarLabel();
        String labelSalida = generarLabel();
        
        System.out.println("  [GENERACIÓN] LOOP");
        emitir("    " + labelInicio + ":");
        emitir("    # Cuerpo del loop");
        
        for (arbol hijo : nodo.hijos) {
            if (hijo.tipo.equals("SENTENCIAS")) {
                generarCodigo(hijo);
            } else if (hijo.tipo.equals("EXIT")) {
                emitir("    # Condición de salida");
                generarCodigo(hijo.hijos.get(0));
                emitir("    bne $t0, $zero, " + labelSalida);
            }
        }
        
        emitir("    j " + labelInicio);
        emitir("    " + labelSalida + ":");
    }
    
    /**
     * Genera código para for
     */
    private void generarFor(arbol nodo) {
        String labelInicio = generarLabel();
        String labelSalida = generarLabel();
        
        System.out.println("  [GENERACIÓN] FOR");
        
        // Inicialización
        generarCodigo(nodo.hijos.get(0));
        
        emitir("    " + labelInicio + ":");
        
        // Condición
        generarCodigo(nodo.hijos.get(1));
        emitir("    beq $t0, $zero, " + labelSalida);
        
        // Bloque
        generarCodigo(nodo.hijos.get(3));
        
        // Incremento
        generarCodigo(nodo.hijos.get(2));
        
        emitir("    j " + labelInicio);
        emitir("    " + labelSalida + ":");
    }
    
    /**
     * Genera código para return
     */
    private void generarReturn(arbol nodo) {
        System.out.println("  [GENERACIÓN] RETURN");
        
        if (nodo.hijos.size() > 0) {
            generarCodigo(nodo.hijos.get(0));
            emitir("    move $v0, $t0             # Mover resultado a $v0");
        }
        
        emitir("    # Retorno de función");
        emitir("    lw $ra, 0($sp)");
        emitir("    addi $sp, $sp, 4");
        emitir("    jr $ra");
    }
}
