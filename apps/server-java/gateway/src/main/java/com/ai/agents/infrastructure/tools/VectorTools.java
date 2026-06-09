package com.ai.agents.infrastructure.tools;

import com.ai.agents.domain.ToolResult;
import com.ai.agents.domain.service.agents.VectorAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Vector database tools implementation.
 * Provides ChromaDB/Qdrant vector operations tools.
 */
@Component
public class VectorTools {

    private static final Logger log = LoggerFactory.getLogger(VectorTools.class);
    private final VectorAgentService domainService;

    public VectorTools(VectorAgentService domainService) {
        this.domainService = domainService;
    }

    public Mono<ToolResult> createCollection(String name, int dimension, String description) {
        return executeTool("vector_create_collection", () -> {
            VectorAgentService.Collection collection = domainService.createCollection(name, dimension, description);

            String output = String.format("""
                    Collection Created

                    Name: %s
                    Dimension: %d
                    Description: %s
                    Count: %d
                    Created: %s""",
                    collection.name(),
                    collection.dimension(),
                    collection.description(),
                    collection.count(),
                    collection.createdAt()
            );
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> listCollections() {
        return executeTool("vector_list_collections", () -> {
            List<VectorAgentService.Collection> collections = domainService.listCollections();

            if (collections.isEmpty()) {
                return ToolResult.success("No collections found.");
            }

            StringBuilder output = new StringBuilder("Collections:\n\n");
            for (VectorAgentService.Collection col : collections) {
                output.append("- ").append(col.name()).append(": ").append(col.count()).append(" vectors\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> query(String collectionName, List<Float> queryEmbedding, int topK) {
        return executeTool("vector_query", () -> {
            float[] embedding = new float[queryEmbedding.size()];
            for (int i = 0; i < queryEmbedding.size(); i++) {
                embedding[i] = queryEmbedding.get(i);
            }

            List<VectorAgentService.QueryResult> results = domainService.query(collectionName, embedding, topK);

            StringBuilder output = new StringBuilder("Query Results from '").append(collectionName).append("'\n\n");
            for (int i = 0; i < results.size(); i++) {
                VectorAgentService.QueryResult r = results.get(i);
                output.append(i + 1).append(". [").append(String.format("%.3f", r.similarity())).append("] ");
                output.append(r.document().substring(0, Math.min(100, r.document().length())));
                if (r.document().length() > 100) output.append("...");
                output.append("\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> addVectors(String collectionName, List<String> ids, List<String> documents) {
        return executeTool("vector_add", () -> {
            int dimension = 384;
            List<float[]> embeddings = ids.stream()
                    .map(id -> {
                        float[] emb = new float[dimension];
                        java.util.Random random = new java.util.Random(id.hashCode());
                        for (int i = 0; i < dimension; i++) {
                            emb[i] = random.nextFloat();
                        }
                        return emb;
                    })
                    .toList();

            List<String> addedIds = domainService.addVectors(collectionName, ids, embeddings, documents, null);

            String output = String.format("Added %d vectors to collection '%s'", addedIds.size(), collectionName);
            return ToolResult.success(output);
        });
    }

    private <T> Mono<ToolResult> executeTool(String toolName, ToolExecutor executor) {
        try {
            ToolResult result = executor.execute();
            log.debug("Tool {} executed successfully", toolName);
            return Mono.just(result);
        } catch (Exception e) {
            log.error("Tool {} failed: {}", toolName, e.getMessage());
            return Mono.just(ToolResult.error(e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface ToolExecutor {
        ToolResult execute();
    }
}
