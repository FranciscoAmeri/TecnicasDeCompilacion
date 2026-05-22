package com.compilador.semantico;

/**
 * Mensaje diagnóstico producido durante el análisis semántico.
 *
 * SemanticAnalyzer mantiene dos listas separadas: una para ERRORs y otra
 * para ADVERTENCIAs.  Usar un único tipo con un campo de severidad permite
 * que las dos listas compartan el mismo tipo y que toString() elija
 * automáticamente la etiqueta correcta al imprimir.
 *
 * Salida de toString():
 *   [Línea 13:4] Error semántico: no se puede asignar tipo 'string' …
 *   [Línea  7:8] Advertencia semántica: variable 'x' usada sin inicializar
 */
public class SemanticError {

    /**
     * ERROR      — problema que impide que el programa sea correcto.
     *              El compilador reporta el error y sigue analizando (no aborta)
     *              para poder mostrar todos los errores de una sola pasada.
     * ADVERTENCIA — condición sospechosa pero no inválida (variable sin inicializar).
     *              El programa puede compilar y ejecutar, pero podría tener bugs.
     */
    public enum Severidad { ERROR, ADVERTENCIA }

    private final int       linea;
    private final int       columna;
    private final String    mensaje;
    private final Severidad severidad;

    public SemanticError(int linea, int columna, String mensaje, Severidad severidad) {
        this.linea     = linea;
        this.columna   = columna;
        this.mensaje   = mensaje;
        this.severidad = severidad;
    }

    public int       getLinea()     { return linea;     }
    public int       getColumna()   { return columna;   }
    public String    getMensaje()   { return mensaje;   }
    public Severidad getSeveridad() { return severidad; }

    @Override
    public String toString() {
        String etiqueta = severidad == Severidad.ERROR
                          ? "Error semántico"
                          : "Advertencia semántica";
        return "[Línea " + linea + ":" + columna + "] " + etiqueta + ": " + mensaje;
    }
}
