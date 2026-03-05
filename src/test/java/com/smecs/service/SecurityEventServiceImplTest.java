package com.smecs.service;

import com.smecs.entity.SecurityEventType;
import com.smecs.repository.SecurityEventRepository;
import com.smecs.service.impl.SecurityEventServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityEventServiceImplTest {

    @Test
    void bruteForceAlertEmitsOnceAfterThreshold() {
        SecurityEventRepository repository = mock(SecurityEventRepository.class);
        SecurityEventServiceImpl service = new SecurityEventServiceImpl(repository);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        for (int i = 0; i < 6; i++) {
            service.recordLoginFailure("alice", request);
        }

        ArgumentCaptor<com.smecs.entity.SecurityEvent> events =
                ArgumentCaptor.forClass(com.smecs.entity.SecurityEvent.class);
        verify(repository, atLeast(7)).save(events.capture());

        long alertCount = events.getAllValues().stream()
                .filter(event -> event.getEventType() == SecurityEventType.BRUTE_FORCE_ALERT)
                .count();

        assertThat(alertCount).isEqualTo(1);
    }
}

