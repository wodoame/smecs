package com.smecs;

import com.smecs.util.EnvironmentConfig;
import com.smecs.util.ReportConfig;
import com.smecs.util.ReportScheduler;
import com.smecs.util.ReportMonitor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize environment configuration (loads credentials from env vars)
        try {
            EnvironmentConfig.logConfiguration();
        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            System.err.println("\nPlease configure environment variables and restart the application.");
            System.exit(1);
        }

        // Initialize smart report generation system
//        initializeReportingSystem();

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/main_layout.fxml")));
        primaryStage.setTitle("Login - Smart E-Commerce System");
//        primaryStage.setScene(new Scene(root, 400, 500)); // size for login / signup screens
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);

        // Setup shutdown handler for graceful shutdown
        primaryStage.setOnCloseRequest(event -> shutdownReportingSystem());

        primaryStage.show();
    }

    /**
     * Initialize the smart automatic reporting system
     */
    private void initializeReportingSystem() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Initializing Smart Reporting System...");
        System.out.println("=".repeat(70));

        try {
            // Start scheduled reports (every 1 hour by default)
            ReportScheduler.getInstance();
            System.out.println("✓ Report Scheduler initialized");

            // Start intelligent performance monitoring
            ReportMonitor monitor = ReportMonitor.getInstance();
            monitor.startMonitoring();
            System.out.println("✓ Report Monitor initialized");
            ReportConfig config = ReportConfig.getInstance();

            System.out.println("\nReporting system is now active!");
            System.out.println("- Reports will be generated every " + config.getSchedulerIntervalSeconds() + " seconds");
            System.out.println("- Performance degradation will trigger immediate reports");
            System.out.println("- All reports saved to ./reports/ directory");
            System.out.println("=".repeat(70) + "\n");

        } catch (Exception e) {
            System.err.println("Error initializing reporting system: " + e.getMessage());
//            e.printStackTrace();
        }
    }

    /**
     * Shutdown the reporting system gracefully
     */
    private void shutdownReportingSystem() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Shutting down Smart Reporting System...");
        System.out.println("=".repeat(70));

        try {
            ReportScheduler scheduler = ReportScheduler.getInstance();
            scheduler.shutdown();
            System.out.println("✓ Report Scheduler shut down");

            ReportMonitor monitor = ReportMonitor.getInstance();
            monitor.shutdown();
            System.out.println("✓ Report Monitor shut down");

            System.out.println("=".repeat(70) + "\n");
        } catch (Exception e) {
            System.err.println("Error shutting down reporting system: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
