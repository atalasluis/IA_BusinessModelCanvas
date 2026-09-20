# Proyecto: Generador Inteligente de Business Model Canvas

## 1. Descripción del proyecto

Aplicación web que recibe de un usuario el contexto de una idea, negocio, producto, servicio o problema junto con parámetros adicionales, utiliza Gemini mediante una API y una base de conocimiento especializada mediante RAG, y genera primero un análisis basado en Jobs to Be Done (JTBD) para después construir un Business Model Canvas (BMC).

La versión 1 se enfocará en demostrar el flujo principal de extremo a extremo, sin cuentas de usuario ni una base de datos tradicional.

## 2. Objetivo de la versión 1

Construir un prototipo web funcional capaz de:

1. Recibir el contexto proporcionado por el usuario.
2. Recibir parámetros adicionales.
3. Recuperar información relevante desde una fuente de conocimiento especializada.
4. Enviar a Gemini el contexto del usuario junto con el conocimiento recuperado.
5. Hacer que Gemini analice el problema mediante Jobs to Be Done.
6. Generar los 9 bloques del Business Model Canvas.
7. Devolver el resultado al backend.
8. Mostrar el resultado en la interfaz web.

## 3. Flujo funcional

```text
USUARIO
   |
   v
Página Web
   |
   | Contexto + parámetros
   v
Backend Java / Spring Boot
   |
   v
RAG
   |
   | Recuperación de conocimiento relevante
   v
Gemini API
   |
   | Análisis JTBD
   |       |
   |       v
   |  Necesidades / dolores / beneficios
   |       |
   |       v
   |  Propuesta de valor
   |       |
   |       v
   |  Business Model Canvas
   |
   v
Backend Java
   |
   v
Página Web
   |
   +--> Resultado JTBD
   |
   +--> Business Model Canvas
```

## 4. Arquitectura de la versión 1

```text
+---------------------------+
|       Usuario             |
+-------------+-------------+
              |
              v
+---------------------------+
|      Frontend Web         |
|  HTML/CSS/JavaScript      |
|  o React                  |
+-------------+-------------+
              |
              | HTTP/REST
              v
+---------------------------+
|      Backend              |
| Java + Spring Boot        |
+-------------+-------------+
              |
              +----------------------+
              |                      |
              v                      v
+----------------------+   +----------------------+
| Sistema RAG          |   | Gemini API           |
|                      |   |                      |
| Documentos BMC       |   | Generación/análisis  |
| Documentos JTBD      |   |                      |
+----------------------+   +----------------------+
              |
              v
       Conocimiento
       recuperado
              |
              +-----------> Gemini
```

## 5. Tecnologías definidas

### Backend

- Java
- Spring Boot
- API REST
- Maven o Gradle (se decidirá al iniciar el proyecto)

### Frontend

Para V1 se puede comenzar con:

- HTML
- CSS
- JavaScript

React queda como alternativa si durante el desarrollo se determina que aporta una ventaja clara para la interfaz.

### Inteligencia artificial

- Google Gemini API
- Gemini será responsable del análisis y generación del contenido.
- El backend será responsable de controlar la entrada, preparar el contexto y procesar la respuesta.

### Base de conocimiento

Se utilizarán documentos especializados sobre:

- Business Model Canvas
- Jobs to Be Done
- Conceptos relacionados con propuesta de valor
- Conceptos relacionados con necesidades, dolores y beneficios

Los documentos podrán estar inicialmente organizados como archivos, por ejemplo:

```text
knowledge/
│
├── libros/
│   ├── libro-1.pdf
│   ├── libro-2.pdf
│   └── ...
│
├── bmc/
│   ├── business-model-canvas.md
│   ├── segmentos-clientes.md
│   ├── propuesta-valor.md
│   ├── canales.md
│   ├── relaciones-clientes.md
│   ├── fuentes-ingresos.md
│   ├── recursos-clave.md
│   ├── actividades-clave.md
│   ├── socios-clave.md
│   └── estructura-costos.md
│
└── jtbd/
    ├── introduccion-jtbd.md
    ├── job-funcional.md
    ├── job-emocional.md
    ├── job-social.md
    ├── customer-profile.md
    └── value-map.md
```

## 6. RAG

La versión 1 utilizará Retrieval-Augmented Generation (RAG).

El objetivo de RAG será evitar depender únicamente del conocimiento general de Gemini.

Flujo:

```text
Documentos
    |
    v
Procesamiento
    |
    v
Fragmentación del contenido
    |
    v
Embeddings
    |
    v
Almacenamiento/búsqueda vectorial
    |
    v
Pregunta/contexto del usuario
    |
    v
Recuperación de fragmentos relevantes
    |
    v
Contexto para Gemini
```

### Importante

RAG no significa entrenar Gemini.

En este proyecto se utilizará conocimiento externo recuperado desde documentos para proporcionar contexto relevante a Gemini en cada generación.

No se plantea entrenar un modelo desde cero.

## 7. Fuentes de conocimiento

La fuente de conocimiento deberá utilizar materiales apropiados y autorizados para este propósito.

Categorías iniciales:

### Business Model Canvas

Información sobre:

- Los 9 bloques del BMC
- Relaciones entre los bloques
- Propuesta de valor
- Segmentos de clientes
- Canales
- Relaciones con clientes
- Fuentes de ingresos
- Recursos clave
- Actividades clave
- Alianzas clave
- Estructura de costos

### Jobs to Be Done

Información sobre:

- Job funcional
- Job social
- Job emocional
- Contexto del usuario
- Necesidades
- Dolores
- Beneficios
- Resultados deseados
- Relación entre JTBD y propuesta de valor

## 8. Entrada del usuario

La interfaz V1 tendrá dos campos principales.

### Campo 1: Contexto

Permitirá describir:

- Idea de negocio
- Producto
- Servicio
- Problema
- Situación
- Público conocido
- Contexto general

Ejemplo:

```text
Quiero crear una aplicación para estudiantes universitarios
que tienen dificultades para organizar sus proyectos y trabajos.
```

### Campo 2: Parámetros

Permitirá agregar restricciones o información adicional.

Ejemplos:

- Público objetivo
- Ubicación
- Presupuesto
- Tipo de negocio
- Restricciones
- Recursos disponibles
- Características deseadas
- Mercado
- Otros datos relevantes

## 9. Proceso de generación

El backend deberá preparar una solicitud estructurada para Gemini.

Conceptualmente:

```text
CONTEXTO DEL USUARIO
+
PARÁMETROS
+
CONOCIMIENTO RECUPERADO MEDIANTE RAG
+
INSTRUCCIONES DEL SISTEMA
        |
        v
      GEMINI
        |
        v
ANÁLISIS JTBD
        |
        v
BUSINESS MODEL CANVAS
```

## 10. Análisis JTBD

Antes de generar el BMC, Gemini deberá analizar el problema mediante Jobs to Be Done.

La salida esperada podrá incluir:

### Job principal

Qué intenta conseguir el usuario.

### Job funcional

Qué tarea necesita realizar.

### Job social

Cómo quiere ser percibido o qué resultado social busca.

### Job emocional

Qué experiencia o estado emocional busca.

### Necesidades

Qué necesita para conseguir el resultado.

### Dolores

Qué dificultades, obstáculos o frustraciones existen.

### Beneficios

Qué resultados positivos espera obtener.

### Resultado esperado

Qué debería conseguir finalmente el usuario.

## 11. Generación del Business Model Canvas

Después del análisis JTBD, Gemini generará los nueve bloques:

1. **Alianzas clave**
2. **Actividades clave**
3. **Recursos clave**
4. **Propuesta de valor**
5. **Relación con clientes**
6. **Segmentos de clientes**
7. **Canales**
8. **Estructura de costos**
9. **Fuentes de ingresos**

Cada bloque deberá estar relacionado con el contexto proporcionado por el usuario y con el análisis realizado previamente.

## 12. Salida de la versión 1

La interfaz mostrará como mínimo:

### Resultado JTBD

Una sección con el análisis utilizado para construir el Canvas.

### Business Model Canvas

Una representación de los nueve bloques.

Ejemplo conceptual:

```text
+-------------------+-------------------+-------------------+
| ALIANZAS CLAVE    | ACTIVIDADES CLAVE | RECURSOS CLAVE   |
|                   |                   |                   |
+-------------------+-------------------+-------------------+
| PROPUESTA DE VALOR| RELACIÓN CLIENTES | SEGMENTOS CLIENTES|
|                   |                   |                   |
+-------------------+-------------------+-------------------+
| CANALES           | ESTRUCTURA COSTOS | FUENTES INGRESOS |
|                   |                   |                   |
+-------------------+-------------------+-------------------+
```

## 13. Lo que NO tendrá la V1

Para mantener el alcance controlado, la primera versión no incluirá:

- Sistema de usuarios
- Inicio de sesión
- PostgreSQL
- Historial de proyectos
- Guardado permanente de Canvas
- Panel administrativo
- Pagos
- Exportación PDF
- Aplicación móvil
- Entrenamiento de un modelo desde cero
- Fine-tuning como requisito
- Integraciones empresariales adicionales

Estas funciones podrán evaluarse para versiones posteriores.

## 14. Razón para no utilizar una base de datos tradicional en V1

El objetivo principal de V1 es validar:

```text
Entrada
  ->
RAG
  ->
Gemini
  ->
JTBD
  ->
Business Model Canvas
  ->
Resultado
```

No existe todavía una necesidad esencial de almacenar:

- usuarios
- proyectos
- historial
- configuraciones
- resultados anteriores

Por ello, una base de datos tradicional como PostgreSQL queda fuera del alcance inicial.

El almacenamiento vectorial necesario para RAG se considera parte del sistema de conocimiento y no una base de datos de aplicación para usuarios.

## 15. Responsabilidades del backend Java

El backend deberá:

1. Recibir la solicitud del frontend.
2. Validar el contexto y parámetros.
3. Procesar o consultar el sistema RAG.
4. Recuperar conocimiento relevante.
5. Construir la solicitud para Gemini.
6. Enviar la solicitud a Gemini.
7. Recibir la respuesta.
8. Validar/estructurar el resultado.
9. Enviar el resultado al frontend.

El backend no deberá depender de que el navegador se comunique directamente con la API de Gemini si eso implica exponer la API key.

## 16. Seguridad básica

La API key de Gemini no deberá estar escrita en el frontend.

La arquitectura será:

```text
Frontend
   |
   | solicitud
   v
Backend
   |
   | API key protegida
   v
Gemini
```

Las credenciales deberán manejarse mediante variables de entorno o un mecanismo equivalente.

Nunca se deberán subir las claves al repositorio Git.

## 17. Estructura inicial propuesta del proyecto

```text
bmc-ai-generator/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │
│   ├── pom.xml
│   └── .env.example
│
├── frontend/
│   ├── index.html
│   ├── css/
│   └── js/
│
├── knowledge/
│   ├── business-model-canvas/
│   └── jobs-to-be-done/
│
├── docs/
│
├── .gitignore
└── README.md
```

La estructura exacta podrá cambiar durante la implementación.

## 18. Versiones futuras

### V2

Mostrar explícitamente el proceso:

```text
Contexto
   ↓
Análisis
   ↓
JTBD
   ↓
Necesidades
   ↓
Dolores
   ↓
Beneficios
   ↓
Propuesta de valor
   ↓
Business Model Canvas
```

La finalidad será hacer visible cómo la IA llega desde el problema inicial hasta el Canvas.

### V3

Posibles funciones:

- Usuarios
- Proyectos
- Historial
- Guardado de Canvas
- Edición manual
- Versionado
- Exportación
- Compartir proyectos

En esta etapa podría incorporarse una base de datos tradicional.

### V4

Posibles funciones avanzadas:

- Múltiples fuentes de conocimiento
- Carga de documentos por parte del usuario
- Diferentes metodologías de análisis
- Comparación entre Canvas
- Evaluación de consistencia entre los 9 bloques
- Mejoras en RAG
- Control de versiones del conocimiento
- Observabilidad y métricas

## 19. Principios del proyecto

1. Priorizar una arquitectura sencilla para V1.
2. Separar frontend, backend, conocimiento e IA.
3. No exponer credenciales de Gemini.
4. No confundir RAG con entrenamiento.
5. Utilizar fuentes de conocimiento autorizadas.
6. Hacer que Gemini utilice el contexto del usuario y el conocimiento recuperado.
7. Generar primero el análisis JTBD y posteriormente el BMC.
8. Mantener el proyecto preparado para futuras versiones.
9. Evitar agregar tecnologías que no sean necesarias para V1.
10. Validar primero el flujo completo antes de agregar funcionalidades adicionales.

## 20. Objetivo técnico final de V1

Al finalizar V1 deberá ser posible ejecutar:

```text
1. Abrir la página web.
2. Introducir un contexto.
3. Introducir parámetros.
4. Presionar "Generar".
5. El backend recibe los datos.
6. RAG recupera conocimiento relevante.
7. Gemini recibe contexto + parámetros + conocimiento.
8. Gemini genera el análisis JTBD.
9. Gemini genera los 9 bloques del BMC.
10. El backend procesa la respuesta.
11. La página muestra JTBD + Business Model Canvas.
```

## 21. Criterio de éxito

La V1 será considerada funcional cuando una entrada real del usuario produzca un resultado coherente y estructurado que:

- Utilice el contexto proporcionado.
- Tome en cuenta los parámetros.
- Utilice conocimiento relevante mediante RAG.
- Presente un análisis JTBD.
- Genere los nueve bloques del Business Model Canvas.
- Mantenga coherencia entre JTBD, propuesta de valor y BMC.
- Sea mostrado correctamente en la interfaz web.
