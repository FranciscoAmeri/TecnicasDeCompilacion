package com.compilador.semantico;

/**
 * Un entrada en la tabla de símbolos: representa una variable, función o parámetro
 * junto con su tipo, categoría, estado de inicialización y posición en el fuente.
 *
 * El campo {@code inicializado} distingue una declaración bare ("int x;") de una
 * con valor inicial ("int x = 10;").  Se cambia a true cuando el analizador visita
 * una asignación posterior ("x = 10;").
 */
public class Symbol {

    /**
     * Las tres clases de nombre que pueden aparecer en el lenguaje:
     *   VARIABLE  — declarada con un tipo base (int, float, …)
     *   FUNCION   — declarada con tipo de retorno y lista de parámetros
     *   PARAMETRO — variable implícita creada al entrar a una función;
     *               siempre se considera inicializada porque recibe valor al llamarla
     */
    public enum Categoria {
        VARIABLE,
        FUNCION,
        PARAMETRO
    }

    private final String    nombre;
    private final String    tipo;
    private final Categoria categoria;
    private       boolean   inicializado; // mutable: puede cambiar de false → true
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

    // ── Fábricas estáticas ─────────────────────────────────────────────────
    // Evitan repetir Categoria.X en cada lugar que crea un símbolo y dejan
    // explícito qué valor tiene inicializado para cada categoría.

    /** Variable: puede nacer sin inicializar (int x;) o con valor (int x = 10;). */
    public static Symbol variable(String nombre, String tipo, boolean inicializado, int linea, int columna) {
        return new Symbol(nombre, tipo, Categoria.VARIABLE, inicializado, linea, columna);
    }

    /** Función: siempre "inicializada" — el cuerpo es su definición. */
    public static Symbol funcion(String nombre, String tipoRetorno, int linea, int columna) {
        return new Symbol(nombre, tipoRetorno, Categoria.FUNCION, true, linea, columna);
    }

    /** Parámetro: siempre "inicializado" — recibe valor en cada llamada. */
    public static Symbol parametro(String nombre, String tipo, int linea, int columna) {
        return new Symbol(nombre, tipo, Categoria.PARAMETRO, true, linea, columna);
    }

    public String    getNombre()      { return nombre;       }
    public String    getTipo()        { return tipo;         }
    public Categoria getCategoria()   { return categoria;    }
    public boolean   isInicializado() { return inicializado; }
    public int       getLinea()       { return linea;        }
    public int       getColumna()     { return columna;      }

    /** Llamado por SemanticAnalyzer cuando visita una asignación a esta variable. */
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
