package com.bmcai.backend.embedding.model;

public class ChunkEmbedding {

    private String documentName;
    private String documentPath;
    private String documentType;
    private int chunkIndex;
    private String content;
    private float[] embedding;

    public ChunkEmbedding() {
    }

    public ChunkEmbedding(
            String documentName,
            String documentPath,
            String documentType,
            int chunkIndex,
            String content,
            float[] embedding
    ) {
        this.documentName = documentName;
        this.documentPath = documentPath;
        this.documentType = documentType;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.embedding = embedding;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}