package com.smecs.repository;

import com.smecs.dto.SecurityEventCountDTO;
import com.smecs.dto.SecurityEventEndpointCountDTO;
import com.smecs.dto.SecurityEventIpCountDTO;
import com.smecs.dto.SecurityEventUserCountDTO;
import com.smecs.entity.SecurityEvent;
import com.smecs.entity.SecurityEventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    @Query("select new com.smecs.dto.SecurityEventCountDTO(e.eventType, count(e)) " +
           "from SecurityEvent e " +
           "where e.eventType in :eventTypes and e.createdAt between :start and :end " +
           "group by e.eventType")
    List<SecurityEventCountDTO> countByEventType(
            @Param("eventTypes") Collection<SecurityEventType> eventTypes,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("select new com.smecs.dto.SecurityEventUserCountDTO(e.userId, e.username, count(e)) " +
           "from SecurityEvent e " +
           "where e.eventType in :eventTypes and e.createdAt between :start and :end " +
           "group by e.userId, e.username " +
           "order by count(e) desc")
    List<SecurityEventUserCountDTO> findTopUsers(
            @Param("eventTypes") Collection<SecurityEventType> eventTypes,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    @Query("select new com.smecs.dto.SecurityEventIpCountDTO(e.ipAddress, count(e)) " +
           "from SecurityEvent e " +
           "where e.eventType in :eventTypes and e.createdAt between :start and :end " +
           "group by e.ipAddress " +
           "order by count(e) desc")
    List<SecurityEventIpCountDTO> findTopIps(
            @Param("eventTypes") Collection<SecurityEventType> eventTypes,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    @Query("select new com.smecs.dto.SecurityEventEndpointCountDTO(e.endpoint, count(e)) " +
           "from SecurityEvent e " +
           "where e.endpoint is not null and e.endpoint <> '' and e.createdAt between :start and :end " +
           "group by e.endpoint " +
           "order by count(e) desc")
    List<SecurityEventEndpointCountDTO> findTopEndpoints(
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    @Query("select count(distinct e.endpoint) " +
           "from SecurityEvent e " +
           "where e.endpoint is not null and e.endpoint <> '' and e.createdAt between :start and :end")
    long countDistinctEndpoints(
            @Param("start") Instant start,
            @Param("end") Instant end);

    long countByCreatedAtBetween(Instant start, Instant end);

    long countByEventTypeAndCreatedAtBetween(SecurityEventType eventType, Instant start, Instant end);
}
