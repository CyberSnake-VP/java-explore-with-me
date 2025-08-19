package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipantRequestDto;

public interface RequestService {
    ParticipantRequestDto addRequest(Long userId, Long eventId);
}
