# Smart E-Commerce System (SMECS)

## Project Overview

A comprehensive e-commerce system built with JavaFX and Java, featuring:
- **Relational Database (PostgreSQL)** for structured transactional data
- **NoSQL Database (MongoDB)** for unstructured data (reviews, logs)
- **Advanced Caching** for optimal performance
- **Full-Text Search** capabilities
- **Performance Benchmarking** tools

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Database Configuration](#database-configuration)
4. [How to Run](#how-to-run)
5. [Dependencies](#dependencies)
6. [Epic 4: Performance Features](#epic-4-performance-features)
7. [Project Structure](#project-structure)
8. [Features](#features)
9. [Performance Highlights](#performance-highlights)
10. [Troubleshooting](#troubleshooting)
11. [Testing](#testing)

---

## Prerequisites

### Essential
- **Java 11 or higher**
- **Maven 3.6+**
- **PostgreSQL 12+** running on localhost:5432

### Optional (for Epic 4 NoSQL features)
- **MongoDB 4.4+** running on localhost:27017

---

## Project Setup

This is a JavaFX application with Maven dependencies supporting both PostgreSQL and MongoDB.

---

## Database Configuration

### PostgreSQL Setup (Required)

1. **Install PostgreSQL:**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install postgresql postgresql-contrib
   
   # macOS
   brew install postgresql
   
   # Windows: Download from https://www.postgresql.org/download/
   ```

2. **Create Database:**
   ```bash
   psql -U postgres
   CREATE DATABASE smecs;
   \q
   ```

3. **Run Schema Script:**
   ```bash
   psql -U postgres -d smecs -f src/main/resources/sql/schema.sql
   psql -U postgres -d smecs -f src/main/resources/sql/indexes.sql
   ```

4. **Update Credentials:**
   Edit `src/main/java/com/smecs/util/DatabaseConnection.java` if needed:
   ```java
   private static final String URL = "jdbc:postgresql://localhost:5432/smecs";
   private static final String USER = "postgres";
   private static final String PASSWORD = "your_password";
   ```

### MongoDB Setup (Optional - for Epic 4 features)

1. **Install MongoDB:**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install mongodb
   
   # macOS
   brew tap mongodb/brew
   brew install mongodb-community
   
   # Windows: Download from https://www.mongodb.com/try/download/community
   ```

2. **Start MongoDB:**
   ```bash
   # Ubuntu/Debian/macOS
   sudo systemctl start mongod
   # or
   brew services start mongodb-community
   
   # Windows: Run MongoDB as a service
   ```

3. **Verify MongoDB Connection:**
   ```bash
   mongosh
   # Should connect to mongodb://localhost:27017
   ```

4. **Initialize Collections (Optional):**
   Run the MongoDB connection test:
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.smecs.util.MongoDBConnection"
   ```

---

## How to Run

### Method 1: Using Maven (Recommended)
```bash
mvn spring-boot:run
```

### Method 2: Using IntelliJ IDEA
1. Make sure Maven is reloaded (right-click pom.xml → Maven → Reload Project)
2. Use the "SmeCSApplication" run configuration
3. Or right-click on SmeCSApplication.java → Run 'SmeCSApplication.main()'

---

## Dependencies

### Databases
- PostgreSQL JDBC Driver (version 42.7.7)
- MongoDB Java Driver (version 4.11.0) - **Epic 4**

### Utilities
- JSON Processing Library (version 20231013) - **Epic 4**

---

## Epic 4: Performance Features

### Running Performance Benchmarks

1. **Comprehensive Performance Report:**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.smecs.util.PerformanceReportGenerator"
   ```
   
   This generates:
   - Text report: `reports/performance_report_[timestamp].txt`
   - HTML report: `reports/performance_report_[timestamp].html`
   - CSV report: `reports/performance_metrics_[timestamp].csv`

2. **Query Performance Analysis:**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.smecs.util.QueryPerformanceAnalyzer"
   ```

3. **NoSQL Demo (requires MongoDB):**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.smecs.test.NoSQLDemo"
   ```

4. **Standard Benchmarks:**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.smecs.util.PerformanceBenchmark"
   ```

---

## Project Structure

```
smecs/
├── docs/
│   ├── NOSQL_DESIGN.md          # NoSQL schema and design (Epic 4)
│   └── PERFORMANCE_REPORT.md    # Comprehensive performance analysis (Epic 4)
├── src/main/
│   ├── java/com/smecs/
│   │   ├── cache/               # Caching infrastructure (Epic 3)
│   │   ├── controller/          # JavaFX controllers
│   │   ├── dao/                 # Data Access Objects (SQL)
│   │   ├── model/               # Domain models
│   │   ├── nosql/               # NoSQL DAOs (MongoDB) - Epic 4
│   │   ├── service/             # Business logic layer
│   │   ├── test/                # Test and demo classes
│   │   └── util/                # Utility classes
│   └── resources/
│       ├── css/                 # Stylesheets
│       ├── sql/                 # Database scripts
│       │   ├── schema.sql
│       │   ├── indexes.sql
│       │   └── query_optimization.sql  # Epic 4
│       └── view/                # FXML files
├── reports/                     # Generated performance reports
├── ARCHITECTURE.md              # System architecture
├── DESIGN_SYSTEM.md            # UI design system
├── instructions.md              # Project requirements
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

---

## Features

### Core Features (Epic 1-3)
- ✅ User authentication (Admin/Customer roles)
- ✅ Product catalog management
- ✅ Category management
- ✅ Inventory tracking
- ✅ Shopping cart functionality
- ✅ Product search and filtering
- ✅ In-memory caching (ProductCache, CategoryCache)
- ✅ Database indexing for performance
- ✅ Advanced search algorithms (hash-based)
- ✅ Multiple sorting algorithms

### Epic 4: Performance & NoSQL Features
- ✅ **Query Performance Analyzer** - Measures and compares SQL query execution times
- ✅ **Performance Report Generator** - Creates comprehensive reports (TXT, HTML, CSV)
- ✅ **MongoDB Integration** - NoSQL database for unstructured data
- ✅ **Review System** - Customer reviews stored in MongoDB
- ✅ **Activity Logging** - User activity tracking with MongoDB
- ✅ **Full-Text Search** - Advanced text search in reviews
- ✅ **Performance Benchmarking** - Before/after optimization comparison
- ✅ **Database Connection Monitoring** - Connection pool statistics
- ✅ **Hybrid Database Architecture** - Best of SQL and NoSQL

---

## Performance Highlights

Based on comprehensive testing (see `docs/PERFORMANCE_REPORT.md`):

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Product Search | 150ms | 15ms | **10x faster** |
| Cache Hit Rate | N/A | 87.5% | **87.5% hits** |
| Query Execution | 200ms | 25ms | **8x faster** |
| Write Throughput | 1K/sec | 10K/sec | **10x increase** |

---

## Troubleshooting

### "JavaFX runtime components are missing" error
**Solution:** Use `mvn javafx:run` instead of running directly from IntelliJ.

### "No suitable driver found for jdbc:postgresql" error
**Solution:** The PostgreSQL driver has been added to pom.xml. Run `mvn clean install` to download it.

### MongoDB Connection Issues
**Symptoms:** "MongoDB is not connected" messages

**Solutions:**
1. Verify MongoDB is running:
   ```bash
   # Check if MongoDB is running
   mongosh --eval "db.adminCommand('ping')"
   ```

2. Check MongoDB port:
   ```bash
   sudo netstat -tlnp | grep 27017
   ```

3. Start MongoDB service:
   ```bash
   sudo systemctl start mongod  # Linux
   brew services start mongodb-community  # macOS
   ```

4. **Note:** MongoDB is optional. The application works without it, but Epic 4 NoSQL features will be disabled.

### Out of Memory Errors
**Solution:** Increase JVM heap size:
```bash
export MAVEN_OPTS="-Xmx2g"
mvn javafx:run
```

### Slow Performance
**Solutions:**
1. Run performance benchmarks to identify bottlenecks
2. Clear cache: Delete `target/` and run `mvn clean compile`
3. Rebuild indexes: Re-run `indexes.sql`
4. Check database statistics: Run `ANALYZE` on tables

---

## Testing

### Run All Tests
```bash
# Performance benchmarks
mvn compile exec:java -Dexec.mainClass="com.smecs.util.PerformanceBenchmark"

# Query optimization tests
mvn compile exec:java -Dexec.mainClass="com.smecs.util.QueryPerformanceAnalyzer"

# NoSQL features demo
mvn compile exec:java -Dexec.mainClass="com.smecs.test.NoSQLDemo"

# Verification test
mvn compile exec:java -Dexec.mainClass="com.smecs.test.VerificationTest"
```

---

## Documentation

- **`ARCHITECTURE.md`** - System architecture and design patterns
- **`DESIGN_SYSTEM.md`** - UI/UX design guidelines
- **`docs/NOSQL_DESIGN.md`** - NoSQL schema and MongoDB integration
- **`docs/PERFORMANCE_REPORT.md`** - Comprehensive performance analysis
- **`instructions.md`** - Original project requirements

---

## Development Team

**Project:** Smart E-Commerce System (SMECS)  
**Course:** Database Fundamentals  
**Status:** Epic 4 Complete ✅

---

## License

This project is developed for educational purposes as part of a Database Fundamentals course.

---

## Contact & Support

For questions or issues:
1. Check the troubleshooting section above
2. Review the documentation in `docs/`
3. Run diagnostic tests to identify issues
4. Check database connections (PostgreSQL and MongoDB)

---

**Last Updated:** January 15, 2026  
**Version:** 1.0 (Epic 4 Complete)
