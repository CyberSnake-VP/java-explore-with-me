package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(final RestTemplate rest) {
        this.rest = rest;
    }

    protected <T, V> ResponseEntity<V> post(String path, T body, ParameterizedTypeReference<V> typeRef) {
        return makeAndSendRequest(HttpMethod.POST, path, null, body, typeRef);
    }

    protected <V> ResponseEntity<V> get(String path, @Nullable Map<String, Object> parameters, ParameterizedTypeReference<V> typeRef) {
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null, typeRef);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private <T, V> ResponseEntity<V> makeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters,
                                                        @Nullable T body, ParameterizedTypeReference<V> typeRef) {
        log.info("StatsClient: Sending request to method:{}, path:{}, parameters:{}, body={}, typeRef={}",
                method, path, parameters, body, typeRef);

        // подготавливаем заголовок и тело запроса.
        HttpEntity<T> entity = new HttpEntity<>(body, defaultHeaders());
        ResponseEntity<V> statsResponse;
        if (parameters != null) {
            /** Получаем ответ, используем метод exchange, в аргументах: путь, метод, подготовленный запрос,
             * ParameterizedTypeReference позволяет методу exchange() напрямую возвращать List<EndpointHidDto>
             * без необходимости приведения типов.
             * Список параметров */
            statsResponse = rest.exchange(path, method, entity, typeRef, parameters);
        } else {
            statsResponse = rest.exchange(path, method, entity, typeRef);
        }
        ResponseEntity<V> response = prepareGatewayResponse(statsResponse);
        log.info("StatsClient: Response status:{}, response body:{}", response.getStatusCode(), response.getBody());
        return response;
    }

    private static <V> ResponseEntity<V> prepareGatewayResponse(ResponseEntity<V> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

}
