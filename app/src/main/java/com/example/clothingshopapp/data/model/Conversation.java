package com.example.clothingshopapp.data.model; // (Package của "bố")

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName; // ⭐ IMPORT CÁI NÀY
import java.util.List;

public class Conversation {

    // Tên biến phải GIỐNG HỆT tên trường trên Firestore
    private List<String> participants;
    private String lastMessage;
    private Timestamp lastTimestamp;
    private long shopUnreadCount;
    private long userUnreadCount; // Dòng này bạn đã thêm
    private String userFullName;
    private String userAvatarUrl;

    public Conversation() {} // Constructor rỗng

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(Timestamp lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public long getShopUnreadCount() {
        return shopUnreadCount;
    }

    public void setShopUnreadCount(long shopUnreadCount) {
        this.shopUnreadCount = shopUnreadCount;
    }

    // ----- SỬA CHỖ NÀY CHO "ĐÓNG ĐINH" -----
    // Thêm @PropertyName để "ép" Firestore nó phải thấy,
    // bất kể cái build cache nó có "lú" hay không.

    @PropertyName("userUnreadCount") // ⭐ THÊM DÒNG NÀY
    public long getUserUnreadCount() {
        return userUnreadCount;
    }

    @PropertyName("userUnreadCount") // ⭐ THÊM DÒNG NÀY
    public void setUserUnreadCount(long userUnreadCount) {
        this.userUnreadCount = userUnreadCount;
    }
    // ----- KẾT THÚC SỬA -----

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
}