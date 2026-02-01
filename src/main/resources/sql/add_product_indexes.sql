-- Performance optimization indexes for product pagination and filtering
-- This script should be run after implementing pagination features

-- Index on product name for filtering and sorting
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- Index on product description for filtering
CREATE INDEX IF NOT EXISTS idx_products_description ON products(description);

-- Index on category_id for filtering by category (likely already exists as FK)
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);

-- Composite index for common query patterns (category + name)
CREATE INDEX IF NOT EXISTS idx_products_category_name ON products(category_id, name);

-- Index on price for sorting by price
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

-- Full-text search index for name and description (PostgreSQL specific)
-- Uncomment if using PostgreSQL and want full-text search
-- CREATE INDEX IF NOT EXISTS idx_products_name_description_fts
--   ON products USING GIN (to_tsvector('english', name || ' ' || description));

-- MySQL full-text index alternative
-- Uncomment if using MySQL
-- ALTER TABLE products ADD FULLTEXT INDEX idx_products_name_description_ft (name, description);
