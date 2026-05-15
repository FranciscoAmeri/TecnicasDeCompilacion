package com.compilador.semantico;

import com.compilador.MiLenguajeBaseVisitor;
import com.compilador.MiLenguajeParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador Semántico — FASE 3 del compilador.
 *
 * Recorre el árbol de parseo (generado en la Fase 2) y verifica
 * que el programa tenga SENTIDO, no solo estructura correcta.
 *
 * Ejemplo de lo que detecta (que el parser NO detecta):
 *   int x = 5;
 *   cout << y;   ← y no fue declarada (error semántico, NO sintáctico)
 *
 * Patrón de diseño:
 *   Extiende MiLenguajeBaseVisitor<String> donde el tipo genérico
 *   String representa el TIPO INFERIDO de cada expresión.
 *
 *   - Sentencias  → retornan null (no tienen tipo)
 *   - Expresiones → retornan el tipo ("int", "bool", "string", etc.)
 *
 * Los errores NO interrumpen el análisis: se acumulan en una lista
 * y se muestran todos al final.
 *
 * Validaciones implementadas:
 *   Variables:
 *     [V1] Variable declarada antes de usarse
 *     [V2] No redeclaración en el mismo scope
 *     [V3] Compatibilidad de tipos en asignaciones
 *     [V4] Aviso de variable usada sin inicializar
 *
 *   Tipos:
 *     [T1] Operandos numéricos en operaciones aritméticas
 *     [T2] Operandos booleanos en operaciones lógicas (&&, ||, !)
 *     [T3] Operandos numéricos en comparaciones relacionales
 *     [T4] Tipos compatibles en comparaciones de igualdad
 *
 *   Control de flujo:
 *     [C1] Condición booleana en if
 *     [C2] Condición booleana en while
 *
 *   Scopes:
 *     [S1] Cada bloque { } tiene su propio scope
 *     [S2] Las variables locales no son visibles fuera de su bloque
 */
public class SemanticAnalyzer extends MiLenguajeBaseVisitor<String> {

    private final SymbolTable        tabla;              // gestiona los scopes y símbolos
    private final List<SemanticError> errores;           // errores acumulados
    private       String              tipoRetornoActual; // tipo de retorno de la función activa (null = ámbito global)

    public SemanticAnalyzer() {
        this.tabla   = new SymbolTable();
        this.errores = new ArrayList<>();
    }

    // =========================================================
    //  Helpers para reportar errores
    // =========================================================

    private void error(Token token, String mensaje) {
        errores.add(new SemanticError(
            token.getLine(),
            token.getCharPositionInLine(),
            mensaje
        ));
    }

    // =========================================================
    //  Acceso a resultados
    // =========================================================

    public List<SemanticError> getErrores()         { return errores; }
    public boolean             hayErrores()          { return !errores.isEmpty(); }
    public SymbolTable         getTablaSimbolos()    { return tabla; }

    // =========================================================
    //  SENTENCIAS
    // =========================================================

    @Override
    public String visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        // El scope global ya existe en SymbolTable.
        // Solo recorremos los hijos.
        return visitChildren(ctx);
    }

    /**
     * DECLARACIÓN DE VARIABLE
     * Ejemplo: int x = 5 + 3;
     *          float pi;
     *
     * Validaciones:
     *   [V2] La variable NO debe estar ya declarada en el scope actual.
     *   [V3] Si tiene valor inicial, el tipo debe ser compatible.
     */
    @Override
    public String visitDeclaracion(MiLenguajeParser.DeclaracionContext ctx) {
        String tipo   = ctx.tipo().getText();   // "int", "float", "string", ...
        String nombre = ctx.ID().getText();     // nombre de la variable
        Token  token  = ctx.ID().getSymbol();  // token del nombre (para errores)

        // [V2] Verificar que no esté ya declarada en el scope actual
        if (tabla.estaDeclaradoLocalmente(nombre)) {
            error(token, "variable '" + nombre + "' ya fue declarada en este ámbito.");
            return null;
        }

        boolean tieneValorInicial = (ctx.expresion() != null);

        // [V3] Si hay valor inicial, verificar compatibilidad de tipos
        if (tieneValorInicial) {
            String tipoExpr = visit(ctx.expresion());
            if (tipoExpr != null && !TypeSystem.ERROR.equals(tipoExpr)) {
                if (!TypeSystem.esCompatibleAsignacion(tipo, tipoExpr)) {
                    error(token,
                          TypeSystem.msgIncompatible(tipo, tipoExpr)
                          + " en la declaración de '" + nombre + "'.");
                }
            }
        }

        // Agregar el símbolo a la tabla de símbolos
        Symbol simbolo = Symbol.variable(nombre, tipo, tieneValorInicial,
                                         token.getLine(),
                                         token.getCharPositionInLine());
        tabla.definir(simbolo);
        return null;
    }

    /**
     * ASIGNACIÓN
     * Ejemplo: x = x + 1;
     *
     * Validaciones:
     *   [V1] La variable DEBE estar declarada (en este scope o en algún padre).
     *   [V3] El tipo de la expresión debe ser compatible con el tipo de la variable.
     */
    @Override
    public String visitAsignacion(MiLenguajeParser.AsignacionContext ctx) {
        String nombre = ctx.ID().getText();
        Token  token  = ctx.ID().getSymbol();

        // [V1] Buscar la variable en la cadena de scopes
        Symbol simbolo = tabla.resolver(nombre);

        if (simbolo == null) {
            error(token, "variable '" + nombre + "' no fue declarada.");
            visit(ctx.expresion()); // visitar igual para encontrar más errores
            return null;
        }

        // Evaluar el tipo de la expresión del lado derecho
        String tipoExpr = visit(ctx.expresion());

        // [V3] Verificar compatibilidad de tipos
        if (tipoExpr != null && !TypeSystem.ERROR.equals(tipoExpr)) {
            if (!TypeSystem.esCompatibleAsignacion(simbolo.getTipo(), tipoExpr)) {
                error(token,
                      TypeSystem.msgIncompatible(simbolo.getTipo(), tipoExpr)
                      + " al asignar a '" + nombre + "'.");
            }
        }

        // Marcar la variable como inicializada
        simbolo.setInicializado(true);
        return null;
    }

    /**
     * COUT
     * Ejemplo: cout << x;
     *
     * cout acepta cualquier tipo de expresión.
     * Solo verificamos que la expresión sea válida.
     */
    @Override
    public String visitSentenciaCout(MiLenguajeParser.SentenciaCoutContext ctx) {
        visit(ctx.expresion()); // visitar para detectar errores en la expresión
        return null;
    }

    /**
     * IF / IF-ELSE
     * Ejemplo: if (x > 0) { ... } else { ... }
     *
     * Validaciones:
     *   [C1] La condición DEBE ser de tipo bool.
     *   [S1] Cada bloque crea su propio scope (gestionado por visitBloque).
     */
    @Override
    public String visitSentenciaIf(MiLenguajeParser.SentenciaIfContext ctx) {
        // [C1] Verificar que la condición sea booleana
        String tipoCondicion = visit(ctx.expresion());
        Token  tokenIf       = ctx.IF().getSymbol();

        if (tipoCondicion != null && !TypeSystem.ERROR.equals(tipoCondicion)) {
            if (!TypeSystem.BOOL.equals(tipoCondicion)) {
                error(tokenIf,
                      "la condición del 'if' debe ser bool, pero es '" + tipoCondicion + "'. "
                      + "Sugerencia: usa una comparación como 'x > 0'.");
            }
        }

        // Visitar cada bloque (visitBloque maneja el scope)
        // ctx.bloque(0) → bloque del if
        // ctx.bloque(1) → bloque del else (puede no existir)
        for (MiLenguajeParser.BloqueContext bloque : ctx.bloque()) {
            visit(bloque);
        }

        return null;
    }

    /**
     * WHILE
     * Ejemplo: while (x < 100) { ... }
     *
     * Validaciones:
     *   [C2] La condición DEBE ser de tipo bool.
     *   [S1] El bloque crea su propio scope.
     */
    @Override
    public String visitSentenciaWhile(MiLenguajeParser.SentenciaWhileContext ctx) {
        // [C2] Verificar que la condición sea booleana
        String tipoCondicion = visit(ctx.expresion());
        Token  tokenWhile    = ctx.WHILE().getSymbol();

        if (tipoCondicion != null && !TypeSystem.ERROR.equals(tipoCondicion)) {
            if (!TypeSystem.BOOL.equals(tipoCondicion)) {
                error(tokenWhile,
                      "la condición del 'while' debe ser bool, pero es '" + tipoCondicion + "'. "
                      + "Sugerencia: usa una comparación como 'x < 100'.");
            }
        }

        visit(ctx.bloque());
        return null;
    }

    /**
     * BLOQUE { }
     * [S1] Crea un nuevo scope al entrar y lo elimina al salir.
     *
     * Esta es la implementación del manejo de scopes.
     * Cada bloque tiene sus propias variables, inaccesibles desde fuera.
     */
    @Override
    public String visitBloque(MiLenguajeParser.BloqueContext ctx) {
        tabla.entrarScope("bloque");   // crear un nuevo scope hijo
        visitChildren(ctx);            // analizar las sentencias del bloque
        tabla.salirScope();            // eliminar el scope (las variables locales desaparecen)
        return null;
    }

    /**
     * DECLARACIÓN DE FUNCIÓN
     * Ejemplo: int sumar(int a, int b) { return a + b; }
     *
     * Validaciones:
     *   [V2] La función NO debe estar ya declarada en el scope actual.
     *   [F1] Los parámetros se agregan al scope de la función.
     *   [F2] El cuerpo se analiza en ese scope (sin crear un scope extra).
     */
    @Override
    public String visitDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        String tipoRetorno = ctx.tipo().getText();
        String nombre      = ctx.ID().getText();
        Token  token       = ctx.ID().getSymbol();

        // [V2] No redeclaración en el mismo scope
        if (tabla.estaDeclaradoLocalmente(nombre)) {
            error(token, "función '" + nombre + "' ya fue declarada en este ámbito.");
            return null;
        }

        // Registrar la función en el scope actual antes de entrar al cuerpo
        tabla.definir(Symbol.funcion(nombre, tipoRetorno, token.getLine(), token.getCharPositionInLine()));

        // Entrar al scope de la función y agregar los parámetros
        tabla.entrarScope("funcion_" + nombre);

        if (ctx.listaParametros() != null) {
            for (MiLenguajeParser.ParametroContext param : ctx.listaParametros().parametro()) {
                String tipoParam  = param.tipo().getText();
                String nombreParam = param.ID().getText();
                Token  tokenParam  = param.ID().getSymbol();
                tabla.definir(Symbol.parametro(nombreParam, tipoParam,
                                               tokenParam.getLine(), tokenParam.getCharPositionInLine()));
            }
        }

        // Guardar el tipo de retorno anterior (soporta funciones anidadas en el futuro)
        String tipoAnterior   = tipoRetornoActual;
        tipoRetornoActual     = tipoRetorno;

        // Visitar el bloque sin crear un scope adicional: saltamos visitBloque
        // y visitamos directamente sus hijos (sentencias internas).
        visitChildren(ctx.bloque());

        tipoRetornoActual = tipoAnterior;
        tabla.salirScope();
        return null;
    }

    /**
     * SENTENCIA RETURN
     * Ejemplo: return a + b;  /  return;
     *
     * Validaciones:
     *   [F3] Solo válido dentro de una función.
     *   [F4] El tipo de la expresión debe ser compatible con el tipo de retorno.
     *   [F5] Una función void no puede retornar un valor.
     *   [F6] Una función no-void debe retornar un valor.
     */
    @Override
    public String visitSentenciaReturn(MiLenguajeParser.SentenciaReturnContext ctx) {
        Token tokenReturn = ctx.RETURN().getSymbol();

        // [F3] Solo dentro de una función
        if (tipoRetornoActual == null) {
            error(tokenReturn, "'return' usado fuera de una función.");
            return null;
        }

        boolean tieneValor = (ctx.expresion() != null);

        if (tieneValor) {
            String tipoExpr = visit(ctx.expresion());

            // [F5] void no puede retornar valor
            if (TypeSystem.VOID.equals(tipoRetornoActual)) {
                error(tokenReturn, "función 'void' no puede retornar un valor.");

            // [F4] Tipo compatible
            } else if (tipoExpr != null && !TypeSystem.ERROR.equals(tipoExpr)
                       && !TypeSystem.esCompatibleAsignacion(tipoRetornoActual, tipoExpr)) {
                error(tokenReturn,
                      "tipo de retorno '" + tipoExpr + "' no es compatible con el tipo declarado '"
                      + tipoRetornoActual + "'.");
            }
        } else {
            // [F6] Función no-void debe retornar un valor
            if (!TypeSystem.VOID.equals(tipoRetornoActual)) {
                error(tokenReturn,
                      "función de tipo '" + tipoRetornoActual + "' debe retornar un valor.");
            }
        }

        return null;
    }

    // =========================================================
    //  EXPRESIONES — devuelven el tipo inferido como String
    // =========================================================

    /**
     * OR lógico: a || b
     * [T2] Ambos operandos deben ser bool → resultado bool
     */
    @Override
    public String visitExprOr(MiLenguajeParser.ExprOrContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String res = TypeSystem.inferirLogico(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            error(ctx.OR().getSymbol(),
                  TypeSystem.msgOperadorInvalido("||", izq, der)
                  + " (se esperan dos bool).");
        }
        return res;
    }

    /**
     * AND lógico: a && b
     * [T2] Ambos operandos deben ser bool → resultado bool
     */
    @Override
    public String visitExprAnd(MiLenguajeParser.ExprAndContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String res = TypeSystem.inferirLogico(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            error(ctx.AND().getSymbol(),
                  TypeSystem.msgOperadorInvalido("&&", izq, der)
                  + " (se esperan dos bool).");
        }
        return res;
    }

    /**
     * Igualdad: a == b, a != b
     * [T4] Los tipos deben ser compatibles (mismo tipo o ambos numéricos) → bool
     */
    @Override
    public String visitExprIgualdad(MiLenguajeParser.ExprIgualdadContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        // Obtener el operador (== o !=) del hijo en posición 1
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirIgualdad(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken,
                  "no se pueden comparar '" + izq + "' y '" + der + "' con '"+ op + "'.");
        }
        return res;
    }

    /**
     * Relacional: a > b, a < b, a >= b, a <= b
     * [T3] Ambos operandos deben ser numéricos → bool
     */
    @Override
    public String visitExprRelacional(MiLenguajeParser.ExprRelacionalContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirRelacional(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken,
                  TypeSystem.msgOperadorInvalido(op, izq, der)
                  + " (se esperan tipos numéricos).");
        }
        return res;
    }

    /**
     * Suma / resta: a + b, a - b
     * [T1] Ambos operandos deben ser numéricos → tipo de mayor precisión
     */
    @Override
    public String visitExprAditiva(MiLenguajeParser.ExprAditivaContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirAritmetico(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken,
                  TypeSystem.msgOperadorInvalido(op, izq, der)
                  + " (se esperan tipos numéricos).");
        }
        return res;
    }

    /**
     * Multiplicación / división / módulo: a * b, a / b, a % b
     * [T1] Ambos operandos deben ser numéricos → tipo de mayor precisión
     */
    @Override
    public String visitExprMultiplicativa(MiLenguajeParser.ExprMultiplicativaContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirAritmetico(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken,
                  TypeSystem.msgOperadorInvalido(op, izq, der)
                  + " (se esperan tipos numéricos).");
        }
        return res;
    }

    /**
     * NOT lógico: !a
     * [T2] El operando debe ser bool → bool
     */
    @Override
    public String visitExprNot(MiLenguajeParser.ExprNotContext ctx) {
        String operando = visit(ctx.expresion());
        String res      = TypeSystem.inferirNot(operando);
        if (TypeSystem.ERROR.equals(res)) {
            error(ctx.NOT().getSymbol(),
                  TypeSystem.msgUnarioInvalido("!", operando)
                  + " (se espera bool).");
        }
        return res;
    }

    /**
     * Negativo unario: -x
     * [T1] El operando debe ser numérico → mismo tipo
     */
    @Override
    public String visitExprNegativo(MiLenguajeParser.ExprNegativoContext ctx) {
        String operando = visit(ctx.expresion());
        String res      = TypeSystem.inferirNegativo(operando);
        if (TypeSystem.ERROR.equals(res)) {
            error(ctx.RES().getSymbol(),
                  TypeSystem.msgUnarioInvalido("-", operando)
                  + " (se espera tipo numérico).");
        }
        return res;
    }

    /**
     * Expresión agrupada: (expr)
     * El tipo es el mismo que el de la expresión interna.
     */
    @Override
    public String visitExprAgrupada(MiLenguajeParser.ExprAgrupadaContext ctx) {
        return visit(ctx.expresion());
    }

    // =========================================================
    //  LITERALES — tipos fijos, sin validación adicional
    // =========================================================

    @Override public String visitExprEntero(MiLenguajeParser.ExprEnteroContext ctx)     { return TypeSystem.INT;    }
    @Override public String visitExprDecimal(MiLenguajeParser.ExprDecimalContext ctx)   { return TypeSystem.DOUBLE; }
    @Override public String visitExprCaracter(MiLenguajeParser.ExprCaracterContext ctx) { return TypeSystem.CHAR;   }
    @Override public String visitExprCadena(MiLenguajeParser.ExprCadenaContext ctx)     { return TypeSystem.STRING; }
    @Override public String visitExprVerdadero(MiLenguajeParser.ExprVerdaderoContext ctx) { return TypeSystem.BOOL; }
    @Override public String visitExprFalso(MiLenguajeParser.ExprFalsoContext ctx)         { return TypeSystem.BOOL; }

    /**
     * USO DE VARIABLE (identificador en una expresión)
     * Ejemplo: cout << x;  →  x es un identificador
     *
     * Validaciones:
     *   [V1] La variable DEBE estar declarada.
     *   [V4] Si no está inicializada, genera una advertencia.
     *
     * Retorna el tipo de la variable si existe, o ERROR si no existe.
     */
    @Override
    public String visitExprIdentificador(MiLenguajeParser.ExprIdentificadorContext ctx) {
        String nombre = ctx.ID().getText();
        Token  token  = ctx.ID().getSymbol();

        // [V1] Buscar en todos los scopes
        Symbol simbolo = tabla.resolver(nombre);

        if (simbolo == null) {
            error(token, "variable '" + nombre + "' no fue declarada.");
            return TypeSystem.ERROR; // ERROR evita errores en cascada
        }

        // [V4] Advertencia si la variable se usa sin haber sido inicializada
        if (!simbolo.isInicializado()) {
            error(token, "variable '" + nombre + "' podría no estar inicializada.");
            // No retornamos ERROR: el tipo se conoce aunque no esté inicializada
        }

        return simbolo.getTipo();
    }
}
