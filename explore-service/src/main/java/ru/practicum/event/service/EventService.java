package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    EventFullDto addEventByUserIdPrivate(NewEventDto event, Long userId);
    List<EventShortDto> getEventsByUserIdPrivate(Long userId, Pageable pageable);
    EventFullDto getEventByUserIdPrivate(Long eventId, Long userId);
    EventFullDto updateEventByUserIdPrivate(UpdateEventUserRequest event, Long userId, Long eventId);
}
