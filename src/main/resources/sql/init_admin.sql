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
CALL seed_database();s created
DO $$
DECLARE
    admin_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO admin_count FROM Users WHERE username = 'admin' AND role = 'admin';

    IF admin_count > 0 THEN
        RAISE NOTICE '✓ Admin user initialized successfully';
        RAISE NOTICE '  Username: admin';
        RAISE NOTICE '  Email: admin@smecs.com';
        RAISE NOTICE '  Password: admin123';
        RAISE NOTICE '';
        RAISE NOTICE '⚠ SECURITY WARNING: Please change the admin password after first login!';
    ELSE
        RAISE EXCEPTION 'Failed to initialize admin user';
    END IF;
END $$;

