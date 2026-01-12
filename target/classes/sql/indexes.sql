-- =====================================================
-- EPIC 3: Database Indexing for Search Optimization
-- Smart E-Commerce System (SMECS)
-- =====================================================

-- ===================
-- PRODUCT INDEXES
-- ===================

-- Index for product name searches (case-insensitive via LOWER in queries)
-- Improves performance of: WHERE LOWER(product_name) LIKE '%query%'
CREATE INDEX IF NOT EXISTS idx_products_name
ON Products(product_name);

-- Index for category-based product searches
-- Improves performance of: WHERE category_id = ?
CREATE INDEX IF NOT EXISTS idx_products_category_id
ON Products(category_id);

-- Composite index for product name and category searches
-- Improves performance when searching both simultaneously
CREATE INDEX IF NOT EXISTS idx_products_name_category
ON Products(product_name, category_id);

-- Index for price-based sorting and filtering
-- Improves performance of: ORDER BY price, WHERE price BETWEEN x AND y
CREATE INDEX IF NOT EXISTS idx_products_price
ON Products(price);

-- Index for date-based sorting (newest/oldest products)
-- Improves performance of: ORDER BY created_at DESC/ASC
CREATE INDEX IF NOT EXISTS idx_products_created_at
ON Products(created_at DESC);

-- ===================
-- CATEGORY INDEXES
-- ===================

-- Index for category name searches
CREATE INDEX IF NOT EXISTS idx_categories_name
ON Categories(category_name);

-- ===================
-- USER INDEXES (if user search is implemented)
-- ===================

-- Index for user email lookups (used in login)
CREATE INDEX IF NOT EXISTS idx_users_email
ON Users(email);

-- Index for username lookups
CREATE INDEX IF NOT EXISTS idx_users_username
ON Users(username);

-- ===================
-- FULL-TEXT SEARCH INDEXES (PostgreSQL)
-- Uncomment if using PostgreSQL for advanced text search
-- ===================

-- -- Full-text index on product name and description
-- CREATE INDEX IF NOT EXISTS idx_products_fulltext
-- ON Products USING gin(to_tsvector('english', product_name || ' ' || COALESCE(description, '')));

-- ===================
-- FULL-TEXT SEARCH INDEXES (MySQL)
-- Uncomment if using MySQL 5.6+ with InnoDB
-- ===================

-- ALTER TABLE Products ADD FULLTEXT INDEX idx_products_fulltext (product_name, description);
-- ALTER TABLE Categories ADD FULLTEXT INDEX idx_categories_fulltext (category_name, description);

-- ===================
-- VERIFY INDEXES
-- ===================

-- PostgreSQL: View all indexes
-- SELECT indexname, indexdef FROM pg_indexes WHERE tablename = 'products';

-- MySQL: View all indexes
-- SHOW INDEX FROM Products;
-- SHOW INDEX FROM Categories;

-- ===================
-- ANALYZE TABLES (update statistics for query optimizer)
-- ===================

-- PostgreSQL:
-- ANALYZE Products;
-- ANALYZE Categories;

-- MySQL:
-- ANALYZE TABLE Products;
-- ANALYZE TABLE Categories;

