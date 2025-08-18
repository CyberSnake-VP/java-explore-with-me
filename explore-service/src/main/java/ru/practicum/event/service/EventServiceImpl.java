package ru.practicum.event.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UpdateEventException;
import ru.practicum.exception.UpdateEventStatusException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public EventFullDto addEventByUserIdPrivate(NewEventDto event, Long userId) {
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

    @Override
    public List<EventShortDto> getEventsByUserIdPrivate(Long userId, Pageable pageable) {
        log.info("Get all events by user: {}, pageable: {}", userId, pageable);

        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
               .map(event -> EventMapper.mapToShortDto(event, getEventHitView(event)))
               .toList();
    }

    @Override
    public EventFullDto getEventByUserIdPrivate(Long eventId, Long userId) {
        log.info("Get event: {}, by user: {}", eventId, userId);

        Event entity = eventRepository.findByInitiatorIdAndId(userId, eventId);

        return EventMapper.mapToFullDto(entity, getEventHitView(entity));
    }

    @Override
    public EventFullDto updateEventByUserIdPrivate(UpdateEventUserRequest event, Long userId, Long eventId) {
        log.info("Update event: {}, by user: {}", eventId, userId);

        Event entity = eventRepository.findByInitiatorIdAndId(userId, eventId);
        if(entity == null) {
            throw  getNotFoundException(eventId);
        }
        if(entity.getState().equals(State.PUBLISHED)) {
            throw  new UpdateEventException("Event must not be published");
        }
        /** Немного не понял этот момент, при добавлении event, у него будет статус pending(ожидание), а при обновлении
         * можно обновлять только не опубликованные, т.е. не подтвержденные события, можно поменять статус на canceled,
         * а sendToReview нужен если событие отменено и пользователь снова бросает его в ожидание?*/
        if(event.getStateAction() != null) {
            switch (event.getStateAction()) {
                case SEND_TO_REVIEW -> entity.setState(State.PENDING);
                case CANCEL_REVIEW -> entity.setState(State.CANCELED);
                default -> throw new UpdateEventStatusException("Only pending or canceled events can be changed");
            }
        }
        if(event.getAnnotation() != null) {
            entity.setAnnotation(event.getAnnotation());
        }
        if(event.getCategory() != null) {
            entity.setCategory(categoryRepository.findById(event.getCategory()).orElseThrow());
        }
        if(event.getDescription() != null) {
            entity.setDescription(event.getDescription());
        }
        if(event.getEventDate() != null) {
            if(validateDate(event)) {
                entity.setEventDate(event.getEventDate());
            } else {
                throw getDateValidationException(event.getEventDate());
            }
        }
        if(event.getLocation() != null) {
            Location location = event.getLocation();
           if(location.getLat() != null) {
               entity.setLat(location.getLat());
           }
           if(location.getLon() != null) {
               entity.setLon(location.getLon());
           }
        }
        if(event.getPaid() != null) {
            entity.setPaid(event.getPaid());
        }
        if(event.getParticipantLimit() != null) {
            entity.setParticipantLimit(event.getParticipantLimit());
        }
        if(event.getRequestModeration() != null) {
            entity.setRequestModeration(event.getRequestModeration());
        }
        if(event.getTitle() != null) {
            entity.setTitle(event.getTitle());
        }

        log.info("Saved event with id {}", entity.getId());

        return EventMapper.mapToFullDto(eventRepository.save(entity), getEventHitView(entity));
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(GetEventRequest req) {
        log.info("Get events by admin: {}", req);
        // Для поиска ссылок используем QueryDSL чтобы было удобно настраивать разные варианты фильтров
        QEvent event = QEvent.event;
        // Мы будем анализировать какие фильтры указал пользователь
        // И все нужные условия фильтрации будем собирать в список
        List<BooleanExpression> conditions = new ArrayList<>();
        if(req.getUserIds() != null) {
            conditions.add(event.id.in(req.getUserIds()));
        }
        if(req.getStates() != null) {
            conditions.add(event.state.in(req.getStates()));
        }
        if(req.getCategoryIds() != null) {
            conditions.add(event.category.id.in(req.getCategoryIds()));
        }
        if(req.getRangeStart() != null) {
            conditions.add(event.eventDate.after(req.getRangeStart()));
        }
        if(req.getRangeEnd() != null) {
            conditions.add(event.eventDate.before(req.getRangeEnd()));
        }

        // из всех подготовленных условий, составляем единое условие
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElse(Expressions.TRUE);

        // решил использовать сортировку по полю id события
        Sort sort = Sort.by( "id");
        PageRequest pageRequest = PageRequest.of(req.getFrom(), req.getSize(), sort);

        log.info("Get events by user: {}, page: {}", req.getUserIds(), pageRequest);
        List<Event> events = eventRepository.findAll(finalCondition, pageRequest).getContent();

        log.info("Found {} events", events.size());
        // получаем список событий с количеством просмотров, я выбрал период год в методе getEventHitView
        return events.stream()
                .map(e -> EventMapper.mapToFullDto(e, getEventHitView(e)))
                .toList();
    }


    private boolean validateDate(NewEventDto event) {
        /** Событие не должно быть раньше, чем за два часа, от текущего времени.*/
        LocalDateTime afterTwoHour = LocalDateTime.now().plusHours(2);
        return event.getEventDate().isAfter(afterTwoHour);
    }

    private boolean validateDate(UpdateEventUserRequest event) {
        LocalDateTime afterTwoHour = LocalDateTime.now().plusHours(2);
        return event.getEventDate().isAfter(afterTwoHour);
    }

    /** Для удобства вынес подготовку исключения в отдельные методы.*/
    private DateValidationException getDateValidationException(LocalDateTime eventDate) {
        log.info("Event date is after two hours: {}", eventDate);
        String message = String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                "Value: %s", eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return new DateValidationException(message);
    }

    private NotFoundException getNotFoundException(Long id) {
        log.info("Event not found with id: {}", id);
        String reason = "The required object was not found.";
        String message = String.format("Event with id=%d was not found", id);
        return new NotFoundException(message, reason);
    }

    // Метод для получения кол-ва просмотров из сервиса статистики.
    // Не понятно за какой период получать статистику, указал за 365 дней.
    private Long getEventHitView(Event event) {
        // получим выборку в один год от текущей даты.
        LocalDateTime start = LocalDateTime.now().minusDays(365);
        LocalDateTime end = LocalDateTime.now();
        Long eventId = event.getId();
        List<String> uris = new ArrayList<>();
        uris.add("/events/" + eventId);
        List<ViewStatsDto> views = statsClient.getStats(start, end, uris, false);
        Long view = 0L;
        if(!views.isEmpty() ) {
            return views.getFirst().getHits();
        }
        return view;
    }

}
