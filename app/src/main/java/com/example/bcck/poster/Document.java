package com.example.bcck.poster;

import java.io.Serializable;

public class Document implements Serializable {

    private String authorName;
    private String title;
    private String docType;
    private String subject;
    private String teacher;
    private String major;
    private int downloads;
    private int likes;
    private float rating;

    private String uploaderName;
    private String year;
    private long uploadTimestamp;
    private String fileUrl;

    private String uploaderId;  // ID người đăng

    public Document() {
        // Firestore bắt buộc cần constructor rỗng
    }

    public Document(String authorName, String title, String docType, String subject, String teacher, String major,
                    int downloads, int likes, float rating, String uploaderName, String year, long uploadTimestamp,
                    String fileUrl, String uploaderId) {
        this.authorName = authorName;
        this.title = title;
        this.docType = docType;
        this.subject = subject;
        this.teacher = teacher;
        this.major = major;
        this.downloads = downloads;
        this.likes = likes;
        this.rating = rating;
        this.uploaderName = uploaderName;
        this.year = year;
        this.uploadTimestamp = uploadTimestamp;
        this.fileUrl = fileUrl;
        this.uploaderId = uploaderId;
    }


    // --- Getters ---
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getDocType() { return docType; }
    public String getSubject() { return subject; }
    public String getTeacher() { return teacher; }
    public String getMajor() { return major; }
    public int getDownloads() { return downloads; }
    public int getLikes() { return likes; }
    public float getRating() { return rating; }
    public String getUploaderName() { return uploaderName; }
    public String getYear() { return year; }
    public long getUploadTimestamp() { return uploadTimestamp; }
    public String getFileUrl() { return fileUrl; }
    public String getUploaderId() { return uploaderId; }

    // --- Setters (Firestore bắt buộc cần tất cả cái này) ---
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setTitle(String title) { this.title = title; }
    public void setDocType(String docType) { this.docType = docType; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public void setMajor(String major) { this.major = major; }
    public void setDownloads(int downloads) { this.downloads = downloads; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setRating(float rating) { this.rating = rating; }

    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
    public void setYear(String year) { this.year = year; }
    public void setUploadTimestamp(long uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public void setDescription(String string) {
    }
}
