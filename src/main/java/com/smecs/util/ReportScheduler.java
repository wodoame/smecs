//package com.smecs.util;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * Smart automatic report generation scheduler for Epic 4.
// * Generates performance reports at configurable intervals without user interaction.
// * Configuration can be customized via config/report_config.properties
// */
//public class ReportScheduler {
//
//    private static ReportScheduler instance;
//    private final PerformanceReportGenerator reportGenerator;
//    private final Timer timer;
//    private final AtomicBoolean isScheduled;
//    private final ReportConfig config;
//
//    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    private ReportScheduler() {
//        this.config = ReportConfig.getInstance();
//        this.reportGenerator = new PerformanceReportGenerator();
//        this.timer = new Timer("ReportScheduler-Thread", true);  // Daemon thread
//        this.isScheduled = new AtomicBoolean(false);
//
//        if (config.isSchedulerEnabled()) {
//            scheduleReports();
//        }
//    }
//
//    /**
//     * Get singleton instance
//     */
//    public static synchronized ReportScheduler getInstance() {
//        if (instance == null) {
//            instance = new ReportScheduler();
//        }
//        return instance;
//    }
//
//    /**
//     * Schedule automatic report generation
//     */
//    public synchronized void scheduleReports() {
//        if (isScheduled.getAndSet(true)) {
//            System.out.println("[ReportScheduler] Reports already scheduled");
//            return;
//        }
//
//        long initialDelayMs = config.getSchedulerInitialDelaySeconds() * 1000;
//        long intervalMs = config.getSchedulerIntervalSeconds() * 1000;
//
//        System.out.println("[ReportScheduler] Starting automatic report generation scheduler");
//        System.out.println("[ReportScheduler] Initial delay: " + config.getSchedulerInitialDelaySeconds() + " seconds");
//        System.out.println("[ReportScheduler] Report interval: " + config.getSchedulerIntervalSeconds() + " seconds");
//
//        timer.scheduleAtFixedRate(new ReportGenerationTask(), initialDelayMs, intervalMs);
//    }
//
//    /**
//     * Stop automatic report generation
//     */
//    public synchronized void stopScheduling() {
//        if (!isScheduled.getAndSet(false)) {
//            System.out.println("[ReportScheduler] Reports not currently scheduled");
//            return;
//        }
//
//        timer.cancel();
//        System.out.println("[ReportScheduler] Report generation scheduler stopped");
//    }
//
//    /**
//     * Check if reports are currently being scheduled
//     */
//    public boolean isScheduling() {
//        return isScheduled.get();
//    }
//
//    /**
//     * Generate reports immediately (on-demand)
//     */
//    public void generateReportsNow() {
//        new ReportGenerationTask().run();
//    }
//
//    /**
//     * Inner class for the scheduled task
//     */
//    private class ReportGenerationTask extends TimerTask {
//
//        @Override
//        public void run() {
//            try {
//                String timestamp = LocalDateTime.now().format(FORMATTER);
//                System.out.println("\n" + "=".repeat(70));
//                System.out.println("[ReportScheduler] Generating performance reports at " + timestamp);
//                System.out.println("=".repeat(70));
//
//                // Generate all report formats
//                reportGenerator.generateAllReports();
//
//                System.out.println("[ReportScheduler] Report generation completed successfully");
//                System.out.println("[ReportScheduler] Next report scheduled for " + getNextScheduledTime());
//                System.out.println("=".repeat(70) + "\n");
//
//            } catch (Exception e) {
//                System.err.println("[ReportScheduler] Error generating reports: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * Calculate the next scheduled report time (for informational purposes)
//     */
//    private String getNextScheduledTime() {
//        long intervalSeconds = config.getSchedulerIntervalSeconds();
//        LocalDateTime nextTime = LocalDateTime.now().plusSeconds(intervalSeconds);
//        return nextTime.format(FORMATTER);
//    }
//
//    /**
//     * Shutdown the scheduler gracefully
//     */
//    public synchronized void shutdown() {
//        stopScheduling();
//        instance = null;
//    }
//}

