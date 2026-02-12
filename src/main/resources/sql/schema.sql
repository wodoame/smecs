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
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    image_url VARCHAR(512)
);

-- 3. Products Table
-- Requirements: Linked to categories, pricing constraints
CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    category_id INTEGER REFERENCES Categories(category_id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
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
CREATE INDEX IF NOT EXISTS idx_products_name ON Products(name);
CREATE INDEX IF NOT EXISTS idx_products_category ON Products(category_id);
-- Composite index for searching by name within a category
CREATE INDEX IF NOT EXISTS idx_products_name_category ON Products(name, category_id);
CREATE INDEX IF NOT EXISTS idx_products_price ON Products(price);
CREATE INDEX IF NOT EXISTS idx_products_created_at ON Products(created_at DESC);

-- Categories Indexes
CREATE INDEX IF NOT EXISTS idx_categories_name ON Categories(name);

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
    v_cat_fishing_id INTEGER;
    v_cat_gaming_id INTEGER;
    v_cat_watches_id INTEGER;
    v_cat_food_id INTEGER;
    v_cat_travel_id INTEGER;
    v_cat_software_id INTEGER;
    v_cat_industrial_id INTEGER;

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

    v_prod_fishing_rod_id INTEGER;
    v_prod_tackle_box_id INTEGER;
    v_prod_mouse_id INTEGER;
    v_prod_keyboard_id INTEGER;
    v_prod_watch_analog_id INTEGER;
    v_prod_watch_digital_id INTEGER;
    v_prod_honey_id INTEGER;
    v_prod_chocolate_id INTEGER;
    v_prod_pillow_id INTEGER;
    v_prod_suitcase_id INTEGER;
    v_prod_antivirus_id INTEGER;
    v_prod_editor_id INTEGER;
    v_prod_microscope_id INTEGER;
    v_prod_labcoat_id INTEGER;

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
    INSERT INTO Categories (name, description, image_url)
    VALUES ('Electronics', 'Devices and gadgets', 'https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_electronics_id FROM Categories WHERE name = 'Electronics';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Books', 'Paperback and Hardcover books', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_books_id FROM Categories WHERE name = 'Books';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Clothing', 'Apparel for men and women', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_clothing_id FROM Categories WHERE name = 'Clothing';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Home & Kitchen', 'Appliances and decor', 'https://images.unsplash.com/photo-1501045661006-fcebe0257c3f?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_home_id FROM Categories WHERE name = 'Home & Kitchen';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Sports & Outdoors', 'Exercise equipment and gear', 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_sports_id FROM Categories WHERE name = 'Sports & Outdoors';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Fishing Gear', 'Fishing gear for both beginners and veterans', 'https://images.unsplash.com/photo-1551131618-3f0a5cf594b4?q=80&w=1171&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_fishing_id FROM Categories WHERE name = 'Fishing Gear';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Gaming', 'Gaming accessories', 'https://m.media-amazon.com/images/I/71RE88AnbGL._AC_SX466_.jpg')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_gaming_id FROM Categories WHERE name = 'Gaming';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Watches', 'Watches', 'https://m.media-amazon.com/images/I/61hBxkEW5ZL._AC_UL320_.jpg')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_watches_id FROM Categories WHERE name = 'Watches';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Food', 'Food items', 'https://m.media-amazon.com/images/I/91OTxrgN1NL._AC_UY218_.jpg')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_food_id FROM Categories WHERE name = 'Food';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Travel', 'Travel', 'https://m.media-amazon.com/images/I/611+Lz6hKvL._AC_UL320_.jpg')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_travel_id FROM Categories WHERE name = 'Travel';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Software', 'Applications for everyday use', 'https://m.media-amazon.com/images/I/61hSSVPvT3L._AC_UY218_.jpg')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_software_id FROM Categories WHERE name = 'Software';

    INSERT INTO Categories (name, description, image_url)
    VALUES ('Industrial & Scientific', 'Industrial & Scientific', 'https://m.media-amazon.com/images/I/71CiFveiCnL._AC_UL320_.jpg')
    ON CONFLICT (name) DO NOTHING;
    SELECT category_id INTO v_cat_industrial_id FROM Categories WHERE name = 'Industrial & Scientific';

    -- 3. Products
    -- Electronics
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('UltraBook Pro', v_cat_electronics_id, 'High-end laptop with 16GB RAM', 1299.99, 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_laptop_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('SmartPhone X', v_cat_electronics_id, 'Latest 5G smartphone', 899.50, 'https://images.unsplash.com/photo-1603184017968-953f59cd2e37?q=80&w=1471&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_phone_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Wireless Earbuds', v_cat_electronics_id, 'Noise cancelling earbuds', 149.99, 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?q=80&w=1074&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_earbuds_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('4K Gaming Monitor', v_cat_electronics_id, '27-inch 144Hz display', 399.99, 'https://images.unsplash.com/photo-1593640408182-31c70c8268f5?q=80&w=1442&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_monitor_id;

    -- Clothing
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Cotton T-Shirt', v_cat_clothing_id, 'Comfortable 100% cotton', 19.99, 'https://images.unsplash.com/photo-1759572095317-3a96f9a98e2b?q=80&w=1471&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_tshirt_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Denim Jacket', v_cat_clothing_id, 'Classic blue denim jacket', 79.99, 'https://images.unsplash.com/photo-1617178388553-a9d022974a5c?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_jacket_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Slim Fit Jeans', v_cat_clothing_id, 'Dark wash denim jeans', 59.99, 'https://images.unsplash.com/photo-1714143164139-8fdc14bf3054?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_jeans_id;

    -- Books
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Sci-Fi Novel', v_cat_books_id, 'Bestselling galactic adventure', 14.99, 'https://images.unsplash.com/photo-1629420251935-41e9c67a8874?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8bm92ZWx8ZW58MHx8MHx8fDA%3D');

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Gourmet Cookbook', v_cat_books_id, '100 recipes for home cooks', 29.99, 'https://images.unsplash.com/photo-1620482060657-38b4bb0ab9c8?q=80&w=1084&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_cookbook_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Water Bottle', v_cat_books_id, 'Quality water-tight bottles', 24.99, 'https://images.unsplash.com/photo-1625708458528-802ec79b1ed8?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_history_id;

    -- Home & Kitchen
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Espresso Maker', v_cat_home_id, 'Automatic coffee machine', 199.99, 'https://images.unsplash.com/photo-1616035596458-2338800a1ff2?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_coffee_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('High-Speed Blender', v_cat_home_id, 'Perfect for smoothies', 89.99, 'https://plus.unsplash.com/premium_photo-1718043036199-d98bef36af46?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_blender_id;

    -- Sports
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Yoga Mat', v_cat_sports_id, 'Non-slip exercise mat', 25.00, 'https://images.unsplash.com/photo-1591291621164-2c6367723315?q=80&w=1471&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_yoga_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Dumbbell Set', v_cat_sports_id, 'Adjustable weights 5-25lbs', 59.99, 'https://images.unsplash.com/photo-1662386392891-688364c5a5d7?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_dumbbell_id;

    -- Fishing Gear
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Graphite Fishing Rod', v_cat_fishing_id, 'Lightweight rod for fast action', 89.99, 'https://images.unsplash.com/photo-1532985686121-69cd16c17471?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_fishing_rod_id;

    -- Gaming
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('RGB Gaming Mouse', v_cat_gaming_id, 'High precision sensor 16000 DPI', 49.99, 'https://images.unsplash.com/photo-1632160871990-be30194885aa?q=80&w=765&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_mouse_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Mechanical Keyboard', v_cat_gaming_id, 'Tactile switches with backlighting', 109.99, 'https://images.unsplash.com/photo-1614920847152-93b7e90b4bd7?q=80&w=1174&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_keyboard_id;

    -- Watches
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Classic Leather Watch', v_cat_watches_id, 'Timeless design with leather strap', 129.00, 'https://images.unsplash.com/photo-1599143844678-6f232bb16ae6?q=80&w=1175&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_watch_analog_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Sport Smartwatch', v_cat_watches_id, 'Fitness tracking and notifications', 199.99, 'https://images.unsplash.com/photo-1758348844371-dfbae2780bd3?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_watch_digital_id;

    -- Food
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Organic Raw Honey', v_cat_food_id, 'Pure wildflower honey 500g', 15.99, 'https://images.unsplash.com/photo-1654515722385-c684c5331c04?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_honey_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Artisan Dark Chocolate', v_cat_food_id, '70% cocoa with sea salt', 8.50, 'https://images.unsplash.com/photo-1511381939415-e44015466834?q=80&w=1000')
    RETURNING product_id INTO v_prod_chocolate_id;

    -- Travel
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Memory Foam Neck Pillow', v_cat_travel_id, 'Ergonomic support for flights', 22.99, 'https://images.unsplash.com/photo-1629949009765-40fc74c9ec21?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_pillow_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Carry-On Suitcase', v_cat_travel_id, 'Hardshell spinner wheels', 119.99, 'https://images.unsplash.com/photo-1666238855001-d11112be431f?q=80&w=627&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_suitcase_id;

    -- Software
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Security Suite 365', v_cat_software_id, '1-year antivirus protection', 39.99, 'https://imgs.search.brave.com/3-d_avcWFr0JC0xb9V-12fYWag3IguKcAqFNvFUoBSY/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pbWcu/ZnJlZXBpay5jb20v/cHJlbWl1bS12ZWN0/b3IvYW50aXZpcnVz/LXZlY3Rvci1pbGx1/c3RyYXRpb25fNzcy/MzUtNDEyLmpwZz9z/ZW10PWFpc19oeWJy/aWQmdz03NDA')
    RETURNING product_id INTO v_prod_antivirus_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Photo Editor Pro', v_cat_software_id, 'Lifetime license for editing software', 79.99, 'https://imgs.search.brave.com/XYWUNGcnwgs-UsZcTRIo6lnEHFUs4DXEuWKYqCRA8RU/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pNS53/YWxtYXJ0aW1hZ2Vz/LmNvbS9zZW8vQWRv/YmUtQ3JlYXRpdmUt/Q2xvdWQtZm9yLVdp/bmRvd3MtTWFjLTEt/WWVhci1TdHVkZW50/LVRlYWNoZXItU3Vi/c2NyaXB0aW9uLURp/Z2l0YWwtRG93bmxv/YWRfMGRmYTE2NmMt/ZGE1Yi00YmNhLWE0/ZWEtN2UwNDkyMGYy/NzI5LjIwYjg5MjI0/YTBiZmViNjdlYjQ0/NThlMjU2MGFiOWEz/LnBuZz9vZG5IZWln/aHQ9NTc2Jm9kbldp/ZHRoPTU3NiZvZG5C/Zz1GRkZGRkY')
    RETURNING product_id INTO v_prod_editor_id;

    -- Industrial & Scientific
    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Digital Microscope', v_cat_industrial_id, '1000x magnification with USB', 149.50, 'https://images.unsplash.com/photo-1614081989290-bcdba07cd9d3?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_microscope_id;

    INSERT INTO Products (name, category_id, description, price, image_url)
    VALUES ('Safety Lab Coat', v_cat_industrial_id, 'Chemical resistant material', 29.99, 'https://plus.unsplash.com/premium_photo-1673953510107-d5aee40d80a7?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')
    RETURNING product_id INTO v_prod_labcoat_id;

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

    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_fishing_rod_id, 15);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_tackle_box_id, 30);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_mouse_id, 80);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_keyboard_id, 45);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_watch_analog_id, 10);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_watch_digital_id, 25);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_honey_id, 100);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_chocolate_id, 200);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_pillow_id, 60);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_suitcase_id, 12);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_antivirus_id, 999);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_editor_id, 999);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_microscope_id, 5);
    INSERT INTO Inventory (product_id, quantity) VALUES (v_prod_labcoat_id, 50);

    RAISE NOTICE 'Database seeding completed successfully.';
END;
$$;
