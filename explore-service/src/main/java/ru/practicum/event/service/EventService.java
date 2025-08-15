package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;

import java.util.List;

public interface EventService {
    EventFullDto addEventPrivate(NewEventDto event, Long userId);
    List<EventShortDto> getEventsPrivate(Long userId, Pageable pageable);
    EventFullDto getEventPrivate(Long eventId, Long userId);
}
