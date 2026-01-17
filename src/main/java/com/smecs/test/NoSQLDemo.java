package com.smecs.test;

import com.smecs.model.ActivityLog;
import com.smecs.model.ReviewFeedback;
import com.smecs.nosql.ActivityLogDAO;
import com.smecs.nosql.ReviewFeedbackDAO;
import com.smecs.service.FeedbackService;
import com.smecs.util.MongoDBConnection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Demonstration and testing of NoSQL features (Epic 4).
 * Shows MongoDB integration for unstructured data.
 */
public class NoSQLDemo {
    
    private final FeedbackService feedbackService;
    private final ReviewFeedbackDAO reviewDAO;
    private final ActivityLogDAO activityLogDAO;
    
    public NoSQLDemo() {
        this.feedbackService = new FeedbackService();
        this.reviewDAO = new ReviewFeedbackDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }
    
    /**
     * Run all NoSQL demonstrations
     */
    public void runAllDemos() {
        System.out.println("=".repeat(70));
        System.out.println("       SMECS - NoSQL (MongoDB) Integration Demo");
        System.out.println("                    Epic 4");
        System.out.println("=".repeat(70));
        System.out.println();
        
        // Check MongoDB connection
        if (!MongoDBConnection.isConnected()) {
            System.out.println("✗ MongoDB is not connected!");
            System.out.println("Please ensure MongoDB is running on localhost:27017");
            System.out.println("Installation: https://www.mongodb.com/try/download/community");
            return;
        }
        
        System.out.println("✓ MongoDB connected successfully\n");
        System.out.println(MongoDBConnection.getConnectionInfo());
        System.out.println();
        
        // Initialize collections
        MongoDBConnection.initializeCollections();
        System.out.println();
        
        // Run demonstrations
        demonstrateReviewFeatures();
        demonstrateActivityLogging();
        demonstrateAnalytics();
        demonstrateTextSearch();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("       Demo completed successfully!");
        System.out.println("=".repeat(70));
    }
    
    /**
     * Demonstrate review features
     */
    private void demonstrateReviewFeatures() {
        System.out.println("=".repeat(70));
        System.out.println("1. CUSTOMER REVIEW FEATURES");
        System.out.println("=".repeat(70));
        System.out.println();
        
        // Create sample reviews
        System.out.println("Creating sample reviews...");
        
        ReviewFeedback review1 = new ReviewFeedback();
        review1.setUserId(1);
        review1.setUserName("john_doe");
        review1.setUserEmail("john@example.com");
        review1.setProductId(101);
        review1.setProductName("Dell XPS 15 Laptop");
        review1.setRating(5);
        review1.setTitle("Excellent laptop for developers!");
        review1.setReviewText("This laptop has exceeded my expectations. The build quality is " +
                "fantastic, and the performance is amazing. Perfect for development work and " +
                "even gaming in my free time.");
        review1.addPro("Fast performance");
        review1.addPro("Great display");
        review1.addPro("Long battery life");
        review1.addCon("Bit expensive");
        review1.setVerifiedPurchase(true);
        review1.addMetadata("ip_address", "192.168.1.1");
        review1.addMetadata("platform", "web");
        
        String id1 = feedbackService.submitReview(review1);
        System.out.println("✓ Review 1 created: " + id1);
        System.out.println("  Sentiment: " + review1.getSentiment() + " (" + 
                          String.format("%.2f", review1.getSentimentScore()) + ")");
        
        ReviewFeedback review2 = new ReviewFeedback();
        review2.setUserId(2);
        review2.setUserName("jane_smith");
        review2.setUserEmail("jane@example.com");
        review2.setProductId(101);
        review2.setProductName("Dell XPS 15 Laptop");
        review2.setRating(4);
        review2.setTitle("Good laptop, minor issues");
        review2.setReviewText("Overall a good laptop. Performance is excellent, but it does get " +
                "hot under heavy load. Great for productivity.");
        review2.addPro("Excellent performance");
        review2.addPro("Beautiful screen");
        review2.addCon("Runs hot");
        review2.addCon("Price could be lower");
        review2.setVerifiedPurchase(true);
        
        String id2 = feedbackService.submitReview(review2);
        System.out.println("✓ Review 2 created: " + id2);
        System.out.println("  Sentiment: " + review2.getSentiment() + " (" + 
                          String.format("%.2f", review2.getSentimentScore()) + ")");
        
        ReviewFeedback review3 = new ReviewFeedback();
        review3.setUserId(3);
        review3.setUserName("bob_wilson");
        review3.setUserEmail("bob@example.com");
        review3.setProductId(102);
        review3.setProductName("Wireless Mouse");
        review3.setRating(3);
        review3.setTitle("Average mouse");
        review3.setReviewText("It's an okay mouse. Works fine for basic tasks but nothing special.");
        review3.addPro("Affordable");
        review3.addCon("Basic features");
        review3.setVerifiedPurchase(false);
        
        String id3 = feedbackService.submitReview(review3);
        System.out.println("✓ Review 3 created: " + id3);
        System.out.println("  Sentiment: " + review3.getSentiment() + " (" + 
                          String.format("%.2f", review3.getSentimentScore()) + ")");
        
        System.out.println();
        
        // Retrieve reviews
        System.out.println("Retrieving reviews for product 101...");
        List<ReviewFeedback> productReviews = feedbackService.getProductReviews(101);
        System.out.println("Found " + productReviews.size() + " reviews:");
        for (ReviewFeedback review : productReviews) {
            System.out.println("  - " + review.getUserName() + " (" + review.getRating() + "⭐): " + 
                              review.getTitle());
        }
        System.out.println();
        
        // Get review statistics
        System.out.println("Review statistics for product 101:");
        FeedbackService.ReviewStats stats = feedbackService.getReviewStats(101);
        System.out.println("  " + stats);
        System.out.println();
    }
    
    /**
     * Demonstrate activity logging
     */
    private void demonstrateActivityLogging() {
        System.out.println("=".repeat(70));
        System.out.println("2. ACTIVITY LOGGING FEATURES");
        System.out.println("=".repeat(70));
        System.out.println();
        
        System.out.println("Logging user activities...");
        
        // Log product view
        ActivityLog viewLog = new ActivityLog("product_view", 1, "sess_" + UUID.randomUUID().toString().substring(0, 8));
        viewLog.setLogId("LOG-" + System.currentTimeMillis());
        viewLog.addDetail("productId", 101);
        viewLog.addDetail("productName", "Dell XPS 15 Laptop");
        viewLog.addDetail("categoryId", 5);
        viewLog.addDetail("categoryName", "Electronics > Laptops");
        viewLog.addDetail("price", 1299.99);
        viewLog.addDetail("referrer", "/search?q=laptop");
        viewLog.addDetail("duration_seconds", 45);
        viewLog.addContext("device_type", "desktop");
        viewLog.addContext("browser", "Chrome");
        viewLog.addContext("os", "Windows 11");
        viewLog.addPerformance("page_load_time_ms", 342);
        viewLog.addPerformance("api_response_time_ms", 89);
        
        String logId1 = activityLogDAO.insertLog(viewLog);
        System.out.println("✓ Product view logged: " + logId1);
        
        // Log search activity
        ActivityLog searchLog = new ActivityLog("search", 1, viewLog.getSessionId());
        searchLog.setLogId("LOG-" + System.currentTimeMillis());
        searchLog.addDetail("search_term", "gaming laptop");
        searchLog.addDetail("results_count", 47);
        searchLog.addDetail("execution_time_ms", 89);
        searchLog.addDetail("cache_hit", true);
        searchLog.addContext("device_type", "desktop");
        
        String logId2 = activityLogDAO.insertLog(searchLog);
        System.out.println("✓ Search activity logged: " + logId2);
        
        // Log add to cart
        ActivityLog cartLog = new ActivityLog("add_to_cart", 1, viewLog.getSessionId());
        cartLog.setLogId("LOG-" + System.currentTimeMillis());
        cartLog.addDetail("productId", 101);
        cartLog.addDetail("productName", "Dell XPS 15 Laptop");
        cartLog.addDetail("quantity", 1);
        cartLog.addDetail("price", 1299.99);
        
        String logId3 = activityLogDAO.insertLog(cartLog);
        System.out.println("✓ Add to cart logged: " + logId3);
        
        System.out.println();
        
        // Retrieve user activities
        System.out.println("Retrieving activities for user 1...");
        List<ActivityLog> userLogs = activityLogDAO.findByUserId(1, 10);
        System.out.println("Found " + userLogs.size() + " activities:");
        for (ActivityLog log : userLogs) {
            System.out.println("  - " + log.getEventType() + " at " + log.getTimestamp());
        }
        System.out.println();
        
        // Get activity counts
        System.out.println("Activity statistics:");
        long viewCount = activityLogDAO.getEventCount("product_view");
        long searchCount = activityLogDAO.getEventCount("search");
        long cartCount = activityLogDAO.getEventCount("add_to_cart");
        System.out.println("  Product views: " + viewCount);
        System.out.println("  Searches: " + searchCount);
        System.out.println("  Add to cart: " + cartCount);
        System.out.println();
    }
    
    /**
     * Demonstrate analytics queries
     */
    private void demonstrateAnalytics() {
        System.out.println("=".repeat(70));
        System.out.println("3. ANALYTICS & AGGREGATION");
        System.out.println("=".repeat(70));
        System.out.println();
        
        System.out.println("Generating analytics reports...");
        
        // Most popular products
        System.out.println("\nMost Popular Products (by views):");
        var popularProducts = activityLogDAO.getMostPopularProducts(5);
        if (popularProducts.isEmpty()) {
            System.out.println("  No data available yet");
        } else {
            for (var doc : popularProducts) {
                System.out.println("  - Product " + doc.get("_id") + ": " + 
                                  doc.get("viewCount") + " views");
            }
        }
        
        // Review sentiment distribution
        System.out.println("\nReview Sentiment Analysis:");
        List<ReviewFeedback> allReviews = reviewDAO.findAll(100);
        long positiveCount = allReviews.stream().filter(r -> "positive".equals(r.getSentiment())).count();
        long neutralCount = allReviews.stream().filter(r -> "neutral".equals(r.getSentiment())).count();
        long negativeCount = allReviews.stream().filter(r -> "negative".equals(r.getSentiment())).count();
        
        System.out.println("  Positive: " + positiveCount + " reviews");
        System.out.println("  Neutral: " + neutralCount + " reviews");
        System.out.println("  Negative: " + negativeCount + " reviews");
        
        if (!allReviews.isEmpty()) {
            double posPercent = (positiveCount * 100.0) / allReviews.size();
            System.out.println("  Overall sentiment: " + String.format("%.1f%%", posPercent) + " positive");
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrate text search
     */
    private void demonstrateTextSearch() {
        System.out.println("=".repeat(70));
        System.out.println("4. FULL-TEXT SEARCH");
        System.out.println("=".repeat(70));
        System.out.println();
        
        System.out.println("Searching reviews for 'excellent'...");
        List<ReviewFeedback> searchResults = feedbackService.searchReviews("excellent");
        System.out.println("Found " + searchResults.size() + " reviews:");
        for (ReviewFeedback review : searchResults) {
            System.out.println("  - " + review.getTitle());
            System.out.println("    Rating: " + review.getRating() + "⭐, By: " + review.getUserName());
        }
        
        System.out.println();
    }
    
    /**
     * Performance comparison demo
     */
    public void demonstratePerformanceComparison() {
        System.out.println("=".repeat(70));
        System.out.println("5. PERFORMANCE COMPARISON: SQL vs NoSQL");
        System.out.println("=".repeat(70));
        System.out.println();
        
        int iterations = 100;
        
        // Write performance
        System.out.println("Write Performance Test (" + iterations + " operations):");
        
        long mongoWriteStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ActivityLog log = new ActivityLog("test_event", 1, "test_session");
            log.addDetail("iteration", i);
            activityLogDAO.insertLog(log);
        }
        long mongoWriteTime = System.nanoTime() - mongoWriteStart;
        
        System.out.println("  MongoDB writes: " + String.format("%.2f", mongoWriteTime / 1_000_000.0) + " ms");
        System.out.println("  Avg per write: " + String.format("%.3f", mongoWriteTime / 1_000_000.0 / iterations) + " ms");
        
        System.out.println("\nMongoDB excels at:");
        System.out.println("  ✓ High-volume writes (logs, events)");
        System.out.println("  ✓ Flexible schema (no migrations)");
        System.out.println("  ✓ Full-text search");
        System.out.println("  ✓ Horizontal scaling");
        
        System.out.println("\nPostgreSQL excels at:");
        System.out.println("  ✓ Complex JOINs");
        System.out.println("  ✓ ACID transactions");
        System.out.println("  ✓ Data integrity constraints");
        System.out.println("  ✓ Standard SQL queries");
        
        System.out.println("\nHybrid Approach (SMECS):");
        System.out.println("  • PostgreSQL: Products, Orders, Inventory, Users");
        System.out.println("  • MongoDB: Reviews, Activity Logs, Search Logs");
        System.out.println("  → Best of both worlds!");
        
        System.out.println();
    }
    
    /**
     * Main method
     */
    public static void main(String[] args) {
        NoSQLDemo demo = new NoSQLDemo();
        
        try {
            demo.runAllDemos();
            demo.demonstratePerformanceComparison();
        } catch (Exception e) {
            System.err.println("Error running demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Keep connection open for continued use
            System.out.println("\nMongoDB connection remains open for application use.");
            System.out.println("Call MongoDBConnection.close() to close when done.");
        }
    }
}

