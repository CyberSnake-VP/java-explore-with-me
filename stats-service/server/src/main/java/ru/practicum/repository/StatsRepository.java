package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EndpointHit;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(distinct e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp BETWEEN :start and :end " +
            "and e.uri in :uris " +
            "group by e.app, e.uri " +
            "order by count(distinct e.ip) desc "
    )
    List<ViewStatsDto> findByUriWithIpUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp BETWEEN :start and :end " +
            "and e.uri in :uris " +
            "group by e.app, e.uri " +
            "order by count(e.ip) desc "
    )
    List<ViewStatsDto> findByUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp between :start and :end " +
            "group by e.app, e.uri " +
            "order by count (e.ip) desc "
    )
    List<ViewStatsDto> findByAllUri(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(distinct e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp between :start and :end " +
            "group by e.app, e.uri " +
            "order by count(distinct e.ip) desc "
    )
    List<ViewStatsDto> findByAllUriWithIpUnique(LocalDateTime start, LocalDateTime end);
}
