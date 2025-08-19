package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEventUserPrivate(@PathVariable("userId") Long userId,
                                            @Valid @RequestBody NewEventDto event) {
        log.info("POST /users/{userId}/events/ userId={}, eventTitle={}", userId, event.getTitle());
        return eventService.addEventByUserPrivate(event, userId);
    }

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsUserPrivate(@PathVariable("userId") Long userId,
                                                    @RequestParam(name = "from") Integer from,
                                                    @RequestParam(name = "size") Integer size) {
        log.info("GET /users/{}/events/ from={}, size={}", userId, from, size);
        return eventService.getEventsByUserPrivate(userId, PageRequest.of(from, size));
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventUserPrivate(@PathVariable("userId") Long userId,
                                            @PathVariable("eventId") Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getEventByUserPrivate(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventUserPrivate(@RequestBody @Valid UpdateEventUserRequest event,
                                               @PathVariable("userId") Long userId,
                                               @PathVariable("eventId") Long eventId) {
        log.info("PATCH /users/{}/events/{}, event title:{}, id{}", userId, eventId, event.getTitle(), eventId);
        return eventService.updateEventByUserPrivate(event, userId, eventId);
    }

    @GetMapping("/admin/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventsByAdmin(@RequestParam(value = "users", required = false) List<Long> userIds,
                                               @RequestParam(value = "states", required = false) List<String> states,
                                               @RequestParam(value = "categories", required = false) List<Long> categoriesIds,
                                               @RequestParam(value = "rangeStart", required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                               @RequestParam(value = "rangeEnd", required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                               @RequestParam(value = "from", defaultValue = "0") Integer from,
                                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("GET /admin/events/ userIds={}, states={}, categoriesIds={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                userIds, states, categoriesIds, rangeStart, rangeEnd, from, size);
        return eventService.getEventsByAdmin(GetEventAdminRequest.of(
                userIds,
                states,
                categoriesIds,
                rangeStart,
                rangeEnd,
                from,
                size)
        );
    }

    @PatchMapping("/admin/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventsByAdmin(@RequestBody @Valid UpdateEventAdminRequest event,
                                            @PathVariable("eventId") Long eventId) {
        return eventService.updateEventByAdmin(event, eventId);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(value = "text", required = false) String text,
                                         @RequestParam(value = "categories", required = false) List<Long> categoriesIds,
                                         @RequestParam(value = "paid", required = false) Boolean paid,
                                         @RequestParam(value = "rangeStart", required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(value = "rangeEnd", required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(value = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(value = "sort", required = false) String sort,
                                         @RequestParam(value = "from", defaultValue = "0") Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size,
                                         HttpServletRequest servlet) {
        log.info("GET /events: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categoriesIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return eventService.getEvents(GetEventRequest.of(
                        text,
                        categoriesIds,
                        paid,
                        rangeStart,
                        rangeEnd,
                        onlyAvailable,
                        sort,
                        from,
                        size),
                servlet
        );
    }

    @GetMapping("events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable("eventId") Long eventId, HttpServletRequest servlet) {
        log.info("GET /events/{}", eventId);
        return eventService.getEvent(eventId, servlet);
    }
}
