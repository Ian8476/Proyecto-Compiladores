package lexer;
import java_cup.runtime.Symbol;

%%
%class Lexer
%public
%unicode
%cup
%line
%column

ALFA  = [A-Za-z_]
DIGIT = [0-9]
ID    = {ALFA}({ALFA}|{DIGIT})*
NUM   = {DIGIT}+

%%

/* Ignorar espacios */
[ \t\r\n]+ {}

/* MAIN */
"navidad" { return new Symbol(sym.MAIN, yyline+1, yycolumn+1); }

/* Palabras reservadas */
"function" { return new Symbol(sym.FUNCTION, yyline+1, yycolumn+1); }
"if"       { return new Symbol(sym.IF, yyline+1, yycolumn+1); }
"else"     { return new Symbol(sym.ELSE, yyline+1, yycolumn+1); }
"for"      { return new Symbol(sym.FOR, yyline+1, yycolumn+1); }
"while"    { return new Symbol(sym.WHILE, yyline+1, yycolumn+1); }
"return"   { return new Symbol(sym.RETURN, yyline+1, yycolumn+1); }

/* Operadores */
"=" { return new Symbol(sym.IGUAL, yyline+1, yycolumn+1); }
"+" { return new Symbol(sym.SUMA, yyline+1, yycolumn+1); }
"-" { return new Symbol(sym.RESTA, yyline+1, yycolumn+1); }
"*" { return new Symbol(sym.MULTIPLICACION, yyline+1, yycolumn+1); }
"/" { return new Symbol(sym.DIVISION, yyline+1, yycolumn+1); }
"^" { return new Symbol(sym.POTENCIA, yyline+1, yycolumn+1); }

/* Delimitadores */
"(" { return new Symbol(sym.PARENTizq, yyline+1, yycolumn+1); }
")" { return new Symbol(sym.PARENder, yyline+1, yycolumn+1); }
"," { return new Symbol(sym.COMA, yyline+1, yycolumn+1); }
";" { return new Symbol(sym.PUNTCOMA, yyline+1, yycolumn+1); }

/* Bloques */
"¡" { return new Symbol(sym.BLOQAB, yyline+1, yycolumn+1); }
"!" { return new Symbol(sym.BLOQCR, yyline+1, yycolumn+1); }

/* Identificadores y números */
{ID}  { return new Symbol(sym.IDENT, yyline+1, yycolumn+1, yytext()); }
{NUM} { return new Symbol(sym.NUMBER, yyline+1, yycolumn+1, Integer.parseInt(yytext())); }

/* EOF */
<<EOF>> { return new Symbol(sym.EOF); }

/* Error léxico */
. {
  System.err.println(
    "Error léxico: '" + yytext() +
    "' línea " + (yyline+1) +
    " columna " + (yycolumn+1)
  );
}
