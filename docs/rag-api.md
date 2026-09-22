# API RAG — IA Business Model Canvas

## 1. Descripción

El backend implementa un sistema RAG (Retrieval-Augmented Generation) que permite buscar información relevante dentro de la base de conocimiento local antes de enviarla al modelo de lenguaje.

El sistema utiliza:

- Archivos Markdown y PDF como fuente de conocimiento.
- Apache PDFBox para extraer texto de los PDF.
- División del contenido en chunks.
- `multilingual-e5-small` para generar embeddings localmente.
- ONNX Runtime mediante Spring AI Transformers.
- Búsqueda semántica mediante similitud coseno.
- Almacenamiento local de embeddings en `models/embeddings.json`.
- Un filtro de relevancia para evitar enviar contexto cuando la consulta no está relacionada con la base de conocimiento.

Gemini no forma parte de este módulo. La integración con Gemini será realizada por otro módulo del backend.

---

# 2. Flujo del RAG

```text
Archivos Markdown / PDF
        ↓
KnowledgeReaderService
        ↓
KnowledgeChunkService
        ↓
KnowledgeChunk
        ↓
EmbeddingChunkService
        ↓
multilingual-e5-small
        ↓
ChunkEmbedding
        ↓
VectorStoreService
        ↓
embeddings.json
        ↓
SemanticSearchService
        ↓
Top-K candidatos
        ↓
RagContextService
        ↓
Filtro de relevancia
        ↓
RagService
        ↓
Contexto para Gemini
```

---

# 3. Base de conocimiento

La carpeta utilizada por el backend es:

```text
knowledge/
```

Actualmente contiene:

```text
knowledge/
├── bmc/
│   ├── actividades-clave.md
│   ├── business-model-canvas.md
│   ├── canales.md
│   ├── estructura-costos.md
│   ├── fuentes-ingresos.md
│   ├── propuesta-valor.md
│   ├── recursos-clave.md
│   ├── relaciones-clientes.md
│   ├── segmentos-clientes.md
│   └── socios-clave.md
│
├── jtbd/
│   ├── customer-profile.md
│   ├── introduccion-jtbd.md
│   ├── job-emocional.md
│   ├── job-funcional.md
│   ├── job-social.md
│   └── value-map.md
│
└── libros/
    ├── La-Vaca-Púrpura-Seth-Godin.pdf
    ├── marketing-7-0-philip-kotler.pdf
    └── tribus-seth-godin.pdf
```

Los documentos se leen dinámicamente desde la carpeta `knowledge`.

---

# 4. Embeddings

El modelo utilizado es:

```text
intfloat/multilingual-e5-small
```

El modelo se encuentra localmente en:

```text
models/
└── multilingual-e5-small/
    ├── model.onnx
    └── tokenizer.json
```

Características principales:

- Embeddings de 384 dimensiones.
- Ejecución local.
- No requiere enviar el contenido de la base de conocimiento a un servicio externo.
- Utiliza los prefijos recomendados por E5:

```text
query: <consulta>
```

para consultas y:

```text
passage: <contenido>
```

para documentos.

---

# 5. Índice vectorial

Los embeddings generados se almacenan en:

```text
models/embeddings.json
```

El índice contiene los chunks y sus vectores.

Actualmente el índice contiene:

```text
526 chunks
```

El backend carga automáticamente el índice al iniciar si `embeddings.json` existe.

Si no existe, se puede generar mediante el endpoint de indexación.

---

# 6. Configuración RAG

En `application.yaml`:

```yaml
rag:
  search:
    top-k: 5
    min-similarity: 0.90
    context-score-window: 0.05
```

### `top-k`

Cantidad máxima de candidatos recuperados por la búsqueda semántica.

Actualmente:

```text
5
```

### `min-similarity`

Similitud mínima requerida para considerar que la consulta tiene contexto relevante.

Actualmente:

```text
0.90
```

### `context-score-window`

Una vez encontrado un resultado suficientemente relevante, permite incluir otros resultados cercanos al mejor resultado.

Actualmente:

```text
0.05
```

Ejemplo:

```text
Mejor resultado: 0.9204

0.9204 - 0.05 = 0.8704
```

Por lo tanto, resultados con similitud >= `0.8704` pueden formar parte del contexto.

---

# 7. Endpoints de Knowledge

## Listar documentos

```http
GET /api/knowledge/documents
```

Devuelve los documentos disponibles en la carpeta `knowledge`.

Ejemplo:

```text
GET http://localhost:8080/api/knowledge/documents
```

---

## Leer un documento

```http
GET /api/knowledge/document?path=<ruta>
```

Ejemplo:

```text
GET /api/knowledge/document?path=bmc/propuesta-valor.md
```

Devuelve:

- nombre
- ruta
- tipo
- tamaño
- contenido

---

## Obtener chunks de un documento

```http
GET /api/knowledge/chunks?path=<ruta>
```

Ejemplo:

```text
GET /api/knowledge/chunks?path=bmc/propuesta-valor.md
```

Devuelve los chunks generados a partir del documento.

La configuración actual utiliza:

```text
Chunk size: 1000 caracteres
Overlap: 150 caracteres
```

---

# 8. Endpoints de Embeddings

## Probar el modelo de embeddings

```http
GET /api/embedding/test
```

Permite comprobar que el modelo local está funcionando.

---

## Calcular similitud

```http
GET /api/embedding/similarity
```

Este endpoint permite probar la similitud entre embeddings.

---

## Generar embedding de un documento

```http
GET /api/embedding/document
```

Permite comprobar la generación de embeddings a partir de contenido de conocimiento.

---

# 9. Índice de embeddings

## Construir/reconstruir índice

```http
GET /api/embedding/index
```

Este endpoint:

1. Limpia el índice actual.
2. Lee todos los documentos compatibles.
3. Divide cada documento en chunks.
4. Genera un embedding para cada chunk.
5. Guarda todos los embeddings.
6. Actualiza `models/embeddings.json`.

El índice actual contiene:

```text
526 chunks
```

---

## Estado del índice

```http
GET /api/embedding/index/status
```

Permite consultar si existe el índice y su estado.

---

## Cargar índice

```http
GET /api/embedding/index/load
```

Carga manualmente `embeddings.json` en memoria.

Normalmente no es necesario utilizar este endpoint porque el índice se carga automáticamente durante el inicio del backend.

---

## Ruta del índice

```http
GET /api/embedding/index/file
```

Devuelve la ubicación física del archivo `embeddings.json`.

---

# 10. Búsqueda semántica

## Buscar contenido relacionado

```http
GET /api/embedding/search?query=<consulta>&topK=<cantidad>
```

Ejemplo:

```text
GET /api/embedding/search?query=propuesta%20de%20valor&topK=5
```

El endpoint devuelve los candidatos ordenados por similitud.

Importante:

`topK=5` significa que se recuperan los cinco mejores candidatos. No significa que los cinco sean necesariamente relevantes.

---

# 11. Endpoint principal del RAG

El endpoint que debe utilizar el módulo de Gemini es:

```http
GET /api/rag/context?query=<consulta>&topK=<cantidad>
```

También puede utilizarse sin especificar `topK`:

```http
GET /api/rag/context?query=<consulta>
```

En ese caso se utiliza el valor configurado:

```text
top-k = 5
```

Ejemplo:

```text
GET http://localhost:8080/api/rag/context?query=propuesta%20de%20valor
```

---

# 12. Respuesta del endpoint RAG

Ejemplo simplificado:

```json
{
  "query": "propuesta de valor",
  "topK": 5,
  "retrievedResults": 5,
  "relevantResults": 2,
  "relevant": true,
  "context": "..."
}
```

Los campos significan:

### `query`

Consulta original del usuario.

### `topK`

Cantidad máxima de candidatos solicitados.

### `retrievedResults`

Cantidad de candidatos recuperados por la búsqueda semántica.

### `relevantResults`

Cantidad de resultados que finalmente se consideran suficientemente relacionados para formar parte del contexto.

### `relevant`

Indica si la consulta tiene contexto relevante.

```text
true
```

significa que existe contexto que puede utilizarse.

```text
false
```

significa que no se encontró contexto suficientemente relevante.

### `context`

Texto recuperado de la base de conocimiento.

Este es el campo que debe utilizar el módulo de Gemini como contexto RAG.

---

# 13. Ejemplo de consulta relevante

Consulta:

```text
propuesta de valor
```

Resultado:

```json
{
  "query": "propuesta de valor",
  "topK": 5,
  "retrievedResults": 5,
  "relevantResults": 2,
  "relevant": true
}
```

El contexto recuperado incluye información de:

```text
bmc/propuesta-valor.md
jtbd/value-map.md
```

Por lo tanto, Gemini puede recibir ese contexto para construir la respuesta.

---

# 14. Ejemplo de consulta no relacionada

Consulta:

```text
receta de cocina
```

Resultado:

```json
{
  "query": "receta de cocina",
  "topK": 5,
  "retrievedResults": 5,
  "relevantResults": 0,
  "relevant": false,
  "context": ""
}
```

Aunque la búsqueda semántica encuentra cinco candidatos, ninguno se considera contexto relevante.

En este caso Gemini no debería recibir contenido de la base de conocimiento como si fuera información relacionada con la consulta.

---

# 15. Integración con Gemini

El módulo de Gemini debe utilizar principalmente:

```text
GET /api/rag/context
```

Flujo esperado:

```text
Usuario
   ↓
Frontend
   ↓
Backend
   ↓
/api/rag/context
   ↓
Búsqueda semántica
   ↓
Contexto relevante
   ↓
Gemini
   ↓
Respuesta
   ↓
Frontend
```

La responsabilidad del módulo RAG termina en proporcionar:

```text
query
relevant
relevantResults
context
```

La generación de la respuesta final corresponde al módulo de Gemini.

---

# 16. Consideraciones para la integración

El módulo de Gemini no debería generar una respuesta basándose únicamente en la existencia de candidatos.

Debe comprobar:

```text
relevant == true
```

antes de utilizar `context` como contexto recuperado.

Si:

```text
relevant == false
```

y `context` está vacío, Gemini puede utilizar la lógica definida por el módulo de generación para responder que la información no está disponible en la base de conocimiento.

---

# 17. Componentes implementados

### Knowledge

```text
knowledge/
```

Lectura de Markdown y PDF.

### Chunking

```text
KnowledgeChunkService
```

Divide los documentos en fragmentos con solapamiento.

### Embeddings

```text
EmbeddingService
EmbeddingChunkService
```

Generan embeddings utilizando `multilingual-e5-small`.

### Vector Store

```text
VectorStoreService
```

Mantiene los embeddings en memoria y los persiste en:

```text
models/embeddings.json
```

### Indexación

```text
KnowledgeEmbeddingIndexService
```

Construye y carga el índice.

### Búsqueda

```text
SemanticSearchService
```

Realiza búsqueda semántica mediante similitud coseno.

### Contexto RAG

```text
RagContextService
```

Determina qué resultados pueden utilizarse como contexto.

### Servicio RAG

```text
RagService
```

Coordina búsqueda y construcción del contexto.

### API

```text
RagController
```

Expone el endpoint:

```text
/api/rag/context
```

---

# 18. Estado actual

La parte de Knowledge + Embeddings + RAG está implementada y probada.

Pruebas realizadas:

```text
propuesta de valor
→ relevant: true
→ relevantResults: 2
→ context generado correctamente
```

```text
receta de cocina
→ relevant: false
→ relevantResults: 0
→ context vacío
```

El módulo queda preparado para ser consumido por el componente de Gemini.