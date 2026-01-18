package com.smecs.nosql;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.smecs.model.ReviewFeedback;
import com.smecs.util.MongoDBConnection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO for ReviewFeedback collection in MongoDB (Epic 4).
 * Handles CRUD operations for customer reviews and feedback.
 */
public class ReviewFeedbackDAO {
    
    private static final String COLLECTION_NAME = "reviews_feedback";
    
    /**
     * Get MongoDB collection
     */
    private MongoCollection<Document> getCollection() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        return database.getCollection(COLLECTION_NAME);
    }
    
    /**
     * Insert a new review
     */
    public String insertReview(ReviewFeedback review) {
        try {
            Document doc = reviewToDocument(review);
            getCollection().insertOne(doc);
            return doc.getObjectId("_id").toString();
        } catch (Exception e) {
            System.err.println("Error inserting review: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find review by ID
     */
    public ReviewFeedback findById(String id) {
        try {
            Document doc = getCollection().find(Filters.eq("_id", new ObjectId(id))).first();
            return doc != null ? documentToReview(doc) : null;
        } catch (Exception e) {
            System.err.println("Error finding review by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find reviews by product ID
     */
    public List<ReviewFeedback> findByProductId(int productId) {
        return findByProductId(productId, 50); // Default limit
    }
    
    /**
     * Find reviews by product ID with limit
     */
    public List<ReviewFeedback> findByProductId(int productId, int limit) {
        List<ReviewFeedback> reviews = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.eq("productId", productId))
                .sort(Sorts.descending("created_at"))
                .limit(limit)
                .forEach(doc -> reviews.add(documentToReview(doc)));
        } catch (Exception e) {
            System.err.println("Error finding reviews by product ID: " + e.getMessage());
        }
        return reviews;
    }
    
    /**
     * Find reviews by user ID
     */
    public List<ReviewFeedback> findByUserId(int userId) {
        List<ReviewFeedback> reviews = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.eq("userId", userId))
                .sort(Sorts.descending("created_at"))
                .forEach(doc -> reviews.add(documentToReview(doc)));
        } catch (Exception e) {
            System.err.println("Error finding reviews by user ID: " + e.getMessage());
        }
        return reviews;
    }
    
    /**
     * Find reviews by rating
     */
    public List<ReviewFeedback> findByRating(int productId, int minRating) {
        List<ReviewFeedback> reviews = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.and(
                    Filters.eq("productId", productId),
                    Filters.gte("rating", minRating)
                ))
                .sort(Sorts.descending("created_at"))
                .forEach(doc -> reviews.add(documentToReview(doc)));
        } catch (Exception e) {
            System.err.println("Error finding reviews by rating: " + e.getMessage());
        }
        return reviews;
    }
    
    /**
     * Search reviews by text
     */
    public List<ReviewFeedback> searchReviews(String searchText) {
        List<ReviewFeedback> reviews = new ArrayList<>();
        try {
            getCollection()
                .find(Filters.text(searchText))
                .limit(50)
                .forEach(doc -> reviews.add(documentToReview(doc)));
        } catch (Exception e) {
            System.err.println("Error searching reviews: " + e.getMessage());
        }
        return reviews;
    }
    
    /**
     * Update review helpful count
     */
    public boolean updateHelpfulCount(String id, boolean isHelpful) {
        try {
            String field = isHelpful ? "helpful_count" : "not_helpful_count";
            Document update = new Document("$inc", new Document(field, 1));
            getCollection().updateOne(Filters.eq("_id", new ObjectId(id)), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating helpful count: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update moderation status
     */
    public boolean updateModerationStatus(String id, String status) {
        try {
            Document update = new Document("$set", new Document("moderation_status", status));
            getCollection().updateOne(Filters.eq("_id", new ObjectId(id)), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating moderation status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete review by ID
     */
    public boolean deleteReview(String id) {
        try {
            getCollection().deleteOne(Filters.eq("_id", new ObjectId(id)));
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting review: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get average rating for a product
     */
    public double getAverageRating(int productId) {
        try {
            List<Document> pipeline = List.of(
                new Document("$match", new Document("productId", productId)),
                new Document("$group", new Document("_id", "$productId")
                    .append("avgRating", new Document("$avg", "$rating")))
            );
            
            Document result = getCollection().aggregate(pipeline).first();
            return result != null ? result.getDouble("avgRating") : 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Get review count for a product
     */
    public long getReviewCount(int productId) {
        try {
            return getCollection().countDocuments(Filters.eq("productId", productId));
        } catch (Exception e) {
            System.err.println("Error counting reviews: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Find all reviews with limit
     */
    public List<ReviewFeedback> findAll(int limit) {
        List<ReviewFeedback> reviews = new ArrayList<>();
        try {
            getCollection()
                .find()
                .sort(Sorts.descending("created_at"))
                .limit(limit)
                .forEach(doc -> reviews.add(documentToReview(doc)));
        } catch (Exception e) {
            System.err.println("Error finding all reviews: " + e.getMessage());
        }
        return reviews;
    }
    
    /**
     * Convert ReviewFeedback to MongoDB Document
     */
    private Document reviewToDocument(ReviewFeedback review) {
        Document doc = new Document();
        
        if (review.getReviewId() != null) {
            doc.append("reviewId", review.getReviewId());
        }
        
        doc.append("userId", review.getUserId())
           .append("userName", review.getUserName())
           .append("userEmail", review.getUserEmail())
           .append("productId", review.getProductId())
           .append("productName", review.getProductName())
           .append("rating", review.getRating())
           .append("title", review.getTitle())
           .append("reviewText", review.getReviewText())
           .append("pros", review.getPros())
           .append("cons", review.getCons())
           .append("images", review.getImages())
           .append("verified_purchase", review.isVerifiedPurchase())
           .append("helpful_count", review.getHelpfulCount())
           .append("not_helpful_count", review.getNotHelpfulCount())
           .append("sentiment", review.getSentiment())
           .append("sentiment_score", review.getSentimentScore())
           .append("moderation_status", review.getModerationStatus())
           .append("created_at", Date.from(review.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()))
           .append("updated_at", Date.from(review.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()))
           .append("metadata", review.getMetadata());
        
        return doc;
    }
    
    /**
     * Convert MongoDB Document to ReviewFeedback
     */
    private ReviewFeedback documentToReview(Document doc) {
        ReviewFeedback review = new ReviewFeedback();
        
        review.setId(doc.getObjectId("_id").toString());
        review.setReviewId(doc.getString("reviewId"));
        review.setUserId(doc.getInteger("userId", 0));
        review.setUserName(doc.getString("userName"));
        review.setUserEmail(doc.getString("userEmail"));
        review.setProductId(doc.getInteger("productId", 0));
        review.setProductName(doc.getString("productName"));
        review.setRating(doc.getInteger("rating", 0));
        review.setTitle(doc.getString("title"));
        review.setReviewText(doc.getString("reviewText"));
        review.setPros(doc.getList("pros", String.class, new ArrayList<>()));
        review.setCons(doc.getList("cons", String.class, new ArrayList<>()));
        review.setImages(doc.getList("images", String.class, new ArrayList<>()));
        review.setVerifiedPurchase(doc.getBoolean("verified_purchase", false));
        review.setHelpfulCount(doc.getInteger("helpful_count", 0));
        review.setNotHelpfulCount(doc.getInteger("not_helpful_count", 0));
        review.setSentiment(doc.getString("sentiment"));
        review.setSentimentScore(doc.getDouble("sentiment_score"));
        review.setModerationStatus(doc.getString("moderation_status"));
        
        Date createdAt = doc.getDate("created_at");
        if (createdAt != null) {
            review.setCreatedAt(createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        Date updatedAt = doc.getDate("updated_at");
        if (updatedAt != null) {
            review.setUpdatedAt(updatedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        Document metadata = doc.get("metadata", Document.class);
        if (metadata != null) {
            review.setMetadata(metadata);
        }
        
        return review;
    }
}
