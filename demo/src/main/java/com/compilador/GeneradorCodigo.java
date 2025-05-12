package com.compilador;

import java.util.ArrayList;
import java.util.List;

/**
 * Generador de código de tres direcciones
 */
public class GeneradorCodigo {
    
    private List<String> codigo;
    private int tempCounter;
    private int labelCounter;
    
    public GeneradorCodigo() {
        this.codigo = new ArrayList<>();
        this.tempCounter = 0;
        this.labelCounter = 0;
    }
    
    /**
     * Genera un nuevo nombre de variable temporal
     */
    public String newTemp() {
        return "t" + (tempCounter++);
    }
    
    /**
     * Genera una nueva etiqueta
     */
    public String newLabel() {
        return "L" + (labelCounter++);
    }
    
    /**
     * Agrega una instrucción al código
     */
    public void append(String instruccion) {
        codigo.add(instruccion);
    }
    
    /**
     * Genera código para una operación binaria
     */
    public String genOperacionBinaria(String op, String left, String right) {
        String temp = newTemp();
        append(temp + " = " + left + " " + op + " " + right);
        return temp;
    }
    
    /**
     * Genera código para una operación unaria
     */
    public String genOperacionUnaria(String op, String expr) {
        String temp = newTemp();
        append(temp + " = " + op + " " + expr);
        return temp;
    }
    
    /**
     * Genera código para una asignación
     */
    public void genAsignacion(String id, String expr) {
        append(id + " = " + expr);
    }
    
    /**
     * Genera código para una etiqueta
     */
    public void genLabel(String label) {
        append(label + ":");
    }
    
    /**
     * Genera código para un salto condicional
     */
    public void genIfFalse(String condicion, String label) {
        append("if !" + condicion + " goto " + label);
    }
    
    /**
     * Genera código para un salto incondicional
     */
    public void genGoto(String label) {
        append("goto " + label);
    }
    
    /**
     * Genera código para una llamada a función
     */
    public String genLlamadaFuncion(String nombre, List<String> args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            append("param " + args.get(i));
        }
        String temp = newTemp();
        append(temp + " = call " + nombre + ", " + args.size());
        return temp;
    }
    
    /**
     * Genera código para un retorno
     */
    public void genReturn(String expr) {
        if (expr != null) {
            append("return " + expr);
        } else {
            append("return");
        }
    }
    
    /**
     * Obtiene el código generado
     */
    public List<String> getCodigo() {
        return codigo;
    }
    
    /**
     * Imprime el código generado
     */
    public void imprimirCodigo() {
        System.out.println("\n=== CÓDIGO DE TRES DIRECCIONES ===");
        for (int i = 0; i < codigo.size(); i++) {
            System.out.println(i + ": " + codigo.get(i));
        }
    }
}