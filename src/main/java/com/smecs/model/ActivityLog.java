package com.smecs.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Model for user activity logs stored in MongoDB (Epic 4).
 * Tracks user interactions and events for analytics.
 */
public class ActivityLog {
    
    private String id; // MongoDB ObjectId as string
    private String logId;
    private LocalDateTime timestamp;
    private String eventType; // product_view, search, add_to_cart, purchase, etc.
    private int userId;
    private String sessionId;
    private Map<String, Object> details;
    private Map<String, Object> context;
    private Map<String, Object> performance;
    
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
        this.context = new HashMap<>();
        this.performance = new HashMap<>();
    }
    
    public ActivityLog(String eventType, int userId, String sessionId) {
        this();
        this.eventType = eventType;
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getLogId() {
        return logId;
    }
    
    public void setLogId(String logId) {
        this.logId = logId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
    
    public void addContext(String key, Object value) {
        this.context.put(key, value);
    }
    
    public Map<String, Object> getPerformance() {
        return performance;
    }
    
    public void setPerformance(Map<String, Object> performance) {
        this.performance = performance;
    }
    
    public void addPerformance(String key, Object value) {
        this.performance.put(key, value);
    }
    
    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId='" + logId + '\'' +
                ", timestamp=" + timestamp +
                ", eventType='" + eventType + '\'' +
                ", userId=" + userId +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}

