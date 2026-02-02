package util;

import arbolSintactico.arbol;
import java.util.HashMap;
import java.util.Map;

public class AnalizadorSemantico {
    private static Map<String, String> tablaVariablesGlobales = new HashMap<>();
    private static Map<String, String> tablaVariablesLocales = new HashMap<>();
    private static Map<String, String> tablaFunciones = new HashMap<>();

    public static String obtenerTipoExpresion(arbol nodo) {
        if (nodo == null) return "unknown";
        String tipo = nodo.tipo;
        String valor = nodo.valor;
        // Literales
        if (tipo.equals("LITERAL_INT")) return "int";
        if (tipo.equals("LITERAL_FLOAT")) return "float";
        if (tipo.equals("LITERAL_BOOL")) return "bool";
        if (tipo.equals("LITERAL_CHAR")) return "char";
        if (tipo.equals("LITERAL_STRING")) return "string";
        // Identificadores
        if (tipo.equals("IDENT")) {
            String nombreVar = valor;
            if (tablaVariablesLocales.containsKey(nombreVar)) {
                return tablaVariablesLocales.get(nombreVar);
            }
            if (tablaVariablesGlobales.containsKey(nombreVar)) {
                return tablaVariablesGlobales.get(nombreVar);
            }
            util.ErrorHandler.agregarErrorSemantico("Variable no declarada: " + nombreVar);
            return "unknown";
        }
        // Operaciones
        if (tipo.equals("OPERACION")) {
            return obtenerTipoOperacion(valor, nodo);
        }
        // Acceso a arreglo
        if (tipo.equals("ARRAY_ACCESS")) {
            String nombreVar = valor;
            if (tablaVariablesLocales.containsKey(nombreVar)) {
                return tablaVariablesLocales.get(nombreVar).replace("[]", "");
            }
            if (tablaVariablesGlobales.containsKey(nombreVar)) {
                return tablaVariablesGlobales.get(nombreVar).replace("[]", "");
            }
            return "unknown";
        }
        // Llamada a función
        if (tipo.equals("LLAMADA")) {
            String nombreFunc = valor;
            if (tablaFunciones.containsKey(nombreFunc)) {
                return tablaFunciones.get(nombreFunc);
            }
            util.ErrorHandler.agregarErrorSemantico("Función no declarada: " + nombreFunc);
            return "unknown";
        }
        return "unknown";
    }

    private static String obtenerTipoOperacion(String operador, arbol nodo) {
        if (operador == null || nodo.hijos.isEmpty()) return "unknown";
        // Operadores lógicos: siempre retornan bool
        if (operador.equals("OR") || operador.equals("AND") || 
            operador.equals("==") || operador.equals("!=") ||
            operador.equals("<") || operador.equals("<=") ||
            operador.equals(">") || operador.equals(">=")) {
            return "bool";
        }
        // Operadores unarios
        if (operador.equals("NOT")) return "bool";
        if (operador.equals("-") || operador.equals("++") || operador.equals("--")) {
            return obtenerTipoExpresion(nodo.hijos.get(0));
        }
        // Operadores binarios: retornan el tipo del operando izquierdo
        if (nodo.hijos.size() >= 2) {
            String tipoIzq = obtenerTipoExpresion(nodo.hijos.get(0));
            String tipoDer = obtenerTipoExpresion(nodo.hijos.get(1));
            validarCompatibilidadTipos(tipoIzq, tipoDer, operador);
            return tipoIzq;
        }
        return "unknown";
    }

    public static void validarCompatibilidadTipos(String tipoIzq, String tipoDer, String operador) {
        if (tipoIzq == null || tipoDer == null) return;
        if (tipoIzq.equals("unknown") || tipoDer.equals("unknown")) return;
        // Asignación: '=' debe validar asignabilidad
        if (operador.equals("=")) {
            if (tipoIzq.equals(tipoDer)) return;
            if (esNumerico(tipoIzq) && esNumerico(tipoDer)) {
                if (tipoIzq.equals("float") && tipoDer.equals("int")) return;
                util.ErrorHandler.agregarErrorSemantico("Error de asignación: no se puede asignar " + tipoDer + " a " + tipoIzq + ".");
                return;
            }
            if (tipoIzq.endsWith("[]")) {
                String baseIzq = tipoIzq.replace("[]", "");
                if (tipoDer.endsWith("[]")) {
                    String baseDer = tipoDer.replace("[]", "");
                    if (!baseIzq.equals(baseDer)) {
                        util.ErrorHandler.agregarErrorSemantico("Error de asignación: tipos de arreglo incompatibles: " + tipoIzq + " y " + tipoDer);
                    }
                    return;
                }
                util.ErrorHandler.agregarErrorSemantico("Error de asignación: no se puede asignar valor no-arreglo a arreglo " + tipoIzq);
                return;
            }
            util.ErrorHandler.agregarErrorSemantico("Error de asignación: tipos incompatibles " + tipoIzq + " <- " + tipoDer);
            return;
        }
        // Operaciones aritméticas: int/float
        if (operador.equals("+") || operador.equals("-") || operador.equals("*") || 
            operador.equals("/") || operador.equals("//") || operador.equals("%") ||
            operador.equals("^")) {
            if (!(esNumerico(tipoIzq) && esNumerico(tipoDer))) {
                util.ErrorHandler.agregarErrorSemantico("Error de tipo: no puedes realizar " + operador + 
                    " entre " + tipoIzq + " y " + tipoDer + ". Se requieren tipos numéricos.");
            }
            return;
        }
        // Operadores lógicos: bool
        if (operador.equals("AND") || operador.equals("OR") || operador.equals("NOT") || operador.equals("OR")) {
            if (!tipoIzq.equals("bool") || !tipoDer.equals("bool")) {
                util.ErrorHandler.agregarErrorSemantico("Error de tipo: operadores lógicos requieren bool, " +
                    "pero se recibieron " + tipoIzq + " y " + tipoDer);
            }
            return;
        }
        // Operadores relacionales: cualquier tipo comparable (requerir mismos tipos)
        if (operador.equals("<") || operador.equals("<=") || operador.equals(">") || operador.equals(">=")) {
            if (!tipoIzq.equals(tipoDer)) {
                util.ErrorHandler.agregarErrorSemantico("Error de tipo: operadores relacionales requieren " +
                    "tipos iguales en ambos operandos. Se recibieron " + tipoIzq + " y " + tipoDer);
            }
            return;
        }
        // Igualdad/Desigualdad: emitir advertencia si tipos diferentes
        if (operador.equals("==") || operador.equals("!=")) {
            if (!tipoIzq.equals(tipoDer)) {
                util.ErrorHandler.agregarErrorSemantico("Advertencia: comparación entre tipos diferentes: " + 
                    tipoIzq + " y " + tipoDer);
            }
        }
    }

    private static boolean esNumerico(String tipo) {
        return tipo.equals("int") || tipo.equals("float");
    }

    public static void registrarVariableGlobal(String nombre, String tipo) {
        if (tablaVariablesGlobales.containsKey(nombre)) {
            util.ErrorHandler.agregarErrorSemantico("Variable global ya declarada: " + nombre);
        } else {
            tablaVariablesGlobales.put(nombre, tipo);
        }
    }

    public static void registrarVariableLocal(String nombre, String tipo) {
        if (tablaVariablesLocales.containsKey(nombre)) {
            util.ErrorHandler.agregarErrorSemantico("Variable local ya declarada: " + nombre);
        } else {
            tablaVariablesLocales.put(nombre, tipo);
        }
    }

    public static String obtenerTipoVariable(String nombre) {
        if (tablaVariablesLocales.containsKey(nombre)) return tablaVariablesLocales.get(nombre);
        if (tablaVariablesGlobales.containsKey(nombre)) return tablaVariablesGlobales.get(nombre);
        return null;
    }

    public static void registrarFuncion(String nombre, String tipoRetorno) {
        if (tablaFunciones.containsKey(nombre)) {
            util.ErrorHandler.agregarErrorSemantico("Función ya declarada: " + nombre);
        } else {
            tablaFunciones.put(nombre, tipoRetorno);
        }
    }

    public static boolean estaVariableDeclara(String nombre) {
        return tablaVariablesGlobales.containsKey(nombre) || tablaVariablesLocales.containsKey(nombre);
    }

    public static boolean estaFuncionDeclarada(String nombre) {
        return tablaFunciones.containsKey(nombre);
    }

    public static void limpiar() {
        tablaVariablesGlobales.clear();
        tablaVariablesLocales.clear();
        tablaFunciones.clear();
    }
}
