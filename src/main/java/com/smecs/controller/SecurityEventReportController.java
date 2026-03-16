package com.smecs.controller;

import com.smecs.dto.*;
import com.smecs.entity.SecurityEventType;
import com.smecs.repository.SecurityEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/security/reports")
public class SecurityEventReportController {

    private static final Duration DEFAULT_WINDOW = Duration.ofDays(7);

    private final SecurityEventRepository securityEventRepository;
    private final Executor reportTaskExecutor;

    @Autowired
    public SecurityEventReportController(SecurityEventRepository securityEventRepository,
                                         @Qualifier("reportTaskExecutor") Executor reportTaskExecutor) {
        this.securityEventRepository = securityEventRepository;
        this.reportTaskExecutor = reportTaskExecutor;
    }

    @GetMapping("/token-usage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<SecurityTokenUsageReportDTO> tokenUsageReport(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        InstantRange range = resolveRange(start, end);
        int safeLimit = Math.max(1, Math.min(limit, 50));
        Set<SecurityEventType> types = Set.of(
                SecurityEventType.TOKEN_ISSUED,
                SecurityEventType.TOKEN_VALID,
                SecurityEventType.TOKEN_INVALID
        );

        CompletableFuture<List<SecurityEventCountDTO>> totalsFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.countByEventType(types, range.start(), range.end()), reportTaskExecutor);

        CompletableFuture<List<SecurityEventUserCountDTO>> topUsersFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.findTopUsers(types, range.start(), range.end(), PageRequest.of(0, safeLimit)), reportTaskExecutor);

        CompletableFuture<List<SecurityEventIpCountDTO>> topIpsFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.findTopIps(types, range.start(), range.end(), PageRequest.of(0, safeLimit)), reportTaskExecutor);

        CompletableFuture.allOf(totalsFuture, topUsersFuture, topIpsFuture).join();

        List<SecurityEventCountDTO> totals = totalsFuture.join();
        long totalEvents = totals.stream().mapToLong(SecurityEventCountDTO::getCount).sum();

        SecurityTokenUsageReportDTO report = new SecurityTokenUsageReportDTO();
        report.setTotalsByType(totals);
        report.setTopUsers(topUsersFuture.join());
        report.setTopIps(topIpsFuture.join());
        report.setTotalEvents(totalEvents);

        return new ResponseDTO<>("success", "Token usage report", report);
    }

    @GetMapping("/brute-force")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<SecurityBruteForceReportDTO> bruteForceReport(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        InstantRange range = resolveRange(start, end);
        int safeLimit = Math.max(1, Math.min(limit, 50));
        Set<SecurityEventType> failureTypes = Set.of(SecurityEventType.LOGIN_FAILURE);

        CompletableFuture<Long> alertCountFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.countByEventTypeAndCreatedAtBetween(
                        SecurityEventType.BRUTE_FORCE_ALERT, range.start(), range.end()), reportTaskExecutor);

        CompletableFuture<List<SecurityEventUserCountDTO>> topUsersFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.findTopUsers(failureTypes, range.start(), range.end(), PageRequest.of(0, safeLimit)), reportTaskExecutor);

        CompletableFuture<List<SecurityEventIpCountDTO>> topIpsFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.findTopIps(failureTypes, range.start(), range.end(), PageRequest.of(0, safeLimit)), reportTaskExecutor);

        CompletableFuture.allOf(alertCountFuture, topUsersFuture, topIpsFuture).join();

        SecurityBruteForceReportDTO report = new SecurityBruteForceReportDTO();
        report.setAlertCount(alertCountFuture.join());
        report.setTopUsernames(topUsersFuture.join());
        report.setTopIps(topIpsFuture.join());

        return new ResponseDTO<>("success", "Brute force report", report);
    }

    @GetMapping("/endpoint-frequency")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<SecurityEndpointAccessReportDTO> endpointAccessFrequency(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        InstantRange range = resolveRange(start, end);
        int safeLimit = Math.max(1, Math.min(limit, 50));

        CompletableFuture<List<SecurityEventEndpointCountDTO>> topEndpointsFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.findTopEndpoints(range.start(), range.end(), PageRequest.of(0, safeLimit)), reportTaskExecutor);

        CompletableFuture<Long> totalEventsFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.countByCreatedAtBetween(range.start(), range.end()), reportTaskExecutor);

        CompletableFuture<Long> uniqueEndpointsFuture = CompletableFuture.supplyAsync(
                () -> securityEventRepository.countDistinctEndpoints(range.start(), range.end()), reportTaskExecutor);

        CompletableFuture.allOf(topEndpointsFuture, totalEventsFuture, uniqueEndpointsFuture).join();

        SecurityEndpointAccessReportDTO report = new SecurityEndpointAccessReportDTO();
        report.setTopEndpoints(topEndpointsFuture.join());
        report.setTotalEvents(totalEventsFuture.join());
        report.setUniqueEndpoints(uniqueEndpointsFuture.join());

        return new ResponseDTO<>("success", "Endpoint access frequency report", report);
    }

    private InstantRange resolveRange(String start, String end) {
        Instant now = Instant.now();
        Instant endInstant = (end != null && !end.isBlank()) ? Instant.parse(end) : now;
        Instant startInstant = (start != null && !start.isBlank())
                ? Instant.parse(start)
                : endInstant.minus(DEFAULT_WINDOW);
        return new InstantRange(startInstant, endInstant);
    }

    private record InstantRange(Instant start, Instant end) {
    }
}
