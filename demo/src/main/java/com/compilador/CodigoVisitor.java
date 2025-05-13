package com.compilador;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * Visitor para generar código de tres direcciones
 */
public class CodigoVisitor extends MiLenguajeBaseVisitor<String> {
    
    private GeneradorCodigo generador;
    private TablaSimbolos tabla;
    
    public CodigoVisitor(TablaSimbolos tabla) {
        this.generador = new GeneradorCodigo();
        this.tabla = tabla;
    }
    
    /**
     * Obtiene el generador de código
     */
    public GeneradorCodigo getGenerador() {
        return generador;
    }
    
    @Override
    public String visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        for (MiLenguajeParser.SentenciaContext sentencia : ctx.sentencia()) {
            visit(sentencia);
        }
        return null;
    }
    
    @Override
    public String visitDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();
        
        // Generar etiqueta para la función
        generador.genLabel("func_" + nombreFuncion);
        
        // Cambiar ámbito
        String ambitoAnterior = tabla.getAmbito();
        tabla.setAmbito(nombreFuncion);
        
        // Visitar el bloque de código
        visit(ctx.bloque());
        
        // Si la función no tiene return explícito y es void, agregar uno
        if (ctx.tipo().getText().equals("void")) {
            generador.genReturn(null);
        }
        
        // Restaurar ámbito
        tabla.setAmbito(ambitoAnterior);
        
        return null;
    }
    
    @Override
    public String visitBloque(MiLenguajeParser.BloqueContext ctx) {
        for (MiLenguajeParser.SentenciaContext sentencia : ctx.sentencia()) {
            visit(sentencia);
        }
        return null;
    }
    
    @Override
    public String visitDeclaracionVariable(MiLenguajeParser.DeclaracionVariableContext ctx) {
        // No generamos código para declaraciones simples
        return null;
    }
    
    @Override
    public String visitAsignacion(MiLenguajeParser.AsignacionContext ctx) {
        String id = ctx.ID().getText();
        String exprResult = visit(ctx.expresion());
        generador.genAsignacion(id, exprResult);
        return null;
    }
    
    @Override
    public String visitRetorno(MiLenguajeParser.RetornoContext ctx) {
        if (ctx.expresion() != null) {
            String exprResult = visit(ctx.expresion());
            generador.genReturn(exprResult);
        } else {
            generador.genReturn(null);
        }
        return null;
    }
    
    @Override
    public String visitSentenciaIf(MiLenguajeParser.SentenciaIfContext ctx) {
        String condicion = visit(ctx.expresion());
        
        String labelElse = generador.newLabel();
        String labelFin = ctx.ELSE() != null ? generador.newLabel() : labelElse;
        
        // Salto condicional
        generador.genIfFalse(condicion, labelElse);
        
        // Bloque if
        visit(ctx.bloque(0));
        
        // Si hay else
        if (ctx.ELSE() != null) {
            generador.genGoto(labelFin);
            generador.genLabel(labelElse);
            visit(ctx.bloque(1));
        }
        
        generador.genLabel(labelFin);
        
        return null;
    }
    
    @Override
    public String visitExpBinaria(MiLenguajeParser.ExpBinariaContext ctx) {
        String left = visit(ctx.expresion(0));
        String right = visit(ctx.expresion(1));
        String op = ctx.operadorBinario().getText();
        
        return generador.genOperacionBinaria(op, left, right);
    }
    
    @Override
    public String visitExpNegacion(MiLenguajeParser.ExpNegacionContext ctx) {
        String expr = visit(ctx.expresion());
        return generador.genOperacionUnaria("!", expr);
    }
    
    @Override
    public String visitExpParentizada(MiLenguajeParser.ExpParentizadaContext ctx) {
        return visit(ctx.expresion());
    }
    
    @Override
    public String visitExpVariable(MiLenguajeParser.ExpVariableContext ctx) {
        return ctx.ID().getText();
    }
    
    @Override
    public String visitExpEntero(MiLenguajeParser.ExpEnteroContext ctx) {
        return ctx.INTEGER().getText();
    }
    
    @Override
    public String visitExpDecimal(MiLenguajeParser.ExpDecimalContext ctx) {
        return ctx.DECIMAL().getText();
    }
    
    @Override
    public String visitExpCaracter(MiLenguajeParser.ExpCaracterContext ctx) {
        return ctx.CHARACTER().getText();
    }
    
    @Override
    public String visitExpFuncion(MiLenguajeParser.ExpFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        List<String> args = new ArrayList<>();
        
        if (ctx.argumentos() != null) {
            for (MiLenguajeParser.ExpresionContext expr : ctx.argumentos().expresion()) {
                args.add(visit(expr));
            }
        }
        
        return generador.genLlamadaFuncion(nombre, args);
    }
}