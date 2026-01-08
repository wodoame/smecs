# Smart E-Commerce System

## Database Fundamentals

* **Complexity:** Advanced
* 
**Time Estimate:** 10-12 hours 



---

## Project Objectives

By the end of this project, learners will be able to:

* Design and normalize a relational database schema for an e-commerce domain.


* Develop conceptual, logical, and physical database models to ensure scalability.


* Implement CRUD operations and complex queries using SQL and JDBC.


* Apply indexing, hashing, searching, and sorting algorithms to optimize access.


* Integrate database operations into a JavaFX application interface.


* Compare relational and NoSQL designs for unstructured data storage.


* Measure and document performance improvements through optimization.



---

## Project Description

This project focuses on building the data layer and persistence logic for a Smart E-Commerce System. Students will design a relational database, implement the schema via SQL, and integrate it with a JavaFX application for product listing, searching, order management, and reporting. The system must reflect real-world logic—including users, products, orders, and inventory—while incorporating data structures and algorithms like caching and indexing to enhance performance.

---

## Epics and User Stories

### Epic 1: Database Design and Modeling

* 
**User Story 1.1:** As a designer, I want to create conceptual, logical, and physical models so the data structure is clear.


* 
**Acceptance Criteria:** Includes ERD, defined attributes/keys, and normalization up to 3NF.




* 
**User Story 1.2:** As an administrator, I want to define indexes and relationships to maintain data integrity and efficiency.


* 
**Acceptance Criteria:** Primary/foreign keys applied; indexes defined on frequently searched columns.





### Epic 2: Data Access and CRUD Operations

* 
**User Story 2.1:** As an administrator, I want to manage inventory (add, update, delete) from the JavaFX interface.


* 
**Acceptance Criteria:** Functional CRUD operations, input validation, and constraint enforcement.




* 
**User Story 2.2:** As a user, I want to view product details to make purchase decisions.


* 
**Acceptance Criteria:** Dynamic listings, pagination/search filters, and parameterized JDBC queries.





### Epic 3: Searching, Sorting, and Optimization

* 
**User Story 3.1:** As a customer, I want to search for products quickly by name or category.


* 
**Acceptance Criteria:** Case-insensitive search optimized through indexing or hashing.




* 
**User Story 3.2:** As a developer, I want to implement in-memory caching and sorting for faster retrieval.


* 
**Acceptance Criteria:** Caching layer using maps/lists with invalidation logic.





### Epic 4: Performance and Query Optimization

* 
**User Story 4.1:** As an analyst, I want to generate performance reports comparing pre- and post-optimization.


* 
**User Story 4.2:** As a developer, I want to explore NoSQL for customer feedback or logs.



### Epic 5: Reporting and Documentation

* 
**User Story 5.1:** As a contributor, I want to produce documentation so the project can be reviewed or extended.



---

## Technical Requirements

### Database

* 
**Platform:** Use MySQL or PostgreSQL.


* 
**Normalization:** Schema must be in Third Normal Form (3NF).


* 
**Entities:** Must include Users, Products, Categories, Orders, OrderItems, Reviews, and Inventory.


* 
**Integrity:** Define indexes on high-frequency columns and implement foreign keys.



### Application Layer (JavaFX + JDBC)

* 
**UI:** JavaFX for interaction (listings, search, admin panel).


* 
**Access:** Use JDBC with parameterized queries to prevent SQL injection.


* 
**Architecture:** Logical separation of concerns (Controller - Service - DAO).


* 
**DSA:** Use in-memory structures (Map, List) for caching and sorting.



---

## Deliverables

| # | Deliverable | Description |
| --- | --- | --- |
| 1 | Database Design Document | Conceptual, logical, and physical models with ERDs.

 |
| 2 | SQL Implementation Script | Scripts for tables, constraints, indexes, and sample data.

 |
| 3 | JavaFX Application | Functional interface for CRUD, search, and reporting.

 |
| 4 | Performance Report | Comparative analysis of optimization metrics.

 |
| 5 | NoSQL Design (Optional) | Schema or implementation for unstructured data.

 |
| 6 | README File | Setup guide, dependencies, and execution steps.

 |
| 7 | Testing Evidence | Screenshots showing query results and validation.

 |

---

## Evaluation Criteria

| Category | Evaluation Criteria | Points |
| --- | --- | --- |
| **Database Design** | Models are complete, normalized (3NF), and documented with ERDs.

 | 25 |
| **SQL Implementation** | Syntactically correct schema, constraints, sample data, and indexes.

 | 20 |
| **JavaFX + JDBC** | Functional CRUD, UI usability, safe JDBC handling, and error feedback.

 | 20 |
| **DSA Application** | Use of caching, sorting, and searching with performance justification.

 | 15 |
| **Performance** | Demonstrated gains through timing and analysis.

 | 10 |
| **Documentation** | README completeness, code organization, and clean coding practices.

 | 10 |
| **Total** |  | **100 pts** |
