package com.bmcai.backend.knowledge.model;

public class KnowledgeChunk {

    private String documentName;
    private String documentPath;
    private String documentType;
    private int chunkIndex;
    private String content;

    public KnowledgeChunk() {
    }

    public KnowledgeChunk(
            String documentName,
            String documentPath,
            String documentType,
            int chunkIndex,
            String content
    ) {
        this.documentName = documentName;
        this.documentPath = documentPath;
        this.documentType = documentType;
        this.chunkIndex = chunkIndex;
        this.content = content;
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
}