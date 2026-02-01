# Copilot instructions for Proyecto1 (Compiladores)

Purpose: give AI coding agents immediate, actionable context for working on this Java compiler project.

- **Big picture**: this repo is a small compiler pipeline: a JFlex lexer + CUP parser produce an AST (`arbol`), followed by semantic checks and a simple MIPS code generator.
  - Entry point: [src/main/Main.java](src/main/Main.java) — orchestrates phases: lexical, syntactic, semantic, codegen, and writes `output/*` artifacts.
  - Lexer spec: [src/lexer/scanner.flex](src/lexer/scanner.flex) (JFlex). Generated scanner lives at [src/lexer/Lexer.java](src/lexer/Lexer.java).
  - Parser spec: [src/parser/parser.cup](src/parser/parser.cup). CUP generates `Parser.java` and `sym.java` (project keeps generated and source copies). The generated parser is used from package `lexer`.
  - AST model: [src/arbolSintactico/arbol.java](src/arbolSintactico/arbol.java) — central tree type used across parser, semantic checks and codegen.
  - Utilities: [src/util/SymbolTable.java](src/util/SymbolTable.java), [src/util/ErrorHandler.java](src/util/ErrorHandler.java), [src/util/CodeGenerator.java](src/util/CodeGenerator.java).

- **Key conventions / project-specific patterns**
  - The project compiles and runs on Windows; classpath examples below use `;` as separator.
  - Generated sources: `JFlex` produces `Lexer.java`; `CUP` produces `Parser.java` and `sym.java`. Keep generator steps in CI/local build before compiling Java sources.
  - Tokens written during the lexing phase go to `output/tokens.txt`; AST JSON goes to `output/arbol.json`; errors to `output/reporte_errores.txt`.
  - The grammar and lexer use some non-ASCII punctuation tokens (e.g. `¿`,`¡`,`Σ`, `->` arrow) and custom keywords (`navidad`, `gift`, `world`, etc.). Match literal spellings from `scanner.flex` exactly.
  - Parser and lexer are in package `lexer` (see `parser.cup` header). Generated classes expect package alignment.
  - Error handling: use `util.ErrorHandler` to report lex/syntax/semantic errors. It collects and writes `output/reporte_errores.txt`.

- **Developer workflows / common commands**
  1. Generate lexer (JFlex):

     `java -jar lib/jflex.jar src/lexer/scanner.flex`

  2. Generate parser (CUP):

     `java -jar lib/cup.jar -symbols sym -parser Parser src/parser/parser.cup`

     Note: the repo contains VS Code tasks named "Generar Scanner (JFlex)" and "Generar Parser (CUP)" for convenience.

  3. Compile Java sources (example):

     `javac -cp ".;lib/cup.jar" -d bin src/**/**/*.java`

     - If you only want to compile `Main`, use: `javac -cp ".;lib/cup.jar" -d bin src/main/Main.java` but ensure generated sources are present.

  4. Run the compiler (example):

     `java -cp "bin;lib/cup.jar" main.Main input/prueba.txt`

  5. Inspect outputs in the `output/` folder: `tokens.txt`, `arbol.json`, `arbol_interactivo.html`, `codigo.asm`, `reporte_errores.txt`.

- **What to check when editing parser/lexer or running into build errors**
  - Always regenerate `Lexer.java` and `Parser.java` after editing `scanner.flex` or `parser.cup`.
  - Ensure package declarations remain `package lexer;` where expected (mismatched packages cause CUP/JFlex compiled classes to be unusable at runtime).
  - If `javac` fails, confirm `lib/cup.jar` exists and that generated sources (`src/lexer/Lexer.java`, `src/lexer/Parser.java`, `sym.java`) are present.

- **Examples of project-specific code patterns**
  - `Main` orchestrates phases and re-creates the `Lexer`/`Parser` for separate lexing and parsing passes; see [src/main/Main.java](src/main/Main.java#L1).
  - The scanner sets `yyline+1` and `yycolumn+1` when returning Symbols; code expects these 1-based coords for error reporting.
  - Semantic checks live inside `parser.cup` as an inner helper (`AnalizadorSemantico`) and across `util/ErrorHandler` and `util/SymbolTable`.

- **If you change code generation**
  - Look at [src/util/CodeGenerator.java](src/util/CodeGenerator.java) — it emits a basic MIPS prologue/epilogue and uses `SymbolTable` for variable lookup; `guardarCodigo()` writes `codigo.asm`.

If anything above is unclear or you'd like more examples (e.g., common refactors, tests to add, or a CI snippet), tell me which part to expand.
