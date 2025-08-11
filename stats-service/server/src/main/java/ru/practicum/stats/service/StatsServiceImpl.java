package ru.practicum.stats.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public EndpointHitDto addEndpoint(EndpointHitDto endpointHitDto) {
        log.info("save hit: {}", endpointHitDto);
        EndpointHit endpointHit = EndpointHitMapper.mapToEndpointHit(endpointHitDto);
        return EndpointHitMapper.mapToEndpointHitDto(statsRepository.save(endpointHit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        validDates(start, end);
        /** Проверяем список uri, если пуст или null, флаг будет false*/
        boolean isHasUris = !(Objects.isNull(uris) || uris.isEmpty());
        if (isHasUris) {
            log.info("uris {}", uris);
            if (unique) {
                log.info("by uris with unique ip {}", uris);
                return statsRepository.findByUriWithIpUnique(start, end, uris);
            } else {
                log.info("by uris without unique ip {}", uris);
                return statsRepository.findByUri(start, end, uris);
            }
        } else if (unique) {
            log.info("by all uris with unique ip {}", uris);
            return statsRepository.findByAllUriWithIpUnique(start, end);
        } else {
            log.info("by all uris without unique ip {}", uris);
            return statsRepository.findByAllUri(start, end);
        }
    }

    private void validDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException(String.format("Incorrected statistic period. Start: %s, End: %s", start, end));
        }
    }
}
