---
name: python-microservices-migration
description: 将 Python/FastAPI 微服务（AI Agents、RAG、TTS、Vision）迁移到 Java/Spring Boot。包含 FastAPI 到 Spring Boot 的完整 API 映射、LangChain4j 集成、向量数据库适配器、多 Provider TTS 架构。
---

# Python 微服务迁移到 Java

## 服务概览

| 服务 | 端口 | 依赖 | 迁移目标 |
|------|------|------|---------|
| `services/ai_agents` | 8003 | LangGraph, LLM Gateway | `langchain4j` |
| `services/rag` | 8001 | Qdrant, Embedding | `qdrant-client` |
| `services/tts-service` | 8002 | Azure, Google, ElevenLabs | Azure/Google SDK |
| `services/vision-service` | 8000 | YOLO, Stable Diffusion | Spring AI |
| `services/text-service` | - | Text processing | `@Service` |

---

## 迁移对照表

### FastAPI → Spring Boot

| FastAPI | Spring Boot |
|---------|-------------|
| `@router.post()` | `@PostMapping` |
| `async def endpoint()` | `@Async` + `CompletableFuture` |
| `UploadFile = File(...)` | `MultipartFile file` |
| `Query(param)` | `@RequestParam` |
| `Path(param)` | `@PathVariable` |
| `Body(param)` | `@RequestBody` |
| `Depends()` | `@Autowired` |
| `HTTPException` | `@ExceptionHandler` |
| `BackgroundTasks` | `@Async` + `@EventListener` |

---

## Part 1: RAG Service 迁移

### 项目结构

```
services/rag/src/
├── main.py                    →  RagServiceApplication.java
├── api/
│   ├── documents.py           →  DocumentController.java
│   └── chat.py               →  ChatController.java
├── core/
│   ├── vector_store.py       →  QdrantAdapter.java
│   ├── embedding.py         →  EmbeddingService.java
│   └── llm_gateway.py       →  LlmGateway.java
├── document_loader/           →  DocumentParser.java
├── persistence/               →  Repository + JPA
└── config.py                 →  RagConfig.java
```

### Document API 迁移

```python
# Python: api/documents.py
@router.post("/documents/upload")
async def upload_document(
    file: UploadFile = File(...),
    collection: str = Query("default")
):
    content = await file.read()
    chunks = document_loader.load(content, file.filename)
    vectors = embedding_model.embed(chunks)
    doc_id = vector_store.upsert(chunks, vectors)
    return {"doc_id": doc_id, "chunk_count": len(chunks)}
```

```java
// Java: DocumentController.java
@RestController
@RequestMapping("/api/rag/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "default") String collection) {

        DocumentUploadResponse response = documentService.uploadDocument(file, collection);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<DocumentDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(documentService.listDocuments(page, size));
    }

    @DeleteMapping("/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String docId) {
        documentService.deleteDocument(docId);
    }
}
```

### Chat API 迁移

```python
# Python: api/chat.py
@router.post("/chat/")
async def chat(request: ChatRequest):
    query_vector = embedding_model.embed([request.query])
    results = vector_store.search(query_vector, top_k=5)
    context = format_context(results)

    response = llm_gateway.generate(
        system_prompt=RAG_PROMPT,
        user_prompt=f"Context: {context}\n\nQuestion: {request.query}"
    )

    return {"answer": response, "sources": [r.payload for r in results]}
```

```java
// Java: ChatController.java
@RestController
@RequestMapping("/api/rag/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }

    @PostMapping("/stream")
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }
}

// ChatService.java
@Service
@RequiredArgsConstructor
public class ChatService {

    private final EmbeddingService embeddingService;
    private final VectorStoreAdapter vectorStoreAdapter;
    private final LlmGateway llmGateway;

    public ChatResponse chat(ChatRequest request) {
        // 1. Generate embedding
        float[] queryVector = embeddingService.embed(request.query());

        // 2. Search vector store
        List<SearchResult> results = vectorStoreAdapter.search(queryVector, 5);

        // 3. Build context
        String context = results.stream()
            .map(SearchResult::getContent)
            .collect(Collectors.joining("\n---\n"));

        // 4. Generate response
        String prompt = String.format("Context:\n%s\n\nQuestion: %s", context, request.query());
        String answer = llmGateway.generate(RAG_PROMPT, prompt);

        return new ChatResponse(answer, results);
    }
}
```

### Qdrant Adapter

```java
// Java: QdrantAdapter.java
@Component
@RequiredArgsConstructor
public class QdrantAdapter {

    private final QdrantClient qdrantClient;
    private final ObjectMapper objectMapper;
    private static final String COLLECTION_NAME = "documents";

    public void createCollection() {
        if (!qdrantClient.collectionExists(COLLECTION_NAME)) {
            qdrantClient.createCollection(COLLECTION_NAME,
                VectorParams.builder().size(1536).distance(Distance.Cosine).build()
            );
        }
    }

    public String upsert(List<DocumentChunk> chunks, List<float[]> vectors) {
        String docId = UUID.randomUUID().toString();
        List<PointStruct> points = IntStream.range(0, chunks.size())
            .mapToObj(i -> PointStruct.of(
                UUID.randomUUID().toString(),
                vectors.get(i),
                Map.of(
                    "doc_id", docId,
                    "content", chunks.get(i).getContent(),
                    "metadata", objectMapper.valueToTree(chunks.get(i).getMetadata())
                )
            ))
            .toList();

        qdrantClient.upsert(COLLECTION_NAME, points);
        return docId;
    }

    public List<SearchResult> search(float[] queryVector, int topK) {
        SearchResponse response = qdrantClient.search(
            SearchRequest.builder()
                .collectionName(COLLECTION_NAME)
                .queryVector(queryVector)
                .limit(topK)
                .build()
        );

        return response.getResults().stream()
            .map(this::toSearchResult)
            .toList();
    }
}
```

---

## Part 2: AI Agents Service 迁移

### 项目结构

```
services/ai_agents/
├── main.py                   →  AiAgentsApplication.java
├── presentation/agents/      →  Controller + Service
│   ├── supervisor.py        →  SupervisorAgentService.java
│   ├── rag_agent.py         →  RagAgentService.java
│   ├── tts_agent.py         →  TtsAgentService.java
│   └── ...
├── domain/services/         →  Domain Service
├── infrastructure/          →  Adapter + Tools
└── application/graphs/      →  State Machine / Workflow
```

### Supervisor Agent

```python
# Python: Supervisor Agent
class SupervisorAgent:
    def route(self, message: str) -> str:
        intent = self.llm.classify_intent(message)
        return intent  # "rag", "tts", "k8s", etc.

    async def process(self, message: str) -> AgentResponse:
        agent_name = self.route(message)
        agent = self.sub_agents[agent_name]
        return await agent.execute(message)
```

```java
// Java: SupervisorAgentService.java
@Service
@RequiredArgsConstructor
public class SupervisorAgentService {

    private final Map<String, SubAgent> subAgents;
    private final IntentClassifier intentClassifier;

    public AgentResponse process(String userMessage) {
        Intent intent = intentClassifier.classify(userMessage);
        SubAgent agent = subAgents.get(intent.getAgentName());

        if (agent == null) {
            return AgentResponse.error("Unknown agent: " + intent.getAgentName());
        }

        return agent.execute(intent);
    }
}

// IntentClassifier.java
@Component
@RequiredArgsConstructor
public class IntentClassifier {

    private final ChatModel chatModel;

    public Intent classify(String message) {
        String prompt = """
            Classify the following message into one of these intents:
            - rag: for document retrieval and Q&A
            - tts: for text-to-speech
            - vision: for image analysis
            - k8s: for Kubernetes operations
            - mlops: for ML pipeline operations

            Message: %s

            Return only the intent name.
            """.formatted(message);

        String response = chatModel.chat(prompt);

        return new Intent(parseIntent(response.trim()), message);
    }
}
```

### 子 Agent 示例

```java
// RagAgentService.java
@Service
@RequiredArgsConstructor
public class RagAgentService implements SubAgent {

    private final DocumentService documentService;
    private final ChatService chatService;

    @Override
    public boolean canHandle(Intent intent) {
        return intent.getAgentName().equals("rag");
    }

    @Override
    public AgentResponse execute(Intent intent) {
        String query = intent.getMessage();
        ChatResponse response = chatService.chat(new ChatRequest(query));
        return AgentResponse.success(response.getAnswer(), response.getSources());
    }
}
```

### LangChain4j 集成

```kotlin
// build.gradle.kts
dependencies {
    implementation("dev.langchain4j:langchain4j:1.0.0")
    implementation("dev.langchain4j:langchain4j-open-ai:1.0.0")
    implementation("dev.langchain4j:langchain4j-ollama:1.0.0")
}
```

```java
// LangChain4jConfig.java
@Configuration
@RequiredArgsConstructor
public class LangChain4jConfig {

    private final AppSettings appSettings;

    @Bean
    public ChatModel chatModel() {
        return switch (appSettings.getLlmProvider()) {
            case "openai" -> OpenAiChatModel.builder()
                .apiKey(appSettings.getOpenAiApiKey())
                .modelName(appSettings.getLlmModel())
                .temperature(0.7)
                .build();
            case "ollama" -> new OllamaChatModel(OllamaChatModel.builder()
                .baseUrl(appSettings.getOllamaBaseUrl())
                .modelName(appSettings.getOllamaModel())
                .build());
            default -> throw new IllegalArgumentException("Unknown LLM provider");
        };
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
```

---

## Part 3: TTS Service 迁移

### Provider 架构

```python
# Python: Provider Factory
class TTSFactory:
    PROVIDERS = {
        "azure": AzureTTSProvider,
        "google": GoogleTTSProvider,
        "elevenlabs": ElevenLabsProvider,
        "edge": EdgeTTSProvider,
        "coqui": CoquiTTSProvider,
    }

    def get_provider(self, name: str) -> TTSProvider:
        return self.PROVIDERS.get(name, EdgeTTSProvider)()
```

```java
// Java: TTS Provider Architecture

// TtsProvider.java
public interface TtsProvider {
    byte[] synthesize(String text, VoiceConfig config);
    List<Voice> listVoices();
    boolean healthCheck();
}

// TtsService.java
@Service
@RequiredArgsConstructor
public class TtsService {

    private final Map<TtsProviderType, TtsProvider> providers;
    private final AppSettings settings;

    public byte[] synthesize(SynthesizeRequest request) {
        TtsProvider provider = providers.get(settings.getTtsProvider());
        return provider.synthesize(request.text(), request.voiceConfig());
    }
}

// Provider Implementations
@Component
@ConditionalOnProperty(name = "tts.provider", havingValue = "azure")
public class AzureTtsProvider implements TtsProvider {

    private final AzureSpeechConfig config;

    @Override
    public byte[] synthesize(String text, VoiceConfig config) {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(
            config.getApiKey(),
            config.getRegion()
        );

        try (SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig)) {
            SpeechSynthesisResult result = synthesizer.speakTextAsync(text).get();
            return result.getAudioData();
        }
    }
}

@Component
@ConditionalOnProperty(name = "tts.provider", havingValue = "google")
public class GoogleTtsProvider implements TtsProvider {
    // Google Cloud Text-to-Speech SDK integration
}
```

### TTS Controller

```java
@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @PostMapping("/synthesize")
    public ResponseEntity<byte[]> synthesize(@Valid @RequestBody SynthesizeRequest request) {
        byte[] audio = ttsService.synthesize(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "audio/mp3")
            .body(audio);
    }

    @GetMapping("/voices")
    public ResponseEntity<List<Voice>> listVoices() {
        return ResponseEntity.ok(ttsService.listVoices());
    }

    @GetMapping("/providers")
    public ResponseEntity<List<String>> listProviders() {
        return ResponseEntity.ok(List.of("azure", "google", "elevenlabs", "edge"));
    }
}
```

---

## Part 4: Vision Service 迁移

### Vision Controller

```python
# Python: api/vision.py
@router.post("/vision/detect")
async def detect_objects(image: UploadFile = File(...)):
    image_bytes = await image.read()
    results = yolo_detector.detect(image_bytes)
    return {"objects": results}
```

```java
// Java: VisionController.java
@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
public class VisionController {

    private final VisionService visionService;

    @PostMapping("/detect")
    public ResponseEntity<DetectionResult> detect(
            @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(visionService.detectObjects(image));
    }

    @PostMapping("/caption")
    public ResponseEntity<CaptionResult> caption(
            @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(visionService.captionImage(image));
    }

    @PostMapping("/ocr")
    public ResponseEntity<OcrResult> ocr(
            @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(visionService.ocr(image));
    }
}

// VisionService.java
@Service
@RequiredArgsConstructor
public class VisionService {

    private final ObjectDetectionAdapter yoloAdapter;
    private final ImageCaptioningAdapter blipAdapter;
    private final OcrAdapter ocrAdapter;

    public DetectionResult detectObjects(MultipartFile image) {
        byte[] imageBytes = image.getResource().getContentAsByteArray();
        return yoloAdapter.detect(imageBytes);
    }
}
```

---

## Part 5: 健康检查迁移

```python
# Python: FastAPI Health Check
@app.get("/health")
async def health():
    qdrant_connected = False
    try:
        vector_store.client.get_collection(collection_name)
        qdrant_connected = True
    except Exception:
        pass

    return HealthResponse(
        status="ok" if qdrant_connected else "degraded",
        qdrant_connected=qdrant_connected,
    )
```

```java
// Java: Spring Boot Health Check
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final QdrantAdapter qdrantAdapter;
    private final EmbeddingService embeddingService;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        boolean qdrantConnected = qdrantAdapter.isConnected();

        String status = qdrantConnected ? "ok" : "degraded";
        return ResponseEntity.ok(new HealthResponse(
            status,
            qdrantConnected,
            embeddingService.getModelName(),
            appSettings.getLlmProvider()
        ));
    }
}
```

---

## 验证清单

### RAG Service
- [ ] 文档上传 → 向量存储
- [ ] 文档搜索 → 相似度检索
- [ ] Chat → RAG 问答

### AI Agents Service
- [ ] Supervisor 正确路由
- [ ] 各子 Agent 执行正常
- [ ] LangChain4j LLM 调用

### TTS Service
- [ ] Edge TTS (默认 Provider)
- [ ] Azure TTS
- [ ] Google TTS
- [ ] ElevenLabs
- [ ] 语音列表获取

### Vision Service
- [ ] YOLO 目标检测
- [ ] BLIP 图像描述
- [ ] OCR 文字识别
- [ ] 图像生成
