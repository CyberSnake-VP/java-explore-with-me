package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            eventsShort = getEventShortDto(eventsEntity); // получаем список событий с кол-вом просмотров для обратного dto
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
            eventsShort = getEventShortDto(eventsEntity);   // заменил получение данных с учетом статистики из бд
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
        List<EventShortDto> eventsShort = getEventShortDto(eventsEntity);
        return CompilationMapper.mapToCompilationDto(entity, eventsShort);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        log.info("Get list compilations: {}, with pinned: {}", pageable, pinned);
        // если есть флаг закрепленных или незакрепленных подборок, то ищем с учетом фильтра и pageable
        if (pinned != null) {
            List<Compilation> compilationsEntity = compilationRepository.findAllByPinned(pinned, pageable);
            return getCompilationDtoList(compilationsEntity);
        } else {
            List<Compilation> compilationsEntity = compilationRepository.findAll(pageable).getContent();
            return getCompilationDtoList(compilationsEntity);
        }
    }

    private NotFoundException getNotFoundException(Long id) {
        log.info("Compilation not found with id: {}", id);
        String reason = "The required object was not found.";
        String message = String.format("Compilation with id=%d was not found", id);
        return new NotFoundException(message, reason);
    }

    private List<CompilationDto> getCompilationDtoList(List<Compilation> compilations) {
        List<CompilationDto> compilationsDto = new ArrayList<>();
        List<EventShortDto> eventsShort;
        for (Compilation c : compilations) {
            eventsShort = getEventShortDto(c.getEvents());  // переделал метод получения данных из бд статистики.
            compilationsDto.add(CompilationMapper.mapToCompilationDto(c, eventsShort));
        }
        return compilationsDto;
    }

    /**
     * Метод для получения списка объектов EventShortDto c количеством просмотров, для формирования обратного dto.
     * Идея в том, что теперь мы будем формировать список из uri каждого события из входящего списка событий.
     * Теперь один раз получаем статистику по этим uri, возьмем от туда кол-во просмотров конкретного события по uri
     */
    private List<EventShortDto> getEventShortDto(List<Event> events) {

        // формируем список для последующей отправки запроса клиента
        log.info("Get events short: {}", events);
        List<String> uris = new ArrayList<>();
        for (Event e : events) {
            log.info("date createdOn on event {}", e.getCreatedOn());
            uris.add("/events/" + e.getId());
        }

        LocalDateTime start = LocalDateTime.now().minusDays(365);

        // поиск будем вести до текущей даты
        LocalDateTime end = LocalDateTime.now();

        // получим статистику за все события.
        List<ViewStatsDto> views = statsClient.getStats(start, end, uris, false);

        // Таблица с событием и его кол-ом просмотров.
        Map<Event, Long> hits = new HashMap<>();

        /** Запускаем перебор по списку событий, используем id конкретного события
         * и запишем это событие в случае совпадения в таблицу, где ключ будет событие и в качестве зн-я будет кол-во просмотров
         * Если в сервисе статистики события еще нет, то запишем в качестве кол-ва просмотров "0" */
        if (!events.isEmpty()) {
            for (Event e : events) {
                views.stream()
                        .filter(v -> v.getUri().equals("/events/" + e.getId()))
                        .findFirst()
                        .ifPresentOrElse(v -> hits.put(e, v.getHits()), () -> hits.put(e, 0L));
            }
        }
        /** Возвращаем список уже подготовленных объектов EventShortDto c количеством просмотров, для формирования обратного dto.
         * Используем нашу таблицу hits, получаем объект EventShortDto, в его параметрах, событие и кол-во просмотров.
         * Берем эти данные из нашей подготовленной таблицы hits.
         * */
        return hits.entrySet().stream()
                .map(entry -> EventMapper.mapToShortDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}
