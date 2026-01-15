package com.smecs.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/smecs";
    private static final String USER = "postgres"; // Placeholder: Modify as per environment
    private static final String PASSWORD = "Bernardxx2003"; // TODO: use environment variables

    // Epic 4: Performance monitoring
    private static final AtomicInteger connectionCount = new AtomicInteger(0);
    private static final AtomicLong totalConnectionTime = new AtomicLong(0);
    private static final List<SlowQuery> slowQueries = new ArrayList<>();
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000; // 1 second
    private static boolean performanceMonitoringEnabled = false;

    /**
     * Slow query record for Epic 4 performance monitoring
     */
    public static class SlowQuery {
        public String query;
        public long executionTimeMs;
        public long timestamp;

        public SlowQuery(String query, long executionTimeMs) {
            this.query = query;
            this.executionTimeMs = executionTimeMs;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("Query: %s\nExecution Time: %d ms\nTimestamp: %d\n",
                    query, executionTimeMs, timestamp);
        }
    }

    /**
     * Connection statistics for Epic 4
     */
    public static class ConnectionStats {
        public int totalConnections;
        public double avgConnectionTimeMs;
        public int slowQueriesCount;

        @Override
        public String toString() {
            return String.format(
                    "Total Connections: %d\n" +
                    "Avg Connection Time: %.3f ms\n" +
                    "Slow Queries Detected: %d\n",
                    totalConnections, avgConnectionTimeMs, slowQueriesCount
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        long startTime = System.nanoTime();
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        long endTime = System.nanoTime();

        // Track connection performance
        if (performanceMonitoringEnabled) {
            connectionCount.incrementAndGet();
            totalConnectionTime.addAndGet(endTime - startTime);
        }

        return conn;
    }

    /**
     * Enable performance monitoring (Epic 4)
     */
    public static void enablePerformanceMonitoring() {
        performanceMonitoringEnabled = true;
    }

    /**
     * Disable performance monitoring (Epic 4)
     */
    public static void disablePerformanceMonitoring() {
        performanceMonitoringEnabled = false;
    }

    /**
     * Record a slow query (Epic 4)
     */
    public static void recordSlowQuery(String query, long executionTimeMs) {
        if (performanceMonitoringEnabled && executionTimeMs > SLOW_QUERY_THRESHOLD_MS) {
            synchronized (slowQueries) {
                slowQueries.add(new SlowQuery(query, executionTimeMs));
                // Keep only the last 100 slow queries
                if (slowQueries.size() > 100) {
                    slowQueries.remove(0);
                }
            }
        }
    }

    /**
     * Get connection statistics (Epic 4)
     */
    public static ConnectionStats getConnectionStats() {
        ConnectionStats stats = new ConnectionStats();
        stats.totalConnections = connectionCount.get();

        if (stats.totalConnections > 0) {
            stats.avgConnectionTimeMs = (totalConnectionTime.get() / (double) stats.totalConnections) / 1_000_000.0;
        }

        synchronized (slowQueries) {
            stats.slowQueriesCount = slowQueries.size();
        }

        return stats;
    }

    /**
     * Get list of slow queries (Epic 4)
     */
    public static List<SlowQuery> getSlowQueries() {
        synchronized (slowQueries) {
            return new ArrayList<>(slowQueries);
        }
    }

    /**
     * Reset performance statistics (Epic 4)
     */
    public static void resetStats() {
        connectionCount.set(0);
        totalConnectionTime.set(0);
        synchronized (slowQueries) {
            slowQueries.clear();
        }
    }
}
