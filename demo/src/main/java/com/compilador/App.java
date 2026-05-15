package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto de entrada del compilador educativo.
 *
 * Este programa realiza DOS fases del análisis:
 *   1. ANÁLISIS LÉXICO  — convierte el texto en tokens
 *   2. ANÁLISIS SINTÁCTICO — verifica que los tokens forman
 *      estructuras válidas según la gramática
 *
 * Para ejecutar:
 *   java -jar demo-1.0-jar-with-dependencies.jar <archivo.txt>
 */
public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -jar demo-1.0-jar-with-dependencies.jar <archivo.txt>");
            System.exit(1);
        }

        try {
            // Cargar el archivo de texto como un stream de caracteres
            CharStream input = CharStreams.fromFileName(args[0]);
            System.out.println("Analizando archivo: " + args[0]);
            System.out.println("=".repeat(65));

            // =========================================================
            //  FASE 1: ANÁLISIS LÉXICO
            //
            //  El Lexer lee los caracteres del archivo y los agrupa
            //  en unidades con significado llamadas TOKENS.
            //
            //  Ejemplo:
            //    "int x = 5 + 3 ;" → [INT] [ID:x] [IGUAL] [INTEGER:5]
            //                          [SUM] [INTEGER:3] [PYC]
            // =========================================================

            MiLenguajeLexer lexer = new MiLenguajeLexer(input);

            // Reemplazamos el manejador de errores por defecto del lexer.
            // Por defecto ANTLR imprime errores en System.err; aquí los
            // capturamos para mostrarlos de forma más clara.
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

            // fill() ejecuta el lexer y almacena TODOS los tokens en memoria.
            // Esto nos permite mostrarlos y luego reutilizarlos para el parser.
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            // Mostrar tabla de tokens
            System.out.println("\n=== FASE 1: ANÁLISIS LÉXICO ===\n");
            System.out.printf("  %-20s %-25s %-8s %-8s%n",
                              "TIPO DE TOKEN", "LEXEMA", "LÍNEA", "COLUMNA");
            System.out.println("  " + "-".repeat(63));

            for (Token token : tokens.getTokens()) {
                if (token.getType() == Token.EOF) continue;

                String tipo = MiLenguajeLexer.VOCABULARY.getSymbolicName(token.getType());
                // Si el tipo es null, probablemente es OTRO (char no reconocido)
                if (tipo == null) tipo = "DESCONOCIDO";

                System.out.printf("  %-20s %-25s %-8d %-8d%n",
                                  tipo,
                                  token.getText(),
                                  token.getLine(),
                                  token.getCharPositionInLine());
            }

            // Si hubo errores léxicos, reportar y detener
            if (!erroresLexicos.isEmpty()) {
                System.out.println("\n  ❌ ERRORES LÉXICOS:");
                for (String error : erroresLexicos) {
                    System.out.println(error);
                }
                System.out.println("\n  El análisis no puede continuar con errores léxicos.");
                return;
            }

            System.out.println("\n  ✅ Análisis léxico completado sin errores.");

            // =========================================================
            //  FASE 2: ANÁLISIS SINTÁCTICO (PARSING)
            //
            //  El Parser recibe los tokens y verifica que forman
            //  estructuras válidas según las REGLAS de la gramática.
            //
            //  Si la estructura es válida, construye un ÁRBOL DE PARSEO
            //  (Parse Tree) que representa la jerarquía del programa.
            //
            //  Ejemplo para "int x = 5 + 3;":
            //    programa
            //      sentencia
            //        declaracion
            //          tipo: INT
            //          ID: x
            //          expresion
            //            exprAditiva
            //              exprEntero: 5
            //              SUM
            //              exprEntero: 3
            // =========================================================

            System.out.println("\n=== FASE 2: ANÁLISIS SINTÁCTICO ===\n");

            // El parser necesita leer los tokens desde el principio.
            // reset() rebobina el stream al token 0.
            tokens.reset();

            MiLenguajeParser parser = new MiLenguajeParser(tokens);

            // Capturar errores sintácticos de forma personalizada
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

            // Ejecutar el parser desde la REGLA INICIAL 'programa'.
            // Esta llamada construye el árbol de parseo (o reporta errores).
            MiLenguajeParser.ProgramaContext arbolParseo = parser.programa();

            // Verificar si hubo errores
            if (!erroresSintacticos.isEmpty()) {
                System.out.println("  ❌ ERRORES SINTÁCTICOS:");
                for (String error : erroresSintacticos) {
                    System.out.println(error);
                }
                System.out.println();
                System.out.println("  Pista: revisa que cada sentencia:");
                System.out.println("    - Termine con punto y coma ';'");
                System.out.println("    - Tenga paréntesis balanceados");
                System.out.println("    - Use tipos válidos (int, float, string, bool, char, double)");
                return;
            }

            System.out.println("  ✅ Análisis sintáctico completado sin errores.");

            // =========================================================
            //  ÁRBOL DE PARSEO en formato LISP
            //
            //  ANTLR puede representar el árbol de parseo como texto
            //  usando la notación de listas anidadas de LISP.
            //
            //  Ejemplo: (programa (sentencia (declaracion int x = 10 ;)))
            //
            //  Cada paréntesis representa un nodo del árbol.
            //  El primer elemento es la regla, el resto son los hijos.
            // =========================================================

            System.out.println("\n--- Árbol de Parseo (formato LISP) ---\n");
            // toStringTree muestra el árbol usando los nombres de las reglas
            System.out.println("  " + arbolParseo.toStringTree(parser));

            // =========================================================
            //  RECORRIDO CON VISITOR
            //
            //  El patrón VISITOR permite "visitar" cada nodo del árbol
            //  de parseo y ejecutar código específico para cada tipo.
            //
            //  Aquí usamos ImprimirVisitor para mostrar la estructura
            //  del árbol de forma legible.
            //
            //  En compiladores reales, los visitors se usan para:
            //    - Construir el AST (Árbol Sintáctico Abstracto)
            //    - Hacer análisis semántico (tipos, ámbitos)
            //    - Generar código
            // =========================================================

            System.out.println("\n--- Árbol de Parseo (recorrido con Visitor) ---\n");
            ImprimirVisitor visitor = new ImprimirVisitor();
            visitor.visit(arbolParseo);

            System.out.println("\n" + "=".repeat(65));
            System.out.println("  Compilación exitosa.");

        } catch (IOException e) {
            System.err.println("❌ No se pudo leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
