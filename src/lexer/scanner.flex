package lexer;
import java_cup.runtime.Symbol;

%%
%class Lexer
%public
%unicode
%cup
%line
%column

%state COMENTARIO_MULTI

ALFA   = [A-Za-z_]
DIGIT  = [0-9]
ID     = {ALFA}({ALFA}|{DIGIT})*
INT    = {DIGIT}+
FLOAT  = {DIGIT}+"."{DIGIT}+

ESPACIO = [ \t\f\r\n]+

%%

/* =========================
   ESPACIOS Y COMENTARIOS
   ========================= */
{ESPACIO}                 { /* ignorar */ }

/* comentario de una línea: | ... fin de línea */
"|"[^\n\r]*               { /* ignorar */ }

/* comentario multi: є .... э */
"є"                       { yybegin(COMENTARIO_MULTI); }
<COMENTARIO_MULTI>[^э]+   { /* consumir hasta cierre (varios chars) */ }
<COMENTARIO_MULTI>"э"     { yybegin(YYINITIAL); }
<COMENTARIO_MULTI>[\s\S]  { /* consumir un caracter restante (incluye salto de línea) */ }
/* =========================
   PALABRAS RESERVADAS / CONTROL
   ========================= */
"navidad"   { return new Symbol(sym.MAIN,   yyline+1, yycolumn+1); }  /* main */
"gift"      { return new Symbol(sym.GIFT,   yyline+1, yycolumn+1); }

"world"     { return new Symbol(sym.WORLD,  yyline+1, yycolumn+1); }
"local"     { return new Symbol(sym.LOCAL,  yyline+1, yycolumn+1); }
"endl"      { return new Symbol(sym.ENDL,   yyline+1, yycolumn+1); }

"int"       { return new Symbol(sym.T_INT,    yyline+1, yycolumn+1); }
"float"     { return new Symbol(sym.T_FLOAT,  yyline+1, yycolumn+1); }
"bool"      { return new Symbol(sym.T_BOOL,   yyline+1, yycolumn+1); }
"char"      { return new Symbol(sym.T_CHAR,   yyline+1, yycolumn+1); }
"string"    { return new Symbol(sym.T_STRING, yyline+1, yycolumn+1); }
"coal"      { return new Symbol(sym.T_COAL,   yyline+1, yycolumn+1); }

"true"      { return new Symbol(sym.BOOL_LIT, yyline+1, yycolumn+1, Boolean.TRUE); }
"false"     { return new Symbol(sym.BOOL_LIT, yyline+1, yycolumn+1, Boolean.FALSE); }

"return"    { return new Symbol(sym.RETURN, yyline+1, yycolumn+1); }
"break"     { return new Symbol(sym.BREAK,  yyline+1, yycolumn+1); }

"decide"    { return new Symbol(sym.DECIDE, yyline+1, yycolumn+1); }
"of"        { return new Symbol(sym.OF,     yyline+1, yycolumn+1); }
"else"      { return new Symbol(sym.ELSE,   yyline+1, yycolumn+1); }
"end"       { return new Symbol(sym.END,    yyline+1, yycolumn+1); }

"loop"      { return new Symbol(sym.LOOP,   yyline+1, yycolumn+1); }
"exit"      { return new Symbol(sym.EXIT,   yyline+1, yycolumn+1); }
"when"      { return new Symbol(sym.WHEN,   yyline+1, yycolumn+1); }

"for"       { return new Symbol(sym.FOR,    yyline+1, yycolumn+1); }

/* =========================
   OPERADORES (orden importa)
   ========================= */

/* ++ y -- antes que + y - */
"++"        { return new Symbol(sym.INC, yyline+1, yycolumn+1); }
"--"        { return new Symbol(sym.DEC, yyline+1, yycolumn+1); }

/* relacionales de 2 chars antes que 1 char */
"<="        { return new Symbol(sym.LE,  yyline+1, yycolumn+1); }
">="        { return new Symbol(sym.GE,  yyline+1, yycolumn+1); }
"=="        { return new Symbol(sym.EQ,  yyline+1, yycolumn+1); }
"!="        { return new Symbol(sym.NEQ, yyline+1, yycolumn+1); }

"<"         { return new Symbol(sym.LT,  yyline+1, yycolumn+1); }
">"         { return new Symbol(sym.GT,  yyline+1, yycolumn+1); }

"="         { return new Symbol(sym.IGUAL, yyline+1, yycolumn+1); }

"//"        { return new Symbol(sym.INTDIV, yyline+1, yycolumn+1); }
"/"         { return new Symbol(sym.DIVISION, yyline+1, yycolumn+1); }

"*"         { return new Symbol(sym.MULTIPLICACION, yyline+1, yycolumn+1); }
"%"         { return new Symbol(sym.MODULO, yyline+1, yycolumn+1); }
"+"         { return new Symbol(sym.SUMA, yyline+1, yycolumn+1); }
"-"         { return new Symbol(sym.RESTA, yyline+1, yycolumn+1); }
"^"         { return new Symbol(sym.POTENCIA, yyline+1, yycolumn+1); }

/* lógicos */
"@"         { return new Symbol(sym.AND, yyline+1, yycolumn+1); }
"~"         { return new Symbol(sym.OR,  yyline+1, yycolumn+1); }
"Σ"         { return new Symbol(sym.NOT, yyline+1, yycolumn+1); }

/* flecha */
"->"        { return new Symbol(sym.ARROW, yyline+1, yycolumn+1); }

/* =========================
   DELIMITADORES
   ========================= */
"¿"         { return new Symbol(sym.PARENTizq, yyline+1, yycolumn+1); }
"?"         { return new Symbol(sym.PARENder,  yyline+1, yycolumn+1); }

"["         { return new Symbol(sym.CORCHizq, yyline+1, yycolumn+1); }
"]"         { return new Symbol(sym.CORCHder, yyline+1, yycolumn+1); }

","         { return new Symbol(sym.COMA, yyline+1, yycolumn+1); }

/* bloques */
"¡"         { return new Symbol(sym.BLOQAB, yyline+1, yycolumn+1); }
"!"         { return new Symbol(sym.BLOQCR, yyline+1, yycolumn+1); }

/* =========================
   LITERALES
   ========================= */
{FLOAT}     { return new Symbol(sym.FLOAT_LIT, yyline+1, yycolumn+1, Double.parseDouble(yytext())); }
{INT}       { return new Symbol(sym.INT_LIT,   yyline+1, yycolumn+1, Integer.parseInt(yytext())); }

/* literal char: 'a' o escapes */
'([^\\\n\r]|\\[nrt\\'])' {
    String s = yytext();
    char c;
    if (s.charAt(1) == '\\') {
        char e = s.charAt(2);
        c = (e=='n')?'\n':(e=='r')?'\r':(e=='t')?'\t':e;
    } else {
        c = s.charAt(1);
    }
    return new Symbol(sym.CHAR_LIT, yyline+1, yycolumn+1, c);
}


/* string literal: "...." con escapes básicos */
\"([^\\\"\n\r]|\\[nrt\"\\])*\" {
  String raw = yytext().substring(1, yytext().length()-1);
  String val = raw
    .replace("\\n","\n").replace("\\r","\r").replace("\\t","\t")
    .replace("\\\"","\"").replace("\\\\","\\");
  return new Symbol(sym.STRING_LIT, yyline+1, yycolumn+1, val);
}

/* =========================
   IDENT
   ========================= */
{ID}        { return new Symbol(sym.IDENT, yyline+1, yycolumn+1, yytext()); }

/* EOF */
<<EOF>>     { return new Symbol(sym.EOF); }

/* Error léxico. despliega el error con linea y columna y prosigue con el siguiente token */
. {
  System.err.println("Error léxico: '" + yytext() +
    "' línea " + (yyline+1) + " columna " + (yycolumn+1));
}