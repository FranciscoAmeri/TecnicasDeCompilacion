package com.compilador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// ========== GENERADOR DE CODIGO BASICO ==========
/**
 * Generador súper simple para explicar conceptos
 */
public class GeneradorCodigo {
    
    private List<String> codigo;
    private int tempCounter;
    private int labelCounter;
    
    public GeneradorCodigo() {
        this.codigo = new ArrayList<>();
        this.tempCounter = 0;
        this.labelCounter = 0;
        System.out.println("🔧 GENERADOR: Iniciado y listo para trabajar");
    }
    
    /**
     * Genera un nuevo nombre de variable temporal
     */
    public String newTemp() {
        String temp = "t" + (tempCounter++);
        System.out.println("🔧 GENERADOR: Creé temporal -> " + temp);
        return temp;
    }
    
    /**
     * Genera una nueva etiqueta
     */
    public String newLabel() {
        String label = "L" + (labelCounter++);
        System.out.println("🔧 GENERADOR: Creé etiqueta -> " + label);
        return label;
    }
    
    /**
     * Genera código para una operación binaria: t0 = a + b
     */
    public String genOperacionBinaria(String op, String left, String right) {
        System.out.println("🔧 GENERADOR: Generando operación " + left + " " + op + " " + right);
        
        String temp = newTemp();
        String instruccion = temp + " = " + left + " " + op + " " + right;
        codigo.add(instruccion);
        
        System.out.println("🔧 GENERADOR: Generé -> " + instruccion);
        return temp;
    }
    
    /**
     * Genera código para una asignación: x = t0
     */
    public void genAsignacion(String variable, String valor) {
        System.out.println("🔧 GENERADOR: Generando asignación " + variable + " = " + valor);
        
        String instruccion = variable + " = " + valor;
        codigo.add(instruccion);
        
        System.out.println("🔧 GENERADOR: Generé -> " + instruccion);
    }
    
    /**
     * Genera una etiqueta: L0:
     */
    public void genLabel(String label) {
        System.out.println("🔧 GENERADOR: Colocando etiqueta " + label);
        
        String instruccion = label + ":";
        codigo.add(instruccion);
        
        System.out.println("🔧 GENERADOR: Generé -> " + instruccion);
    }
    
    /**
     * Genera un salto condicional: if !condicion goto L0
     */
    public void genIfFalse(String condicion, String label) {
        System.out.println("🔧 GENERADOR: Generando salto condicional");
        
        String instruccion = "if !" + condicion + " goto " + label;
        codigo.add(instruccion);
        
        System.out.println("🔧 GENERADOR: Generé -> " + instruccion);
    }
    
    /**
     * Genera un salto incondicional: goto L0
     */
    public void genGoto(String label) {
        System.out.println("🔧 GENERADOR: Generando salto incondicional a " + label);
        
        String instruccion = "goto " + label;
        codigo.add(instruccion);
        
        System.out.println("🔧 GENERADOR: Generé -> " + instruccion);
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
        System.out.println("\n📝 === CÓDIGO DE TRES DIRECCIONES ===");
        for (int i = 0; i < codigo.size(); i++) {
            System.out.printf("%3d: %s\n", i, codigo.get(i));
        }
        System.out.println("Total instrucciones: " + codigo.size());
    }
    
    /**
     * Para compatibilidad - métodos dummy
     */
    public Map<String, String> getTiposVariables() { return null; }
    public void imprimirTipos() { }
    public void imprimirEstadisticas() { 
        System.out.println("\n📊 ESTADÍSTICAS:");
        System.out.println("   - Temporales creados: " + tempCounter);
        System.out.println("   - Etiquetas creadas: " + labelCounter);
        System.out.println("   - Instrucciones totales: " + codigo.size());
    }
}