package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SecurityBruteForceReportDTO {
    private long alertCount;
    private List<SecurityEventUserCountDTO> topUsernames;
    private List<SecurityEventIpCountDTO> topIps;
}

