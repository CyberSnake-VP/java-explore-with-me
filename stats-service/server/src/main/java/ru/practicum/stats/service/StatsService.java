package ru.practicum.stats.service;

import ru.practicum.EndpointHitDto;

public interface StatsService {
    EndpointHitDto addEndpoint(EndpointHitDto endpointHitDto);
}
