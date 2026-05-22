package com.compilador.semantico;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la pila de scopes activos durante el análisis semántico.
 *
 * En todo momento solo hay UN scope activo (scopeActual), que es el más
 * interno. Al entrar a una función o bloque se apila un scope nuevo;
 * al salir se desapila volviendo al padre.
 *
 * Adicionalmente mantiene un historial de todos los scopes creados
 * (incluso los ya cerrados) para poder imprimir la tabla completa al final.
 *
 * Estado durante el análisis de:
 *   int x = 1;                 → scopeActual = "global"
 *   int f(int a) {             → scopeActual = "funcion_f_1"
 *       if (a > 0) {           → scopeActual = "bloque_2"
 *       }                      → scopeActual = "funcion_f_1"  (salirScope)
 *   }                          → scopeActual = "global"       (salirScope)
 */
public class SymbolTable {

    // Columnas de la tabla: Nombre(18) | Tipo(8) | Categoría(10) | Estado(15) | Pos(9)
    private static final String FMT_FILA =
        "  | %-18s| %-8s| %-10s| %-15s| %-9s|%n";
    private static final String SEP_TABLA =
        "  +--------------------+----------+------------+-----------------+-----------+";

    private Scope             scopeActual;    // scope más interno en este momento
    // Siempre crece, nunca se resetea: garantiza nombres únicos aunque dos
    // funciones distintas generen scopes con el mismo prefijo (ej. "bloque_2", "bloque_5").
    private int               contadorScopes;
    private final List<Scope> historial;      // todos los scopes, incluso los cerrados

    public SymbolTable() {
        // El scope global es la raíz: no tiene padre y nunca se cierra.
        Scope global = new Scope("global", null);
        this.scopeActual    = global;
        this.contadorScopes = 0;
        this.historial      = new ArrayList<>();
        this.historial.add(global);
    }

    /**
     * Abre un nuevo scope hijo del actual.
     * El nombre generado es "contexto_N" (ej. "funcion_suma_1", "bloque_2").
     * El contador garantiza que dos scopes con el mismo contexto tengan nombres distintos.
     */
    public void entrarScope(String contexto) {
        contadorScopes++;
        Scope nuevo = new Scope(contexto + "_" + contadorScopes, scopeActual);
        historial.add(nuevo);
        scopeActual = nuevo;
    }

    /**
     * Cierra el scope actual volviendo a su padre.
     * Los símbolos del scope cerrado dejan de ser visibles, pero el scope
     * permanece en el historial para que imprimirTabla() lo muestre.
     */
    public void salirScope() {
        // La guardia evita salir del scope global (que no tiene padre).
        // Sin ella, un '}' extra en el fuente podría causar NPE al subir.
        if (scopeActual.getPadre() != null) {
            scopeActual = scopeActual.getPadre();
        }
    }

    /**
     * Agrega el símbolo en el scope activo.
     * Retorna false si ya existe localmente (redeclaración en el mismo ámbito).
     */
    public boolean definir(Symbol simbolo) {
        return scopeActual.definir(simbolo);
    }

    /**
     * Busca el nombre desde el scope actual hacia arriba.
     * Delega en Scope.resolver(), que sube por la cadena de padres.
     * Retorna null si el nombre no existe en ningún scope visible.
     */
    public Symbol resolver(String nombre) {
        return scopeActual.resolver(nombre);
    }

    /**
     * Verifica si el nombre ya fue declarado en el scope actual (solo aquí,
     * sin subir al padre). Se usa para detectar redeclaraciones.
     */
    public boolean estaDeclaradoLocalmente(String nombre) {
        return scopeActual.estaDefinidoLocalmente(nombre);
    }

    public Scope getScopeActual() { return scopeActual; }

    /**
     * Imprime la tabla completa al final del análisis.
     *
     * Usa el historial (no la pila activa), por lo que muestra todos los scopes
     * incluso los que ya fueron cerrados (ej. el scope de una función cuyo '}'
     * ya fue procesado y que volvió al scope padre hace tiempo).
     */
    public void imprimirTabla() {
        final int totalAncho = SEP_TABLA.length() - 2;
        final String linea   = "  " + "=".repeat(totalAncho);

        final String titulo = "TABLA DE SIMBOLOS";
        final int    pad    = (totalAncho - titulo.length()) / 2;

        System.out.println();
        System.out.println(linea);
        System.out.println("  " + " ".repeat(pad) + titulo);
        System.out.println(linea);

        String encabezado = String.format(FMT_FILA,
                "Nombre", "Tipo", "Categoria", "Estado", "Pos");

        int totalSimbolos      = 0;
        int scopesConVariables = 0;

        // Recorre el historial en orden de creación: global primero, luego los anidados.
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
                        s.getNombre(), s.getTipo(), s.getCategoria(),
                        estado, s.getLinea() + ":" + s.getColumna());
                totalSimbolos++;
            }

            System.out.print(SEP_TABLA + "\n");
        }

        System.out.println();
        System.out.println(linea);
        System.out.printf("  Total: %d simbolo(s) en %d scope(s) (%d con variables)%n",
                totalSimbolos, historial.size(), scopesConVariables);
        System.out.println(linea);
    }
}
