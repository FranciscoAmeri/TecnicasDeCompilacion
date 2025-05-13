package com.compilador;

import java.util.*;

/**
 * Optimizador de código de tres direcciones
 */
public class Optimizador {
    
    private List<String> codigo;
    
    public Optimizador(List<String> codigo) {
        this.codigo = new ArrayList<>(codigo);
    }
    
    /**
     * Realiza todas las optimizaciones disponibles
     */
    public List<String> optimizar() {
        eliminarCodigoMuerto();
        propagarConstantes();
        simplificarExpresiones();
        eliminarSentenciasRedundantes();
        return codigo;
    }
    
    /**
     * Elimina código muerto (código inalcanzable)
     */
    public void eliminarCodigoMuerto() {
        Set<Integer> lineasAlcanzables = new HashSet<>();
        Map<String, Integer> etiquetas = new HashMap<>();
        
        // Primero, identificar todas las etiquetas y sus líneas
        for (int i = 0; i < codigo.size(); i++) {
            String linea = codigo.get(i);
            if (linea.endsWith(":")) {
                String etiqueta = linea.substring(0, linea.length() - 1);
                etiquetas.put(etiqueta, i);
            }
        }
        
        // Marcar líneas alcanzables con un recorrido desde el inicio
        marcarLineasAlcanzables(0, lineasAlcanzables, etiquetas);
        
        // También asegurarnos de que main sea considerado como punto de entrada
        if (etiquetas.containsKey("func_main")) {
            marcarLineasAlcanzables(etiquetas.get("func_main"), lineasAlcanzables, etiquetas);
        }
        
        // Eliminar líneas no alcanzables
        List<String> codigoOptimizado = new ArrayList<>();
        for (int i = 0; i < codigo.size(); i++) {
            if (lineasAlcanzables.contains(i)) {
                codigoOptimizado.add(codigo.get(i));
            }
        }
        
        codigo = codigoOptimizado;
    }
    
    /**
     * Recursivamente marca líneas alcanzables
     */
    private void marcarLineasAlcanzables(int linea, Set<Integer> lineasAlcanzables, Map<String, Integer> etiquetas) {
        // Si ya visitamos esta línea, retornar
        if (linea >= codigo.size() || lineasAlcanzables.contains(linea)) {
            return;
        }
        
        lineasAlcanzables.add(linea);
        String instruccion = codigo.get(linea);
        
        // Si es un goto incondicional
        if (instruccion.startsWith("goto ")) {
            String etiqueta = instruccion.substring(5);
            if (etiquetas.containsKey(etiqueta)) {
                marcarLineasAlcanzables(etiquetas.get(etiqueta), lineasAlcanzables, etiquetas);
            }
            return; // No continúa con la siguiente instrucción
        }
        
        // Si es un goto condicional
        if (instruccion.startsWith("if ")) {
            String[] partes = instruccion.split(" goto ");
            if (partes.length == 2) {
                String etiqueta = partes[1];
                if (etiquetas.containsKey(etiqueta)) {
                    marcarLineasAlcanzables(etiquetas.get(etiqueta), lineasAlcanzables, etiquetas);
                }
            }
        }
        
        // Si es return, no continúa con la siguiente instrucción
        if (instruccion.equals("return") || instruccion.startsWith("return ")) {
            return;
        }
        
        // Continuar con la siguiente instrucción
        marcarLineasAlcanzables(linea + 1, lineasAlcanzables, etiquetas);
    }
    
    /**
     * Propaga constantes cuando es posible
     */
    public void propagarConstantes() {
        Map<String, String> constantValues = new HashMap<>();
        List<String> codigoOptimizado = new ArrayList<>();
        
        for (String linea : codigo) {
            // Buscar asignaciones de constantes
            if (linea.contains(" = ") && !linea.contains(" + ") && !linea.contains(" - ") && 
                !linea.contains(" * ") && !linea.contains(" / ") && !linea.contains(" % ") &&
                !linea.contains(" call ")) {
                
                String[] partes = linea.split(" = ");
                if (partes.length == 2) {
                    String destino = partes[0].trim();
                    String valor = partes[1].trim();
                    
                    // Si es un número o un carácter literal
                    if (valor.matches("-?\\d+") || valor.matches("-?\\d+\\.\\d+") || 
                        (valor.startsWith("'") && valor.endsWith("'"))) {
                        constantValues.put(destino, valor);
                        codigoOptimizado.add(linea);
                        continue;
                    }
                    
                    // Reemplazar variables con sus valores constantes conocidos
                    if (constantValues.containsKey(valor)) {
                        constantValues.put(destino, constantValues.get(valor));
                        codigoOptimizado.add(destino + " = " + constantValues.get(valor));
                        continue;
                    }
                }
            }
            
            // Reemplazar usos de constantes en expresiones
            String lineaOptimizada = linea;
            for (Map.Entry<String, String> entry : constantValues.entrySet()) {
                // Solo reemplazar si es un operando completo (no parte de otro identificador)
                String regex = "\\b" + entry.getKey() + "\\b";
                lineaOptimizada = lineaOptimizada.replaceAll(regex, entry.getValue());
            }
            
            codigoOptimizado.add(lineaOptimizada);
        }
        
        codigo = codigoOptimizado;
    }
    
    /**
     * Simplifica expresiones constantes (ej: 2 + 3 -> 5)
     */
    public void simplificarExpresiones() {
        List<String> codigoOptimizado = new ArrayList<>();
        
        for (String linea : codigo) {
            if (linea.contains(" = ")) {
                String[] partes = linea.split(" = ");
                if (partes.length == 2) {
                    String destino = partes[0].trim();
                    String expr = partes[1].trim();
                    
                    // Evaluar expresiones aritméticas constantes
                    if (expr.matches("-?\\d+ [+\\-*/%] -?\\d+")) {
                        String[] operacion = expr.split(" ");
                        int a = Integer.parseInt(operacion[0]);
                        String op = operacion[1];
                        int b = Integer.parseInt(operacion[2]);
                        int resultado = 0;
                        
                        switch (op) {
                            case "+": resultado = a + b; break;
                            case "-": resultado = a - b; break;
                            case "*": resultado = a * b; break;
                            case "/": 
                                if (b != 0) resultado = a / b; 
                                else {
                                    codigoOptimizado.add(linea); // División por cero, mantener original
                                    continue;
                                }
                                break;
                            case "%": 
                                if (b != 0) resultado = a % b; 
                                else {
                                    codigoOptimizado.add(linea); // Módulo por cero, mantener original
                                    continue;
                                }
                                break;
                        }
                        
                        codigoOptimizado.add(destino + " = " + resultado);
                        continue;
                    }
                    
                    // Simplificar expresiones lógicas constantes (para enteros)
                    if (expr.matches("-?\\d+ [<>]=? -?\\d+") || expr.matches("-?\\d+ [!=]= -?\\d+")) {
                        String[] operacion = expr.split(" ");
                        int a = Integer.parseInt(operacion[0]);
                        String op = operacion[1];
                        int b = Integer.parseInt(operacion[2]);
                        boolean resultado = false;
                        
                        switch (op) {
                            case ">": resultado = a > b; break;
                            case ">=": resultado = a >= b; break;
                            case "<": resultado = a < b; break;
                            case "<=": resultado = a <= b; break;
                            case "==": resultado = a == b; break;
                            case "!=": resultado = a != b; break;
                        }
                        
                        codigoOptimizado.add(destino + " = " + (resultado ? 1 : 0));
                        continue;
                    }
                }
            }
            
            codigoOptimizado.add(linea);
        }
        
        codigo = codigoOptimizado;
    }
    
    /**
     * Elimina sentencias redundantes (asignación a sí mismo, etc)
     */
    public void eliminarSentenciasRedundantes() {
        List<String> codigoOptimizado = new ArrayList<>();
        
        for (String linea : codigo) {
            // Eliminar asignaciones a sí mismo (a = a)
            if (linea.contains(" = ")) {
                String[] partes = linea.split(" = ");
                if (partes.length == 2) {
                    String destino = partes[0].trim();
                    String valor = partes[1].trim();
                    
                    if (destino.equals(valor)) {
                        continue; // Saltar esta línea
                    }
                }
            }
            
            codigoOptimizado.add(linea);
        }
        
        codigo = codigoOptimizado;
    }
    
    /**
     * Obtiene el código optimizado
     */
    public List<String> getCodigoOptimizado() {
        return codigo;
    }
    
    /**
     * Imprime el código optimizado
     */
    public void imprimirCodigoOptimizado() {
        System.out.println("\n=== CÓDIGO OPTIMIZADO ===");
        for (int i = 0; i < codigo.size(); i++) {
            System.out.println(i + ": " + codigo.get(i));
        }
    }
}