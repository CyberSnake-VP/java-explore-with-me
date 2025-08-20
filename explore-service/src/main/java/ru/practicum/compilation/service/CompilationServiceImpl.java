package ru.practicum.compilation.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.dto.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public CompilationDto addCompilationByAdmin(NewCompilationDto newCompilationDto) {
        log.debug("Add compilation: {}", newCompilationDto);

        // список для хранения сущности событий
        List<Event> eventsEntity = new ArrayList<>();
        // список для хранения объекта dto для возврата клиенту
        List<EventShortDto> eventsShort = new ArrayList<>();
        // сущность
        Compilation entity;

        /** Если во входящем dto есть список с id событий.
         * Получаем список сущностей событий, подготавливаем сущность для записи в бд,
         * заполняем возвращаемый список событий для возвратного dto */
        if (newCompilationDto.getEvents() != null) {
            eventsEntity = eventRepository.findAllById(newCompilationDto.getEvents());
            entity = CompilationMapper.mapToEntity(newCompilationDto, eventsEntity);
            eventsShort = eventsEntity.stream()
                    .map(e -> EventMapper.mapToShortDto(e, getEventHitView(e)))
                    .toList();
        } else {
            entity = CompilationMapper.mapToEntity(newCompilationDto, eventsEntity);
        }
        entity = compilationRepository.save(entity);
        return CompilationMapper.mapToCompilationDto(entity, eventsShort);
    }

    @Transactional
    @Override
    public void deleteCompilationByAdmin(Long compId) {
        log.info("Delete compilation: {}", compId);
        if (compilationRepository.existsById(compId)) {
            log.info("compilation: {}, deleted", compId);
            compilationRepository.deleteById(compId);
        } else {
            throw getNotFoundException(compId);
        }
    }

    @Transactional
    @Override
    public CompilationDto updateCompilationByAdmin(UpdateCompilationRequest requestDto, Long compId) {
        log.info("Update compilation: {}", requestDto);
        // ищем сущность по id
        Compilation entity = compilationRepository.findById(compId).orElseThrow(() -> getNotFoundException(compId));

        // подготавливаем список событий для обратного dto
        List<EventShortDto> eventsShort = new ArrayList<>();

        // проверяем на замену списка событий, если список не пуст, то получаем события по id из бд и записываем в сущность
        if (requestDto.getEvents() != null) {
            List<Event> eventsEntity = eventRepository.findAllById(requestDto.getEvents());
            entity.setEvents(eventsEntity);
            eventsShort = eventsEntity.stream()
                    .map(e -> EventMapper.mapToShortDto(e, getEventHitView(e)))
                    .toList();
        }
        if (requestDto.getPinned() != null) {
            entity.setPinned(requestDto.getPinned());
        }
        if (requestDto.getTitle() != null) {
            entity.setTitle(requestDto.getTitle());
        }
        entity = compilationRepository.save(entity);

        log.info("compilation: {}, updated", compId);
        return CompilationMapper.mapToCompilationDto(entity, eventsShort);
    }

    @Override
    public CompilationDto get(Long compId) {
        log.info("Get compilation: {}", compId);
        Compilation entity = compilationRepository.findById(compId).orElseThrow(() -> getNotFoundException(compId));
        List<Event> eventsEntity = entity.getEvents();
        // формируем список событий для обратного dto с учетом кол-ва просмотров
        List<EventShortDto> eventsShort = eventsEntity.stream()
                .map(e -> EventMapper.mapToShortDto(e, getEventHitView(e)))
                .toList();
        return CompilationMapper.mapToCompilationDto(entity, eventsShort);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        log.info("Get list compilations: {}, with pinned: {}", pageable, pinned);
        List<CompilationDto> compilationsDto = new ArrayList<>();
        // если есть флаг закрепленных или незакрепленных подборок, то ищем с учетом фильтра и pageable
        if (pinned != null) {
            List<Compilation> compilationsEntity = compilationRepository.findAllByPinned(pinned, pageable);
            return getCompilationDtoList(compilationsEntity);
        } else {
            List<Compilation> compilationsEntity = compilationRepository.findAll(pageable).getContent();
            return getCompilationDtoList(compilationsEntity);
        }
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

    private NotFoundException getNotFoundException(Long id) {
        log.info("Compilation not found with id: {}", id);
        String reason = "The required object was not found.";
        String message = String.format("Compilation with id=%d was not found", id);
        return new NotFoundException(message, reason);
    }

    /** Вспомогательный метод для получения списка объектов dto, думал поместить его в CompilationMapper, но
     * тут используется метод getEventHitView для получения из сервиса статистики данных о кол-ве просмотров.
     * Удобнее использовать его отсюда.*/
    private List<CompilationDto> getCompilationDtoList(List<Compilation> compilations ) {
        List<CompilationDto> compilationsDto = new ArrayList<>();
        List<EventShortDto> eventsShort;
        for (Compilation c : compilations) {
            eventsShort = c.getEvents().stream()
                    .map(e -> EventMapper.mapToShortDto(e, getEventHitView(e)))
                    .toList();
            compilationsDto.add(CompilationMapper.mapToCompilationDto(c, eventsShort));
        }
        return compilationsDto;
    }

}
