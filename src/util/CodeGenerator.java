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
    private int offsetMemoria = 0;              // Offset para variables en la pila (bytes desde $fp hacia abajo)
    private Map<String, Integer> offsetVariables; // Mapeo variable -> offset en stack
    private int profundidadBloque = 0;          // Para manejo de variables locales

    // Manejo de scopes para variables locales
    private final Stack<Integer> bytesAllocadosEnBloque = new Stack<>();
    private final Stack<List<String>> varsPorBloque = new Stack<>();

    // Contexto para RETURN (main vs función)
    private boolean enMain = false;
    private boolean enFuncion = false;
    private static final String LABEL_SALIDA_PROGRAMA = "L_program_end";

    // --- Configuración de temporales ---
    private static final int TEMP_REG_MIN = 0;
    private static final int TEMP_REG_MAX = 9;
    
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
        registrosDisponibles.clear();
        for (int i = 9; i >= 0; i--) {
            registrosDisponibles.push(i);
        }
    }

    private String treg(int r) {
        return "$t" + r;
    }

    private int allocT() {
        if (registrosDisponibles.isEmpty()) {
            System.err.println("[CodeGenerator] ERROR: Sin registros temporales disponibles ($t0-$t9). Expresión demasiado compleja.");
            return 0;
        }
        int r = registrosDisponibles.pop();
        if (r < TEMP_REG_MIN || r > TEMP_REG_MAX) return 0;
        return r;
    }

    private void freeT(int r) {
        if (r < TEMP_REG_MIN || r > TEMP_REG_MAX) return;
        // Evitar duplicados en la pila
        if (!registrosDisponibles.contains(r)) {
            registrosDisponibles.push(r);
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
        emitir("        move $fp, $sp              # Establecer frame pointer (base del frame)");
        emitir("");
    }
    
    /**
     * Genera el epílogo del programa MIPS
     */
    private void generarEpilogo() {
        emitir("");
        emitir(LABEL_SALIDA_PROGRAMA + ":");
        emitir("        # Epílogo: restaurar registros y retornar");
        emitir("        move $sp, $fp              # Restaurar stack pointer al frame");
        emitir("        lw $ra, 4($sp)             # Restaurar dirección de retorno");
        emitir("        lw $fp, 0($sp)             # Restaurar frame pointer");
        emitir("        addi $sp, $sp, 8           # Liberar espacio en la pila");
        emitir("        li $v0, 10                 # Salir (QtSPIM)");
        emitir("        syscall");
        emitir("");
        emitir("    # Fin del programa");
    }

    /**
     * Genera una expresión y devuelve el registro $tN donde queda el resultado.
     * Nota: Este método NO debe llamar generarCodigo() para evitar interferencias.
     */
    private int genExpr(arbol nodo) {
        if (nodo == null) {
            int r = allocT();
            emitir("    li " + treg(r) + ", 0");
            return r;
        }

        switch (nodo.tipo) {
            case "LITERAL_INT": {
                int r = allocT();
                emitir("    li " + treg(r) + ", " + nodo.valor + "          # literal int");
                return r;
            }
            case "LITERAL_BOOL": {
                int r = allocT();
                int v = (nodo.valor != null && nodo.valor.equalsIgnoreCase("true")) ? 1 : 0;
                emitir("    li " + treg(r) + ", " + v + "          # literal bool");
                return r;
            }
            case "IDENT": {
                int r = allocT();
                String variable = nodo.valor;
                if (offsetVariables.containsKey(variable)) {
                    emitir("    lw " + treg(r) + ", " + (-offsetVariables.get(variable)) + "($fp)   # local " + variable);
                } else {
                    emitir("    lw " + treg(r) + ", " + variable + "    # global " + variable);
                }
                return r;
            }
            case "LLAMADA": {
                int r = allocT();
                String nombreFunc = nodo.valor;
                emitir("    # Llamada a función: " + nombreFunc);
                emitir("    jal " + nombreFunc);
                emitir("    move " + treg(r) + ", $v0");
                return r;
            }
            case "OPERACION": {
                String op = nodo.valor;

                if (nodo.hijos.size() == 2) {
                    int rl = genExpr(nodo.hijos.get(0));
                    int rr = genExpr(nodo.hijos.get(1));

                    switch (op) {
                        case "+":
                        case "++": // requerido: solo binario
                            emitir("    add " + treg(rl) + ", " + treg(rl) + ", " + treg(rr) + "");
                            break;
                        case "-":
                        case "--": // por consistencia si llegase a existir binario
                            emitir("    sub " + treg(rl) + ", " + treg(rl) + ", " + treg(rr) + "");
                            break;
                        case "*":
                            emitir("    mult " + treg(rl) + ", " + treg(rr));
                            emitir("    mflo " + treg(rl));
                            break;
                        case "/":
                        case "//":
                                        // Proteger contra división por cero (evita excepción en QtSPIM)
                                        {
                                            String lOk = generarLabelFor("L_div_ok_");
                                            String lEnd = generarLabelFor("L_div_end_");
                                            emitir("    bne " + treg(rr) + ", $zero, " + lOk);
                                            emitir("    li " + treg(rl) + ", 0");
                                            emitir("    j " + lEnd);
                                            emitir(lOk + ":");
                                            emitir("    div " + treg(rl) + ", " + treg(rr));
                                            emitir("    mflo " + treg(rl));
                                            emitir(lEnd + ":");
                                        }
                            break;
                        case "%":
                                        {
                                            String lOk = generarLabelFor("L_mod_ok_");
                                            String lEnd = generarLabelFor("L_mod_end_");
                                            emitir("    bne " + treg(rr) + ", $zero, " + lOk);
                                            emitir("    li " + treg(rl) + ", 0");
                                            emitir("    j " + lEnd);
                                            emitir(lOk + ":");
                                            emitir("    div " + treg(rl) + ", " + treg(rr));
                                            emitir("    mfhi " + treg(rl));
                                            emitir(lEnd + ":");
                                        }
                            break;
                        case "^": {
                            // Potencia entera simple: rl ^ rr
                            String lLoop = generarLabelFor("L_pow_loop_");
                            String lEnd = generarLabelFor("L_pow_end_");
                            int rRes = allocT();
                            emitir("    li " + treg(rRes) + ", 1");
                            emitir(lLoop + ":");
                            emitir("    blez " + treg(rr) + ", " + lEnd);
                            emitir("    mult " + treg(rRes) + ", " + treg(rl));
                            emitir("    mflo " + treg(rRes));
                            emitir("    addi " + treg(rr) + ", " + treg(rr) + ", -1");
                            emitir("    j " + lLoop);
                            emitir(lEnd + ":");
                            emitir("    move " + treg(rl) + ", " + treg(rRes));
                            freeT(rRes);
                            break;
                        }
                        case "==":
                            emitir("    xor " + treg(rl) + ", " + treg(rl) + ", " + treg(rr));
                            emitir("    sltiu " + treg(rl) + ", " + treg(rl) + ", 1");
                            break;
                        case "!=":
                            emitir("    xor " + treg(rl) + ", " + treg(rl) + ", " + treg(rr));
                            emitir("    sltu " + treg(rl) + ", $zero, " + treg(rl));
                            break;
                        case "<":
                            emitir("    slt " + treg(rl) + ", " + treg(rl) + ", " + treg(rr));
                            break;
                        case ">":
                            emitir("    slt " + treg(rl) + ", " + treg(rr) + ", " + treg(rl));
                            break;
                        case "<=":
                            emitir("    slt " + treg(rl) + ", " + treg(rr) + ", " + treg(rl));
                            emitir("    xori " + treg(rl) + ", " + treg(rl) + ", 1");
                            break;
                        case ">=":
                            emitir("    slt " + treg(rl) + ", " + treg(rl) + ", " + treg(rr));
                            emitir("    xori " + treg(rl) + ", " + treg(rl) + ", 1");
                            break;
                        case "AND":
                            emitir("    sltu " + treg(rl) + ", $zero, " + treg(rl));
                            emitir("    sltu " + treg(rr) + ", $zero, " + treg(rr));
                            emitir("    and " + treg(rl) + ", " + treg(rl) + ", " + treg(rr));
                            break;
                        case "OR":
                            emitir("    sltu " + treg(rl) + ", $zero, " + treg(rl));
                            emitir("    sltu " + treg(rr) + ", $zero, " + treg(rr));
                            emitir("    or " + treg(rl) + ", " + treg(rl) + ", " + treg(rr));
                            break;
                        default:
                            System.err.println("[CodeGenerator] Operador binario no soportado en MIPS: " + op);
                            // devolver el operando izquierdo como fallback
                            break;
                    }

                    freeT(rr);
                    return rl;
                }

                if (nodo.hijos.size() == 1) {
                    // Operación unaria
                    arbol operando = nodo.hijos.get(0);

                    // Nota: el parser actual produce i++ / ++i como OPERACION("++") con 1 hijo.
                    // Aunque el requerimiento es "solo binario", aquí lo tratamos como AZÚCAR
                    // para i = i + 1 / i = i - 1, porque si no, el FOR se encicla.
                    if ("++".equals(op) || "--".equals(op)) {
                        if (operando == null || !"IDENT".equals(operando.tipo)) {
                            System.err.println("[CodeGenerator] ERROR: '" + op + "' requiere IDENT como operando.");
                            return genExpr(operando);
                        }

                        String var = operando.valor;
                        int r = allocT();
                        if (offsetVariables.containsKey(var)) {
                            emitir("    lw " + treg(r) + ", " + (-offsetVariables.get(var)) + "($fp)   # local " + var);
                        } else {
                            emitir("    lw " + treg(r) + ", " + var + "    # global " + var);
                        }

                        int delta = "++".equals(op) ? 1 : -1;
                        emitir("    addi " + treg(r) + ", " + treg(r) + ", " + delta);

                        if (offsetVariables.containsKey(var)) {
                            emitir("    sw " + treg(r) + ", " + (-offsetVariables.get(var)) + "($fp)   # guardar local " + var);
                        } else {
                            emitir("    sw " + treg(r) + ", " + var + "    # guardar global " + var);
                        }
                        return r;
                    }

                    int r = genExpr(operando);
                    switch (op) {
                        case "-":
                            emitir("    sub " + treg(r) + ", $zero, " + treg(r));
                            break;
                        case "NOT":
                            // Normalizar a 0/1 y negar
                            emitir("    sltu " + treg(r) + ", $zero, " + treg(r));
                            emitir("    xori " + treg(r) + ", " + treg(r) + ", 1");
                            break;
                        default:
                            System.err.println("[CodeGenerator] Operador unario no soportado en MIPS: " + op);
                            break;
                    }
                    return r;
                }

                int r = allocT();
                emitir("    li " + treg(r) + ", 0");
                return r;
            }
            default: {
                // Fallback: evaluar hijos y devolver último
                if (nodo.hijos != null && !nodo.hijos.isEmpty()) {
                    int last = 0;
                    for (arbol h : nodo.hijos) {
                        last = genExpr(h);
                    }
                    return last;
                }
                int r = allocT();
                emitir("    li " + treg(r) + ", 0");
                return r;
            }
        }
    }

    /** Evalúa una expresión y deja el resultado en $t0 (interfaz usada por sentencias). */
    private void genExprToT0(arbol nodo) {
        inicializarRegistros();
        int r = genExpr(nodo);
        if (r != 0) {
            emitir("    move $t0, " + treg(r));
        }
        // Reset bookkeeping para evitar fugas de registros entre expresiones
        inicializarRegistros();
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
                genExprToT0(nodo);
                break;
            case "LITERAL_INT":
                genExprToT0(nodo);
                break;
            case "LITERAL_FLOAT":
                generarLiteralFloat(nodo);
                break;
            case "LITERAL_BOOL":
                genExprToT0(nodo);
                break;
            case "IDENT":
                genExprToT0(nodo);
                break;
            case "LLAMADA":
                genExprToT0(nodo);
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
        // Reservar espacio en .data para la variable global
        datosGlobales.append("    " + nomVariable + ": .word 0\n");
        
        emitir("    # Declaración global: " + nomVariable);
        
        if (nodo.hijos.size() > 0) {
            arbol tipo = nodo.hijos.get(0);
            System.out.println("    - Tipo: " + tipo.valor);
            
            // Si hay inicialización (segundo hijo)
            if (nodo.hijos.size() > 1) {
                arbol inicializacion = nodo.hijos.get(1);
                System.out.println("    - Con inicialización");
                genExprToT0(inicializacion);
                emitir("    sw $t0, " + nomVariable);
                emitir("    # Fin inicialización de " + nomVariable);
            }
        }
    }
    
    /**
     * Genera código para la función main
     */
    private void generarMain(arbol nodo) {
        System.out.println("  [SEMÁNTICA] Analizando MAIN");

        boolean prevMain = enMain;
        boolean prevFun = enFuncion;
        enMain = true;
        enFuncion = false;
        
        // Procesar los hijos (normalmente un BLOQUE)
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }

        enMain = prevMain;
        enFuncion = prevFun;
    }
    
    /**
     * Genera código para operaciones binarias y unarias
     */
    private void generarOperacion(arbol nodo) {
        // La evaluación de expresiones se hace vía genExpr/genExprToT0.
        genExprToT0(nodo);
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
        
        if (offsetVariables.containsKey(variable)) {
            emitir("    lw $t0, " + (-offsetVariables.get(variable)) + "($fp)   # Cargar local " + variable);
        } else {
            emitir("    lw $t0, " + variable + "    # Cargar global " + variable);

        }

    }
    
    /**
     * Emite una línea de código MIPS
     */
    private void emitir(String linea) {
        codigo.append(linea).append("\n");
    }

    private boolean esExpresionComoSentencia(arbol nodo) {
        if (nodo == null) return false;
        switch (nodo.tipo) {
            case "OPERACION":
            case "IDENT":
            case "LITERAL_INT":
            case "LITERAL_BOOL":
            case "LLAMADA":
            case "ARRAY_ACCESS":
                return true;
            default:
                return false;
        }
    }

    private void imprimirEnteroEnT0ConSalto() {
        emitir("    move $a0, $t0");
        emitir("    li $v0, 1");
        emitir("    syscall");
        emitir("    la $a0, newline");
        emitir("    li $v0, 4");
        emitir("    syscall");
    }
    
    /**
     * Genera un nuevo label único
     */
    private String generarLabel() {
        return "L" + (labelContador++);
    }

    /**
     * Version 2 de generarLabel
     * ********************
     * @return
     */

    private String generarLabelFor(String prefix) {
        return prefix + (labelContador++);
    }



    
    /**
     * Obtiene el registro temporal siguiente
     */
    private int obtenerRegistro() {
        return allocT();
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

        boolean prevMain = enMain;
        boolean prevFun = enFuncion;
        enMain = false;
        enFuncion = true;
        
        emitir("");
        emitir("    # Función: " + nombreFunc);
        emitir("    " + nombreFunc + ":");
        emitir("        addi $sp, $sp, -8");
        emitir("        sw $ra, 4($sp)");
        emitir("        sw $fp, 0($sp)");
        emitir("        move $fp, $sp");
        
        // Procesar bloque de función
        for (arbol hijo : nodo.hijos) {
            if (hijo.tipo.equals("BLOQUE")) {
                generarCodigo(hijo);
            }
        }

        emitir("        # Retorno de función (implícito)");
        emitir("        move $sp, $fp");
        emitir("        lw $ra, 4($sp)");
        emitir("        lw $fp, 0($sp)");
        emitir("        addi $sp, $sp, 8");
        emitir("        jr $ra");

        enMain = prevMain;
        enFuncion = prevFun;
    }
    
    /**
     * código para bloques de código
     */
    private void generarBloque(arbol nodo) {
        System.out.println("  [GENERACIÓN] Abriendo bloque");
        profundidadBloque++;

        // Iniciar scope de variables locales para este bloque
        bytesAllocadosEnBloque.push(0);
        varsPorBloque.push(new ArrayList<>());
        
        for (arbol hijo : nodo.hijos) {
            generarCodigo(hijo);
        }

        // Salir del scope: liberar offsets y espacio de stack
        List<String> vars = varsPorBloque.pop();
        for (String v : vars) {
            offsetVariables.remove(v);
        }

        int bytesBloque = bytesAllocadosEnBloque.pop();
        if (bytesBloque > 0) {
            emitir("    # Liberar espacio de locales");
            emitir("    addi $sp, $sp, " + bytesBloque);
            offsetMemoria -= bytesBloque;
        }
        
        profundidadBloque--;
    }
    
    /**
     * código para lista de sentencias
     */
    private void generarSentencias(arbol nodo) {
        for (arbol hijo : nodo.hijos) {
            if (esExpresionComoSentencia(hijo)) {
                // Regla: stmt ::= expr ENDL. Aquí le damos semántica de "print expr".
                genExprToT0(hijo);
                imprimirEnteroEnT0ConSalto();
                continue;
            }
            generarCodigo(hijo);
        }
    }
    
    /**
     *  código para declaraciones locales
     */
    private void generarDeclLocal(arbol nodo) {
        String nomVariable = nodo.valor;
        System.out.println("  [SEMÁNTICA] Declaración local: " + nomVariable);
        
        // Reservar 4 bytes y asignar offset (desde $fp hacia abajo)
        emitir("    addi $sp, $sp, -4");
        offsetMemoria += 4;
        offsetVariables.put(nomVariable, offsetMemoria);

        if (!bytesAllocadosEnBloque.isEmpty()) {
            bytesAllocadosEnBloque.push(bytesAllocadosEnBloque.pop() + 4);
        }
        if (!varsPorBloque.isEmpty()) {
            varsPorBloque.peek().add(nomVariable);
        }
        
        emitir("    # Declaración local: " + nomVariable);
        
        if (nodo.hijos.size() > 1) {
            // Tiene inicialización
            arbol inicializacion = nodo.hijos.get(1);
            System.out.println("    - Con inicialización");
            genExprToT0(inicializacion);
            emitir("    sw $t0, " + (-offsetVariables.get(nomVariable)) + "($fp)");
        }
    }
    
    /**
     *  código para asignaciones mejorado
     */
    private void generarAsignacion(arbol nodo) {
        String variable = nodo.valor;
        System.out.println("  [SEMÁNTICA] Asignación a: " + variable);
        
        if (nodo.hijos.size() > 0) {
            arbol expresion = nodo.hijos.get(0);
            System.out.println("    - Expresión: " + expresion.tipo);
            
            emitir("    # Asignación: " + variable + " = ...");
            genExprToT0(expresion);
            
            if (offsetVariables.containsKey(variable)) {
                // Variable local
                emitir("    sw $t0, " + (-offsetVariables.get(variable)) + "($fp)   # Guardar " + variable);
            } else {
                // Variable global
                emitir("    sw $t0, " + variable + "    # Guardar global " + variable);

            }
        }
    }
    
    /**
     *  código para asignaciones de arreglos
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
     *  código para literales flotantes
     */
    private void generarLiteralFloat(arbol nodo) {
        String valor = nodo.valor;
        System.out.println("  [SEMÁNTICA] Literal FLOAT: " + valor);
        emitir("    li.s $f0, " + valor + "        # Cargar literal float: " + valor);
    }
    
    /**
     *  código para literales booleanos
     */
    private void generarLiteralBool(arbol nodo) {
        String valor = nodo.valor;
        System.out.println("  [SEMÁNTICA] Literal BOOL: " + valor);
        int valorInt = valor.equalsIgnoreCase("true") ? 1 : 0;
        emitir("    li $t0, " + valorInt + "          # Cargar literal bool: " + valor);
    }
    
    /**
     *  código para llamadas a función
     */
    private void generarLlamada(arbol nodo) {
        String nombreFunc = nodo.valor;
        System.out.println("  [SEMÁNTICA] Llamada a función: " + nombreFunc);
        
        emitir("    # Llamada a función: " + nombreFunc);
        emitir("    jal " + nombreFunc);
        emitir("    # Valor retornado en $v0");
    }
    
    /**
     *  código para acceso a arreglos
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
     *  código para decide (switch)
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
     *  código para casos del decide
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
     *  código para loop
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
                genExprToT0(hijo.hijos.get(0));
                emitir("    bne $t0, $zero, " + labelSalida);
            }
        }
        
        emitir("    j " + labelInicio);
        emitir("    " + labelSalida + ":");
    }
    
    /**
     *  código para for
     */
    private void generarFor(arbol nodo) {
        String labelInicio = generarLabel();
        String labelSalida = generarLabel();
        
        System.out.println("  [GENERACIÓN] FOR");
        
        // Inicialización
        generarCodigo(nodo.hijos.get(0));
        
        emitir("    " + labelInicio + ":");
        
        // Condición
        genExprToT0(nodo.hijos.get(1));
        emitir("    beq $t0, $zero, " + labelSalida);
        
        // Bloque
        generarCodigo(nodo.hijos.get(3));
        
        // Incremento
        genExprToT0(nodo.hijos.get(2));
        
        emitir("    j " + labelInicio);
        emitir("    " + labelSalida + ":");
    }
    
    /**
     *  código para return
     */
    private void generarReturn(arbol nodo) {
        System.out.println("  [GENERACIÓN] RETURN");
        
        if (nodo.hijos.size() > 0) {
            genExprToT0(nodo.hijos.get(0));
            emitir("    move $v0, $t0             # Mover resultado a $v0");
        }

        if (enMain) {
            emitir("    j " + LABEL_SALIDA_PROGRAMA + "        # return en main -> salir del programa");
            return;
        }

        if (enFuncion) {
            emitir("    # Retorno de función");
            emitir("    move $sp, $fp");
            emitir("    lw $ra, 4($sp)");
            emitir("    lw $fp, 0($sp)");
            emitir("    addi $sp, $sp, 8");
            emitir("    jr $ra");
            return;
        }

        // Fallback
        emitir("    j " + LABEL_SALIDA_PROGRAMA);
    }
}