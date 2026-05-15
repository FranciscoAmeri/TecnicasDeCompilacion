package com.compilador.semantico;

/**
 * Representa una entrada en la tabla de símbolos.
 *
 * En este mini compilador, un símbolo es una VARIABLE declarada.
 * La clase está diseñada para ser extensible: la enumeración Categoria
 * permite agregar funciones y parámetros en el futuro.
 *
 * Información que guarda cada símbolo:
 *   - nombre      : el identificador ("x", "contador", "activo")
 *   - tipo        : el tipo de dato ("int", "float", "bool", etc.)
 *   - categoria   : qué tipo de símbolo es (variable, función, etc.)
 *   - inicializado: si ya se le asignó un valor
 *   - linea/columna: dónde fue declarado (para mensajes de error)
 */
public class Symbol {

    // =========================================================
    //  Categorías de símbolos
    //  Actualmente solo VARIABLE está implementado.
    //  FUNCION y PARAMETRO son extensiones futuras.
    // =========================================================
    public enum Categoria {
        VARIABLE,
        FUNCION,    // para extensión futura (cuando se agreguen funciones a la gramática)
        PARAMETRO   // para extensión futura (parámetros de funciones)
    }

    private final String    nombre;
    private final String    tipo;
    private final Categoria categoria;
    private       boolean   inicializado; // puede cambiar después de la declaración
    private final int       linea;
    private final int       columna;

    public Symbol(String nombre, String tipo, Categoria categoria,
                  boolean inicializado, int linea, int columna) {
        this.nombre       = nombre;
        this.tipo         = tipo;
        this.categoria    = categoria;
        this.inicializado = inicializado;
        this.linea        = linea;
        this.columna      = columna;
    }

    // =========================================================
    //  Factory methods (constructores con nombre descriptivo)
    // =========================================================

    /** Crea un símbolo de tipo VARIABLE. */
    public static Symbol variable(String nombre, String tipo,
                                  boolean inicializado, int linea, int columna) {
        return new Symbol(nombre, tipo, Categoria.VARIABLE, inicializado, linea, columna);
    }

    /** Crea un símbolo de tipo FUNCION. El campo 'tipo' almacena el tipo de retorno. */
    public static Symbol funcion(String nombre, String tipoRetorno, int linea, int columna) {
        return new Symbol(nombre, tipoRetorno, Categoria.FUNCION, true, linea, columna);
    }

    /** Crea un símbolo de tipo PARAMETRO. Los parámetros se consideran siempre inicializados. */
    public static Symbol parametro(String nombre, String tipo, int linea, int columna) {
        return new Symbol(nombre, tipo, Categoria.PARAMETRO, true, linea, columna);
    }

    // =========================================================
    //  Getters / Setters
    // =========================================================

    public String    getNombre()      { return nombre;       }
    public String    getTipo()        { return tipo;         }
    public Categoria getCategoria()   { return categoria;    }
    public boolean   isInicializado() { return inicializado; }
    public int       getLinea()       { return linea;        }
    public int       getColumna()     { return columna;      }

    /** Marca el símbolo como inicializado (al hacer una asignación). */
    public void setInicializado(boolean inicializado) {
        this.inicializado = inicializado;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-8s %-12s %s [%d:%d]",
                categoria, tipo, nombre,
                inicializado ? "(inicializado)" : "(sin inicializar)",
                linea, columna);
    }
}
