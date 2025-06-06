// Generated from com\compilador\MiLenguaje.g4 by ANTLR 4.9.3
package com.compilador;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MiLenguajeParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MiLenguajeVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#programa}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrograma(MiLenguajeParser.ProgramaContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#sentencia}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentencia(MiLenguajeParser.SentenciaContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#sentenciaIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaIf(MiLenguajeParser.SentenciaIfContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#bloque}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBloque(MiLenguajeParser.BloqueContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#declaracionFuncion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#parametros}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParametros(MiLenguajeParser.ParametrosContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#parametro}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParametro(MiLenguajeParser.ParametroContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#declaracionVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracionVariable(MiLenguajeParser.DeclaracionVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#asignacion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsignacion(MiLenguajeParser.AsignacionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#retorno}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRetorno(MiLenguajeParser.RetornoContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#tipo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTipo(MiLenguajeParser.TipoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expNegacion}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpNegacion(MiLenguajeParser.ExpNegacionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expDecimal}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpDecimal(MiLenguajeParser.ExpDecimalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expBinaria}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpBinaria(MiLenguajeParser.ExpBinariaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expParentizada}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpParentizada(MiLenguajeParser.ExpParentizadaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expCaracter}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpCaracter(MiLenguajeParser.ExpCaracterContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expEntero}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpEntero(MiLenguajeParser.ExpEnteroContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expVariable}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpVariable(MiLenguajeParser.ExpVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expFuncion}
	 * labeled alternative in {@link MiLenguajeParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpFuncion(MiLenguajeParser.ExpFuncionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#operadorBinario}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperadorBinario(MiLenguajeParser.OperadorBinarioContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiLenguajeParser#argumentos}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentos(MiLenguajeParser.ArgumentosContext ctx);
}