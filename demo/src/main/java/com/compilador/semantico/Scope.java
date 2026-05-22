package com.compilador.semantico;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa un ámbito (scope): un espacio de nombres donde se declaran símbolos.
 *
 * Los scopes forman una cadena de padres:
 *
 *   global
 *     └── funcion_suma_1
 *           └── bloque_2
 *
 * Cada scope conoce a su padre pero no a sus hijos. La búsqueda de un nombre
 * siempre va hacia arriba (hijo → padre → abuelo…) nunca hacia abajo.
 */
public class Scope {

    private final String              nombre;
    private final Scope               padre;     // null solo en el scope global
    // LinkedHashMap: acceso O(1) por nombre Y orden de inserción garantizado,
    // así imprimirTabla() muestra los símbolos en el orden en que fueron declarados.
    private final Map<String, Symbol> simbolos;

    public Scope(String nombre, Scope padre) {
        this.nombre   = nombre;
        this.padre    = padre;
        this.simbolos = new LinkedHashMap<>();
    }

    /**
     * Agrega un símbolo a este scope.
     * Retorna false si el nombre ya existe localmente (redeclaración).
     *
     * Intencionalmente NO sube al padre: declarar "int x" dentro de un bloque
     * cuando ya existe "int x" en global está permitido (shadowing).
     * Solo es error redeclarar en el MISMO scope.
     */
    public boolean definir(Symbol simbolo) {
        if (simbolos.containsKey(simbolo.getNombre())) return false;
        simbolos.put(simbolo.getNombre(), simbolo);
        return true;
    }

    /**
     * Busca un nombre empezando en este scope y subiendo por la cadena de padres.
     *
     * Ejemplo con el programa:
     *   int x = 10;               ← declarado en "global"
     *   int suma(int a, int b) {
     *       return a + x;         ← busca "x" en "funcion_suma_1", no lo encuentra,
     *   }                            sube a "global", lo encuentra → ok
     *
     * Si ningún scope en la cadena lo tiene, retorna null → variable no declarada.
     */
    public Symbol resolver(String nombre) {
        Symbol s = simbolos.get(nombre);       // 1. buscar en este scope
        if (s != null) return s;
        if (padre != null) return padre.resolver(nombre);  // 2. subir al padre
        return null;                           // 3. llegamos al global y no está
    }

    /**
     * Busca el nombre SOLO en este scope, sin subir al padre.
     * Se usa para detectar redeclaraciones: está bien tener "int x" en global
     * y también "int x" dentro de un if, porque son scopes distintos.
     */
    public boolean estaDefinidoLocalmente(String nombre) {
        return simbolos.containsKey(nombre);
    }

    public String             getNombre()   { return nombre;            }
    public Scope              getPadre()    { return padre;             }
    public Collection<Symbol> getSimbolos() { return simbolos.values(); }
    public boolean            esGlobal()    { return padre == null;     }

    @Override
    public String toString() {
        return "Scope[" + nombre + "]" + (esGlobal() ? " (global)" : "");
    }
}
