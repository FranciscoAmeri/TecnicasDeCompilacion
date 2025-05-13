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
            
            // 1. ANÁLISIS LÉXICO
            System.out.println("Analizando archivo: " + inputFilePath);
            CharStream input = CharStreams.fromFileName(inputFilePath);

            List<String> erroresLexicos = new ArrayList<>();
            MiLenguajeLexer lexer = new MiLenguajeLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresLexicos.add(RED + "ERROR LÉXICO en línea " + line + ":" + charPositionInLine + " - " + msg + RESET);
                    throw new ParseCancellationException(msg);
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            System.out.println("\n=== ANÁLISIS LÉXICO ===");
            if (erroresLexicos.isEmpty()) {
              //  System.out.printf("%-20s %-30s %-10s %-10s\n", "TIPO", "LEXEMA", "LÍNEA", "COLUMNA");
               // System.out.println("-------------------------------------------------------------------");
                for (Token token : tokens.getTokens()) {
                    if (token.getType() != Token.EOF) {
                        String tokenName = MiLenguajeLexer.VOCABULARY.getSymbolicName(token.getType());
                        //System.out.printf("%-20s %-30s %-10d %-10d\n",
                        //        tokenName, token.getText(), token.getLine(), token.getCharPositionInLine());
                    }
                }
                System.out.println("\n" + GREEN + "✅ Análisis léxico completado sin errores." + RESET);
            } else {
                erroresLexicos.forEach(System.out::println);
                return;
            }

            // 2. ANÁLISIS SINTÁCTICO
            MiLenguajeParser parser = new MiLenguajeParser(tokens);
            List<String> erroresSintacticos = new ArrayList<>();
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresSintacticos.add(RED + "ERROR SINTÁCTICO en línea " + line + ":" + charPositionInLine + " - " + msg + RESET);
                }
            });

            System.out.println("\n=== ANÁLISIS SINTÁCTICO ===");
            ParseTree tree = parser.programa();
            if (!erroresSintacticos.isEmpty()) {
                erroresSintacticos.forEach(System.out::println);
                return;
            } else {
                System.out.println(GREEN + "✅ Análisis sintáctico completado sin errores." + RESET);
              //  System.out.println("Representación textual del árbol sintáctico:");
              //s  System.out.println(tree.toStringTree(parser));
            }

            // 3. VISUALIZACIÓN DEL ÁRBOL SINTÁCTICO
            generarImagenArbolSintactico(tree, parser);

            // 4. ANÁLISIS SEMÁNTICO
            SimbolosListener listener = new SimbolosListener();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);

            TablaSimbolos tabla = listener.getTablaSimbolos();
           // tabla.imprimir();

            List<String> erroresSemanticos = listener.getErrores();
            List<String> warningsSemanticos = listener.getWarnings();
            
            // Colorear errores y warnings
            List<String> erroresColoreados = new ArrayList<>();
            for (String error : erroresSemanticos) {
                erroresColoreados.add(RED + error + RESET);
            }
            
            List<String> warningsColoreados = new ArrayList<>();
            for (String warning : warningsSemanticos) {
                warningsColoreados.add(YELLOW + warning + RESET);
            }
            
            // Mostrar errores semánticos
            if (!erroresSemanticos.isEmpty()) {
                System.out.println("\n=== ERRORES SEMÁNTICOS ===");
                erroresColoreados.forEach(System.out::println);
                return; // No continuar si hay errores semánticos
            } else {
                System.out.println("\n" + GREEN + "✅ Análisis semántico completado sin errores." + RESET);
            }
            
            // Mostrar warnings semánticos (no impiden continuar)
            if (!warningsSemanticos.isEmpty()) {
                System.out.println("\n=== WARNINGS SEMÁNTICOS ===");
                warningsColoreados.forEach(System.out::println);
                System.out.println("\n" + YELLOW + "⚠️ El código tiene warnings, pero se puede continuar con la compilación." + RESET);
            }
            
            // 5. GENERACIÓN DE CÓDIGO INTERMEDIO
            System.out.println("\n=== GENERACIÓN DE CÓDIGO INTERMEDIO ===");
            CodigoVisitor visitor = new CodigoVisitor(tabla);
            visitor.visit(tree);
            
            GeneradorCodigo generador = visitor.getGenerador();
            //generador.imprimirCodigo();
            
            // Guardar código intermedio en archivo
            String codigoIntermedioPath = baseName + "_codigo_intermedio.txt";
            guardarCodigoEnArchivo(generador.getCodigo(), codigoIntermedioPath);
            System.out.println(GREEN + "✅ Código intermedio guardado en: " + codigoIntermedioPath + RESET);
            
            // 6. OPTIMIZACIÓN DE CÓDIGO
            System.out.println("\n=== OPTIMIZACIÓN DE CÓDIGO ===");
            Optimizador optimizador = new Optimizador(generador.getCodigo());
            optimizador.optimizar();
           // optimizador.imprimirCodigoOptimizado();
            
            // Guardar código optimizado en archivo
            String codigoOptimizadoPath = baseName + "_codigo_optimizado.txt";
            guardarCodigoEnArchivo(optimizador.getCodigoOptimizado(), codigoOptimizadoPath);
            System.out.println(GREEN + "✅ Código optimizado guardado en: " + codigoOptimizadoPath + RESET);

        } catch (IOException e) {
            System.err.println(RED + "❌ Error al leer o escribir archivos: " + e.getMessage() + RESET);
        } catch (ParseCancellationException e) {
            System.err.println(RED + "❌ Error de análisis: " + e.getMessage() + RESET);
        } catch (Exception e) {
            System.err.println(RED + "❌ Error inesperado:" + RESET);
            e.printStackTrace();
        }
    }

    /**
     * Guarda una lista de líneas de código en un archivo de texto
     */
    private static void guardarCodigoEnArchivo(List<String> codigo, String rutaArchivo) throws IOException {
        Path filePath = Paths.get(rutaArchivo);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (int i = 0; i < codigo.size(); i++) {
                writer.write(codigo.get(i));
                writer.newLine();
            }
        }
    }

    private static void generarImagenArbolSintactico(ParseTree tree, Parser parser) {
        try {
            JFrame frame = new JFrame("Árbol Sintáctico");
            JPanel panel = new JPanel();

            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
            viewer.setScale(1.5); // Zoom

            panel.add(viewer);

            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            frame.add(scrollPane);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            //frame.setVisible(true);
            viewer.open();  // Esto lanza una ventana gráfica con el árbol de análisis

        } catch (Exception e) {
            System.err.println(RED + "❌ Error al mostrar árbol sintáctico: " + e.getMessage() + RESET);
        }
    }
}