package ru.practicum.event.dto.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.Location;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.user.dto.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    public static Event mapToEntity (NewEventDto newEventDto, User user, Category category) {
        return Event.builder()
                .eventDate(newEventDto.getEventDate())
                .annotation(newEventDto.getAnnotation())
                .confirmedRequest(0L)
                .category(category)
                .createdOn(LocalDateTime.now())
                .paid(newEventDto.getPaid() != null && newEventDto.getPaid())
                .title(newEventDto.getTitle())
                .description(newEventDto.getDescription())
                .lat(newEventDto.getLocation().getLat())
                .lon(newEventDto.getLocation().getLon())
                .participantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration())
                .state(State.PENDING)
                .initiator(user)
                .publishedOn(LocalDateTime.now())
                .build();
    }

    public static EventFullDto mapToFullDto (Event event, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToDto(event.getCategory()))
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequest())
                .createdOn(event.getCreatedOn())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .views(views == null ? 0 : views)
                .build();
    }
}
