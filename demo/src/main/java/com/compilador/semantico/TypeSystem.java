package com.compilador.semantico;

/**
 * Sistema de Tipos del mini lenguaje.
 *
 * Centraliza TODAS las reglas de compatibilidad entre tipos.
 * Es la única clase que decide si una operación de tipos es válida.
 *
 * Tipos disponibles:
 *   int    → números enteros:          int x = 5;
 *   float  → números decimales:        float pi = 3.14;
 *   double → decimales alta precisión: double d = 3.14159;
 *   char   → un carácter:              char c = 'A';
 *   string → cadena de texto:          string s = "hola";
 *   bool   → booleano:                 bool b = true;
 *   void   → sin valor (futuras funciones)
 *
 * Jerarquía numérica (de menor a mayor precisión):
 *   int  <  float  <  double
 *
 * Regla de promoción aritmética:
 *   int + float   = float
 *   int + double  = double
 *   float + double = double
 */
public class TypeSystem {

    // Constantes para los nombres de tipos (evita errores de tipeo)
    public static final String INT    = "int";
    public static final String FLOAT  = "float";
    public static final String DOUBLE = "double";
    public static final String CHAR   = "char";
    public static final String STRING = "string";
    public static final String BOOL   = "bool";
    public static final String VOID   = "void";

    // Tipo especial para indicar un error ya reportado.
    // Cuando una expresión tiene tipo ERROR, no se genera un segundo error.
    public static final String ERROR  = "ERROR";

    // =========================================================
    //  COMPATIBILIDAD DE ASIGNACIÓN
    // =========================================================

    /**
     * ¿Se puede asignar un valor de tipo 'desde' a una variable de tipo 'hacia'?
     *
     * Tabla de compatibilidad:
     *   hacia=int    ← desde: int
     *   hacia=float  ← desde: int, float, double (cualquier numérico)
     *   hacia=double ← desde: int, float, double (cualquier numérico)
     *   hacia=char   ← desde: char
     *   hacia=string ← desde: string
     *   hacia=bool   ← desde: bool
     *
     * Si alguno es ERROR, se retorna true para no generar un doble error.
     */
    public static boolean esCompatibleAsignacion(String hacia, String desde) {
        if (hacia == null || desde == null)   return false;
        if (ERROR.equals(hacia) || ERROR.equals(desde)) return true; // no doble error

        if (hacia.equals(desde)) return true; // mismo tipo → siempre válido

        // Ampliación numérica implícita
        if (DOUBLE.equals(hacia) && (INT.equals(desde) || FLOAT.equals(desde))) return true;
        if (FLOAT.equals(hacia)  && (INT.equals(desde) || DOUBLE.equals(desde))) return true;

        return false;
    }

    // =========================================================
    //  INFERENCIA DE TIPOS EN EXPRESIONES
    // =========================================================

    /**
     * Tipo resultado de una operación aritmética (+, -, *, /, %).
     *
     * Regla: solo se permiten tipos numéricos.
     *        El resultado es el tipo de mayor precisión.
     *
     * Ejemplos:
     *   int + int    = int
     *   int + float  = float
     *   int + double = double
     *   int + string = ERROR   ← tipos incompatibles
     */
    public static String inferirAritmetico(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der)) return ERROR;
        if (izq == null || der == null) return ERROR;
        if (!esNumerico(izq) || !esNumerico(der)) return ERROR;

        // Regla de promoción: gana el tipo más preciso
        if (DOUBLE.equals(izq) || DOUBLE.equals(der)) return DOUBLE;
        if (FLOAT.equals(izq)  || FLOAT.equals(der))  return FLOAT;
        return INT;
    }

    /**
     * Tipo resultado de una comparación relacional (<, >, <=, >=).
     *
     * Solo se pueden comparar tipos numéricos entre sí.
     * El resultado siempre es bool.
     */
    public static String inferirRelacional(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der)) return ERROR;
        if (izq == null || der == null) return ERROR;
        if (!esNumerico(izq) || !esNumerico(der)) return ERROR;
        return BOOL;
    }

    /**
     * Tipo resultado de igualdad (==, !=).
     *
     * Se pueden comparar tipos iguales o tipos numéricos entre sí.
     * El resultado siempre es bool.
     *
     * Ejemplos:
     *   int == int     = bool
     *   int == float   = bool   ← numéricos son comparables
     *   string == string = bool
     *   int == string  = ERROR  ← incompatibles
     */
    public static String inferirIgualdad(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der)) return ERROR;
        if (izq == null || der == null) return ERROR;
        if (izq.equals(der)) return BOOL;
        if (esNumerico(izq) && esNumerico(der)) return BOOL;
        return ERROR;
    }

    /**
     * Tipo resultado de un operador lógico (&&, ||).
     *
     * Ambos operandos DEBEN ser bool.
     * El resultado es bool.
     */
    public static String inferirLogico(String izq, String der) {
        if (ERROR.equals(izq) || ERROR.equals(der)) return ERROR;
        if (izq == null || der == null) return ERROR;
        if (BOOL.equals(izq) && BOOL.equals(der)) return BOOL;
        return ERROR;
    }

    /**
     * Tipo resultado del operador NOT (!).
     *
     * El operando DEBE ser bool.
     * El resultado es bool.
     */
    public static String inferirNot(String operando) {
        if (ERROR.equals(operando) || operando == null) return ERROR;
        if (BOOL.equals(operando)) return BOOL;
        return ERROR;
    }

    /**
     * Tipo resultado del negativo unario (-x).
     *
     * El operando DEBE ser numérico.
     * El resultado es el mismo tipo del operando.
     */
    public static String inferirNegativo(String operando) {
        if (ERROR.equals(operando) || operando == null) return ERROR;
        if (esNumerico(operando)) return operando;
        return ERROR;
    }

    // =========================================================
    //  CONSULTAS DE TIPO
    // =========================================================

    /** ¿Es un tipo numérico (int, float o double)? */
    public static boolean esNumerico(String tipo) {
        return INT.equals(tipo) || FLOAT.equals(tipo) || DOUBLE.equals(tipo);
    }

    /** ¿Es un tipo válido del lenguaje? */
    public static boolean esValido(String tipo) {
        return INT.equals(tipo)    || FLOAT.equals(tipo)  || DOUBLE.equals(tipo) ||
               CHAR.equals(tipo)   || STRING.equals(tipo) || BOOL.equals(tipo)   ||
               VOID.equals(tipo);
    }

    // =========================================================
    //  MENSAJES DE ERROR
    // =========================================================

    /** Genera un mensaje descriptivo de incompatibilidad de tipos. */
    public static String msgIncompatible(String hacia, String desde) {
        return "no se puede asignar tipo '" + desde + "' a variable de tipo '" + hacia + "'";
    }

    /** Genera un mensaje para operadores con tipos incorrectos. */
    public static String msgOperadorInvalido(String operador, String izq, String der) {
        return "el operador '" + operador + "' no puede aplicarse a tipos '"
               + izq + "' y '" + der + "'";
    }

    /** Genera un mensaje para operadores unarios con tipo incorrecto. */
    public static String msgUnarioInvalido(String operador, String tipo) {
        return "el operador '" + operador + "' no puede aplicarse al tipo '" + tipo + "'";
    }
}
