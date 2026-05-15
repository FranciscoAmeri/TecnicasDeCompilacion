package com.compilador.semantico;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa un ÁMBITO (scope) en el programa.
 *
 * ¿Qué es un scope?
 *   Un scope define dónde son visibles las variables.
 *   Cada par de llaves { } crea un nuevo scope.
 *
 * Ejemplo:
 *   int x = 1;           // scope global
 *   {
 *       int y = 2;       // scope local — y solo existe aquí
 *       cout << x;       // x es visible (busca en el padre)
 *   }
 *   cout << y;           // ERROR: y ya no existe
 *
 * Estructura de scopes:
 *   Los scopes forman un ÁRBOL donde cada scope tiene referencia
 *   a su padre. La búsqueda de un símbolo sube por el árbol:
 *
 *   [global]
 *     ├── [bloque_1]  (primer if)
 *     │     └── [bloque_2]  (bloque anidado)
 *     └── [bloque_3]  (while)
 */
public class Scope {

    private final String              nombre;   // nombre descriptivo: "global", "bloque_1", etc.
    private final Scope               padre;    // scope padre (null solo para el scope global)
    private final Map<String, Symbol> simbolos; // símbolos declarados en ESTE scope

    public Scope(String nombre, Scope padre) {
        this.nombre   = nombre;
        this.padre    = padre;
        this.simbolos = new LinkedHashMap<>(); // LinkedHashMap preserva el orden de inserción
    }

    // =========================================================
    //  Operaciones principales
    // =========================================================

    /**
     * Define un símbolo en ESTE scope.
     *
     * @return true  → el símbolo se definió correctamente
     * @return false → el símbolo ya estaba definido en este scope (redeclaración)
     */
    public boolean definir(Symbol simbolo) {
        if (simbolos.containsKey(simbolo.getNombre())) {
            return false; // redeclaración en el mismo scope → error semántico
        }
        simbolos.put(simbolo.getNombre(), simbolo);
        return true;
    }

    /**
     * Resuelve un símbolo: busca primero en este scope, luego sube al padre.
     *
     * Este es el mecanismo de LOOKUP en la cadena de scopes:
     *   1. ¿Está en este scope? → retorna el símbolo
     *   2. ¿Tiene padre? → busca recursivamente
     *   3. Llegó al global sin encontrar → retorna null (no declarado)
     *
     * @return el Symbol encontrado, o null si no existe en ningún scope
     */
    public Symbol resolver(String nombre) {
        Symbol s = simbolos.get(nombre);
        if (s != null) return s;
        if (padre != null) return padre.resolver(nombre);
        return null;
    }

    /**
     * Verifica si un nombre está definido SOLO en este scope (sin buscar en padres).
     * Usado para detectar redeclaraciones en el mismo ámbito.
     */
    public boolean estaDefinidoLocalmente(String nombre) {
        return simbolos.containsKey(nombre);
    }

    // =========================================================
    //  Getters
    // =========================================================

    public String              getNombre()   { return nombre;             }
    public Scope               getPadre()    { return padre;              }
    public Collection<Symbol>  getSimbolos() { return simbolos.values();  }
    public boolean             esGlobal()    { return padre == null;      }

    @Override
    public String toString() {
        return "Scope[" + nombre + "]" + (esGlobal() ? " (global)" : "");
    }
}
