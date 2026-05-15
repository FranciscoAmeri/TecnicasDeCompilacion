package com.compilador.semantico;

/**
 * Representa un error semántico encontrado durante el análisis.
 *
 * A diferencia de los errores sintácticos (que detienen el parser),
 * los errores semánticos se ACUMULAN en una lista y se muestran todos
 * al final, permitiendo que el análisis continúe y detecte más errores.
 *
 * Formato de salida:
 *   [Línea 10:5] Error semántico: variable 'x' no declarada.
 */
public class SemanticError {

    private final int    linea;
    private final int    columna;
    private final String mensaje;

    public SemanticError(int linea, int columna, String mensaje) {
        this.linea   = linea;
        this.columna = columna;
        this.mensaje = mensaje;
    }

    public int    getLinea()   { return linea;   }
    public int    getColumna() { return columna; }
    public String getMensaje() { return mensaje; }

    @Override
    public String toString() {
        return "[Línea " + linea + ":" + columna + "] Error semántico: " + mensaje;
    }
}
