# Smart E-Commerce System: Spring Security

**Complexity:** Advanced | **Time Estimate:** 10-12 hours 

---

## Project Overview

This phase emphasizes building **secure, scalable, and production-ready web backends** that can safely handle authentication, authorization, and cross-origin requests. Learners will apply Spring Security concepts to secure REST and GraphQL APIs using:

* 
**JWT-based authentication** 


* 
**OAuth2 login (Google)** 


* 
**Role-Based Access Control (RBAC)** 


* 
**Security-related DSA concepts** such as hashing, encryption, and secure token validation 



---

## Project Objectives

By the end of this project, learners will be able to:

1. Apply Spring Security configurations to enforce access control across REST and GraphQL APIs.


2. Implement JWT authentication, Google OAuth2 login, and secure password hashing using **BCrypt**.


3. Configure and analyze **CORS and CSRF** policies for different client interactions.


4. Apply DSA concepts (hashing, encryption, token validation) to strengthen data security.


5. Develop and test RBAC and secure endpoint communication for real-world deployment.



---

## Epics and User Stories

### Epic 1: Security Configuration and Access Policies

* 
**User Story 1.1:** As a developer, I want to configure Spring Security filters to protect endpoints and define authentication requirements.


* 
**Acceptance Criteria:** `SecurityFilterChain` configured; public and restricted endpoints defined; passwords stored using `BCryptPasswordEncoder`.




* 
**User Story 1.2:** As a frontend developer, I want to enable secure cross-origin requests.


* 
**Acceptance Criteria:** Global CORS configuration implemented; unauthorized origins rejected; tested with Postman and web frontend.





### Epic 2: JWT-Based Authentication

* 
**User Story 2.1:** As a user, I want to log in and receive a JWT token to access restricted endpoints.


* 
**Acceptance Criteria:** `/auth/login` generates signed JWTs; tokens validated on each request; tampered/expired tokens rejected with 401.




* 
**User Story 2.2:** As a system analyst, I want to verify token structure and claims.


* **Acceptance Criteria:** JWT includes subject, issued time, and expiration; HMAC SHA-256 or RSA used; payload viewable in Postman.





### Epic 3: CSRF and Session Security

* 
**User Story 3.1:** As a security engineer, I want to configure CSRF protection properly.


* 
**Acceptance Criteria:** CSRF disabled for stateless JWT APIs; explanation provided for when to enable it; mechanism demonstrated in one endpoint.




* 
**User Story 3.2:** As a developer, I want to document CSRF and CORS differences.


* 
**Acceptance Criteria:** Technical documentation included in README; practical demonstration using Postman and browser.





### Epic 4: OAuth2 and Role-Based Access Control (RBAC)

* 
**User Story 4.1:** As a user, I want to log in with my Google account.


* 
**Acceptance Criteria:** OAuth2 integrated with Google; user details persisted; roles assigned after authentication.




* 
**User Story 4.2:** As an administrator, I want to restrict access based on user roles.


* 
**Acceptance Criteria:** Roles defined (ADMIN, CUSTOMER, STAFF); endpoints annotated with `@PreAuthorize` or `@Secured`; verified via Postman.





### Epic 5: DSA and Security Optimization

* 
**User Story 5.1:** As a developer, I want to apply DSA principles to improve security and performance.


* 
**Acceptance Criteria:** Hashing for passwords/tokens; caching or lookup map for token blacklisting.




* 
**User Story 5.2:** As an auditor, I want to analyze security event logs.


* 
**Acceptance Criteria:** Logging for auth success/failure; reports on token usage; logs reviewed for brute-force detection.





---

## Technical Requirements

| # | Area | Description |
| --- | --- | --- |
| 1 | **Framework** | Spring Boot 3.x (Spring Security, JWT, OAuth2 Client, Validation, Cache) 

 |
| 2 | **Language** | Java 21 

 |
| 3 | **Authentication** | Username/password with JWT, OAuth2 login (Google) 

 |
| 4 | **Authorization** | Role-Based Access Control (RBAC) 

 |
| 5 | **Security Features** | CORS setup, CSRF demonstration, password hashing 

 |
| 6 | **Password Encryption** | BCryptPasswordEncoder 

 |
| 7 | **Database** | Existing database extended with user and role entities 

 |
| 8 | **Testing** | Postman or web frontend for login and authorization testing 

 |
| 9 | **Documentation** | OpenAPI documentation covering secured and public endpoints 

 |
| 10 | **DSA Integration** | Hashing, token validation, map-based blacklisting, and secure lookup design 

 |

---

## Deliverables

1. 
**Spring Security Integration:** Application configured with auth filters.


2. 
**JWT Authentication System:** Functional login, token generation, and validation.


3. 
**CORS & CSRF Configuration:** Working setup with documentation and demonstrations.


4. 
**OAuth2 (Google Login):** Social login with user persistence and mapped roles.


5. 
**RBAC Enforcement:** Role-based access control on key endpoints.


6. 
**Security Event Logging:** Logs capturing authentication and access details.


7. 
**DSA Implementation:** Applied hashing, caching, and lookup optimization.


8. 
**README & OpenAPI Docs:** Comprehensive setup and testing documentation.



---

## Evaluation Criteria

| Category | Description | Points |
| --- | --- | --- |
| **Security Configuration (CORS & CSRF)** | Correct configuration and explanation of use cases 

 | 15 |
| **JWT Implementation** | Functional JWT auth with secure generation and validation 

 | 20 |
| **OAuth2 (Google Integration)** | External login with consistent user mapping and persistence 

 | 15 |
| **RBAC and Role Enforcement** | Role-based restrictions implemented accurately 

 | 15 |
| **DSA in Security** | Application of hashing, lookup, or caching for security logic 

 | 15 |
| **Testing & Logging** | Postman tests, security event logs, and error tracking 

 | 10 |
| **Code Quality & Documentation** | Organized configuration and complete API documentation 

 | 10 |
| **Total** |  | **100 pts** |
