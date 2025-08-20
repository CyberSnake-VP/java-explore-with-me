package ru.practicum.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.*;
import ru.practicum.request.dto.ParticipantRequestDto;
import ru.practicum.request.dto.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public EventFullDto addEventByUserPrivate(NewEventDto event, Long userId) {
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
        if (validateDate(event)) {
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
    public List<EventShortDto> getEventsByUserPrivate(Long userId, Pageable pageable) {
        log.info("Get all events by user: {}, pageable: {}", userId, pageable);

        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(event -> EventMapper.mapToShortDto(event, getEventHitView(event)))
                .toList();
    }

    @Override
    public EventFullDto getEventByUserPrivate(Long eventId, Long userId) {
        log.info("Get event: {}, by user: {}", eventId, userId);

        Event entity = eventRepository.findByInitiatorIdAndId(userId, eventId);

        return EventMapper.mapToFullDto(entity, getEventHitView(entity));
    }

    @Override
    public EventFullDto updateEventByUserPrivate(UpdateEventUserRequest event, Long userId, Long eventId) {
        log.info("Update event: {}, by user: {}", eventId, userId);

        Event entity = eventRepository.findByInitiatorIdAndId(userId, eventId);
        if (entity == null) {
            throw getNotFoundException(eventId);
        }
        if (entity.getState().equals(State.PUBLISHED)) {
            throw new UpdateEventException("Event must not be published");
        }
        /** Немного не понял этот момент, при добавлении event, у него будет статус pending(ожидание), а при обновлении
         * можно обновлять только не опубликованные, т.е. не подтвержденные события, можно поменять статус на canceled,
         * а sendToReview нужен если событие отменено и пользователь снова бросает его в ожидание?*/
        if (event.getStateAction() != null) {
            switch (event.getStateAction()) {
                case SEND_TO_REVIEW -> entity.setState(State.PENDING);
                case CANCEL_REVIEW -> entity.setState(State.CANCELED);
                default -> throw new UpdateEventStatusException("Only pending or canceled events can be changed");
            }
        }
        if (event.getAnnotation() != null) {
            entity.setAnnotation(event.getAnnotation());
        }
        /** Тут проверяем категорию, если ее нужно заменить, проверяем на корректность.
         * Категория обязательна, если она указана не верно, не стоит ее менять.*/
        if (event.getCategory() != null) {
            entity.setCategory(categoryRepository.findById(event.getCategory()).orElseThrow(() -> {
                String reason = "The required object was not found.";
                String message = String.format("Category with id=%d was not found", event.getCategory());
                return new NotFoundException(message, reason);
            }));
        }
        if (event.getDescription() != null) {
            entity.setDescription(event.getDescription());
        }
        if (event.getEventDate() != null) {
            if (validateDate(event)) {
                entity.setEventDate(event.getEventDate());
            } else {
                throw getDateValidationException(event.getEventDate());
            }
        }
        if (event.getLocation() != null) {
            Location location = event.getLocation();
            if (location.getLat() != null) {
                entity.setLat(location.getLat());
            }
            if (location.getLon() != null) {
                entity.setLon(location.getLon());
            }
        }
        if (event.getPaid() != null) {
            entity.setPaid(event.getPaid());
        }
        if (event.getParticipantLimit() != null) {
            entity.setParticipantLimit(event.getParticipantLimit());
        }
        if (event.getRequestModeration() != null) {
            entity.setRequestModeration(event.getRequestModeration());
        }
        if (event.getTitle() != null) {
            entity.setTitle(event.getTitle());
        }

        log.info("Saved event with id {}", entity.getId());

        return EventMapper.mapToFullDto(eventRepository.save(entity), getEventHitView(entity));
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(GetEventAdminRequest req) {
        log.info("Get events by admin: {}", req);
        // Для поиска ссылок используем QueryDSL чтобы было удобно настраивать разные варианты фильтров
        QEvent event = QEvent.event;
        // Мы будем анализировать какие фильтры указал пользователь
        // И все нужные условия фильтрации будем собирать в список
        List<BooleanExpression> conditions = new ArrayList<>();
        if (req.getUserIds() != null) {
            conditions.add(event.id.in(req.getUserIds()));
        }
        if (req.getStates() != null) {
            conditions.add(event.state.in(req.getStates()));
        }
        if (req.getCategoryIds() != null) {
            conditions.add(event.category.id.in(req.getCategoryIds()));
        }
        if (req.getRangeStart() != null) {
            conditions.add(event.eventDate.after(req.getRangeStart()));
        }
        if (req.getRangeEnd() != null) {
            conditions.add(event.eventDate.before(req.getRangeEnd()));
        }

        // из всех подготовленных условий, составляем единое условие
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElse(Expressions.TRUE);

        // решил использовать сортировку по полю id события
        Sort sort = Sort.by("id");
        PageRequest pageRequest = PageRequest.of(req.getFrom(), req.getSize(), sort);

        log.info("Get events by user: {}, page: {}", req.getUserIds(), pageRequest);
        List<Event> events = eventRepository.findAll(finalCondition, pageRequest).getContent();

        log.info("Found {} events", events.size());
        // получаем список событий с количеством просмотров, я выбрал период год в методе getEventHitView
        return events.stream()
                .map(e -> EventMapper.mapToFullDto(e, getEventHitView(e)))
                .toList();
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest event, Long eventId) {
        log.info("Update event: c ID: {}", eventId);
        Event entity = eventRepository.findById(eventId).orElseThrow(() -> getNotFoundException(eventId));
        if (event.getAnnotation() != null) {
            entity.setAnnotation(event.getAnnotation());
        }
        if (event.getCategory() != null) {
            entity.setCategory(categoryRepository.findById(event.getCategory()).orElseThrow(() -> {
                String reason = "The required object was not found.";
                String message = String.format("Category with id=%d was not found", event.getCategory());
                return new NotFoundException(message, reason);
            }));
        }
        if (event.getDescription() != null) {
            entity.setDescription(event.getDescription());
        }
        if (event.getEventDate() != null) {
            if (validateDateUpdateAdmin(event, entity)) {
                entity.setEventDate(event.getEventDate());
            } else {
                throw getDateValidationException(event.getEventDate());
            }
        }
        if (event.getPaid() != null) {
            entity.setPaid(event.getPaid());
        }
        if (event.getParticipantLimit() != null) {
            entity.setParticipantLimit(event.getParticipantLimit());
        }
        if (event.getRequestModeration() != null) {
            entity.setRequestModeration(event.getRequestModeration());
        }
        if (event.getTitle() != null) {
            entity.setTitle(event.getTitle());
        }
        if (event.getLocation() != null) {
            Location location = event.getLocation();
            if (location.getLat() != null) {
                entity.setLat(location.getLat());
            }
            if (location.getLon() != null) {
                entity.setLon(location.getLon());
            }
        }
        log.info("Updated event by Admin with id {}", entity.getId());
        /** Cобытие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
         событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)*/
        if (event.getStateAction() != null) {
            switch (event.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (entity.getState().equals(State.PENDING)) {
                        entity.setState(State.PUBLISHED);
                    } else {
                        throw getUpdateEventStatusException(entity.getState());
                    }
                }
                case REJECT_EVENT -> {
                    if (entity.getState().equals(State.PENDING)) {
                        entity.setState(State.CANCELED);
                    } else {
                        throw getUpdateEventStatusException(entity.getState());
                    }
                }
            }
        }

        return EventMapper.mapToFullDto(eventRepository.save(entity), getEventHitView(entity));
    }

    @Override
    public List<EventShortDto> getEvents(GetEventRequest req, HttpServletRequest servlet) {
        log.info("Get events by admin: {}", req);

        // добавить в сервис статистики с помощью клиента данные о просмотре
        addHitEvent(servlet);
        // Так же для формирования запросов по фильтрам используем QueryDSL, все фильтры складываем в список Expression
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        // раз метод публичный, получаем только опубликованные события
        conditions.add(event.state.eq(State.PUBLISHED));

        // ищем текст в аннотации и подробном описании, без учета регистра
        if (req.getText() != null) {
            conditions.add(event.annotation.containsIgnoreCase(req.getText())
                    .or(event.description.containsIgnoreCase(req.getText())));
        }
        if (req.getCategoriesIds() != null) {
            conditions.add(event.category.id.in(req.getCategoriesIds()));
        }
        if (req.getPaid() != null) {
            conditions.add(event.paid.eq(req.getPaid()));
        }
        if (req.getRangeStart() != null) {
            conditions.add(event.eventDate.after(req.getRangeStart()));
        }
        if (req.getRangeEnd() != null) {
            conditions.add(event.eventDate.before(req.getRangeEnd()));
        }
        if (req.getOnlyAvailable()) {
            conditions.add(event.confirmedRequest.lt(event.participantLimit));
        }

        // из всех подготовленных условий, составляем единое условие. Если ни одного фильтра не получили, используем
        // Expressions.TRUE, устанавливаем значение true,  предикат не может быть пустым.
        BooleanExpression request = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElse(Expressions.TRUE);

        Sort sort = Sort.by("id");
        if (req.getSort() != null) {
            sort = makeOrderBySort(req.getSort());
        }
        PageRequest pageRequest = PageRequest.of(req.getFrom(), req.getSize(), sort);

        log.info("Get events page: {}", pageRequest);
        List<Event> events = eventRepository.findAll(request, pageRequest).getContent();

        log.info("Found events, size:{}", events.size());
        return events.stream()
                .map(e -> EventMapper.mapToShortDto(e, getEventHitView(e)))
                .toList();
    }

    @Override
    public EventFullDto getEvent(Long eventId, HttpServletRequest servlet) {
        log.info("Get event: {}", eventId);
        // отправляем в сервис статистики данные, через клиента.
        addHitEvent(servlet);

        Event entity = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> getNotFoundException(eventId));
        log.info("event: {}", entity);
        return EventMapper.mapToFullDto(entity, getEventHitView(entity));
    }

    @Override
    public List<ParticipantRequestDto> getRequestByUserEvent(Long userId, Long eventId) {
        log.info("Get request at user: {}, event: {}", userId, eventId);
        // Проверяю, существует ли событие
        Event eventEntity = eventRepository.findById(eventId).orElseThrow(() -> getNotFoundException(eventId));
        // Проверка на то, чтобы пользователь был инициатор для данного события.
        if (!Objects.equals(eventEntity.getInitiator().getId(), userId)) {
            throw new ValidationException("Did not initiator with id " + userId + " for event with id " + eventId);
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::mapToDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest req) {
        log.info("Update event request status for user: {}, event: {}, request status: {}", userId, eventId, req.getStatus());
        List<Request> requestsEntity = requestRepository.findByIdIn(req.getRequestIds());
        userRepository.findById(userId).orElseThrow(() -> getNotFoundException(userId));
        Event eventEntity = eventRepository.findById(eventId).orElseThrow(() -> getNotFoundException(eventId));
        // количество участников = количеству заявок
        int participantCount = requestsEntity.size();

        List<ParticipantRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipantRequestDto> rejectedRequests = new ArrayList<>();

        // Проверяем событие на количество участников, если неограниченно, можно ставить статус CONFIRMED, а так же модерация не нужна.
        // Хотя при создании заявки, уст. статус в CONFIRMED, если модерация отключена, все таки включил это условие в это условие
        // Повторно уст-ся статус, но зато запишем кол-во участников и сформируем dto, избежим повторение кода.
        if(eventEntity.getParticipantLimit() == 0 || (eventEntity.getRequestModeration() == false)) {
            log.info("Request moderation is false or participant limit is zero");
            // устанавливаем кол-во участников, а так же записываем все заявки в список подверженных
            eventEntity.setConfirmedRequest(eventEntity.getConfirmedRequest() + participantCount);
            requestsEntity.forEach(request -> {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(RequestMapper.mapToDto(request));
            });
            eventRepository.save(eventEntity);
            requestRepository.saveAll(requestsEntity);
            return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
        }

        // превышен лимит на кол-во участников
        if(eventEntity.getConfirmedRequest() == eventEntity.getParticipantLimit().longValue()) {
            log.warn("Request limit is greater than participant limit");
            throw new ValidationException("Participant limit exceeded");
        }

        // У заявок для пре-модерации должен быть статус PENDING
        requestsEntity.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.PENDING))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Request must have status PENDING"));


        /** Логика установки статуса заявкам. Пробегаемся циклом по заявкам, если лимит участников не превышен,
         * устанавливаем статус заявке CONFIRMED, записываем в событие участника(прибавляем кол-во)
         * и вносим заявку в подготовленный список dto: ConfirmedRequests, если при добавлении участника к событию
         * будет превышен лимит, то остальные заявки на участие будут отклонены, и записаны в список dto: RejectedRequests*/
        if(req.getStatus() != null) {
            switch (req.getStatus()) {
                case CONFIRMED: {
                    requestsEntity.forEach(request -> {
                        if(eventEntity.getParticipantLimit() > eventEntity.getConfirmedRequest() + 1) {
                            request.setStatus(RequestStatus.CONFIRMED);
                            eventEntity.setConfirmedRequest(eventEntity.getConfirmedRequest() + 1);
                            confirmedRequests.add(RequestMapper.mapToDto(request));
                        } else {
                            request.setStatus(RequestStatus.REJECTED);
                            rejectedRequests.add(RequestMapper.mapToDto(request));
                        }
                    });
                }
                case REJECTED: {
                    requestsEntity.forEach(request -> {
                        request.setStatus(RequestStatus.REJECTED);
                        rejectedRequests.add(RequestMapper.mapToDto(request));
                    });
                }
            }
        }

        eventRepository.save(eventEntity);
        requestRepository.saveAll(requestsEntity);
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    // метод будет возвращать нужный вид сортировки.
    private Sort makeOrderBySort(GetEventRequest.Sort sort) {
        return switch (sort) {
            case EVENT_DATE -> Sort.by("eventDate");
            case VIEWS -> Sort.by("views");
        };
    }

    // сделал универсальный метод для получения валидации для разных объектов dto
    private <T> boolean validateDate(T event) {
        LocalDateTime afterTwoHour = LocalDateTime.now().plusHours(2);

        if (event.getClass().equals(UpdateEventUserRequest.class)) {
            return ((UpdateEventUserRequest) event).getEventDate().isAfter(afterTwoHour);
        }
        if (event.getClass().equals(NewEventDto.class)) {
            return ((NewEventDto) event).getEventDate().isAfter(afterTwoHour);
        }
        return false;
    }

    private boolean validateDateUpdateAdmin(UpdateEventAdminRequest event, Event enitiy) {
        // дата публикации
        LocalDateTime publishedOn = enitiy.getPublishedOn();
        // дата изменяемого события
        LocalDateTime eventDate = event.getEventDate();
        /** Дата начала изменяемого события должна быть не ранее чем за час от даты публикации*/
        return publishedOn.plusHours(1).isBefore(eventDate);

    }

    /**
     * Для удобства вынес подготовку исключения в отдельные методы.
     */
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

    private UpdateEventStatusException getUpdateEventStatusException(State action) {
        log.info("Update status failed: {}", action);
        String message = String.format("Cannot publish the event because it's not in the right state: %s",
                action);
        return new UpdateEventStatusException(message);
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
        if (!views.isEmpty()) {
            return views.getFirst().getHits();
        }
        return view;
    }

    // метод для записи в сервис статистики данных о просмотрах событий
    private void addHitEvent(HttpServletRequest servlet) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(servlet.getRequestURI())
                .ip(servlet.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.addHit(hitDto);
    }
}
