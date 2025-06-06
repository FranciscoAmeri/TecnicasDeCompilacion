package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class App {
    // Códigos ANSI para colores
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(RED + "Uso: java -jar compilador.jar <archivo.txt>" + RESET);
            System.exit(1);
        }

        try {
            // Obtener el nombre del archivo de entrada para generar nombres de salida
            String inputFilePath = args[0];
            String inputFileName = new File(inputFilePath).getName();
            String baseName = inputFileName.substring(0, inputFileName.lastIndexOf('.'));
            
            // Verificar que el archivo existe
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                System.err.println(RED + "❌ Error: El archivo '" + inputFilePath + "' no existe." + RESET);
                System.exit(1);
            }
            
            System.out.println(CYAN + "🚀 Iniciando compilación de: " + inputFilePath + RESET);
            System.out.println("=" .repeat(60));
            
            // 1. ANÁLISIS LÉXICO
            System.out.println(BLUE + "\n=== 1. ANÁLISIS LÉXICO ===" + RESET);
            CharStream input = CharStreams.fromFileName(inputFilePath);

            List<String> erroresLexicos = new ArrayList<>();
            MiLenguajeLexer lexer = new MiLenguajeLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresLexicos.add("ERROR LÉXICO en línea " + line + ":" + charPositionInLine + " - " + msg);
                    throw new ParseCancellationException(msg);
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            if (erroresLexicos.isEmpty()) {
                System.out.println(GREEN + "✅ Análisis léxico completado sin errores." + RESET);
                System.out.println("   📊 Tokens procesados: " + (tokens.size() - 1)); // -1 para no contar EOF
            } else {
                erroresLexicos.forEach(error -> System.out.println(RED + error + RESET));
                return;
            }

            // 2. ANÁLISIS SINTÁCTICO
            System.out.println(BLUE + "\n=== 2. ANÁLISIS SINTÁCTICO ===" + RESET);
            MiLenguajeParser parser = new MiLenguajeParser(tokens);
            List<String> erroresSintacticos = new ArrayList<>();
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresSintacticos.add("ERROR SINTÁCTICO en línea " + line + ":" + charPositionInLine + " - " + msg);
                }
            });

            ParseTree tree = parser.programa();
            if (!erroresSintacticos.isEmpty()) {
                erroresSintacticos.forEach(error -> System.out.println(RED + error + RESET));
                return;
            } else {
                System.out.println(GREEN + "✅ Análisis sintáctico completado sin errores." + RESET);
                System.out.println("   📊 Árbol sintáctico generado correctamente");
            }

            // 3. VISUALIZACIÓN DEL ÁRBOL SINTÁCTICO
            System.out.println(BLUE + "\n=== 3. VISUALIZACIÓN DEL AST ===" + RESET);
            generarImagenArbolSintactico(tree, parser);
            System.out.println("   📊 Ventana del árbol sintáctico abierta");

            // 4. ANÁLISIS SEMÁNTICO
            System.out.println(BLUE + "\n=== 4. ANÁLISIS SEMÁNTICO ===" + RESET);
            SimbolosListener listener = new SimbolosListener();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);

            TablaSimbolos tabla = listener.getTablaSimbolos();
            
            // Mostrar tabla de símbolos
            System.out.println("   📋 Tabla de símbolos construida:");
            tabla.imprimir();

            List<String> erroresSemanticos = listener.getErrores();
            List<String> warningsSemanticos = listener.getWarnings();
            
            // Mostrar errores semánticos
            if (!erroresSemanticos.isEmpty()) {
                System.out.println(RED + "\n❌ ERRORES SEMÁNTICOS:" + RESET);
                erroresSemanticos.forEach(error -> System.out.println(RED + "   " + error + RESET));
                return; // No continuar si hay errores semánticos
            } else {
                System.out.println(GREEN + "✅ Análisis semántico completado sin errores." + RESET);
            }
            
            // Mostrar warnings semánticos (no impiden continuar)
            if (!warningsSemanticos.isEmpty()) {
                System.out.println(YELLOW + "\n⚠️ WARNINGS SEMÁNTICOS:" + RESET);
                warningsSemanticos.forEach(warning -> System.out.println(YELLOW + "   " + warning + RESET));
                System.out.println(YELLOW + "   ⚠️ El código tiene warnings, pero se puede continuar." + RESET);
            }
            
            // 5. GENERACIÓN DE CÓDIGO INTERMEDIO
            System.out.println(BLUE + "\n=== 5. GENERACIÓN DE CÓDIGO INTERMEDIO ===" + RESET);
            System.out.println("   🎯 Iniciando recorrido del AST con CodigoVisitor...");
            
            // Crear el visitor con la tabla de símbolos
            CodigoVisitor visitor = new CodigoVisitor(tabla);
            
            // ¡AQUÍ! - Recorrer el AST para generar código intermedio
            visitor.visit(tree);
            
            // Obtener el generador con el código generado
            GeneradorCodigo generador = visitor.getGenerador();
            
            // Mostrar el código generado en consola
            System.out.println("   📝 Código de tres direcciones generado:");
            generador.imprimirCodigo();
            
            // Mostrar información adicional
            if (generador.getTiposVariables() != null) {
                generador.imprimirTipos();
            }
            generador.imprimirEstadisticas();
            
            // Guardar código intermedio en archivo
            String codigoIntermedioPath = baseName + "_codigo_intermedio.txt";
            guardarCodigoEnArchivo(generador.getCodigo(), codigoIntermedioPath);
            System.out.println(GREEN + "✅ Código intermedio guardado en: " + codigoIntermedioPath + RESET);
            
            // 6. RESUMEN FINAL
            System.out.println(BLUE + "\n=== 6. RESUMEN DE COMPILACIÓN ===" + RESET);
            System.out.println("   📁 Archivo procesado: " + inputFilePath);
            System.out.println("   🔤 Tokens analizados: " + (tokens.size() - 1));
            System.out.println("   📊 Símbolos en tabla: " + contarSimbolos(tabla));
            System.out.println("   📝 Instrucciones generadas: " + generador.getCodigo().size());
            System.out.println("   📄 Archivo de salida: " + codigoIntermedioPath);
            
            if (erroresSemanticos.isEmpty()) {
                System.out.println(GREEN + "\n🎉 ¡COMPILACIÓN EXITOSA! 🎉" + RESET);
            }

        } catch (IOException e) {
            System.err.println(RED + "❌ Error al leer o escribir archivos: " + e.getMessage() + RESET);
        } catch (ParseCancellationException e) {
            System.err.println(RED + "❌ Error de análisis léxico: " + e.getMessage() + RESET);
        } catch (Exception e) {
            System.err.println(RED + "❌ Error inesperado:" + RESET);
            e.printStackTrace();
        }
    }

    /**
     * Cuenta la cantidad de símbolos en la tabla
     */
    private static int contarSimbolos(TablaSimbolos tabla) {
        // Implementación simple - en la tabla real deberías tener un método size()
        return 0; // Por ahora retorna 0, implementa según tu TablaSimbolos
    }

    /**
     * Guarda una lista de líneas de código en un archivo de texto
     */
    private static void guardarCodigoEnArchivo(List<String> codigo, String rutaArchivo) throws IOException {
        Path filePath = Paths.get(rutaArchivo);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("// Código de tres direcciones generado automáticamente");
            writer.newLine();
            writer.write("// Archivo: " + rutaArchivo);
            writer.newLine();
            writer.write("// Total de instrucciones: " + codigo.size());
            writer.newLine();
            writer.newLine();
            
            for (int i = 0; i < codigo.size(); i++) {
                writer.write(String.format("%3d: %s", i, codigo.get(i)));
                writer.newLine();
            }
        }
        System.out.println("   💾 Archivo guardado con " + codigo.size() + " instrucciones");
    }

    /**
     * Genera y muestra el árbol sintáctico visualmente
     */
    private static void generarImagenArbolSintactico(ParseTree tree, Parser parser) {
        try {
            JFrame frame = new JFrame("Árbol Sintáctico - Compilador");
            JPanel panel = new JPanel();

            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
            viewer.setScale(1.5); // Zoom

            panel.add(viewer);

            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            frame.add(scrollPane);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null); // Centrar ventana
            
            // Comentado para evitar que bloquee la ejecución
            // frame.setVisible(true);
            
            viewer.open();  // Esto lanza una ventana gráfica con el árbol de análisis

        } catch (Exception e) {
            System.err.println(RED + "❌ Error al mostrar árbol sintáctico: " + e.getMessage() + RESET);
            System.err.println("   ⚠️ La visualización del AST falló, pero la compilación continúa...");
        }
    }
}