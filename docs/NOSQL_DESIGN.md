# NoSQL Design for Smart E-Commerce System (SMECS)

## Epic 4: NoSQL Integration for Unstructured Data

**Date:** January 15, 2026  
**Author:** SMECS Development Team  
**Purpose:** Document the NoSQL database design for handling unstructured and semi-structured data in SMECS

---

## Table of Contents
1. [Overview](#overview)
2. [Why NoSQL?](#why-nosql)
3. [MongoDB Schema Design](#mongodb-schema-design)
4. [Data Models](#data-models)
5. [Comparison: Relational vs NoSQL](#comparison-relational-vs-nosql)
6. [Integration Architecture](#integration-architecture)
7. [Use Cases](#use-cases)
8. [Performance Considerations](#performance-considerations)

---

## Overview

While the core SMECS system uses PostgreSQL for structured transactional data (products, orders, inventory), certain types of data are better suited for NoSQL storage. This document describes the integration of MongoDB for handling unstructured and semi-structured data.

### Scope
This NoSQL implementation covers:
- **Customer Reviews & Feedback** - Rich text with variable structure
- **User Activity Logs** - High-volume event tracking
- **Product View History** - User behavior analytics
- **Search Query Logs** - Search analytics and optimization

---

## Why NoSQL?

### Advantages for SMECS

| Feature | Relational (PostgreSQL) | NoSQL (MongoDB) | Best For |
|---------|------------------------|-----------------|----------|
| **Schema** | Fixed, normalized | Flexible, denormalized | Relational: Structured data<br>NoSQL: Variable structure |
| **Scalability** | Vertical (scale up) | Horizontal (scale out) | NoSQL: High-volume data |
| **Joins** | Efficient, built-in | Manual, application-level | Relational: Complex relationships |
| **Write Speed** | Moderate (ACID overhead) | Fast (eventual consistency) | NoSQL: High write throughput |
| **Query Flexibility** | SQL - powerful & standard | JSON queries - flexible | Relational: Complex queries<br>NoSQL: Document retrieval |
| **Text Search** | Limited (requires extensions) | Built-in, powerful | NoSQL: Full-text search |

### Use Cases Perfect for NoSQL in SMECS

1. **Customer Reviews/Feedback**
   - Variable structure (some with images, videos, ratings)
   - Rich text content
   - Rapid writes (no need for complex transactions)
   - Full-text search requirements

2. **Activity Logs**
   - High write volume
   - Time-series data
   - No need for complex joins
   - Append-only operations

3. **User Behavior Analytics**
   - Product view tracking
   - Search pattern analysis
   - Session data
   - A/B testing data

---

## MongoDB Schema Design

### Database Structure
```
Database: smecs_nosql
├── Collections:
│   ├── reviews_feedback
│   ├── activity_logs
│   ├── product_views
│   └── search_logs
```

### Collection Design Philosophy

**Denormalization Strategy:**
- Embed related data that is frequently accessed together
- Optimize for read performance
- Accept some data duplication
- Minimize application-level joins

---

## Data Models

### 1. Reviews Feedback Collection

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6789abcdef0"),
  "reviewId": "REV-2026-001234",
  "userId": 42,
  "userName": "john_doe",
  "userEmail": "john@example.com",
  "productId": 101,
  "productName": "Dell XPS 15 Laptop",
  "rating": 5,
  "title": "Excellent laptop for developers",
  "reviewText": "This laptop has exceeded my expectations...",
  "pros": [
    "Fast performance",
    "Great display",
    "Long battery life"
  ],
  "cons": [
    "Bit expensive",
    "Runs hot under load"
  ],
  "images": [
    "https://cdn.smecs.com/reviews/img1.jpg",
    "https://cdn.smecs.com/reviews/img2.jpg"
  ],
  "verified_purchase": true,
  "helpful_count": 42,
  "not_helpful_count": 2,
  "sentiment": "positive",
  "sentiment_score": 0.92,
  "moderation_status": "approved",
  "created_at": ISODate("2026-01-10T14:30:00Z"),
  "updated_at": ISODate("2026-01-10T14:30:00Z"),
  "metadata": {
    "ip_address": "192.168.1.1",
    "user_agent": "Mozilla/5.0...",
    "platform": "web"
  }
}
```

**Indexes:**
```javascript
db.reviews_feedback.createIndex({ "productId": 1, "created_at": -1 })
db.reviews_feedback.createIndex({ "userId": 1 })
db.reviews_feedback.createIndex({ "rating": 1 })
db.reviews_feedback.createIndex({ "reviewText": "text", "title": "text" })
db.reviews_feedback.createIndex({ "sentiment": 1 })
```

**Why This Structure?**
- Embedded user info eliminates JOIN with Users table
- Array fields (pros/cons) store variable-length data
- Rich metadata without schema changes
- Full-text search on review content

---

### 2. Activity Logs Collection

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6789abcdef1"),
  "logId": "LOG-2026-001234567",
  "timestamp": ISODate("2026-01-15T10:23:45Z"),
  "eventType": "product_view",
  "userId": 42,
  "sessionId": "sess_abc123xyz",
  "details": {
    "productId": 101,
    "productName": "Dell XPS 15 Laptop",
    "categoryId": 5,
    "categoryName": "Electronics > Laptops",
    "price": 1299.99,
    "referrer": "/search?q=laptop",
    "duration_seconds": 45
  },
  "context": {
    "ip_address": "192.168.1.1",
    "user_agent": "Mozilla/5.0...",
    "device_type": "desktop",
    "browser": "Chrome",
    "os": "Windows 11",
    "location": {
      "country": "USA",
      "state": "CA",
      "city": "San Francisco"
    }
  },
  "performance": {
    "page_load_time_ms": 342,
    "api_response_time_ms": 89
  }
}
```

**Indexes:**
```javascript
db.activity_logs.createIndex({ "timestamp": -1 })
db.activity_logs.createIndex({ "userId": 1, "timestamp": -1 })
db.activity_logs.createIndex({ "eventType": 1, "timestamp": -1 })
db.activity_logs.createIndex({ "details.productId": 1 })
db.activity_logs.createIndex({ "sessionId": 1 })
```

**Why This Structure?**
- Time-series optimized (timestamp index)
- Flexible event details (no schema constraints)
- Rich context for analytics
- Fast writes for high-volume logging

---

### 3. Product Views Collection (Aggregated)

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6789abcdef2"),
  "productId": 101,
  "date": ISODate("2026-01-15T00:00:00Z"),
  "metrics": {
    "total_views": 1250,
    "unique_users": 892,
    "avg_view_duration_seconds": 38.5,
    "bounce_rate": 0.23,
    "conversion_rate": 0.048
  },
  "hourly_breakdown": [
    { "hour": 0, "views": 45 },
    { "hour": 1, "views": 32 },
    // ... 24 hours
  ],
  "top_referrers": [
    { "source": "search", "count": 567 },
    { "source": "category_page", "count": 423 },
    { "source": "recommendations", "count": 260 }
  ],
  "device_breakdown": {
    "desktop": 689,
    "mobile": 456,
    "tablet": 105
  }
}
```

**Indexes:**
```javascript
db.product_views.createIndex({ "productId": 1, "date": -1 })
db.product_views.createIndex({ "date": -1 })
```

---

### 4. Search Logs Collection

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6789abcdef3"),
  "searchId": "SEARCH-2026-001234567",
  "timestamp": ISODate("2026-01-15T10:23:45Z"),
  "userId": 42,
  "sessionId": "sess_abc123xyz",
  "query": {
    "search_term": "gaming laptop",
    "normalized_term": "gaming laptop",
    "filters": {
      "category": "Electronics",
      "price_min": 800,
      "price_max": 2000,
      "sort_by": "price_desc"
    }
  },
  "results": {
    "total_found": 47,
    "displayed": 20,
    "execution_time_ms": 89,
    "cache_hit": true
  },
  "user_interaction": {
    "clicked_products": [101, 105, 112],
    "first_click_position": 3,
    "time_to_first_click_seconds": 5.2,
    "added_to_cart": [101]
  },
  "context": {
    "referrer": "/",
    "device_type": "desktop"
  }
}
```

**Indexes:**
```javascript
db.search_logs.createIndex({ "timestamp": -1 })
db.search_logs.createIndex({ "query.search_term": 1 })
db.search_logs.createIndex({ "userId": 1, "timestamp": -1 })
db.search_logs.createIndex({ "query.normalized_term": "text" })
```

---

## Comparison: Relational vs NoSQL

### Scenario 1: Storing Product Reviews

#### Relational Approach (PostgreSQL)
```sql
-- Multiple normalized tables
CREATE TABLE Reviews (
    review_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES Users,
    product_id INTEGER REFERENCES Products,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(200),
    review_text TEXT,
    verified_purchase BOOLEAN,
    created_at TIMESTAMP
);

CREATE TABLE ReviewImages (
    image_id SERIAL PRIMARY KEY,
    review_id INTEGER REFERENCES Reviews,
    image_url TEXT
);

CREATE TABLE ReviewHelpfulness (
    vote_id SERIAL PRIMARY KEY,
    review_id INTEGER REFERENCES Reviews,
    user_id INTEGER REFERENCES Users,
    is_helpful BOOLEAN
);

-- Complex query with multiple JOINs
SELECT r.*, u.username, p.product_name,
       COUNT(rh.vote_id) as helpful_count
FROM Reviews r
JOIN Users u ON r.user_id = u.user_id
JOIN Products p ON r.product_id = p.product_id
LEFT JOIN ReviewHelpfulness rh ON r.review_id = rh.review_id 
                                AND rh.is_helpful = true
GROUP BY r.review_id, u.username, p.product_name;
```

**Pros:**
- Data integrity enforced
- No duplication
- Complex queries supported

**Cons:**
- Multiple JOINs impact performance
- Schema changes require migrations
- Harder to add new fields

#### NoSQL Approach (MongoDB)
```javascript
// Single document with embedded data
db.reviews_feedback.findOne({ "reviewId": "REV-2026-001234" })

// Simple aggregation
db.reviews_feedback.aggregate([
  { $match: { productId: 101 } },
  { $group: {
      _id: "$rating",
      count: { $sum: 1 },
      avgHelpful: { $avg: "$helpful_count" }
    }
  }
])
```

**Pros:**
- Single query (no JOINs)
- Flexible schema
- Fast reads
- Easy to add fields

**Cons:**
- Data duplication
- Manual consistency management
- No foreign key constraints

---

### Scenario 2: Activity Logging

#### Comparison Table

| Aspect | PostgreSQL | MongoDB | Winner |
|--------|-----------|---------|--------|
| **Write Speed** | ~5,000 inserts/sec | ~50,000 inserts/sec | MongoDB (10x) |
| **Storage** | Normalized, efficient | Denormalized, larger | PostgreSQL |
| **Query Complexity** | Complex SQL supported | Simple queries preferred | PostgreSQL |
| **Schema Evolution** | Requires migrations | Add fields anytime | MongoDB |
| **Horizontal Scaling** | Difficult | Built-in sharding | MongoDB |

**Verdict:** MongoDB is superior for high-volume activity logging.

---

## Integration Architecture

### Hybrid Database Architecture

```
┌─────────────────────────────────────────────────────┐
│              SMECS Application Layer                │
│                  (JavaFX + Java)                    │
└────────────┬──────────────────────┬─────────────────┘
             │                      │
             │                      │
    ┌────────▼────────┐    ┌───────▼──────────┐
    │   PostgreSQL    │    │     MongoDB      │
    │   (Relational)  │    │     (NoSQL)      │
    └─────────────────┘    └──────────────────┘
             │                      │
    ┌────────▼────────┐    ┌───────▼──────────┐
    │  Structured     │    │   Unstructured   │
    │  Transactional  │    │   Analytics      │
    │  Data:          │    │   Data:          │
    │  • Products     │    │  • Reviews       │
    │  • Orders       │    │  • Activity Logs │
    │  • Inventory    │    │  • Search Logs   │
    │  • Users        │    │  • View History  │
    └─────────────────┘    └──────────────────┘
```

### Data Flow

1. **Write Path:**
   - Transactional data → PostgreSQL
   - Analytics/logs → MongoDB
   - Some data written to both (e.g., review summary in PostgreSQL, full review in MongoDB)

2. **Read Path:**
   - Product listings → PostgreSQL
   - Product reviews → MongoDB
   - Analytics dashboards → MongoDB aggregations
   - Reports → Join data from both sources

### Integration Points

```java
// Service layer handles both databases
public class ReviewService {
    private ReviewDAO postgresDAO;      // For review summaries
    private ReviewFeedbackDAO mongoDAO; // For full reviews
    
    public void createReview(Review review) {
        // Save full review to MongoDB
        mongoDAO.insertReview(review);
        
        // Save summary to PostgreSQL for joins
        postgresDAO.insertReviewSummary(review);
    }
    
    public List<ReviewFeedback> getProductReviews(int productId) {
        // Fetch from MongoDB (no JOINs needed)
        return mongoDAO.findByProductId(productId);
    }
}
```

---

## Use Cases

### Use Case 1: Customer Review System

**Requirements:**
- Variable review structure (text, images, video links)
- Full-text search
- Sentiment analysis
- Fast retrieval without JOINs

**Solution:**
```javascript
// MongoDB Query: Get recent positive reviews
db.reviews_feedback.find({
  productId: 101,
  sentiment: "positive",
  rating: { $gte: 4 }
})
.sort({ created_at: -1 })
.limit(10)
```

**Performance:** ~5ms (vs ~50ms with SQL JOINs)

---

### Use Case 2: User Activity Analytics

**Requirements:**
- Track all user interactions
- High write volume (1000s of events/second)
- Time-based queries
- Flexible event schema

**Solution:**
```javascript
// MongoDB Aggregation: Popular products last 24h
db.activity_logs.aggregate([
  {
    $match: {
      eventType: "product_view",
      timestamp: { $gte: ISODate("2026-01-14T10:00:00Z") }
    }
  },
  {
    $group: {
      _id: "$details.productId",
      views: { $sum: 1 },
      unique_users: { $addToSet: "$userId" }
    }
  },
  {
    $sort: { views: -1 }
  },
  {
    $limit: 20
  }
])
```

---

### Use Case 3: Search Analytics

**Requirements:**
- Track search queries and results
- Identify zero-result searches
- Analyze search patterns
- Optimize search algorithm

**Solution:**
```javascript
// Find searches with no results
db.search_logs.find({
  "results.total_found": 0,
  timestamp: { $gte: ISODate("2026-01-14T00:00:00Z") }
})

// Most common search terms
db.search_logs.aggregate([
  {
    $group: {
      _id: "$query.normalized_term",
      count: { $sum: 1 },
      avg_results: { $avg: "$results.total_found" }
    }
  },
  { $sort: { count: -1 } },
  { $limit: 50 }
])
```

---

## Performance Considerations

### Indexing Strategy

**Critical Indexes:**
```javascript
// Compound index for common query patterns
db.reviews_feedback.createIndex(
  { "productId": 1, "rating": -1, "created_at": -1 }
)

// Text index for search
db.reviews_feedback.createIndex(
  { "reviewText": "text", "title": "text" },
  { weights: { title: 10, reviewText: 5 } }
)

// TTL index for auto-cleanup of old logs
db.activity_logs.createIndex(
  { "timestamp": 1 },
  { expireAfterSeconds: 7776000 } // 90 days
)
```

### Query Optimization

**Best Practices:**
1. Always use indexes for filter fields
2. Limit result set with `.limit()`
3. Use projections to return only needed fields
4. Leverage aggregation pipeline for complex queries
5. Use covered queries when possible

### Scalability

**Horizontal Scaling with Sharding:**
```javascript
// Shard by productId for reviews
sh.shardCollection("smecs_nosql.reviews_feedback", { "productId": "hashed" })

// Shard by timestamp for logs (range-based)
sh.shardCollection("smecs_nosql.activity_logs", { "timestamp": 1 })
```

---

## Migration Strategy

### Phase 1: Parallel Write
- Write to both PostgreSQL and MongoDB
- Compare results for consistency
- Duration: 2 weeks

### Phase 2: Read Migration
- Gradually shift reads to MongoDB
- Monitor performance metrics
- Duration: 2 weeks

### Phase 3: Full Migration
- MongoDB becomes primary for unstructured data
- PostgreSQL maintains summary/reference data
- Duration: 1 week

---

## Monitoring & Maintenance

### Key Metrics to Track
- Write throughput (ops/sec)
- Query response time (p50, p95, p99)
- Index hit rate
- Storage size growth
- Replication lag

### Tools
- MongoDB Atlas (cloud monitoring)
- MongoDB Compass (GUI)
- mongosh (CLI)
- Custom monitoring via Java driver

---

## Conclusion

### When to Use NoSQL (MongoDB)
✅ Unstructured or semi-structured data  
✅ High write volume  
✅ Flexible schema requirements  
✅ Horizontal scalability needed  
✅ Full-text search  
✅ Analytics and aggregations  

### When to Use Relational (PostgreSQL)
✅ Structured transactional data  
✅ Complex relationships (many JOINs)  
✅ ACID compliance critical  
✅ Vertical scaling sufficient  
✅ Standard SQL queries  
✅ Data integrity constraints  

### SMECS Hybrid Approach
The hybrid architecture leverages the strengths of both:
- **PostgreSQL** for core e-commerce transactions
- **MongoDB** for analytics, logs, and reviews

This provides optimal performance, scalability, and flexibility for the Smart E-Commerce System.

---

**Document Version:** 1.0  
**Last Updated:** January 15, 2026  
**Next Review:** February 15, 2026

