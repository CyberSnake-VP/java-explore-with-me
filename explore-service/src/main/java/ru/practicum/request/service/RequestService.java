package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipantRequestDto;

import java.util.List;

public interface RequestService {
    ParticipantRequestDto addRequest(Long userId, Long eventId);

    List<ParticipantRequestDto> getRequest(Long userId);

    ParticipantRequestDto rejectRequest(Long userId, Long requestId);
}
