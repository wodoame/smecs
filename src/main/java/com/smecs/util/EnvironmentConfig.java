package com.smecs.util;

/**
 * Centralized environment configuration manager.
 * Loads sensitive credentials and configuration from environment variables.
 *
 * Required Environment Variables:
 * - DB_HOST: PostgreSQL host (default: localhost)
 * - DB_PORT: PostgreSQL port (default: 5432)
 * - DB_NAME: PostgreSQL database name (default: smecs)
 * - DB_USER: PostgreSQL username (required)
 * - DB_PASSWORD: PostgreSQL password (required)
 * - MONGODB_URI: MongoDB connection string (optional, for NoSQL features)
 * - LOG_LEVEL: Logging level (default: INFO)
 */
public class EnvironmentConfig {

    // PostgreSQL Configuration
    public static final String DB_HOST = getEnv("DB_HOST", "localhost");
    public static final int DB_PORT = Integer.parseInt(getEnv("DB_PORT", "5432"));
    public static final String DB_NAME = getEnv("DB_NAME", "smecs");
    public static final String DB_USER = getEnvRequired("DB_USER");
    public static final String DB_PASSWORD = getEnvRequired("DB_PASSWORD");

    // MongoDB Configuration
    public static final String MONGODB_URI = getEnv("MONGODB_URI", null);
    public static final String MONGODB_DATABASE = getEnv("MONGODB_DATABASE", "smecs");

    // Application Configuration
    public static final String LOG_LEVEL = getEnv("LOG_LEVEL", "INFO");
    public static final boolean REPORT_MONITORING_ENABLED = Boolean.parseBoolean(getEnv("REPORT_MONITORING_ENABLED", "true"));

    /**
     * Get environment variable with default value
     * @param key Environment variable name
     * @param defaultValue Default value if not found
     * @return Environment variable value or default
     */
    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Get required environment variable
     * @param key Environment variable name
     * @return Environment variable value
     * @throws IllegalArgumentException if variable is not set
     */
    public static String getEnvRequired(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Required environment variable not set: " + key +
                "\nPlease set this variable before running the application."
            );
        }
        return value;
    }

    /**
     * Build PostgreSQL JDBC URL
     * @return JDBC connection URL for PostgreSQL
     */
    public static String getPostgresJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", DB_HOST, DB_PORT, DB_NAME);
    }

    /**
     * Check if MongoDB is configured
     * @return true if MongoDB URI is set
     */
    public static boolean isMongoDBConfigured() {
        return MONGODB_URI != null && !MONGODB_URI.isEmpty();
    }

    /**
     * Log configuration on startup (masks sensitive data)
     */
    public static void logConfiguration() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Configuration Loaded from Environment Variables:");
        System.out.println("=".repeat(70));
        System.out.println("Database Host: " + DB_HOST);
        System.out.println("Database Port: " + DB_PORT);
        System.out.println("Database Name: " + DB_NAME);
        System.out.println("Database User: " + DB_USER);
        System.out.println("Database Password: ****" + (DB_PASSWORD.length() > 4 ? DB_PASSWORD.substring(DB_PASSWORD.length() - 4) : ""));
        System.out.println("MongoDB Configured: " + isMongoDBConfigured());
        if (isMongoDBConfigured()) {
            System.out.println("MongoDB Database: " + MONGODB_DATABASE);
        }
        System.out.println("Log Level: " + LOG_LEVEL);
        System.out.println("Report Monitoring: " + (REPORT_MONITORING_ENABLED ? "Enabled" : "Disabled"));
        System.out.println("=".repeat(70) + "\n");
    }
}

