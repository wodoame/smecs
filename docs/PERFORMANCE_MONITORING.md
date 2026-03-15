# Performance Monitoring Guide

Use this guide to capture CPU, memory, and response-time data while benchmarking SMECS.

## What Is Already Enabled
- Spring Boot Actuator is included in `pom.xml`.
- Actuator endpoints are exposed in `src/main/resources/application-dev.properties`.
- `/actuator/**` is permitted in security config for local testing.

No extra dependency is required for basic CPU and JVM memory monitoring.

## Start the Application
```bash
mvn spring-boot:run
```

Base URL:
- App: `http://localhost:8080`
- Actuator root: `http://localhost:8080/actuator`

## Metrics to Use

### Response Time
- `GET /actuator/metrics/http.server.requests`

Useful tag examples:
- `http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/products&tag=method:GET`

### CPU
- `GET /actuator/metrics/process.cpu.usage`

Primary CPU metric for this project:
- `process.cpu.usage` shows CPU used by the SMECS Java process.

Optional supporting metric:
- `GET /actuator/metrics/system.cpu.usage`

### Memory
- `GET /actuator/metrics/jvm.memory.used`
- `GET /actuator/metrics/jvm.memory.max`

Useful tag examples:
- `http://localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap`
- `http://localhost:8080/actuator/metrics/jvm.memory.max?tag=area:heap`

Optional supporting metrics:
- `GET /actuator/metrics/jvm.gc.pause`
- `GET /actuator/metrics/jvm.threads.live`

## Recommended Test Flow
1. Start the application and confirm `http://localhost:8080/actuator/health` is up.
2. Record a pre-test CPU and heap memory reading.
3. Run your Postman performance test.
4. During the run, sample these endpoints 2-3 times:
   - `/actuator/metrics/process.cpu.usage`
   - `/actuator/metrics/jvm.memory.used?tag=area:heap`
5. Record the peak or representative values in `docs/PERFORMANCE_REPORT.md`.
6. After optimization, repeat the exact same scenario and compare results.

## What to Record in the Report
For each scenario, capture:
- requests
- concurrency / virtual users
- average response time
- P90 / P95 / P99 if available
- throughput
- process CPU usage
- heap memory used
- errors / timeouts

## Practical Notes
- Take CPU and memory samples while the test is running, not only before or after.
- For short tests, sample once near the middle and once near the end.
- For longer tests, record the highest stable CPU and memory values you see.
- Keep the same dataset and machine conditions for before/after comparisons.

## Optional GUI Profiling
If you want richer CPU and memory views, use one of these alongside Actuator:
- VisualVM
- Java Flight Recorder (JFR)

Actuator is enough for the report table; VisualVM or JFR helps explain bottlenecks with screenshots.
