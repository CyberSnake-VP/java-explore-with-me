package ru.practicum;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {
    private String app;           // название сервиса
    private String uri;           // URI сервиса
    private Long hits;            // количество просмотров
}
