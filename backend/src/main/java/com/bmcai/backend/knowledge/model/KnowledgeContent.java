package com.bmcai.backend.knowledge.model;

public class KnowledgeContent {

    private String name;
    private String path;
    private String type;
    private String content;
    private long size;

    public KnowledgeContent() {
    }

    public KnowledgeContent(
            String name,
            String path,
            String type,
            String content,
            long size
    ) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.content = content;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}