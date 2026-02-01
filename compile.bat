@echo off
REM ==========================================
REM Script de compilacion para Compilador
REM Proyecto: Verano 2025 - Compiladores
REM Autor: Grupo de desarrollo
REM ==========================================

setlocal enabledelayedexpansion

echo.
echo ==========================================
echo  COMPILADOR - SCRIPT DE COMPILACION
echo ==========================================
echo.

REM Verificar si existen los archivos necesarios
if not exist "lib\cup.jar" (
    echo [ERROR] No se encuentra lib\cup.jar
    exit /b 1
)

if not exist "src\parser\parser.cup" (
    echo [ERROR] No se encuentra src\parser\parser.cup
    exit /b 1
)

if not exist "src\lexer\scanner.flex" (
    echo [ERROR] No se encuentra src\lexer\scanner.flex
    exit /b 1
)

echo [INFO] Verificacion de archivos exitosa
echo.

REM =========================================
REM PASO 1: Generar Parser con CUP
REM =========================================
echo.
echo ==========================================
echo PASO 1: Generar Parser con CUP
echo ==========================================
echo.
echo Comando: java -cp lib/cup.jar java_cup.Main -symbols sym -parser Parser -destdir src/lexer src/parser/parser.cup
echo.

cd /d "%CD%"
java -cp lib/cup.jar java_cup.Main -symbols sym -parser Parser -destdir src/lexer src/parser/parser.cup

if errorlevel 1 (
    echo [ERROR] Fallo en la generacion del Parser con CUP
    exit /b 1
)

echo [EXITO] Parser y simbolos generados en src/lexer/
echo.

REM =========================================
REM PASO 2: Compilar sym.java
REM =========================================
echo.
echo ==========================================
echo PASO 2: Compilar sym.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/lexer/sym.java
echo.

javac -cp ".;lib/cup.jar" -d . src/lexer/sym.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar sym.java
    exit /b 1
)

echo [EXITO] sym.java compilado
echo.

REM =========================================
REM PASO 3: Compilar arbol.java
REM =========================================
echo.
echo ==========================================
echo PASO 3: Compilar arbol.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/arbolSintactico/arbol.java
echo.

javac -cp ".;lib/cup.jar" -d . src/arbolSintactico/arbol.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar arbol.java
    exit /b 1
)

echo [EXITO] arbol.java compilado
echo.

REM =========================================
REM PASO 4: Compilar Token.java
REM =========================================
echo.
echo ==========================================
echo PASO 4: Compilar Token.java (Tabla de Simbolos)
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/Token.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/Token.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar Token.java
    exit /b 1
)

echo [EXITO] Token.java compilado
echo.

REM =========================================
REM PASO 5: Compilar SymbolTable.java
REM =========================================
echo.
echo ==========================================
echo PASO 5: Compilar SymbolTable.java (Tabla de Simbolos)
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/SymbolTable.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/SymbolTable.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar SymbolTable.java
    exit /b 1
)

echo [EXITO] SymbolTable.java compilado
echo.

REM =========================================
REM PASO 6: Compilar ErrorHandler.java
REM =========================================
echo.
echo ==========================================
echo PASO 6: Compilar ErrorHandler.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/ErrorHandler.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/ErrorHandler.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar ErrorHandler.java
    exit /b 1
)

echo [EXITO] ErrorHandler.java compilado
echo.

REM =========================================
REM PASO 7: Compilar CodeGenerator.java
REM =========================================
echo.
echo ==========================================
echo PASO 7: Compilar CodeGenerator.java (Generador de Codigo MIPS)
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/CodeGenerator.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/CodeGenerator.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar CodeGenerator.java
    exit /b 1
)

echo [EXITO] CodeGenerator.java compilado
echo.

REM =========================================
REM PASO 8: Compilar Lexer.java y Parser.java
REM =========================================
echo.
echo ==========================================
echo PASO 8: Compilar Lexer.java, Parser.java y sym.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/lexer/Lexer.java src/lexer/Parser.java
echo.

javac -cp ".;lib/cup.jar" -d . src/lexer/Lexer.java src/lexer/Parser.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar Lexer.java o Parser.java
    exit /b 1
)

echo [EXITO] Lexer.java y Parser.java compilados
echo.

REM =========================================
REM PASO 9: Compilar Main.java
REM =========================================
echo.
echo ==========================================
echo PASO 9: Compilar Main.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/main/Main.java
echo.

javac -cp ".;lib/cup.jar" -d . src/main/Main.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar Main.java
    exit /b 1
)

echo [EXITO] Main.java compilado
echo.

REM =========================================
REM COMPILACION COMPLETADA
REM =========================================
echo.
echo ==========================================
echo COMPILACION COMPLETADA EXITOSAMENTE
echo ==========================================
echo.
echo Para ejecutar el compilador, use:
echo   java -cp ".;lib/cup.jar;." main.Main [archivo_entrada] [archivo_salida.asm]
echo.
echo Ejemplos:
echo   java -cp ".;lib/cup.jar;." main.Main input/prueba.txt output/prueba.asm
echo   java -cp ".;lib/cup.jar;." main.Main input/prueba.txt
echo.
echo Si no especifica archivo de salida, se derivara del archivo de entrada
echo cambiando su extension a .asm
echo.
echo ==========================================
echo.
