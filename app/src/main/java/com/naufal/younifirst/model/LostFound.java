package com.naufal.younifirst.model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LostFound {
    private String id;
    private String itemName;
    private String description;
    private String location;
    private String status; // "lost" atau "found"
    private String userId;
    private String userName;
    private String userImage;

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    private String Category;
    private String itemImage;
    private String createdAt;
    private String updatedAt;
    private int likes;
    private int comments;

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String phone;
    private String email;
    private boolean isClaimed;

    // Konstruktor
    public LostFound() {}

    public LostFound(String id, String itemName, String description, String location,
                     String status, String Category, String userId, String userName, String itemImage, String email, String phone) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.status = status;
        this.userId = userId;
        this.Category = Category;
        this.userName = userName;
        this.phone = phone;
        this.email = email;
        this.itemImage = itemImage;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserImage() { return userImage; }
    public void setUserImage(String userImage) { this.userImage = userImage; }

    public String getItemImage() { return itemImage; }
    public void setItemImage(String itemImage) { this.itemImage = itemImage; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

    public boolean isClaimed() { return isClaimed; }
    public void setClaimed(boolean claimed) { isClaimed = claimed; }

    public String getTimeAgo() {
        try {
            if (createdAt == null || createdAt.isEmpty()) {
                return "Baru saja";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date createdDate = sdf.parse(createdAt);
            Date currentDate = new Date();

            long diff = currentDate.getTime() - createdDate.getTime();
            long diffMinutes = diff / (60 * 1000);
            long diffHours = diff / (60 * 60 * 1000);
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays > 0) {
                return diffDays + " hari lalu";
            } else if (diffHours > 0) {
                return diffHours + " jam lalu";
            } else if (diffMinutes > 0) {
                return diffMinutes + " menit lalu";
            } else {
                return "Baru saja";
            }
        } catch (Exception e) {
            Log.e("LOSTFOUND_MODEL", "Error calculating time ago: " + e.getMessage());
            return "Baru saja";
        }
    }
}