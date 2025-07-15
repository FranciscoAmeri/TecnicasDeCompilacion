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
 * Clase para manejar errores y warnings del compilador
 */
class ErrorHandler {
    public enum TipoError {
        ERROR("❌ Error"),
        WARNING("⚠️ Warning"),
        INFO("ℹ️ Info");
        
        private final String prefix;
        
        TipoError(String prefix) {
            this.prefix = prefix;
        }
        
        public String getPrefix() {
            return prefix;
        }
    }
    
    private List<String> errores;
    private List<String> warnings;
    private List<String> infos;
    
    public ErrorHandler() {
        this.errores = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.infos = new ArrayList<>();
    }
    
    public void addError(String mensaje) {
        errores.add(TipoError.ERROR.getPrefix() + ": " + mensaje);
    }
    
    public void addWarning(String mensaje) {
        warnings.add(TipoError.WARNING.getPrefix() + ": " + mensaje);
    }
    
    public void addInfo(String mensaje) {
        infos.add(TipoError.INFO.getPrefix() + ": " + mensaje);
    }
    
    public List<String> getErrores() {
        return errores;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public List<String> getInfos() {
        return infos;
    }
    
    public boolean hasErrors() {
        return !errores.isEmpty();
    }
    
    public void printAll() {
        System.out.println("\n=== REPORTE DE ANÁLISIS SEMÁNTICO ===");
        
        if (!errores.isEmpty()) {
            System.out.println("\nErrores encontrados:");
            for (String error : errores) {
                System.out.println(error);
            }
        }
        
        if (!warnings.isEmpty()) {
            System.out.println("\nWarnings encontrados:");
            for (String warning : warnings) {
                System.out.println(warning);
            }
        }
        
        if (!infos.isEmpty()) {
            System.out.println("\nInformación adicional:");
            for (String info : infos) {
                System.out.println(info);
            }
        }
        
        if (errores.isEmpty() && warnings.isEmpty() && infos.isEmpty()) {
            System.out.println("\n✅ No se encontraron errores ni warnings.");
        }
    }
}

/**
 * Listener completo para construir la tabla de símbolos y realizar análisis semántico
 */
public class SimbolosListener extends MiLenguajeBaseListener {
    
    private TablaSimbolos tablaSimbolos;
    private ErrorHandler errorHandler;
    private String tipoRetornoActual;
    private String modificadorAccesoActual;
    
    // Conjunto para hacer seguimiento de variables utilizadas
    private Map<String, Set<String>> variablesUtilizadas;
    private Map<String, Map<String, Integer>> variablesDeclaradas;
    
    public SimbolosListener() {
        this.tablaSimbolos = new TablaSimbolos();
        this.errorHandler = new ErrorHandler();
        this.tipoRetornoActual = null;
        this.modificadorAccesoActual = "private";
        this.variablesUtilizadas = new HashMap<>();
        this.variablesDeclaradas = new HashMap<>();
        
        variablesUtilizadas.put("global", new HashSet<String>());
        variablesDeclaradas.put("global", new HashMap<String, Integer>());
    }
    
    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }
    
    public List<String> getErrores() {
        return errorHandler.getErrores();
    }
    
    public List<String> getWarnings() {
        return errorHandler.getWarnings();
    }
    
    public List<String> getInfos() {
        return errorHandler.getInfos();
    }
    
    public void printReport() {
        errorHandler.printAll();
    }
    
    // ================ DECLARACIONES DE NAMESPACES ================
    @Override
    public void enterDeclaracionNamespace(MiLenguajeParser.DeclaracionNamespaceContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
            nombre, "namespace", "namespace", linea, columna, "global"
        );
        
        if (!tablaSimbolos.agregar(simbolo)) {
            errorHandler.addError(String.format(
                "El namespace '%s' ya está declarado en el ámbito global (línea %d, columna %d)",
                nombre, linea, columna
            ));
        } else {
            errorHandler.addInfo(String.format(
                "Namespace '%s' declarado correctamente (línea %d)",
                nombre, linea
            ));
        }
        
        tablaSimbolos.setAmbito(nombre);
        variablesUtilizadas.put(nombre, new HashSet<String>());
        variablesDeclaradas.put(nombre, new HashMap<String, Integer>());
    }
    
    @Override
    public void exitDeclaracionNamespace(MiLenguajeParser.DeclaracionNamespaceContext ctx) {
        String nombreNamespace = tablaSimbolos.getAmbito();
        verificarVariablesNoUtilizadas(nombreNamespace);
        tablaSimbolos.salirAmbito();
    }
    
    // ================ DECLARACIONES DE CLASES ================
    @Override
    public void enterDeclaracionClase(MiLenguajeParser.DeclaracionClaseContext ctx) {
        String nombre = ctx.ID(0).getText();
        int linea = ctx.ID(0).getSymbol().getLine();
        int columna = ctx.ID(0).getSymbol().getCharPositionInLine();
        
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
            nombre, "class", "clase", linea, columna, tablaSimbolos.getAmbito()
        );
        
        // Manejar herencia e interfaces con verificación de límites
        if (ctx.modificadorAcceso() != null) {
            int numModificadores = ctx.modificadorAcceso().size();
            int numIDs = ctx.ID().size();
            
            if (numIDs > numModificadores) {
                for (int i = 0; i < numModificadores; i++) {
                    String modificador = ctx.modificadorAcceso(i).getText();
                    
                    if (i + 1 < numIDs) {
                        String claseBase = ctx.ID(i + 1).getText();
                        
                        if (i == 0) {
                            simbolo.setClaseBase(claseBase);
                            errorHandler.addInfo(String.format(
                                "Clase '%s' hereda de '%s' (línea %d)",
                                nombre, claseBase, linea
                            ));
                        } else {
                            simbolo.addInterface(claseBase);
                            errorHandler.addInfo(String.format(
                                "Clase '%s' implementa interfaz '%s' (línea %d)",
                                nombre, claseBase, linea
                            ));
                        }
                    }
                }
            }
        }
        
        if (!tablaSimbolos.agregar(simbolo)) {
            errorHandler.addError(String.format(
                "La clase '%s' ya está declarada en el ámbito '%s' (línea %d, columna %d)",
                nombre, tablaSimbolos.getAmbito(), linea, columna
            ));
        } else {
            errorHandler.addInfo(String.format(
                "Clase '%s' declarada correctamente (línea %d)",
                nombre, linea
            ));
        }
        
        tablaSimbolos.setAmbito(nombre);
        variablesUtilizadas.put(nombre, new HashSet<String>());
        variablesDeclaradas.put(nombre, new HashMap<String, Integer>());
    }
    
    @Override
    public void exitDeclaracionClase(MiLenguajeParser.DeclaracionClaseContext ctx) {
        String nombreClase = tablaSimbolos.getAmbito();
        verificarVariablesNoUtilizadas(nombreClase);
        tablaSimbolos.salirAmbito();
    }
    
    // ================ DECLARACIONES DE FUNCIONES ================
    @Override
    public void enterDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        String ambito = tablaSimbolos.getAmbito();
        
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
            nombre, tipo, "funcion", linea, columna, ambito
        );
        
        // Agregar parámetros si existen - VERSION COMPATIBLE
        if (ctx.parametros() != null) {
            for (MiLenguajeParser.ParametroContext param : ctx.parametros().parametro()) {
                String tipoParam = param.tipo().getText();
                simbolo.addParametro(tipoParam);
            }
        }
        
        if (!tablaSimbolos.agregar(simbolo)) {
            errorHandler.addError(String.format(
                "La función '%s' ya está declarada en el ámbito '%s' (línea %d, columna %d)",
                nombre, ambito, linea, columna
            ));
        } else {
            errorHandler.addInfo(String.format(
                "Función '%s' de tipo '%s' declarada correctamente (línea %d)",
                nombre, tipo, linea
            ));
        }
        
        // Cambiar al ámbito de la función
        tablaSimbolos.setAmbito(nombre);
        variablesUtilizadas.put(nombre, new HashSet<String>());
        variablesDeclaradas.put(nombre, new HashMap<String, Integer>());
        
        // Guardar el tipo de retorno actual
        tipoRetornoActual = tipo;
    }
    
    @Override
    public void exitDeclaracionFuncion(MiLenguajeParser.DeclaracionFuncionContext ctx) {
        String nombreFuncion = tablaSimbolos.getAmbito();
        
        // Verificar variables no utilizadas en esta función
        verificarVariablesNoUtilizadas(nombreFuncion);
        
        // Salir del ámbito de la función
        tablaSimbolos.salirAmbito();
        tipoRetornoActual = null;
    }
    
    // ================ PARÁMETROS ================
    @Override
    public void enterParametro(MiLenguajeParser.ParametroContext ctx) {
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        String ambito = tablaSimbolos.getAmbito();
        
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
            nombre, tipo, "parametro", linea, columna, ambito
        );
        
        // Verificar si es puntero - VERSION COMPATIBLE
        if (ctx.PTR() != null) {
            simbolo.setEsPuntero(true);
        }
        
        // Verificar si es array - VERSION COMPATIBLE  
        if (ctx.CA() != null) {
            simbolo.setEsArray(true);
            if (ctx.INTEGER() != null) {
                int tamano = Integer.parseInt(ctx.INTEGER().getText());
                simbolo.setTamanoArray(tamano);
            }
        }
        
        if (!tablaSimbolos.agregar(simbolo)) {
            errorHandler.addError(String.format(
                "El parámetro '%s' ya está declarado en la función (línea %d, columna %d)",
                nombre, linea, columna
            ));
        } else {
            // Agregar a variables declaradas
            if (!variablesDeclaradas.containsKey(ambito)) {
                variablesDeclaradas.put(ambito, new HashMap<String, Integer>());
            }
            variablesDeclaradas.get(ambito).put(nombre, linea);
            
            errorHandler.addInfo(String.format(
                "Parámetro '%s' de tipo '%s' declarado correctamente (línea %d)",
                nombre, tipo, linea
            ));
        }
    }
    
    // ================ DECLARACIONES DE VARIABLES ================
    @Override
    public void enterDeclaracionVariable(MiLenguajeParser.DeclaracionVariableContext ctx) {
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        String ambito = tablaSimbolos.getAmbito();
        
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
            nombre, tipo, "variable", linea, columna, ambito
        );
        
        if (tablaSimbolos.getAmbito().equals("clase")) {
            simbolo.setModificadorAcceso(modificadorAccesoActual);
            errorHandler.addInfo(String.format(
                "Variable '%s' declarada como %s en la clase (línea %d)",
                nombre, modificadorAccesoActual, linea
            ));
        }
        
        if (ctx.PTR() != null) {
            simbolo.setEsPuntero(true);
            errorHandler.addInfo(String.format(
                "Variable '%s' declarada como puntero (línea %d)",
                nombre, linea
            ));
        }
        
        if (ctx.CA() != null) {
            simbolo.setEsArray(true);
            if (ctx.INTEGER() != null) {
                int tamano = Integer.parseInt(ctx.INTEGER().getText());
                simbolo.setTamanoArray(tamano);
                errorHandler.addInfo(String.format(
                    "Array '%s' declarado con tamaño %d (línea %d)",
                    nombre, tamano, linea
                ));
            }
        }
        
        if (!tablaSimbolos.agregar(simbolo)) {
            errorHandler.addError(String.format(
                "La variable '%s' ya está declarada en el ámbito '%s' (línea %d, columna %d)",
                nombre, ambito, linea, columna
            ));
        } else {
            // Verificar que el ámbito existe en el mapa antes de usarlo
            if (!variablesDeclaradas.containsKey(ambito)) {
                variablesDeclaradas.put(ambito, new HashMap<String, Integer>());
            }
            variablesDeclaradas.get(ambito).put(nombre, linea);
            
            errorHandler.addInfo(String.format(
                "Variable '%s' de tipo '%s' declarada correctamente (línea %d)",
                nombre, tipo, linea
            ));
        }
    }
    
    // ================ ASIGNACIONES ================
    @Override
    public void enterAsignacion(MiLenguajeParser.AsignacionContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        String ambito = tablaSimbolos.getAmbito();
        
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errorHandler.addError(String.format(
                "Variable '%s' no declarada en el ámbito '%s' (línea %d)",
                nombre, ambito, linea
            ));
            return;
        }
        
        if (!simbolo.getCategoria().equals("variable") && !simbolo.getCategoria().equals("parametro")) {
            errorHandler.addError(String.format(
                "No se puede asignar valor a '%s' porque no es una variable (línea %d)",
                nombre, linea
            ));
            return;
        }
        
        if (simbolo.getAmbito().equals("clase") && simbolo.getModificadorAcceso().equals("private")) {
            String claseActual = tablaSimbolos.getAmbito();
            if (!claseActual.equals(simbolo.getAmbito())) {
                errorHandler.addError(String.format(
                    "No se puede acceder al miembro privado '%s' desde fuera de la clase (línea %d)",
                    nombre, linea
                ));
                return;
            }
        }
        
        String ambitoDeclaracion = simbolo.getAmbito();
        
        // Verificar que el ámbito existe antes de usarlo
        if (!variablesUtilizadas.containsKey(ambitoDeclaracion)) {
            variablesUtilizadas.put(ambitoDeclaracion, new HashSet<String>());
        }
        
        variablesUtilizadas.get(ambitoDeclaracion).add(nombre);
        errorHandler.addInfo(String.format(
            "Asignación a variable '%s' realizada correctamente (línea %d)",
            nombre, linea
        ));
    }
    
    // ================ EXPRESIONES ================
    @Override
    public void enterExpFuncion(MiLenguajeParser.ExpFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombreFuncion);
        if (simbolo == null) {
            errorHandler.addError(String.format(
                "Función '%s' no declarada (línea %d)",
                nombreFuncion, linea
            ));
            return;
        }
        
        if (!simbolo.getCategoria().equals("funcion")) {
            errorHandler.addError(String.format(
                "'%s' no es una función (línea %d)",
                nombreFuncion, linea
            ));
            return;
        }
        
        errorHandler.addInfo(String.format(
            "Llamada a función '%s' realizada correctamente (línea %d)",
            nombreFuncion, linea
        ));
    }
    
    @Override
    public void enterExpVariable(MiLenguajeParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        String ambito = tablaSimbolos.getAmbito();
        
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errorHandler.addError(String.format(
                "Variable '%s' no declarada en el ámbito '%s' (línea %d)",
                nombre, ambito, linea
            ));
            return;
        }
        
        String ambitoDeclaracion = simbolo.getAmbito();
        
        // Marcar como utilizada
        if (!variablesUtilizadas.containsKey(ambitoDeclaracion)) {
            variablesUtilizadas.put(ambitoDeclaracion, new HashSet<String>());
        }
        
        variablesUtilizadas.get(ambitoDeclaracion).add(nombre);
    }
    
    // ================ MODIFICADORES DE ACCESO ================
    @Override
    public void enterModificadorAcceso(MiLenguajeParser.ModificadorAccesoContext ctx) {
        modificadorAccesoActual = ctx.getText();
    }
    
    // ================ MÉTODOS AUXILIARES ================
    private void verificarVariablesNoUtilizadas(String ambito) {
        if (!variablesUtilizadas.containsKey(ambito) || !variablesDeclaradas.containsKey(ambito)) {
            return;
        }
        
        Set<String> utilizadas = variablesUtilizadas.get(ambito);
        Map<String, Integer> declaradas = variablesDeclaradas.get(ambito);
        
        for (Map.Entry<String, Integer> entry : declaradas.entrySet()) {
            String varNombre = entry.getKey();
            int varLinea = entry.getValue();
            
            if (!utilizadas.contains(varNombre)) {
                TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(varNombre, ambito);
                if (simbolo != null && simbolo.getCategoria().equals("variable")) {
                    errorHandler.addWarning(String.format(
                        "Variable '%s' declarada pero nunca utilizada en el ámbito '%s' (línea %d)",
                        varNombre, ambito, varLinea
                    ));
                }
            }
        }
    }
    
    // ================ MANEJO DE ERRORES ================
    @Override
    public void visitErrorNode(ErrorNode node) {
        errorHandler.addError(String.format(
            "Error sintáctico en token: '%s' (línea %d)",
            node.getText(),
            node.getSymbol().getLine()
        ));
    }
    
    // ================ FINALIZACIÓN DEL ANÁLISIS ================
    @Override
    public void exitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        // Verificar variables globales no utilizadas al final
        verificarVariablesNoUtilizadas("global");
        
        int totalSimbolos = 0;
        for (String ambito : variablesDeclaradas.keySet()) {
            totalSimbolos += variablesDeclaradas.get(ambito).size();
        }
        
        errorHandler.addInfo(String.format(
            "Análisis semántico completado. Total de símbolos: %d",
            totalSimbolos
        ));
    }
}