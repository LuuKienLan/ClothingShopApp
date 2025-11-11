package com.example.clothingshopapp.data.model;

public class ChatMessage {
    private String text;
    private String senderId;
    private long timestamp;

    public ChatMessage() {
        // Bắt buộc cho Firebase
    }

    public ChatMessage(String text, String senderId, long timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Getters (Bắt buộc cho Firebase)
    public String getText() {
        return text;
    }
    public String getSenderId() {
        return senderId;
    }
    public long getTimestamp() {
        return timestamp;
    }
}