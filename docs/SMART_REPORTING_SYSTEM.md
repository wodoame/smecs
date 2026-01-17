# Smart Automatic Report Generation System

## Overview

The Smart E-Commerce System (SMECS) now includes an intelligent automatic report generation system that operates without requiring explicit user interaction. Reports are generated automatically based on:

1. **Time-based scheduling** - Regular interval-based generation
2. **Performance monitoring** - Intelligent triggering based on system metrics
3. **Degradation detection** - Immediate reporting when performance issues are detected

## Features

### 1. Scheduled Report Generation
- **Default**: Reports generated every 1 hour
- **Configurable**: Adjust intervals via configuration file
- **Formats**: TXT, HTML, and CSV
- **Non-blocking**: Runs in background threads

### 2. Intelligent Performance Monitoring
- Continuously monitors cache hit rates
- Detects performance degradation automatically
- Triggers immediate reports when issues detected
- Prevents report spam with minimum interval thresholds

### 3. Graceful Lifecycle Management
- Automatic initialization on application startup
- Proper shutdown on application exit
- Thread-safe implementation

## Architecture

### Core Components

#### 1. `ReportScheduler`
- Manages time-based scheduled report generation
- Uses `Timer` with daemon threads
- Singleton pattern for single instance
- **Default behavior**: Starts immediately on instantiation

#### 2. `ReportMonitor`
- Intelligent performance monitoring system
- Tracks cache hit rates and system metrics
- Triggers reports on performance degradation
- Implements smart thresholds to prevent alert fatigue

#### 3. `ReportConfig`
- Centralized configuration management
- Loads from `config/report_config.properties`
- Auto-creates default config if not found
- All parameters customizable

#### 4. `PerformanceReportGenerator`
- Generates reports in multiple formats (TXT, HTML, CSV)
- Collects comprehensive performance metrics
- Provides executive summaries and recommendations

## Configuration

### Default Configuration File
Location: `config/report_config.properties`

```properties
# Scheduler Configuration
scheduler.enabled=true
scheduler.initial_delay_seconds=60
scheduler.interval_seconds=3600

# Monitor Configuration
monitor.enabled=true
monitor.interval_seconds=30
monitor.cache_hitrate_threshold=70.0
monitor.cache_recovery_threshold=85.0
monitor.min_report_interval_seconds=1800

# Report Generation Configuration
reports.output_directory=reports
reports.generate_text=true
reports.generate_html=true
reports.generate_csv=true
```

### Configuration Parameters

#### Scheduler Settings
| Parameter | Description | Default |
|-----------|-------------|---------|
| `scheduler.enabled` | Enable/disable automatic scheduling | `true` |
| `scheduler.initial_delay_seconds` | Delay before first report | `60` |
| `scheduler.interval_seconds` | Interval between scheduled reports | `3600` (1 hour) |

#### Monitor Settings
| Parameter | Description | Default |
|-----------|-------------|---------|
| `monitor.enabled` | Enable/disable performance monitoring | `true` |
| `monitor.interval_seconds` | How often to check metrics | `30` |
| `monitor.cache_hitrate_threshold` | Alert if hit rate falls below | `70.0` |
| `monitor.cache_recovery_threshold` | Alert clears when hit rate reaches | `85.0` |
| `monitor.min_report_interval_seconds` | Minimum time between triggered reports | `1800` (30 minutes) |

#### Report Settings
| Parameter | Description | Default |
|-----------|-------------|---------|
| `reports.output_directory` | Where to save reports | `reports` |
| `reports.generate_text` | Generate .txt reports | `true` |
| `reports.generate_html` | Generate .html reports | `true` |
| `reports.generate_csv` | Generate .csv reports | `true` |

## Usage

### Automatic Operation
Simply run the application normally. The reporting system will:

1. Initialize on startup
2. Begin monitoring immediately
3. Generate first scheduled report after `initial_delay_seconds`
4. Continue generating scheduled reports at the configured interval
5. Trigger extra reports on performance issues
6. Shut down gracefully when application closes

### Console Output

You'll see logging output like:

```
======================================================================
Initializing Smart Reporting System...
======================================================================
✓ Report Scheduler initialized
✓ Report Monitor initialized

Reporting system is now active!
- Reports will be generated hourly
- Performance degradation will trigger immediate reports
- All reports saved to ./reports/ directory
======================================================================

[ReportScheduler] Starting automatic report generation scheduler
[ReportScheduler] Initial delay: 60 seconds
[ReportScheduler] Report interval: 3600 seconds

[ReportMonitor] Starting intelligent performance monitoring
[ReportMonitor] Monitor interval: 30 seconds
[ReportMonitor] Cache hit rate threshold: 70.0%
```

### Performance Degradation Alerts

When performance issues are detected:

```
[ReportMonitor] ⚠ WARNING: Cache hit rate degraded to 65.3%
[ReportMonitor] ALERT: Generating performance degradation report
[ReportMonitor] Timestamp: 2026-01-17 14:32:15
[ReportMonitor] Cache hit rate: 65.3%
```

### Report Files

Reports are automatically saved to the `reports/` directory:

- **Text**: `performance_report_20260117_143215.txt`
- **HTML**: `performance_report_20260117_143215.html`
- **CSV**: `performance_metrics_20260117_143215.csv`

## Monitoring Behavior

### Normal Operation
- Monitor checks every 30 seconds (configurable)
- Compares current cache hit rate to threshold
- If healthy, continues monitoring

### Degradation Detection
1. Cache hit rate falls below threshold (70%)
2. Alert flag is set
3. Monitor tracks consecutive degraded checks
4. After 3 consecutive degraded checks (90 seconds default), report is triggered
5. Report generation is rate-limited (minimum 30 minutes between reports)

### Recovery
When cache performance recovers above recovery threshold (85%):
- Alert flag is cleared
- Degradation counter is reset
- System returns to normal monitoring

## Advanced Configuration

### Scenario: High-Frequency Monitoring
For systems requiring more frequent reporting:

```properties
scheduler.interval_seconds=600
monitor.interval_seconds=10
monitor.cache_hitrate_threshold=75.0
monitor.min_report_interval_seconds=300
```

### Scenario: Low-Frequency Monitoring
For less critical systems:

```properties
scheduler.interval_seconds=86400
monitor.interval_seconds=120
monitor.cache_hitrate_threshold=60.0
monitor.min_report_interval_seconds=3600
```

### Scenario: CSV Reports Only
To reduce disk I/O:

```properties
reports.generate_text=false
reports.generate_html=false
reports.generate_csv=true
```

## Integration Points

The system is integrated into the application lifecycle:

### Startup (`MainApp.start()`)
```
1. Application launches
2. reportingSystem.initializeReportingSystem() is called
3. ReportConfig loads configuration
4. ReportScheduler starts scheduling
5. ReportMonitor starts monitoring
```

### Shutdown (`MainApp.setOnCloseRequest()`)
```
1. User closes application
2. shutdownReportingSystem() is called
3. Scheduler stops gracefully
4. Monitor stops gracefully
5. Timers are cancelled
```

## API for Manual Control

While the system is automatic, you can also trigger reports manually:

```java
// Trigger immediate report generation
ReportMonitor monitor = ReportMonitor.getInstance();
monitor.triggerManualReport("User requested report");

// Get monitoring status
String status = monitor.getMonitoringStatus();
System.out.println(status);
```

## Performance Impact

The smart reporting system is designed to minimize performance impact:

- **Daemon threads**: Don't prevent application shutdown
- **Configurable intervals**: Adjust monitoring frequency as needed
- **Rate limiting**: Prevents report spam during issues
- **Async execution**: Reports generated without blocking UI

## Troubleshooting

### Reports not being generated
1. Check `config/report_config.properties` exists
2. Verify `scheduler.enabled=true`
3. Check console output for initialization messages
4. Ensure `reports/` directory is writable

### Too many reports being generated
1. Increase `monitor.min_report_interval_seconds`
2. Increase `monitor.cache_hitrate_threshold` (less sensitive)
3. Disable monitoring with `monitor.enabled=false`

### Reports not capturing accurate data
1. Ensure cache statistics are being tracked (check ProductCache)
2. Verify database connectivity
3. Check `reports/` directory for generated files

## Best Practices

1. **Regular backups**: Archive old reports periodically
2. **Configuration review**: Adjust thresholds based on your system's needs
3. **Log rotation**: Implement log rotation for console output
4. **Threshold tuning**: Start with defaults, then customize based on observations

## Future Enhancements

Potential improvements:
- Email notifications on performance issues
- Database-backed report history
- Real-time dashboard integration
- Custom alert conditions
- Report comparison tools

