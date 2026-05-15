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

## 5. Qué errores detecta el analizador semántico

El análisis semántico NO verifica estructura (eso ya lo hizo el parser).
Verifica que el programa tenga **sentido**: tipos correctos, variables declaradas, condiciones booleanas, etc.

---

### Uso de variables no declaradas

Si usás una variable que nunca fue declarada, el analizador lo detecta.

```cpp
int resultado = z + 1;   // ERROR: 'z' no existe
```
```
Error semántico: variable 'z' no fue declarada.
```

---

### Declarar la misma variable dos veces en el mismo bloque

No podés tener dos variables con el mismo nombre en el mismo ámbito.

```cpp
int x = 10;
int x = 20;   // ERROR: x ya existe
```
```
Error semántico: variable 'x' ya fue declarada en este ámbito.
```

Nota: sí es válido tener una variable `x` global y otra `x` dentro de un `if { }`, porque están en **ámbitos distintos**.

---

### Asignar un tipo incompatible

Cada variable tiene un tipo fijo. No podés guardar un texto en un `int`, ni un número en un `bool`.

```cpp
bool activo = 42;        // ERROR: 42 es int, no bool
string nombre = "Juan";
nombre = 99;             // ERROR: 99 es int, no string
```
```
Error semántico: no se puede asignar tipo 'int' a variable de tipo 'bool'.
Error semántico: no se puede asignar tipo 'int' a variable de tipo 'string'.
```

Lo que sí está permitido es asignar un `int` a un `float` o `double` (el número se amplía automáticamente):

```cpp
float f = 10;      // OK: int → float, se convierte solo
double d = 3.14;   // OK: mismo tipo
```

---

### Usar una variable sin inicializar

Si declarás una variable pero nunca le asignás un valor, el analizador avisa.

```cpp
int sinValor;
int uso = sinValor + 1;   // AVISO: sinValor podría no estar inicializada
```
```
Error semántico: variable 'sinValor' podría no estar inicializada.
```

---

### Operaciones aritméticas con tipos que no son números

Los operadores `+`, `-`, `*`, `/`, `%` solo funcionan con `int`, `float` o `double`.

```cpp
string texto = "hola";
int suma = texto + 5;   // ERROR: no tiene sentido sumar texto y número
```
```
Error semántico: el operador '+' no puede aplicarse a tipos 'string' e 'int'.
```

Cuando se mezclan tipos numéricos, el resultado es el tipo más preciso:
```cpp
int    a = 5;
double b = 3.14;
double c = a + b;   // OK: int + double = double
```

---

### Operadores lógicos con tipos que no son bool

Los operadores `&&`, `||`, `!` solo funcionan con valores `true`/`false`.

```cpp
int a = 5;
int b = 3;
bool cond = a && b;   // ERROR: && necesita dos bool, no dos int

int num = 5;
bool negado = !num;   // ERROR: ! necesita un bool, no un int
```
```
Error semántico: el operador '&&' no puede aplicarse a tipos 'int' e 'int'.
Error semántico: el operador '!' no puede aplicarse al tipo 'int'.
```

---

### Condición del `if` o `while` que no es booleana

La condición entre paréntesis **siempre debe ser `bool`**. Un número o texto no es una condición válida.

```cpp
int valor = 10;
if (valor) { ... }      // ERROR: valor es int, no bool

string s = "hola";
while (s) { ... }       // ERROR: s es string, no bool
```
```
Error semántico: la condición del 'if' debe ser bool, pero es 'int'.
Error semántico: la condición del 'while' debe ser bool, pero es 'string'.
```

Para corregirlo, usá una comparación que dé `bool`:
```cpp
if (valor > 0) { ... }     // OK: > produce bool
while (s != "") { ... }    // OK: != produce bool
```

---

### Errores en funciones

```cpp
// 'return' fuera de cualquier función
return 42;
// Error semántico: 'return' usado fuera de una función.

// Función declarada como 'int' pero retorna texto
int getDato() {
    return "hola";
}
// Error semántico: tipo de retorno 'string' no es compatible con 'int'.

// Función 'void' no puede retornar un valor
void nada() {
    return 1;
}
// Error semántico: función 'void' no puede retornar un valor.

// Función no-void que no retorna nada
int sinRetorno() {
    return;
}
// Error semántico: función de tipo 'int' debe retornar un valor.
```

---

### Ámbitos (scopes): variables que "viven" solo dentro de un bloque

Cada par de llaves `{ }` crea un ámbito nuevo. Las variables declaradas adentro **no existen fuera**.

```cpp
int x = 1;          // scope global

if (x > 0) {
    int local = 5;  // existe solo dentro de este if
    cout << local;  // OK
}

cout << local;      // ERROR: 'local' ya no existe aquí
```

Esto es correcto en el compilador porque la tabla de símbolos se "limpia" al salir de cada bloque.

---

## 6. Cómo se reportan los errores

### El analizador no se detiene al primer error

A diferencia de la fase léxica y sintáctica (que detienen el análisis cuando encuentran un error), el analizador semántico **acumula todos los errores** y los muestra al final.

Esto es útil para el programador: en lugar de corregir un error, compilar, ver el siguiente, compilar de nuevo... ve todos los errores de una sola vez.

Ejemplo con tres errores en el mismo archivo:
```
=== FASE 3: ANÁLISIS SEMÁNTICO ===

  ❌ ERRORES SEMÁNTICOS (3):

  [Línea 3:16]  Error semántico: variable 'z' no fue declarada.
  [Línea 7:4]   Error semántico: variable 'x' ya fue declarada en este ámbito.
  [Línea 11:5]  Error semántico: no se puede asignar tipo 'int' a variable de tipo 'bool'.
```

El formato de cada error muestra en qué línea y columna ocurre el problema.

---

### Por qué un error no genera errores "en cadena"

Cuando una expresión ya produjo un error, el compilador no genera errores adicionales por esa misma causa. Internamente, la expresión queda marcada con el tipo especial `ERROR`.

Ejemplo:
```cpp
string texto = "hola";
int suma = texto + 5;       // Error: no se puede sumar string + int
cout << suma + 1;           // suma está marcada como ERROR → no genera segundo error
```

Sin este mecanismo, el compilador generaría un error para cada uso de `suma`, llenando la pantalla de mensajes confusos. Con él, se reporta solo el error original.

---

## 7. Ejemplos de programas

### Programa correcto (`ejemplo_semantico.txt`)

```cpp
int    entero  = 42;
float  decimal = 3.14;
bool   activo  = true;
string nombre  = "Ana";

// int → float está permitido (ampliación numérica)
float f = 10;

// Operaciones aritméticas
int suma      = entero + 5;
double mezcla = entero + 3.14;   // int + double = double

// El if necesita una condición bool: entero > 10 produce bool
if (entero > 10) {
    cout << entero;
}

// Operadores lógicos: && necesita dos bool
bool rango = entero > 0 && entero < 100;

// Variable local: 'local' solo existe dentro del if
if (entero > 0) {
    int local = entero * 2;
    cout << local;
}

// While con condición booleana
int contador = 0;
while (contador < 10) {
    contador = contador + 1;
}

// Función: parámetros, cuerpo con return compatible
int sumar(int a, int b) {
    int c = a + b;
    return c;
}

// Función void: puede hacer return; sin valor
void imprimir(string msg) {
    cout << msg;
    return;
}
```

### Programa con errores (`ejemplo_semantico_error.txt`)

Ejecutá con `java -jar target/demo-1.0-jar-with-dependencies.jar ejemplo_semantico_error.txt` y vas a ver los 14 errores acumulados.

---

## 8. Construcciones del lenguaje

### Qué acepta el compilador

| Construcción | Ejemplo |
|---|---|
| Declaración de variable | `int x = 10;` |
| Declaración sin valor | `int x;` |
| Asignación | `x = x + 1;` |
| Salida por pantalla | `cout << x;` |
| Condicional | `if (x > 0) { ... } else { ... }` |
| Bucle | `while (x < 100) { ... }` |
| Función | `int sumar(int a, int b) { return a + b; }` |
| Retorno | `return expr;` o `return;` (para void) |

### Tipos de datos

| Tipo | Qué guarda | Ejemplo de declaración |
|---|---|---|
| `int` | Número entero | `int edad = 25;` |
| `float` | Número decimal | `float precio = 9.99;` |
| `double` | Decimal de alta precisión | `double pi = 3.14159265;` |
| `char` | Un solo carácter | `char letra = 'A';` |
| `string` | Texto | `string nombre = "Juan";` |
| `bool` | Verdadero o falso | `bool activo = true;` |
| `void` | Sin valor de retorno | Solo para funciones |

### Precedencia de operadores

Los operadores se evalúan en este orden (de menor a mayor prioridad):

```
||          → se evalúa último
&&
== !=
< > <= >=
+ -
* / %
! -(unario) → se evalúa primero
```

Esto significa que `2 + 3 * 4` es `14` (no `20`), porque `*` tiene mayor prioridad que `+`.
Y que `a || b && c` es `a || (b && c)` (no `(a || b) && c`).

### Errores sintácticos comunes

```cpp
int x = 10        // Falta el ';' al final
if (x > 0 {       // Falta el ')' antes de '{'
int z = x + ;     // La expresión queda incompleta
entero a = 5;     // 'entero' no es un tipo válido
```

---

## 9. Cómo funciona el Visitor

El analizador semántico usa el **patrón Visitor**: un objeto que recorre el árbol de parseo nodo por nodo y hace algo en cada uno.

Funciona así: ANTLR4 construye el árbol del programa. El `SemanticAnalyzer` lo recorre, y por cada tipo de nodo llama al método correspondiente.

```
Árbol de parseo:
    declaracion
    ├── tipo: "int"
    ├── ID:   "x"
    └── expresion: 5 + 3
          ├── expresion: 5   (tipo: int)
          └── expresion: 3   (tipo: int)

SemanticAnalyzer recorre el árbol:
    visitDeclaracion()
        → visita la expresion → devuelve "int"
        → verifica: ¿"int" es compatible con "int"? → SI
        → agrega x a la tabla de símbolos
```

Convención del proyecto:
- Los métodos que visitan **sentencias** (`visitDeclaracion`, `visitSentenciaIf`, etc.) devuelven `null` — las sentencias no tienen tipo.
- Los métodos que visitan **expresiones** (`visitExprAditiva`, `visitExprIdentificador`, etc.) devuelven el tipo resultante como `String` — por ejemplo `"int"`, `"bool"`, `"double"`.

---

## 10. Posibles mejoras

Estas funcionalidades no están implementadas y pueden ser un ejercicio de extensión:

**Sintaxis nueva**
- Sentencia `for`: `for (int i = 0; i < 10; i = i + 1) { ... }`
- Llamada a funciones: `int r = sumar(3, 4);`
- Arrays: `int arr[5];` y acceso `arr[i]`
- Operador ternario: `int max = a > b ? a : b;`

**Semántica más avanzada**
- Detectar código inalcanzable después de un `return`
- Constantes que no pueden reasignarse: `const int MAX = 100;`
- Verificar que toda rama de una función con tipo de retorno efectivamente retorne un valor

**Generación de código**
- Traducir el árbol a instrucciones de tres direcciones: `t1 = a + b`
- Generar bytecode para una máquina virtual simple

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
| Función | `tipo ID(tipo ID, ...) { ... }` |
| Return | `return expr;` / `return;` |
| Tipos | `int float double char string bool void` |
| Literales | `42` `3.14` `'A'` `"hola"` `true` `false` |
| Aritmética | `+ - * / %` |
| Comparación | `== != > < >= <=` |
| Lógicos | `&& \|\| !` |
