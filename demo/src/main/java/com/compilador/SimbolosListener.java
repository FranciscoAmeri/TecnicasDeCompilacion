package com.compilador;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Listener mejorado para construir la tabla de símbolos y realizar análisis semántico
 * con distinción entre errores y warnings
 */
public class SimbolosListener extends MiLenguajeBaseListener {
    
    private TablaSimbolos tablaSimbolos;
    private List<String> errores;
    private List<String> warnings;
    private String tipoRetornoActual;
    
    // Conjunto para hacer seguimiento de variables utilizadas
    private Map<String, Set<String>> variablesUtilizadas; // ámbito -> conjunto de variables utilizadas
    
    // Conjunto para hacer seguimiento de variables declaradas
    private Map<String, Map<String, Integer>> variablesDeclaradas; // ámbito -> (nombre -> línea de declaración)
    
    public SimbolosListener() {
        this.tablaSimbolos = new TablaSimbolos();
        this.errores = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.tipoRetornoActual = null;
        this.variablesUtilizadas = new HashMap<>();
        this.variablesDeclaradas = new HashMap<>();
        
        // Inicializar para ámbito global
        this.variablesUtilizadas.put("global", new HashSet<>());
        this.variablesDeclaradas.put("global", new HashMap<>());
    }
    
    /**
     * Obtiene la tabla de símbolos construida
     */
    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }
    
    /**
     * Obtiene la lista de errores semánticos
     */
    public List<String> getErrores() {
        return errores;
    }
    
    /**
     * Obtiene la lista de warnings semánticos
     */
    public List<String> getWarnings() {
        return warnings;
    }
    
    /**
     * Cuando se encuentra una declaración de función
     */
    @Override
    public void enterDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        // Obtener información de la función
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        
        // Crear símbolo para la función
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
            nombre, tipo, "funcion", linea, columna, "global"
        );
        
        // Marcar la función como utilizada (siendo una declaración global)
        variablesUtilizadas.get("global").add(nombre);
        
        // Agregar parámetros si existen
        if (ctx.parametros() != null) {
            for (MiLenguajeParser.ParametroContext paramCtx : ctx.parametros().parametro()) {
                String tipoParam = paramCtx.tipo().getText();
                String nombreParam = paramCtx.ID().getText();
                
                // Agregar tipo de parámetro a la función
                simbolo.addParametro(tipoParam);
                
                // Crear símbolo para el parámetro
                TablaSimbolos.Simbolo paramSimbolo = new TablaSimbolos.Simbolo(
                    nombreParam, tipoParam, "parametro", 
                    paramCtx.ID().getSymbol().getLine(),
                    paramCtx.ID().getSymbol().getCharPositionInLine(),
                    nombre  // El ámbito del parámetro es el nombre de la función
                );
                
                // Agregar el parámetro a la tabla de símbolos
                if (!tablaSimbolos.agregar(paramSimbolo)) {
                    errores.add("Error semántico en línea " + paramCtx.ID().getSymbol().getLine() + 
                              ": Parámetro duplicado '" + nombreParam + "'");
                }
                
                // Inicializar el seguimiento para este ámbito si no existe
                if (!variablesUtilizadas.containsKey(nombre)) {
                    variablesUtilizadas.put(nombre, new HashSet<>());
                    variablesDeclaradas.put(nombre, new HashMap<>());
                }
                
                // Marcar el parámetro como declarado
                variablesDeclaradas.get(nombre).put(nombreParam, paramCtx.ID().getSymbol().getLine());
                
                // Marcar el parámetro como utilizado (porque los parámetros se consideran utilizados)
                variablesUtilizadas.get(nombre).add(nombreParam);
            }
        }
        
        // Agregar la función a la tabla de símbolos
        if (!tablaSimbolos.agregar(simbolo)) {
            errores.add("Error semántico en línea " + linea + 
                      ": Función '" + nombre + "' ya declarada");
        } else {
            // Marcar la función como declarada
            variablesDeclaradas.get("global").put(nombre, linea);
        }
        
        // Cambiar el ámbito actual
        tablaSimbolos.setAmbito(nombre);
        
        // Inicializar el seguimiento para este ámbito si no existe
        if (!variablesUtilizadas.containsKey(nombre)) {
            variablesUtilizadas.put(nombre, new HashSet<>());
            variablesDeclaradas.put(nombre, new HashMap<>());
        }
        
        // Guardar el tipo de retorno para verificar las sentencias return
        tipoRetornoActual = tipo;
    }
    
    /**
     * Al salir de una declaración de función
     */
    @Override
    public void exitDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        
        // Verificar si la función no void tiene al menos un return
        if (!tipo.equals("void")) {
            // Podríamos hacer un análisis más profundo para garantizar que todos los caminos tienen return
            boolean tieneReturn = false;
            
            for (int i = 0; i < ctx.bloque().sentencia().size(); i++) {
                if (ctx.bloque().sentencia(i).retorno() != null) {
                    tieneReturn = true;
                    break;
                }
            }
            
            if (!tieneReturn) {
                errores.add("Error semántico en función '" + nombreFuncion + "': Función con tipo de retorno '" + 
                          tipo + "' debe tener al menos una sentencia return");
            }
        }
        
        // Verificar variables declaradas pero no utilizadas en este ámbito
        Set<String> utilizadas = variablesUtilizadas.get(nombreFuncion);
        Map<String, Integer> declaradas = variablesDeclaradas.get(nombreFuncion);
        
        for (Map.Entry<String, Integer> entry : declaradas.entrySet()) {
            String varNombre = entry.getKey();
            int varLinea = entry.getValue();
            
            if (!utilizadas.contains(varNombre)) {
                // Solo reportar warnings para variables (no para parámetros, que ya consideramos utilizados)
                TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(varNombre, nombreFuncion);
                if (simbolo != null && simbolo.getCategoria().equals("variable")) {
                    warnings.add("Warning semántico en línea " + varLinea + 
                              ": Variable '" + varNombre + "' declarada pero nunca utilizada");
                }
            }
        }
        
        // Restaurar el ámbito global y el tipo de retorno
        tablaSimbolos.setAmbito("global");
        tipoRetornoActual = null;
    }
    
    /**
     * Al salir del programa completo
     */
    @Override
    public void exitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        // Verificar variables globales declaradas pero no utilizadas
        Set<String> utilizadas = variablesUtilizadas.get("global");
        Map<String, Integer> declaradas = variablesDeclaradas.get("global");
        
        for (Map.Entry<String, Integer> entry : declaradas.entrySet()) {
            String varNombre = entry.getKey();
            int varLinea = entry.getValue();
            
            if (!utilizadas.contains(varNombre)) {
                // Solo reportar warnings para variables (no para funciones, que podrían ser utilizadas externamente)
                TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(varNombre, "global");
                if (simbolo != null && simbolo.getCategoria().equals("variable")) {
                    warnings.add("Warning semántico en línea " + varLinea + 
                              ": Variable global '" + varNombre + "' declarada pero nunca utilizada");
                }
            }
        }
    }
    
    /**
     * Cuando se encuentra una declaración de variable
     */
    @Override
    public void enterDeclaracionVariable(MiLenguajeParser.DeclaracionVariableContext ctx) {
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        String ambito = tablaSimbolos.getAmbito();
        
        // Crear y agregar el símbolo
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
            nombre, tipo, "variable", linea, columna, ambito
        );
        
        if (!tablaSimbolos.agregar(simbolo)) {
            errores.add("Error semántico en línea " + linea + 
                      ": Variable '" + nombre + "' ya declarada en este ámbito");
        } else {
            // Marcar la variable como declarada
            variablesDeclaradas.get(ambito).put(nombre, linea);
        }
    }
    
    /**
     * Cuando se encuentra una asignación
     */
    @Override
    public void enterAsignacion(MiLenguajeParser.AsignacionContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        String ambito = tablaSimbolos.getAmbito();
        
        // Verificar si la variable existe
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea + 
                      ": Variable '" + nombre + "' no declarada");
            return;
        }
        
        // Verificación de categoría (solo variables pueden ser asignadas, no funciones)
        if (!simbolo.getCategoria().equals("variable") && !simbolo.getCategoria().equals("parametro")) {
            errores.add("Error semántico en línea " + linea + 
                      ": No se puede asignar valor a '" + nombre + "' porque no es una variable");
            return;
        }
        
        // Marcar la variable como utilizada
        // Utilizamos el ámbito donde se declaró la variable
        String ambitoDeclaracion = simbolo.getAmbito();
        variablesUtilizadas.get(ambitoDeclaracion).add(nombre);
    }
    
    /**
     * Cuando se encuentra una expresión de variable
     */
    @Override
    public void enterExpVariable(MiLenguajeParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea + 
                      ": Identificador '" + nombre + "' no declarado");
        } else {
            // Marcar la variable como utilizada
            String ambitoDeclaracion = simbolo.getAmbito();
            variablesUtilizadas.get(ambitoDeclaracion).add(nombre);
        }
    }
    
    /**
     * Cuando se encuentra una llamada a función
     */
    @Override
    public void enterExpFuncion(MiLenguajeParser.ExpFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        
        // Verificar si la función existe
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea + 
                      ": Función '" + nombre + "' no declarada");
            return;
        }
        
        // Verificar que sea una función
        if (!simbolo.getCategoria().equals("funcion")) {
            errores.add("Error semántico en línea " + linea + 
                      ": '" + nombre + "' no es una función");
            return;
        }
        
        // Marcar la función como utilizada
        variablesUtilizadas.get("global").add(nombre);
        
        // Verificar número de argumentos
        int numArgumentosEsperados = simbolo.getParametros().size();
        int numArgumentosRecibidos = ctx.argumentos() == null ? 0 : ctx.argumentos().expresion().size();
        
        if (numArgumentosEsperados != numArgumentosRecibidos) {
            errores.add("Error semántico en línea " + linea + 
                      ": Función '" + nombre + "' espera " + numArgumentosEsperados + 
                      " argumentos, pero recibió " + numArgumentosRecibidos);
        }
    }
    
    /**
     * Cuando se encuentra una sentencia return
     */
    @Override
    public void enterRetorno(MiLenguajeParser.RetornoContext ctx) {
        if (tipoRetornoActual == null) {
            errores.add("Error semántico en línea " + ctx.getStart().getLine() + 
                      ": Sentencia return fuera de una función");
            return;
        }
        
        // Verificar compatibilidad del tipo de retorno
        if (tipoRetornoActual.equals("void")) {
            if (ctx.expresion() != null) {
                errores.add("Error semántico en línea " + ctx.getStart().getLine() + 
                          ": Función void no debe retornar un valor");
            }
        } else {
            if (ctx.expresion() == null) {
                errores.add("Error semántico en línea " + ctx.getStart().getLine() + 
                          ": Función con tipo de retorno '" + tipoRetornoActual + 
                          "' debe retornar un valor");
            }
        }
    }


    
    /**
     * Al encontrar un nodo de error en el árbol de análisis sintáctico
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
        errores.add("Error sintáctico en token: " + node.getText());
    }

    
}