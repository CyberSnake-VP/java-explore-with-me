package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public EventFullDto addEvent(NewEventDto event, Long userId) {
        log.info("Add event: {}, by user: {}", event.getTitle(), userId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            String reason = "The required object was not found.";
            String message = String.format("User with id=%d  was not found", userId);
            return new NotFoundException(message, reason);
        });

        Category category = categoryRepository.findById(event.getCategory()).orElseThrow(() -> {
            String reason = "The required object was not found.";
            String message = String.format("Category with id=%d was not found", event.getCategory());
            return new NotFoundException(message, reason);
        });

        /** Проверяем даты на валидность, выбрасываем исключение, если проверка не прошла.*/
        if(validateDate(event)) {
            Event entity = EventMapper.mapToEntity(event, user, category);
            entity = eventRepository.save(entity);
            log.info("Saved event: с id {}, title {}", entity.getId(), entity.getTitle());
            // событие только создается, поэтому пишем просмотров 0
            return EventMapper.mapToFullDto(entity, 0L);
        } else {
            throw getDateValidationException(event.getEventDate());
        }
    }

    private boolean validateDate(NewEventDto event) {
        /** Событие не должно быть раньше, чем за два часа, от текущего времени.*/
        LocalDateTime afterTwoHour = LocalDateTime.now().plusHours(2);
        return event.getEventDate().isAfter(afterTwoHour);
    }

    private DateValidationException getDateValidationException(LocalDateTime eventDate) {
        /** Формируем сообщение для ошибки*/
        log.info("Event date is after two hours: {}", eventDate);
        String message = String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                "Value: %s", eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return new DateValidationException(message);
    }

}
