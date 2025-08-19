package ru.practicum.request.dto.mapper;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.request.dto.ParticipantRequestDto;
import ru.practicum.request.model.Request;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {
    public static ParticipantRequestDto mapToDto(Request entity) {
        return ParticipantRequestDto
                .builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .requester(entity.getRequester().getId())
                .event(entity.getEvent().getId())
                .created(entity.getCreated())
                .build();
    }
}
