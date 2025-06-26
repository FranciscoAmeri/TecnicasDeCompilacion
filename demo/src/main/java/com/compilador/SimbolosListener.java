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
 * Listener mejorado para construir la tabla de símbolos y realizar análisis semántico
 * con soporte para características de C++
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
        
        variablesUtilizadas.put("global", new HashSet<>());
        variablesDeclaradas.put("global", new HashMap<>());
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
        variablesUtilizadas.put(nombre, new HashSet<>());
        variablesDeclaradas.put(nombre, new HashMap<>());
    }
    @Override
public void enterDeclaracionClase(MiLenguajeParser.DeclaracionClaseContext ctx) {
    String nombre = ctx.ID(0).getText();           // ✅ Correcto
    int linea = ctx.ID(0).getSymbol().getLine();   // ✅ Correcto
    int columna = ctx.ID(0).getSymbol().getCharPositionInLine(); // ✅ Correcto
    
    TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
        nombre, "class", "clase", linea, columna, tablaSimbolos.getAmbito()
    );
    
    // Manejar herencia e interfaces con verificación de límites
    if (ctx.modificadorAcceso() != null) {
        int numModificadores = ctx.modificadorAcceso().size();
        int numIDs = ctx.ID().size();
        
        // Verificar que tenemos suficientes IDs para los modificadores
        if (numIDs > numModificadores) {
            for (int i = 0; i < numModificadores; i++) {
                String modificador = ctx.modificadorAcceso(i).getText();
                
                // Verificar que el índice sea válido antes de acceder
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
    variablesUtilizadas.put(nombre, new HashSet<>());
    variablesDeclaradas.put(nombre, new HashMap<>());
}
    
    @Override
public void enterDeclaracionVariable(MiLenguajeParser.DeclaracionVariableContext ctx) {
    String nombre = ctx.ID().getText();          // ✅ Correcto (solo hay un ID)
    String tipo = ctx.tipo().getText();
    int linea = ctx.ID().getSymbol().getLine();  // ✅ Correcto
    int columna = ctx.ID().getSymbol().getCharPositionInLine(); // ✅ Correcto
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
            variablesDeclaradas.put(ambito, new HashMap<>());
        }
        variablesDeclaradas.get(ambito).put(nombre, linea);
        
        errorHandler.addInfo(String.format(
            "Variable '%s' de tipo '%s' declarada correctamente (línea %d)",
            nombre, tipo, linea
        ));
    }
}
    
    @Override
public void enterAsignacion(MiLenguajeParser.AsignacionContext ctx) {
    String nombre = ctx.ID().getText();         // ✅ Correcto
    int linea = ctx.ID().getSymbol().getLine(); // ✅ Correcto
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
        variablesUtilizadas.put(ambitoDeclaracion, new HashSet<>());
    }
    
    variablesUtilizadas.get(ambitoDeclaracion).add(nombre);
    errorHandler.addInfo(String.format(
        "Asignación a variable '%s' realizada correctamente (línea %d)",
        nombre, linea
    ));
}
    
    private void verificarVariablesNoUtilizadas(String ambito) {
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
    
    @Override
    public void visitErrorNode(ErrorNode node) {
        errorHandler.addError(String.format(
            "Error sintáctico en token: '%s' (línea %d)",
            node.getText(),
            node.getSymbol().getLine()
        ));
    }
}