package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipantRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantRequestDto postRequest(@PathVariable("userId") Long userId,
                                             @RequestParam("eventId") Long eventId) {
        log.info("POST request for user {} with event id {}", userId, eventId);
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipantRequestDto> getRequests(@PathVariable("userId") Long userId) {
        log.info("GET requests for user {}", userId);
        return requestService.getRequest(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipantRequestDto rejectRequest(@PathVariable("userId") Long userId,
                                               @PathVariable("requestId") Long requestId) {
        log.info("PATCH request reject for user {} with request id {}", userId, requestId);
        return requestService.rejectRequest(userId, requestId);
    }
}
