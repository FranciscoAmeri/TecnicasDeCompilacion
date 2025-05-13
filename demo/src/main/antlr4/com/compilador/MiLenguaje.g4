grammar MiLenguaje;

programa
    : (sentencia)* EOF
    ;

sentencia
    : sentenciaIf
    | declaracionFuncion
    | declaracionVariable
    | asignacion
    | retorno
    ;

sentenciaIf
    : IF PA expresion PC bloque (ELSE bloque)?
    ;

bloque
    : LA (sentencia)* LC
    ;

declaracionFuncion
    : tipo ID PA parametros? PC bloque
    ;

parametros
    : parametro (COMA parametro)*
    ;

parametro
    : tipo ID
    ;

declaracionVariable
    : tipo ID PYC
    | tipo ID IGUAL expresion PYC
    ;

asignacion
    : ID IGUAL expresion PYC
    ;

retorno
    : RETURN expresion? PYC
    ;

tipo
    : INT
    | CHAR
    | DOUBLE
    | VOID
    ;

expresion
    : expresion operadorBinario expresion     #expBinaria
    | NOT expresion                           #expNegacion
    | PA expresion PC                         #expParentizada
    | ID                                      #expVariable
    | INTEGER                                 #expEntero
    | DECIMAL                                 #expDecimal
    | CHARACTER                               #expCaracter
    | ID PA argumentos? PC                    #expFuncion
    ;

operadorBinario
    : SUM | RES | MUL | DIV | MOD
    | MAYOR | MAYOR_IGUAL | MENOR | MENOR_IGUAL | EQL | DISTINTO
    | AND | OR
    ;
    
argumentos
    : expresion (COMA expresion)*
    ;
PA   : '(' ;
PC   : ')' ;
CA   : '[' ;
CC   : ']' ;
LA   : '{' ;
LC   : '}' ;

PYC  : ';' ;
COMA : ',' ;

IGUAL : '=' ;

MAYOR  : '>' ;
MAYOR_IGUAL: '>=' ;
MENOR  : '<' ;
MENOR_IGUAL: '<=' ;
EQL  : '==' ;
DISTINTO  : '!=' ;

SUM  : '+' ;
RES  : '-' ;
MUL  : '*' ;
DIV  : '/' ;
MOD  : '%' ;

OR   : '||' ;
AND  : '&&' ;
NOT  : '!'  ;

FOR   : 'for' ;
WHILE : 'while' ;

IF    : 'if' ;
ELSE  : 'else' ;

INT     : 'int' ;
CHAR    : 'char' ;
DOUBLE  : 'double' ;
VOID    : 'void' ;

RETURN : 'return' ;

ID : (LETRA | '_') (LETRA | DIGITO | '_')* ;
INTEGER : DIGITO+ ;
DECIMAL : INTEGER '.' INTEGER ;
CHARACTER : '\'' (~['\r\n] | '\\' .) '\'' ;

COMENTARIO_LINEA : '//' ~[\r\n]* -> skip ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> skip ;

WS : [ \r\n\t] -> skip ;

OTRO : . ;

fragment LETRA : [A-Za-z] ;
fragment DIGITO : [0-9] ;
