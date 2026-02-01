## Compilador - Guía de Compilación y Ejecución

### Descripción
Compilador multifase con análisis léxico, sintáctico, semántico y generación de código MIPS.

### Mejoras Implementadas

#### 1. **CodeGenerator.java (v0.2)**
- Soporte extendido para todas las construcciones del lenguaje:
  - Variables globales y locales con offsets en stack
  - Funciones con manejo de parámetros y retornos
  - Operaciones aritméticas (suma, resta, multiplicación, división, potencia, módulo)
  - Operaciones lógicas (AND, OR, NOT)
  - Operaciones relacionales (==, !=, <, <=, >, >=)
  - Estructuras de control (decide/switch, loop, for)
  - Arreglos bidimensionales
- Frame pointer ($fp) para manejo de stack frames
- Sección .data separada para definiciones globales
- Mapeo de variables a offsets en stack
- Labels únicos para control de flujo

#### 2. **Main.java (actualizado)**
- Soporte de archivo de salida MIPS personalizado:
  ```
  java -cp ".;lib/cup.jar;." main.Main [archivo_entrada] [archivo_salida.asm]
  ```
- Si no se especifica archivo de salida, se deriva automáticamente del archivo de entrada
- Ejemplo: `input/prueba.txt` → `input/prueba.asm`

#### 3. **compile.bat (nuevo)**
- Script de compilación automatizado con 9 pasos
- Genera automáticamente Parser y símbolos con CUP
- Compila todas las dependencias en orden correcto
- Validación de archivos previos a compilación
- Instrucciones claras para ejecución

---

## Cómo Usar

### Opción 1: Compilación Automática (Recomendado)

Desde el directorio raíz del proyecto:

```batch
compile.bat
```

El script automaticamente:
1. Genera Parser y símbolos con CUP
2. Compila toda la estructura del proyecto
3. Muestra instrucciones finales

### Opción 2: Compilación Manual

Si prefieres compilar manualmente, ejecuta estos comandos en orden:

```batch
REM Generar Parser con CUP
java -cp lib/cup.jar java_cup.Main -symbols sym -parser Parser -destdir src/lexer src/parser/parser.cup

REM Compilar todas las clases
javac -cp ".;lib/cup.jar" -d . ^
  src/lexer/sym.java ^
  src/arbolSintactico/arbol.java ^
  src/util/Token.java ^
  src/util/SymbolTable.java ^
  src/util/ErrorHandler.java ^
  src/util/CodeGenerator.java ^
  src/lexer/Lexer.java ^
  src/lexer/Parser.java ^
  src/main/Main.java
```

---

## Ejecución del Compilador

### Opción 1: Con archivo de entrada por defecto
```batch
java -cp ".;lib/cup.jar;." main.Main
```
Procesa: `input/prueba.txt`
Salida MIPS: `input/prueba.asm`

### Opción 2: Especificar archivo de entrada
```batch
java -cp ".;lib/cup.jar;." main.Main input/miarchivo.txt
```
Procesa: `input/miarchivo.txt`
Salida MIPS: `input/miarchivo.asm`

### Opción 3: Especificar entrada y salida
```batch
java -cp ".;lib/cup.jar;." main.Main input/miarchivo.txt output/miarchivo.asm
```
Procesa: `input/miarchivo.txt`
Salida MIPS: `output/miarchivo.asm`

---

## Estructura de Salida

Después de ejecutar el compilador, en el directorio `output/` se generan:

- **tokens.txt**: Lista de tokens identificados en análisis léxico
- **arbol.json**: Árbol sintáctico en formato JSON
- **arbol_interactivo.html**: Visualización interactiva del árbol
- **symbol_table.json**: Tabla de símbolos en formato JSON
- **symbol_table.txt**: Tabla de símbolos en formato texto
- **reporte_errores.txt**: Reporte de errores sintácticos y semánticos
- **ARCHIVO_ENTRADA.asm**: Código MIPS generado (ubicado en mismo directorio que entrada)

---

## Estructura del Código MIPS Generado

El código MIPS generado contiene:

### Sección .data
```mips
.data
    # Variables globales
    newline: .asciiz "\n"
    space: .asciiz " "
    prompt_write: .asciiz "Valor: "
```

### Sección .text
```mips
.text
    .globl main
    main:
        # Prólogo
        addi $sp, $sp, -8
        sw $ra, 4($sp)          # Dirección de retorno
        sw $fp, 0($sp)          # Frame pointer
        addi $fp, $sp, 8
        
        # Código del programa
        ...
        
        # Epílogo
        lw $ra, 4($sp)
        lw $fp, 0($sp)
        addi $sp, $sp, 8
        jr $ra
```

---

## Registros MIPS Utilizados

- `$t0-$t9`: Registros temporales para expresiones
- `$f0`: Registro para valores flotantes
- `$v0`: Valor de retorno de función
- `$sp`: Stack pointer
- `$fp`: Frame pointer
- `$ra`: Dirección de retorno
- `$gp`: Global pointer

---

## Ejemplo Completo

```bash
cd Proyecto-Compiladores
compile.bat

REM Luego ejecutar:
java -cp ".;lib/cup.jar;." main.Main input/prueba.txt output/prueba.asm

REM O simplemente:
java -cp ".;lib/cup.jar;." main.Main
```

---

## Características Semánticas Soportadas

- ✅ Declaración de variables globales y locales
- ✅ Funciones con parámetros y valores de retorno
- ✅ Arreglos bidimensionales
- ✅ Todas las operaciones aritméticas
- ✅ Operaciones lógicas y relacionales
- ✅ Estructuras de control (decide, loop, for)
- ✅ Validación de tipos
- ✅ Detección de variables no declaradas
- ✅ Tabla de símbolos con alcance

---

## Notas Importantes

1. El archivo `compile.bat` debe ejecutarse desde el directorio raíz del proyecto
2. Se requiere Java 8 o superior
3. Los archivos de entrada deben ubicarse preferiblemente en el directorio `input/`
4. La extensión del archivo de salida MIPS siempre será `.asm`
5. Los caracteres especiales en nombres de archivos pueden causar problemas

---

**Última actualización**: 1 de febrero de 2026
**Versión**: 0.2
