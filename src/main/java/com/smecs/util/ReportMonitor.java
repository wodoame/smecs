//package com.smecs.util;
//
//import com.smecs.cache.ProductCache;
//import com.smecs.service.ProductService;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * Intelligent report monitoring system for Epic 4.
// * Monitors system performance metrics and triggers reports based on:
// * - Periodic intervals
// * - Performance degradation
// * - Cache hit rate changes
// * - Database query performance
// * Configuration can be customized via config/report_config.properties
// */
//public class ReportMonitor {
//
//    private static ReportMonitor instance;
//    private final PerformanceReportGenerator reportGenerator;
//    private final ProductService productService;
//    private final Timer monitoringTimer;
//    private final AtomicLong lastReportTime;
//    private final ReportConfig config;
//
//    // Degradation tracking
//    private double lastCacheHitRate = 100.0;
//    private boolean cacheHitRateAlertActive = false;
//    private long consecutiveDegradationChecks = 0;
//    private static final long DEGRADATION_THRESHOLD = 3;  // Trigger after 3 consecutive checks
//
//    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    private ReportMonitor() {
//        this.config = ReportConfig.getInstance();
//        this.reportGenerator = new PerformanceReportGenerator();
//        this.productService = new ProductService();
//        this.monitoringTimer = new Timer("ReportMonitor-Thread", true);  // Daemon thread
//        this.lastReportTime = new AtomicLong(System.currentTimeMillis());
//    }
//
//    /**
//     * Get singleton instance
//     */
//    public static synchronized ReportMonitor getInstance() {
//        if (instance == null) {
//            instance = new ReportMonitor();
//        }
//        return instance;
//    }
//
//    /**
//     * Start monitoring system performance
//     */
//    public synchronized void startMonitoring() {
//        long monitorIntervalMs = config.getMonitorIntervalSeconds() * 1000;
//
//        System.out.println("[ReportMonitor] Starting intelligent performance monitoring");
//        System.out.println("[ReportMonitor] Monitor interval: " + config.getMonitorIntervalSeconds() + " seconds");
//        System.out.println("[ReportMonitor] Cache hit rate threshold: " + config.getCacheHitRateThreshold() + "%");
//
//        monitoringTimer.scheduleAtFixedRate(new PerformanceMonitoringTask(), monitorIntervalMs, monitorIntervalMs);
//    }
//
//    /**
//     * Stop monitoring
//     */
//    public synchronized void stopMonitoring() {
//        monitoringTimer.cancel();
//        System.out.println("[ReportMonitor] Performance monitoring stopped");
//    }
//
//    /**
//     * Inner class for the monitoring task
//     */
//    private class PerformanceMonitoringTask extends TimerTask {
//
//        @Override
//        public void run() {
//            try {
//                // Get current cache statistics
//                ProductCache.CacheStats cacheStats = productService.getCacheStats();
//                double currentCacheHitRate = cacheStats.getHitRate();
//
//                // Check for cache hit rate degradation
//                if (currentCacheHitRate < config.getCacheHitRateThreshold()) {
//                    if (!cacheHitRateAlertActive) {
//                        System.out.println("[ReportMonitor] ⚠ WARNING: Cache hit rate degraded to " +
//                            String.format("%.1f%%", currentCacheHitRate));
//                        cacheHitRateAlertActive = true;
//                        consecutiveDegradationChecks = 1;
//                    } else {
//                        consecutiveDegradationChecks++;
//                    }
//
//                    // Trigger report if degradation persists
//                    if (consecutiveDegradationChecks >= DEGRADATION_THRESHOLD) {
//                        triggerPerformanceDegradationReport(currentCacheHitRate);
//                        consecutiveDegradationChecks = 0;
//                    }
//                } else if (currentCacheHitRate >= config.getCacheRecoveryThreshold() && cacheHitRateAlertActive) {
//                    // Cache performance recovered
//                    System.out.println("[ReportMonitor] ✓ Cache performance recovered to " +
//                        String.format("%.1f%%", currentCacheHitRate));
//                    cacheHitRateAlertActive = false;
//                    consecutiveDegradationChecks = 0;
//                }
//
//                lastCacheHitRate = currentCacheHitRate;
//
//            } catch (Exception e) {
//                System.err.println("[ReportMonitor] Error during monitoring: " + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Trigger report due to performance degradation
//     */
//    private synchronized void triggerPerformanceDegradationReport(double currentCacheHitRate) {
//        long currentTime = System.currentTimeMillis();
//        long timeSinceLastReport = currentTime - lastReportTime.get();
//        long minReportIntervalMs = config.getMinReportIntervalSeconds() * 1000;
//
//        // Avoid generating reports too frequently
//        if (timeSinceLastReport < minReportIntervalMs) {
//            System.out.println("[ReportMonitor] Skipping report (generated " +
//                (timeSinceLastReport / 1000) + " seconds ago)");
//            return;
//        }
//
//        String timestamp = LocalDateTime.now().format(FORMATTER);
//        System.out.println("\n" + "=".repeat(70));
//        System.out.println("[ReportMonitor] ALERT: Generating performance degradation report");
//        System.out.println("[ReportMonitor] Timestamp: " + timestamp);
//        System.out.println("[ReportMonitor] Cache hit rate: " + String.format("%.1f%%", currentCacheHitRate));
//        System.out.println("=".repeat(70));
//
//        try {
//            reportGenerator.generateAllReports();
//            lastReportTime.set(currentTime);
//            System.out.println("[ReportMonitor] Degradation report generated successfully");
//            System.out.println("=".repeat(70) + "\n");
//        } catch (Exception e) {
//            System.err.println("[ReportMonitor] Error generating degradation report: " + e.getMessage());
////            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Manually trigger report generation (for API or admin requests)
//     */
//    public void triggerManualReport(String reason) {
//        String timestamp = LocalDateTime.now().format(FORMATTER);
//        System.out.println("\n" + "=".repeat(70));
//        System.out.println("[ReportMonitor] Manual report generation requested");
//        System.out.println("[ReportMonitor] Reason: " + reason);
//        System.out.println("[ReportMonitor] Timestamp: " + timestamp);
//        System.out.println("=".repeat(70));
//
//        try {
//            reportGenerator.generateAllReports();
//            lastReportTime.set(System.currentTimeMillis());
//            System.out.println("[ReportMonitor] Manual report generated successfully");
//            System.out.println("=".repeat(70) + "\n");
//        } catch (Exception e) {
//            System.err.println("[ReportMonitor] Error generating manual report: " + e.getMessage());
////            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Get current monitoring statistics
//     */
//    public String getMonitoringStatus() {
//        StringBuilder status = new StringBuilder();
//        status.append("=== Report Monitor Status ===\n");
//        status.append("Last Cache Hit Rate: ").append(String.format("%.1f%%", lastCacheHitRate)).append("\n");
//        status.append("Cache Alert Active: ").append(cacheHitRateAlertActive ? "YES" : "NO").append("\n");
//        status.append("Consecutive Degradations: ").append(consecutiveDegradationChecks).append("\n");
//        status.append("Time since last report: ").append((System.currentTimeMillis() - lastReportTime.get()) / 1000).append("s\n");
//        return status.toString();
//    }
//
//    /**
//     * Shutdown the monitor gracefully
//     */
//    public synchronized void shutdown() {
//        stopMonitoring();
//        instance = null;
//    }
//}

