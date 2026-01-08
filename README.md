# Smart E-Commerce System (SMECS)

## Project Setup

This is a JavaFX application with Maven dependencies.

## Dependencies Added

### JavaFX (version 21.0.1)
- javafx-controls
- javafx-fxml
- javafx-graphics
- javafx-base

### Database
- PostgreSQL JDBC Driver (version 42.7.1)

## How to Run

### Method 1: Using Maven (Recommended)
```bash
mvn clean javafx:run
```

### Method 2: Using IntelliJ IDEA
1. Make sure Maven is reloaded (right-click pom.xml → Maven → Reload Project)
2. Use the "MainApp" run configuration
3. Or right-click on MainApp.java → Run 'MainApp.main()'

## Requirements
- Java 11 or higher
- Maven
- PostgreSQL database running on localhost:5432

## Database Configuration
Make sure your PostgreSQL database is running and accessible at:
- URL: jdbc:postgresql://localhost:5432/smecs
- Update credentials in DatabaseConnection.java if needed

## Troubleshooting

### "JavaFX runtime components are missing" error
**Solution:** Use `mvn javafx:run` instead of running directly from IntelliJ.

### "No suitable driver found for jdbc:postgresql" error
**Solution:** The PostgreSQL driver has been added to pom.xml. Run `mvn clean install` to download it.

