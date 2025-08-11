package ru.practicum;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String API_PREFIX = "/";

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );

    }

    public EndpointHitDto addHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitDto dto =
                EndpointHitDto.builder()
                        .app(app)
                        .uri(uri)
                        .ip(ip)
                        .timestamp(timestamp)
                        .build();
        ParameterizedTypeReference<EndpointHitDto> responseType = new ParameterizedTypeReference<>() {};
       return post("/hit", dto, responseType).getBody();
    }

}
