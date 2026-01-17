package com.smecs.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Report generation configuration manager.
 * Allows customization of report scheduling and monitoring parameters.
 * Configuration file: ./config/report_config.properties
 */
public class ReportConfig {
    
    private static ReportConfig instance;
    private final Properties properties;
    private static final String CONFIG_FILE = "config/report_config.properties";
    
    private ReportConfig() {
        this.properties = new Properties();
        loadOrCreateConfig();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ReportConfig getInstance() {
        if (instance == null) {
            instance = new ReportConfig();
        }
        return instance;
    }
    
    /**
     * Load existing configuration or create default one
     */
    private void loadOrCreateConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    properties.load(input);
                    System.out.println("[ReportConfig] Loaded configuration from " + CONFIG_FILE);
                }
            } else {
                createDefaultConfig();
            }
        } catch (IOException e) {
            System.err.println("[ReportConfig] Error loading config: " + e.getMessage());
            loadDefaults();
        }
    }
    
    /**
     * Create default configuration file
     */
    private void createDefaultConfig() {
        try {
            Files.createDirectories(Paths.get("config"));
            
            // Set default properties
            properties.setProperty("scheduler.enabled", "true");
            properties.setProperty("scheduler.initial_delay_seconds", "60");
            properties.setProperty("scheduler.interval_seconds", "60");

            properties.setProperty("monitor.enabled", "true");
            properties.setProperty("monitor.interval_seconds", "30");
            properties.setProperty("monitor.cache_hitrate_threshold", "70.0");
            properties.setProperty("monitor.cache_recovery_threshold", "85.0");
            properties.setProperty("monitor.min_report_interval_seconds", "1800");
            
            properties.setProperty("reports.output_directory", "reports");
            properties.setProperty("reports.generate_text", "true");
            properties.setProperty("reports.generate_html", "true");
            properties.setProperty("reports.generate_csv", "true");
            
            // Save to file
            try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
                properties.store(output, "SMECS Report Generation Configuration\nAuto-generated on " + new java.util.Date());
            }
            System.out.println("[ReportConfig] Created default configuration file: " + CONFIG_FILE);
            
        } catch (IOException e) {
            System.err.println("[ReportConfig] Error creating config file: " + e.getMessage());
            loadDefaults();
        }
    }
    
    /**
     * Load default in-memory configuration
     */
    private void loadDefaults() {
        properties.setProperty("scheduler.enabled", "true");
        properties.setProperty("scheduler.initial_delay_seconds", "60");
        properties.setProperty("scheduler.interval_seconds", "60");

        properties.setProperty("monitor.enabled", "true");
        properties.setProperty("monitor.interval_seconds", "30");
        properties.setProperty("monitor.cache_hitrate_threshold", "70.0");
        properties.setProperty("monitor.cache_recovery_threshold", "85.0");
        properties.setProperty("monitor.min_report_interval_seconds", "1800");
        
        properties.setProperty("reports.output_directory", "reports");
        properties.setProperty("reports.generate_text", "true");
        properties.setProperty("reports.generate_html", "true");
        properties.setProperty("reports.generate_csv", "true");
        
        System.out.println("[ReportConfig] Using default in-memory configuration");
    }
    
    // Scheduler configuration getters
    public boolean isSchedulerEnabled() {
        return Boolean.parseBoolean(properties.getProperty("scheduler.enabled", "true"));
    }
    
    public long getSchedulerInitialDelaySeconds() {
        return Long.parseLong(properties.getProperty("scheduler.initial_delay_seconds", "60"));
    }
    
    public long getSchedulerIntervalSeconds() {
        return Long.parseLong(properties.getProperty("scheduler.interval_seconds", "3600"));
    }
    
    // Monitor configuration getters
    public boolean isMonitorEnabled() {
        return Boolean.parseBoolean(properties.getProperty("monitor.enabled", "true"));
    }
    
    public long getMonitorIntervalSeconds() {
        return Long.parseLong(properties.getProperty("monitor.interval_seconds", "30"));
    }
    
    public double getCacheHitRateThreshold() {
        return Double.parseDouble(properties.getProperty("monitor.cache_hitrate_threshold", "70.0"));
    }
    
    public double getCacheRecoveryThreshold() {
        return Double.parseDouble(properties.getProperty("monitor.cache_recovery_threshold", "85.0"));
    }
    
    public long getMinReportIntervalSeconds() {
        return Long.parseLong(properties.getProperty("monitor.min_report_interval_seconds", "1800"));
    }
    
    // Reports configuration getters
    public String getOutputDirectory() {
        return properties.getProperty("reports.output_directory", "reports");
    }
    
    public boolean shouldGenerateTextReports() {
        return Boolean.parseBoolean(properties.getProperty("reports.generate_text", "true"));
    }
    
    public boolean shouldGenerateHtmlReports() {
        return Boolean.parseBoolean(properties.getProperty("reports.generate_html", "true"));
    }
    
    public boolean shouldGenerateCsvReports() {
        return Boolean.parseBoolean(properties.getProperty("reports.generate_csv", "true"));
    }
    
    /**
     * Get configuration summary
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== Report Configuration ===\n");
        summary.append("Scheduler Enabled: ").append(isSchedulerEnabled()).append("\n");
        summary.append("Scheduler Interval: ").append(getSchedulerIntervalSeconds()).append(" seconds\n");
        summary.append("Monitor Enabled: ").append(isMonitorEnabled()).append("\n");
        summary.append("Cache Hit Rate Threshold: ").append(getCacheHitRateThreshold()).append("%\n");
        summary.append("Output Directory: ").append(getOutputDirectory()).append("\n");
        return summary.toString();
    }
}

