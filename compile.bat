@echo off
REM ==========================================
REM Script de compilación para Compilador
REM Versión completa con todos los pasos
REM ==========================================

setlocal enabledelayedexpansion

echo.
echo ==========================================
echo  COMPILADOR - SCRIPT DE COMPILACIÓN
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

echo [INFO] Verificación de archivos exitosa
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

java -cp lib/cup.jar java_cup.Main -symbols sym -parser Parser -destdir src/lexer src/parser/parser.cup

if errorlevel 1 (
    echo [ERROR] Fallo en la generación del Parser con CUP
    exit /b 1
)

echo [ÉXITO] Parser y símbolos generados en src/lexer/
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

echo [ÉXITO] sym.java compilado
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

echo [ÉXITO] arbol.java compilado
echo.

REM =========================================
REM PASO 4: Compilar Token.java
REM =========================================
echo.
echo ==========================================
echo PASO 4: Compilar Token.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/Token.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/Token.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar Token.java
    exit /b 1
)

echo [ÉXITO] Token.java compilado
echo.

REM =========================================
REM PASO 5: Compilar SymbolTable.java
REM =========================================
echo.
echo ==========================================
echo PASO 5: Compilar SymbolTable.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/SymbolTable.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/SymbolTable.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar SymbolTable.java
    exit /b 1
)

echo [ÉXITO] SymbolTable.java compilado
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

echo [ÉXITO] ErrorHandler.java compilado
echo.

REM =========================================
REM PASO 7: Compilar CodeGenerator.java
REM =========================================
echo.
echo ==========================================
echo PASO 7: Compilar CodeGenerator.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/CodeGenerator.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/CodeGenerator.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar CodeGenerator.java
    exit /b 1
)

echo [ÉXITO] CodeGenerator.java compilado
echo.

REM =========================================
REM PASO 8: Compilar AnalizadorSemantico.java
REM =========================================
echo.
echo ==========================================
echo PASO 8: Compilar AnalizadorSemantico.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/util/AnalizadorSemantico.java
echo.

javac -cp ".;lib/cup.jar" -d . src/util/AnalizadorSemantico.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar AnalizadorSemantico.java
    exit /b 1
)

echo [ÉXITO] AnalizadorSemantico.java compilado
echo.

REM =========================================
REM PASO 9: Compilar Lexer.java, Parser.java y sym.java
REM =========================================
echo.
echo ==========================================
echo PASO 9: Compilar Lexer.java, Parser.java y sym.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/lexer/Lexer.java src/lexer/Parser.java src/lexer/sym.java
echo.

javac -cp ".;lib/cup.jar" -d . src/lexer/Lexer.java src/lexer/Parser.java src/lexer/sym.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar Lexer.java, Parser.java o sym.java
    exit /b 1
)

echo [ÉXITO] Lexer.java, Parser.java y sym.java compilados
echo.

REM =========================================
REM PASO 10: Compilar Main.java
REM =========================================
echo.
echo ==========================================
echo PASO 10: Compilar Main.java
echo ==========================================
echo.
echo Comando: javac -cp ".;lib/cup.jar" -d . src/main/Main.java
echo.

javac -cp ".;lib/cup.jar" -d . src/main/Main.java

if errorlevel 1 (
    echo [ERROR] Fallo al compilar Main.java
    exit /b 1
)

echo [ÉXITO] Main.java compilado
echo.

REM =========================================
REM PASO 11: Ejecutar el Compilador
REM =========================================
echo.
echo ==========================================
echo PASO 11: Ejecutar el Compilador
echo ==========================================
echo.
echo Comando: java -cp ".;lib/cup.jar;." main.Main
echo.

java -cp ".;lib/cup.jar;." main.Main

if errorlevel 1 (
    echo [ERROR] Fallo al ejecutar el compilador
    exit /b 1
)

REM =========================================
REM COMPILACIÓN Y EJECUCIÓN COMPLETADAS
REM =========================================
echo.
echo ==========================================
echo COMPILACIÓN Y EJECUCIÓN COMPLETADAS
echo ==========================================
echo.
echo El compilador se ha ejecutado exitosamente.
echo.
echo Para ejecutar nuevamente con diferentes parámetros:
echo   java -cp ".;lib/cup.jar;." main.Main
echo.
echo ==========================================
echo.