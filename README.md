# 🚀 Compilador TC25 - Proyecto de Técnicas de Compilación  

## 🏗️ Configuración Inicial del Proyecto  

### 📦 Creación del Proyecto Maven  

El primer paso para desarrollar nuestro **compilador TC25** es crear la estructura básica del proyecto usando **Maven**, que nos ayudará con la gestión de dependencias y el ciclo de vida de construcción.  

Ejecutamos el siguiente comando Maven:  

```bash
mvn org.apache.maven.plugins:maven-archetype-plugin:3.1.2:generate \
    -DarchetypeArtifactId="maven-archetype-quickstart" \
    -DarchetypeGroupId="org.apache.maven.archetypes" \
    -DarchetypeVersion="1.4" \
    -DgroupId="com.compilador" \
    -DartifactId="demo"
```

🔹 **Configuración durante la ejecución:**  
- **groupId**: `com.compilador`  
- **artifactId**: `demo`  
- **version**: `1.0`  
- **package**: `com.compilador`  

📂 **Estructura generada del proyecto:**  
```
demo/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── compilador/
│   │               └── App.java
│   └── test/
│       └── java/
│           └── com/
│               └── compilador/
│                   └── AppTest.java
```

---

## 🎭 Configuración de ANTLR para el Análisis Léxico  

Después de crear el proyecto básico, configuramos **ANTLR4** para implementar el **analizador léxico**.  

### ✍️ 1. Modificación del `pom.xml`  

Añadimos la dependencia de **ANTLR** y los plugins necesarios:  

```xml
<!-- 📌 Propiedades del proyecto -->
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>
  <antlr.version>4.9.3</antlr.version>
</properties>

<!-- 📌 Dependencias -->
<dependencies>
  <!-- ANTLR para análisis léxico y sintáctico -->
  <dependency>
    <groupId>org.antlr</groupId>
    <artifactId>antlr4-runtime</artifactId>
    <version>${antlr.version}</version>
  </dependency>
</dependencies>

<!-- 📌 Configuración de plugins -->
<build>
  <plugins>
    <!-- 🛠️ Plugin para ANTLR -->
    <plugin>
      <groupId>org.antlr</groupId>
      <artifactId>antlr4-maven-plugin</artifactId>
      <version>${antlr.version}</version>
      <executions>
        <execution>
          <goals>
            <goal>antlr4</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <sourceDirectory>${basedir}/src/main/antlr4</sourceDirectory>
        <outputDirectory>${basedir}/src/main/java</outputDirectory>
        <visitor>true</visitor>
        <listener>true</listener>
      </configuration>
    </plugin>
    
    <!-- 📦 Plugin para crear un JAR ejecutable -->
    <plugin>
      <artifactId>maven-assembly-plugin</artifactId>
      <configuration>
        <archive>
          <manifest>
            <mainClass>com.compilador.App</mainClass>
          </manifest>
        </archive>
        <descriptorRefs>
          <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
      </configuration>
      <executions>
        <execution>
          <id>make-assembly</id>
          <phase>package</phase>
          <goals>
            <goal>single</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

---

## 🗂️ Configuración de ANTLR para el Análisis Léxico  

### 📂 2. Creación de la Estructura de Directorios  

Ejecutamos el siguiente comando para crear el directorio donde residirán nuestros archivos de gramática **ANTLR**:  

```bash
mkdir -p src/main/antlr4/com/compilador
```

---

### 📝 3. Creación del Archivo de Gramática ANTLR  

📌 Creamos el archivo **MiniLenguaje.g4** en `src/main/antlr4/com/compilador/` con las reglas léxicas y sintácticas básicas:  

```antlr
grammar MiniLenguaje;

// 📌 Reglas del parser
program : token* EOF ;
token   : ID | INTEGER | STRING | KEYWORD | OPERATOR | SEPARATOR ;

// 📌 Reglas léxicas

// 🆔 Identificadores
ID          : [a-zA-Z][a-zA-Z0-9_]* ;

// 🔢 Literales
INTEGER     : [0-9]+ ;
STRING      : '"' (~["\r\n] | '\"')* '"' ;
BOOLEAN     : 'true' | 'false' ;

// 🏷️ Palabras clave
KEYWORD     : 'var' | 'if' | 'else' | 'print' | 'while' | 'function' | 'return' ;

// 🔣 Operadores
OPERATOR    : '+' | '-' | '*' | '/' | '%' | '=' | '==' | '!=' | '<' | '>' | '<=' | '>=' | '&&' | '||' | '!' ;

// ✨ Separadores
SEPARATOR   : ';' | '(' | ')' | '{' | '}' | ',' | '.' ;

// 🧹 Ignorar espacios en blanco y comentarios
WS          : [ \t\r\n]+ -> skip ;
COMMENT     : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
```

---

## 🎯 ¡Listo para Empezar!  

Con esta configuración ya podemos **compilar y probar nuestro compilador TC25** usando **ANTLR4 y Maven**. Ahora podemos comenzar a definir más reglas y construir el análisis semántico.  

🔥 **¡Hora de compilar y ejecutar!**  
```bash
mvn clean package
```

🚀 **¿Próximo paso?** Implementar la generación de código 🛠️✨  

---
