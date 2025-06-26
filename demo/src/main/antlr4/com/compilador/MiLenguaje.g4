grammar MiLenguaje;

programa
    : (sentencia)* EOF
    ;

sentencia
    : sentenciaIf
    | sentenciaFor
    | sentenciaWhile
    | sentenciaDoWhile
    | sentenciaSwitch
    | declaracionFuncion
    | declaracionVariable
    | declaracionClase
    | declaracionNamespace
    | declaracionEnum
    | declaracionStruct
    | declaracionInterface
    | declaracionTypedef
    | declaracionUsing
    | asignacion
    | retorno
    | break
    | continue
    | expresion PYC
    ;

sentenciaIf
    : IF PA expresion PC bloque (ELSE IF PA expresion PC bloque)* (ELSE bloque)?
    ;

sentenciaFor
    : FOR PA (declaracionVariable | asignacion | )? PYC expresion? PYC (asignacion | )? PC bloque
    ;

sentenciaWhile
    : WHILE PA expresion PC bloque
    ;

sentenciaDoWhile
    : DO bloque WHILE PA expresion PC PYC
    ;

sentenciaSwitch
    : SWITCH PA expresion PC LA (CASE expresion COLON (sentencia)*)* (DEFAULT COLON (sentencia)*)? LC
    ;

bloque
    : LA (sentencia)* LC
    ;

declaracionClase
    : (modificadorAcceso | FINAL | ABSTRACT)* CLASS ID 
      (COLON modificadorAcceso? ID (COMA modificadorAcceso? ID)*)? 
      LA (miembroClase)* LC
    ;

miembroClase
    : modificadorAcceso COLON
    | declaracionVariable
    | declaracionFuncion
    | constructor
    | destructor
    ;

constructor
    : ID PA parametros? PC (COLON inicializador (COMA inicializador)*)? bloque
    ;

destructor
    : TILDE ID PA PC bloque
    ;

inicializador
    : ID PA expresion PC
    ;

modificadorAcceso
    : PUBLIC
    | PRIVATE
    | PROTECTED
    ;

declaracionNamespace
    : NAMESPACE ID LA (sentencia)* LC
    ;

declaracionEnum
    : ENUM (CLASS)? ID LA (ID (IGUAL expresion)? (COMA ID (IGUAL expresion)?)*)? LC
    ;

declaracionStruct
    : STRUCT ID (COLON modificadorAcceso? ID (COMA modificadorAcceso? ID)*)? 
      LA (miembroClase)* LC
    ;

declaracionInterface
    : INTERFACE ID (COLON ID (COMA ID)*)? LA (miembroClase)* LC
    ;

declaracionTypedef
    : TYPEDEF tipo ID PYC
    ;

declaracionUsing
    : USING (ID IGUAL)? tipo PYC
    ;

declaracionFuncion
    : (modificadorAcceso | STATIC | VIRTUAL | OVERRIDE | FINAL | INLINE | CONST | VOLATILE)* 
      tipo ID PA parametros? PC (CONST)? bloque
    ;

parametros
    : parametro (COMA parametro)*
    ;

parametro
    : (modificadorAcceso | CONST | VOLATILE)* 
      tipo (PTR | AMPERSAND)* ID (CA INTEGER? CC)*
    ;

declaracionVariable
    : (modificadorAcceso | STATIC | CONST | VOLATILE | MUTABLE)* 
      tipo (PTR | AMPERSAND)* ID (CA INTEGER? CC)* 
      (IGUAL expresion)? PYC
    ;

asignacion
    : ID IGUAL expresion PYC
    | ID CA expresion CC IGUAL expresion PYC
    | PTR ID IGUAL expresion PYC
    ;

retorno
    : RETURN expresion? PYC
    ;

tipo
    : (CONST | VOLATILE)* 
      (INT | CHAR | DOUBLE | FLOAT | BOOL | VOID | STRING | ID)
      (PTR | AMPERSAND)*
    ;

expresion
    : expresion operadorBinario expresion     #expBinaria
    | NOT expresion                           #expNegacion
    | TILDE expresion                         #expComplemento
    | INCREMENTO expresion                    #expPreIncremento
    | expresion INCREMENTO                    #expPostIncremento
    | DECREMENTO expresion                    #expPreDecremento
    | expresion DECREMENTO                    #expPostDecremento
    | PA expresion PC                         #expParentizada
    | ID                                      #expVariable
    | INTEGER                                 #expEntero
    | DECIMAL                                 #expDecimal
    | CHARACTER                               #expCaracter
    | STRING                                  #expCadena
    | TRUE                                    #expTrue
    | FALSE                                   #expFalse
    | NULL                                    #expNull
    | THIS                                    #expThis
    | SUPER                                   #expSuper
    | ID PA argumentos? PC                    #expFuncion
    | expresion PUNTO ID                      #expAccesoMiembro
    | expresion FLECHA ID                     #expAccesoPuntero
    | expresion CA expresion CC               #expAccesoArray
    | NEW tipo (PA argumentos? PC)?           #expNew
    | DELETE expresion                        #expDelete
    | SIZEOF PA (tipo | expresion) PC         #expSizeof
    | CAST PA tipo PC expresion               #expCast
    ;

operadorBinario
    : SUM | RES | MUL | DIV | MOD
    | MAYOR | MAYOR_IGUAL | MENOR | MENOR_IGUAL | EQL | DISTINTO
    | AND | OR | XOR
    | SHIFT_IZQ | SHIFT_DER
    | AND_BIT | OR_BIT | XOR_BIT
    | PTR_OP
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
AMPERSAND : '&' ;
PTR : '*' ;
PTR_OP : '->' ;
FLECHA : '->' ;

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

INCREMENTO : '++' ;
DECREMENTO : '--' ;

OR   : '||' ;
AND  : '&&' ;
NOT  : '!'  ;
XOR  : '^'  ;

SHIFT_IZQ : '<<' ;
SHIFT_DER : '>>' ;
AND_BIT   : '&'  ;
OR_BIT    : '|'  ;
XOR_BIT   : '^'  ;

FOR   : 'for' ;
WHILE : 'while' ;
DO    : 'do' ;
IF    : 'if' ;
ELSE  : 'else' ;
SWITCH: 'switch' ;
CASE  : 'case' ;
DEFAULT: 'default' ;

BREAK    : 'break' ;
CONTINUE : 'continue' ;
RETURN   : 'return' ;

CLASS     : 'class' ;
STRUCT    : 'struct' ;
ENUM      : 'enum' ;
INTERFACE : 'interface' ;
NAMESPACE : 'namespace' ;
TYPEDEF   : 'typedef' ;
USING     : 'using' ;

PUBLIC    : 'public' ;
PRIVATE   : 'private' ;
PROTECTED : 'protected' ;

STATIC    : 'static' ;
VIRTUAL   : 'virtual' ;
OVERRIDE  : 'override' ;
FINAL     : 'final' ;
ABSTRACT  : 'abstract' ;
INLINE    : 'inline' ;
CONST     : 'const' ;
VOLATILE  : 'volatile' ;
MUTABLE   : 'mutable' ;

INT     : 'int' ;
CHAR    : 'char' ;
DOUBLE  : 'double' ;
FLOAT   : 'float' ;
BOOL    : 'bool' ;
VOID    : 'void' ;
STRING  : 'string' ;

TRUE    : 'true' ;
FALSE   : 'false' ;
NULL    : 'null' ;
THIS    : 'this' ;
SUPER   : 'super' ;

NEW     : 'new' ;
DELETE  : 'delete' ;
SIZEOF  : 'sizeof' ;
CAST    : 'cast' ;

ID : (LETRA | '_') (LETRA | DIGITO | '_')* ;
INTEGER : DIGITO+ ;
DECIMAL : INTEGER '.' INTEGER ;
CHARACTER : '\'' (~['\r\n] | '\\' .) '\'' ;
STRING : '"' (~["\r\n] | '\\' .)* '"' ;

COMENTARIO_LINEA : '//' ~[\r\n]* -> skip ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> skip ;

WS : [ \r\n\t] -> skip ;

fragment LETRA : [A-Za-z] ;
fragment DIGITO : [0-9] ;

