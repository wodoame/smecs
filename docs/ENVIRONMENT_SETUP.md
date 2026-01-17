# Environment Variables Configuration Guide

## Overview

This application uses environment variables to manage sensitive credentials and configuration. This approach follows security best practices by:

- **Keeping secrets out of source code** - Credentials are never committed to version control
- **Supporting different environments** - Different credentials for dev, staging, and production
- **Simplifying deployment** - Configuration is externalized from the application
- **Reducing security risks** - No hardcoded passwords in the codebase

## Required Environment Variables

### PostgreSQL Database Configuration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | No | `localhost` | PostgreSQL server hostname or IP address |
| `DB_PORT` | No | `5432` | PostgreSQL server port |
| `DB_NAME` | No | `smecs` | Database name |
| `DB_USER` | **Yes** | None | PostgreSQL username |
| `DB_PASSWORD` | **Yes** | None | PostgreSQL password |

### MongoDB Configuration (Optional)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `MONGODB_URI` | No | None | MongoDB connection string. Leave empty to disable MongoDB support. |
| `MONGODB_DATABASE` | No | `smecs` | MongoDB database name |

### Application Configuration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `LOG_LEVEL` | No | `INFO` | Logging level (DEBUG, INFO, WARN, ERROR) |
| `REPORT_MONITORING_ENABLED` | No | `true` | Enable/disable report monitoring |

## Setup Instructions

### Option 1: Linux/macOS (Bash/Zsh)

1. **Copy the example environment file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit the .env file with your credentials:**
   ```bash
   nano .env
   # or
   vi .env
   ```

3. **Load environment variables before running the application:**
   ```bash
   # Load all variables from .env file
   export $(cat .env | grep -v '^#' | xargs)
   
   # Run the application
   mvn javafx:run
   ```

   Or use a simpler approach:
   ```bash
   # Load and run in a single command
   set -a
   source .env
   set +a
   mvn javafx:run
   ```

### Option 2: Windows (PowerShell)

1. **Copy the example environment file:**
   ```powershell
   Copy-Item .env.example .env
   ```

2. **Edit the .env file with your credentials:**
   ```powershell
   notepad .env
   ```

3. **Set environment variables before running:**
   ```powershell
   $env:DB_HOST = "localhost"
   $env:DB_PORT = "5432"
   $env:DB_NAME = "smecs"
   $env:DB_USER = "postgres"
   $env:DB_PASSWORD = "your_password"
   
   mvn javafx:run
   ```

### Option 3: Windows (Command Prompt)

```cmd
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=smecs
set DB_USER=postgres
set DB_PASSWORD=your_password

mvn javafx:run
```

### Option 4: IDE Configuration (IntelliJ IDEA)

1. **Open Run Configuration:**
   - Run → Edit Configurations...

2. **Set Environment Variables:**
   - Find the "Environment variables" field
   - Enter variables in format: `KEY1=value1;KEY2=value2`
   
   Example:
   ```
   DB_HOST=localhost;DB_PORT=5432;DB_NAME=smecs;DB_USER=postgres;DB_PASSWORD=your_password
   ```

3. **Apply and run**

### Option 5: Docker (Recommended for Production)

**Dockerfile example:**
```dockerfile
FROM openjdk:11
WORKDIR /app
COPY . .
RUN mvn clean package

# Set environment variables
ENV DB_HOST=postgres-service
ENV DB_PORT=5432
ENV DB_NAME=smecs
ENV DB_USER=postgres
ENV DB_PASSWORD=${DB_PASSWORD}

CMD ["java", "-jar", "target/smecs-1.0-SNAPSHOT.jar"]
```

**Run with docker-compose:**
```yaml
version: '3.8'
services:
  app:
    build: .
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=smecs
      - DB_USER=postgres
      - DB_PASSWORD=secure_password
  postgres:
    image: postgres:13
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: secure_password
      POSTGRES_DB: smecs
```

## Security Best Practices

### ✅ DO:
- Use strong, unique passwords for each environment
- Store .env files securely (outside version control)
- Use `.gitignore` to prevent committing .env files
- Rotate credentials regularly
- Use environment-specific values for dev/staging/production
- Use secrets management tools in production (AWS Secrets Manager, Azure Key Vault, etc.)

### ❌ DON'T:
- Commit .env files to version control
- Use the same password for multiple environments
- Share credentials via email or chat
- Use generic passwords like "password123"
- Log or print sensitive information

## .gitignore Configuration

Ensure your `.gitignore` file contains:
```
.env
.env.local
.env.*.local
*.properties
!.env.example
```

## Configuration on Startup

When the application starts, it will:

1. **Load environment variables** from the system
2. **Validate required variables** (DB_USER and DB_PASSWORD)
3. **Log configuration** (masking sensitive data)
4. **Initialize database connections** using the provided credentials

Example startup output:
```
======================================================================
Configuration Loaded from Environment Variables:
======================================================================
Database Host: localhost
Database Port: 5432
Database Name: smecs
Database User: postgres
Database Password: ****2003
MongoDB Configured: false
Log Level: INFO
Report Monitoring: Enabled
======================================================================
```

## Troubleshooting

### Error: "Required environment variable not set: DB_USER"
**Solution:** Set the DB_USER environment variable before running the application.

### Error: "Failed to connect to PostgreSQL"
**Possible causes:**
- Incorrect DB_HOST or DB_PORT
- Database server not running
- Wrong DB_USER or DB_PASSWORD
- Database doesn't exist

**Solution:** Verify your connection parameters and ensure PostgreSQL is running.

### MongoDB Connection Fails
**Solution:** MongoDB is optional. Leave MONGODB_URI empty or ensure your connection string is correct.

## Advanced Configuration

### Environment-Specific Configurations

Create multiple environment files for different deployments:

**Development (.env.dev):**
```
DB_HOST=localhost
DB_PORT=5432
DB_USER=dev_user
DB_PASSWORD=dev_password
LOG_LEVEL=DEBUG
```

**Production (.env.prod):**
```
DB_HOST=prod-db.example.com
DB_PORT=5432
DB_USER=prod_user
DB_PASSWORD=prod_secure_password
LOG_LEVEL=INFO
```

**Load specific environment:**
```bash
source .env.prod
mvn javafx:run
```

### Using External Secret Management

For production, integrate with external secret managers:

**AWS Secrets Manager:**
```bash
aws secretsmanager get-secret-value --secret-id smecs/db --query SecretString --output text > .env
source .env
```

**HashiCorp Vault:**
```bash
vault read -field=value secret/data/smecs/db > .env
source .env
```

## Code References

### EnvironmentConfig.java
Central configuration class that loads all environment variables:
- Location: `src/main/java/com/smecs/util/EnvironmentConfig.java`
- Provides methods: `getEnv()`, `getEnvRequired()`, `logConfiguration()`

### DatabaseConnection.java
Updated to use environment variables for PostgreSQL connections:
- Location: `src/main/java/com/smecs/util/DatabaseConnection.java`
- Uses: `EnvironmentConfig.getPostgresJdbcUrl()`

### MongoDBConnection.java
Updated to use environment variables for MongoDB connections:
- Location: `src/main/java/com/smecs/util/MongoDBConnection.java`
- Uses: `EnvironmentConfig.MONGODB_URI`

### MainApp.java
Application entry point that validates configuration on startup:
- Location: `src/main/java/com/smecs/MainApp.java`
- Initializes: `EnvironmentConfig.logConfiguration()`

## Additional Resources

- [12-Factor App Configuration](https://12factor.net/config)
- [OWASP: Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [PostgreSQL JDBC Connection Strings](https://jdbc.postgresql.org/documentation/head/connect.html)
- [MongoDB Connection Strings](https://docs.mongodb.com/manual/reference/connection-string/)

## Support

For issues or questions regarding environment configuration, please refer to:
- ARCHITECTURE.md - System design and structure
- README.md - Project overview
- docs/ - Additional documentation

