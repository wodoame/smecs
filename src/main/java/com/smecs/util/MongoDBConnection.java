package com.smecs.util;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDB Connection utility for Epic 4.
 * Manages connection to MongoDB for unstructured data storage.
 * Connection configured via MONGODB_URI environment variable.
 */
public class MongoDBConnection {
    
    private static final String CONNECTION_STRING = EnvironmentConfig.MONGODB_URI;
    private static final String DATABASE_NAME = EnvironmentConfig.MONGODB_DATABASE;

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;
    
    /**
     * Get MongoDB client instance (singleton pattern)
     */
    public static void getClient() {
        if (mongoClient == null) {
            synchronized (MongoDBConnection.class) {
                if (mongoClient == null) {
                    initializeConnection();
                }
            }
        }
    }
    
    /**
     * Get MongoDB database instance
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            synchronized (MongoDBConnection.class) {
                if (database == null) {
                    getClient(); // Ensure client is initialized
                    database = mongoClient.getDatabase(DATABASE_NAME);
                }
            }
        }
        return database;
    }
    
    /**
     * Initialize MongoDB connection
     */
    private static void initializeConnection() {
        // Check if MongoDB is configured
        if (!EnvironmentConfig.isMongoDBConfigured()) {
            System.out.println("MongoDB is not configured. Set MONGODB_URI environment variable to enable MongoDB support.");
            return;
        }

        try {
            // Configure MongoDB client settings
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                    .serverApi(serverApi)
                    .build();
            
            // Create MongoClient
            mongoClient = MongoClients.create(settings);
            
            // Test connection
            database = mongoClient.getDatabase(DATABASE_NAME);
            database.listCollectionNames().first(); // Ping database
            
            System.out.println("MongoDB connection established successfully");
            System.out.println("Database: " + DATABASE_NAME);
            
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB Atlas: " + e.getMessage());
            System.err.println("Please check your internet connection and MongoDB Atlas credentials");
            System.err.println("Connection string: " + CONNECTION_STRING.replaceAll(":[^:@]+@", ":****@"));
            System.err.println("Application will continue with relational database only.");
            // Don't throw exception - allow app to run without MongoDB
        }
    }
    
    /**
     * Check if MongoDB is connected
     */
    public static boolean isConnected() {
        try {
            if (mongoClient != null && database != null) {
                database.listCollectionNames().first();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    
    /**
     * Close MongoDB connection
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("MongoDB connection closed");
        }
    }
    
    /**
     * Get connection statistics
     */
    public static String getConnectionInfo() {
        if (isConnected()) {
            StringBuilder info = new StringBuilder();
            info.append("MongoDB Connection Info:\n");
            info.append("  Status: Connected\n");
            info.append("  Database: ").append(DATABASE_NAME).append("\n");
            info.append("  Server: MongoDB Atlas (").append(CONNECTION_STRING.replaceAll(":[^:@]+@", ":****@")).append(")\n");

            // List collections
            info.append("  Collections: ");
            database.listCollectionNames().forEach(name -> info.append(name).append(", "));
            info.append("\n");
            
            return info.toString();
        } else {
            return "MongoDB: Not connected\n";
        }
    }
    
    /**
     * Initialize database collections and indexes
     */
    public static void initializeCollections() {
        if (!isConnected()) {
            System.out.println("MongoDB not connected. Skipping collection initialization.");
            return;
        }
        
        try {
            MongoDatabase db = getDatabase();
            
            // Create collections if they don't exist
            createCollectionIfNotExists(db, "reviews_feedback");
            createCollectionIfNotExists(db, "activity_logs");
            createCollectionIfNotExists(db, "product_views");
            createCollectionIfNotExists(db, "search_logs");
            
            // Create indexes
            createIndexes(db);
            
            System.out.println("MongoDB collections and indexes initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Error initializing MongoDB collections: " + e.getMessage());
        }
    }
    
    /**
     * Create collection if it doesn't exist
     */
    private static void createCollectionIfNotExists(MongoDatabase db, String collectionName) {
        boolean exists = false;
        for (String name : db.listCollectionNames()) {
            if (name.equals(collectionName)) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            db.createCollection(collectionName);
            System.out.println("Created collection: " + collectionName);
        }
    }
    
    /**
     * Create indexes for optimal query performance
     */
    private static void createIndexes(MongoDatabase db) {
        // Reviews indexes
        db.getCollection("reviews_feedback").createIndex(
            com.mongodb.client.model.Indexes.ascending("productId", "created_at")
        );
        db.getCollection("reviews_feedback").createIndex(
            com.mongodb.client.model.Indexes.ascending("userId")
        );
        db.getCollection("reviews_feedback").createIndex(
            com.mongodb.client.model.Indexes.text("reviewText")
        );
        
        // Activity logs indexes
        db.getCollection("activity_logs").createIndex(
            com.mongodb.client.model.Indexes.descending("timestamp")
        );
        db.getCollection("activity_logs").createIndex(
            com.mongodb.client.model.Indexes.ascending("userId", "timestamp")
        );
        db.getCollection("activity_logs").createIndex(
            com.mongodb.client.model.Indexes.ascending("eventType", "timestamp")
        );
        
        System.out.println("Indexes created successfully");
    }
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        System.out.println("Testing MongoDB connection...\n");
        
        // Initialize connection first
        getClient(); // This triggers initializeConnection()

        // Test connection
        if (isConnected()) {
            System.out.println("✓ Connection successful\n");
            System.out.println(getConnectionInfo());
            
            // Initialize collections
            initializeCollections();
        } else {
            System.out.println("✗ Connection failed");
            System.out.println("Please check your internet connection and MongoDB Atlas credentials");
        }
        
        // Close connection
        close();
    }
}

