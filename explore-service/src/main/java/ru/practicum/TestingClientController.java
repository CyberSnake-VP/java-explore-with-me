package ru.practicum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestingClientController {
    private final StatsClient statsClient;
    /** Проверка работы клиента сервиса статистики.*/
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto addHit(HttpServletRequest request) {
        String app = "ewm-service";
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        LocalDateTime time = LocalDateTime.now();
        log.info("POST /controller/test/hit app: {}, ip: {}, uri: {}, time: {}", app, ip, uri, time);
        return statsClient.addHit(app, ip, uri, time);
    }

    @GetMapping("/stats")
    public List<EndpointHitDto> getStats() {
        return List.of();
    }
}
