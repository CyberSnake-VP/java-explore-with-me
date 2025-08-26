package ru.practicum.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatsClientUtil {
    private final StatsClient statsClient;

    /**
     * Метод для получения списка объектов EventShortDto c количеством просмотров, для формирования обратного dto.
     * Идея в том, что теперь мы будем формировать список из uri каждого события из входящего списка событий.
     * Теперь один раз получаем статистику по этим uri, возьмем от туда кол-во просмотров конкретного события по uri
     */
    public List<EventShortDto> getEventShortDto(List<Event> events) {

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
        // заменил hash map на список
        List<EventShortDto> result = new ArrayList<>();

        /** Переделал логику работы метода, заменил таблицу hash-map, где ключ был event а значение view(кол-во просмотров)
         * Проблема в том, что комменты можно оставлять к одному и тому же событию много раз.
         * Мапа не позволяла хранить одинаковые Event по ключу, заменил на добавление результата сразу итоговый в список.
         * Тем самым получаем список с возможными одинаковыми событиями в условиях комментариев, и с установленным зн-ем просмотров.
         * */
        if (!events.isEmpty()) {
            for (Event e : events) {
                views.stream()
                        .filter(v -> v.getUri().equals("/events/" + e.getId()))
                        .findFirst()
                        .ifPresentOrElse(v -> result.add(EventMapper.mapToShortDto(e, v.getHits())),
                                () -> result.add(EventMapper.mapToShortDto(e, 0L)));
            }
        }

        return result;
    }

    // Метод для получения кол-ва просмотров из сервиса статистики.
    // Не понятно за какой период получать статистику, указал за 365 дней.
    public Long getEventHitView(Event event) {
        // получим выборку в один год от текущей даты.
        LocalDateTime start = LocalDateTime.now().minusDays(365);
        LocalDateTime end = LocalDateTime.now();
        Long eventId = event.getId();
        List<String> uris = new ArrayList<>();
        uris.add("/events/" + eventId);
        List<ViewStatsDto> views = statsClient.getStats(start, end, uris, true);
        Long view = 0L;
        if (!views.isEmpty()) {
            return views.getFirst().getHits();
        }
        return view;
    }

    // метод для записи в сервис статистики данных о просмотренных событиях
    public void addHitEvent(HttpServletRequest servlet, List<Event> events) {
        for (Event e : events) {
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(servlet.getRequestURI() + "/" + e.getId())
                    .ip(servlet.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.addHit(hitDto);
        }
    }

    // метод для записи в сервис статистики данных о просмотренном событии
    public void addHitEvent(HttpServletRequest servlet) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(servlet.getRequestURI())
                .ip(servlet.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.addHit(hitDto);
    }
}
