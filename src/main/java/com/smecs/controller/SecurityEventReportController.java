package com.smecs.controller;

import com.smecs.dto.ResponseDTO;
import com.smecs.dto.SecurityBruteForceReportDTO;
import com.smecs.dto.SecurityEndpointAccessReportDTO;
import com.smecs.dto.SecurityEventCountDTO;
import com.smecs.dto.SecurityEventEndpointCountDTO;
import com.smecs.dto.SecurityEventIpCountDTO;
import com.smecs.dto.SecurityEventUserCountDTO;
import com.smecs.dto.SecurityTokenUsageReportDTO;
import com.smecs.entity.SecurityEventType;
import com.smecs.repository.SecurityEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/security/reports")
public class SecurityEventReportController {

//    private static final int DEFAULT_LIMIT = 10;
    private static final Duration DEFAULT_WINDOW = Duration.ofDays(7);

    private final SecurityEventRepository securityEventRepository;

    @Autowired
    public SecurityEventReportController(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
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

        List<SecurityEventCountDTO> totals = securityEventRepository.countByEventType(types, range.start(), range.end());
        List<SecurityEventUserCountDTO> topUsers = securityEventRepository.findTopUsers(
                types, range.start(), range.end(), PageRequest.of(0, safeLimit));
        List<SecurityEventIpCountDTO> topIps = securityEventRepository.findTopIps(
                types, range.start(), range.end(), PageRequest.of(0, safeLimit));

        long totalEvents = totals.stream().mapToLong(SecurityEventCountDTO::getCount).sum();

        SecurityTokenUsageReportDTO report = new SecurityTokenUsageReportDTO();
        report.setTotalsByType(totals);
        report.setTopUsers(topUsers);
        report.setTopIps(topIps);
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

        long alertCount = securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEventType.BRUTE_FORCE_ALERT, range.start(), range.end());

        List<SecurityEventUserCountDTO> topUsers = securityEventRepository.findTopUsers(
                failureTypes, range.start(), range.end(), PageRequest.of(0, safeLimit));
        List<SecurityEventIpCountDTO> topIps = securityEventRepository.findTopIps(
                failureTypes, range.start(), range.end(), PageRequest.of(0, safeLimit));

        SecurityBruteForceReportDTO report = new SecurityBruteForceReportDTO();
        report.setAlertCount(alertCount);
        report.setTopUsernames(topUsers);
        report.setTopIps(topIps);

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

        List<SecurityEventEndpointCountDTO> topEndpoints = securityEventRepository.findTopEndpoints(
                range.start(), range.end(), PageRequest.of(0, safeLimit));
        long totalEvents = securityEventRepository.countByCreatedAtBetween(range.start(), range.end());
        long uniqueEndpoints = securityEventRepository.countDistinctEndpoints(range.start(), range.end());

        SecurityEndpointAccessReportDTO report = new SecurityEndpointAccessReportDTO();
        report.setTopEndpoints(topEndpoints);
        report.setTotalEvents(totalEvents);
        report.setUniqueEndpoints(uniqueEndpoints);

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

