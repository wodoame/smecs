# Project Architecture Overview

This is a **JavaFX CRUD application** following a **layered architecture pattern**. This document explains how all components fit together.

## Layer Structure

```
┌─────────────────────────────────┐
│   Presentation Layer (FXML UI)  │
└─────────────────┬───────────────┘
                  ↓
┌─────────────────────────────────┐
│     Controller Layer            │
│  (AdminController, UserController)
└─────────────────┬───────────────┘
                  ↓
┌─────────────────────────────────┐
│      Service Layer              │
│  (ProductService, CategoryService)
└─────────────────┬───────────────┘
                  ↓
┌─────────────────────────────────┐
│       DAO Layer                 │
│  (ProductDAO, CategoryDAO)      │
└─────────────────┬───────────────┘
                  ↓
┌─────────────────────────────────┐
│         Database                │
│    (DatabaseConnection)         │
└─────────────────────────────────┘
```

## Component Breakdown

### **1. Models** (`com.smecs.model`)
**Purpose:** Data representation objects

- Plain Java objects (POJOs) representing database entities
- Examples: `Product`, `Category`, `User`
- Contain fields matching database columns (e.g., `productId`, `productName`, `price`)
- Used to transfer data between all layers
- No business logic, just getters/setters

**Example:**
```java
public class Product {
    private int productId;
    private String productName;
    private double price;
    private String description;
    private String categoryName;
    // getters and setters
}
```

### **2. Controllers** (`com.smecs.controller`)
**Purpose:** Handle UI interactions and user events

- **JavaFX controllers** like `AdminController`, `UserController`
- Handle UI events (button clicks, table selections, form submissions)
- Bind UI components to data using `@FXML` annotations
- Call service methods to perform business operations
- Update the UI with data from services
- No direct database access
- No business logic

**Key Responsibilities:**
- Input validation (UI-level)
- Event handling
- Updating ObservableList for tables
- Navigation between views

**Example:**
```java
@FXML
private void handleAdd() {
    if (isInputValid()) {
        Product product = new Product();
        product.setProductName(nameField.getText());
        productService.createProduct(product);
        loadData();
    }
}
```

### **3. Services** (`com.smecs.service`)
**Purpose:** Business logic and transaction management

- **Business logic layer**: `ProductService`, `CategoryService`
- Provide high-level methods like `getAllProducts()`, `createProduct()`, `updateProduct()`, `deleteProduct()`
- Act as intermediary between controllers and DAOs
- Handle business rules and validation
- Manage transactions
- Can call multiple DAOs if needed

**Key Responsibilities:**
- Business rule enforcement
- Data transformation
- Coordinating multiple DAO operations
- Transaction boundaries

**Example:**
```java
public class ProductService {
    private ProductDAO productDAO = new ProductDAO();
    
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
    
    public void createProduct(Product product) {
        // Business logic here
        productDAO.createProduct(product);
    }
}
```

### **4. DAOs (Data Access Objects)** (`com.smecs.dao`)
**Purpose:** Database operations

- Handle direct database operations (SQL queries)
- Perform CRUD operations: Create, Read, Update, Delete
- Return model objects populated with database data
- Abstract database complexity from upper layers
- Use `DatabaseConnection` for connection management

**Key Responsibilities:**
- Execute SQL statements
- Map ResultSet to Model objects
- Handle SQLExceptions
- Close resources properly

**Example:**
```java
public class ProductDAO {
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        // Execute query and populate products list
        return products;
    }
}
```

### **5. Database Connection** (`com.smecs.util`)
**Purpose:** Database connectivity management

- `DatabaseConnection` utility class
- Provides database connection instances
- Manages connection pooling (if implemented)
- Centralizes database configuration

## Data Flow Examples

### **Example 1: Adding a Product (Admin)**
1. User fills form and clicks "Add" button
2. `AdminController.handleAdd()` is triggered
3. Controller validates input using `isInputValid()`
4. Controller creates a `Product` model object with form data
5. Controller calls `productService.createProduct(product)`
6. Service applies business rules (if any)
7. Service calls `productDAO.createProduct(product)`
8. DAO executes SQL INSERT statement
9. DAO saves to database via `DatabaseConnection`
10. Controller refreshes table by calling `loadData()`
11. UI updates to show new product

### **Example 2: Searching Products (User)**
1. User types in search field and clicks search
2. `UserController.handleSearch()` is triggered
3. Controller gets search query from `searchField.getText()`
4. Controller calls `productService.searchProducts(query)`
5. Service calls `productDAO.searchProducts(query)`
6. DAO executes SQL SELECT with WHERE clause
7. DAO returns List<Product> with matching results
8. Service returns the list to controller
9. Controller updates `productList` ObservableList
10. TableView automatically updates to show filtered results

### **Example 3: Loading All Products**
1. View initializes, `initialize()` method called
2. Controller sets up table columns with `PropertyValueFactory`
3. Controller calls `loadProducts()` private method
4. `loadProducts()` calls `productService.getAllProducts()`
5. Service calls `productDAO.getAllProducts()`
6. DAO executes SQL SELECT * query
7. DAO maps each ResultSet row to a Product object
8. List<Product> returned up through service to controller
9. Controller updates ObservableList: `productList.setAll(...)`
10. TableView displays all products

## Design Patterns Used

### **1. Layered Architecture**
- Clear separation of concerns
- Each layer has specific responsibility
- Dependencies flow downward only

### **2. Data Access Object (DAO) Pattern**
- Separates data persistence logic from business logic
- Provides abstract interface to database
- Easy to mock for testing

### **3. Service Layer Pattern**
- Encapsulates business logic
- Provides transaction boundaries
- Coordinates multiple DAOs

### **4. Model-View-Controller (MVC)**
- FXML files = View
- Controller classes = Controller
- Model classes = Model
- Separates presentation from business logic

## Benefits of This Architecture

### **Maintainability**
- Changes in one layer don't affect others
- Easy to locate and fix bugs
- Clear file organization

### **Testability**
- Each layer can be tested independently
- Easy to mock dependencies
- Business logic isolated in services

### **Scalability**
- Easy to add new features
- Can replace database without changing controllers
- Can change UI without touching business logic

### **Reusability**
- Services can be used by multiple controllers
- DAOs can be used by multiple services
- Models used throughout the application

## File Structure

```
src/main/java/com/smecs/
├── MainApp.java                    # Application entry point
├── controller/
│   ├── AdminController.java        # Admin CRUD operations
│   └── UserController.java         # User view/search operations
├── service/
│   ├── ProductService.java         # Product business logic
│   └── CategoryService.java        # Category business logic
├── dao/
│   ├── ProductDAO.java             # Product data access
│   └── CategoryDAO.java            # Category data access
├── model/
│   ├── Product.java                # Product entity
│   ├── Category.java               # Category entity
│   └── User.java                   # User entity
├── util/
│   └── DatabaseConnection.java     # Database connection utility
└── test/
    └── VerificationTest.java       # Testing utilities

src/main/resources/view/
├── main_layout.fxml                # Main application layout
├── admin_view.fxml                 # Admin interface
└── user_view.fxml                  # User interface
```

## Best Practices Implemented

1. **Single Responsibility Principle**: Each class has one job
2. **Dependency Injection**: Services injected into controllers
3. **ObservableList**: For reactive UI updates
4. **PropertyValueFactory**: For table column binding
5. **FXML Separation**: UI defined separately from logic
6. **Resource Management**: Proper closing of database connections

## Future Enhancement Possibilities

- Add dependency injection framework (Spring)
- Implement connection pooling
- Add logging framework (SLF4J/Log4j)
- Implement DTOs for data transfer
- Add validation framework
- Implement caching layer
- Add exception handling middleware
- Implement user authentication/authorization
- Add unit and integration tests

---

**Last Updated:** January 11, 2026

