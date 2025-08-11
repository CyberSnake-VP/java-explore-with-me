package ru.practicum.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@ActiveProfiles(profiles = {"test"})
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatsServiceImplTest {
    private final StatsService statsService;
    private final EntityManager entityManager;

    @Test
    void testAddEndpoint() {
        EndpointHitDto endpointHitDto = makeEndpointHitDto("ewm-main-service", "192.163.0.1", "/events/1", "2025-01-01 00:00:00");

        statsService.addEndpoint(endpointHitDto);

        TypedQuery<EndpointHit> query = entityManager.createQuery("SELECT e FROM EndpointHit e WHERE e.app = :app", EndpointHit.class);
        EndpointHit endpointHit = query.setParameter("app", endpointHitDto.getApp()).getSingleResult();

        assertThat(endpointHit.getId(), notNullValue());
        assertThat(endpointHit.getTimestamp(), equalTo(endpointHitDto.getTimestamp()));
        assertThat(endpointHit.getIp(), equalTo(endpointHitDto.getIp()));
        assertThat(endpointHit.getUri(), equalTo(endpointHitDto.getUri()));
        assertThat(endpointHit.getApp(), equalTo(endpointHitDto.getApp()));
        assertThat(endpointHit.getTimestamp(), equalTo(endpointHitDto.getTimestamp()));
    }

    @Test
    void testGetStats() {
        List<EndpointHitDto> endpointHits = List.of(
                makeEndpointHitDto("ewm-main-service", "192.163.0.1", "/events/1", "2025-02-01 00:00:00"),
                makeEndpointHitDto("ewm-main-service", "192.163.0.1", "/events/1", "2025-03-01 00:00:00"),
                makeEndpointHitDto("ewm-main-service", "192.163.0.1", "/events/1", "2025-04-01 00:00:00")
        );

        for (EndpointHitDto endpointHitDto : endpointHits) {
            EndpointHit entity = EndpointHitMapper.mapToEndpointHit(endpointHitDto);
            entityManager.persist(entity);
        }
        entityManager.flush();

        List<ViewStatsDto> result = statsService.getStats(LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0), List.of(), false);

        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(1));
        for (EndpointHitDto endpointHitDto : endpointHits) {
            assertThat(result, hasItem(allOf(
                    hasProperty("app", is(endpointHitDto.getApp())),
                    hasProperty("uri", is(endpointHitDto.getUri())),
                    hasProperty("hits", is(3L))
            )));
        }
    }


    private EndpointHitDto makeEndpointHitDto(String app, String ip, String uri, String timestamp) {
        String format = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return EndpointHitDto.builder()
                .app(app)
                .ip(ip)
                .timestamp(LocalDateTime.parse(timestamp, formatter))
                .uri(uri)
                .build();
    }
}