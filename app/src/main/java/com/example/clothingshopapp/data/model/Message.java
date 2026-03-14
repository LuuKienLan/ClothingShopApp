package com.example.clothingshopapp.model; // (Hoặc package của "bố")

import com.google.firebase.Timestamp;

public class Message {
    private String text;
    private String senderId;
    private Timestamp timestamp;

    // Constructor rỗng cho Firestore
    public Message() {}

    public Message(String text, String senderId, Timestamp timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Getters và Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}