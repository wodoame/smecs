package com.smecs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model for customer review feedback stored in MongoDB (Epic 4).
 * Represents unstructured review data with flexible schema.
 */
public class ReviewFeedback {
    
    private String id; // MongoDB ObjectId as string
    private String reviewId;
    private int userId;
    private String userName;
    private String userEmail;
    private int productId;
    private String productName;
    private int rating; // 1-5
    private String title;
    private String reviewText;
    private List<String> pros;
    private List<String> cons;
    private List<String> images;
    private boolean verifiedPurchase;
    private int helpfulCount;
    private int notHelpfulCount;
    private String sentiment; // positive, negative, neutral
    private double sentimentScore; // 0.0 to 1.0
    private String moderationStatus; // pending, approved, rejected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
    
    public ReviewFeedback() {
        this.pros = new ArrayList<>();
        this.cons = new ArrayList<>();
        this.images = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.moderationStatus = "pending";
        this.helpfulCount = 0;
        this.notHelpfulCount = 0;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getReviewText() {
        return reviewText;
    }
    
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
    
    public List<String> getPros() {
        return pros;
    }
    
    public void setPros(List<String> pros) {
        this.pros = pros;
    }
    
    public void addPro(String pro) {
        this.pros.add(pro);
    }
    
    public List<String> getCons() {
        return cons;
    }
    
    public void setCons(List<String> cons) {
        this.cons = cons;
    }
    
    public void addCon(String con) {
        this.cons.add(con);
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public void addImage(String imageUrl) {
        this.images.add(imageUrl);
    }
    
    public boolean isVerifiedPurchase() {
        return verifiedPurchase;
    }
    
    public void setVerifiedPurchase(boolean verifiedPurchase) {
        this.verifiedPurchase = verifiedPurchase;
    }
    
    public int getHelpfulCount() {
        return helpfulCount;
    }
    
    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }
    
    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }
    
    public int getNotHelpfulCount() {
        return notHelpfulCount;
    }
    
    public void setNotHelpfulCount(int notHelpfulCount) {
        this.notHelpfulCount = notHelpfulCount;
    }
    
    public void incrementNotHelpfulCount() {
        this.notHelpfulCount++;
    }
    
    public String getSentiment() {
        return sentiment;
    }
    
    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
    
    public double getSentimentScore() {
        return sentimentScore;
    }
    
    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }
    
    public String getModerationStatus() {
        return moderationStatus;
    }
    
    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    @Override
    public String toString() {
        return "ReviewFeedback{" +
                "reviewId='" + reviewId + '\'' +
                ", userName='" + userName + '\'' +
                ", productName='" + productName + '\'' +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", sentiment='" + sentiment + '\'' +
                ", verifiedPurchase=" + verifiedPurchase +
                ", helpfulCount=" + helpfulCount +
                '}';
    }
}

