package com.smecs.nosql;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.smecs.model.ActivityLog;
import com.smecs.util.MongoDBConnection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO for ActivityLog collection in MongoDB (Epic 4).
 * Handles user activity tracking and analytics.
 */
public class ActivityLogDAO {
    
    private static final String COLLECTION_NAME = "activity_logs";
    
    /**
     * Get MongoDB collection
     */
    private MongoCollection<Document> getCollection() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        return database.getCollection(COLLECTION_NAME);
    }
    
    /**
     * Insert a new activity log
     */
    public String insertLog(ActivityLog log) {
        try {
            Document doc = logToDocument(log);
            getCollection().insertOne(doc);
            return doc.getObjectId("_id").toString();
        } catch (Exception e) {
            System.err.println("Error inserting activity log: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find log by ID
     */
    public ActivityLog findById(String id) {
        try {
            Document doc = getCollection().find(Filters.eq("_id", new ObjectId(id))).first();
            return doc != null ? documentToLog(doc) : null;
        } catch (Exception e) {
            System.err.println("Error finding log by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find logs by user ID
     */
    public List<ActivityLog> findByUserId(int userId, int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.eq("userId", userId))
                .sort(Sorts.descending("timestamp"))
                .limit(limit)
                .forEach(doc -> logs.add(documentToLog(doc)));
        } catch (Exception e) {
            System.err.println("Error finding logs by user ID: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Find logs by event type
     */
    public List<ActivityLog> findByEventType(String eventType, int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.eq("eventType", eventType))
                .sort(Sorts.descending("timestamp"))
                .limit(limit)
                .forEach(doc -> logs.add(documentToLog(doc)));
        } catch (Exception e) {
            System.err.println("Error finding logs by event type: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Find logs by session ID
     */
    public List<ActivityLog> findBySessionId(String sessionId) {
        List<ActivityLog> logs = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.eq("sessionId", sessionId))
                .sort(Sorts.ascending("timestamp"))
                .forEach(doc -> logs.add(documentToLog(doc)));
        } catch (Exception e) {
            System.err.println("Error finding logs by session ID: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Find logs by time range
     */
    public List<ActivityLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        try {
            Date start = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());
            
            getCollection()
                .find(Filters.and(
                    Filters.gte("timestamp", start),
                    Filters.lte("timestamp", end)
                ))
                .sort(Sorts.descending("timestamp"))
                .limit(limit)
                .forEach(doc -> logs.add(documentToLog(doc)));
        } catch (Exception e) {
            System.err.println("Error finding logs by time range: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Find product views for a specific product
     */
    public List<ActivityLog> findProductViews(int productId, int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.and(
                    Filters.eq("eventType", "product_view"),
                    Filters.eq("details.productId", productId)
                ))
                .sort(Sorts.descending("timestamp"))
                .limit(limit)
                .forEach(doc -> logs.add(documentToLog(doc)));
        } catch (Exception e) {
            System.err.println("Error finding product views: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Get event count by type
     */
    public long getEventCount(String eventType) {
        try {
            return getCollection().countDocuments(Filters.eq("eventType", eventType));
        } catch (Exception e) {
            System.err.println("Error counting events: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get user activity count
     */
    public long getUserActivityCount(int userId) {
        try {
            return getCollection().countDocuments(Filters.eq("userId", userId));
        } catch (Exception e) {
            System.err.println("Error counting user activity: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get most popular products (by views)
     */
    public List<Document> getMostPopularProducts(int limit) {
        try {
            List<Document> pipeline = List.of(
                new Document("$match", new Document("eventType", "product_view")),
                new Document("$group", new Document("_id", "$details.productId")
                    .append("viewCount", new Document("$sum", 1))
                    .append("productName", new Document("$first", "$details.productName"))),
                new Document("$sort", new Document("viewCount", -1)),
                new Document("$limit", limit)
            );
            
            List<Document> results = new ArrayList<>();
            getCollection().aggregate(pipeline).forEach(results::add);
            return results;
        } catch (Exception e) {
            System.err.println("Error getting popular products: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get recent logs (for testing/debugging)
     */
    public List<ActivityLog> getRecentLogs(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        try {
            getCollection()
                .find()
                .sort(Sorts.descending("timestamp"))
                .limit(limit)
                .forEach(doc -> logs.add(documentToLog(doc)));
        } catch (Exception e) {
            System.err.println("Error getting recent logs: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Delete old logs (for maintenance)
     */
    public long deleteOldLogs(LocalDateTime beforeDate) {
        try {
            Date cutoffDate = Date.from(beforeDate.atZone(ZoneId.systemDefault()).toInstant());
            return getCollection().deleteMany(Filters.lt("timestamp", cutoffDate)).getDeletedCount();
        } catch (Exception e) {
            System.err.println("Error deleting old logs: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Convert ActivityLog to MongoDB Document
     */
    private Document logToDocument(ActivityLog log) {
        Document doc = new Document();
        
        if (log.getLogId() != null) {
            doc.append("logId", log.getLogId());
        }
        
        doc.append("timestamp", Date.from(log.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
           .append("eventType", log.getEventType())
           .append("userId", log.getUserId())
           .append("sessionId", log.getSessionId())
           .append("details", new Document(log.getDetails()))
           .append("context", new Document(log.getContext()))
           .append("performance", new Document(log.getPerformance()));
        
        return doc;
    }
    
    /**
     * Convert MongoDB Document to ActivityLog
     */
    private ActivityLog documentToLog(Document doc) {
        ActivityLog log = new ActivityLog();
        
        log.setId(doc.getObjectId("_id").toString());
        log.setLogId(doc.getString("logId"));
        
        Date timestamp = doc.getDate("timestamp");
        if (timestamp != null) {
            log.setTimestamp(timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        log.setEventType(doc.getString("eventType"));
        log.setUserId(doc.getInteger("userId", 0));
        log.setSessionId(doc.getString("sessionId"));
        
        Document details = doc.get("details", Document.class);
        if (details != null) {
            log.setDetails(details);
        }
        
        Document context = doc.get("context", Document.class);
        if (context != null) {
            log.setContext(context);
        }
        
        Document performance = doc.get("performance", Document.class);
        if (performance != null) {
            log.setPerformance(performance);
        }
        
        return log;
    }
}

