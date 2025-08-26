package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.dto.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.utils.StatsClientUtil;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    // утилитарный класс с методами для получения статистики.
    private final StatsClientUtil statsClient;

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
            eventsShort = statsClient.getEventShortDto(eventsEntity); // получаем список событий с кол-вом просмотров для обратного dto
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
            eventsShort = statsClient.getEventShortDto(eventsEntity);   // заменил получение данных с учетом статистики из бд
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
        List<EventShortDto> eventsShort = statsClient.getEventShortDto(eventsEntity);
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
            eventsShort = statsClient.getEventShortDto(c.getEvents());  // переделал метод получения данных из бд статистики.
            compilationsDto.add(CompilationMapper.mapToCompilationDto(c, eventsShort));
        }
        return compilationsDto;
    }

}
