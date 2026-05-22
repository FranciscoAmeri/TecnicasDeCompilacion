package com.compilador.semantico;

import com.compilador.MiLenguajeBaseVisitor;
import com.compilador.MiLenguajeParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Recorre el árbol de parseo generado por ANTLR y realiza el análisis semántico.
 *
 * Extiende MiLenguajeBaseVisitor<String>: el tipo String es el tipo de dato
 * que devuelve cada método visit. Las expresiones devuelven su tipo ("int",
 * "bool", etc.) y las sentencias devuelven null porque no tienen tipo.
 *
 * Los errores y advertencias se acumulan en listas separadas; el análisis
 * no se detiene al encontrar el primero.
 */
public class SemanticAnalyzer extends MiLenguajeBaseVisitor<String> {

    private final SymbolTable         tabla;
    private final List<SemanticError> errores;
    private final List<SemanticError> advertencias;

    // Tipo de retorno de la función que se está visitando en este momento.
    // null significa que estamos en el scope global (fuera de toda función).
    // Se guarda y restaura al entrar/salir de cada función para soportar
    // funciones anidadas.
    private String tipoRetornoActual;

    public SemanticAnalyzer() {
        this.tabla        = new SymbolTable();
        this.errores      = new ArrayList<>();
        this.advertencias = new ArrayList<>();
    }

    // ── Diagnósticos ─────────────────────────────────────────────────────────

    // error() detiene la compilación; warning() no.
    private void error(Token token, String mensaje) {
        errores.add(new SemanticError(
            token.getLine(),
            token.getCharPositionInLine(),
            mensaje,
            SemanticError.Severidad.ERROR
        ));
    }

    private void warning(Token token, String mensaje) {
        advertencias.add(new SemanticError(
            token.getLine(),
            token.getCharPositionInLine(),
            mensaje,
            SemanticError.Severidad.ADVERTENCIA
        ));
    }

    public List<SemanticError> getErrores()       { return errores;               }
    public List<SemanticError> getAdvertencias()  { return advertencias;          }
    public boolean             hayErrores()        { return !errores.isEmpty();    }
    public boolean             hayAdvertencias()   { return !advertencias.isEmpty(); }
    public SymbolTable         getTablaSimbolos()  { return tabla;                }

    // ── Programa ─────────────────────────────────────────────────────────────

    @Override
    public String visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        // Punto de entrada: visita todas las sentencias del programa de arriba a abajo.
        return visitChildren(ctx);
    }

    // ── Sentencias ───────────────────────────────────────────────────────────

    @Override
    public String visitDeclaracion(MiLenguajeParser.DeclaracionContext ctx) {
        String tipo   = ctx.tipo().getText();
        String nombre = ctx.ID().getText();
        Token  token  = ctx.ID().getSymbol();

        // Solo verificamos en el scope actual para detectar redeclaraciones.
        // Tener "int x" en global e "int x" dentro de un if es válido.
        if (tabla.estaDeclaradoLocalmente(nombre)) {
            error(token, "variable '" + nombre + "' ya fue declarada en este ámbito.");
            return null;
        }

        boolean tieneValorInicial = (ctx.expresion() != null);

        if (tieneValorInicial) {
            String tipoExpr = visit(ctx.expresion());
            // Si la expresión ya dio ERROR no generamos un segundo error por el tipo,
            // solo reportamos el error original que lo causó.
            if (tipoExpr != null && !TypeSystem.ERROR.equals(tipoExpr)) {
                if (!TypeSystem.esCompatibleAsignacion(tipo, tipoExpr)) {
                    error(token,
                          TypeSystem.msgIncompatible(tipo, tipoExpr)
                          + " en la declaración de '" + nombre + "'.");
                }
            }
        }

        // Se define el símbolo incluso si hubo error de tipo, para que usos
        // posteriores de esta variable no generen un segundo error de "no declarada".
        tabla.definir(Symbol.variable(nombre, tipo, tieneValorInicial,
                                      token.getLine(), token.getCharPositionInLine()));
        return null;
    }

    @Override
    public String visitAsignacion(MiLenguajeParser.AsignacionContext ctx) {
        String nombre  = ctx.ID().getText();
        Token  token   = ctx.ID().getSymbol();
        Symbol simbolo = tabla.resolver(nombre);

        if (simbolo == null) {
            error(token, "variable '" + nombre + "' no fue declarada.");
            // Visitamos la expresión de todas formas para detectar errores dentro de ella.
            visit(ctx.expresion());
            return null;
        }

        String tipoExpr = visit(ctx.expresion());

        if (tipoExpr != null && !TypeSystem.ERROR.equals(tipoExpr)) {
            if (!TypeSystem.esCompatibleAsignacion(simbolo.getTipo(), tipoExpr)) {
                error(token,
                      TypeSystem.msgIncompatible(simbolo.getTipo(), tipoExpr)
                      + " al asignar a '" + nombre + "'.");
            }
        }

        // Una asignación, incluso con tipo incorrecto, cuenta como intento de inicialización.
        simbolo.setInicializado(true);
        return null;
    }

    @Override
    public String visitSentenciaCout(MiLenguajeParser.SentenciaCoutContext ctx) {
        // Solo visitamos para detectar errores dentro de la expresión (ej. variable no declarada).
        visit(ctx.expresion());
        return null;
    }

    @Override
    public String visitSentenciaIf(MiLenguajeParser.SentenciaIfContext ctx) {
        String tipoCondicion = visit(ctx.expresion());
        Token  tokenIf       = ctx.IF().getSymbol();

        // Solo reportamos si la condición es un tipo concreto incorrecto.
        // Si ya es ERROR significa que la expresión ya reportó su propio error.
        if (tipoCondicion != null && !TypeSystem.ERROR.equals(tipoCondicion)
                && !TypeSystem.BOOL.equals(tipoCondicion)) {
            error(tokenIf,
                  "la condición del 'if' debe ser bool, pero es '" + tipoCondicion + "'. "
                  + "Sugerencia: usa una comparación como 'x > 0'.");
        }

        // ctx.bloque() puede tener 1 (solo if) o 2 elementos (if + else).
        for (MiLenguajeParser.BloqueContext bloque : ctx.bloque()) {
            visit(bloque);
        }
        return null;
    }

    @Override
    public String visitSentenciaWhile(MiLenguajeParser.SentenciaWhileContext ctx) {
        String tipoCondicion = visit(ctx.expresion());
        Token  tokenWhile    = ctx.WHILE().getSymbol();

        if (tipoCondicion != null && !TypeSystem.ERROR.equals(tipoCondicion)
                && !TypeSystem.BOOL.equals(tipoCondicion)) {
            error(tokenWhile,
                  "la condición del 'while' debe ser bool, pero es '" + tipoCondicion + "'. "
                  + "Sugerencia: usa una comparación como 'x < 100'.");
        }

        visit(ctx.bloque());
        return null;
    }

    @Override
    public String visitBloque(MiLenguajeParser.BloqueContext ctx) {
        // Cada par de llaves { } crea su propio ámbito.
        // Las variables declaradas adentro dejan de existir al salir.
        tabla.entrarScope("bloque");
        visitChildren(ctx);
        tabla.salirScope();
        return null;
    }

    @Override
    public String visitDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        String tipoRetorno = ctx.tipo().getText();
        String nombre      = ctx.ID().getText();
        Token  token       = ctx.ID().getSymbol();

        if (tabla.estaDeclaradoLocalmente(nombre)) {
            error(token, "función '" + nombre + "' ya fue declarada en este ámbito.");
            return null;
        }

        // La función se registra en el scope ANTES de abrir el scope interno,
        // así queda visible en el ámbito donde fue declarada (para llamadas recursivas).
        tabla.definir(Symbol.funcion(nombre, tipoRetorno, token.getLine(), token.getCharPositionInLine()));
        tabla.entrarScope("funcion_" + nombre);

        // Los parámetros se registran dentro del scope de la función.
        if (ctx.listaParametros() != null) {
            for (MiLenguajeParser.ParametroContext param : ctx.listaParametros().parametro()) {
                String tipoParam   = param.tipo().getText();
                String nombreParam = param.ID().getText();
                Token  tokenParam  = param.ID().getSymbol();
                tabla.definir(Symbol.parametro(nombreParam, tipoParam,
                                               tokenParam.getLine(), tokenParam.getCharPositionInLine()));
            }
        }

        // Guardamos el tipo de retorno anterior para soportar funciones anidadas:
        // al salir restauramos el contexto del nivel superior.
        String tipoAnterior = tipoRetornoActual;
        tipoRetornoActual   = tipoRetorno;

        // visitChildren sobre el bloque (no visit(bloque)) para evitar que
        // visitBloque() cree un scope extra. El scope de la función ya alcanza.
        visitChildren(ctx.bloque());

        tipoRetornoActual = tipoAnterior;
        tabla.salirScope();
        return null;
    }

    @Override
    public String visitSentenciaReturn(MiLenguajeParser.SentenciaReturnContext ctx) {
        Token tokenReturn = ctx.RETURN().getSymbol();

        // tipoRetornoActual == null significa que estamos en el scope global.
        if (tipoRetornoActual == null) {
            error(tokenReturn, "'return' usado fuera de una función.");
            return null;
        }

        boolean tieneValor = (ctx.expresion() != null);

        if (tieneValor) {
            String tipoExpr = visit(ctx.expresion());
            if (TypeSystem.VOID.equals(tipoRetornoActual)) {
                error(tokenReturn, "función 'void' no puede retornar un valor.");
            } else if (tipoExpr != null && !TypeSystem.ERROR.equals(tipoExpr)
                       && !TypeSystem.esCompatibleAsignacion(tipoRetornoActual, tipoExpr)) {
                error(tokenReturn,
                      "tipo de retorno '" + tipoExpr + "' no es compatible con el tipo declarado '"
                      + tipoRetornoActual + "'.");
            }
        } else {
            // return; sin valor solo es válido en funciones void.
            if (!TypeSystem.VOID.equals(tipoRetornoActual)) {
                error(tokenReturn,
                      "función de tipo '" + tipoRetornoActual + "' debe retornar un valor.");
            }
        }

        return null;
    }

    // ── Expresiones ──────────────────────────────────────────────────────────
    //
    // Patrón común: visitar los dos operandos primero (para detectar errores
    // en ellos), luego preguntar a TypeSystem cuál es el tipo resultante.
    // Si TypeSystem devuelve ERROR, reportar el error con el token del operador.

    @Override
    public String visitExprOr(MiLenguajeParser.ExprOrContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String res = TypeSystem.inferirLogico(izq, der);
        if (TypeSystem.ERROR.equals(res))
            error(ctx.OR().getSymbol(),
                  TypeSystem.msgOperadorInvalido("||", izq, der) + " (se esperan dos bool).");
        return res;
    }

    @Override
    public String visitExprAnd(MiLenguajeParser.ExprAndContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String res = TypeSystem.inferirLogico(izq, der);
        if (TypeSystem.ERROR.equals(res))
            error(ctx.AND().getSymbol(),
                  TypeSystem.msgOperadorInvalido("&&", izq, der) + " (se esperan dos bool).");
        return res;
    }

    @Override
    public String visitExprIgualdad(MiLenguajeParser.ExprIgualdadContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirIgualdad(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken, "no se pueden comparar '" + izq + "' y '" + der + "' con '" + op + "'.");
        }
        return res;
    }

    @Override
    public String visitExprRelacional(MiLenguajeParser.ExprRelacionalContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirRelacional(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken, TypeSystem.msgOperadorInvalido(op, izq, der) + " (se esperan tipos numéricos).");
        }
        return res;
    }

    @Override
    public String visitExprAditiva(MiLenguajeParser.ExprAditivaContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirAritmetico(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken, TypeSystem.msgOperadorInvalido(op, izq, der) + " (se esperan tipos numéricos).");
        }
        return res;
    }

    @Override
    public String visitExprMultiplicativa(MiLenguajeParser.ExprMultiplicativaContext ctx) {
        String izq = visit(ctx.expresion(0));
        String der = visit(ctx.expresion(1));
        String op  = ctx.getChild(1).getText();
        String res = TypeSystem.inferirAritmetico(izq, der);
        if (TypeSystem.ERROR.equals(res)) {
            Token opToken = ((TerminalNode) ctx.getChild(1)).getSymbol();
            error(opToken, TypeSystem.msgOperadorInvalido(op, izq, der) + " (se esperan tipos numéricos).");
        }
        return res;
    }

    @Override
    public String visitExprNot(MiLenguajeParser.ExprNotContext ctx) {
        String operando = visit(ctx.expresion());
        String res      = TypeSystem.inferirNot(operando);
        if (TypeSystem.ERROR.equals(res))
            error(ctx.NOT().getSymbol(),
                  TypeSystem.msgUnarioInvalido("!", operando) + " (se espera bool).");
        return res;
    }

    @Override
    public String visitExprNegativo(MiLenguajeParser.ExprNegativoContext ctx) {
        String operando = visit(ctx.expresion());
        String res      = TypeSystem.inferirNegativo(operando);
        if (TypeSystem.ERROR.equals(res))
            error(ctx.RES().getSymbol(),
                  TypeSystem.msgUnarioInvalido("-", operando) + " (se espera tipo numérico).");
        return res;
    }

    @Override
    public String visitExprAgrupada(MiLenguajeParser.ExprAgrupadaContext ctx) {
        // Los paréntesis no cambian el tipo, solo la precedencia (ya resuelta por el parser).
        return visit(ctx.expresion());
    }

    // Literales: su tipo es conocido estáticamente, no hay que analizar nada.
    @Override public String visitExprEntero(MiLenguajeParser.ExprEnteroContext ctx)       { return TypeSystem.INT;    }
    @Override public String visitExprDecimal(MiLenguajeParser.ExprDecimalContext ctx)     { return TypeSystem.DOUBLE; }
    @Override public String visitExprCaracter(MiLenguajeParser.ExprCaracterContext ctx)   { return TypeSystem.CHAR;   }
    @Override public String visitExprCadena(MiLenguajeParser.ExprCadenaContext ctx)       { return TypeSystem.STRING; }
    @Override public String visitExprVerdadero(MiLenguajeParser.ExprVerdaderoContext ctx) { return TypeSystem.BOOL;   }
    @Override public String visitExprFalso(MiLenguajeParser.ExprFalsoContext ctx)         { return TypeSystem.BOOL;   }

    @Override
    public String visitExprLlamada(MiLenguajeParser.ExprLlamadaContext ctx) {
        String nombre  = ctx.ID().getText();
        Token  token   = ctx.ID().getSymbol();
        Symbol simbolo = tabla.resolver(nombre);

        if (simbolo == null) {
            error(token, "función '" + nombre + "' no fue declarada.");
            return TypeSystem.ERROR;
        }

        // Distinguimos el caso donde el nombre existe pero es una variable,
        // no una función (ej. el usuario escribe x(3) siendo x una variable).
        if (simbolo.getCategoria() != Symbol.Categoria.FUNCION) {
            error(token, "'" + nombre + "' no es una función, es una "
                         + simbolo.getCategoria().toString().toLowerCase() + ".");
            return TypeSystem.ERROR;
        }

        // Visitamos los argumentos para detectar errores dentro de ellos,
        // aunque no validamos cantidad ni tipos contra la firma de la función.
        for (MiLenguajeParser.ExpresionContext arg : ctx.expresion()) {
            visit(arg);
        }

        // El tipo de la llamada es el tipo de retorno declarado de la función.
        return simbolo.getTipo();
    }

    @Override
    public String visitExprIdentificador(MiLenguajeParser.ExprIdentificadorContext ctx) {
        String nombre  = ctx.ID().getText();
        Token  token   = ctx.ID().getSymbol();
        Symbol simbolo = tabla.resolver(nombre);

        // La búsqueda sube por todos los scopes padres hasta el global.
        // Si retorna null, el nombre no existe en ningún ámbito visible.
        if (simbolo == null) {
            error(token, "variable '" + nombre + "' no fue declarada.");
            return TypeSystem.ERROR;
        }

        // Advertencia en lugar de error: el programa puede seguir ejecutando,
        // pero el comportamiento sería impredecible (valor basura en memoria).
        if (!simbolo.isInicializado()) {
            warning(token, "variable '" + nombre + "' podría no estar inicializada.");
        }

        return simbolo.getTipo();
    }
}
