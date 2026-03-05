package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SecurityEndpointAccessReportDTO {
    private List<SecurityEventEndpointCountDTO> topEndpoints;
    private long totalEvents;
    private long uniqueEndpoints;
}

