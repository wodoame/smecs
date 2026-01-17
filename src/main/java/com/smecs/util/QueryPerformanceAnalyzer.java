package com.smecs.util;

import java.sql.*;
import java.util.*;
import com.smecs.util.DatabaseConnection;

/**
 * Query Performance Analyzer for Epic 4.
 * Measures and compares SQL query execution times before and after optimization.
 */
public class QueryPerformanceAnalyzer {
    
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TEST_ITERATIONS = 10;
    
    /**
     * Result of a query performance test
     */
    public static class QueryPerformanceResult {
        public String queryName;
        public String query;
        public double avgExecutionTimeMs;
        public double minExecutionTimeMs;
        public double maxExecutionTimeMs;
        public int rowsReturned;
        public boolean usesIndex;
        public String executionPlan;
        
        public QueryPerformanceResult(String queryName, String query) {
            this.queryName = queryName;
            this.query = query;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Query: %s\n" +
                "   Avg Time: %.3f ms\n" +
                "   Min Time: %.3f ms\n" +
                "   Max Time: %.3f ms\n" +
                "   Rows: %d\n" +
                "   Uses Index: %s\n",
                queryName, avgExecutionTimeMs, minExecutionTimeMs, 
                maxExecutionTimeMs, rowsReturned, usesIndex ? "Yes" : "No"
            );
        }
    }
    
    /**
     * Comparison result between two queries
     */
    public static class ComparisonResult {
        public QueryPerformanceResult baseline;
        public QueryPerformanceResult optimized;
        public double speedupFactor;
        public double improvementPercent;
        
        @Override
        public String toString() {
            return String.format(
                "=== COMPARISON: %s ===\n" +
                "Baseline:  %.3f ms (avg)\n" +
                "Optimized: %.3f ms (avg)\n" +
                "Speedup:   %.2fx faster\n" +
                "Improvement: %.1f%%\n",
                baseline.queryName,
                baseline.avgExecutionTimeMs,
                optimized.avgExecutionTimeMs,
                speedupFactor,
                improvementPercent
            );
        }
    }
    
    /**
     * Execute a query multiple times and measure performance
     */
    public QueryPerformanceResult analyzeQuery(String queryName, String query) {
        QueryPerformanceResult result = new QueryPerformanceResult(queryName, query);
        List<Double> executionTimes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Warmup iterations
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                executeQuery(conn, query);
            }
            
            // Actual test iterations
            for (int i = 0; i < TEST_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                int rowCount = executeQuery(conn, query);
                long endTime = System.nanoTime();
                
                double executionTimeMs = (endTime - startTime) / 1_000_000.0;
                executionTimes.add(executionTimeMs);
                result.rowsReturned = rowCount;
            }
            
            // Calculate statistics
            result.avgExecutionTimeMs = executionTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            
            result.minExecutionTimeMs = executionTimes.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0);
            
            result.maxExecutionTimeMs = executionTimes.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
            
            // Check if query uses indexes
            result.usesIndex = checkIndexUsage(conn, query);
            
            // Get execution plan
            result.executionPlan = getExecutionPlan(conn, query);
            
        } catch (SQLException e) {
            System.err.println("Error analyzing query: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Execute a query and return row count
     */
    private int executeQuery(Connection conn, String query) throws SQLException {
        int rowCount = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rowCount++;
            }
        }
        return rowCount;
    }
    
    /**
     * Check if query uses indexes by analyzing execution plan
     */
    private boolean checkIndexUsage(Connection conn, String query) {
        try {
            String explainQuery = "EXPLAIN " + query;
            try (PreparedStatement pstmt = conn.prepareStatement(explainQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    String plan = rs.getString(1).toLowerCase();
                    if (plan.contains("index scan") || 
                        plan.contains("index seek") ||
                        plan.contains("using index")) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            // EXPLAIN might not be supported or query might be invalid
            return false;
        }
        return false;
    }
    
    /**
     * Get query execution plan
     */
    private String getExecutionPlan(Connection conn, String query) {
        StringBuilder plan = new StringBuilder();
        try {
            String explainQuery = "EXPLAIN " + query;
            try (PreparedStatement pstmt = conn.prepareStatement(explainQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    plan.append(rs.getString(1)).append("\n");
                }
            }
        } catch (SQLException e) {
            plan.append("Unable to retrieve execution plan: ").append(e.getMessage());
        }
        return plan.toString();
    }
    
    /**
     * Compare two queries (baseline vs optimized)
     */
    public ComparisonResult compareQueries(String queryName, String baselineQuery, String optimizedQuery) {
        ComparisonResult comparison = new ComparisonResult();
        
        System.out.println("Analyzing baseline query: " + queryName);
        comparison.baseline = analyzeQuery(queryName + " (Baseline)", baselineQuery);
        
        System.out.println("Analyzing optimized query: " + queryName);
        comparison.optimized = analyzeQuery(queryName + " (Optimized)", optimizedQuery);
        
        // Calculate improvement metrics
        if (comparison.optimized.avgExecutionTimeMs > 0) {
            comparison.speedupFactor = comparison.baseline.avgExecutionTimeMs / 
                                      comparison.optimized.avgExecutionTimeMs;
            comparison.improvementPercent = 
                ((comparison.baseline.avgExecutionTimeMs - comparison.optimized.avgExecutionTimeMs) / 
                 comparison.baseline.avgExecutionTimeMs) * 100;
        }
        
        return comparison;
    }
    
    /**
     * Run comprehensive performance analysis
     */
    public String runComprehensiveAnalysis() {
        StringBuilder report = new StringBuilder();
        report.append("=== EPIC 4: Query Performance Analysis Report ===\n\n");
        report.append("Test Configuration:\n");
        report.append(String.format("  Warmup Iterations: %d\n", WARMUP_ITERATIONS));
        report.append(String.format("  Test Iterations: %d\n\n", TEST_ITERATIONS));
        
        List<ComparisonResult> comparisons = new ArrayList<>();
        
        // Test 1: Product search optimization
        report.append("1. PRODUCT SEARCH QUERY OPTIMIZATION\n");
        report.append("-".repeat(60)).append("\n");
        
        String searchBaseline = 
            "SELECT p.product_id, p.product_name, p.price, c.category_name " +
            "FROM Products p " +
            "LEFT JOIN Categories c ON p.category_id = c.category_id " +
            "WHERE LOWER(p.product_name) LIKE '%laptop%' " +
            "OR LOWER(p.description) LIKE '%laptop%'";
        
        String searchOptimized = 
            "SELECT p.product_id, p.product_name, p.price, c.category_name " +
            "FROM Products p " +
            "LEFT JOIN Categories c ON p.category_id = c.category_id " +
            "WHERE LOWER(p.product_name) LIKE '%laptop%'";
        
        ComparisonResult search = compareQueries("Product Search", searchBaseline, searchOptimized);
        comparisons.add(search);
        report.append(search.toString()).append("\n");
        
        // Test 2: Product with inventory optimization
        report.append("2. PRODUCT INVENTORY QUERY OPTIMIZATION\n");
        report.append("-".repeat(60)).append("\n");
        
        String inventoryBaseline = 
            "SELECT p.product_id, p.product_name, p.price, " +
            "(SELECT quantity FROM Inventory WHERE product_id = p.product_id) as stock " +
            "FROM Products p " +
            "WHERE p.product_id IN (SELECT product_id FROM Inventory WHERE quantity > 0) " +
            "ORDER BY p.price DESC";
        
        String inventoryOptimized = 
            "SELECT p.product_id, p.product_name, p.price, i.quantity as stock " +
            "FROM Products p " +
            "INNER JOIN Inventory i ON p.product_id = i.product_id " +
            "WHERE i.quantity > 0 " +
            "ORDER BY p.price DESC";
        
        ComparisonResult inventory = compareQueries("Inventory Query", inventoryBaseline, inventoryOptimized);
        comparisons.add(inventory);
        report.append(inventory.toString()).append("\n");
        
        // Test 3: Category aggregation optimization
        report.append("3. CATEGORY AGGREGATION OPTIMIZATION\n");
        report.append("-".repeat(60)).append("\n");
        
        String aggBaseline = 
            "SELECT c.category_id, c.category_name, " +
            "(SELECT COUNT(*) FROM Products p WHERE p.category_id = c.category_id) as product_count " +
            "FROM Categories c " +
            "ORDER BY product_count DESC";
        
        String aggOptimized = 
            "SELECT c.category_id, c.category_name, COUNT(p.product_id) as product_count " +
            "FROM Categories c " +
            "LEFT JOIN Products p ON c.category_id = p.category_id " +
            "GROUP BY c.category_id, c.category_name " +
            "ORDER BY product_count DESC";
        
        ComparisonResult aggregation = compareQueries("Category Aggregation", aggBaseline, aggOptimized);
        comparisons.add(aggregation);
        report.append(aggregation.toString()).append("\n");
        
        // Summary
        report.append("=".repeat(60)).append("\n");
        report.append("SUMMARY\n");
        report.append("=".repeat(60)).append("\n");
        
        double avgSpeedup = comparisons.stream()
            .mapToDouble(c -> c.speedupFactor)
            .average()
            .orElse(0.0);
        
        double avgImprovement = comparisons.stream()
            .mapToDouble(c -> c.improvementPercent)
            .average()
            .orElse(0.0);
        
        report.append(String.format("Total queries tested: %d\n", comparisons.size()));
        report.append(String.format("Average speedup: %.2fx\n", avgSpeedup));
        report.append(String.format("Average improvement: %.1f%%\n", avgImprovement));
        
        long queriesWithIndexes = comparisons.stream()
            .filter(c -> c.optimized.usesIndex)
            .count();
        
        report.append(String.format("Queries using indexes: %d/%d\n", queriesWithIndexes, comparisons.size()));
        
        return report.toString();
    }
    
    /**
     * Analyze database connection performance
     */
    public String analyzeConnectionPerformance() {
        StringBuilder report = new StringBuilder();
        report.append("=== DATABASE CONNECTION PERFORMANCE ===\n\n");
        
        List<Long> connectionTimes = new ArrayList<>();
        
        // Test connection establishment time
        for (int i = 0; i < 10; i++) {
            long startTime = System.nanoTime();
            try (Connection conn = DatabaseConnection.getConnection()) {
                long endTime = System.nanoTime();
                connectionTimes.add(endTime - startTime);
            } catch (SQLException e) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }
        
        double avgConnectionTime = connectionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0) / 1_000_000.0;
        
        double minConnectionTime = connectionTimes.stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0L) / 1_000_000.0;
        
        double maxConnectionTime = connectionTimes.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L) / 1_000_000.0;
        
        report.append(String.format("Connection establishment time:\n"));
        report.append(String.format("  Average: %.3f ms\n", avgConnectionTime));
        report.append(String.format("  Min: %.3f ms\n", minConnectionTime));
        report.append(String.format("  Max: %.3f ms\n", maxConnectionTime));
        report.append(String.format("  Test iterations: %d\n", connectionTimes.size()));
        
        return report.toString();
    }
    
    /**
     * Export results to CSV format
     */
    public String exportToCSV(List<ComparisonResult> comparisons) {
        StringBuilder csv = new StringBuilder();
        csv.append("Query Name,Baseline Avg (ms),Optimized Avg (ms),Speedup Factor,Improvement %,Uses Index\n");
        
        for (ComparisonResult comp : comparisons) {
            csv.append(String.format("%s,%.3f,%.3f,%.2f,%.1f,%s\n",
                comp.baseline.queryName,
                comp.baseline.avgExecutionTimeMs,
                comp.optimized.avgExecutionTimeMs,
                comp.speedupFactor,
                comp.improvementPercent,
                comp.optimized.usesIndex ? "Yes" : "No"
            ));
        }
        
        return csv.toString();
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        System.out.println("Starting Query Performance Analysis...\n");
        
        QueryPerformanceAnalyzer analyzer = new QueryPerformanceAnalyzer();
        
        // Run comprehensive analysis
        String report = analyzer.runComprehensiveAnalysis();
        System.out.println(report);
        
        // Analyze connection performance
        String connReport = analyzer.analyzeConnectionPerformance();
        System.out.println(connReport);
    }
}

