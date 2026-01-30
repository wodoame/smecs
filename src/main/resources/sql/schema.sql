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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_url VARCHAR(512)
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

-- =====================================================
-- EPIC 2: Seed Data (Optional)
-- Statistics / Reporting Requirement: Need data to report on.
-- Usage: CALL seed_database();
-- =====================================================

CREATE OR REPLACE PROCEDURE seed_database()
LANGUAGE plpgsql
AS $$
DECLARE
    v_cat_electronics_id INTEGER;
    v_cat_books_id INTEGER;
    v_cat_clothing_id INTEGER;
    v_cat_home_id INTEGER;
    v_cat_sports_id INTEGER;

    v_prod_laptop_id INTEGER;
    v_prod_phone_id INTEGER;
    v_prod_tshirt_id INTEGER;
    v_prod_coffee_id INTEGER;
    v_prod_blender_id INTEGER;
    v_prod_yoga_id INTEGER;
    v_prod_dumbbell_id INTEGER;
    v_prod_earbuds_id INTEGER;
    v_prod_monitor_id INTEGER;
    v_prod_jacket_id INTEGER;
    v_prod_jeans_id INTEGER;
    v_prod_cookbook_id INTEGER;
    v_prod_history_id INTEGER;

    v_user_admin_id INTEGER;
    v_user_john_id INTEGER;
    v_user_jane_id INTEGER;
    v_order_id INTEGER;
    v_order2_id INTEGER;
    v_order3_id INTEGER;
    v_cart_id INTEGER;
BEGIN
    -- 1. Users // No users
    -- 2. Categories
    INSERT INTO Categories (category_name, description)
    VALUES ('Electronics', 'Devices and gadgets')
    RETURNING category_id INTO v_cat_electronics_id;

    INSERT INTO Categories (category_name, description)
    VALUES ('Books', 'Paperback and Hardcover books')
    RETURNING category_id INTO v_cat_books_id;

    INSERT INTO Categories (category_name, description)
    VALUES ('Clothing', 'Apparel for men and women')
    RETURNING category_id INTO v_cat_clothing_id;

    INSERT INTO Categories (category_name, description)
    VALUES ('Home & Kitchen', 'Appliances and decor')
    RETURNING category_id INTO v_cat_home_id;

    INSERT INTO Categories (category_name, description)
    VALUES ('Sports & Outdoors', 'Exercise equipment and gear')
    RETURNING category_id INTO v_cat_sports_id;

    -- 3. Products
    -- Electronics
    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('UltraBook Pro', v_cat_electronics_id, 'High-end laptop with 16GB RAM', 1299.99, 'https://example.com/images/ultrabook_pro.jpg')
    RETURNING product_id INTO v_prod_laptop_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('SmartPhone X', v_cat_electronics_id, 'Latest 5G smartphone', 899.50, 'https://example.com/images/smartphone_x.jpg')
    RETURNING product_id INTO v_prod_phone_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Wireless Earbuds', v_cat_electronics_id, 'Noise cancelling earbuds', 149.99, 'https://example.com/images/wireless_earbuds.jpg')
    RETURNING product_id INTO v_prod_earbuds_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('4K Gaming Monitor', v_cat_electronics_id, '27-inch 144Hz display', 399.99, 'https://example.com/images/4k_gaming_monitor.jpg')
    RETURNING product_id INTO v_prod_monitor_id;

    -- Clothing
    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Cotton T-Shirt', v_cat_clothing_id, 'Comfortable 100% cotton', 19.99, 'https://example.com/images/cotton_tshirt.jpg')
    RETURNING product_id INTO v_prod_tshirt_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Denim Jacket', v_cat_clothing_id, 'Classic blue denim jacket', 79.99, 'https://example.com/images/denim_jacket.jpg')
    RETURNING product_id INTO v_prod_jacket_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Slim Fit Jeans', v_cat_clothing_id, 'Dark wash denim jeans', 59.99, 'https://example.com/images/slim_fit_jeans.jpg')
    RETURNING product_id INTO v_prod_jeans_id;

    -- Books
    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Sci-Fi Novel', v_cat_books_id, 'Bestselling galactic adventure', 14.99, 'https://example.com/images/scifi_novel.jpg');

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Gourmet Cookbook', v_cat_books_id, '100 recipes for home cooks', 29.99, 'https://example.com/images/gourmet_cookbook.jpg')
    RETURNING product_id INTO v_prod_cookbook_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('World History', v_cat_books_id, 'Comprehensive history guide', 24.99, 'https://example.com/images/world_history.jpg')
    RETURNING product_id INTO v_prod_history_id;

    -- Home & Kitchen
    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Espresso Maker', v_cat_home_id, 'Automatic coffee machine', 199.99, 'https://example.com/images/espresso_maker.jpg')
    RETURNING product_id INTO v_prod_coffee_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('High-Speed Blender', v_cat_home_id, 'Perfect for smoothies', 89.99, 'https://example.com/images/high_speed_blender.jpg')
    RETURNING product_id INTO v_prod_blender_id;

    -- Sports
    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Yoga Mat', v_cat_sports_id, 'Non-slip exercise mat', 25.00, 'https://example.com/images/yoga_mat.jpg')
    RETURNING product_id INTO v_prod_yoga_id;

    INSERT INTO Products (product_name, category_id, description, price, image_url)
    VALUES ('Dumbbell Set', v_cat_sports_id, 'Adjustable weights 5-25lbs', 59.99, 'https://example.com/images/dumbbell_set.jpg')
    RETURNING product_id INTO v_prod_dumbbell_id;

    -- 4. Inventory
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_laptop_id, 50);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_phone_id, 100);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_earbuds_id, 200);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_monitor_id, 30);

    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_tshirt_id, 200);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_jacket_id, 75);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_jeans_id, 120);

    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_cookbook_id, 40);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_history_id, 60);

    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_coffee_id, 25);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_blender_id, 45);

    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_yoga_id, 150);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_dumbbell_id, 20);

    -- 5. Orders (Historical Data)
    -- John's Order 1 (Delivered)
    INSERT INTO Orders (user_id, total_amount, status, created_at)
    VALUES (v_user_john_id, 1319.98, 'DELIVERED', CURRENT_TIMESTAMP - INTERVAL '30 days')
    RETURNING order_id INTO v_order_id;

    INSERT INTO OrderItems (order_id, product_id, quantity, price)
    VALUES (v_order_id, v_prod_laptop_id, 1, 1299.99);

    INSERT INTO OrderItems (order_id, product_id, quantity, price)
    VALUES (v_order_id, v_prod_tshirt_id, 1, 19.99);

    -- Jane's Order 1 (Shipped)
    INSERT INTO Orders (user_id, total_amount, status, created_at)
    VALUES (v_user_jane_id, 289.98, 'SHIPPED', CURRENT_TIMESTAMP - INTERVAL '5 days')
    RETURNING order_id INTO v_order2_id;

    INSERT INTO OrderItems (order_id, product_id, quantity, price)
    VALUES (v_order2_id, v_prod_coffee_id, 1, 199.99);

    INSERT INTO OrderItems (order_id, product_id, quantity, price)
    VALUES (v_order2_id, v_prod_blender_id, 1, 89.99);

    -- John's Order 2 (Recent/Pending)
    INSERT INTO Orders (user_id, total_amount, status, created_at)
    VALUES (v_user_john_id, 424.99, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '1 days')
    RETURNING order_id INTO v_order3_id;

    INSERT INTO OrderItems (order_id, product_id, quantity, price)
    VALUES (v_order3_id, v_prod_monitor_id, 1, 399.99);

    INSERT INTO OrderItems (order_id, product_id, quantity, price)
    VALUES (v_order3_id, v_prod_yoga_id, 1, 25.00);

    -- 6. Reviews
    INSERT INTO Reviews (user_id, product_id, rating, comment)
    VALUES (v_user_john_id, v_prod_laptop_id, 5, 'Excellent performance! Worth every penny.');

    INSERT INTO Reviews (user_id, product_id, rating, comment)
    VALUES (v_user_jane_id, v_prod_coffee_id, 4, 'Makes great espresso, but a bit loud.');

    INSERT INTO Reviews (user_id, product_id, rating, comment)
    VALUES (v_user_john_id, v_prod_tshirt_id, 3, 'Decent quality, shrank a bit after wash.');

    -- 7. Carts (Active Cart for John)
    INSERT INTO Carts (user_id)
    VALUES (v_user_john_id)
    RETURNING cart_id INTO v_cart_id;

    INSERT INTO CartItems (cart_id, product_id, quantity, price_at_addition)
    VALUES (v_cart_id, v_prod_phone_id, 1, 899.50);

    INSERT INTO CartItems (cart_id, product_id, quantity, price_at_addition)
    VALUES (v_cart_id, v_prod_earbuds_id, 2, 149.99);

    -- Active Cart for Jane
    INSERT INTO Carts (user_id)
    VALUES (v_user_jane_id)
    RETURNING cart_id INTO v_cart_id; -- reusing variable

    INSERT INTO CartItems (cart_id, product_id, quantity, price_at_addition)
    VALUES (v_cart_id, v_prod_cookbook_id, 1, 29.99);

    RAISE NOTICE 'Database seeding completed successfully.';
END;
$$;
