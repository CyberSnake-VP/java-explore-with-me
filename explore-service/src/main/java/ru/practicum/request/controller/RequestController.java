package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipantRequestDto;
import ru.practicum.request.service.RequestService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantRequestDto postRequest(@PathVariable("userId") Long userId,
                            @RequestParam("eventId") Long eventId){
        log.info("POST request for user {} with event id {}", userId, eventId);
        return requestService.addRequest(userId, eventId);
    }
}
