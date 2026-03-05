package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SecurityTokenUsageReportDTO {
    private List<SecurityEventCountDTO> totalsByType;
    private List<SecurityEventUserCountDTO> topUsers;
    private List<SecurityEventIpCountDTO> topIps;
    private long totalEvents;
}

