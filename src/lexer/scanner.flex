package lexer;
%%

%class Lexer
%public
%unicode 
%cup
%line
%column

%%

[ \t\r\n]+     {/* ignorar estos espacios...testeo */ }

"navidad"       { return new java_cup.runtime.Symbol(sym.MAIN);}

.               {
    System.err.println("Error léxico: '" + yytext() +
                        "' linea " + (yyline+1) +
                        " columna " + (yycolumn+1));
}