# Smart Report Generation System - Quick Start Guide

## What's New?

The SMECS application now automatically generates performance reports without any user interaction. The system is "smart" because it:

✅ **Generates reports on a schedule** - Every hour by default  
✅ **Monitors performance** - Every 30 seconds  
✅ **Detects issues** - Triggers alerts on cache degradation  
✅ **Runs in background** - Doesn't interfere with normal operations  
✅ **Configurable** - Easy to customize via config file  

## How It Works

### On Application Startup

When you launch the SMECS application:

1. The reporting system initializes automatically
2. The scheduler starts planning hourly report generations
3. The monitor begins tracking performance metrics
4. You see confirmation in the console

### During Normal Operation

While the application runs:

- **Every 30 seconds**: System monitors cache performance
- **Every hour** (by default): Scheduled report is generated
- **On performance issues**: Immediate report triggered
- **All reports saved**: To `./reports/` directory

### On Application Shutdown

When you close the application:

1. All reporting tasks are cancelled gracefully
2. Timers are stopped
3. System shuts down cleanly

## Output

### Console Feedback

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
```

### Report Files

Three formats automatically generated:

```
reports/
├── performance_report_20260117_143215.txt    # Text format
├── performance_report_20260117_143215.html   # Web-viewable
└── performance_metrics_20260117_143215.csv   # Excel-ready
```

## Configuration

To customize the system, edit: `config/report_config.properties`

### Quick Settings

**Generate reports every 30 minutes:**
```properties
scheduler.interval_seconds=1800
```

**Monitor more frequently:**
```properties
monitor.interval_seconds=10
```

**More sensitive performance alerts:**
```properties
monitor.cache_hitrate_threshold=80.0
```

**Only generate CSV (saves disk space):**
```properties
reports.generate_text=false
reports.generate_html=false
reports.generate_csv=true
```

## Monitoring

### Performance Degradation

When cache performance drops below 70%:

```
[ReportMonitor] ⚠ WARNING: Cache hit rate degraded to 65.3%
```

After 3 consecutive checks (90 seconds), a report is automatically generated:

```
[ReportMonitor] ALERT: Generating performance degradation report
[ReportMonitor] Timestamp: 2026-01-17 14:32:15
[ReportMonitor] Cache hit rate: 65.3%
```

### Performance Recovery

When performance recovers to 85% or higher:

```
[ReportMonitor] ✓ Cache performance recovered to 87.2%
```

## What Gets Measured?

Each report includes:

📊 **Cache Performance**
- Hit rate and miss statistics
- Number of cached products
- Search query caching

📊 **Query Optimization**
- Database query execution times
- Connection performance
- Before/after optimization metrics

📊 **System Performance**
- Overall throughput
- Search performance
- System-wide metrics

📊 **Recommendations**
- Suggestions for further optimization
- Performance best practices
- Scalability considerations

## Troubleshooting

### Reports aren't generating
1. Check `config/report_config.properties` exists (if not, it will be auto-created)
2. Verify `scheduler.enabled=true`
3. Check console for error messages
4. Ensure `reports/` directory is writable

### Too many reports
Increase the minimum report interval:
```properties
monitor.min_report_interval_seconds=3600
```

### Want to disable temporarily
In `config/report_config.properties`:
```properties
scheduler.enabled=false
monitor.enabled=false
```

## Manual Report Generation

If you need a report immediately (via Java code):

```java
ReportMonitor monitor = ReportMonitor.getInstance();
monitor.triggerManualReport("User requested urgent report");
```

## Directory Structure

```
smecs/
├── reports/                              # Auto-generated reports
│   ├── performance_report_*.txt
│   ├── performance_report_*.html
│   └── performance_metrics_*.csv
├── config/                               # Configuration
│   └── report_config.properties          # Auto-created if missing
└── src/main/java/com/smecs/util/
    ├── ReportScheduler.java              # Scheduling logic
    ├── ReportMonitor.java                # Performance monitoring
    ├── ReportConfig.java                 # Configuration management
    └── PerformanceReportGenerator.java    # Report generation
```

## Default Configuration

The system creates `config/report_config.properties` automatically with these defaults:

```properties
# Generate reports every 1 hour
scheduler.interval_seconds=3600

# Check performance every 30 seconds
monitor.interval_seconds=30

# Alert if cache hit rate falls below 70%
monitor.cache_hitrate_threshold=70.0

# Clear alert when hit rate recovers to 85%
monitor.cache_recovery_threshold=85.0

# Minimum 30 minutes between triggered reports (prevents spam)
monitor.min_report_interval_seconds=1800

# Generate all three report formats
reports.generate_text=true
reports.generate_html=true
reports.generate_csv=true
```

## Examples

### Example 1: High-Frequency Monitoring (Development)
```properties
scheduler.interval_seconds=300        # Reports every 5 minutes
monitor.interval_seconds=5            # Check performance every 5 seconds
monitor.cache_hitrate_threshold=75.0  # More sensitive
monitor.min_report_interval_seconds=60 # Allow frequent reports
```

### Example 2: Light Monitoring (Production)
```properties
scheduler.interval_seconds=86400      # Reports daily
monitor.interval_seconds=300          # Check every 5 minutes
monitor.cache_hitrate_threshold=60.0  # Less sensitive
monitor.min_report_interval_seconds=7200 # At least 2 hours between reports
```

### Example 3: CSV-Only for Data Analysis
```properties
reports.generate_text=false
reports.generate_html=false
reports.generate_csv=true
```

## Key Features

🎯 **Zero Configuration Required** - Works out of the box with sensible defaults

🎯 **Fully Configurable** - Adjust every aspect via config file

🎯 **Non-Intrusive** - Runs in background using daemon threads

🎯 **Graceful Lifecycle** - Proper initialization and shutdown

🎯 **Performance Aware** - Smart monitoring prevents resource exhaustion

🎯 **Multiple Formats** - TXT for reading, HTML for viewing, CSV for analysis

## Next Steps

1. Run the application normally - reporting system activates automatically
2. Check the `reports/` directory after 1 minute for first report
3. Open `performance_report_*.html` in a browser to view nicely formatted reports
4. Review `config/report_config.properties` to customize settings
5. Check console output to see monitoring and report generation activities

---

For detailed configuration and advanced usage, see: `docs/SMART_REPORTING_SYSTEM.md`

