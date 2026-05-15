# Compilador Educativo — Mini Lenguaje C++

Proyecto de Técnicas de Compilación.  
Implementa las dos primeras fases de un compilador: **análisis léxico** y **análisis sintáctico**.

---

## Estructura del proyecto

```
demo/
├── src/main/antlr4/com/compilador/
│   └── MiLenguaje.g4          <- gramática ANTLR4 (lexer + parser)
├── src/main/java/com/compilador/
│   ├── App.java                <- punto de entrada
│   └── ImprimirVisitor.java    <- visitor educativo
├── ejemplo.txt                 <- programa válido de prueba
├── ejemplo_error.txt           <- programa con errores sintácticos
└── pom.xml                     <- configuración Maven
```

Archivos **generados automáticamente** por ANTLR4 (no editarlos):
```
src/main/java/com/compilador/
├── MiLenguajeLexer.java
├── MiLenguajeParser.java
├── MiLenguajeVisitor.java
└── MiLenguajeBaseVisitor.java
```

---

## Cómo compilar y ejecutar

```bash
# 1. Compilar todo (genera el lexer/parser de ANTLR y compila Java)
cd demo
mvn clean package

# 2. Ejecutar con el programa de ejemplo válido
java -jar target/demo-1.0-jar-with-dependencies.jar ejemplo.txt

# 3. Ejecutar con el programa de ejemplo con errores
java -jar target/demo-1.0-jar-with-dependencies.jar ejemplo_error.txt
```

---

## Salida del programa

Al ejecutarse con un archivo válido, el programa produce:

### En consola

```
Analizando archivo: ejemplo.txt
=================================================================

=== FASE 1: ANÁLISIS LÉXICO ===

  TIPO DE TOKEN        LEXEMA                    LÍNEA    COLUMNA
  ---------------------------------------------------------------
  INT                  int                       1        0
  ID                   x                         1        4
  IGUAL                =                         1        6
  INTEGER              10                        1        8
  PYC                  ;                         1        10
  ...

  Análisis léxico completado sin errores.

=== FASE 2: ANÁLISIS SINTÁCTICO ===

  Análisis sintáctico completado sin errores.

=================================================================
  Compilacion exitosa.

  Abriendo visualizador grafico del arbol...
```

### Ventana gráfica (Swing)

Se abre automáticamente una ventana con el árbol de parseo completo, generada con `TreeViewer` de ANTLR4.

```
+---------------------------+
|  Árbol de Parseo          |
|                           |
|       programa            |
|      /   |   \            |
| decl  asig  while         |
|  ...   ...   ...          |
+---------------------------+
```

La ventana soporta zoom y scroll para navegar árboles grandes.

---

---

# Guía educativa — Análisis Sintáctico

---

## 1. ¿Qué es el análisis sintáctico?

El **análisis léxico** convierte el texto en tokens (palabras).  
El **análisis sintáctico** verifica que esas palabras forman **frases con sentido**.

Analogía con el lenguaje natural:

```
Frase:  "El gato come pescado"   -> estructura válida
Frase:  "Gato el pescado come"   -> palabras correctas, estructura inválida
```

En programación:

```
int x = 5;          -> tokens válidos, estructura válida      OK
int = x 5;          -> tokens válidos, estructura inválida    ERROR
```

El análisis sintáctico NO verifica el significado (eso es análisis semántico).  
Solo verifica que la **estructura** respeta las reglas de la gramática.

---

## 2. ¿Qué hace esta gramática?

Este proyecto implementa un **mini lenguaje inspirado en C++** con:

| Construcción         | Ejemplo                           |
|----------------------|-----------------------------------|
| Declaración          | `int x = 10;`                     |
| Asignación           | `x = x + 1;`                      |
| Salida               | `cout << x;`                      |
| Condicional          | `if (x > 0) { ... } else { ... }` |
| Bucle                | `while (x < 100) { ... }`         |
| Expresiones          | `(x + y) * 2`                     |
| Tipos                | `int float double char string bool`|

### Programa de ejemplo

```cpp
int x = 10;
int y = 20;

x = x + y;

if (x > 10) {
    cout << x;
}

while (x < 100) {
    x = x + 1;
}
```

---

## 3. Explicación de las reglas de la gramática

El archivo `MiLenguaje.g4` contiene dos tipos de reglas:

- **Reglas del parser** (en minúscula): definen la estructura
- **Reglas del lexer** (en MAYÚSCULA): definen los tokens

### 3.1 Regla `programa`

```antlr4
programa : sentencia* EOF ;
```

Un programa es **cero o más sentencias** seguidas del fin del archivo.  
El `*` significa "cero o más veces" (como las expresiones regulares).

### 3.2 Regla `sentencia`

```antlr4
sentencia
    : declaracion
    | asignacion
    | sentenciaCout
    | sentenciaIf
    | sentenciaWhile
    | bloque
    ;
```

El `|` significa **alternativa** (OR). Una sentencia puede ser cualquiera de esos tipos.  
ANTLR prueba cada alternativa en orden hasta encontrar una que coincida.

### 3.3 Regla `declaracion`

```antlr4
declaracion : tipo ID (IGUAL expresion)? PYC ;
```

- `tipo` → palabra clave de tipo (`int`, `float`, etc.)
- `ID` → nombre de la variable
- `(IGUAL expresion)?` → valor inicial **opcional** (el `?` significa 0 o 1 vez)
- `PYC` → punto y coma obligatorio

Ejemplos válidos:
```cpp
int x;          // sin valor inicial
int x = 5;      // con valor inicial
float pi = 3.14;
```

### 3.4 Regla `expresion` y precedencia de operadores

```antlr4
expresion
    : expresion OR expresion            // Nivel 6: menor precedencia
    | expresion AND expresion           // Nivel 5
    | expresion (EQL | DISTINTO) expresion   // Nivel 4
    | expresion (MAYOR | MENOR | ...) expresion  // Nivel 3
    | expresion (SUM | RES) expresion   // Nivel 2
    | expresion (MUL | DIV | MOD) expresion  // Nivel 1
    | NOT expresion                     // Unarios
    | RES expresion
    | PA expresion PC                   // Paréntesis
    | INTEGER                           // Literales
    | ID                                // Variables
    ;
```

**La precedencia en ANTLR4 se define por el ORDEN:**  
Las alternativas más arriba tienen **menor precedencia** (se evalúan al final).  
Las más abajo tienen **mayor precedencia** (se evalúan primero).

Esto asegura que `2 + 3 * 4` se parsee como `2 + (3 * 4) = 14` y no como `(2 + 3) * 4 = 20`.

---

## 4. Cómo funciona

### El flujo de análisis

```
Texto fuente
     |
     v
+---------+       tokens        +---------+      árbol de parseo
|  LEXER  | ------------------> | PARSER  | -------------------->
+---------+                     +---------+           |
     |                               |                v
     | MiLenguajeLexer.java          | MiLenguajeParser.java
     | (generado por ANTLR4)         | (generado por ANTLR4)     TreeViewer (GUI)
```

### El Lexer

El **Lexer** (analizador léxico) lee el texto carácter por carácter y lo convierte en **tokens**.

```
"int x = 5 + 3 ;"
  ---  -   -  -  -  -   -
  INT  ID  =  5  +  3   ;

 -> [INT] [ID:"x"] [IGUAL] [INTEGER:"5"] [SUM] [INTEGER:"3"] [PYC]
```

**Reglas del lexer en ANTLR4:**
- Si dos reglas pueden coincidir, gana la que coincide con el texto **más largo**
- Si coinciden con el mismo largo, gana la que aparece **primero** en el archivo
- Por eso las palabras clave (`int`, `while`) van ANTES que `ID` en la gramática

### El Parser

El **Parser** (analizador sintáctico) toma la secuencia de tokens y verifica que forman una estructura válida según la gramática.

Si la estructura es válida, construye un **Árbol de Parseo** (Parse Tree).

### El Árbol de Parseo

El árbol tiene:
- **Nodos internos**: reglas del parser (como `programa`, `sentencia`, `expresion`)
- **Hojas**: tokens del lexer (como `INT`, `ID`, `INTEGER`, `PYC`)

El árbol se visualiza automáticamente en la **ventana gráfica** al ejecutar el compilador.

---

## 5. El patrón Visitor

ANTLR4 genera una interfaz `MiLenguajeVisitor<T>` con un método por cada regla.  
Extendemos `MiLenguajeBaseVisitor<T>` y sobreescribimos los métodos que nos interesan.

```java
public class MiVisitor extends MiLenguajeBaseVisitor<String> {

    @Override
    public String visitDeclaracion(MiLenguajeParser.DeclaracionContext ctx) {
        // ctx da acceso a todos los hijos del nodo
        String tipo  = ctx.tipo().getText();    // "int"
        String nombre = ctx.ID().getText();      // "x"
        // ...
        return visitChildren(ctx);
    }
}
```

El Visitor se usa para:
- **Imprimir** el árbol (como hace `ImprimirVisitor.java`)
- **Construir** un AST (Árbol Sintáctico Abstracto)
- **Analizar** tipos y ámbitos (análisis semántico)
- **Generar** código intermedio o final

---

## 6. Visualizador gráfico (TreeViewer)

El programa usa `org.antlr.v4.gui.TreeViewer` de ANTLR4 para mostrar el árbol en una ventana Swing.


El `TreeViewer` necesita:
- La lista de nombres de reglas del parser (para mostrar etiquetas como `declaracion`, `exprAditiva`)
- El árbol de parseo devuelto por `parser.programa()`


---

## 7. Ejemplos prácticos

### Programa válido

```cpp
int x = 10;
int y = 20;
x = x + y;

if (x > 10) {
    cout << x;
} else {
    cout << y;
}

while (x < 100) {
    x = x + 1;
}
```

### Programas inválidos y sus errores

```cpp
// Error 1: falta el ';'
int x = 10      // <- ERROR: missing ';' at 'int'
int y = 20;
```

```cpp
// Error 2: paréntesis sin cerrar en if
if (x > 0 {     // <- ERROR: missing ')' at '{'
    cout << x;
}
```

```cpp
// Error 3: expresión incompleta
int z = x + ;   // <- ERROR: mismatched input ';'
```

```cpp
// Error 4: tipo desconocido
entero a = 5;   // <- ERROR: mismatched input 'entero'
```

---

## 8. Qué queda por implementar

### Sintaxis adicional
- [ ] Sentencia `for` `while` `etc`  
- [ ] Declaración y llamada de funciones
- [ ] Arrays y acceso por índice (`arr[i]`)
- [ ] Operador ternario (`x > 0 ? x : -x`)


---

## 9. Ejercicios propuestos

### Nivel 1 — Familiarización

1. Ejecutá el compilador con `ejemplo.txt` y observá la consola y la ventana gráfica.
2. Introducí errores en `ejemplo.txt` (quitá un `;`, un `)`, una `}`) y observá los mensajes.
3. Agregá una variable `string saludo = "Hola mundo";` y verificá que compila.

### Nivel 2 — Modificar la gramática

4. **Agregá el tipo `long`** como tipo de dato válido (solo en `MiLenguaje.g4`).
5. **Agregá `cout <<` con múltiples valores** separados por `<<`:  
   `cout << x << y << z;`  
   Pista: modificá la regla `sentenciaCout`.
6. **Agregá el operador `+=`**:  
   `x += 5;` equivale a `x = x + 5;`

### Nivel 3 — Extender el lenguaje

7. **Agregá la sentencia `for`:**
   ```cpp
   for (int i = 0; i < 10; i = i + 1) {
       cout << i;
   }
   ```

8. **Agregá funciones sin parámetros:**
   ```cpp
   void saludar() {
       cout << "Hola";
   }
   ```


## 10. Preguntas de comprensión

1. ¿Cuál es la diferencia entre un **token** y una **regla de parser**?
2. ¿Por qué las palabras clave (`int`, `while`) deben estar ANTES que `ID` en el lexer?
3. ¿Cómo determina ANTLR4 la **precedencia de operadores** en la regla `expresion`?
4. ¿Qué haría el compilador con `int x = "hola";`? ¿Lo detectaría en esta fase?
