package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.ParticipantRequestDto;
import ru.practicum.request.dto.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ParticipantRequestDto addRequest(Long userId, Long eventId) {
        log.info("Add Request with userId: {}, eventId: {}", userId, eventId);
        Event eventEntity = eventRepository.findById(eventId).orElseThrow(() -> getNotFoundException(eventId, "Event"));
        User userEntity = userRepository.findById(userId).orElseThrow(() -> getNotFoundException(userId, "User"));
        Request requestEntity = requestRepository.findRequestByEventIdAndRequesterId(eventId, userId);

        // проверим чтобы событие было опубликованным.
        if (!eventEntity.getState().equals(State.PUBLISHED)) {
            throw new ValidationException("Request is not published");
        }
        // проверим, чтобы запрос не создавался второй раз пользователем на то же событие
        if (requestEntity != null) {
            throw new ValidationException("Request already exists");
        }
        // проверим чтобы тот, кто создал событие, не подавал заявку на участие в нем.
        if (Objects.equals(eventEntity.getInitiator().getId(), userId)) {
            throw new ValidationException("Requestor cannot be the initiator of the event");
        }
        // проверка на количество участников
        if (eventEntity.getConfirmedRequests() == eventEntity.getParticipantLimit().longValue()) {
            throw new ValidationException("participant limit exceeded");
        }
        // проверим модерацию
        boolean isModeration = eventEntity.getRequestModeration();
        // заполняем сущность request
        requestEntity = Request.builder()
                .requester(userEntity)
                .event(eventEntity)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        // если модерация включена, то статус у запроса на участие будет PENDING на рассмотрении иначе сразу CONFIRMED
        if (isModeration) {
            return RequestMapper.mapToDto(requestRepository.save(requestEntity));
        } else {
            // увеличиваем кол-во участников у события, раз модерация не нужна.
            eventEntity.setConfirmedRequests(eventEntity.getConfirmedRequests() + 1);
            eventRepository.save(eventEntity);
            requestEntity.setStatus(RequestStatus.CONFIRMED);
            return RequestMapper.mapToDto(requestRepository.save(requestEntity));
        }
    }

    @Override
    public List<ParticipantRequestDto> getRequest(Long userId) {
        log.info("Get Request with userId: {}", userId);
        userRepository.findById(userId).orElseThrow(() -> getNotFoundException(userId, "User"));
        List<Request> requestsEntity = requestRepository.findRequestByRequesterId(userId);
        return requestsEntity.stream().map(RequestMapper::mapToDto).toList();
    }

    @Transactional
    @Override
    public ParticipantRequestDto rejectRequest(Long userId, Long requestId) {
        log.info("Reject Request with userId: {}, requestId: {}", userId, requestId);
        Request requestEntity = requestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(
                () -> getNotFoundException(requestId, "Request"));
        // Устанавливаем статус CANCELED, теперь нашу заявку не смогут принять.
        requestEntity.setStatus(RequestStatus.CANCELED);
        // возвращаем dto со статусом CANCELED
        return RequestMapper.mapToDto(requestRepository.save(requestEntity));
    }


    // формируем исключение
    private NotFoundException getNotFoundException(Long id, String nameEntity) {
        log.info("{} not found with id: {}", nameEntity, id);
        String reason = "The required object was not found.";
        String message = String.format("%s with id=%d was not found", nameEntity, id);
        return new NotFoundException(message, reason);
    }
}
