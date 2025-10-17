package com.restaurant.vip.dto;

public class FileUploadResponse {
    
    private String filename;
    private String url;
    private long size;
    private String contentType;
    private String message;
    
    // Constructors
    public FileUploadResponse() {}
    
    public FileUploadResponse(String filename, String url, long size, String contentType) {
        this.filename = filename;
        this.url = url;
        this.size = size;
        this.contentType = contentType;
        this.message = "File uploaded successfully";
    }
    
    public FileUploadResponse(String filename, String url, long size, String contentType, String message) {
        this.filename = filename;
        this.url = url;
        this.size = size;
        this.contentType = contentType;
        this.message = message;
    }
    
    // Getters and Setters
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}