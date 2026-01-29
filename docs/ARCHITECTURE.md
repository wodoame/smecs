# Project Architecture Overview (Spring Boot)

This document describes the architecture of the **smecs** backend, now implemented as a modern Spring Boot application. It outlines the layered structure, key components, data flow, best practices, and future enhancements.

---

## Layered Architecture

```
┌───────────────────────────────┐
│      Controller Layer         │
│   (@RestController classes)   │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│        Service Layer          │
│      (@Service classes)       │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│      Repository Layer         │
│    (@Repository interfaces)   │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│         Database              │
└───────────────────────────────┘
```

### Additional Components
- **DTOs (Data Transfer Objects):** Used for API requests/responses, including generic wrappers like `ResponseDTO<T>`.
- **Entities (Models):** Annotated with `@Entity`, represent database tables.
- **Configuration:** Classes annotated with `@Configuration` for app setup.

---

## Component Breakdown

### 1. Entities (`com.smecs.model`)
- Annotated with `@Entity`
- Represent database tables
- Example:
```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    // getters and setters
}
```

### 2. DTOs (`com.smecs.dto`)
- Used for transferring data between layers, especially for API requests/responses
- No business logic, only fields and accessors
- Example:
```java
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    // getters and setters
}
```
- **Generic Response Wrapper:**
```java
public class ResponseDTO<T> {
    private String status;
    private String message;
    private T data;
    // constructor, getters, setters
}
```

### 3. Repositories (`com.smecs.repository`)
- Interfaces extending `JpaRepository` or `CrudRepository`
- Handle database operations
- Example:
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {}
```

### 4. Services (`com.smecs.service`)
- Annotated with `@Service`
- Contain business logic and transaction management
- Example:
```java
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    // other business methods
}
```

### 5. Controllers (`com.smecs.controller`)
- Annotated with `@RestController`
- Handle HTTP requests and responses
- Use DTOs for input/output
- Example:
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseDTO<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProductDTOs();
        return new ResponseDTO<>("success", "Products fetched", products);
    }
    // other endpoints
}
```

---

## File Structure Example

```
src/main/java/com/smecs/
├── SmecsApplication.java           # Spring Boot entry point
├── controller/
│   └── ProductController.java
├── service/
│   └── ProductService.java
├── repository/
│   └── ProductRepository.java
├── dto/
│   ├── ProductDTO.java
│   └── ResponseDTO.java
├── model/
│   └── Product.java
└── config/
    └── WebConfig.java
```

---

## Data Flow Example: Creating a Product
1. **Client** sends POST `/api/products` with JSON body (ProductDTO)
2. **Controller** receives request, deserializes JSON to ProductDTO
3. **Service** validates and processes ProductDTO, maps to Product entity
4. **Repository** saves Product entity to database
5. **Service** maps saved Product to ProductDTO
6. **Controller** wraps ProductDTO in ResponseDTO and returns as JSON

---

## Best Practices & Design Patterns
- **Layered Architecture:** Clear separation of concerns
- **DTO Pattern:** Decouples API from internal models
- **Repository Pattern:** Abstracts data access
- **Service Layer Pattern:** Encapsulates business logic
- **Exception Handling:** Use `@ControllerAdvice` for global error handling
- **Validation:** Use `@Valid` and validation annotations on DTOs
- **Dependency Injection:** Use `@Autowired` or constructor injection
- **Configuration Management:** Use `application.properties` or `application.yml`

---

## Future Enhancements
- Add security with Spring Security (JWT, OAuth2)
- Implement pagination and filtering
- Add OpenAPI/Swagger documentation
- Integrate caching (e.g., Redis)
- Add asynchronous processing (e.g., @Async)
- Implement unit and integration tests
- Add monitoring and logging (e.g., Actuator, SLF4J)
- Support for microservices architecture

---

**Last Updated:** January 29, 2026
