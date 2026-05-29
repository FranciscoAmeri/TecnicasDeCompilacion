package com.compilador.codigoIntermedio;

import com.compilador.MiLenguajeBaseVisitor;
import com.compilador.MiLenguajeParser;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera código de tres direcciones (TAC) recorriendo el árbol de parseo.
 *
 * Extiende MiLenguajeBaseVisitor<String>: el tipo genérico String indica
 * que cada método visit devuelve un String. Ese String es el "lugar" donde
 * quedó el resultado de una expresión:
 *
 *   - Un temporal:   "t1", "t2", "t3"  (cuando se necesitó una variable auxiliar)
 *   - Una variable:  "x", "resultado"   (cuando la expresión ES una variable)
 *   - Un literal:    "10", "true", "'a'"  (cuando la expresión ES un valor constante)
 *
 * Los métodos de SENTENCIAS (if, while, declaracion, etc.) devuelven null
 * porque las sentencias no tienen valor; solo producen instrucciones TAC.
 *
 * Los métodos de EXPRESIONES siempre devuelven el lugar del resultado
 * para que el método que los llamó pueda usarlo como operando.
 *
 * Ejemplo de cómo se encadenan:
 *   Código fuente:  int z = suma(x, y);
 *   visitDeclaracion llama a visit(expresion) → visitExprLlamada
 *   visitExprLlamada emite:  param x / param y / t3 = call suma, 2
 *   visitExprLlamada devuelve "t3"
 *   visitDeclaracion emite:  z = t3
 */
public class CodigoTresDir extends MiLenguajeBaseVisitor<String> {

    // =========================================================================
    // ESTADO INTERNO
    // =========================================================================

    // Lista donde se acumulan todas las instrucciones TAC generadas.
    // emitir() agrega al final; imprimir() recorre esta lista para mostrarlas.
    private final List<Instruccion> instrucciones = new ArrayList<>();

    // Contador de temporales. Se incrementa cada vez que se necesita
    // una variable auxiliar nueva. Nunca se reutilizan ni se resetean.
    // Ejemplo: contTemp=0 → nuevaTemp() devuelve "t1" y deja contTemp=1
    private int contTemp = 0;

    // Contador de etiquetas. Igual que contTemp pero para los puntos de salto.
    // Ejemplo: contEtiq=2 → nuevaEtiqueta() devuelve "L3" y deja contEtiq=3
    private int contEtiq = 0;

    // ── Métodos auxiliares ────────────────────────────────────────────────────

    /** Genera el siguiente nombre de temporal: t1, t2, t3, ... */
    private String nuevaTemp() {
        return "t" + (++contTemp);
    }

    /**
     * Genera el siguiente nombre de etiqueta: L1, L2, L3, ...
     * Las etiquetas se crean ANTES de emitir las instrucciones que las rodean,
     * así el código que salta (if_false, goto) ya sabe el nombre a donde va.
     */
    private String nuevaEtiqueta() {
        return "L" + (++contEtiq);
    }

    /** Agrega una instrucción al final de la lista de instrucciones. */
    private void emitir(Instruccion i) {
        instrucciones.add(i);
    }

    /** Devuelve la lista completa de instrucciones generadas. */
    public List<Instruccion> getInstrucciones() {
        return instrucciones;
    }

    // ── Impresión ─────────────────────────────────────────────────────────────

    /**
     * Imprime todas las instrucciones numeradas en consola.
     *
     * Las ETIQUETAS (L1:, L2:, ...) no reciben número de línea porque son
     * marcadores de posición, no operaciones. El resto sí se numera.
     *
     * Salida de ejemplo:
     *   (  1)  x = 10
     *   (  2)  y = 3
     *   (  3)  begin_func suma
     *   (  4)  t1 = a + b
     *   (  5)  return t1
     *   (  6)  end_func suma
     *   (  7)  t2 = x > y
     *   (  8)  if_false t2 goto L1
     *   (  9)  print "es mayor x"
     *   L1:                          ← etiqueta: sin número
     *   ( 10)  param x
     */
    public void imprimir() {
        System.out.println("\n=== FASE 4: CÓDIGO DE TRES DIRECCIONES ===\n");
        int numero = 1;
        for (Instruccion inst : instrucciones) {
            if (inst.getTipo() == Instruccion.Tipo.ETIQUETA) {
                System.out.println("  " + inst);         // "  L1:"  sin número
            } else {
                System.out.printf("  (%3d)  %s%n", numero++, inst); // "  ( 1)  x = 10"
            }
        }
    }


    // =========================================================================
    // VISITOR — Programa
    // =========================================================================

    /**
     * Punto de entrada: visita todas las sentencias del programa de arriba a abajo.
     * No emite ninguna instrucción propia; solo delega en cada sentencia hija.
     */
    @Override
    public String visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        visitChildren(ctx);
        return null;
    }


    // =========================================================================
    // VISITOR — Sentencias
    // =========================================================================
    //
    // Los métodos de sentencias siempre devuelven null porque las sentencias
    // no tienen un "valor". Su único trabajo es emitir instrucciones TAC.

    /**
     * Declaración de variable: tipo ID (= expresion)? ;
     *
     * Si tiene inicializador, visita la expresión para obtener el lugar
     * donde quedó el valor y emite una asignación.
     * Si no tiene inicializador, no emite nada (el espacio en memoria
     * ya fue reservado conceptualmente por la tabla de símbolos).
     *
     * Ejemplo:  int x = 10;
     *   visit(expresion "10") → devuelve "10"  (visitExprEntero no crea temporal)
     *   emite: x = 10
     *
     * Ejemplo:  int z = suma(x, y);
     *   visit(expresion "suma(x,y)") → visitExprLlamada emite param/call, devuelve "t3"
     *   emite: z = t3
     *
     * Ejemplo:  int i;   (sin inicializador)
     *   no emite nada
     */
    @Override
    public String visitDeclaracion(MiLenguajeParser.DeclaracionContext ctx) {
        if (ctx.expresion() != null) {
            String lugar = visit(ctx.expresion()); // obtener el "lugar" del valor inicial
            emitir(Instruccion.asignar(ctx.ID().getText(), lugar));
        }
        return null;
    }

    /**
     * Asignación: ID = expresion ;
     *
     * Visita la expresión para obtener el lugar del resultado
     * y emite una asignación al identificador.
     *
     * Ejemplo:  i = i + 1;
     *   visit(expresion "i+1") → visitExprAditiva emite "t4 = i + 1", devuelve "t4"
     *   emite: i = t4
     *
     * Ejemplo:  resultado = z * 2;
     *   visit(expresion "z*2") → visitExprMultiplicativa emite "t6 = z * 2", devuelve "t6"
     *   emite: resultado = t6
     */
    @Override
    public String visitAsignacion(MiLenguajeParser.AsignacionContext ctx) {
        String lugar = visit(ctx.expresion());
        emitir(Instruccion.asignar(ctx.ID().getText(), lugar));
        return null;
    }

    /**
     * Sentencia cout: cout << expresion ;
     *
     * Visita la expresión y emite una instrucción IMPRIMIR con el lugar resultado.
     *
     * Ejemplo:  cout << z;
     *   visit(expresion "z") → visitExprIdentificador devuelve "z" directamente
     *   emite: print z
     *
     * Ejemplo:  cout << "es mayor x";
     *   visit(expresion) → visitExprCadena devuelve "\"es mayor x\"" directamente
     *   emite: print "es mayor x"
     */
    @Override
    public String visitSentenciaCout(MiLenguajeParser.SentenciaCoutContext ctx) {
        String lugar = visit(ctx.expresion());
        emitir(Instruccion.imprimir(lugar));
        return null;
    }

    /**
     * Sentencia if / if-else: if (expresion) bloque (else bloque)?
     *
     * Patrón TAC para IF SIN ELSE:
     *   (N)   tX = <condición>
     *   (N+1) if_false tX goto L_fin   ← si es false, saltamos el bloque
     *          ...bloque then...
     *   L_fin:
     *
     * Patrón TAC para IF CON ELSE:
     *   (N)   tX = <condición>
     *   (N+1) if_false tX goto L_else  ← si es false, ir al else
     *          ...bloque then...
     *   (M)   goto L_fin               ← saltar el else (ya ejecutamos el then)
     *   L_else:
     *          ...bloque else...
     *   L_fin:
     *
     * Nota: las etiquetas se crean ANTES de visitar los bloques para que
     * los gotos ya tengan el nombre correcto al emitirse.
     */
    @Override
    public String visitSentenciaIf(MiLenguajeParser.SentenciaIfContext ctx) {
        // Evaluar la condición primero
        String cond      = visit(ctx.expresion());
        boolean tieneElse = ctx.bloque().size() == 2; // 2 bloques = hay else
        String lFin      = nuevaEtiqueta();            // etiqueta de salida siempre se necesita

        if (tieneElse) {
            String lElse = nuevaEtiqueta();                    // etiqueta de inicio del else
            emitir(Instruccion.saltoCondFalso(cond, lElse));   // if_false → ir al else
            visit(ctx.bloque(0));                              // generar el bloque then
            emitir(Instruccion.saltoIncond(lFin));             // saltar el bloque else
            emitir(Instruccion.etiqueta(lElse));               // L_else:
            visit(ctx.bloque(1));                              // generar el bloque else
        } else {
            emitir(Instruccion.saltoCondFalso(cond, lFin));    // if_false → saltamos todo
            visit(ctx.bloque(0));                              // generar el bloque then
        }

        emitir(Instruccion.etiqueta(lFin)); // L_fin: punto de reunión después del if
        return null;
    }

    /**
     * Sentencia while: while (expresion) bloque
     *
     * Patrón TAC:
     *   L_inicio:                        ← volver aquí en cada iteración
     *   (N)   tX = <condición>
     *   (N+1) if_false tX goto L_fin     ← si es false, salir del ciclo
     *          ...cuerpo...
     *   (M)   goto L_inicio              ← volver a evaluar la condición
     *   L_fin:
     *
     * Ejemplo para  while (i < 5) { i = i + 1; }:
     *   L1:
     *   ( 5)  t3 = i < 5
     *   ( 6)  if_false t3 goto L2
     *   ( 7)  t4 = i + 1
     *   ( 8)  i = t4
     *   ( 9)  goto L1
     *   L2:
     */
    @Override
    public String visitSentenciaWhile(MiLenguajeParser.SentenciaWhileContext ctx) {
        String lInicio = nuevaEtiqueta(); // etiqueta de retorno al inicio del ciclo
        String lFin    = nuevaEtiqueta(); // etiqueta de salida del ciclo

        emitir(Instruccion.etiqueta(lInicio));             // L_inicio:
        String cond = visit(ctx.expresion());              // evaluar condición
        emitir(Instruccion.saltoCondFalso(cond, lFin));    // if_false → salir
        visit(ctx.bloque());                               // generar el cuerpo
        emitir(Instruccion.saltoIncond(lInicio));          // goto L_inicio
        emitir(Instruccion.etiqueta(lFin));                // L_fin:
        return null;
    }

    /**
     * Bloque de sentencias: { sentencia* }
     * Solo delega en cada sentencia interior. No emite instrucciones propias.
     */
    @Override
    public String visitBloque(MiLenguajeParser.BloqueContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Declaración de función: tipo ID (params)? bloque
     *
     * Emite begin_func, visita el cuerpo del bloque directamente con
     * visitChildren (NO con visitBloque) para evitar crear un scope extra,
     * y cierra con end_func.
     *
     * Ejemplo para  int suma(int a, int b) { return a + b; }:
     *   begin_func suma
     *   t1 = a + b
     *   return t1
     *   end_func suma
     *
     * Los parámetros (a, b) no generan instrucciones TAC propias: ya
     * están disponibles por nombre dentro del cuerpo de la función.
     */
    @Override
    public String visitDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        emitir(Instruccion.inicioFuncion(nombre));  // begin_func suma
        visitChildren(ctx.bloque());                // cuerpo (sin crear scope extra)
        emitir(Instruccion.finFuncion(nombre));     // end_func suma
        return null;
    }

    /**
     * Sentencia return: return expresion? ;
     *
     * Si tiene expresión, la visita para obtener el lugar del valor y
     * emite return con ese lugar. Si no tiene expresión (función void),
     * emite un return vacío.
     *
     * Ejemplo con valor:  return a + b;
     *   visit(expresion "a+b") → visitExprAditiva emite "t1 = a + b", devuelve "t1"
     *   emite: return t1
     *
     * Ejemplo sin valor:  return;
     *   emite: return
     */
    @Override
    public String visitSentenciaReturn(MiLenguajeParser.SentenciaReturnContext ctx) {
        if (ctx.expresion() != null) {
            String lugar = visit(ctx.expresion());
            emitir(Instruccion.retorno(lugar));
        } else {
            emitir(Instruccion.retornoVacio());
        }
        return null;
    }


    // =========================================================================
    // VISITOR — Expresiones binarias
    // =========================================================================
    //
    // Todas siguen el mismo patrón:
    //   1. Visitar el operando izquierdo → obtener su "lugar"
    //   2. Visitar el operando derecho  → obtener su "lugar"
    //   3. Crear un nuevo temporal tN
    //   4. Emitir:  tN = izq op der
    //   5. Devolver "tN" para que el método que llamó pueda usarlo

    /**
     * Expresión OR: expresion || expresion
     * Ejemplo:  a || b   →  emite "t1 = a || b",  devuelve "t1"
     */
    @Override
    public String visitExprOr(MiLenguajeParser.ExprOrContext ctx) {
        String izq = visit(ctx.expresion(0)); // lado izquierdo del ||
        String der = visit(ctx.expresion(1)); // lado derecho  del ||
        String t   = nuevaTemp();
        emitir(Instruccion.binaria(t, izq, "||", der));
        return t;
    }

    /**
     * Expresión AND: expresion && expresion
     * Ejemplo:  x > 0 && y > 0   →  (asumiendo que x>0 ya está en t1 e y>0 en t2)
     *           emite "t3 = t1 && t2",  devuelve "t3"
     */
    @Override
    public String visitExprAnd(MiLenguajeParser.ExprAndContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String t   = nuevaTemp();
        emitir(Instruccion.binaria(t, izq, "&&", der));
        return t;
    }

    /**
     * Expresión de igualdad: expresion == expresion  |  expresion != expresion
     * El operador (== o !=) se lee directamente del árbol con getChild(1).getText().
     * Ejemplo:  x == y   →  emite "t1 = x == y",  devuelve "t1"
     */
    @Override
    public String visitExprIgualdad(MiLenguajeParser.ExprIgualdadContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText(); // "==" o "!="
        String t   = nuevaTemp();
        emitir(Instruccion.binaria(t, izq, op, der));
        return t;
    }

    /**
     * Expresión relacional: expresion < | > | <= | >= expresion
     * El operador se lee del árbol igual que en igualdad.
     * Ejemplo:  x > y   →  emite "t2 = x > y",  devuelve "t2"
     * Ejemplo:  i < 5   →  emite "t3 = i < 5",  devuelve "t3"
     */
    @Override
    public String visitExprRelacional(MiLenguajeParser.ExprRelacionalContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText(); // "<", ">", "<=", ">="
        String t   = nuevaTemp();
        emitir(Instruccion.binaria(t, izq, op, der));
        return t;
    }

    /**
     * Expresión aditiva: expresion + expresion  |  expresion - expresion
     * Ejemplo:  a + b   →  emite "t1 = a + b",  devuelve "t1"
     * Ejemplo:  i + 1   →  emite "t4 = i + 1",  devuelve "t4"
     */
    @Override
    public String visitExprAditiva(MiLenguajeParser.ExprAditivaContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText(); // "+" o "-"
        String t   = nuevaTemp();
        emitir(Instruccion.binaria(t, izq, op, der));
        return t;
    }

    /**
     * Expresión multiplicativa: expresion * | / | % expresion
     * Ejemplo:  z * 2   →  emite "t6 = z * 2",  devuelve "t6"
     * Ejemplo:  n % 2   →  emite "t1 = n % 2",  devuelve "t1"
     */
    @Override
    public String visitExprMultiplicativa(MiLenguajeParser.ExprMultiplicativaContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText(); // "*", "/" o "%"
        String t   = nuevaTemp();
        emitir(Instruccion.binaria(t, izq, op, der));
        return t;
    }


    // =========================================================================
    // VISITOR — Expresiones unarias
    // =========================================================================

    /**
     * Negación lógica: !expresion
     * Ejemplo:  !activo   →  emite "t1 = !activo",  devuelve "t1"
     */
    @Override
    public String visitExprNot(MiLenguajeParser.ExprNotContext ctx) {
        String operando = visit(ctx.expresion());
        String t        = nuevaTemp();
        emitir(Instruccion.unaria(t, "!", operando));
        return t;
    }

    /**
     * Negación numérica: -expresion
     * Ejemplo:  -x   →  emite "t1 = -x",  devuelve "t1"
     */
    @Override
    public String visitExprNegativo(MiLenguajeParser.ExprNegativoContext ctx) {
        String operando = visit(ctx.expresion());
        String t        = nuevaTemp();
        emitir(Instruccion.unaria(t, "-", operando));
        return t;
    }

    /**
     * Expresión agrupada: (expresion)
     * Los paréntesis no generan ninguna instrucción: solo cambian la precedencia,
     * y el parser ya los resolvió al construir el árbol. Se delega directamente al hijo.
     * Ejemplo:  (a + b)   →  el resultado es el mismo que si fuera  a + b
     */
    @Override
    public String visitExprAgrupada(MiLenguajeParser.ExprAgrupadaContext ctx) {
        return visit(ctx.expresion()); // transparente: devuelve lo que devuelva el hijo
    }


    // =========================================================================
    // VISITOR — Literales
    // =========================================================================
    //
    // Los literales NO crean un temporal. Devuelven su texto directamente.
    // Esto evita instrucciones redundantes como:
    //   t1 = 10       ← innecesario
    //   x = t1
    // En su lugar se genera simplemente:
    //   x = 10

    /** Número entero. Ejemplo: 10  →  devuelve "10" */
    @Override
    public String visitExprEntero(MiLenguajeParser.ExprEnteroContext ctx) {
        return ctx.INTEGER().getText();  // "10", "3", "0", ...
    }

    /** Número decimal. Ejemplo: 3.14  →  devuelve "3.14" */
    @Override
    public String visitExprDecimal(MiLenguajeParser.ExprDecimalContext ctx) {
        return ctx.DECIMAL().getText();  // "3.14", "0.5", ...
    }

    /** Carácter. Ejemplo: 'a'  →  devuelve "'a'" (con comillas simples) */
    @Override
    public String visitExprCaracter(MiLenguajeParser.ExprCaracterContext ctx) {
        return ctx.CHARACTER().getText();  // "'a'", "'z'", ...
    }

    /** Cadena de texto. Ejemplo: "hola"  →  devuelve "\"hola\"" (con comillas dobles) */
    @Override
    public String visitExprCadena(MiLenguajeParser.ExprCadenaContext ctx) {
        return ctx.CADENA().getText();  // "\"es mayor x\"", "\"hola\"", ...
    }

    /** Literal true  →  devuelve "true" */
    @Override
    public String visitExprVerdadero(MiLenguajeParser.ExprVerdaderoContext ctx) {
        return "true";
    }

    /** Literal false  →  devuelve "false" */
    @Override
    public String visitExprFalso(MiLenguajeParser.ExprFalsoContext ctx) {
        return "false";
    }


    // =========================================================================
    // VISITOR — Variables e identificadores
    // =========================================================================

    /**
     * Referencia a una variable: ID
     * Tampoco crea temporal: devuelve el nombre de la variable directamente.
     * Ejemplo:  z   →  devuelve "z"
     * Ejemplo:  resultado  →  devuelve "resultado"
     *
     * Esto hace que  cout << z;  genere  print z  (no  t1 = z / print t1).
     */
    @Override
    public String visitExprIdentificador(MiLenguajeParser.ExprIdentificadorContext ctx) {
        return ctx.ID().getText(); // nombre tal cual aparece en el código fuente
    }


    // =========================================================================
    // VISITOR — Llamadas a función
    // =========================================================================

    /**
     * Llamada a función: ID (expresion, expresion, ...)
     *
     * Patrón TAC:
     *   1. Por cada argumento: visitar la expresión → obtener lugar, emitir PARAM
     *   2. Emitir LLAMADA con el nombre de la función y la cantidad de argumentos
     *   3. Devolver el temporal donde quedó el valor de retorno
     *
     * Ejemplo para  suma(x, y):
     *   visit("x") → devuelve "x"     →  param x
     *   visit("y") → devuelve "y"     →  param y
     *   nuevaTemp() → "t3"
     *   emite:  t3 = call suma, 2
     *   devuelve "t3"
     *
     * Ejemplo con expresión como argumento  suma(a + 1, b):
     *   visit("a+1") → visitExprAditiva emite "t1 = a + 1", devuelve "t1"
     *   param t1
     *   visit("b")   → devuelve "b"
     *   param b
     *   t2 = call suma, 2
     */
    @Override
    public String visitExprLlamada(MiLenguajeParser.ExprLlamadaContext ctx) {
        List<MiLenguajeParser.ExpresionContext> args = ctx.expresion();

        // Emitir un PARAM por cada argumento en el orden en que aparecen
        for (MiLenguajeParser.ExpresionContext arg : args) {
            String lugar = visit(arg); // evaluar el argumento (puede crear temporales)
            emitir(Instruccion.param(lugar));
        }

        // Emitir el LLAMADA y guardar el valor de retorno en un nuevo temporal
        String t = nuevaTemp();
        emitir(Instruccion.llamada(t, ctx.ID().getText(), String.valueOf(args.size())));
        return t; // devolver el temporal para que el llamador pueda usarlo
    }
}
