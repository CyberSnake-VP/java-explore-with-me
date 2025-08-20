package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipantRequestDto;

import java.util.List;

public interface EventService {
    EventFullDto addEventByUserPrivate(NewEventDto event, Long userId);
    List<EventShortDto> getEventsByUserPrivate(Long userId, Pageable pageable);
    EventFullDto getEventByUserPrivate(Long eventId, Long userId);
    EventFullDto updateEventByUserPrivate(UpdateEventUserRequest event, Long userId, Long eventId);
    List<EventFullDto> getEventsByAdmin(GetEventAdminRequest req);
    EventFullDto updateEventByAdmin(UpdateEventAdminRequest event, Long eventId);
    List<EventShortDto> getEvents(GetEventRequest req, HttpServletRequest httpServletRequest);
    EventFullDto getEvent(Long eventId, HttpServletRequest servlet);
    List<ParticipantRequestDto> getRequestByUserEvent (Long userId, Long eventId);
    EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest req);
}
