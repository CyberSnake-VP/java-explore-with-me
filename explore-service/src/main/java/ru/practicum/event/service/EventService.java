package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;

import java.util.List;

public interface EventService {
    EventFullDto addEventByUserIdPrivate(NewEventDto event, Long userId);
    List<EventShortDto> getEventsByUserIdPrivate(Long userId, Pageable pageable);
    EventFullDto getEventByUserIdPrivate(Long eventId, Long userId);
    EventFullDto updateEventByUserIdPrivate(UpdateEventUserRequest event, Long userId, Long eventId);
    List<EventFullDto> getEventsByAdmin(GetEventRequest req);
    EventFullDto updateEventByAdmin(UpdateEventAdminRequest event, Long eventId);
}
