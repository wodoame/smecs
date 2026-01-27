# Smart E-Commerce System

**Complexity:** Advanced | **Time Estimate:** 10-12 hours 

---

## Project Overview

This phase of the **Smart E-Commerce System** transforms the existing database foundation into a web-based **Spring Boot** application. Learners will design and implement **RESTful and GraphQL APIs** while applying validation, exception handling, Aspect-Oriented Programming (AOP), and OpenAPI documentation.

### Project Objectives

By the end of this project, learners will be able to:

* Apply Spring Boot configuration principles, IoC, and **Dependency Injection** for modular applications.


* Develop RESTful APIs using a **layered architecture** (Controller-Service-Repository).


* Implement validation and exception handling using **Bean Validation** and `@ControllerAdvice`.


* Integrate **GraphQL** schemas, queries, and mutations alongside REST endpoints.


* Apply **AOP** and algorithmic techniques for logging, monitoring, and efficient data processing.



---

## Epics and User Stories

### Epic 1: Application Setup and Dependency Management

* 
**User Story 1.1:** As a developer, I want to configure and structure a Spring Boot project to run efficiently across multiple environments.


* **Acceptance Criteria:**
* Project initialized with required dependencies.


* Profiles configured for **dev, test, and prod**.


* Consistent use of **constructor-based dependency injection**.







### Epic 2: RESTful API Development

* 
**User Story 2.1:** As an administrator, I want to manage users, products, and categories through REST endpoints.


* **Acceptance Criteria:**
* CRUD APIs implemented following REST conventions.


* Responses structured with status, message, and data.


* Layered communication: Controllers → Services → Repositories.






* 
**User Story 2.2:** As a customer, I want to view, sort, and filter products easily.


* **Acceptance Criteria:**
* Support for **pagination, sorting, and filtering**.


* Efficient algorithms used for data retrieval.







### Epic 3: Validation, Exception Handling, and Documentation

* 
**User Story 3.1:** As a developer, I want to validate and document all API endpoints for consistency.


* **Acceptance Criteria:**
* Bean Validation annotations on DTOS.


* Custom validators for complex rules.


* 
**OpenAPI documentation** generated automatically.







### Epic 4: GraphQL Integration

* 
**User Story 4.1:** As a frontend developer, I want to fetch data using GraphQL to retrieve only the information I need.


* **Acceptance Criteria:**
* GraphQL schema defined for key entities.


* Successful implementation of queries and mutations.


* Coexistence of REST and GraphQL endpoints.







### Epic 5: Cross-Cutting Concerns (AOP)

* 
**User Story 5.1:** As a developer, I want to use AOP for centralized logging and monitoring.


* **Acceptance Criteria:**
* Aspects implemented using `@Before`, `@After`, and `@Around`.


* Applied to critical service methods.







---

## Technical Requirements

| Area | Description |
| --- | --- |
| **Framework** | Spring Boot 3.x (Spring Web, Validation, AOP, GraphQL, Springdoc OpenAPI) 

 |
| **Language** | Java 21 

 |
| **Database** | Relational database from Module 4 

 |
| **Architecture** | Layered (Controller → Service → Repository) 

 |
| **Validation** | Bean Validation and custom validators 

 |
| **Documentation** | Springdoc OpenAPI for Swagger 

 |
| **DSA Integration** | Sorting, searching, and pagination algorithms 

 |

---

## Deliverables

1. 
**Spring Boot Web Application:** Backend with REST and GraphQL APIs.


2. 
**Validation and Exception Handling:** DTOs and centralized error management.


3. 
**API Documentation:** Interactive Swagger/OpenAPI.


4. 
**AOP Implementation:** Logging and performance aspects.


5. 
**GraphQL Schema and Queries:** Defined schema with mutations.


6. 
**Performance Report:** Comparison between REST and GraphQL.


7. 
**README File:** Setup instructions and testing guide.



---

## Evaluation Criteria

| Category | Description | Points |
| --- | --- | --- |
| Spring Boot & IoC | Proper setup and Dependency Injection 

 | 15 |
| REST API | CRUD and architecture implementation 

 | 20 |
| Validation & Docs | Validation, error handling, and OpenAPI 

 | 20 |
| GraphQL | Queries, mutations, and performance comparisons 

 | 15 |
| AOP & DSA | Logging, monitoring, and algorithmic improvements 

 | 15 |
| Code Quality | Readability, structure, and reporting 

 | 15 |
| **Total** |  | <br>**100 pts** 

 |
