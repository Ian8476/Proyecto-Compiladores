package util;

import java.util.ArrayList;
import java.util.List;


/**
 * Maneja la recolección y reporte de errores léxicos y sintácticos.
 * Permite agregar errores, obtener resúmenes y generar reportes detallados.
 * @version 1.0
 * @author Duan Antonio Espinoza
 * 
 */

// Clase para manejar errores léxicos y sintácticos
public class ErrorHandler {
    // Lista para almacenar los mensajes de error
    private static List<String> errores = new ArrayList<>();
    // Contadores de errores
    private static int erroresLexicos = 0;
    // Contador de errores sintácticos
    private static int erroresSintacticos = 0;
    // Contador de errores semánticos
    private static int erroresSemanticos = 0;
    
    // Agrega un error léxico a la lista
    public static void agregarErrorLexico(String mensaje, int linea, int columna) {
        // Incrementa el contador de errores léxicos
        erroresLexicos++;
        String error = String.format("ERROR LÉXICO (Línea %d, Columna %d): %s", 
                                    linea, columna, mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    // Agrega un error sintáctico a la lista
    public static void agregarErrorSintactico(String mensaje, int linea, int columna) {
        erroresSintacticos++;
        String error = String.format("ERROR SINTÁCTICO (Línea %d, Columna %d): %s", 
                                    linea, columna, mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    // Agrega un error sintáctico sin información de línea y columna
    public static void agregarErrorSintactico(String mensaje) {
        erroresSintacticos++;
        String error = String.format("ERROR SINTÁCTICO: %s", mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    // Agrega un error semántico a la lista
    public static void agregarErrorSemantico(String mensaje) {
        erroresSemanticos++;
        String error = String.format("ERROR SEMÁNTICO: %s", mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    // Agrega un error semántico con información de línea y columna
    public static void agregarErrorSemantico(String mensaje, int linea, int columna) {
        erroresSemanticos++;
        String error = String.format("ERROR SEMÁNTICO (Línea %d, Columna %d): %s", 
                                    linea, columna, mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    // Obtiene la lista de errores registrados
    // error como tal ya está definido en parser.cup de manera interna
    // por eso se usa getErrores 
    public static List<String> getErrores() {
        return new ArrayList<>(errores);
    }
    
    // Obtiene el total de errores registrados
    public static int getTotalErrores() {
        return erroresLexicos + erroresSintacticos + erroresSemanticos;
    }
    
    // Obtiene el total de errores léxicos
    public static int getErroresLexicos() {
        return erroresLexicos;
    }
    
    // Obtiene el total de errores sintácticos
    public static int getErroresSintacticos() {
        return erroresSintacticos;
    }
    
    // Obtiene el total de errores semánticos
    public static int getErroresSemanticos() {
        return erroresSemanticos;
    }

    // Limpia la lista de errores y resetea los contadores
    public static void limpiar() {
        errores.clear();
        erroresLexicos = 0;
        erroresSintacticos = 0;
        erroresSemanticos = 0;
    }


    // Genera un reporte de errores en un archivo de texto
    // archivoSalida: ruta del archivo donde se guardará el reporte
    // El reporte incluye el total de errores y una lista detallada

    
    public static void generarReporte(String archivoSalida) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(archivoSalida)) {
            writer.println("=== REPORTE DE ERRORES ===");
            writer.println("Total errores léxicos: " + erroresLexicos);
            writer.println("Total errores sintácticos: " + erroresSintacticos);
            writer.println("Total errores semánticos: " + erroresSemanticos);
            writer.println("Total errores: " + getTotalErrores());
            writer.println("\n=== LISTA DETALLADA ===");
            
            for (String error : errores) {
                writer.println(error);
            }
            
            if (errores.isEmpty()) {
                writer.println("No se encontraron errores.");
            }
        } catch (Exception e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }

    // Muestra un resumen de errores en la consola
    
    public static void mostrarResumen() {
        System.out.println("\n=== RESUMEN DE ERRORES ===");
        System.out.println("Errores léxicos: " + erroresLexicos);
        System.out.println("Errores sintácticos: " + erroresSintacticos);
        System.out.println("Errores semánticos: " + erroresSemanticos);
        System.out.println("Total errores: " + getTotalErrores());
        if (getTotalErrores() == 0) {
            System.out.println(" No se encontraron errores");
        }
    }
}
