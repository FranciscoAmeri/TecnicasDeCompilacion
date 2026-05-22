grammar MiLenguaje;

// ─── PARSER ──────────────────────────────────────────────────────────────────

programa
    : sentencia* EOF
    ;

sentencia
    : declaracionFuncion
    | declaracion
    | asignacion
    | sentenciaCout
    | sentenciaIf
    | sentenciaWhile
    | sentenciaReturn
    | bloque
    ;

declaracion
    : tipo ID (IGUAL expresion)? PYC
    ;

tipo
    : INT
    | FLOAT
    | DOUBLE
    | CHAR
    | STRING_TYPE
    | BOOL
    | VOID
    ;

asignacion
    : ID IGUAL expresion PYC
    ;

sentenciaCout
    : COUT SHIFT_L expresion PYC
    ;

sentenciaIf
    : IF PA expresion PC bloque (ELSE bloque)?
    ;

sentenciaWhile
    : WHILE PA expresion PC bloque
    ;

bloque
    : LA sentencia* LC
    ;

declaracionFuncion
    : tipo ID PA listaParametros? PC bloque
    ;

listaParametros
    : parametro (COMA parametro)*
    ;

parametro
    : tipo ID
    ;

sentenciaReturn
    : RETURN expresion? PYC
    ;

expresion
    : expresion OR expresion                                            # exprOr
    | expresion AND expresion                                           # exprAnd
    | expresion (EQL | DISTINTO) expresion                             # exprIgualdad
    | expresion (MAYOR | MENOR | MAYOR_IGUAL | MENOR_IGUAL) expresion  # exprRelacional
    | expresion (SUM | RES) expresion                                  # exprAditiva
    | expresion (MUL | DIV | MOD) expresion                           # exprMultiplicativa
    | NOT expresion                                                     # exprNot
    | RES expresion                                                     # exprNegativo
    | PA expresion PC                                                   # exprAgrupada
    | INTEGER                                                           # exprEntero
    | DECIMAL                                                           # exprDecimal
    | CHARACTER                                                         # exprCaracter
    | CADENA                                                            # exprCadena
    | VERDADERO                                                         # exprVerdadero
    | FALSO                                                             # exprFalso
    | ID PA (expresion (COMA expresion)*)? PC                          # exprLlamada
    | ID                                                                # exprIdentificador
    ;

// ─── LEXER ───────────────────────────────────────────────────────────────────

fragment LETRA  : [A-Za-z];
fragment DIGITO : [0-9];

PA   : '(';
PC   : ')';
CA   : '[';
CC   : ']';
LA   : '{';
LC   : '}';
PYC  : ';';
COMA : ',';

IGUAL       : '=';
EQL         : '==';
DISTINTO    : '!=';
MAYOR_IGUAL : '>=';
MENOR_IGUAL : '<=';
MAYOR       : '>';
MENOR       : '<';
SHIFT_L     : '<<';

SUM : '+';
RES : '-';
MUL : '*';
DIV : '/';
MOD : '%';

OR  : '||';
AND : '&&';
NOT : '!';

FOR    : 'for';
WHILE  : 'while';
IF     : 'if';
ELSE   : 'else';
RETURN : 'return';

INT         : 'int';
FLOAT       : 'float';
DOUBLE      : 'double';
CHAR        : 'char';
STRING_TYPE : 'string';
BOOL        : 'bool';
VOID        : 'void';

VERDADERO : 'true';
FALSO     : 'false';

COUT : 'cout';

ID : (LETRA | '_') (LETRA | DIGITO | '_')*;

INTEGER   : DIGITO+;
DECIMAL   : DIGITO+ '.' DIGITO+;
CHARACTER : '\'' (~['\r\n] | '\\' .) '\'';
CADENA    : '"' (~["\r\n] | '\\' .)* '"';

COMENTARIO_LINEA  : '//' ~[\r\n]*  -> skip;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> skip;
WS                : [ \r\n\t]+    -> skip;

OTRO : .;
