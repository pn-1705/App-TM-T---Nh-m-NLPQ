package com.example.appthuongmaidientu.model;

public class NotifyList {
    String id,status,content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotifyList(String id, String status, String content) {
        this.id = id;
        this.status = status;
        this.content = content;
    }
}
