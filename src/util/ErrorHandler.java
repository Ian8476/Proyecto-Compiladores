package util;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private static List<String> errores = new ArrayList<>();
    private static int erroresLexicos = 0;
    private static int erroresSintacticos = 0;
    
    public static void agregarErrorLexico(String mensaje, int linea, int columna) {
        erroresLexicos++;
        String error = String.format("ERROR LÉXICO (Línea %d, Columna %d): %s", 
                                    linea, columna, mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    public static void agregarErrorSintactico(String mensaje, int linea, int columna) {
        erroresSintacticos++;
        String error = String.format("ERROR SINTÁCTICO (Línea %d, Columna %d): %s", 
                                    linea, columna, mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    public static void agregarErrorSintactico(String mensaje) {
        erroresSintacticos++;
        String error = String.format("ERROR SINTÁCTICO: %s", mensaje);
        errores.add(error);
        System.err.println(error);
    }
    
    public static List<String> getErrores() {
        return new ArrayList<>(errores);
    }
    
    public static int getTotalErrores() {
        return erroresLexicos + erroresSintacticos;
    }
    
    public static int getErroresLexicos() {
        return erroresLexicos;
    }
    
    public static int getErroresSintacticos() {
        return erroresSintacticos;
    }
    
    public static void limpiar() {
        errores.clear();
        erroresLexicos = 0;
        erroresSintacticos = 0;
    }
    
    public static void generarReporte(String archivoSalida) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(archivoSalida)) {
            writer.println("=== REPORTE DE ERRORES ===");
            writer.println("Total errores léxicos: " + erroresLexicos);
            writer.println("Total errores sintácticos: " + erroresSintacticos);
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
    
    public static void mostrarResumen() {
        System.out.println("\n=== RESUMEN DE ERRORES ===");
        System.out.println("Errores léxicos: " + erroresLexicos);
        System.out.println("Errores sintácticos: " + erroresSintacticos);
        System.out.println("Total errores: " + getTotalErrores());
        if (getTotalErrores() == 0) {
            System.out.println("✓ No se encontraron errores");
        }
    }
}
