package com.compilador.semantico;

/**
 * Centraliza todas las reglas de tipos del lenguaje.
 *
 * Diseño: métodos estáticos puros — no guardan estado, solo reciben tipos
 * (como Strings) y devuelven el tipo resultante o ERROR.
 *
 * Jerarquía numérica para widening: int < float < double
 *   Un tipo "más ancho" puede recibir un tipo "más estrecho" sin pérdida.
 *   Ejemplo: double puede contener int, pero int NO puede contener float.
 */
public class TypeSystem {

    private TypeSystem() {}  // clase utilitaria — no instanciar

    // ── Constantes de tipo ──────────────────────────────────────────────────
    public static final String INT    = "int";
    public static final String FLOAT  = "float";
    public static final String DOUBLE = "double";
    public static final String CHAR   = "char";
    public static final String STRING = "string";
    public static final String BOOL   = "bool";
    public static final String VOID   = "void";

    /**
     * Tipo centinela que indica que una subexpresión ya produjo un error.
     * Al propagarlo en vez de generar un nuevo error se evitan cascadas:
     *   int x = "hola" + 1;  → un solo error en el literal "hola", no dos.
     * Toda función que recibe ERROR como operando lo devuelve sin reportar nada.
     */
    public static final String ERROR  = "ERROR";

    /**
     * Verifica si se puede asignar un valor de tipo {@code desde} a una
     * variable declarada como {@code hacia}.
     *
     * Reglas de widening permitido:
     *   double ← int | float   (acepta tipos más estrechos)
     *   float  ← int | double  (acepta int más estrecho, y double más ancho*)
     *   int    ← int           (solo exacto — no acepta float ni double)
     *
     * (*) float ← double es permisivo para no bloquear operaciones mixtas;
     *     en la práctica el programador debería usar cast explícito.
     *
     * Si alguno de los dos tipos es ERROR se retorna true para silenciar
     * cualquier segundo error derivado de un primero ya reportado.
     *
     * Ejemplos:
     *   esCompatibleAsignacion("int",    "int")    → true   (mismo tipo)
     *   esCompatibleAsignacion("double", "int")    → true   (widening)
     *   esCompatibleAsignacion("double", "float")  → true   (widening)
     *   esCompatibleAsignacion("int",    "float")  → false  (perdería precisión)
     *   esCompatibleAsignacion("int",    "string") → false  (tipos incompatibles)
     *   esCompatibleAsignacion("int",    "ERROR")  → true   (silencia cascada)
     */
    public static boolean esCompatibleAsignacion(String hacia, String desde) {
        if (hacia == null || desde == null) return false;
        if (ERROR.equals(hacia) || ERROR.equals(desde)) return true;  // error ya reportado
        if (hacia.equals(desde)) return true;
        if (DOUBLE.equals(hacia) && (INT.equals(desde) || FLOAT.equals(desde))) return true;
        return FLOAT.equals(hacia) && (INT.equals(desde) || DOUBLE.equals(desde));
    }

    /**
     * Tipo resultante de una operación aritmética (+, -, *, /).
     *
     * Aplica promoción numérica: el tipo de mayor precisión "gana".
     *   double op X → double
     *   float  op X → float   (si no hay double)
     *   int    op int → int
     *
     * Retorna ERROR si algún operando no es numérico (ej. "hola" + 3).
     *
     * Ejemplos:
     *   inferirAritmetico("int",    "int")    → "int"
     *   inferirAritmetico("int",    "float")  → "float"   (float gana)
     *   inferirAritmetico("float",  "double") → "double"  (double gana)
     *   inferirAritmetico("int",    "string") → "ERROR"   (string no es numérico)
     *   inferirAritmetico("ERROR",  "int")    → "ERROR"   (propaga centinela)
     */
    public static String inferirAritmetico(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der) || izq == null || der == null) return ERROR;
        if (!esNumerico(izq) || !esNumerico(der)) return ERROR;
        if (DOUBLE.equals(izq) || DOUBLE.equals(der)) return DOUBLE;  // máxima precisión
        if (FLOAT.equals(izq)  || FLOAT.equals(der))  return FLOAT;
        return INT;
    }

    /**
     * Tipo resultante de una comparación relacional (<, >, <=, >=).
     * Solo acepta operandos numéricos; el resultado siempre es bool.
     * No se permiten comparaciones como "hola" < "mundo".
     */
    public static String inferirRelacional(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der) || izq == null || der == null) return ERROR;
        if (!esNumerico(izq) || !esNumerico(der)) return ERROR;
        return BOOL;  // la comparación siempre produce un booleano
    }

    /**
     * Tipo resultante de una comparación de igualdad (==, !=).
     * Más permisivo que relacional: admite comparar tipos iguales o
     * cualquier combinación de numéricos (int == float es válido).
     */
    public static String inferirIgualdad(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der) || izq == null || der == null) return ERROR;
        if (izq.equals(der)) return BOOL;
        if (esNumerico(izq) && esNumerico(der)) return BOOL;  // int == float es aceptable
        return ERROR;
    }

    /**
     * Tipo resultante de un operador lógico (&&, ||).
     * Estricto: ambos operandos deben ser bool — no hay coerción implícita
     * (a diferencia de C/C++ donde 0/1 se convierten a bool automáticamente).
     */
    public static String inferirLogico(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der) || izq == null || der == null) return ERROR;
        if (BOOL.equals(izq) && BOOL.equals(der)) return BOOL;
        return ERROR;
    }

    /**
     * Tipo resultante del operador unario NOT (!expr).
     * El operando debe ser bool; el resultado también es bool.
     */
    public static String inferirNot(String operando) {
        if (ERROR.equals(operando) || operando == null) return ERROR;
        return BOOL.equals(operando) ? BOOL : ERROR;
    }

    /**
     * Tipo resultante del operador unario negativo (-expr).
     * Preserva el tipo del operando: -3 sigue siendo int, -3.0 sigue siendo float.
     * Solo aplica a tipos numéricos.
     */
    public static String inferirNegativo(String operando) {
        if (ERROR.equals(operando) || operando == null) return ERROR;
        return esNumerico(operando) ? operando : ERROR;
    }

    // ── Predicados auxiliares ───────────────────────────────────────────────

    /** true si el tipo puede participar en operaciones aritméticas o relacionales. */
    public static boolean esNumerico(String tipo) {
        return INT.equals(tipo) || FLOAT.equals(tipo) || DOUBLE.equals(tipo);
    }

    /** true si el tipo es uno de los tipos primitivos del lenguaje (no ERROR, no null). */
    public static boolean esValido(String tipo) {
        return INT.equals(tipo) || FLOAT.equals(tipo)  || DOUBLE.equals(tipo) ||
               CHAR.equals(tipo) || STRING.equals(tipo) || BOOL.equals(tipo)  ||
               VOID.equals(tipo);
    }

    // ── Fábricas de mensajes de error ──────────────────────────────────────

    public static String msgIncompatible(String hacia, String desde) {
        return "no se puede asignar tipo '" + desde + "' a variable de tipo '" + hacia + "'";
    }

    public static String msgOperadorInvalido(String operador, String izq, String der) {
        return "el operador '" + operador + "' no puede aplicarse a tipos '"
               + izq + "' y '" + der + "'";
    }

    public static String msgUnarioInvalido(String operador, String tipo) {
        return "el operador '" + operador + "' no puede aplicarse al tipo '" + tipo + "'";
    }
}
