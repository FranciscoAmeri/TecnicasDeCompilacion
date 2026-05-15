# Compilador Educativo — Mini Lenguaje C++

Proyecto de Técnicas de Compilación.  
Implementa las **tres primeras fases** de un compilador: análisis léxico, análisis sintáctico y análisis semántico.

---

## Estructura del proyecto

```
demo/
├── src/main/antlr4/com/compilador/
│   └── MiLenguaje.g4                   <- gramática ANTLR4 (lexer + parser)
├── src/main/java/com/compilador/
│   ├── App.java                         <- punto de entrada (3 fases)
│   └── semantico/
│       ├── SemanticAnalyzer.java        <- visitor de análisis semántico
│       ├── SymbolTable.java             <- tabla de símbolos con scopes
│       ├── Scope.java                   <- un ámbito de nombres
│       ├── Symbol.java                  <- un símbolo (variable)
│       ├── TypeSystem.java              <- reglas de compatibilidad de tipos
│       └── SemanticError.java           <- registro de error semántico
├── ejemplo.txt                          <- programa válido (sintáctico)
├── ejemplo_error.txt                    <- programa con errores sintácticos
├── ejemplo_semantico.txt                <- programa válido (semántico)
├── ejemplo_semantico_error.txt          <- programa con 10 errores semánticos
└── pom.xml                              <- configuración Maven
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

# 2. Ejecutar con el programa semántico válido
java -jar target/demo-1.0-jar-with-dependencies.jar ejemplo_semantico.txt

# 3. Ejecutar con el programa con errores semánticos
java -jar target/demo-1.0-jar-with-dependencies.jar ejemplo_semantico_error.txt

# 4. Ejecutar con programas de prueba sintáctica
java -jar target/demo-1.0-jar-with-dependencies.jar ejemplo.txt
java -jar target/demo-1.0-jar-with-dependencies.jar ejemplo_error.txt
```

---

## Salida del programa

Al ejecutarse con un archivo válido, el compilador produce:

```
Analizando archivo: ejemplo_semantico.txt
=================================================================

=== FASE 1: ANÁLISIS LÉXICO ===

  TIPO DE TOKEN        LEXEMA                    LÍNEA    COLUMNA
  ---------------------------------------------------------------
  INT                  int                       1        0
  ID                   entero                    1        4
  IGUAL                =                         1        11
  INTEGER              42                        1        13
  PYC                  ;                         1        15
  ...

  Análisis léxico completado sin errores.

=== FASE 2: ANÁLISIS SINTÁCTICO ===

  Análisis sintáctico completado sin errores.

=== FASE 3: ANÁLISIS SEMÁNTICO ===

  Análisis semántico completado sin errores.

  === TABLA DE SÍMBOLOS ===

  Scope: global
  +------------------+--------+-----------+-------------+
  | Nombre           | Tipo   | Inicializ | Línea       |
  +------------------+--------+-----------+-------------+
  | entero           | int    | SI        | 1:4         |
  | decimal          | float  | SI        | 2:7         |
  | ...                                                  |
  +------------------+--------+-----------+-------------+

=================================================================
  Compilacion exitosa.

  Abriendo visualizador grafico del arbol...
```

Se abre además una **ventana gráfica** (Swing) con el árbol de parseo completo, navegable con zoom y scroll.

---

---

# Guía educativa — Tres Fases de Análisis

---

## 1. Introducción

Un compilador convierte código fuente en código ejecutable a través de varias fases. Este proyecto implementa las tres primeras:

```
Código fuente (.txt)
       |
       v
+-------------+     tokens      +--------------+     árbol       +--------------+
|  FASE 1     | --------------> |   FASE 2     | -------------> |   FASE 3     |
|  LÉXICA     |                 |  SINTÁCTICA  |                |  SEMÁNTICA   |
+-------------+                 +--------------+                +--------------+
  Lexer                           Parser                          Visitor
  MiLenguajeLexer                 MiLenguajeParser                SemanticAnalyzer
```

| Fase | Pregunta que responde | Qué detecta |
|------|----------------------|-------------|
| Léxica | ¿Son válidos los caracteres? | `@`, `#`, caracteres no reconocidos |
| Sintáctica | ¿Es válida la estructura? | `;` faltante, `{` sin cerrar, expresión incompleta |
| Semántica | ¿Tiene sentido el programa? | Variable no declarada, tipos incompatibles, condición no booleana |

---

## 2. Arquitectura del proyecto

### Paquete principal (`com.compilador`)

- **`App.java`** — Punto de entrada. Orquesta las tres fases secuencialmente. Si una fase falla, las siguientes no se ejecutan (excepto la semántica, que acumula errores).
- **`MiLenguaje.g4`** — Gramática que define tanto el lexer como el parser.

### Paquete semántico (`com.compilador.semantico`)

```
SemanticAnalyzer          <- Visitor que recorre el árbol de parseo
      |
      +--- SymbolTable    <- Gestiona el stack de scopes
              |
              +--- Scope  <- Un ámbito: mapa nombre -> Symbol
                    |
                    +--- Symbol    <- Un símbolo: nombre, tipo, inicializado
      |
      +--- TypeSystem     <- Reglas de compatibilidad de tipos
      |
      +--- SemanticError  <- Un error con línea, columna y mensaje
```

### Flujo de datos

```
programa                          SemanticAnalyzer.visit(árbol)
   |                                      |
   +--> visitDeclaracion()         SymbolTable.definir(Symbol)
   |         |                            |
   |         +--> visit(expresion)  TypeSystem.esCompatibleAsignacion()
   |
   +--> visitSentenciaIf()         TypeSystem.BOOL check en condición
   |         |
   |         +--> visitBloque()    SymbolTable.entrarScope / salirScope
   |
   +--> visitExprIdentificador()   SymbolTable.resolver() → tipo de la variable
```

---

## 3. Tabla de símbolos

La tabla de símbolos registra cada variable declarada y gestiona los **ámbitos** (scopes).

### Estructura de clases

#### `Symbol` — un símbolo individual

```java
Symbol {
    String   nombre       // "x"
    String   tipo         // "int", "float", "string", ...
    Categoria categoria   // VARIABLE, FUNCION, PARAMETRO
    boolean  inicializado // true si fue asignada algún valor
    int      linea        // línea de declaración
    int      columna      // columna de declaración
}
```

#### `Scope` — un ámbito de nombres

```java
Scope {
    String                     nombre  // "global", "bloque_1", "bloque_2", ...
    Scope                      padre   // scope que lo contiene (null en global)
    LinkedHashMap<String, Symbol> simbolos
}
```

- `definir(Symbol)` — agrega un símbolo; retorna `false` si ya existe localmente (redeclaración).
- `resolver(String)` — busca el nombre en este scope; si no lo encuentra, sube al padre. Así funciona la visibilidad de scopes.
- `estaDefinidoLocalmente(String)` — solo busca en el scope actual (para detectar redeclaraciones).

#### `SymbolTable` — gestión de la pila de scopes

```java
SymbolTable {
    Scope scopeActual   // siempre apunta al scope más interno
    List<Scope> historial  // todos los scopes creados (para imprimir la tabla)
}
```

Métodos principales:
- `entrarScope(String nombre)` — crea un nuevo scope hijo del actual.
- `salirScope()` — vuelve al scope padre.
- `definir(Symbol)` — agrega en el scope actual.
- `resolver(String)` — busca desde el scope actual hacia arriba.

### Ciclo de vida de un scope

```
visitBloque() → tabla.entrarScope("bloque")
    visitDeclaracion(int local = ...)  → tabla.definir(Symbol{"local","int"})
    visitExprIdentificador(local)      → tabla.resolver("local") → encontrado
tabla.salirScope()
// "local" ya no existe
visitExprIdentificador(local)         → tabla.resolver("local") → null → ERROR
```

---

## 4. Sistema de tipos

La clase `TypeSystem` centraliza **todas** las reglas de compatibilidad. Ninguna otra clase toma decisiones de tipos directamente.

### Tipos del lenguaje

| Tipo | Descripción | Ejemplo |
|------|-------------|---------|
| `int` | Número entero | `int x = 42;` |
| `float` | Número decimal | `float pi = 3.14;` |
| `double` | Decimal alta precisión | `double d = 2.718;` |
| `char` | Un carácter | `char c = 'A';` |
| `string` | Cadena de texto | `string s = "hola";` |
| `bool` | Booleano | `bool b = true;` |
| `void` | Sin valor (reservado para funciones) | — |
| `ERROR` | Sentinel interno — error ya reportado | — |

### Jerarquía numérica

```
int  <  float  <  double
```

Las operaciones entre numéricos retornan el tipo de mayor precisión:
- `int + int` → `int`
- `int + float` → `float`
- `float + double` → `double`

### Compatibilidad de asignación

| Variable (hacia) | Tipos aceptados (desde) |
|-----------------|------------------------|
| `int` | `int` |
| `float` | `int`, `float`, `double` (ampliación numérica) |
| `double` | `int`, `float`, `double` (ampliación numérica) |
| `char` | `char` |
| `string` | `string` |
| `bool` | `bool` |

### Reglas por operador

| Operador | Operandos requeridos | Resultado |
|----------|---------------------|-----------|
| `+`, `-`, `*`, `/`, `%` | Ambos numéricos | Tipo más preciso |
| `<`, `>`, `<=`, `>=` | Ambos numéricos | `bool` |
| `==`, `!=` | Mismo tipo o ambos numéricos | `bool` |
| `&&`, `\|\|` | Ambos `bool` | `bool` |
| `!` | `bool` | `bool` |
| `-` (unario) | Numérico | Mismo tipo |

### El tipo ERROR

`ERROR` es un centinela especial que se propaga sin generar errores adicionales.

```
string texto = "hola";
int suma = texto + 5;
         ^^^^^^^^^
         inferirAritmetico("string", "int") → ERROR
         → se reporta UN solo error, no uno por cada uso posterior de "suma"
```

---

## 5. Validaciones realizadas

### Variables

| Código | Nombre | Descripción | Ejemplo de error |
|--------|--------|-------------|-----------------|
| V1 | Variable no declarada | Se usa una variable que no fue declarada | `cout << z;` (z no existe) |
| V2 | Redeclaración | Se declara una variable que ya existe en el mismo scope | `int x = 1; int x = 2;` |
| V3 | Tipos incompatibles | El tipo de la expresión no es asignable al tipo de la variable | `bool activo = 42;` |
| V4 | Variable no inicializada | Se usa una variable declarada pero sin asignar valor | `int x; cout << x;` |

### Tipos

| Código | Nombre | Descripción |
|--------|--------|-------------|
| T1 | Aritmética inválida | Operandos de `+`, `-`, `*`, `/`, `%` no son numéricos |
| T2 | Lógica inválida | Operandos de `&&`, `\|\|`, `!` no son `bool` |
| T3 | Relacional inválida | Operandos de `<`, `>`, `<=`, `>=` no son numéricos |
| T4 | Igualdad inválida | Operandos de `==`, `!=` son de tipos incomparables |

### Control de flujo

| Código | Nombre | Descripción |
|--------|--------|-------------|
| C1 | Condición if no bool | La expresión del `if` no es booleana |
| C2 | Condición while no bool | La expresión del `while` no es booleana |

### Scopes

| Código | Nombre | Descripción |
|--------|--------|-------------|
| S1 | Scope por bloque | Cada `{ }` crea un ámbito nuevo |
| S2 | Visibilidad | Variables locales no son visibles fuera de su bloque |

---

## 6. Manejo de errores

### Acumulación de errores

A diferencia de los errores léxicos y sintácticos (que detienen el análisis), los errores semánticos se **acumulan**. El analizador siempre intenta continuar para reportar todos los errores en una sola pasada.

```java
// En SemanticAnalyzer, los errores se acumulan en una lista
private void error(Token token, String mensaje) {
    errores.add(new SemanticError(token.getLine(), token.getCharPositionInLine(), mensaje));
    // El análisis NO se detiene
}
```

### Formato de error

```
[Línea 15:13] Error semántico: variable 'z' no fue declarada.
[Línea 22:4]  Error semántico: variable 'x' ya fue declarada en este ámbito.
[Línea 28:5]  Error semántico: no se puede asignar tipo 'int' a variable de tipo 'bool'.
```

### Prevención de errores en cascada

Cuando una expresión ya falló, retorna el tipo centinela `ERROR`. Las operaciones que reciben `ERROR` como operando retornan `ERROR` inmediatamente **sin reportar un error nuevo**, evitando una cascada de falsos positivos.

```
int suma = texto + 5;  // texto es string
           ↑
           inferirAritmetico("string", "int")
           → ERROR: "el operador '+' no puede aplicarse a tipos 'string' e 'int'"
           ↓
           suma tiene tipo ERROR
           ↓
           Si luego se usa suma en otra operación:
           inferirAritmetico("ERROR", "int") → ERROR (sin nuevo mensaje)
```

---

## 7. Ejemplos prácticos

### Programa válido (`ejemplo_semantico.txt`)

```cpp
// Tipos básicos bien asignados
int    entero   = 42;
float  decimal  = 3.14;
double preciso  = 2.718281828;
bool   activo   = true;
string nombre   = "Ana";
char   inicial  = 'A';

// Ampliación numérica válida: int puede asignarse a float o double
float  f = 10;
double d = 3.14;

// Operaciones aritméticas válidas
int suma      = entero + 5;
double mezcla = entero + preciso;  // int + double = double

// Condición booleana correcta
if (entero > 10) {
    cout << entero;
}

// Operadores lógicos con bool
bool rango = entero > 0 && entero < 100;

// Scope: variable local solo visible dentro del bloque
if (entero > 0) {
    int local = entero * 2;
    cout << local;
}
// Aquí 'local' ya no existe

// While con condición booleana
int contador = 0;
while (contador < 10) {
    contador = contador + 1;
}

// NOT lógico
bool inactivo = !activo;
```

### Programa con errores (`ejemplo_semantico_error.txt`)

```cpp
// ERROR 1 — Variable no declarada
// 'z' no existe en ningún scope
int resultado = z + 1;

// ERROR 2 — Redeclaración en el mismo scope
int x = 10;
int x = 20;    // x ya fue declarada

// ERROR 3 — Tipo incompatible en declaración
// bool no acepta int
bool activo = 42;

// ERROR 4 — Tipo incompatible en asignación
string nombre = "Juan";
nombre = 99;   // string no acepta int

// ERROR 5 — Aritmética con string
string texto = "hola";
int suma = texto + 5;  // string + int no es válido

// ERROR 6 — Condición del if no es bool
int valor = 10;
if (valor) {   // se necesita bool, no int
    cout << valor;
}

// ERROR 7 — Condición del while no es bool
string s = "hola";
while (s) {    // se necesita bool, no string
    cout << s;
}

// ERROR 8 — Operador lógico con int en lugar de bool
int a = 5;
int b = 3;
bool cond = a && b;   // && requiere bool && bool

// ERROR 9 — Variable sin inicializar
int sinValor;
int uso = sinValor + 1;   // sinValor podría no estar inicializada

// ERROR 10 — NOT sobre tipo no booleano
int num = 5;
bool negado = !num;   // ! requiere bool
```

Salida esperada:
```
=== FASE 3: ANÁLISIS SEMÁNTICO ===

  ❌ ERRORES SEMÁNTICOS (10):

  [Línea 15:16] Error semántico: variable 'z' no fue declarada.
  [Línea 22:4]  Error semántico: variable 'x' ya fue declarada en este ámbito.
  [Línea 28:5]  Error semántico: no se puede asignar tipo 'int' a variable de tipo 'bool' en la declaración de 'activo'.
  [Línea 35:7]  Error semántico: no se puede asignar tipo 'int' a variable de tipo 'string' al asignar a 'nombre'.
  [Línea 42:11] Error semántico: el operador '+' no puede aplicarse a tipos 'string' e 'int'.
  [Línea 49:4]  Error semántico: la condición del 'if' debe ser bool, pero es 'int'.
  [Línea 58:7]  Error semántico: la condición del 'while' debe ser bool, pero es 'string'.
  [Línea 67:16] Error semántico: el operador '&&' no puede aplicarse a tipos 'int' e 'int'.
  [Línea 75:10] Error semántico: variable 'sinValor' podría no estar inicializada.
  [Línea 82:13] Error semántico: el operador '!' no puede aplicarse al tipo 'int'.
```

---

## 8. Análisis sintáctico — reglas de la gramática

### Qué acepta el parser

| Construcción | Ejemplo |
|---|---|
| Declaración | `int x = 10;` |
| Asignación | `x = x + 1;` |
| Salida | `cout << x;` |
| Condicional | `if (x > 0) { ... } else { ... }` |
| Bucle | `while (x < 100) { ... }` |
| Tipos | `int float double char string bool` |

### Precedencia de operadores (menor a mayor)

```
||                 (OR lógico)
&&                 (AND lógico)
== !=              (igualdad)
< > <= >=          (relacional)
+ -                (suma, resta)
* / %              (multiplicación, división, módulo)
! -(unario)        (unarios, mayor precedencia)
( expr )           (agrupación)
literal / ID       (átomos)
```

La precedencia se implementa por **orden de alternativas** en la regla `expresion` de ANTLR4: las alternativas listadas primero tienen menor precedencia.

### Ejemplos de errores sintácticos

```cpp
int x = 10      // Error: falta ';'
if (x > 0 {     // Error: falta ')'
int z = x + ;   // Error: expresión incompleta
entero a = 5;   // Error: 'entero' no es un tipo válido
```

---

## 9. El patrón Visitor

ANTLR4 genera la interfaz `MiLenguajeVisitor<T>`. El analizador semántico extiende `MiLenguajeBaseVisitor<String>`, donde el tipo `String` representa el **tipo inferido** de cada expresión.

```java
public class SemanticAnalyzer extends MiLenguajeBaseVisitor<String> {

    @Override
    public String visitDeclaracion(MiLenguajeParser.DeclaracionContext ctx) {
        String tipo   = ctx.tipo().getText();    // "int"
        String nombre = ctx.ID().getText();       // "x"
        String tipoExpr = visit(ctx.expresion()); // tipo de la parte derecha
        // ... validar y registrar en tabla de símbolos
        return null; // las sentencias no tienen tipo
    }

    @Override
    public String visitExprAditiva(MiLenguajeParser.ExprAditivaContext ctx) {
        String izq = visit(ctx.expresion(0));  // tipo del operando izquierdo
        String der = visit(ctx.expresion(1));  // tipo del operando derecho
        return TypeSystem.inferirAritmetico(izq, der); // tipo del resultado
    }
}
```

Convención de retorno:
- **Sentencias** (`visitDeclaracion`, `visitSentenciaIf`, etc.) → retornan `null`
- **Expresiones** (`visitExprAditiva`, `visitExprIdentificador`, etc.) → retornan el tipo inferido

---

## 10. Posibles mejoras futuras

### Sintaxis adicional
- [ ] Sentencia `for`: `for (int i = 0; i < 10; i = i + 1) { ... }`
- [ ] Declaración y llamada de funciones con parámetros y tipo de retorno
- [ ] Arrays y acceso por índice: `arr[i]`
- [ ] Operador ternario: `x > 0 ? x : -x`
- [ ] `break` y `continue` dentro de bucles
- [ ] Operadores de incremento/decremento: `x++`, `x--`

### Análisis semántico avanzado
- [ ] Verificar que `return` sea compatible con el tipo de retorno de la función
- [ ] Control de flujo: detectar código inalcanzable después de `return`
- [ ] Inferencia de tipos para variables sin tipo explícito (`auto x = 5;`)
- [ ] Constantes (`const int MAX = 100;`) que no pueden reasignarse

### Árbol Sintáctico Abstracto (AST)
- [ ] Construir un AST separado del árbol de parseo
- [ ] El AST omite nodos no relevantes (paréntesis, puntos y coma, palabras clave)
- [ ] Imprimir el AST de forma estructurada

### Generación de código
- [ ] Código intermedio de tres direcciones: `t1 = x + 5`
- [ ] Bytecode para una máquina virtual simple
- [ ] Traducción a C o Java

### Optimizaciones
- [ ] Plegado de constantes: `2 + 3` → `5` en tiempo de compilación
- [ ] Eliminación de código muerto
- [ ] Propagación de constantes

---

## Referencia rápida

| Construcción | Sintaxis |
|---|---|
| Declaración | `tipo ID = expr;` o `tipo ID;` |
| Asignación | `ID = expr;` |
| Salida | `cout << expr;` |
| If | `if (expr_bool) { ... }` |
| If-Else | `if (expr_bool) { ... } else { ... }` |
| While | `while (expr_bool) { ... }` |
| Bloque | `{ sentencia* }` |
| Tipos | `int float double char string bool` |
| Literales | `42` `3.14` `'A'` `"hola"` `true` `false` |
| Aritmética | `+ - * / %` |
| Comparación | `== != > < >= <=` |
| Lógicos | `&& \|\| !` |
