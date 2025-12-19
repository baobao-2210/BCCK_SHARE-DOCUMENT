package com.example.bcck.Home;
public class PdfItem {
    private String title;
    private String url;

    public PdfItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
}
