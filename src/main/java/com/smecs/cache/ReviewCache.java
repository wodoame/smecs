//package com.smecs.cache;
//
//import com.smecs.model.ReviewFeedback;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * In-memory cache for ReviewFeedback with TTL-based invalidation.
// */
//public class ReviewCache {
//    private static ReviewCache instance;
//
//    // Cache: reviewId -> ReviewFeedback
//    private final Map<String, ReviewFeedback> reviewById;
//
//    // Index: productId -> List<ReviewFeedback>
//    private final Map<Integer, List<ReviewFeedback>> reviewsByProductId;
//
//    // Cache for all reviews
//    private List<ReviewFeedback> allReviewsCache;
//    private long allReviewsCacheTimestamp;
//
//    // Timestamps for product specific caches
//    private final Map<Integer, Long> productReviewsTimestamp;
//
//    // Cache configuration
//    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
//
//    // Statistics
//    private long cacheHits = 0;
//    private long cacheMisses = 0;
//
//    private ReviewCache() {
//        this.reviewById = new ConcurrentHashMap<>();
//        this.reviewsByProductId = new ConcurrentHashMap<>();
//        this.productReviewsTimestamp = new ConcurrentHashMap<>();
//        this.allReviewsCache = null;
//        this.allReviewsCacheTimestamp = 0;
//    }
//
//    public static synchronized ReviewCache getInstance() {
//        if (instance == null) {
//            instance = new ReviewCache();
//        }
//        return instance;
//    }
//
//    /**
//     * Get all reviews from cache if valid.
//     */
//    public Optional<List<ReviewFeedback>> getAllReviews() {
//        if (allReviewsCache != null && !isCacheExpired(allReviewsCacheTimestamp)) {
//            cacheHits++;
//            return Optional.of(new ArrayList<>(allReviewsCache));
//        }
//        cacheMisses++;
//        return Optional.empty();
//    }
//
//    /**
//     * Put all reviews into cache.
//     */
//    public void putAllReviews(List<ReviewFeedback> reviews) {
//        this.allReviewsCache = new ArrayList<>(reviews);
//        this.allReviewsCacheTimestamp = System.currentTimeMillis();
//
//        // Also update individual lookups
//        for (ReviewFeedback review : reviews) {
//            cacheReview(review);
//        }
//    }
//
//    /**
//     * Get reviews for a product from cache.
//     */
//    public Optional<List<ReviewFeedback>> getReviewsByProductId(int productId) {
//        if (reviewsByProductId.containsKey(productId)) {
//            Long timestamp = productReviewsTimestamp.get(productId);
//            if (timestamp != null && !isCacheExpired(timestamp)) {
//                cacheHits++;
//                return Optional.of(new ArrayList<>(reviewsByProductId.get(productId)));
//            }
//        }
//        cacheMisses++;
//        return Optional.empty();
//    }
//
//    /**
//     * Put product reviews into cache.
//     */
//    public void putProductReviews(int productId, List<ReviewFeedback> reviews) {
//        reviewsByProductId.put(productId, new ArrayList<>(reviews));
//        productReviewsTimestamp.put(productId, System.currentTimeMillis());
//
//        // Update review by ID map
//        for (ReviewFeedback review : reviews) {
//            if (review.getId() != null) {
//                reviewById.put(review.getId(), review);
//            }
//        }
//    }
//
//    /**
//     * Invalidate cache for a specific product.
//     */
//    public void invalidateProductReviews(int productId) {
//        reviewsByProductId.remove(productId);
//        productReviewsTimestamp.remove(productId);
//        allReviewsCache = null; // Invalidate all reviews list too as it's stale
//    }
//
//    /**
//     * Invalidate the entire cache.
//     */
//    public void invalidateAll() {
//        reviewById.clear();
//        reviewsByProductId.clear();
//        productReviewsTimestamp.clear();
//        allReviewsCache = null;
//        allReviewsCacheTimestamp = 0;
//    }
//
//    private void cacheReview(ReviewFeedback review) {
//        if (review.getId() != null) {
//            reviewById.put(review.getId(), review);
//        }
//
//        // Update product index
//        reviewsByProductId.computeIfAbsent(review.getProductId(), k -> new ArrayList<>()).add(review);
//    }
//
//    private boolean isCacheExpired(long timestamp) {
//        return (System.currentTimeMillis() - timestamp) > CACHE_TTL_MS;
//    }
//
//    public long getCacheHits() {
//        return cacheHits;
//    }
//
//    public long getCacheMisses() {
//        return cacheMisses;
//    }
//}
