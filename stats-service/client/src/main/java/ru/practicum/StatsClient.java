package ru.practicum;


import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j

public class StatsClient extends BaseClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String API_PREFIX = "/";
    private final RestClient restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .uriBuilderFactory(new DefaultUriBuilderFactory("http://localhost:9090" + API_PREFIX))
            .build();


    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public EndpointHitDto addHit(EndpointHitDto endpointHitDto) {
        ParameterizedTypeReference<EndpointHitDto> responseType = new ParameterizedTypeReference<>() {
        };
        return post("/hit", endpointHitDto, responseType).getBody();
    }

//    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
//        // проверим даты на валидность.
//        validDates(start, end);
//
//        /** Используем reference<List<ViewStatsDto> чтобы запарсить ответ сразу в список объектов ViewStatsDto*/
//        ParameterizedTypeReference<List<ViewStatsDto>> responseType = new ParameterizedTypeReference<>() {};
//        Map<String, Object> params = new HashMap<>();
//        params.put("start", start.format(DATE_TIME_FORMATTER));
//        params.put("end", end.format(DATE_TIME_FORMATTER));
//        params.put("unique", unique);
//        String uriString;
//        boolean isHasUri = !(Objects.isNull(uris) || uris.isEmpty());
//
//        /** Приходится использовать стрим, чтобы собрать одну строку из списка параметров uris
//         * Дело в том, что для параметров используется map, но у нас тогда ключ будет uris, а значения должны быть разные.
//         * Что не получится сделать.*/
//        if (isHasUri) {
//            uriString = uris.stream().map(uri -> "&uris=" + uri).collect(Collectors.joining());
//                   return get("/stats?start={start}&end={end}" + uriString + "&unique={unique}", params, responseType).getBody();
//        } else {
//                   return get("/stats?start={start}&end={end}&unique={unique}", params, responseType).getBody();
//        }
//
//    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stats")
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("uris", uris)
                            .queryParam("unique", unique)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                    });
        } catch (RestClientException e) {
            log.error("Ошибка при получении статистики");
            System.out.println(e.getMessage());
        }
        return List.of();
    }

    private void validDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException(String.format("Incorrected statistic period. Start: %s, End: %s", start, end));
        }
    }

}


