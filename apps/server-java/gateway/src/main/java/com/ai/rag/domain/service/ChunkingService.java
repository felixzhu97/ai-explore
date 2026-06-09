package com.ai.rag.domain.service;

import com.ai.rag.domain.Chunk;
import com.ai.rag.domain.ChunkingPolicy;
import com.ai.rag.domain.DocumentContent;
import com.ai.rag.domain.DocumentId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Domain service for text chunking operations.
 * Pure business logic with no external dependencies (no Spring).
 */
public class ChunkingService {

    private static final String[] SENTENCE_SEPARATORS = {"\n\n", "\n", ". ", "? ", "! ", "; ", ", "};
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");

    /**
     * Chunks the document content according to the given policy.
     * 
     * @param content Document content to chunk
     * @param policy  Chunking policy to apply
     * @param docId   Document ID for chunk metadata
     * @return List of Chunk value objects
     */
    public List<Chunk> chunk(DocumentContent content, ChunkingPolicy policy, DocumentId docId) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        String text = content.value();
        List<String> chunkTexts = switch (policy.strategy()) {
            case BY_SIZE -> chunkBySize(text, policy.chunkSize(), policy.overlap());
            case BY_PARAGRAPH -> chunkByParagraphs(text, policy.chunkSize(), policy.overlap());
        };

        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkTexts.size(); i++) {
            chunks.add(Chunk.create(chunkTexts.get(i), i, docId));
        }
        return chunks;
    }

    /**
     * Chunks text by size with overlap.
     */
    public List<String> chunkBySize(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        int start = 0;

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            String chunk = text.substring(start, end);

            // Try to break at natural boundaries
            if (end < textLength) {
                chunk = breakAtNaturalBoundary(chunk);
            }

            if (!chunk.isBlank()) {
                chunks.add(chunk.trim());
            }

            // Move start position, accounting for overlap
            start = start + chunk.length() - overlap;
            if (start <= 0 || start >= textLength) {
                break;
            }
        }

        return chunks;
    }

    /**
     * Chunks text by paragraphs first, then by size if paragraphs are too large.
     */
    public List<String> chunkByParagraphs(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // Split into paragraphs
        String[] paragraphs = PARAGRAPH_PATTERN.split(text);

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmedParagraph = paragraph.trim();
            if (trimmedParagraph.isEmpty()) {
                continue;
            }

            // If adding this paragraph exceeds chunk size, save current and start new
            if (currentChunk.length() + trimmedParagraph.length() + 2 > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }

                // If single paragraph exceeds chunk size, split it further
                if (trimmedParagraph.length() > chunkSize) {
                    chunks.addAll(chunkBySize(trimmedParagraph, chunkSize, overlap));
                } else {
                    currentChunk.append(trimmedParagraph);
                }
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmedParagraph);
            }
        }

        // Add final chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * Attempts to break a chunk at a natural sentence/paragraph boundary.
     */
    private String breakAtNaturalBoundary(String chunk) {
        // Find the last occurrence of sentence separators
        int bestBreakPoint = -1;
        int minPosition = chunk.length() / 2; // Don't break in the middle

        for (String separator : SENTENCE_SEPARATORS) {
            int lastIndex = chunk.lastIndexOf(separator);
            if (lastIndex > minPosition) {
                bestBreakPoint = lastIndex + separator.length();
                break;
            }
        }

        if (bestBreakPoint > 0 && bestBreakPoint < chunk.length()) {
            return chunk.substring(0, bestBreakPoint);
        }

        return chunk;
    }

    /**
     * Estimates token count (rough estimate: 1 token ≈ 4 characters for English).
     */
    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.length() / 4;
    }

    /**
     * Checks if text needs chunking based on threshold.
     */
    public boolean needsChunking(String text, int threshold) {
        return estimateTokens(text) > threshold;
    }
}
