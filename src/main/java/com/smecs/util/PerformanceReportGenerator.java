package com.smecs.util;

import com.smecs.cache.ProductCache;
import com.smecs.service.ProductService;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Performance Report Generator for Epic 4.
 * Generates comprehensive performance reports in multiple formats.
 */
public class PerformanceReportGenerator {
    
    private final PerformanceBenchmark benchmark;
    private final QueryPerformanceAnalyzer queryAnalyzer;
    private final ProductService productService;
    
    public PerformanceReportGenerator() {
        this.benchmark = new PerformanceBenchmark();
        this.queryAnalyzer = new QueryPerformanceAnalyzer();
        this.productService = new ProductService();
    }
    
    /**
     * Generate comprehensive performance report
     */
    public String generateComprehensiveReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=".repeat(70)).append("\n");
        report.append("       SMART E-COMMERCE SYSTEM (SMECS)\n");
        report.append("     EPIC 4: PERFORMANCE OPTIMIZATION REPORT\n");
        report.append("=".repeat(70)).append("\n\n");
        
        // Report metadata
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        report.append("Report Generated: ").append(LocalDateTime.now().format(formatter)).append("\n");
        report.append("Report Type: Comprehensive Performance Analysis\n\n");
        
        // Executive Summary
        report.append(generateExecutiveSummary());
        
        // Section 1: Cache Performance Analysis
        report.append("\n").append("=".repeat(70)).append("\n");
        report.append("SECTION 1: CACHE PERFORMANCE ANALYSIS\n");
        report.append("=".repeat(70)).append("\n\n");
        report.append(benchmark.runAllBenchmarks());
        
        // Section 2: Query Optimization Results
        report.append("\n").append("=".repeat(70)).append("\n");
        report.append("SECTION 2: QUERY OPTIMIZATION RESULTS\n");
        report.append("=".repeat(70)).append("\n\n");
        report.append(queryAnalyzer.runComprehensiveAnalysis());
        
        // Section 3: Database Connection Performance
        report.append("\n").append("=".repeat(70)).append("\n");
        report.append("SECTION 3: DATABASE CONNECTION PERFORMANCE\n");
        report.append("=".repeat(70)).append("\n\n");
        report.append(queryAnalyzer.analyzeConnectionPerformance());
        
        // Section 4: Overall System Performance
        report.append("\n").append("=".repeat(70)).append("\n");
        report.append("SECTION 4: OVERALL SYSTEM PERFORMANCE\n");
        report.append("=".repeat(70)).append("\n\n");
        report.append(generateSystemPerformanceMetrics());
        
        // Section 5: Recommendations
        report.append("\n").append("=".repeat(70)).append("\n");
        report.append("SECTION 5: OPTIMIZATION RECOMMENDATIONS\n");
        report.append("=".repeat(70)).append("\n\n");
        report.append(generateRecommendations());
        
        // Footer
        report.append("\n").append("=".repeat(70)).append("\n");
        report.append("                    END OF REPORT\n");
        report.append("=".repeat(70)).append("\n");
        
        return report.toString();
    }
    
    /**
     * Generate executive summary
     */
    private String generateExecutiveSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("EXECUTIVE SUMMARY\n");
        summary.append("-".repeat(70)).append("\n\n");
        
        summary.append("This report presents a comprehensive analysis of the performance\n");
        summary.append("optimizations implemented in the Smart E-Commerce System (SMECS).\n\n");
        
        summary.append("Key Optimization Techniques Applied:\n");
        summary.append("  ✓ In-memory caching for frequently accessed data\n");
        summary.append("  ✓ Database indexing on high-frequency query columns\n");
        summary.append("  ✓ Query optimization through JOIN and subquery improvements\n");
        summary.append("  ✓ Hash-based search algorithms for fast lookups\n");
        summary.append("  ✓ Efficient sorting algorithms (QuickSort, MergeSort, TimSort)\n\n");
        
        summary.append("Performance Metrics Analyzed:\n");
        summary.append("  • Cache hit rates and speedup factors\n");
        summary.append("  • Search query response times (cold vs. warm)\n");
        summary.append("  • Sorting algorithm performance comparisons\n");
        summary.append("  • Database query execution times\n");
        summary.append("  • Connection pool performance\n\n");
        
        return summary.toString();
    }
    
    /**
     * Generate system performance metrics
     */
    private String generateSystemPerformanceMetrics() {
        StringBuilder metrics = new StringBuilder();
        
        // Get cache statistics
        ProductCache.CacheStats cacheStats = productService.getCacheStats();
        
        metrics.append("System-Wide Performance Metrics:\n\n");
        
        metrics.append("1. Cache Efficiency:\n");
        metrics.append(String.format("   Total Requests: %d\n", cacheStats.hits + cacheStats.misses));
        metrics.append(String.format("   Cache Hits: %d\n", cacheStats.hits));
        metrics.append(String.format("   Cache Misses: %d\n", cacheStats.misses));
        metrics.append(String.format("   Hit Rate: %.1f%%\n", cacheStats.getHitRate()));
        
        if (cacheStats.getHitRate() >= 80) {
            metrics.append("   Status: ✓ EXCELLENT (>80%)\n\n");
        } else if (cacheStats.getHitRate() >= 60) {
            metrics.append("   Status: ⚠ GOOD (60-80%)\n\n");
        } else {
            metrics.append("   Status: ✗ NEEDS IMPROVEMENT (<60%)\n\n");
        }
        
        metrics.append("2. Data Volume:\n");
        metrics.append(String.format("   Products Cached: %d\n", cacheStats.productsCached));
        metrics.append(String.format("   Search Queries Cached: %d\n\n", cacheStats.searchQueriesCached));
        
        metrics.append("3. Search Index Performance:\n");
        ProductSearcher.IndexStats indexStats = productService.getSearchIndexStats();
        metrics.append(String.format("   Name Index Size: %d\n", indexStats.nameIndexSize));
        metrics.append(String.format("   Category Index Size: %d\n", indexStats.categoryIndexSize));
        metrics.append(String.format("   Exact Name Index Size: %d\n", indexStats.exactNameIndexSize));
        metrics.append("\n");
        
        return metrics.toString();
    }
    
    /**
     * Generate optimization recommendations
     */
    private String generateRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        
        ProductCache.CacheStats cacheStats = productService.getCacheStats();
        double hitRate = cacheStats.getHitRate();
        
        recommendations.append("Based on the performance analysis, the following recommendations\n");
        recommendations.append("are provided to further optimize system performance:\n\n");
        
        recommendations.append("1. CACHING STRATEGY\n");
        if (hitRate < 60) {
            recommendations.append("   ⚠ Current hit rate is low (").append(String.format("%.1f%%", hitRate)).append(")\n");
            recommendations.append("   → Increase cache size or TTL (Time To Live)\n");
            recommendations.append("   → Implement cache warming on application startup\n");
            recommendations.append("   → Consider using distributed caching (Redis)\n\n");
        } else if (hitRate < 80) {
            recommendations.append("   ✓ Cache performing adequately (").append(String.format("%.1f%%", hitRate)).append(")\n");
            recommendations.append("   → Fine-tune cache eviction policies\n");
            recommendations.append("   → Monitor cache memory usage\n\n");
        } else {
            recommendations.append("   ✓ Cache performing excellently (").append(String.format("%.1f%%", hitRate)).append(")\n");
            recommendations.append("   → Maintain current cache configuration\n");
            recommendations.append("   → Consider caching additional data types\n\n");
        }
        
        recommendations.append("2. DATABASE OPTIMIZATION\n");
        recommendations.append("   → Continue monitoring slow queries (>1 second)\n");
        recommendations.append("   → Run ANALYZE on tables weekly to update statistics\n");
        recommendations.append("   → Consider partitioning large tables (>1M rows)\n");
        recommendations.append("   → Implement connection pooling for better throughput\n\n");
        
        recommendations.append("3. SEARCH OPTIMIZATION\n");
        recommendations.append("   → Implement full-text search for better text matching\n");
        recommendations.append("   → Consider Elasticsearch for advanced search features\n");
        recommendations.append("   → Add search autocomplete using Trie data structure\n");
        recommendations.append("   → Implement search result caching with shorter TTL\n\n");
        
        recommendations.append("4. SCALABILITY CONSIDERATIONS\n");
        recommendations.append("   → Implement database read replicas for read-heavy workloads\n");
        recommendations.append("   → Consider NoSQL for unstructured data (logs, reviews)\n");
        recommendations.append("   → Implement API rate limiting to prevent abuse\n");
        recommendations.append("   → Add monitoring and alerting (Prometheus/Grafana)\n\n");
        
        recommendations.append("5. CODE-LEVEL OPTIMIZATIONS\n");
        recommendations.append("   → Use prepared statement pooling\n");
        recommendations.append("   → Implement lazy loading for heavy objects\n");
        recommendations.append("   → Add pagination for large result sets\n");
        recommendations.append("   → Profile memory usage and optimize object allocation\n\n");
        
        return recommendations.toString();
    }
    
    /**
     * Generate HTML report
     */
    public String generateHTMLReport() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>SMECS Performance Report</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 40px; background: #f5f5f5; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append("        h2 { color: #34495e; margin-top: 30px; border-left: 4px solid #3498db; padding-left: 10px; }\n");
        html.append("        .metric { background: #ecf0f1; padding: 15px; margin: 10px 0; border-radius: 5px; }\n");
        html.append("        .metric-value { font-size: 24px; font-weight: bold; color: #2980b9; }\n");
        html.append("        .metric-label { color: #7f8c8d; font-size: 14px; }\n");
        html.append("        .status-good { color: #27ae60; font-weight: bold; }\n");
        html.append("        .status-warning { color: #f39c12; font-weight: bold; }\n");
        html.append("        .status-error { color: #e74c3c; font-weight: bold; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background-color: #3498db; color: white; }\n");
        html.append("        tr:hover { background-color: #f5f5f5; }\n");
        html.append("        .timestamp { color: #95a5a6; font-size: 12px; }\n");
        html.append("        pre { background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px; overflow-x: auto; }\n");
        html.append("    </style>\n");
        html.append("</head>\n<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>Smart E-Commerce System (SMECS)</h1>\n");
        html.append("        <h2>Epic 4: Performance Optimization Report</h2>\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        html.append("        <p class=\"timestamp\">Generated: ").append(LocalDateTime.now().format(formatter)).append("</p>\n");
        
        // Cache metrics
        ProductCache.CacheStats cacheStats = productService.getCacheStats();
        html.append("        <h2>Cache Performance Metrics</h2>\n");
        html.append("        <div class=\"metric\">\n");
        html.append("            <div class=\"metric-label\">Cache Hit Rate</div>\n");
        html.append("            <div class=\"metric-value\">").append(String.format("%.1f%%", cacheStats.getHitRate())).append("</div>\n");
        html.append("        </div>\n");
        
        html.append("        <table>\n");
        html.append("            <tr><th>Metric</th><th>Value</th></tr>\n");
        html.append("            <tr><td>Cache Hits</td><td>").append(cacheStats.hits).append("</td></tr>\n");
        html.append("            <tr><td>Cache Misses</td><td>").append(cacheStats.misses).append("</td></tr>\n");
        html.append("            <tr><td>Products Cached</td><td>").append(cacheStats.productsCached).append("</td></tr>\n");
        html.append("            <tr><td>Search Queries Cached</td><td>").append(cacheStats.searchQueriesCached).append("</td></tr>\n");
        html.append("        </table>\n");
        
        // Recommendations
        html.append("        <h2>Optimization Status</h2>\n");
        if (cacheStats.getHitRate() >= 80) {
            html.append("        <p class=\"status-good\">✓ System performing excellently</p>\n");
        } else if (cacheStats.getHitRate() >= 60) {
            html.append("        <p class=\"status-warning\">⚠ System performing adequately</p>\n");
        } else {
            html.append("        <p class=\"status-error\">✗ System needs optimization</p>\n");
        }
        
        html.append("        <h2>Detailed Report</h2>\n");
        html.append("        <pre>").append(generateComprehensiveReport().replace("<", "&lt;").replace(">", "&gt;")).append("</pre>\n");
        
        html.append("    </div>\n");
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    /**
     * Generate CSV report for Excel analysis
     */
    public String generateCSVReport() {
        StringBuilder csv = new StringBuilder();
        
        csv.append("Metric Category,Metric Name,Value,Unit\n");
        
        // Cache metrics
        ProductCache.CacheStats cacheStats = productService.getCacheStats();
        csv.append("Cache,Hit Rate,").append(String.format("%.2f", cacheStats.getHitRate())).append(",%\n");
        csv.append("Cache,Total Hits,").append(cacheStats.hits).append(",count\n");
        csv.append("Cache,Total Misses,").append(cacheStats.misses).append(",count\n");
        csv.append("Cache,Products Cached,").append(cacheStats.productsCached).append(",count\n");
        csv.append("Cache,Search Queries Cached,").append(cacheStats.searchQueriesCached).append(",count\n");
        
        // Search metrics
        ProductSearcher.IndexStats indexStats = productService.getSearchIndexStats();
        csv.append("Search,Name Index Size,").append(indexStats.nameIndexSize).append(",count\n");
        csv.append("Search,Category Index Size,").append(indexStats.categoryIndexSize).append(",count\n");
        csv.append("Search,Exact Name Index Size,").append(indexStats.exactNameIndexSize).append(",count\n");

        return csv.toString();
    }
    
    /**
     * Save report to file
     */
    public void saveReportToFile(String content, String filename) {
        try {
            // Create reports directory if it doesn't exist
            Files.createDirectories(Paths.get("reports"));
            
            String filepath = "reports/" + filename;
            try (FileWriter writer = new FileWriter(filepath)) {
                writer.write(content);
            }
            System.out.println("Report saved to: " + filepath);
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }
    
    /**
     * Generate all report formats and save them
     */
    public void generateAllReports() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        
        System.out.println("Generating comprehensive performance reports...\n");
        
        // Text report
        System.out.println("1. Generating text report...");
        String textReport = generateComprehensiveReport();
        saveReportToFile(textReport, "performance_report_" + timestamp + ".txt");
        
        // HTML report
        System.out.println("2. Generating HTML report...");
        String htmlReport = generateHTMLReport();
        saveReportToFile(htmlReport, "performance_report_" + timestamp + ".html");
        
        // CSV report
        System.out.println("3. Generating CSV report...");
        String csvReport = generateCSVReport();
        saveReportToFile(csvReport, "performance_metrics_" + timestamp + ".csv");
        
        System.out.println("\n✓ All reports generated successfully!");
        System.out.println("Reports saved in ./reports/ directory");
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        System.out.println("=== SMECS Performance Report Generator ===\n");
        
        PerformanceReportGenerator generator = new PerformanceReportGenerator();
        
        // Generate and display text report
        String report = generator.generateComprehensiveReport();
        System.out.println(report);
        
        // Generate all report formats
        generator.generateAllReports();
    }
}

