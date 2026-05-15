package com.compilador.semantico;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de Símbolos — gestiona la pila de scopes activos.
 *
 * Es la estructura central del análisis semántico.
 * Mantiene un "scope actual" que cambia a medida que el
 * analizador entra y sale de bloques { }.
 *
 * Operaciones básicas:
 *   entrarScope()  → al encontrar una llave de apertura  {
 *   salirScope()   → al encontrar una llave de cierre    }
 *   definir()      → al declarar una variable
 *   resolver()     → al usar una variable (en expresiones)
 *
 * Diagrama de la pila para este código:
 *
 *   int x = 1;           → define x en [global]
 *   if (x > 0) {         → entrarScope("bloque_1")
 *       int y = 2;       → define y en [bloque_1]
 *       if (y > 0) {     → entrarScope("bloque_2")
 *           int z = 3;   → define z en [bloque_2]
 *       }                → salirScope() → vuelve a [bloque_1]
 *   }                    → salirScope() → vuelve a [global]
 *
 * Estado de la pila en cada momento:
 *   Inicio:      [global]
 *   Dentro if:   [bloque_1] → [global]
 *   if anidado:  [bloque_2] → [bloque_1] → [global]
 *   Después if:  [global]
 */
public class SymbolTable {

    // Formato de cada fila y separador de la tabla de símbolos.
    // Anchos: Nombre=18, Tipo=8, Categoria=10, Estado=15, Pos=9
    private static final String FMT_FILA =
        "  | %-18s| %-8s| %-10s| %-15s| %-9s|%n";
    private static final String SEP_TABLA =
        "  +--------------------+----------+------------+-----------------+-----------+";

    private Scope        scopeActual;
    private int          contadorScopes; // para nombres únicos: bloque_1, bloque_2, etc.
    private final List<Scope> historial; // todos los scopes creados (para mostrar al final)

    public SymbolTable() {
        Scope global = new Scope("global", null);
        this.scopeActual    = global;
        this.contadorScopes = 0;
        this.historial      = new ArrayList<>();
        this.historial.add(global);
    }

    // =========================================================
    //  Gestión de scopes
    // =========================================================

    /**
     * Crea un nuevo scope hijo del actual y lo hace el scope activo.
     * Llamar al encontrar '{' en un bloque.
     *
     * @param contexto descripción del bloque ("if", "while", "bloque")
     */
    public void entrarScope(String contexto) {
        contadorScopes++;
        String nombre = contexto + "_" + contadorScopes;
        Scope nuevo = new Scope(nombre, scopeActual);
        historial.add(nuevo);
        scopeActual = nuevo;
    }

    /**
     * Sale del scope actual y vuelve al scope padre.
     * Llamar al encontrar '}' en un bloque.
     */
    public void salirScope() {
        if (scopeActual.getPadre() != null) {
            scopeActual = scopeActual.getPadre();
        }
    }

    // =========================================================
    //  Operaciones con símbolos
    // =========================================================

    /**
     * Declara un símbolo en el scope actual.
     *
     * @return true  si se declaró correctamente
     * @return false si ya existe en el scope actual (redeclaración)
     */
    public boolean definir(Symbol simbolo) {
        return scopeActual.definir(simbolo);
    }

    /**
     * Busca un símbolo en el scope actual y en todos sus padres.
     *
     * @return el Symbol encontrado, o null si no está declarado
     */
    public Symbol resolver(String nombre) {
        return scopeActual.resolver(nombre);
    }

    /**
     * Verifica si un nombre ya está declarado en el scope ACTUAL
     * (sin buscar en padres). Útil para detectar redeclaraciones.
     */
    public boolean estaDeclaradoLocalmente(String nombre) {
        return scopeActual.estaDefinidoLocalmente(nombre);
    }

    // =========================================================
    //  Inspección / Depuración
    // =========================================================

    public Scope getScopeActual() { return scopeActual; }

    /**
     * Imprime la tabla de símbolos completa con formato tabular.
     * Muestra cada scope con su encabezado, jerarquía de padres y
     * una tabla con nombre, tipo, categoría, estado y posición de cada símbolo.
     */
    public void imprimirTabla() {
        // SEP_TABLA sin los 2 espacios de sangria = ancho real de la tabla
        final int totalAncho = SEP_TABLA.length() - 2;
        final String linea   = "  " + "=".repeat(totalAncho);

        // Titulo centrado
        final String titulo = "TABLA DE SIMBOLOS";
        final int    pad    = (totalAncho - titulo.length()) / 2;

        System.out.println();
        System.out.println(linea);
        System.out.println("  " + " ".repeat(pad) + titulo);
        System.out.println(linea);

        // Encabezado de columnas
        String encabezado = String.format(FMT_FILA,
                "Nombre", "Tipo", "Categoria", "Estado", "Pos");

        int totalSimbolos      = 0;
        int scopesConVariables = 0;

        for (Scope scope : historial) {
            System.out.println();

            String padreInfo = scope.getPadre() != null
                               ? "  [padre: " + scope.getPadre().getNombre() + "]"
                               : "  [ambito raiz]";
            System.out.println("  Scope: " + scope.getNombre() + padreInfo);

            if (scope.getSimbolos().isEmpty()) {
                System.out.println("    (sin variables declaradas en este ambito)");
                continue;
            }

            scopesConVariables++;
            System.out.print(SEP_TABLA + "\n");
            System.out.print(encabezado);
            System.out.print(SEP_TABLA + "\n");

            for (Symbol s : scope.getSimbolos()) {
                String estado = s.isInicializado() ? "inicializado" : "no inicializado";
                System.out.printf(FMT_FILA,
                        s.getNombre(),
                        s.getTipo(),
                        s.getCategoria(),
                        estado,
                        s.getLinea() + ":" + s.getColumna());
                totalSimbolos++;
            }

            System.out.print(SEP_TABLA + "\n");
        }

        // Resumen final
        System.out.println();
        System.out.println(linea);
        System.out.printf("  Total: %d simbolo(s) en %d scope(s) (%d con variables)%n",
                totalSimbolos, historial.size(), scopesConVariables);
        System.out.println(linea);
    }
}
