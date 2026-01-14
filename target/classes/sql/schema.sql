-- =====================================================
-- EPIC 1: Database Design and Modeling
-- Smart E-Commerce System (SMECS)
-- Schema Re-creation Script (PostgreSQL)
-- =====================================================

-- Drop tables in dependency order to allow clean recreation
DROP TABLE IF EXISTS CartItems CASCADE;
DROP TABLE IF EXISTS Carts CASCADE;
DROP TABLE IF EXISTS Reviews CASCADE;
DROP TABLE IF EXISTS OrderItems CASCADE;
DROP TABLE IF EXISTS Orders CASCADE;
DROP TABLE IF EXISTS Inventory CASCADE;
DROP TABLE IF EXISTS Products CASCADE;
DROP TABLE IF EXISTS Categories CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

-- 1. Users Table
-- Requirements: Managed by Admin, unique emails, roles
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(20) DEFAULT 'customer' CHECK (role IN ('admin', 'customer')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Categories Table
-- Requirements: Taxonomy for products
CREATE TABLE Categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- 3. Products Table
-- Requirements: Linked to categories, pricing constraints
CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    category_id INTEGER REFERENCES Categories(category_id) ON DELETE SET NULL,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Inventory Table
-- Requirements: Track stock levels (Technical Req: Inventory Entity)
CREATE TABLE Inventory (
    inventory_id SERIAL PRIMARY KEY,
    product_id INTEGER UNIQUE REFERENCES Products(product_id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Orders Table
-- Requirements: Track customer purchases (Technical Req: Orders Entity)
CREATE TABLE Orders (
    order_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES Users(user_id) ON DELETE SET NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 CHECK (total_amount >= 0),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. OrderItems Table
-- Requirements: Line items for orders (Technical Req: OrderItems Entity)
CREATE TABLE OrderItems (
    order_item_id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES Orders(order_id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES Products(product_id) ON DELETE SET NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0)
);

-- 7. Reviews Table
-- Requirements: Customer feedback (Technical Req: Reviews Entity)
CREATE TABLE Reviews (
    review_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES Users(user_id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES Products(product_id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Carts Table
-- Requirements: Shopping cart for users
CREATE TABLE Carts (
    cart_id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE REFERENCES Users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. CartItems Table
-- Requirements: Items in shopping carts
CREATE TABLE CartItems (
    cart_item_id SERIAL PRIMARY KEY,
    cart_id INTEGER REFERENCES Carts(cart_id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES Products(product_id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price_at_addition DECIMAL(10, 2) NOT NULL CHECK (price_at_addition >= 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, product_id)
);

-- =====================================================
-- EPIC 3: Indexes for Performance
-- =====================================================

-- Users Indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON Users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON Users(email);

-- Products Indexes
CREATE INDEX IF NOT EXISTS idx_products_name ON Products(product_name);
CREATE INDEX IF NOT EXISTS idx_products_category ON Products(category_id);
-- Composite index for searching by name within a category
CREATE INDEX IF NOT EXISTS idx_products_name_category ON Products(product_name, category_id);
CREATE INDEX IF NOT EXISTS idx_products_price ON Products(price);
CREATE INDEX IF NOT EXISTS idx_products_created_at ON Products(created_at DESC);

-- Categories Indexes
CREATE INDEX IF NOT EXISTS idx_categories_name ON Categories(category_name);

-- Orders Indexes
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON Orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON Orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON Orders(created_at DESC);

-- OrderItems Indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON OrderItems(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON OrderItems(product_id);

-- Reviews Indexes
CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON Reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON Reviews(user_id);

-- Inventory Indexes
-- product_id is already UNIQUE, which creates an index implicitly.

-- Carts Indexes
CREATE INDEX IF NOT EXISTS idx_carts_user_id ON Carts(user_id);

-- CartItems Indexes
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON CartItems(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON CartItems(product_id);

