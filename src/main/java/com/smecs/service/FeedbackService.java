package com.smecs.service;

import com.smecs.model.ReviewFeedback;
import com.smecs.nosql.ReviewFeedbackDAO;
import com.smecs.util.MongoDBConnection;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing customer reviews using MongoDB (Epic 4).
 */
public class FeedbackService {
    
    private final ReviewFeedbackDAO reviewDAO;
    
    public FeedbackService() {
        this.reviewDAO = new ReviewFeedbackDAO();
    }
    
    /**
     * Submit a new review
     */
    public String submitReview(ReviewFeedback review) {
        // Validate review
        if (!isValidReview(review)) {
            throw new IllegalArgumentException("Invalid review data");
        }
        
        // Generate review ID if not present
        if (review.getReviewId() == null) {
            review.setReviewId("REV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        // Auto-detect sentiment (simple implementation)
        analyzeSentiment(review);
        
        // Insert review
        return reviewDAO.insertReview(review);
    }
    
    /**
     * Get reviews for a product
     */
    public List<ReviewFeedback> getProductReviews(int productId) {
        return reviewDAO.findByProductId(productId);
    }
    
    /**
     * Get reviews for a product with limit
     */
    public List<ReviewFeedback> getProductReviews(int productId, int limit) {
        return reviewDAO.findByProductId(productId, limit);
    }
    
    /**
     * Get reviews by user
     */
    public List<ReviewFeedback> getUserReviews(int userId) {
        return reviewDAO.findByUserId(userId);
    }
    
    /**
     * Get positive reviews for a product
     */
    public List<ReviewFeedback> getPositiveReviews(int productId) {
        return reviewDAO.findByRating(productId, 4); // 4 and 5 stars
    }
    
    /**
     * Search reviews by text
     */
    public List<ReviewFeedback> searchReviews(String searchText) {
        return reviewDAO.searchReviews(searchText);
    }
    
    /**
     * Mark review as helpful
     */
    public boolean markReviewHelpful(String reviewId, boolean isHelpful) {
        return reviewDAO.updateHelpfulCount(reviewId, isHelpful);
    }
    
    /**
     * Moderate review (approve/reject)
     */
    public boolean moderateReview(String reviewId, String status) {
        if (!status.equals("approved") && !status.equals("rejected")) {
            throw new IllegalArgumentException("Invalid moderation status");
        }
        return reviewDAO.updateModerationStatus(reviewId, status);
    }
    
    /**
     * Delete review
     */
    public boolean deleteReview(String reviewId) {
        return reviewDAO.deleteReview(reviewId);
    }
    
    /**
     * Get review statistics for a product
     */
    public ReviewStats getReviewStats(int productId) {
        ReviewStats stats = new ReviewStats();
        stats.averageRating = reviewDAO.getAverageRating(productId);
        stats.totalReviews = reviewDAO.getReviewCount(productId);
        return stats;
    }
    
    /**
     * Check if MongoDB is available
     */
    public boolean isMongoDBAvailable() {
        return MongoDBConnection.isConnected();
    }
    
    /**
     * Validate review data
     */
    private boolean isValidReview(ReviewFeedback review) {
        if (review.getUserId() <= 0) return false;
        if (review.getProductId() <= 0) return false;
        if (review.getRating() < 1 || review.getRating() > 5) return false;
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) return false;
        return true;
    }
    
    /**
     * Simple sentiment analysis based on rating and keywords
     */
    private void analyzeSentiment(ReviewFeedback review) {
        String text = (review.getTitle() + " " + review.getReviewText()).toLowerCase();
        
        // Positive keywords
        String[] positiveWords = {"excellent", "great", "amazing", "wonderful", "perfect", 
                                  "love", "best", "fantastic", "awesome", "good"};
        // Negative keywords
        String[] negativeWords = {"terrible", "bad", "worst", "poor", "hate", 
                                  "disappointing", "awful", "horrible", "useless"};
        
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String word : positiveWords) {
            if (text.contains(word)) positiveCount++;
        }
        
        for (String word : negativeWords) {
            if (text.contains(word)) negativeCount++;
        }
        
        // Determine sentiment
        if (review.getRating() >= 4 || positiveCount > negativeCount) {
            review.setSentiment("positive");
            review.setSentimentScore(0.7 + (review.getRating() * 0.05));
        } else if (review.getRating() <= 2 || negativeCount > positiveCount) {
            review.setSentiment("negative");
            review.setSentimentScore(0.3 - (review.getRating() * 0.05));
        } else {
            review.setSentiment("neutral");
            review.setSentimentScore(0.5);
        }
    }
    
    /**
     * Review statistics class
     */
    public static class ReviewStats {
        public double averageRating;
        public long totalReviews;
        
        @Override
        public String toString() {
            return String.format("Avg Rating: %.1f, Total Reviews: %d", averageRating, totalReviews);
        }
    }
}

