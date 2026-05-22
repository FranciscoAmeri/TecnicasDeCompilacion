package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;
import com.compilador.semantico.SemanticAnalyzer;
import com.compilador.semantico.SemanticError;
import javax.swing.*;
import java.util.Arrays;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -jar demo-1.0-jar-with-dependencies.jar <archivo.txt>");
            System.exit(1);
        }

        try {
            CharStream input = CharStreams.fromFileName(args[0]);
            System.out.println("Analizando archivo: " + args[0]);
            System.out.println("=".repeat(65));

            // ── FASE 1: ANÁLISIS LÉXICO ──────────────────────────────────
            MiLenguajeLexer lexer = new MiLenguajeLexer(input);

            List<String> erroresLexicos = new ArrayList<>();
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg, RecognitionException e) {
                    erroresLexicos.add(
                        "  [Línea " + line + ":" + charPositionInLine + "] " + msg
                    );
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            System.out.println("\n=== FASE 1: ANÁLISIS LÉXICO ===\n");
            System.out.printf("  %-20s %-25s %-8s %-8s%n",
                              "TIPO DE TOKEN", "LEXEMA", "LÍNEA", "COLUMNA");
            System.out.println("  " + "-".repeat(63));

            for (Token token : tokens.getTokens()) {
                if (token.getType() == Token.EOF) continue;
                String tipo = MiLenguajeLexer.VOCABULARY.getSymbolicName(token.getType());
                if (tipo == null) tipo = "DESCONOCIDO";
                System.out.printf("  %-20s %-25s %-8d %-8d%n",
                                  tipo,
                                  token.getText(),
                                  token.getLine(),
                                  token.getCharPositionInLine());
            }

            if (!erroresLexicos.isEmpty()) {
                System.out.println("\n  ❌ ERRORES LÉXICOS:");
                for (String error : erroresLexicos) System.out.println(error);
                System.out.println("\n  El análisis no puede continuar con errores léxicos.");
                return;
            }

            System.out.println("\n  ✅ Análisis léxico completado sin errores.");

            // ── FASE 2: ANÁLISIS SINTÁCTICO ──────────────────────────────
            System.out.println("\n=== FASE 2: ANÁLISIS SINTÁCTICO ===\n");

            tokens.reset();
            MiLenguajeParser parser = new MiLenguajeParser(tokens);

            List<String> erroresSintacticos = new ArrayList<>();
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg, RecognitionException e) {
                    String tokenErroneo = (offendingSymbol != null)
                                         ? "'" + offendingSymbol + "'"
                                         : "fin de archivo";
                    erroresSintacticos.add(
                        "  [Línea " + line + ":" + charPositionInLine + "] "
                        + "cerca de " + tokenErroneo + " → " + msg
                    );
                }
            });

            MiLenguajeParser.ProgramaContext arbolParseo = parser.programa();

            if (!erroresSintacticos.isEmpty()) {
                System.out.println("  ❌ ERRORES SINTÁCTICOS:");
                for (String error : erroresSintacticos) System.out.println(error);
                System.out.println();
                System.out.println("  Pista: revisa que cada sentencia:");
                System.out.println("    - Termine con punto y coma ';'");
                System.out.println("    - Tenga paréntesis balanceados");
                System.out.println("    - Use tipos válidos (int, float, string, bool, char, double)");
                return;
            }

            System.out.println("  ✅ Análisis sintáctico completado sin errores.");

            // ── FASE 3: ANÁLISIS SEMÁNTICO ───────────────────────────────
            System.out.println("\n=== FASE 3: ANÁLISIS SEMÁNTICO ===\n");

            SemanticAnalyzer semantico = new SemanticAnalyzer();
            semantico.visit(arbolParseo);

            if (semantico.hayErrores()) {
                System.out.println("  ❌ ERRORES SEMÁNTICOS ("
                                   + semantico.getErrores().size() + "):\n");
                for (SemanticError error : semantico.getErrores()) {
                    System.out.println("  " + error);
                }
                System.out.println();
            }

            if (semantico.hayAdvertencias()) {
                System.out.println("  ⚠️  ADVERTENCIAS ("
                                   + semantico.getAdvertencias().size() + "):\n");
                for (SemanticError adv : semantico.getAdvertencias()) {
                    System.out.println("  " + adv);
                }
                System.out.println();
            }

            semantico.getTablaSimbolos().imprimirTabla();
            System.out.println("\n" + "=".repeat(65));

            if (semantico.hayErrores()) {
                System.out.println("  Compilacion finalizada con errores semanticos.");
            } else if (semantico.hayAdvertencias()) {
                System.out.println("  Compilacion exitosa con advertencias.");
            } else {
                System.out.println("  Compilacion exitosa.");
            }
            System.out.println(semantico.getErrores().size() + " error(es) semántico(s), "
                               + semantico.getAdvertencias().size() + " advertencia(s).");
            for (SemanticError error : semantico.getErrores()) {
                System.out.println("  ❌ " + error);
            }

            System.out.println("\n  Abriendo visualizador grafico del arbol...");
            mostrarArbol(arbolParseo, parser);

        } catch (IOException e) {
            System.err.println("❌ No se pudo leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarArbol(ParseTree tree, Parser parser) {
        JFrame frame = new JFrame("Árbol Sintáctico");
        JPanel panel = new JPanel();
        TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
        viewer.setScale(1.5);
        panel.add(viewer);

        JScrollPane scrollPane = new JScrollPane(panel);
        frame.add(scrollPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
