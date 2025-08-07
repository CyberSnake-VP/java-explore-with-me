package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.EndpointHitDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.stats.repository.StatsRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public EndpointHitDto addEndpoint(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit =EndpointHitMapper.mapToEndpointHit(endpointHitDto);
        return EndpointHitMapper.mapToEndpointHitDto(statsRepository.save(endpointHit));
    }
}
