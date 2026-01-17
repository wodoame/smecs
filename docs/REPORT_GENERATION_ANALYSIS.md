# Report Generation Deep Dive - SMECS

## Overview
This document provides a comprehensive analysis of how and where reports are generated and saved to the `reports/` directory in the Smart E-Commerce System (SMECS).

---

## Report Generation Architecture

### Key Components

The report generation system consists of **4 main Java classes** located in `src/main/java/com/smecs/util/`:

1. **PerformanceReportGenerator.java** - Core report generation engine
2. **ReportScheduler.java** - Time-based report scheduling
3. **ReportMonitor.java** - Performance-aware triggering
4. **ReportConfig.java** - Configuration management

---

## Entry Points: Where Reports Are Generated

### 1. **Application Startup** (PRIMARY ENTRY POINT)
**File:** `src/main/java/com/smecs/MainApp.java`

```
MainApp.start()
  └─> initializeReportingSystem()
      ├─> ReportScheduler.getInstance()  [Creates Singleton]
      │   └─> scheduleReports()
      │       └─> Timer.scheduleAtFixedRate(ReportGenerationTask)
      │           └─> Triggers every [scheduler.interval_seconds] (default: 60 seconds in config)
      │
      └─> ReportMonitor.getInstance()    [Creates Singleton]
          └─> startMonitoring()
              └─> Timer.scheduleAtFixedRate(PerformanceMonitoringTask)
                  └─> Monitors cache hit rate every [monitor.interval_seconds] (default: 30 seconds)
```

**Lifecycle:**
- ✅ **Initialization:** When application starts via `JavaFX Application.launch()`
- ✅ **Continuous Running:** Runs in background daemon threads
- ✅ **Shutdown:** Gracefully stops when application closes via `primaryStage.setOnCloseRequest()`

---

## Report Generation Triggers

### **Trigger 1: Time-Based Scheduled Reports**
**Source:** `ReportScheduler.java` (Lines 43-63)

```java
public synchronized void scheduleReports() {
    long initialDelayMs = config.getSchedulerInitialDelaySeconds() * 1000;    // Default: 60 seconds
    long intervalMs = config.getSchedulerIntervalSeconds() * 1000;            // Default: 60 seconds (or 3600 for 1 hour)
    
    timer.scheduleAtFixedRate(new ReportGenerationTask(), initialDelayMs, intervalMs);
}
```

**What Happens:**
1. Reports start generating after `initial_delay_seconds` from startup
2. Reports continue generating at fixed `interval_seconds` intervals
3. Each task calls: `reportGenerator.generateAllReports()`

**Console Output Pattern:**
```
[ReportScheduler] Starting automatic report generation scheduler
[ReportScheduler] Initial delay: 60 seconds
[ReportScheduler] Report interval: 60 seconds
(... 60 seconds pass ...)
======================================================================
[ReportScheduler] Generating performance reports at 2026-01-17 12:34:56
======================================================================
Generating comprehensive performance reports...
1. Generating text report...
Report saved to: reports/performance_report_20260117_123456.txt
2. Generating HTML report...
Report saved to: reports/performance_report_20260117_123456.html
3. Generating CSV report...
Report saved to: reports/performance_metrics_20260117_123456.csv
✓ All reports generated successfully!
Reports saved in ./reports/ directory
======================================================================
```

---

### **Trigger 2: Performance Degradation Reports**
**Source:** `ReportMonitor.java` (Lines 65-135)

```java
public synchronized void startMonitoring() {
    monitoringTimer.scheduleAtFixedRate(
        new PerformanceMonitoringTask(), 
        monitorIntervalMs,           // Default: 30 seconds
        monitorIntervalMs
    );
}
```

**What Happens:**
1. Monitors cache hit rate every 30 seconds
2. If cache hit rate **falls below threshold** (default: 70%):
   - Alerts: `[ReportMonitor] ⚠ WARNING: Cache hit rate degraded to X.X%`
   - Waits for 3 consecutive degraded checks (threshold)
   - Then triggers: `reportGenerator.generateAllReports()`
   
3. If cache hit rate **recovers above recovery threshold** (default: 85%):
   - Alerts: `[ReportMonitor] ✓ Cache performance recovered to X.X%`
   - Clears degradation alert state

**Console Output Pattern:**
```
[ReportMonitor] Starting intelligent performance monitoring
[ReportMonitor] Monitor interval: 30 seconds
[ReportMonitor] Cache hit rate threshold: 70.0%

(... monitoring in background ...)

[ReportMonitor] ⚠ WARNING: Cache hit rate degraded to 65.3%
(... 2 more consecutive checks ...)

======================================================================
[ReportMonitor] ALERT: Generating performance degradation report
[ReportMonitor] Timestamp: 2026-01-17 12:35:45
[ReportMonitor] Cache hit rate: 65.3%
======================================================================
Generating comprehensive performance reports...
1. Generating text report...
Report saved to: reports/performance_report_20260117_123545.txt
...
======================================================================
```

**Smart Features:**
- Prevents "alert fatigue" with `min_report_interval_seconds` (default: 1800 = 30 minutes)
  - Won't generate degradation reports more frequently than every 30 minutes
- Requires 3 consecutive degradation checks before reporting
  - Avoids false positives from temporary fluctuations

---

### **Trigger 3: Manual Reports** (Optional)
**Source:** `ReportMonitor.java` (Lines 169-185)

```java
public void triggerManualReport(String reason) {
    // Can be called programmatically to generate reports on-demand
    reportGenerator.generateAllReports();
}
```

This allows other parts of the application to trigger reports for specific reasons (e.g., admin request, batch job completion, etc.)

---

## Report File Generation Details

### Core Generation Method
**File:** `PerformanceReportGenerator.java` (Lines 328-359)

```java
public void generateAllReports() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    String timestamp = LocalDateTime.now().format(formatter);
    
    // 1. Generate Text Report
    String textReport = generateComprehensiveReport();
    saveReportToFile(textReport, "performance_report_" + timestamp + ".txt");
    
    // 2. Generate HTML Report
    String htmlReport = generateHTMLReport();
    saveReportToFile(htmlReport, "performance_report_" + timestamp + ".html");
    
    // 3. Generate CSV Report
    String csvReport = generateCSVReport();
    saveReportToFile(csvReport, "performance_metrics_" + timestamp + ".csv");
}
```

### File Saving Implementation
**File:** `PerformanceReportGenerator.java` (Lines 311-322)

```java
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
```

**Key Points:**
- ✅ Automatically creates `reports/` directory if it doesn't exist
- ✅ Files are written with `FileWriter` (text mode)
- ✅ Uses timestamp format: `yyyyMMdd_HHmmss` (e.g., `20260117_123456`)
- ✅ Three file extensions generated per report cycle:
  - `.txt` - Plain text format
  - `.html` - HTML with styling for web viewing
  - `.csv` - CSV format for Excel/analysis

---

## Report Content Structure

### 1. **Text Report** (`generateComprehensiveReport()`)
**File:** `PerformanceReportGenerator.java` (Lines 29-80)

Contains:
- Executive Summary
- Section 1: Cache Performance Analysis
  - Cache hit/miss statistics
  - Cache efficiency metrics
- Section 2: Query Optimization Results
- Section 3: Database Connection Performance
- Section 4: Overall System Performance
- Section 5: Optimization Recommendations

### 2. **HTML Report** (`generateHTMLReport()`)
**File:** `PerformanceReportGenerator.java` (Lines 283-310)

Contains:
- Styled HTML with CSS
- Cache performance metrics display
- Interactive table formatting
- Status indicators (✓ EXCELLENT, ⚠ GOOD, ✗ NEEDS IMPROVEMENT)
- Embedded comprehensive report

### 3. **CSV Report** (`generateCSVReport()`)
**File:** `PerformanceReportGenerator.java` (Lines 265-281)

Format:
```csv
Metric Category,Metric Name,Value,Unit
Cache,Hit Rate,XX.XX,%
Cache,Total Hits,XXXX,count
Cache,Total Misses,XXXX,count
Cache,Products Cached,XXXX,count
...
```

---

## Configuration Management

**File:** `config/report_config.properties`

**Default Configuration:**
```properties
# Scheduler Configuration
scheduler.enabled=true
scheduler.initial_delay_seconds=60
scheduler.interval_seconds=3600          # 1 hour

# Monitor Configuration
monitor.enabled=true
monitor.interval_seconds=30
monitor.cache_hitrate_threshold=70.0
monitor.cache_recovery_threshold=85.0
monitor.min_report_interval_seconds=1800 # 30 minutes

# Report Generation Configuration
reports.output_directory=reports
reports.generate_text=true
reports.generate_html=true
reports.generate_csv=true
```

**Configuration Management:**
- **File:** `ReportConfig.java` (Lines 27-65)
- Auto-creates `config/report_config.properties` if not found
- Falls back to hardcoded defaults if file cannot be read
- Singleton pattern ensures single configuration instance

---

## Complete Call Flow Diagram

```
APPLICATION STARTUP
│
├─ MainApp.start() [JavaFX Application Entry]
│  │
│  └─ initializeReportingSystem()
│     │
│     ├─ ReportScheduler.getInstance()
│     │  └─ Constructor calls: scheduleReports()
│     │     └─ Timer.scheduleAtFixedRate()
│     │        └─ EVERY [interval] seconds:
│     │           └─ ReportGenerationTask.run()
│     │              └─ PerformanceReportGenerator.generateAllReports()
│     │                 ├─ generateComprehensiveReport() → save .txt
│     │                 ├─ generateHTMLReport() → save .html
│     │                 └─ generateCSVReport() → save .csv
│     │
│     └─ ReportMonitor.getInstance()
│        └─ Constructor + startMonitoring()
│           └─ Timer.scheduleAtFixedRate()
│              └─ EVERY [monitor_interval] seconds:
│                 └─ PerformanceMonitoringTask.run()
│                    ├─ Get cache hit rate
│                    ├─ IF degraded:
│                    │  └─ AFTER [3 checks]:
│                    │     └─ triggerPerformanceDegradationReport()
│                    │        └─ PerformanceReportGenerator.generateAllReports()
│                    │           └─ Save .txt, .html, .csv files
│                    │
│                    └─ IF recovered:
│                       └─ Clear alert state
│
APPLICATION SHUTDOWN
│
└─ MainApp.shutdownReportingSystem()
   ├─ ReportScheduler.shutdown()
   └─ ReportMonitor.shutdown()
```

---

## File Output Pattern

Reports are saved to the `reports/` directory with the following naming pattern:

```
reports/
├── performance_report_20260117_100431.txt     ← Text format
├── performance_report_20260117_100431.html    ← HTML format
├── performance_metrics_20260117_100431.csv    ← CSV format
├── performance_report_20260117_100531.txt
├── performance_report_20260117_100531.html
├── performance_metrics_20260117_100531.csv
└── ... (more reports every interval)
```

**Filename Components:**
- `performance_report_` or `performance_metrics_` prefix
- `yyyyMMdd_HHmmss` timestamp (e.g., `20260117_100431` = Jan 17, 2026 at 10:04:31)
- `.txt`, `.html`, or `.csv` extension

---

## Key Statistics from Code

| Metric | Default Value | Location |
|--------|---------------|----------|
| **Initial Delay** | 60 seconds | `ReportConfig.java`, `report_config.properties` |
| **Report Interval** | 3600 seconds (1 hour) | `ReportConfig.java`, `report_config.properties` |
| **Monitor Check Interval** | 30 seconds | `ReportConfig.java`, `report_config.properties` |
| **Cache Hit Rate Threshold** | 70.0% | `ReportConfig.java`, `report_config.properties` |
| **Cache Recovery Threshold** | 85.0% | `ReportConfig.java`, `report_config.properties` |
| **Min Report Interval (Degradation)** | 1800 seconds (30 min) | `ReportConfig.java`, `report_config.properties` |
| **Degradation Checks Required** | 3 consecutive | `ReportMonitor.java`, line 33 |

---

## Summary

### **Report Generation Happens At:**

1. **On Application Startup**
   - After `scheduler.initial_delay_seconds` (default: 60 seconds)
   - Then repeats every `scheduler.interval_seconds` (default: 3600 seconds = 1 hour)
   - **Source:** `ReportScheduler` with `Timer.scheduleAtFixedRate()`

2. **On Performance Degradation Detection**
   - When cache hit rate falls below `monitor.cache_hitrate_threshold` (default: 70%)
   - After 3 consecutive degradation checks (every 30 seconds = ~90 second delay)
   - With minimum interval throttling: max once per `monitor.min_report_interval_seconds` (default: 30 minutes)
   - **Source:** `ReportMonitor` with performance monitoring task

3. **On Manual Trigger** (if called by other code)
   - Via `ReportMonitor.triggerManualReport(reason)`
   - **Source:** Programmatic API call

### **Reports Are Saved To:**
- **Directory:** `reports/` (created automatically if not exists)
- **Formats:** `.txt`, `.html`, `.csv`
- **Timestamp:** `yyyyMMdd_HHmmss` format
- **Count Per Cycle:** 3 files (one per format)

### **Configuration:**
- **File:** `config/report_config.properties`
- All parameters customizable
- Auto-creates defaults if file not found
- Singleton instance manages all settings


