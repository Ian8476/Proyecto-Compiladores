package util;

/**
 * Clase de demostración del uso de la tabla de símbolos.
 * Muestra cómo registrar, buscar y gestionar símbolos en diferentes alcances.
 * @version 1.0
 * @author Duan Antonio Espinoza
 */
public class SymbolTableDemo {
    
    public static void demoBasico() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  DEMOSTRACIÓN: Gestión de Tablas de Símbolos              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        SymbolTable tablaSimbolos = new SymbolTable();
        
        // =====================================================
        // 1. Agregar variables globales
        // =====================================================
        System.out.println("1️⃣  AGREGANDO VARIABLES GLOBALES...\n");
        
        Token varX = new Token("IDENT", "x", 5, 10);
        varX.setTipoVariable("int");
        varX.setInicializado(true);
        varX.setObservaciones("Variable global de tipo entero");
        tablaSimbolos.agregar("x", varX);
        System.out.println(" Agregado: x (int)");
        
        Token varY = new Token("IDENT", "y", 6, 10);
        varY.setTipoVariable("float");
        varY.setInicializado(false);
        varY.setObservaciones("Variable global sin inicializar");
        tablaSimbolos.agregar("y", varY);
        System.out.println("Agregado: y (float)");
        
        Token matriz = new Token("IDENT", "datos", 7, 10);
        matriz.setTipoVariable("int");
        matriz.setEsArreglo(true);
        matriz.setDimensiones("10x20");
        matriz.setInicializado(false);
        matriz.setObservaciones("Arreglo bidimensional para almacenar datos");
        tablaSimbolos.agregar("datos", matriz);
        System.out.println(" Agregado: datos (int[10x20])");
        
        // =====================================================
        // 2. Crear un nuevo alcance para una función
        // =====================================================
        System.out.println("\n  CREANDO ALCANCE PARA FUNCIÓN 'calcular'...\n");
        
        tablaSimbolos.crearAlcance("FUNC_calcular", "FUNCION");
        
        // Parámetros de la función
        Token param1 = new Token("IDENT", "a", 10, 20);
        param1.setTipoVariable("int");
        param1.setAlcance("PARAMETRO");
        param1.setInicializado(true);
        param1.setObservaciones("Primer parámetro de función");
        tablaSimbolos.agregar("a", param1);
        System.out.println("✓ Parámetro: a (int)");
        
        Token param2 = new Token("IDENT", "b", 10, 25);
        param2.setTipoVariable("int");
        param2.setAlcance("PARAMETRO");
        param2.setInicializado(true);
        param2.setObservaciones("Segundo parámetro de función");
        tablaSimbolos.agregar("b", param2);
        System.out.println(" Parámetro: b (int)");
        
        // Variables locales de la función
        Token resultado = new Token("IDENT", "resultado", 11, 10);
        resultado.setTipoVariable("int");
        resultado.setInicializado(false);
        resultado.setObservaciones("Variable local para almacenar resultado");
        tablaSimbolos.agregar("resultado", resultado);
        System.out.println(" Variable local: resultado (int)");
        
        // =====================================================
        // 3. Buscar variables
        // =====================================================
        System.out.println("\n  BUSCANDO VARIABLES...\n");
        
        Token encontrado = tablaSimbolos.buscar("x");
        if (encontrado != null) {
            System.out.println(" Variable 'x' encontrada en alcance: " + encontrado.getAlcance());
            System.out.println("  Tipo: " + encontrado.getTipoVariable());
        }
        
        encontrado = tablaSimbolos.buscar("resultado");
        if (encontrado != null) {
            System.out.println(" Variable 'resultado' encontrada en alcance: " + encontrado.getAlcance());
            System.out.println("  Tipo: " + encontrado.getTipoVariable());
        }
        
        // =====================================================
        // 4. Salir del alcance de la función
        // =====================================================
        System.out.println("\n  SALIENDO DEL ALCANCE DE FUNCIÓN...\n");
        
        tablaSimbolos.salirAlcance();
        System.out.println("✓ Regresando a alcance: " + tablaSimbolos.obtenerAlcanceActual());
        
        // =====================================================
        // 5. Crear otro alcance
        // =====================================================
        System.out.println("\n  CREANDO ALCANCE PARA FUNCIÓN 'procesar'...\n");
        
        tablaSimbolos.crearAlcance("FUNC_procesar", "FUNCION");
        
        Token varLocal = new Token("IDENT", "temporal", 15, 10);
        varLocal.setTipoVariable("string");
        varLocal.setInicializado(false);
        varLocal.setObservaciones("Variable temporal en función procesar");
        tablaSimbolos.agregar("temporal", varLocal);
        System.out.println(" Variable local: temporal (string)");
        
        tablaSimbolos.salirAlcance();
        System.out.println(" Regresando a alcance: " + tablaSimbolos.obtenerAlcanceActual());
        
        // =====================================================
        // 6. Generar reportes
        // =====================================================
        System.out.println("\n  GENERANDO REPORTES...\n");
        
        System.out.println(tablaSimbolos.generarReporte());
    }
    
    public static void main(String[] args) {
        demoBasico();
    }
}

/**
 * Esta funcionalidad es simplemente una demostración y no forma parte del núcleo del compilador.
 * Su propósito es ilustrar cómo se puede utilizar la tabla de símbolos para gestionar variables
 * y alcances en un entorno de compilación.
 * En general es un ambito para pruebas y ejemplos y ver si la implementación de la tabla de símbolos es correcta.
 */