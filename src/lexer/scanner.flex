package lexer;
%%

%class Lexer
%public
%unicode
%cup
%line
%column

ALFA = [A-Za-z_]   /*Alfanumerico*/
DIGIT = [0-9]       /*Digito*/
ID = {ALFA}({ALFA}|{DIGIT})*
NUM = {DIGIT}+          /*Numero*/

%%

[ ,\t,\r,\n]+     {/* ignorar estos espacios...testeo */ }


"navidad"       { return new java_cup.runtime.Symbol(sym.MAIN);}



"function" { return new java_cup.runtime.Symbol(sym.FUNCTION); }
"if" { return new java_cup.runtime.Symbol(sym.IF); }
"else" { return new java_cup.runtime.Symbol(sym.ELSE); }
"for" { return new java_cup.runtime.Symbol(sym.FOR); }
"while" { return new java_cup.runtime.Symbol(sym.WHILE); }
"return" { return new java_cup.runtime.Symbol(sym.RETURN); }

"=" { return new java_cup.runtime.Symbol(sym.IGUAL); }
"+" { return new java_cup.runtime.Symbol(sym.SUMA); }
"-" { return new java_cup.runtime.Symbol(sym.RESTA); }
"*" { return new java_cup.runtime.Symbol(sym.MULTIPLICACION); }
"/" { return new java_cup.runtime.Symbol(sym.DIVISION); }
"^" { return new java_cup.runtime.Symbol(sym.POTENCIA); }
"(" { return new java_cup.runtime.Symbol(sym.PARENTizq); }
")" { return new java_cup.runtime.Symbol(sym.PARENder); }
"," { return new java_cup.runtime.Symbol(sym.COMA); }
";" { return new java_cup.runtime.Symbol(sym.PUNTCOMA); }

"¡" { return new java_cup.runtime.Symbol(sym.BLOQAB); } /* inicio de bloque */
"!" { return new java_cup.runtime.Symbol(sym.BLOQCR); } /* fin de bloque */




.               {
    System.err.println("Error léxico: '" + yytext() +
                        "' linea " + (yyline+1) +
                        " columna " + (yycolumn+1));
}