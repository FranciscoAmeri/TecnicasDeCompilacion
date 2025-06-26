package com.compilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de una tabla de símbolos para el compilador C++
 */
public class TablaSimbolos {
    
    // Clase para representar un símbolo
    public static class Simbolo {
        private String nombre;
        private String tipo;        // int, char, double, void, class_name
        private String categoria;   // variable, funcion, parametro, clase, namespace
        private int linea;
        private int columna;
        private String ambito;      // global, namespace_name, class_name, function_name
        private List<String> parametros;  // Para funciones (lista de tipos)
        private String modificadorAcceso; // public, private, protected (para miembros de clase)
        private boolean esPuntero;  // Indica si es un puntero
        private boolean esArray;    // Indica si es un array
        private int tamanoArray;    // Tamaño del array si es array
        private String claseBase;   // Para clases que heredan
        private List<String> interfaces; // Para clases que implementan interfaces
        
        public Simbolo(String nombre, String tipo, String categoria, int linea, int columna, String ambito) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.categoria = categoria;
            this.linea = linea;
            this.columna = columna;
            this.ambito = ambito;
            this.parametros = new ArrayList<>();
            this.modificadorAcceso = "private"; // Por defecto
            this.esPuntero = false;
            this.esArray = false;
            this.tamanoArray = 0;
            this.claseBase = null;
            this.interfaces = new ArrayList<>();
        }
        
        // Getters
        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
        public String getCategoria() { return categoria; }
        public int getLinea() { return linea; }
        public int getColumna() { return columna; }
        public String getAmbito() { return ambito; }
        public List<String> getParametros() { return parametros; }
        
        // Nuevos getters y setters
        public String getModificadorAcceso() { return modificadorAcceso; }
        public void setModificadorAcceso(String modificadorAcceso) { this.modificadorAcceso = modificadorAcceso; }
        
        public boolean esPuntero() { return esPuntero; }
        public void setEsPuntero(boolean esPuntero) { this.esPuntero = esPuntero; }
        
        public boolean esArray() { return esArray; }
        public void setEsArray(boolean esArray) { this.esArray = esArray; }
        
        public int getTamanoArray() { return tamanoArray; }
        public void setTamanoArray(int tamanoArray) { this.tamanoArray = tamanoArray; }
        
        public String getClaseBase() { return claseBase; }
        public void setClaseBase(String claseBase) { this.claseBase = claseBase; }
        
        public List<String> getInterfaces() { return interfaces; }
        public void addInterface(String interfaz) { this.interfaces.add(interfaz); }
        
        // Agregar un parámetro a una función
        public void addParametro(String tipo) {
            parametros.add(tipo);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-15s %-10s %-15s %-10d %-10d %-15s", 
                      nombre, tipo, categoria, linea, columna, ambito));
            
            // Agregar información de puntero/array
            if (esPuntero) sb.append(" [ptr]");
            if (esArray) sb.append(" [arr:" + tamanoArray + "]");
            
            // Agregar modificador de acceso para miembros de clase
            if (categoria.equals("variable") || categoria.equals("funcion")) {
                sb.append(" [" + modificadorAcceso + "]");
            }
            
            // Agregar parámetros para funciones
            if (!parametros.isEmpty()) {
                sb.append(" [");
                for (int i = 0; i < parametros.size(); i++) {
                    sb.append(parametros.get(i));
                    if (i < parametros.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            }
            
            // Agregar información de herencia para clases
            if (categoria.equals("clase")) {
                if (claseBase != null) {
                    sb.append(" extends " + claseBase);
                }
                if (!interfaces.isEmpty()) {
                    sb.append(" implements ");
                    for (int i = 0; i < interfaces.size(); i++) {
                        sb.append(interfaces.get(i));
                        if (i < interfaces.size() - 1) {
                            sb.append(", ");
                        }
                    }
                }
            }
            
            return sb.toString();
        }
    }
    
    // Lista de símbolos
    private List<Simbolo> simbolos;
    
    // Ámbito actual (global, namespace, class, function)
    private String ambitoActual;
    
    // Pila de ámbitos para manejar anidamiento
    private List<String> pilaAmbitos;
    
    /**
     * Constructor
     */
    public TablaSimbolos() {
        this.simbolos = new ArrayList<>();
        this.ambitoActual = "global";
        this.pilaAmbitos = new ArrayList<>();
        this.pilaAmbitos.add("global");
    }
    
    /**
     * Establece el ámbito actual
     */
    public void setAmbito(String ambito) {
        this.ambitoActual = ambito;
        this.pilaAmbitos.add(ambito);
    }
    
    /**
     * Sale del ámbito actual y vuelve al anterior
     */
    public void salirAmbito() {
        if (pilaAmbitos.size() > 1) {
            pilaAmbitos.remove(pilaAmbitos.size() - 1);
            this.ambitoActual = pilaAmbitos.get(pilaAmbitos.size() - 1);
        }
    }
    
    /**
     * Obtiene el ámbito actual
     */
    public String getAmbito() {
        return this.ambitoActual;
    }
    
    /**
     * Obtiene el ámbito padre
     */
    public String getAmbitoPadre() {
        if (pilaAmbitos.size() > 1) {
            return pilaAmbitos.get(pilaAmbitos.size() - 2);
        }
        return "global";
    }
    
    /**
     * Agrega un símbolo a la tabla
     * @param simbolo Símbolo a agregar
     * @return true si se agregó correctamente, false si ya existía en el mismo ámbito
     */
    public boolean agregar(Simbolo simbolo) {
        // Verificar si ya existe un símbolo con el mismo nombre en el mismo ámbito
        for (Simbolo s : simbolos) {
            if (s.getNombre().equals(simbolo.getNombre()) && 
                s.getAmbito().equals(simbolo.getAmbito())) {
                return false;
            }
        }
        
        // Agregar el símbolo a la tabla
        simbolos.add(simbolo);
        return true;
    }
    
    /**
     * Busca un símbolo por nombre en el ámbito actual y superiores
     * @param nombre Nombre del símbolo a buscar
     * @return El símbolo encontrado o null si no existe
     */
    public Simbolo buscar(String nombre) {
        // Buscar en todos los ámbitos de la pila, desde el más reciente
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            String ambito = pilaAmbitos.get(i);
            for (Simbolo s : simbolos) {
                if (s.getNombre().equals(nombre) && s.getAmbito().equals(ambito)) {
                    return s;
                }
            }
        }
        return null;
    }
    
    /**
     * Busca un símbolo por nombre y ámbito específico
     * @param nombre Nombre del símbolo
     * @param ambito Ámbito donde buscar
     * @return El símbolo encontrado o null si no existe
     */
    public Simbolo buscar(String nombre, String ambito) {
        for (Simbolo s : simbolos) {
            if (s.getNombre().equals(nombre) && s.getAmbito().equals(ambito)) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los símbolos de un ámbito específico
     * @param ambito Ámbito del que obtener los símbolos
     * @return Lista de símbolos en ese ámbito
     */
    public List<Simbolo> getSimbolosAmbito(String ambito) {
        List<Simbolo> resultado = new ArrayList<>();
        for (Simbolo s : simbolos) {
            if (s.getAmbito().equals(ambito)) {
                resultado.add(s);
            }
        }
        return resultado;
    }
    
    /**
     * Imprime la tabla de símbolos
     */
    public void imprimir() {
        System.out.println("\n=== TABLA DE SÍMBOLOS ===");
        System.out.printf("%-15s %-10s %-15s %-10s %-10s %-15s %s\n", 
                         "NOMBRE", "TIPO", "CATEGORÍA", "LÍNEA", "COLUMNA", "ÁMBITO", "DETALLES");
        System.out.println("--------------------------------------------------------------------------------------------");
        
        for (Simbolo s : simbolos) {
            System.out.println(s);
        }
    }
}