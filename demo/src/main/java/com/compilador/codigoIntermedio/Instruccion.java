package com.compilador.codigoIntermedio;

/**
 * Representa UNA instrucción de código de tres direcciones (TAC).
 *
 * En TAC cada instrucción opera con a lo sumo tres "direcciones"
 * (variables, temporales o literales). La forma general es:
 *
 *     resultado = arg1  operador  arg2
 *
 * Por ejemplo:
 *     t1 = a + b        →  resultado="t1"  arg1="a"  operador="+"  arg2="b"
 *     if_false t1 goto L2 →  resultado="L2"  arg1="t1"  (operador y arg2 son null)
 *     return t1         →  arg1="t1"  (el resto es null)
 *
 * No todos los campos se usan en todos los tipos; los que no aplican
 * se guardan como null. El tipo exacto de instrucción queda fijo en
 * el campo "tipo" (ver enum Tipo).
 *
 * Los objetos son INMUTABLES: una vez creados no se modifican.
 * Solo se pueden crear usando los métodos de fábrica estáticos
 * (asignar, binaria, etiqueta, etc.), nunca con "new Instruccion(...)".
 */
public class Instruccion {

    // =========================================================================
    // ENUM: tipos de instrucción
    // =========================================================================
    //
    // Cada valor representa una forma distinta de instrucción TAC.
    // El comentario de cada línea muestra cómo se ve al imprimirse.

    public enum Tipo {
        ASIGNAR,           // "x = y"                      →  copia simple
        BINARIA,           // "t1 = a + b"                 →  operación con dos operandos
        UNARIA,            // "t1 = -x"                    →  operación con un operando
        ETIQUETA,          // "L1:"                        →  punto de salto en el código
        SALTO_INCOND,      // "goto L1"                    →  salta siempre
        SALTO_COND_FALSO,  // "if_false t1 goto L1"        →  salta solo si la condición es false
        PARAM,             // "param x"                    →  declara un argumento antes de llamar
        LLAMADA,           // "t2 = call suma, 2"          →  llama a una función
        RETORNO,           // "return t1"  o  "return"     →  sale de una función
        IMPRIMIR,          // "print z"                    →  imprime en consola (cout)
        INICIO_FUNC,       // "begin_func suma"            →  marca el inicio de una función
        FIN_FUNC           // "end_func suma"              →  marca el fin de una función
    }

    // =========================================================================
    // CAMPOS internos
    // =========================================================================
    //
    // Todos son final: no cambian después de que el objeto es creado.
    //
    // Qué campo contiene qué según el tipo de instrucción:
    //
    //  Tipo              │ resultado       │ arg1          │ operador │ arg2
    //  ──────────────────┼─────────────────┼───────────────┼──────────┼────────
    //  ASIGNAR           │ destino  "x"    │ fuente "10"   │  null    │  null
    //  BINARIA           │ destino  "t1"   │ izq    "a"    │  "+"     │ "b"
    //  UNARIA            │ destino  "t1"   │ operando "x"  │  "-"     │  null
    //  ETIQUETA          │ nombre   "L1"   │  null         │  null    │  null
    //  SALTO_INCOND      │ etiqueta "L1"   │  null         │  null    │  null
    //  SALTO_COND_FALSO  │ etiqueta "L1"   │ cond   "t1"   │  null    │  null
    //  PARAM             │  null           │ arg    "x"    │  null    │  null
    //  LLAMADA           │ destino  "t2"   │ función "suma"│  null    │ "#args"
    //  RETORNO           │  null           │ valor  "t1"   │  null    │  null
    //  IMPRIMIR          │  null           │ valor  "z"    │  null    │  null
    //  INICIO_FUNC       │ nombre   "suma" │  null         │  null    │  null
    //  FIN_FUNC          │ nombre   "suma" │  null         │  null    │  null

    private final Tipo   tipo;
    private final String resultado;   // dónde se guarda el resultado (o nombre de etiqueta/función)
    private final String arg1;        // primer operando, condición, o argumento
    private final String operador;    // símbolo aritmético/lógico: +, -, *, /, %, ==, !=, <, >, &&, ||, !, -
    private final String arg2;        // segundo operando o número de argumentos en LLAMADA

    // Constructor privado: la única manera de crear una Instruccion es
    // a través de los métodos de fábrica de abajo. Así se garantiza que
    // cada tipo siempre tenga exactamente los campos que necesita.
    private Instruccion(Tipo tipo, String resultado, String arg1, String operador, String arg2) {
        this.tipo      = tipo;
        this.resultado = resultado;
        this.arg1      = arg1;
        this.operador  = operador;
        this.arg2      = arg2;
    }

    // Getter usado por CodigoTresDir.imprimir() para saber si la instrucción
    // es una ETIQUETA (se imprime sin número de línea) o no (se imprime numerada).
    public Tipo getTipo() { return tipo; }


    // =========================================================================
    // MÉTODOS DE FÁBRICA
    // =========================================================================
    //
    // Un método de fábrica es un método estático que crea y devuelve
    // un objeto. Su nombre describe qué tipo de instrucción produce,
    // lo que hace el código que llama a estas fábricas mucho más legible.
    //
    // Ejemplo sin fábrica (difícil de leer):
    //   new Instruccion(Tipo.BINARIA, "t1", "a", "+", "b")
    //
    // Ejemplo con fábrica (claro e intuitivo):
    //   Instruccion.binaria("t1", "a", "+", "b")

    /**
     * Copia el valor de src en dest.
     *
     * Ejemplos de uso y lo que produce:
     *   asignar("x", "10")    →  x = 10
     *   asignar("z", "t3")    →  z = t3
     *   asignar("i", "0")     →  i = 0
     */
    public static Instruccion asignar(String dest, String src) {
        return new Instruccion(Tipo.ASIGNAR, dest, src, null, null);
    }

    /**
     * Aplica un operador binario a dos operandos y guarda el resultado en dest.
     * El operador puede ser aritmético (+, -, *, /, %) o relacional/lógico (==, !=, <, >, <=, >=, &&, ||).
     *
     * Ejemplos:
     *   binaria("t1", "a",  "+",  "b")   →  t1 = a + b
     *   binaria("t2", "x",  ">",  "y")   →  t2 = x > y
     *   binaria("t3", "t1", "&&", "t2")  →  t3 = t1 && t2
     */
    public static Instruccion binaria(String dest, String a, String op, String b) {
        return new Instruccion(Tipo.BINARIA, dest, a, op, b);
    }

    /**
     * Aplica un operador unario a un solo operando y guarda el resultado en dest.
     * Los operadores posibles son: - (negación numérica) y ! (negación lógica).
     *
     * Ejemplos:
     *   unaria("t1", "-", "x")    →  t1 = -x
     *   unaria("t2", "!", "cond") →  t2 = !cond
     *
     * Nota interna: el operando "a" se guarda en arg1 y el símbolo en "operador".
     */
    public static Instruccion unaria(String dest, String op, String a) {
        return new Instruccion(Tipo.UNARIA, dest, a, op, null);
    }

    /**
     * Define un punto en el código al que se puede saltar con goto o if_false.
     * En el listado de instrucciones se imprime sin número de línea.
     *
     * Ejemplos:
     *   etiqueta("L1")  →  L1:
     *   etiqueta("L4")  →  L4:
     *
     * Cómo se usan en la práctica (while):
     *   L1:              ← comienzo del ciclo (se vuelve aquí en cada iteración)
     *   ( 5)  if_false t1 goto L2
     *   ...cuerpo...
     *   ( 9)  goto L1    ← volver arriba
     *   L2:              ← salida del ciclo
     */
    public static Instruccion etiqueta(String label) {
        // El nombre de la etiqueta se guarda en "resultado" porque es el único dato que necesita.
        return new Instruccion(Tipo.ETIQUETA, label, null, null, null);
    }

    /**
     * Salta siempre a la etiqueta indicada, sin evaluar ninguna condición.
     * Se usa para saltar el bloque else una vez que el then ya se ejecutó.
     *
     * Ejemplos:
     *   saltoIncond("L3")  →  goto L3
     *   saltoIncond("L1")  →  goto L1   (retorno al inicio de un while)
     */
    public static Instruccion saltoIncond(String label) {
        return new Instruccion(Tipo.SALTO_INCOND, label, null, null, null);
    }

    /**
     * Salta a la etiqueta SOLO SI la condición es false.
     * Es la instrucción clave para implementar if y while:
     * si la condición falla, se salta el bloque de código.
     *
     * Ejemplos:
     *   saltoCondFalso("t1", "L2")  →  if_false t1 goto L2
     *   saltoCondFalso("t3", "L4")  →  if_false t3 goto L4
     *
     * Cómo se ve en un if sin else:
     *   (3)  t1 = x > 0        ← evaluar condición
     *   (4)  if_false t1 goto L1  ← si x <= 0, saltar el bloque
     *   (5)  print x
     *   L1:
     *
     * Nota sobre los campos:
     *   cond  se guarda en arg1      (la variable que se evalúa)
     *   label se guarda en resultado (a dónde saltar si es false)
     */
    public static Instruccion saltoCondFalso(String cond, String label) {
        return new Instruccion(Tipo.SALTO_COND_FALSO, label, cond, null, null);
    }

    /**
     * Declara un argumento que se pasará en la próxima llamada a función.
     * Se emite uno por cada argumento, en orden, justo antes del LLAMADA.
     *
     * Ejemplos para suma(x, y):
     *   param("x")  →  param x
     *   param("y")  →  param y
     *   ... luego:  t2 = call suma, 2
     *
     * El receptor de la llamada sabe cuántos PARAM leer gracias al campo
     * numArgs del LLAMADA (en este caso, 2).
     */
    public static Instruccion param(String arg) {
        return new Instruccion(Tipo.PARAM, null, arg, null, null);
    }

    /**
     * Llama a una función con los argumentos ya declarados con PARAM.
     * El resultado de la función se guarda en el temporal dest.
     * numArgs indica cuántos PARAM se emitieron antes.
     *
     * Ejemplo completo para  int z = suma(x, y):
     *   param x
     *   param y
     *   t3 = call suma, 2   ← "2" son los 2 params anteriores
     *   z = t3
     *
     * Campos internos:
     *   dest    → resultado   ("t3")
     *   func    → arg1        ("suma")
     *   numArgs → arg2        ("2")
     */
    public static Instruccion llamada(String dest, String func, String numArgs) {
        return new Instruccion(Tipo.LLAMADA, dest, func, null, numArgs);
    }

    /**
     * Sale de la función actual devolviendo un valor.
     *
     * Ejemplos:
     *   retorno("t1")  →  return t1
     *   retorno("0")   →  return 0
     */
    public static Instruccion retorno(String valor) {
        return new Instruccion(Tipo.RETORNO, null, valor, null, null);
    }

    /**
     * Sale de la función actual sin devolver ningún valor (funciones void).
     *
     * Ejemplo:
     *   retornoVacio()  →  return
     *
     * Internamente arg1 queda null; toString() detecta esto y omite el operando.
     */
    public static Instruccion retornoVacio() {
        return new Instruccion(Tipo.RETORNO, null, null, null, null);
    }

    /**
     * Imprime un valor en consola. Se genera a partir de "cout << expr".
     *
     * Ejemplos:
     *   imprimir("z")            →  print z
     *   imprimir("\"es mayor\"") →  print "es mayor"
     *   imprimir("t2")           →  print t2
     */
    public static Instruccion imprimir(String arg) {
        return new Instruccion(Tipo.IMPRIMIR, null, arg, null, null);
    }

    /**
     * Marca el comienzo del cuerpo de una función en el listado TAC.
     * Sirve como delimitador visual y para futuros análisis o generación de código.
     *
     * Ejemplo:
     *   inicioFuncion("suma")  →  begin_func suma
     */
    public static Instruccion inicioFuncion(String nombre) {
        return new Instruccion(Tipo.INICIO_FUNC, nombre, null, null, null);
    }

    /**
     * Marca el fin del cuerpo de una función.
     * Siempre aparece en pareja con el INICIO_FUNC correspondiente.
     *
     * Ejemplo:
     *   finFuncion("suma")  →  end_func suma
     *
     * Bloque completo de una función:
     *   begin_func suma
     *     t1 = a + b
     *     return t1
     *   end_func suma
     */
    public static Instruccion finFuncion(String nombre) {
        return new Instruccion(Tipo.FIN_FUNC, nombre, null, null, null);
    }


    // =========================================================================
    // toString() — convierte la instrucción en texto imprimible
    // =========================================================================
    //
    // Cada case ensambla la cadena usando los campos que corresponden al tipo.
    // Los campos que son null simplemente no se usan en ese case.

    @Override
    public String toString() {
        switch (tipo) {

            case ASIGNAR:
                // resultado = arg1
                // Ejemplo:  x = 10   |   z = t3
                return resultado + " = " + arg1;

            case BINARIA:
                // resultado = arg1 operador arg2
                // Ejemplo:  t1 = a + b   |   t2 = x > y
                return resultado + " = " + arg1 + " " + operador + " " + arg2;

            case UNARIA:
                // resultado = operador arg1   (sin espacio entre operador y operando)
                // Ejemplo:  t1 = -x   |   t2 = !cond
                return resultado + " = " + operador + arg1;

            case ETIQUETA:
                // resultado:
                // Ejemplo:  L1:   |   L4:
                return resultado + ":";

            case SALTO_INCOND:
                // goto resultado
                // Ejemplo:  goto L3
                return "goto " + resultado;

            case SALTO_COND_FALSO:
                // if_false arg1 goto resultado
                // arg1      = condición a evaluar (ej. "t1")
                // resultado = etiqueta de destino  (ej. "L2")
                // Ejemplo:  if_false t1 goto L2
                return "if_false " + arg1 + " goto " + resultado;

            case PARAM:
                // param arg1
                // Ejemplo:  param x   |   param y
                return "param " + arg1;

            case LLAMADA:
                // resultado = call arg1, arg2
                // arg1 = nombre de la función, arg2 = número de argumentos
                // Si resultado es null: la función se llama pero el valor de retorno se ignora.
                // Ejemplo con retorno:  t2 = call suma, 2
                // Ejemplo sin retorno:  call imprimir, 1
                if (resultado != null) return resultado + " = call " + arg1 + ", " + arg2;
                else                   return "call " + arg1 + ", " + arg2;

            case RETORNO:
                // return arg1   |   return
                // Si arg1 es null → función void, no hay valor que devolver.
                // Ejemplo con valor:  return t1
                // Ejemplo vacío:      return
                if (arg1 != null) return "return " + arg1;
                else              return "return";

            case IMPRIMIR:
                // print arg1
                // Ejemplo:  print z   |   print "es mayor x"
                return "print " + arg1;

            case INICIO_FUNC:
                // begin_func resultado
                // Ejemplo:  begin_func suma
                return "begin_func " + resultado;

            case FIN_FUNC:
                // end_func resultado
                // Ejemplo:  end_func suma
                return "end_func " + resultado;

            default:
                return "???";
        }
    }
}
