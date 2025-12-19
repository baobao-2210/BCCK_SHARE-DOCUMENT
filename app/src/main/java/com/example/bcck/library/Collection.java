package com.example.bcck.library;
public class Collection {
    private String name;
    private int documentCount;
    private String color;

    public Collection(String name, int documentCount, String color) {
        this.name = name;
        this.documentCount = documentCount;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}