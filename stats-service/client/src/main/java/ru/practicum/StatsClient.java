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

    // метод для отправки запроса на отправки события в сервис статистики
    public EndpointHitDto addHit(EndpointHitDto endpointHitDto) {
        ParameterizedTypeReference<EndpointHitDto> responseType = new ParameterizedTypeReference<>() {
        };
        return post("/hit", endpointHitDto, responseType).getBody();
    }


    // метод для отправки запроса на получение статистики по событиям
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        try {
            validDates(start, end);
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


