package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EndpointHitDto {
    private Long id;                            // Идентификатор записи
    @NotBlank(message = "App can't be empty")
    private String app;                         // Идентификатор сервиса, для которого записывается информация
    @NotBlank(message = "Uri can't be empty")
    private String uri;                         // URI, для которого был осуществлен запрос
    @NotBlank(message = "Ip can't be empty")
    private String ip;                          // IP-адрес пользователя, осуществившего запрос
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;            // Дата и время запроса эндпоинта
}
