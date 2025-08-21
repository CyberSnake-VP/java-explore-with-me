package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @Size(min = 20, max = 2000)
    @NotBlank(message = "Field: annotation. Error: must not be blank. Value: null")
    private String annotation;        // краткое описание события

    @NotNull(message = "Field: category. Error: must not be blank. Value: null")
    private Long category;            // id категории к которой относится событие

    @Size(min = 20, max = 7000)
    @NotBlank(message = "Field: description. Error: must not be blank. Value: null")
    private String description;       // Полное описание события

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Field: eventDate. Error: must not be blank. Value: null")
    private LocalDateTime eventDate;  // Дата и время на которые намечено событие в формате "yyyy-MM-dd HH:mm:ss"

    @NotNull(message = "Field: location. Error: must not be blank. Value: null")
    private Location location;

    private Boolean paid;           // Нужно ли оплачивать участие в событии, default false

    @Min(0)
    private Integer participantLimit;  // Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    /** Нужна ли пре-модерация заявок на участие.
     * Если true, то все заявки будут ожидать подтверждения инициатором события.
     * Если false - то будут подтверждаться автоматически.*/
    private Boolean requestModeration;

    @Size(min = 3, max = 120)
    @NotBlank(message = "Field: title. Error: must not be blank. Value: null")
    private String title;          // Заголовок события
}
