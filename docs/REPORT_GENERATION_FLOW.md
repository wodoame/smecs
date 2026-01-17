# Report Generation Flow - Visual Reference

## Quick Reference: Where Reports Are Generated

### THE TWO MAIN REPORT GENERATION FLOWS

```
┌─────────────────────────────────────────────────────────────────────┐
│                     APPLICATION STARTUP                            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
                 ┌─────────────────────────┐
                 │ MainApp.start()         │
                 │ (MainApp.java:16-25)    │
                 └────────────┬────────────┘
                              │
                              ▼
              ┌──────────────────────────────────┐
              │ initializeReportingSystem()      │
              │ (MainApp.java:31-49)             │
              └──────────────┬───────────────────┘
                             │
            ┌────────────────┴────────────────┐
            │                                 │
            ▼                                 ▼
  ┌─────────────────────┐        ┌────────────────────────┐
  │ReportScheduler      │        │ ReportMonitor          │
  │.getInstance()       │        │ .getInstance()         │
  │(Singleton)          │        │ (Singleton)            │
  │                     │        │                        │
  │scheduleReports()    │        │ startMonitoring()      │
  │(Line 43-63)         │        │ (Line 56-60)           │
  └────────────┬────────┘        └────────────┬───────────┘
               │                               │
               ▼                               ▼
    ┌──────────────────────┐      ┌─────────────────────────┐
    │ Timer.               │      │ Timer.                  │
    │ scheduleAtFixedRate()│      │ scheduleAtFixedRate()   │
    │                      │      │                         │
    │ initial_delay:       │      │ interval:               │
    │ 60 seconds (default) │      │ 30 seconds (default)    │
    │                      │      │                         │
    │ interval:            │      └─────────────┬───────────┘
    │ 3600 seconds         │                    │
    │ (1 hour - default)   │                    ▼
    └────────────┬─────────┘      ┌──────────────────────────────┐
                 │                │ PerformanceMonitoringTask   │
                 │                │ .run() every 30 seconds     │
                 │                │                             │
                 │                │ • Get cache hit rate        │
                 │                │ • Compare to threshold      │
                 │                │   (70% default)            │
                 │                │ • Track consecutive         │
                 │                │   degradations             │
                 │                └─────────┬──────────────────┘
                 │                          │
                 │                ┌─────────┴──────────┐
                 │                │                    │
                 │         HIT RATE OK         HIT RATE DEGRADED
                 │                │             │
                 │                │      Count 3 consecutive checks
                 │         [No action]          │
                 │                │             ▼
                 │                │      [THRESHOLD MET]
                 │                │             │
                 │                │      Check min interval since
                 │                │      last degradation report
                 │                │      (1800 seconds = 30 min)
                 │                │             │
                 │                ├─ TOO SOON? │
                 │                │   └─> SKIP │
                 │                │             │
                 └────────────────┼─────OK─────┘
                                  │
                                  ▼
                 ┌──────────────────────────────────┐
                 │ ReportGenerationTask.run()       │
                 │ (Called every interval OR        │
                 │  on degradation)                 │
                 │                                  │
                 │ (ReportScheduler.java:87-104)   │
                 └────────────────┬─────────────────┘
                                  │
                                  ▼
                 ┌──────────────────────────────────┐
                 │ PerformanceReportGenerator       │
                 │ .generateAllReports()            │
                 │ (PerformanceReportGenerator      │
                 │  .java:328-359)                  │
                 └────────────────┬─────────────────┘
                                  │
                ┌─────────────────┼─────────────────┐
                │                 │                 │
                ▼                 ▼                 ▼
  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
  │generateComprehensive
  │Report()          │  │generateHTML      │  │generateCSV       │
  │                  │  │Report()          │  │Report()          │
  │Returns:          │  │                  │  │                  │
  │Text content      │  │Returns:          │  │Returns:          │
  │with sections:    │  │HTML formatted    │  │CSV formatted     │
  │• Executive       │  │content with CSS  │  │data with headers │
  │  Summary         │  │styling           │  │                  │
  │• Cache Perf      │  │                  │  │Metrics:          │
  │• Query Optim     │  │Interactive       │  │• Cache stats     │
  │• DB Connect      │  │tables & status   │  │• Search indices  │
  │• System Perf     │  │indicators        │  │• Performance     │
  │• Recommend       │  │                  │  │  data            │
  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘
           │                     │                     │
           └─────────────────────┼─────────────────────┘
                                 │
                                 ▼
                ┌──────────────────────────────────┐
                │ saveReportToFile(content, name)  │
                │ (PerformanceReportGenerator      │
                │  .java:311-322)                  │
                │                                  │
                │ • Creates reports/ directory     │
                │ • Writes file with FileWriter    │
                │ • Logs "Report saved to: ..."    │
                └────────────────┬─────────────────┘
                                 │
                ┌────────────────┬┴─────────────────┐
                │                │                  │
                ▼                ▼                  ▼
  ┌─────────────────────┐ ┌──────────────────┐ ┌──────────────────┐
  │reports/             │ │reports/          │ │reports/          │
  │performance_report_  │ │performance_report│ │performance_      │
  │20260117_123456.txt  │ │_20260117_123456. │ │metrics_20260117_ │
  │                     │ │html              │ │123456.csv        │
  │Plain text format    │ │                  │ │                  │
  │with all sections    │ │HTML format with  │ │CSV format for    │
  │                     │ │styling & links   │ │Excel/analysis    │
  └─────────────────────┘ └──────────────────┘ └──────────────────┘
                                 │
                                 ▼
                 ┌──────────────────────────────────┐
                 │ Console Output:                  │
                 │ "Report saved to: reports/..."   │
                 │ "✓ All reports generated!"       │
                 │ "Reports saved in ./reports/"    │
                 └──────────────────────────────────┘
```

---

## Configuration Flow

```
┌──────────────────────────────────┐
│ ReportConfig.getInstance()       │
│ (Singleton Pattern)              │
└────────────────┬─────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
  ┌──────────────┐   ┌────────────────────┐
  │Config file   │   │ Default hardcoded  │
  │exists?       │   │ configuration      │
  │              │   │                    │
  │config/       │   │scheduler.enabled   │
  │report_config │   │scheduler.interval  │
  │.properties   │   │monitor.enabled     │
  └──┬──────┬────┘   │monitor.interval    │
     │      │        │monitor.threshold   │
    YES    NO        └────────────────────┘
     │      │
     ▼      ▼
   LOAD   CREATE DEFAULT
   FILE   & SAVE FILE
     │      │
     └──────┴──────┐
                   │
                   ▼
        ┌──────────────────────┐
        │ Properties object    │
        │ with all settings    │
        │                      │
        │ Used by:             │
        │ • ReportScheduler    │
        │ • ReportMonitor      │
        │ • PerformanceReport  │
        │   Generator          │
        └──────────────────────┘
```

---

## Parameter Timing Diagram

```
APPLICATION START
│
├─ Wait: scheduler.initial_delay_seconds (60s default)
│
├─ FIRST REPORT GENERATION
│  └─ Save 3 files: .txt, .html, .csv
│
├─ Wait: scheduler.interval_seconds (3600s/1hr default)
│
├─ SECOND REPORT GENERATION
│  └─ Save 3 files: .txt, .html, .csv
│
└─ ... continues every interval_seconds

PARALLEL: Every monitor.interval_seconds (30s):
│
├─ Check cache hit rate
│ │
│ ├─ IF rate >= threshold (70% default):
│ │  └─ No action
│ │
│ └─ IF rate < threshold:
│    ├─ Increment degradation counter
│    │
│    └─ IF degradation_counter >= 3:
│       ├─ Check: time since last degradation report
│       │
│       ├─ IF < min_report_interval (1800s/30min default):
│       │  └─ SKIP (prevent alert fatigue)
│       │
│       └─ IF >= min_report_interval:
│          └─ GENERATE EMERGENCY REPORT
│             └─ Save 3 files: .txt, .html, .csv
```

---

## Code Call Stack for File Save

```
ReportScheduler.ReportGenerationTask.run()
│
└─ PerformanceReportGenerator.generateAllReports()
   │
   ├─ generateComprehensiveReport() ──┐
   │  (builds text content)            │
   │                                   ▼
   ├─ saveReportToFile(textReport, "performance_report_TIMESTAMP.txt")
   │  │
   │  ├─ Files.createDirectories(Paths.get("reports"))
   │  ├─ FileWriter fw = new FileWriter("reports/performance_report_TIMESTAMP.txt")
   │  ├─ fw.write(content)
   │  ├─ fw.close()
   │  └─ System.out.println("Report saved to: ...")
   │
   ├─ generateHTMLReport() ───────────┐
   │  (builds HTML content)            │
   │                                   ▼
   ├─ saveReportToFile(htmlReport, "performance_report_TIMESTAMP.html")
   │  │
   │  ├─ Files.createDirectories(Paths.get("reports"))
   │  ├─ FileWriter fw = new FileWriter("reports/performance_report_TIMESTAMP.html")
   │  ├─ fw.write(content)
   │  ├─ fw.close()
   │  └─ System.out.println("Report saved to: ...")
   │
   └─ generateCSVReport() ────────────┐
      (builds CSV content)             │
                                       ▼
   └─ saveReportToFile(csvReport, "performance_metrics_TIMESTAMP.csv")
      │
      ├─ Files.createDirectories(Paths.get("reports"))
      ├─ FileWriter fw = new FileWriter("reports/performance_metrics_TIMESTAMP.csv")
      ├─ fw.write(content)
      ├─ fw.close()
      └─ System.out.println("Report saved to: ...")
```

---

## File Naming Convention

```
TIMESTAMP FORMAT: yyyyMMdd_HHmmss

Example: 20260117_123456
         ││││││││_││││││
         ││││││││ ││││└─ Seconds (00-59)
         ││││││││ │││└── Minutes (00-59)
         ││││││││ ││└─── Hours (00-23)
         ││││││││ └───── Hours (00-23)
         ││││└└└└ Day (01-31)
         ││└─────── Month (01-12)
         └────────── Year (YYYY)

TEXT REPORT: performance_report_20260117_123456.txt
HTML REPORT: performance_report_20260117_123456.html
CSV REPORT:  performance_metrics_20260117_123456.csv

Multiple generations create files with different timestamps:
- performance_report_20260117_100000.txt
- performance_report_20260117_100000.html
- performance_metrics_20260117_100000.csv
- performance_report_20260117_110000.txt  (1 hour later)
- performance_report_20260117_110000.html
- performance_metrics_20260117_110000.csv
```

---

## Configuration Parameters Reference

| Parameter | Purpose | Default | Location |
|-----------|---------|---------|----------|
| `scheduler.enabled` | Enable/disable time-based reports | `true` | config/report_config.properties |
| `scheduler.initial_delay_seconds` | Seconds before first report | `60` | config/report_config.properties |
| `scheduler.interval_seconds` | Seconds between scheduled reports | `3600` (1 hour) | config/report_config.properties |
| `monitor.enabled` | Enable/disable performance monitoring | `true` | config/report_config.properties |
| `monitor.interval_seconds` | Seconds between cache checks | `30` | config/report_config.properties |
| `monitor.cache_hitrate_threshold` | Alert if hit rate drops below | `70.0%` | config/report_config.properties |
| `monitor.cache_recovery_threshold` | Clear alert if hit rate rises above | `85.0%` | config/report_config.properties |
| `monitor.min_report_interval_seconds` | Min time between degradation reports | `1800` (30 min) | config/report_config.properties |
| `reports.output_directory` | Where to save reports | `reports` | config/report_config.properties |
| `reports.generate_text` | Generate .txt reports | `true` | config/report_config.properties |
| `reports.generate_html` | Generate .html reports | `true` | config/report_config.properties |
| `reports.generate_csv` | Generate .csv reports | `true` | config/report_config.properties |

---

## Key File Locations

```
src/main/java/com/smecs/
│
├─ MainApp.java
│  └─ initializeReportingSystem() [ENTRY POINT]
│  └─ shutdownReportingSystem() [SHUTDOWN]
│
└─ util/
   ├─ ReportScheduler.java
   │  └─ scheduleReports() → ReportGenerationTask
   │
   ├─ ReportMonitor.java
   │  └─ startMonitoring() → PerformanceMonitoringTask
   │  └─ triggerPerformanceDegradationReport()
   │
   ├─ PerformanceReportGenerator.java
   │  ├─ generateAllReports() ← MAIN METHOD
   │  ├─ generateComprehensiveReport()
   │  ├─ generateHTMLReport()
   │  ├─ generateCSVReport()
   │  └─ saveReportToFile() ← SAVES TO DISK
   │
   └─ ReportConfig.java
      └─ getInstance() [SINGLETON]

OUTPUT:
reports/
├─ performance_report_TIMESTAMP.txt
├─ performance_report_TIMESTAMP.html
└─ performance_metrics_TIMESTAMP.csv

CONFIG:
config/
└─ report_config.properties
```


